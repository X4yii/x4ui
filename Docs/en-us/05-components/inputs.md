# Inputs (Text Fields and Sliders)

Components for the user to type text or select values by sliding.

## GuiTextInput

Replaces the classic Minecraft `GuiTextField`. Allows typing, selecting, and editing text in a modern way.

### Creation
```java
GuiTextInput input = new GuiTextInput(x, y, w, h);
input.setPlaceholder("Type Waypoint name...");
input.setMaxLength(32);
```

### Behavior
- It only detects the keyboard when you click it to focus.
- Supports normal shortcuts like copy (`Ctrl+C`), paste (`Ctrl+V`), and cut (`Ctrl+X`).
- You can select parts of the text by holding `Shift` plus the arrow keys.

## Sliders (`GuiSlider`)

Sliding bars to adjust numbers.

There are three types depending on the kind of number you need:
- `GuiSliderInt`: For whole numbers (e.g. 1, 2, 50).
- `GuiSliderFloat`: For small decimal numbers.
- `GuiSliderDouble`: For highly precise decimal numbers.

### Creation
```java
State<Integer> renderDistance = new State<>(8);
// (X, Y, Width, Height, State, Minimum, Maximum, Step)
GuiSliderInt slider = new GuiSliderInt(0, 0, 100, 20, renderDistance, 2, 32, 1);
```

### Behavior
- When dragging the bar with the mouse, it calculates the value based on where you let go.
- Automatically updates the `State`, notifying your code of the new value in real-time, without requiring extra manual listeners.

> **Note:** There are two slider systems in X4UI:
> - **`GuiSlider`** (in `component`): A label-based slider with a text input field. Uses `Consumer<Float>` for callbacks. Good for configuration menus.
> - **`GuiSlider<T>`** (in `component.slider`): A minimal generic slider backed by `State<T>`. Subclasses: `GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`. These automatically sync with the State and update the visual position when the State changes externally.
