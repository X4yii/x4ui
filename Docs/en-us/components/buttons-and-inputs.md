# Buttons & Inputs (`GuiButton`, `GuiTextInput`, `GuiToggle`, `GuiSlider`)

Primary interactive widgets follow X4UI's reactive `State<T>` paradigm and `GuiComponent` fluent API, avoiding manual event boilerplate. In `r1.0b7`, core widgets provide flex-first `.of(...)` factories and covariant fluent setters (`withPercentWidth`, `withMargin`, `withFlexGrow`, `withFlexShrink`, `withMinSize`, `withMaxSize`, `withTooltip`), eliminating manual casts.

---

## 1. `GuiButton`

Interactive button with theme variants (`DEFAULT`, `PRIMARY`, `SECONDARY`, `SUCCESS`, `DANGER`, `GHOST`) and tooltip support:

```java
GuiButton saveBtn = GuiButton.of("Save", () -> {
    // Click action
})
    .withPercentWidth(1.0f)
    .withVariant(GuiButton.Variant.PRIMARY)
    .withTooltip("Saves the current configuration");
```

---

## 2. `GuiTextInput`

Text field supporting focus management, cursor rendering, selection, clipboard operations, and two-way binding with `State<String>`:

```java
State<String> username = new State<>("");

GuiTextInput input = GuiTextInput.of(username)
    .setSubmitKey(KeyCodes.ENTER)
    .onEnter(() -> submitForm(username.get()))
    .setOnTextChanged(txt -> {
        // Optional per-keystroke hook
    })
    .withPercentWidth(1.0f);

input.setPlaceholder("Username...");
```

---

## 3. `GuiToggle`

Boolean switch bound either to a `State<Boolean>` or an explicit `Consumer<Boolean>`:

```java
State<Boolean> showHud = new State<>(true);

// Direct reactive binding (cast-free)
GuiToggle toggle = GuiToggle.of("Show HUD", showHud)
    .withPercentWidth(1.0f);

// Or explicit initial value + callback
GuiToggle altToggle = new GuiToggle(0, 0, 140, 20, "Compact Mode", false, enabled -> {
    // Explicit handler
});
```

---

## 4. Typed Sliders (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`)

Sliders bind directly to a typed `State<T>` with `min`, `max`, and `step`:

```java
State<Integer> volume = new State<>(50);

GuiSliderInt volumeSlider = GuiSliderInt.of(volume, 0, 100, 1)
    .withPercentWidth(1.0f);
```

`GuiSliderFloat` and `GuiSliderDouble` follow the same reactive constructor pattern. `GuiLabel.of(text)` / `GuiLabel.of(text, color)` and `GuiDropdown.of(state, options)` provide matching cast-free factories.

---

## 5. What to Avoid with Inputs

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Passing `org.lwjgl.input.Keyboard` constants to `setSubmitKey(...)` | Breaks portability under CleanroomMC (LWJGL 3) | Use `KeyCodes.ENTER` and constants from `KeyCodes` |
| Dividing by `mc.displayWidth` directly in custom widgets | Triggers division by zero when the window is minimized | Use `UIInput.safeMouseX(...)` / `safeMouseY(...)` |
| Mirroring widget values in plain local fields outside `State<T>` | Desynchronizes the UI when server batches update state | Bind widgets directly to `State<T>` |
