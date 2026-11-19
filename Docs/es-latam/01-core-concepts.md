# 01 - Conceptos Fundamentales y Arquitectura

Este documento define el paradigma de modo retenido de X4UI, las decisiones de diseño que sustentan su árbol de componentes, el ciclo de vida determinista, la gestión de estado reactivo y los criterios de optimización mediante banderas sucias (*dirty flags*).

---

## 1. El Paradigma de X4UI y Qué Soluciona

En el sistema vanilla de Minecraft (`GuiScreen`), el código de interfaz es imperativo e inmediato: cada fotograma recalcula posiciones, dibuja primitivas crudas y mezcla la lógica de negocio con el renderizado. Esto provoca acoplamiento fuerte, fugas de estado al redimensionar la ventana y código difícil de mantener.

X4UI adopta un **paradigma de modo retenido tipo DOM**:
1. **Separación entre estructura y dibujo:** Los componentes se instancian una sola vez en `initComponents()` y se organizan en un árbol jerárquico (`rootPanel`).
2. **Layout perezoso:** Las coordenadas y dimensiones se recalculan únicamente cuando cambia la estructura o la resolución, nunca en cada fotograma por defecto.
3. **Vistas reactivas y pasivas (*Dumb Views*):** Los datos residen en contenedores observables (`State<T>`) o en `UIStateBus`; la vista se limita a suscribirse y reflejar cambios.
4. **Limpieza automática de memoria:** Al destruir un componente o cerrar la pantalla, X4UI desvincula sus suscripciones reactivas y libera animaciones asociadas.

---

## 2. Árbol Jerárquico de Componentes

Cada elemento visual hereda de `GuiComponent` (patrón Composite). La raíz de toda pantalla es `rootPanel`, un `GuiPanel` que cubre el área lógica de la ventana:

```
rootPanel (GuiPanel)
├── headerPanel (GuiPanel.row)
│   ├── GuiLabel
│   ├── spacer (GuiPanel.spacer)
│   └── GuiButton ("X")
├── contentPanel (GuiScrollPanel)
│   ├── card1 (GuiPanel.card)
│   └── card2 (GuiPanel.card)
└── footerPanel (GuiPanel.row)
    └── GuiSliderInt
```

### Operaciones sobre el Árbol

```java
GuiPanel panel = GuiPanel.column(10, 6).withPercentWidth(1.0f);
GuiButton btn = panel.add(GuiButton.of("Aceptar", () -> {}), b -> b.withPercentWidth(0.5f));

panel.removeChild(btn);    // Separa btn e invoca onDetached()
panel.clearChildren();     // Separa y destruye todos los hijos
```

---

## 3. Ciclo de Vida de `GuiComponent`

El ciclo de vida sigue seis etapas deterministas:

1. **Construcción:** Asignación de propiedades iniciales en el constructor o mediante API fluida (`withPercentWidth`, `withMargin`, `withLayer`).
2. **Adjunción (`onAttached()`):** Se ejecuta al agregarse a un contenedor padre.
3. **Actualización (`update(float deltaTime)`):** Se ejecuta antes de renderizar. Si existen banderas sucias de diseño (`requestLayout()`), resuelve dimensiones porcentuales y posicionamiento del `ILayoutManager`, propagándolo hacia los hijos.
4. **Renderizado (`render(IGraphics gfx, int mouseX, int mouseY, float partialTicks)`):**
   - Invoca `renderSelf(gfx, mouseX, mouseY, partialTicks)`.
   - Si existe bandera sucia de render (`requestRender()`), reordena los hijos por `layer` (Z-index).
   - Invoca `renderChildren(gfx, ...)` aplicando recorte (*clipping*) cuando corresponde.
5. **Separación (`onDetached()`):** Se ejecuta al remover el componente de su padre.
6. **Destrucción (`destroy()` / `onDestroy()`):**
   - Desvincula automáticamente todos los listeners enlazados mediante `bindState()` o `mapState()`.
   - Libera animaciones en `GuiAnimator` y se propaga recursivamente a los hijos.

---

## 4. Propagación de Eventos

Los eventos de mouse (`mouseClicked`, `mouseReleased`, `mouseDragged`, `mouseScrolled`) y teclado (`keyTyped`) recorren el árbol respetando visibilidad y orden de capas (`layer`):
- Los componentes en capas superiores (`layer` mayor) reciben prioridad de captura.
- Cuando un componente consume el evento (retorna `true`), detiene la propagación hacia capas inferiores.

