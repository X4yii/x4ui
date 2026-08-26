# Interactive (Buttons and Toggles)

Components designed to receive clicks and execute code.

## GuiButton

The classic button. Displays a background, centered text, and changes color when hovered over.

### Creation
```java
GuiButton btn = new GuiButton(x, y, width, height, "Connect to Server", () -> {
    Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, mc, myServerData));
});
```

### Behavior
- When hovered, the background color transitions smoothly.
- On left click, it automatically plays the Minecraft click sound.

## GuiToggle

Works like an On/Off switch.

### Creation
```java
GuiToggle light = new GuiToggle(x, y, width, height, "Render Particles", true, (particlesActive) -> {
    MyModConfigs.renderParticles = particlesActive;
});
```

### Behavior
- Shows text on the left and a switch on the right ("ON" / "OFF").
- On click, it moves from side to side with a smooth animation and notifies your code.

## GuiDropdown<T>

A dropdown menu to choose an option from a list. It's connected to a `State<T>`.

### Creation
```java
State<String> flightMode = new State<>("Normal");
List<String> options = Arrays.asList("Normal", "Jetpack", "Hover");

GuiDropdown<String> menu = new GuiDropdown<>(x, y, width, height, flightMode, options);
```

### How it works
- Shows the current option. On click, it opens a list with the other options.
- To prevent the list from being hidden behind other things, the menu automatically increases its layer (`layer`) when opened, drawing itself safely above everything else.
