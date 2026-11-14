# Multimedia Components (`GuiImage` & `GuiVideo`)

X4UI's multimedia components manage asynchronous loading locally within their own bounds without blocking the render thread.

---

## `GuiImage` (Static, Remote & 9-Slice Images)

`GuiImage` supports both packaged textures (`ResourceLocation`) and asynchronously downloaded remote images via `setUrl(String)`:

```java
GuiImage banner = (GuiImage) new GuiImage(0, 0, 0, 150).withPercentWidth(1.0f);
banner.setUrl("https://example.com/banner.png");

// Or with a local ResourceLocation and 9-slice border scaling
banner.setResource(new ResourceLocation("mymod", "textures/gui/panel.png"));
banner.withNineSlice(new NineSliceConfig(4, 4, 4, 4));
banner.setNineSliceEnabled(true);
```

---

## `GuiVideo` (Asynchronous Video Player)

`GuiVideo` plays video and audio asynchronously on top of `MediaPipeline` (using OS-discovered `ffmpeg` or JCodec fallback), with built-in status display, optional controls, and error handling:

```java
GuiVideo introVideo = (GuiVideo) new GuiVideo(0, 0, 0, 240).withPercentWidth(1.0f);

introVideo.setVideoUrl("https://example.com/my_intro.mp4");
introVideo.setVolume(0.5f);
introVideo.setControlsEnabled(true);
introVideo.withOnError(err -> {
    // Optional: react when download or decoding fails
    System.err.println("Video error: " + err);
});
introVideo.play();
```

### Loading & Error Handling
- While downloading or preparing media, `GuiVideo` renders an internal progress bar inside its bounds.
- On network or codec failure, `GuiVideo` safely cleans up its resources, renders the diagnostic error state (`getVideoError()`), and invokes the `withOnError(...)` callback if registered.