---

## 5. Estado Reactivo (`State<T>`) y Bus de Estados (`UIStateBus`)

### `State<T>`
`com.x4yi.x4ui.common.State<T>` es un contenedor observable seguro para hilos (`volatile` + serialización de notificaciones en `set(T)` para evitar condiciones de carrera entre hilos Netty/I/O y el hilo principal) y compatible con servidor dedicado. Deduplica notificaciones mediante `Objects.equals()` y aísla excepciones por listener para que un fallo en un suscriptor no interrumpa a los demás:

```java
State<Integer> score = new State<>(0);

// Vinculación segura ligada al ciclo de vida del componente
label.bindState(score, val -> label.setText("Puntos: " + val));

score.set(10); // Actualiza la etiqueta automáticamente
```

### `UIStateBus`
Permite compartir estado entre módulos o sincronizar datos desde el servidor sin acoplar referencias directas. Soporta tres alcances:
- `Scope.SCREEN`: se limpia automáticamente al cerrar la pantalla (`GuiBaseScreen.onGuiClosed()`). Es el alcance utilizado por `syncedState(key, valorInicial)`.
- `Scope.SESSION`: persiste entre pantallas abiertas y se limpia al desconectarse del servidor.
- `Scope.GLOBAL`: persiste durante toda la ejecución del cliente.

```java
String key = UIStateBus.scopedKey("machine:10,64,10", "energy");
State<Integer> energy = UIStateBus.getOrCreate(key, 0, UIStateBus.Scope.SCREEN);
```

---

## 6. Sistema de Temas (`ITheme` y `ThemeRegistry`)

La apariencia visual está desacoplada de la lógica de componentes mediante `ITheme`. `DefaultTheme.INSTANCE` provee una paleta oscura sólida y minimalista. Los componentes heredan el tema de su cadena de ancestros o utilizan `ThemeRegistry.getCurrentTheme()` como respaldo:

```java
ThemeRegistry.registerTheme("custom_dark", customTheme);
rootPanel.setTheme(ThemeRegistry.getTheme("custom_dark"));
```

---

## 7. Optimización mediante Dirty Flags, `ScreenHost` y Caché de Coordenadas

- `requestLayout()`: marca que las dimensiones o la jerarquía estructural cambiaron; el layout se recalculará una única vez en el siguiente `update()`.
- `requestRender()`: solicita reordenar las capas visuales (`layer`).
- **Caché de Coordenadas Absolutas:** `GuiComponent` cachea `absX`/`absY` e invalida en cascada solo al mutar `x`, `y` o `parent`, evitando recorrer toda la cadena de padres en cada hit-test o dibujo.
- **`ScreenHost` Unificado:** `GuiBaseScreen` y `GuiBaseContainer` delegan la lógica compartida a `ScreenHost` — seguimiento y expiración unificada de RPCs, `deltaTime`, tooltips multilínea cacheados, animación opcional de apertura independiente de FPS (`setAnimateOpen(boolean)`) y limpieza automática en `onGuiClosed()`.
- **Composición sin Casts:** `GuiComponent.add(T child)` y `add(T child, Consumer<T> config)` (delegados también en `GuiScrollPanel`) retornan el tipo exacto del hijo para evitar casts manuales.
- Las mutaciones puramente visuales (color, opacidad, texto cuando no altera tamaño estructural) no requieren `requestLayout()`.

---

## 8. Qué Evitar al Crear una GUI

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Llamar `addChild()` dentro de `renderSelf()` o `render()` | Crea componentes infinitos por fotograma y satura memoria/GC | Instanciar la jerarquía en `initComponents()` |
| Llamar `requestLayout()` incondicionalmente cada tick | Anula la optimización perezosa y fuerza recálculo completo en cada frame | Invocar solo ante cambios reales de tamaño, visibilidad o hijos |
| Suscribirse manualmente con `state.addListener(...)` sin limpiar | Retiene referencias fuertes al `GuiComponent` y provoca fugas de memoria | Usar `component.bindState(state, callback)` |
| Ejecutar I/O o red síncrona en callbacks de botones | Congela el hilo principal del cliente Minecraft | Delegar en `MediaPipeline`, `CompletableFuture` o `UINetwork` |
