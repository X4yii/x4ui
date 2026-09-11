# 02 - Remote Assets & Asynchrony (MediaPipeline)

This document describes how X4UI handles asynchronous operations such as downloading images or processing FFmpeg videos in the background using the decoupled `MediaPipeline`.

---

## 1. The Decoupled Asynchrony Paradigm

The `MediaPipeline` is completely decoupled from the UI. It returns `CompletableFuture`s, allowing the caller (or the specific GUI component) to decide when and how to notify the user.

---

## 2. Using MediaPipeline

When downloading or processing external media in a sandboxed, per-mod cached pipeline, invoke `MediaPipeline`:

```java
import com.x4yi.x4ui.impl.client.media.MediaPipeline;

// Fetch media with per-mod cache isolation (.minecraft/x4ui/cache/<modid>/)
CompletableFuture<File> mediaFuture = MediaPipeline.getInstance().obtainMedia("mymodid", "https://example.com/video.mp4", (status, percent) -> {
    System.out.println("Progress [" + status + "]: " + percent + "%");
});

mediaFuture.thenAccept(file -> {
    // Media was successfully downloaded, optimized, and cached
    System.out.println("Ready at: " + file.getAbsolutePath());
}).exceptionally(ex -> {
    // Handle network errors or FFmpeg failures
    ex.printStackTrace();
    return null;
});
```

---

## 3. UI Integration (GuiVideo & GuiImage)

You do not need to manually handle `CompletableFuture`s if you use the native multimedia components, which have built-in localized loading states.

### GuiVideo
`GuiVideo` acts as a self-contained asynchronous component. When you assign a URL via `setVideoUrl(url)`, it automatically requests the `MediaPipeline` to stream or download it, displaying localized loading progress inside the video component bounds.

```java
GuiVideo videoPlayer = (GuiVideo) new GuiVideo(0, 0, 0, 200).withPercentWidth(1.0f);
videoPlayer.setVideoUrl("https://example.com/trailer.mp4");
videoPlayer.play();
```

### GuiImage
Similarly, `GuiImage` supports both local assets (`ResourceLocation`) and remote URLs via `setUrl(url)`:

```java
GuiImage banner = (GuiImage) new GuiImage(0, 0, 0, 100).withPercentWidth(1.0f);
banner.setUrl("https://example.com/banner.png");
```

---

## 4. What To Avoid (Anti-Patterns)

> [!CAUTION]
> **1. NEVER block the thread waiting for a CompletableFuture.**
> Never call `future.join()` or `future.get()` inside `update()` or `render()`. Minecraft will freeze completely.

> [!CAUTION]
> **2. NEVER inject global Toasts from background managers.**
> The `MediaPipeline` must never know about `GuiToastManager`. If you want to notify the user of a download completion, do it in the `.thenAccept()` callback within your *Screen* class.
