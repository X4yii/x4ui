# 04 - Diseño Responsivo y Posicionamiento Relativo (GUI Scale 1x a 4x)

Este documento describe el estándar oficial de diseño responsivo en X4UI. Con la adopción obligatoria de `FlexLayout` y tamaños porcentuales, garantizamos que las interfaces permanezcan perfectamente legibles y alineadas bajo cualquier resolución y factor de escala de Minecraft Forge 1.12.2 (GUI Scale 1, 2, 3, 4 y Auto).

---

## 1. El Problema del Escalado en Minecraft

Minecraft calcula las dimensiones lógicas de la pantalla (`ScaledResolution`) dividiendo la resolución en píxeles reales de la ventana entre el factor de escala:

$$\text{scaledWidth} = \left\lfloor \frac{\text{displayWidth}}{\text{scaleFactor}} \right\rfloor, \quad \text{scaledHeight} = \left\lfloor \frac{\text{displayHeight}}{\text{scaleFactor}} \right\rfloor$$

Si utiliza dimensiones absolutas y coordenadas fijas `(X, Y)`, su interfaz fallará y se cortará en pantallas de escala grande (Scale 3 y 4). Es por esto que el posicionamiento fijo se considera un **antipatrón severo**.

---

## 2. Los Cuatro Pilares del Estándar Responsivo X4UI

Para construir UIs verdaderamente robustas y escalables, se deben seguir estas cuatro reglas de oro:

---

### Pilar 1: Dimensionamiento Porcentual Fluido en `GuiComponent`

`GuiComponent` cuenta con soporte nativo para dimensiones y coordenadas dinámicamente relativas al componente padre mediante la API fluida:

```java
// Ocupar el 100% del ancho del contenedor padre
card.withPercentWidth(1.0f);

// Dividir en dos columnas al 50% de ancho
colLeft.withPercentWidth(0.5f);
colRight.withPercentWidth(0.5f);

// Fijar ancho y alto porcentuales simultáneamente
modal.withPercentSize(0.8f, 0.6f); // 80% ancho, 60% alto

// Posicionamiento porcentual manual (centrado al 50% X)
badge.withPercentPosition(0.5f, 0.1f);
```

#### Métodos Fluent Responsivos:
- `.withPercentWidth(float percent)`
- `.withPercentHeight(float percent)`
- `.withPercentSize(float percentWidth, float percentHeight)`
- `.withPercentPosition(float percentX, float percentY)`

*Nota: Pasar `-1.0f` desactiva el cálculo porcentual.*

---

### Pilar 2: `FlexLayout`, Alineación (`FlexAlign`) y Primitivas Fluentes

Siempre que agrupe varios elementos, utilice los constructores semánticos de `GuiPanel` en lugar de calcular coordenadas manuales:

```java
// Ventana modal centrada estándar (con dimBackground automático en GuiBaseScreen)
GuiPanel window = createCenteredWindow(420, 300);

// Encabezado en fila con título a la izquierda y botón cerrar a la derecha
GuiPanel header = GuiPanel.row(8);
header.addChild(new GuiLabel(0, 0, "Panel de Control", 0xFFFFFFFF));
header.addChild(GuiPanel.spacer());
header.addChild(new GuiButton(0, 0, 20, 20, "X", this::closeScreen));

// Tarjeta de sección con tema, padding y altura automática (hugContent)
GuiPanel section = GuiPanel.card(10, 6);
```

#### Alineación con `FlexAlign`:
`FlexLayout` soporta `FlexAlign.START`, `CENTER`, `END` y `STRETCH` tanto en el eje transversal (`setAlignItems`) como en el eje principal (`setJustifyContent`), además de `GuiPanel.spacer()` para empujar elementos a los extremos.

#### Control de Tamaño del Contenedor Padre (`hugContent`):
Por defecto en `r1.0b5`, `FlexLayout` es **no destructivo** (`hugContent = false`): respeta fielmente el tamaño del contenedor padre si este tiene dimensiones fijas o responsivas por porcentaje (`withPercentWidth`/`withPercentHeight`).

Si deseas que el contenedor padre **abrace y adapte automáticamente su tamaño** a la suma total de sus hijos (comportamiento "Hug / Fit Content"), debes indicarlo explícitamente:

