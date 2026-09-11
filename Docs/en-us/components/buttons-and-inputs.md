# Buttons and Inputs

Primary interaction components are designed to fully leverage the **Fluent API** and eliminate the need to clutter the code with repetitive setter calls.

## GuiButton

The classic interactive button. Secondary visual properties and variants are configured through fluent builders:

```java
GuiButton btn = (GuiButton) new GuiButton(0, 0, 0, 20, "Save", () -> {
    System.out.println("Saved!");
})
.withPercentWidth(1.0f)                           // Responsive sizing
.withVariant(GuiButton.Variant.SUCCESS)            // Visual variant from theme (DEFAULT, PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST)
.withTooltip("Saves changes permanently");         // Instant hover tooltip
```

## Text Inputs (GuiTextInput)

`GuiTextInput` internally handles focus, blinking cursor rendering, text selection, and clipboard operations.

```java
GuiTextInput nameInput = (GuiTextInput) new GuiTextInput(0, 0, 0, 20, "")
    .withPercentWidth(1.0f);

// Configure placeholder
nameInput.setPlaceholder("Enter your name...");

// Listen for text changes
nameInput.setOnTextChanged(text -> {
    System.out.println("Current text: " + text);
});
```

### Reactive States
To bind `GuiTextInput` to a reactive `State<String>`:

```java
State<String> userName = new State<>("");
nameInput.bindState(userName, txt -> {
    nameInput.setText(txt);
});
```

## Sliders and Toggles

### Numeric Sliders (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`)

Allow continuous value adjustments via mouse drag or keyboard navigation:

```java
GuiSliderInt volumeSlider = (GuiSliderInt) new GuiSliderInt(0, 0, 0, 20, 0, 100, 50, "Volume: ")
    .withPercentWidth(1.0f);

volumeSlider.setOnValueChanged(val -> {
    System.out.println("New volume: " + val);
});
```

### Switches and Checkboxes (`GuiToggle`)

`GuiToggle` provides a smooth, animated boolean switch with label support:

```java
GuiToggle showHUD = (GuiToggle) new GuiToggle(0, 0, 120, 20, "Show HUD", true);

showHUD.setOnToggle(enabled -> {
    System.out.println("HUD enabled: " + enabled);
});
```
