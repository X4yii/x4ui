# Botones e Inputs

Este documento cubre los componentes interactivos de entrada: `GuiButton`, `GuiTextInput`, `GuiToggle`, `GuiSlider` (a nivel de componente) y las variantes tipadas de slider (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`).

---

## GuiButton

Un botón clickeable con estado de hover animado, texto centrado y sonido de clic.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiButton`

### Constructor

```java
new GuiButton(int x, int y, int width, int height, String text, Runnable onClick)
```

El callback `onClick` se invoca al hacer clic izquierdo. Pasa `null` para manejar los clics mediante override.

### Uso Básico

```java
import com.x4yi.x4ui.client.gui.component.GuiButton;

GuiButton saveBtn = new GuiButton(10, 10, 120, 25, "Save", () -> {
    System.out.println("Saved!");
});

rootPanel.addChild(saveBtn);
```

### Texto Dinámico

```java
GuiButton toggleBtn = new GuiButton(10, 40, 120, 25, "Enable", null);

toggleBtn.setOnClick(() -> {
    if ("Enable".equals(toggleBtn.getText())) {
        toggleBtn.setText("Disable");
    } else {
        toggleBtn.setText("Enable");
    }
});
```

### Estado Deshabilitado

```java
GuiButton disabledBtn = new GuiButton(10, 70, 120, 25, "Disabled", null);
disabledBtn.setEnabled(false);
```

Los botones deshabilitados se renderizan con el color de texto deshabilitado del tema y no responden a clics.

### Con Tooltip

```java
GuiButton tooltipBtn = new GuiButton(10, 100, 120, 25, "Hover Me", null);
tooltipBtn.setTooltip("This button does something important");
```

### En un Layout Flex

```java
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

GuiPanel menu = new GuiPanel(10, 10, 200, 200);
menu.setFlexDirection(FlexDirection.VERTICAL);
menu.setGap(5);

menu.addChild(new GuiButton(0, 0, 180, 20, "Option 1", () -> {}));
menu.addChild(new GuiButton(0, 0, 180, 20, "Option 2", () -> {}));
menu.addChild(new GuiButton(0, 0, 180, 20, "Option 3", () -> {}));
```

### Mediante GuiBuilder

```java
import com.x4yi.x4ui.client.gui.utils.GuiBuilder;

GuiButton btn = GuiBuilder.createButton("Built Button")
    .position(10, 10)
    .size(120, 25)
    .tooltip("Built with GuiBuilder")
    .onClick(() -> System.out.println("Clicked!"))
    .build();
```

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getText()` | `String` | Devuelve el texto del botón |
| `setText(String)` | `void` | Actualiza el texto del botón |
| `setOnClick(Runnable)` | `void` | Establece el callback de clic |

### Renderizado

- Color de fondo: `lerpColor(theme.getButtonBackgroundColor(), theme.getButtonHoverColor(), hoverLerp)`
- Borde: `lerpColor(theme.getBackgroundColor(), theme.getPrimaryColor(), hoverLerp)`
- Texto: `theme.getTextColor()` cuando está habilitado, `theme.getDisabledTextColor()` cuando está deshabilitado
- Animación de hover: `hoverLerp` interpola de 0→1 en el tiempo usando delta time

---

## GuiTextInput

Un campo de entrada de texto completo con cursor, selección, operaciones de portapapeles, texto de placeholder y manejo de tecla de envío.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiTextInput`

### Constructor

```java
new GuiTextInput(int x, int y, int width, int height)
```

### Uso Básico

```java
import com.x4yi.x4ui.client.gui.component.GuiTextInput;

GuiTextInput nameInput = new GuiTextInput(10, 10, 200, 20);
nameInput.setPlaceholder("Enter your name...");
nameInput.setMaxLength(32);

rootPanel.addChild(nameInput);
```

### Leer el Valor

```java
String currentValue = nameInput.getText();
```

### Envío con Enter

```java
GuiTextInput searchInput = new GuiTextInput(10, 40, 200, 20);
searchInput.setPlaceholder("Search...");
searchInput.onEnter(() -> {
    System.out.println("Searching for: " + searchInput.getText());
});
```

### Tecla de Envío Personalizada

```java
import org.lwjgl.input.Keyboard;

GuiTextInput tabInput = new GuiTextInput(10, 70, 200, 20);
tabInput.setSubmitKey(Keyboard.KEY_TAB);
tabInput.onEnter(() -> System.out.println("Tab pressed"));
```

### Establecimiento Programático de Texto

```java
GuiTextInput field = new GuiTextInput(10, 100, 200, 20);
field.setText("Initial value");
```

### Atajos de Teclado

La entrada de texto soporta:
- **Ctrl+A** -- Seleccionar todo
- **Ctrl+C** -- Copiar selección al portapapeles
- **Ctrl+V** -- Pegar del portapapeles
- **Ctrl+X** -- Cortar selección al portapapeles
- **Backspace** -- Eliminar carácter antes del cursor (Ctrl+Backspace elimina palabra)
- **Delete** -- Eliminar carácter después del cursor
- **Flechas izquierda/derecha** -- Mover cursor (Shift+Flecha para selección)
- **Home/End** -- Mover al inicio/fin del texto

### Comportamiento de Enfoque

- Hacer clic en la entrada de texto le da enfoque
- Hacer clic fuera limpia el enfoque
- Solo un componente puede estar enfocado a la vez en toda la pantalla
- El cursor parpadea cuando está enfocado (controlado por `cursorCounter`)

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getText()` | `String` | Devuelve el texto actual |
| `setText(String)` | `void` | Establece el texto y mueve el cursor al final |
| `setPlaceholder(String)` | `void` | Establece el texto de placeholder (se muestra cuando está vacío y sin enfoque) |
| `setMaxLength(int)` | `void` | Establece el número máximo de caracteres (predeterminado: 256) |
| `setSubmitKey(int)` | `GuiTextInput` | Establece el código de tecla que activa el envío (predeterminado: Enter) |
| `onEnter(Runnable)` | `GuiTextInput` | Establece el callback de envío |

---

## GuiToggle

Un interruptor tipo iOS con transición de color animada e insignia ON/OFF.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiToggle`

### Constructor

```java
new GuiToggle(int x, int y, int width, int height, String label, boolean defaultState, Consumer<Boolean> onStateChanged)
```

### Uso Básico

```java
import com.x4yi.x4ui.client.gui.component.GuiToggle;

GuiToggle darkMode = new GuiToggle(10, 10, 150, 20, "Dark Mode", true, (enabled) -> {
    System.out.println("Dark mode: " + enabled);
});

rootPanel.addChild(darkMode);
```

### Leer y Establecer Estado

```java
boolean currentState = darkMode.getState();
darkMode.setState(false);
```

### Etiqueta Dinámica

```java
GuiToggle volumeToggle = new GuiToggle(10, 40, 150, 20, "Sound", false, (on) -> {
    volumeToggle.setLabel(on ? "Sound: ON" : "Sound: OFF");
});
```

### En un Layout Flex

```java
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

GuiPanel settings = new GuiPanel(10, 10, 200, 200);
settings.setFlexDirection(FlexDirection.VERTICAL);
settings.setGap(5);

settings.addChild(new GuiToggle(0, 0, 180, 20, "Notifications", true, null));
settings.addChild(new GuiToggle(0, 0, 180, 20, "Auto-save", false, null));
settings.addChild(new GuiToggle(0, 0, 180, 20, "Sounds", true, null));
```

### Renderizado

- Insignia: `lerpColor(0xFF424248, 0xFF00C853, animationLerp)` -- de gris a verde
- Texto de insignia: `lerpColor(0xFFA0A0A5, 0xFFFFFFFF, animationLerp)` -- muestra "ON" o "OFF"
- Etiqueta: `0xFFFFFFFF` cuando está en hover, `0xFFB0B0BB` en caso contrario
- Sonido de clic: `SoundEvents.UI_BUTTON_CLICK`

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getState()` | `boolean` | Devuelve el estado actual del interruptor |
| `setState(boolean)` | `void` | Establece el estado (sin activar callback) |
| `getLabel()` | `String` | Devuelve el texto de la etiqueta |
| `setLabel(String)` | `void` | Actualiza el texto de la etiqueta |

---

## GuiSlider (A Nivel de Componente)

Un slider compuesto con etiqueta, pista draggable y un campo de texto editable para entrada directa de valores.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiSlider`

### Constructores

```java
// Modo float
new GuiSlider(int x, int y, int width, int height, String label, float min, float max, float current, Consumer<Float> onValueChanged)

// Modo integer
new GuiSlider(int x, int y, int width, int height, String label, float min, float max, float current, boolean isInt, Consumer<Float> onValueChanged)
```

### Uso Básico

```java
import com.x4yi.x4ui.client.gui.component.GuiSlider;

// Slider float
GuiSlider volumeSlider = new GuiSlider(10, 10, 200, 20, "Volume", 0f, 1f, 0.8f, (value) -> {
    System.out.println("Volume: " + value);
});

// Slider integer
GuiSlider countSlider = new GuiSlider(10, 40, 200, 20, "Count", 0, 100, 25, true, (value) -> {
    System.out.println("Count: " + value.intValue());
});

rootPanel.addChild(volumeSlider);
rootPanel.addChild(countSlider);
```

### Leer y Establecer Valor

```java
float currentValue = volumeSlider.getValue();
volumeSlider.setValue(0.5f);
```

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getValue()` | `float` | Devuelve el valor actual |
| `setValue(float)` | `void` | Establece el valor y actualiza la UI |
| `getLabel()` | `String` | Devuelve el texto de la etiqueta |
| `setLabel(String)` | `void` | Actualiza el texto de la etiqueta |
| `setOnValueChanged(Consumer<Float>)` | `void` | Establece el callback de cambio |

### Renderizado

- Etiqueta: Dibujada a la izquierda del componente
- Campo de texto: Dibuja el valor actual, editable al hacer clic
- Pista: `theme.getSliderTrackColor()`
- Relleno: `theme.getSliderFillColor()`, ancho proporcional al valor
- Mango: `theme.getSliderHandleColor()` (o `getSliderHandleHoverColor()` cuando está en hover)

### Comportamiento

- Arrastrar la pista del slider actualiza el valor en tiempo real
- Hacer clic en el campo de texto permite entrada numérica directa
- La tecla Enter en el campo de texto aplica el valor escrito
- Solo un campo de texto está activo globalmente (rastreado mediante `activeTextField` estático)

---

## Sliders Tipados (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`)

