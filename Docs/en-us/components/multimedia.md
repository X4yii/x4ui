# Multimedia Components (`GuiImage`, `GuiVideo`, `GuiAudio`, `GuiSprite`)

Multimedia components encapsulate asynchronous loading on top of `MediaPipeline` and `MediaManager` within their own bounds, keeping the render thread non-blocking and avoiding global toast spam.

---

## 1. `GuiImage` (Local Assets, URLs & 9-Slice)

`GuiImage` supports packaged textures (`ResourceLocation`), asynchronous remote URLs (`setUrl`), and protected 9-slice border scaling (`NineSliceConfig`):

```java
GuiImage banner = (GuiImage) new GuiImage(0, 0, 0, 150).withPercentWidth(1.0f);
banner.setUrl("https://example.com/banner.png");

// Or local asset with 9-slice borders
banner.setResource(new ResourceLocation("mymod", "textures/gui/panel.png"));
banner.withNineSlice(new NineSliceConfig(4, 4, 4, 4));
banner.setNineSliceEnabled(true);
```

---

## 2. `GuiVideo` (Asynchronous Player & Error Handling)

`GuiVideo` plays video and audio asynchronously (using OS-discovered `ffmpeg` or automatic JCodec fallback), featuring an integrated loading bar, optional playback controls, and visible error recovery:

```java
GuiVideo video = (GuiVideo) new GuiVideo(0, 0, 0, 240).withPercentWidth(1.0f);

video.setVideoUrl("https://example.com/intro.mp4");
video.setVolume(0.5f);
video.setControlsEnabled(true);
video.withOnError(errorMsg -> {
    // Optional handler for network, SSRF validation, or codec failures
});
video.play();
```

- While buffering or downloading, `GuiVideo` renders an internal progress bar.
- On failure, it safely disposes resources, displays the diagnostic error state (`getVideoError()`), and invokes `withOnError(...)`.

---

## 3. `GuiAudio` & `GuiSprite`

- **`GuiAudio`:** asynchronous remote audio playback with `setVolume(...)` and `setMuted(...)`.
- **`GuiSprite`:** 2D sprite animation driven by `SpriteSheetSource` or `SequenceSource` with `withFPS(...)`, `withLoopMode(LoopMode.LOOP)`, and `withUVScrollSpeed(...)`.

---

## 4. What to Avoid with Multimedia

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Passing local/private network URLs | Rejected by `SecurityUtils` (SSRF and path traversal protection) | Use public HTTP/HTTPS URLs or `ResourceLocation` |
| Omitting `withOnError(...)` or H.264 MP4 fallbacks | Leaves failures unhandled or breaks playback on Android launchers | Register `withOnError(...)` and provide `.mp4` (H.264) sources |
