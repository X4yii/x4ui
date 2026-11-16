# Botones y Entradas (`GuiButton`, `GuiTextInput`, `GuiToggle`, `GuiSlider`)

Los componentes interactivos primarios operan bajo el paradigma reactivo de X4UI (`State<T>`) y la API fluida de `GuiComponent`, eliminando el acoplamiento manual de eventos.

---

## 1. `GuiButton`

Botón interactivo con variantes temáticas (`DEFAULT`, `PRIMARY`, `SECONDARY`, `SUCCESS`, `DANGER`, `GHOST`) y soporte de tooltips:

```java
GuiButton saveBtn = (GuiButton) new GuiButton(0, 0, 0, 22, "Guardar", () -> {
    // Acción al hacer clic
})
    .withPercentWidth(1.0f)
    .withVariant(GuiButton.Variant.PRIMARY)
    .withTooltip("Guarda la configuración actual");
```

---

## 2. `GuiTextInput`

Campo de texto con foco automático, cursor, selección, portapapeles y enlace bidireccional con `State<String>`:

```java
State<String> username = new State<>("");

GuiTextInput input = (GuiTextInput) new GuiTextInput(0, 0, 0, 20, username)
    .setSubmitKey(KeyCodes.ENTER)
    .onEnter(() -> submitForm(username.get()))
    .setOnTextChanged(txt -> {
        // Reacción inmediata por tecla (opcional si ya usa State<String>)
    })
    .withPercentWidth(1.0f);

input.setPlaceholder("Nombre de usuario...");
```

---

## 3. `GuiToggle`

Interruptor booleano enlazable a un `State<Boolean>` o a un `Consumer<Boolean>`:

```java
State<Boolean> showHud = new State<>(true);

// Enlace reactivo directo
GuiToggle toggle = (GuiToggle) new GuiToggle(0, 0, 0, 20, "Mostrar HUD", showHud)
    .withPercentWidth(1.0f);

// O con valor inicial y callback
GuiToggle altToggle = new GuiToggle(0, 0, 140, 20, "Modo compacto", false, enabled -> {
    // Manejo explícito
});
```

---

## 4. Sliders Numéricos (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`)

Los deslizadores trabajan directamente sobre un `State<T>` tipado con límites `min`, `max` y paso `step`:

```java
State<Integer> volume = new State<>(50);

GuiSliderInt volumeSlider = (GuiSliderInt) new GuiSliderInt(
    0, 0, 0, 20, volume, 0, 100, 1
).withPercentWidth(1.0f);
```

También están disponibles `GuiSliderFloat` y `GuiSliderDouble` con la misma firma reactiva.

---

## 5. Qué Evitar en Entradas

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Usar constantes de `org.lwjgl.input.Keyboard` en `setSubmitKey(...)` | Rompe portabilidad en CleanroomMC (LWJGL 3) | Utilizar `KeyCodes.ENTER` u otras constantes de `KeyCodes` |
| Leer `mc.displayWidth` sin guarda en componentes custom | Puede dividir por `0` al minimizar la ventana | Delegar en `UIInput.safeMouseX(...)` / `safeMouseY(...)` |
| Duplicar estado en campos booleanos locales fuera de `State<T>` | Desincroniza la vista si el servidor o un lote actualiza el valor | Enlazar el widget directamente a `State<T>` |
