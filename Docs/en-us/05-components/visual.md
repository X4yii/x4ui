# Visual Components

Elements created simply to display information on the screen.

## GuiLabel

Draws a simple single-line text.

### Usage
You don't need to specify the exact width; the text calculates its own size automatically.

```java
GuiLabel cordY = new GuiLabel(0, 0, "Y Coordinate: 64", 0xFF00FF00); // Green text
cordY.setCentered(true);
```
If you use `setCentered(true)`, the text will be drawn horizontally centered from the coordinate you provided.

## GuiSprite

Draws images (textures).

### Smart Borders (9-Slice)
Normally, if you stretch an image in Minecraft, the borders get deformed and look blurry or stretched out.
`GuiSprite` solves this by letting you define static corners.

```java
ResourceLocation texture = new ResourceLocation("mymod", "textures/gui/machine_panel.png");
GuiSprite background = new GuiSprite(x, y, menuWidth, menuHeight, texture, 256, 256);
background.setSliceSize(8); // Keep 8 pixels of the borders unstretched
```
This will only stretch the center of the image, keeping the borders perfect no matter what size the menu is.

## GuiModelRender

Draws a 3D model (like a player or a mob) or an item on the screen.

### Controls
```java
GuiModelRender mr = new GuiModelRender(x, y, width, height);
// Shows a Creeper or whatever entity you need
mr.setEntity(new EntityCreeper(Minecraft.getMinecraft().world)); 
mr.setScale(30f); // Model size in the menu
mr.setAllowDragRotation(true); // Allows the user to rotate it with the mouse
mr.setLookAtCursor(true); // Makes the model's head follow the mouse
```

This component safely handles everything: lighting, rotation, and prevents breaking the real model's neck when you close the menu.

## GuiSlot

A special bridge that lets you use real Minecraft Slots inside an X4UI menu.

Normally, slots are hard to move in Vanilla, but if you put a `GuiSlot` inside a `GuiPanel`, X4UI will take care of moving the real slot to the exact screen coordinates so it works perfectly with your layout.

## GuiImage

Displays an image from a `ResourceLocation` or downloads it from a URL with disk caching.

### Usage
```java
// From a ResourceLocation
GuiImage img = new GuiImage(x, y, 64, 64);
img.setResource(new ResourceLocation("mymod", "textures/gui/logo.png"));

// From a URL (automatically cached to disk)
GuiImage avatar = new GuiImage(x, y, 32, 32);
avatar.setUrl("https://example.com/avatar.png");

// With UV mapping (show a portion of the texture)
GuiImage crop = new GuiImage(x, y, 16, 16);
crop.setResource(texture);
crop.setUV(0.0f, 0.0f, 0.5f, 0.5f); // Top-left quarter
```

## GuiVideo

A mock video player component. It extends `GuiImage` and shows a play/pause overlay with a progress bar. Currently a placeholder for future real video support.

### Usage
```java
GuiVideo video = new GuiVideo(x, y, 200, 150);
video.setVideoUrl("https://example.com/intro.mp4");
video.play();

// Toggle play/pause on click is automatic
```