```java
// El contenedor padre ajustará su altura/ancho exactamente al contenido de sus hijos
FlexLayout layout = new FlexLayout(FlexDirection.VERTICAL, 8, false)
    .withHugContent(true);

panel.setLayoutManager(layout);
```

> [!NOTE]
> - Si `hugContent` es `false` (por defecto): el panel padre mantiene su tamaño asignado (o porcentaje), ideal para modales centrados, sidebars fijos o vistas divididas.
> - Si `hugContent` es `true`: el panel padre se expande o contrae dinámicamente según la cantidad y tamaño de sus hijos visibles, ideal para tarjetas dinámicas, popups flotantes o paneles de items dentro de un scroll.

> [!WARNING]
> **El Bucle de Tamaños en FlexLayout con Hug Content:**
> Si un contenedor usa `FlexDirection.VERTICAL` con `hugContent(true)`, el contenedor calculará su altura sumando la altura de todos sus hijos. Por ende, los hijos **no pueden** usar `withPercentHeight(...)` (un hijo no puede tomar el 100% de la altura de un padre que a su vez está tratando de calcular su altura en base al hijo).
> 
> **Regla universal para FlexLayout VERTICAL:**
> Los contenedores deben usar `.withPercentWidth(1.0f)` para expandirse horizontalmente. Los hijos de su interior también deben usar `.withPercentWidth(1.0f)` (o fracciones). **El alto (height) de los hijos debe ser absoluto (en píxeles)** para que el motor apile todo verticalmente.

---

### Pilar 3: Contención Obligatoria en `GuiScrollPanel`

**Regla de Oro:** *Todo contenido vertical (listas, formularios, flujos de paneles) debe residir dentro de un `GuiScrollPanel`.*

En GUI Scale 1x y 2x, la altura de la pantalla es grande y el `GuiScrollPanel` no mostrará barra de desplazamiento. En GUI Scale 4x, la altura disponible puede ser minúscula y el scroll se activará automáticamente, asegurando accesibilidad al 100% del contenido.

**Cuidado con el Ancho en Scrolls:** 
El contenedor interno de un `GuiScrollPanel` se expone mediante `.getContentPanel()`. Para asegurar que todo su interior crezca responsivamente, use la regla del Pilar 2:

```java
// Scroll vertical responsivo listo para usar (contentPanel ya usa VERTICAL, hugContent=true y 100% ancho)
GuiScrollPanel scrollPanel = GuiScrollPanel.vertical(180, 6);
```

---

### Pilar 4: Nunca Destruir la Jerarquía en OnScreenResize

**Antipatrón estrictamente prohibido:** Sobreescribir `onScreenResize()` en el *Screen* principal y ejecutar un método como `initComponents()` que borre todos los componentes y los vuelva a instanciar de cero usando `this.width` para darles un nuevo tamaño.

Gracias a `FlexLayout` y `withPercentWidth`, los contenedores recalcularán y acomodarán absolutamente todos sus hijos de manera automática cuando el jugador redimensiona la pantalla, preservando el estado de inputs de texto, posiciones de scroll y focos de teclado sin interrupción.

---

## Qué Evitar Hacer (Antipatrones Críticos Responsivos)

> [!CAUTION]
> **1. NUNCA usar Coordenadas Absolutas (X,Y) para posicionamiento estructural.**
> Las declaraciones como `new GuiButton(340, 150, ...)` provocarán superposiciones y pantallas rotas. Use anchos de `0` y deléguele la responsabilidad a `FlexLayout` y `.withPercentWidth()`.

> [!CAUTION]
> **2. NUNCA purgar y recrear la interfaz durante `onScreenResize`.**
> Recrear miles de componentes interrumpe ciclos de vida, cancela `GuiAnimator`s y causa fugas de estado si no se maneja bien. Confíe en el motor de diseño.

> [!WARNING]
> **3. NUNCA crear un layout Horizontal con Wrap (FlexWrap = true) sin Heights explícitos.**
> Si sus componentes envuelven a una nueva línea, la fila entera colapsará visualmente si no ha definido la altura base absoluta de cada elemento en la fila.
