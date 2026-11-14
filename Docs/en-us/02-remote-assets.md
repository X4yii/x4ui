# 02 - Remote Assets & Asynchrony (`MediaPipeline`)

This document explains how X4UI downloads, validates, and processes external media in the background using `MediaPipeline` and `MediaManager` without blocking the main render thread.

*(For ready-to-use UI widgets, see **Multimedia Components** in `components/multimedia.md`.)*

---

## 1. The Decoupled Asynchrony Paradigm

`MediaPipeline` is completely decoupled from the UI. It returns `CompletableFuture` promises so the caller decides **when** and **how** to update the interface.

---

## 2. Using `MediaPipeline`

To download or process external media in a per-mod sandboxed cache (`.minecraft/x4ui/cache/<modid>/`), invoke `MediaPipeline`:

```java
import com.x4yi.x4ui.impl.client.media.MediaPipeline;

CompletableFuture<File> mediaFuture = MediaPipeline.getInstance().obtainMedia(
    "mymodid",
    "https://example.com/video.mp4",
    (status, percent) -> System.out.println("Progress [" + status + "]: " + percent + "%")
);

mediaFuture.thenAccept(file -> {
    System.out.println("Ready at: " + file.getAbsolutePath());
}).exceptionally(ex -> {
    ex.printStackTrace();
    return null;
});
```

---

## 3. OS-Level FFmpeg Discovery & Security

- **OS Binary Discovery (`MediaManager`):** Starting in `r1.0b6`, X4UI automatically locates `ffmpeg` installed on **Linux, macOS, and Windows 10/11** (`PATH` and standard OS locations) instead of downloading binaries into `.minecraft/`. If `ffmpeg` is unavailable, X4UI falls back to **JCodec** automatically.
- **URL & Path Validation (`SecurityUtils`):** Remote downloads enforce HTTP/HTTPS, block local/private network addresses (SSRF protection), and prevent path traversal when writing cache files.

---

## 4. Supported Media Formats

### PC (Windows, Linux, macOS)
- **Video:** MP4, MKV, WebM, AVI, FLV
- **Audio:** MP3, AAC, OGG, WAV, FLAC

### Android (PojavLauncher, FoldCraft)
- **Video:** **MP4 with H.264 encoding ONLY** (*Baseline* or *Main* profile). Attempting to play WebM or MKV on Android will fail.
- If your mod uses `GuiVideo`, include an `.mp4` (H.264) fallback for mobile players.

---

## 5. What To Avoid (Critical Anti-Patterns)

> [!CAUTION]
> **1. NEVER block the thread waiting for a `CompletableFuture`.**
> Never call `future.join()` or `future.get()` inside `update()` or `render()`. Minecraft will freeze completely.

> [!CAUTION]
> **2. NEVER inject global Toasts from background managers.**
> `MediaPipeline` must never reference `GuiToastManager`. Handle user notifications in your *Screen* callback or rely on `GuiVideo`'s built-in status display.