Variantes de slider类型安全 que usan `State<T>` para enlace reactivo. Son más simples que el `GuiSlider` a nivel de componente (sin etiqueta ni campo de texto).

**Paquete:** `com.x4yi.x4ui.client.gui.component.slider`

### GuiSliderInt

```java
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderInt;
import com.x4yi.x4ui.common.State;

State<Integer> volume = new State<>(50);

GuiSliderInt slider = new GuiSliderInt(10, 10, 150, 15, volume, 0, 100, 5);
rootPanel.addChild(slider);

// Escuchar cambios
volume.addListener(v -> System.out.println("Volume: " + v));
```

### GuiSliderFloat

```java
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderFloat;
import com.x4yi.x4ui.common.State;

State<Float> brightness = new State<>(0.75f);

GuiSliderFloat slider = new GuiSliderFloat(10, 30, 150, 15, brightness, 0f, 1f, 0.05f);
rootPanel.addChild(slider);
```

### GuiSliderDouble

```java
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderDouble;
import com.x4yi.x4ui.common.State;

State<Double> precision = new State<>(3.14);

GuiSliderDouble slider = new GuiSliderDouble(10, 50, 150, 15, precision, 0.0, 10.0, 0.01);
rootPanel.addChild(slider);
```

### Mediante GuiBuilder

