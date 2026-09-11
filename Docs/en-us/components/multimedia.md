# Multimedia Components (Videos and Images)

In X4UI, the multimedia components are designed under strict adherence to the Liskov Substitution Principle.

## GuiImage (Static Images and 9-Slice)

`GuiImage` handles both packaged textures (`ResourceLocation`) and images downloaded from the web, with support for normal rendering and **NineSlice** scaling.

### Loading Web Textures

```java
GuiImage banner = (GuiImage) new GuiImage(0, 0, 0, 150).withPercentWidth(1.0f);
// Asynchronous download; the game thread is not blocked
banner.setImage("https://example.com/background.jpg");
```

### 9-Slice Configurations
The way to apply slice margins is by instantiating a `NineSliceConfig`:

```java
// Configure the 9-slice margins for the texture
banner.setNineSliceConfig(new NineSliceConfig(4, 4, 4, 4));
// Instruct GuiImage to draw using the protected borders
banner.setMode(GuiImage.ImageMode.NINESLICE);
```

## GuiVideo (FFmpeg Player)

`GuiVideo` is a fully independent entity extending `GuiComponent`.
It manages its own asynchronous frame renderer tied directly to the `MediaPipeline` promises.

```java
GuiVideo introVideo = (GuiVideo) new GuiVideo(0, 0, 0, 240).withPercentWidth(1.0f);

introVideo.setVideo("https://example.com/my_intro.mp4");
introVideo.setLooping(true); // Repeat when finished
introVideo.setVolume(0.5f);  // Volume at 50%
```

### Integrated Loading Bar
Upon starting the video download, the component will internally render a loading spinner. It will NOT inject `Toasts` into the global manager, keeping your screen entirely clean of popups even if you are iterating over heavy multimedia menus.
