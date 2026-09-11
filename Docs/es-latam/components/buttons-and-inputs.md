# Botones y Entradas (Inputs)

Los componentes de interacción primaria están diseñados para aprovechar al máximo la **API Fluida** (Fluent API) y eliminar la necesidad de sobrecargar el código con llamadas repetitivas.

## GuiButton

El botón clásico interactivo. Las propiedades visuales y variantes se configuran mediante builders fluidos:

```java
GuiButton btn = (GuiButton) new GuiButton(0, 0, 0, 20, "Guardar", () -> {
    System.out.println("Guardado!");
})
.withPercentWidth(1.0f)                           // Dimensionamiento responsivo
.withVariant(GuiButton.Variant.SUCCESS)            // Variante visual del tema (DEFAULT, PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST)
.withTooltip("Guarda los cambios permanentemente"); // Tooltip instantáneo al pasar el cursor
```

## Entradas de Texto (GuiTextInput)

`GuiTextInput` gestiona internamente el foco, renderizado de cursor parpadeante, selección y portapapeles.

```java
GuiTextInput nameInput = (GuiTextInput) new GuiTextInput(0, 0, 0, 20, "")
    .withPercentWidth(1.0f);

// Configurar placeholder
nameInput.setPlaceholder("Escribe tu nombre...");

// Escuchar cambios de texto
nameInput.setOnTextChanged(text -> {
    System.out.println("Texto actual: " + text);
});
```

### Estados Reactivos
Para vincular `GuiTextInput` a un `State<String>` reactivo:

```java
State<String> userName = new State<>("");
nameInput.bindState(userName, txt -> {
    nameInput.setText(txt);
});
```

## Sliders y Toggles

### Sliders Numéricos (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`)

Permiten seleccionar valores numéricos con arrastre del ratón o teclado:

```java
GuiSliderInt volumeSlider = (GuiSliderInt) new GuiSliderInt(0, 0, 0, 20, 0, 100, 50, "Volumen: ")
    .withPercentWidth(1.0f);

volumeSlider.setOnValueChanged(val -> {
    System.out.println("Nuevo volumen: " + val);
});
```

### Interruptores y Casillas (`GuiToggle`)

`GuiToggle` proporciona un interruptor booleano animado con soporte para etiquetas:

```java
GuiToggle showHUD = (GuiToggle) new GuiToggle(0, 0, 120, 20, "Mostrar HUD", true);

showHUD.setOnToggle(enabled -> {
    System.out.println("HUD activo: " + enabled);
});
```