```java
import com.x4yi.x4ui.client.gui.utils.GuiBuilder;
import com.x4yi.x4ui.common.State;

State<Integer> speed = new State<>(10);

GuiSliderInt slider = GuiBuilder.createSliderInt(speed, 1, 50, 1)
    .position(10, 10)
    .size(150, 15)
    .build();
```

### Constructor

```java
new GuiSliderInt(int x, int y, int width, int height, State<Integer> state, int min, int max, int step)
new GuiSliderFloat(int x, int y, int width, int height, State<Float> state, float min, float max, float step)
new GuiSliderDouble(int x, int y, int width, int height, State<Double> state, double min, double max, double step)
```

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getState()` | `State<T>` | Devuelve el estado enlazado |
| `setHorizontal(boolean)` | `void` | Establece orientación horizontal (predeterminada) o vertical |

### Renderizado

- Pista: `theme.getSliderTrackColor()`
- Relleno: `theme.getSliderFillColor()`, ancho proporcional al porcentaje
- Mango: `theme.getSliderHandleColor()` (o `getSliderHandleHoverColor()` cuando está en hover)

### Comportamiento

- Arrastrar el mango para cambiar el valor
- El valor se ajusta al incremento de `step` más cercano
- El valor se limita a `[min, max]`
- Los cambios se propagan a través de `State<T>` a todos los listeners