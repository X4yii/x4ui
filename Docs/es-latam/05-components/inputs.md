# Inputs (Entradas de Texto y Sliders)

Componentes para que el usuario escriba texto o seleccione valores deslizando.

## GuiTextInput

Reemplaza al `GuiTextField` clásico de Minecraft. Permite escribir, seleccionar y editar texto de forma moderna.

### Creación
```java
GuiTextInput input = new GuiTextInput(x, y, w, h);
input.setPlaceholder("Escribe el nombre del Waypoint...");
input.setMaxLength(32);
```

### Comportamiento
- Solo detecta el teclado cuando le haces clic para enfocarlo.
- Soporta atajos normales como copiar (`Ctrl+C`), pegar (`Ctrl+V`) y cortar (`Ctrl+X`).
- Puedes seleccionar partes del texto manteniendo pulsado `Shift` más las flechas direccionales.

## Sliders (`GuiSlider`)

Barras deslizantes para ajustar números.

Existen tres tipos dependiendo del tipo de número que necesites:
- `GuiSliderInt`: Para números enteros (Ej: 1, 2, 50).
- `GuiSliderFloat`: Para números decimales pequeños.
- `GuiSliderDouble`: Para números decimales precisos.

### Creación
```java
State<Integer> distanciaRender = new State<>(8);
// (X, Y, Ancho, Alto, Estado, Mínimo, Máximo, Salto)
GuiSliderInt slider = new GuiSliderInt(0, 0, 100, 20, distanciaRender, 2, 32, 1);
```

### Comportamiento
- Cuando arrastras la barra con el ratón, calcula el valor según dónde soltaste.
- Actualiza el `State` automáticamente, lo que avisará a tu código del nuevo valor en tiempo real, sin requerir listeners extra manuales.

> **Nota:** Hay dos sistemas de sliders en X4UI:
> - **`GuiSlider`** (en `component`): Un slider con label y campo de texto. Usa `Consumer<Float>` para callbacks. Ideal para menús de configuración.
> - **`GuiSlider<T>`** (en `component.slider`): Un slider genérico minimalista respaldado por `State<T>`. Subclases: `GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`. Estos se sincronizan automáticamente con el State y actualizan la posición visual cuando el State cambia externamente.
