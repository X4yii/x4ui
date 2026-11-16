# 04 - Diseño Responsivo y Posicionamiento Relativo (GUI Scale 1x a 4x)

Este documento establece el estándar oficial de diseño responsivo de X4UI, diseñado para que toda interfaz conserve legibilidad, alineación y accesibilidad bajo cualquier resolución y factor de escala (`GUI Scale` 1, 2, 3, 4 y Auto) en Forge 1.12.2 y CleanroomMC.

---

## 1. El Problema del Escalado en Minecraft

Minecraft calcula las dimensiones lógicas de la pantalla (`ScaledResolution`) dividiendo la resolución real en píxeles de la ventana entre el factor de escala:

$$\text{scaledWidth} = \left\lfloor \frac{\text{displayWidth}}{\text{scaleFactor}} \right\rfloor, \quad \text{scaledHeight} = \left\lfloor \frac{\text{displayHeight}}{\text{scaleFactor}} \right\rfloor$$

Una ventana en `1920x1080` pasa de `960x540` en Scale 2 a `480x270` en Scale 4. Cualquier interfaz basada en coordenadas `(X, Y)` o anchos fijos superiores al área lógica se recorta o superpone.

---

## 2. Los Cuatro Pilares del Estándar Responsivo X4UI

### Pilar 1: Dimensionamiento Porcentual en `GuiComponent`

`GuiComponent` resuelve dimensiones y posiciones relativas al padre mediante API fluida (`-1.0f` desactiva el cálculo porcentual):

```java
card.withPercentWidth(1.0f);         // 100% del ancho disponible del padre
colLeft.withPercentWidth(0.5f);      // 50% del ancho disponible
modal.withPercentSize(0.8f, 0.6f);   // 80% ancho, 60% alto
badge.withPercentPosition(0.5f, 0.1f);
```

### Pilar 2: `FlexLayout`, Alineación (`FlexAlign`) y Primitivas de `GuiPanel`

Los contenedores se construyen mediante las primitivas semánticas de `GuiPanel` y `GuiBaseScreen` en lugar de calcular coordenadas manuales:

```java
// Ventana modal centrada con atenuación de fondo (dimBackground)
GuiPanel window = createCenteredWindow(420, 300);

// Fila de encabezado con título a la izquierda y cierre a la derecha
GuiPanel header = GuiPanel.row(8);
header.addChild(new GuiLabel(0, 0, "Panel de Control", 0xFFFFFFFF));
header.addChild(GuiPanel.spacer());
header.addChild(new GuiButton(0, 0, 20, 20, "X", this::closeScreen));

// Tarjeta temática con padding y altura automática (hugContent)
GuiPanel section = GuiPanel.card(10, 6);
```

- **Alineación (`FlexAlign`):** `START`, `CENTER`, `END` y `STRETCH` en eje transversal (`setAlignItems`) y eje principal (`setJustifyContent`), complementados con `GuiPanel.spacer()`.
- **Control `hugContent`:** Por defecto `FlexLayout` es **no destructivo** (`hugContent = false`) y respeta el tamaño asignado al padre. Activar `.withHugContent(true)` hace que el contenedor ajuste su tamaño a la suma de sus hijos visibles.
- **Regla de bucle en `VERTICAL` + `hugContent(true)`:** Si la altura del contenedor depende de sus hijos, los hijos deben definir un alto absoluto en píxeles (no `withPercentHeight(...)`) y usar `withPercentWidth(...)` para adaptarse horizontalmente.

### Pilar 3: Contención Obligatoria en `GuiScrollPanel`

Todo contenido vertical extenso (listas, formularios, secciones apiladas) debe residir dentro de un `GuiScrollPanel`:

```java
// Scroll vertical preconfigurado (contentPanel VERTICAL, hugContent=true, 100% ancho)
GuiScrollPanel scrollPanel = GuiScrollPanel.vertical(180, 6);
```

`GuiScrollPanel` reserva automáticamente el ancho de su barra de desplazamiento cuando el contenido excede el viewport, evitando solapamientos sobre el borde derecho en escalas 3x y 4x.

### Pilar 4: Preservación del Árbol en `onScreenResize`

Nunca se debe limpiar y recrear la jerarquía de componentes dentro de `onScreenResize()`. Al cambiar el tamaño de la ventana, `FlexLayout` y `withPercentWidth` recalculan posiciones y dimensiones preservando el texto ingresado, el foco de teclado y la posición de scroll.

---

## 3. Qué Evitar al Diseñar Layouts

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Coordenadas absolutas `(X, Y)` para posicionamiento estructural | Se recorta o desborda en GUI Scale 3x y 4x | Usar `0` en `X, Y`, `FlexLayout` y `withPercentWidth()` |
| Llamar `initComponents()` o `clearChildren()` en `onScreenResize()` | Destruye foco, estado de inputs, scroll y cancela animaciones | Confiar en la propagación automática de `FlexLayout` |
| Usar `withPercentHeight()` dentro de un contenedor `VERTICAL` con `hugContent(true)` | Dependencia circular entre el alto del padre y el del hijo | Definir alto absoluto en los hijos y usar `withPercentWidth()` |
| Usar `FlexWrap = true` sin altos definidos por elemento | La fila envuelta colapsa visualmente a altura `0` | Asignar altura explícita en cada elemento de la fila |
