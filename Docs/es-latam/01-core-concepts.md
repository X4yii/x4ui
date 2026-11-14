# 01 - Conceptos Fundamentales y Arquitectura

Este documento describe la arquitectura central de X4UI: la filosofía del framework, el árbol jerárquico de componentes, el ciclo de vida estandarizado, el sistema de diseño flexbox, la propagación de eventos, el estado reactivo (`State<T>`), y los antipatrones que debes evitar a toda costa.

---

## 2. Árbol Jerárquico de Componentes

X4UI organiza la interfaz de usuario en un árbol jerárquico de modo retenido (patrón Composite). Cada elemento visual hereda de `GuiComponent`. La raíz de la jerarquía es `rootPanel` (un `GuiPanel` que abarca toda la pantalla).

```
rootPanel (GuiPanel - Flex Vertical, Ancho 100%)
├── headerPanel (GuiPanel - Flex Horizontal)
│   ├── GuiLabel "Título"
│   └── GuiButton "[X]" (Cerrar)
├── contentPanel (GuiScrollPanel - Viewport responsivo)
│   ├── card1 (GuiPanel)
│   └── card2 (GuiPanel)
└── footerPanel (GuiPanel - Barra inferior)
    └── GuiSliderInt
```

### Operaciones del Árbol

```java
GuiPanel panel = new GuiPanel(0, 0, 0, 300).withPercentWidth(1.0f);
GuiButton btn = (GuiButton) new GuiButton(0, 0, 0, 20, "Aceptar", () -> {}).withPercentWidth(0.5f);

panel.addChild(btn);       // Agrega btn y dispara onAttached()
panel.removeChild(btn);    // Remueve btn y dispara onDetached()
panel.clearChildren();     // Remueve y destruye todos los hijos
```

---

## 3. Ciclo de Vida del Componente (`GuiComponent`)

El ciclo de vida en X4UI está estandarizado en etapas deterministas:

1. **Construcción:** Se establecen las propiedades iniciales en el constructor o mediante la API fluida (`.withPercentWidth()`, `.withVariant()`).
2. **Adjunción (`onAttached()`):** Invocado al agregarse a un contenedor padre.
3. **Actualización de Cuadro (`update(float deltaTime)`):** Se ejecuta en cada fotograma antes del renderizado. Si hay flags sucias (`requestLayout()`), se resuelve el diseño y los anchos porcentuales basados en el padre, propagándose a todos los hijos.
4. **Renderizado (`render(IGraphics gfx, int mouseX, int mouseY, float partialTicks)`):**
   - Llama a `renderSelf(gfx, mouseX, mouseY, partialTicks)`.
   - Ordena los hijos por capa (`layer`) y llama a `renderChildren(gfx, ...)` aplicando recorte (*clipping*).
5. **Separación (`onDetached()`):** Se invoca al removerse del padre.
6. **Destrucción (`onDestroy()` / `destroy()`):**
   - Se desvinculan automáticamente todos los listeners reactivos enlazados mediante `bindState()` o `mapState()`.
   - Se liberan referencias débiles en `GuiAnimator` y se propaga a los hijos.

---

## 4. Estado Reactivo (`State<T>`)

`State<T>` (en `com.x4yi.x4ui.common`) es un contenedor reactivo seguro para hilos que notifica a los suscriptores cuando su valor muta. Deduplica actualizaciones mediante `Objects.equals()`.

```java
import com.x4yi.x4ui.common.State;

State<Integer> score = new State<>(0);

// Enlace seguro al ciclo de vida del componente
label.bindState(score, val -> label.setText("Puntos: " + val));

score.set(10); // Actualiza la etiqueta automáticamente
```

---

## 5. Bus de Estados (`UIStateBus`)

Para comunicar módulos o sincronizar datos con el servidor sin acoplamiento directo, `UIStateBus` soporta tres alcances:
- `Scope.SCREEN`: se limpia automáticamente al cerrar la pantalla (`GuiBaseScreen.onGuiClosed()`), ideal con `syncedState(key, valor)`.
- `Scope.SESSION`: sobrevive al cierre de pantallas y se limpia al desconectarse del servidor.
- `Scope.GLOBAL`: persiste durante toda la ejecución del cliente.

```java
// Estado por pantalla con namespace (evita colisiones entre bloques/entidades)
String key = UIStateBus.scopedKey("tile:10,64,10", "energy");
State<Integer> energy = UIStateBus.getOrCreate(key, 0, UIStateBus.Scope.SCREEN);

// Estado global compartido
State<Double> power = UIStateBus.getOrCreate("mimod:power", 1.0, UIStateBus.Scope.GLOBAL);
```

---

## 6. Sistema de Temas (`ITheme` y `ThemeRegistry`)

Los estilos visuales están encapsulados en la interfaz `ITheme`. `DefaultTheme.INSTANCE` provee una paleta oscura estándar. Los componentes heredan el tema de su cadena de ancestros o de `ThemeRegistry.getCurrentTheme()`.

```java
ThemeRegistry.registerTheme("mi_tema_oscuro", customTheme);
rootPanel.setTheme(ThemeRegistry.getTheme("mi_tema_oscuro"));
```

---

## 7. Banderas de Actualización (Dirty Flags)

- `requestLayout()`: Solicita recalcular dimensiones y posiciones en el próximo cuadro.
- `requestRender()`: Solicita reordenar las capas visuales (`layer`).
- Cambios puramente visuales (color, alfa) NO requieren llamar a `requestLayout()`.

---

## Qué Evitar Hacer (Antipatrones Críticos)

> [!CAUTION]
> **1. NUNCA agregar hijos (`addChild()`) dentro de `renderSelf()` o `render()`.**
> Instancie y agregue los componentes únicamente al inicializar la vista, nunca durante un fotograma de dibujo.

> [!WARNING]
> **2. NUNCA llamar a `requestLayout()` incondicionalmente en cada tick.**
> Hágalo solo cuando una propiedad estructural (ancho, alto, visibilidad, separación) realmente cambie.

> [!WARNING]
> **3. NUNCA suscribirse manualmente a `State.addListener()` sin desuscribirse.**
> Utilice siempre `component.bindState()`, que asegura la limpieza de listeners automáticamente cuando el componente muere.

> [!IMPORTANT]
> **4. NUNCA bloquear el hilo principal de Minecraft.**
> No realice peticiones de red o disco bloqueantes en callbacks de botones. Utilice `MediaPipeline` (con `CompletableFuture`) o hilos secundarios.
