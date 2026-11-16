# 02 - Remote Assets & Asynchrony (`MediaPipeline`)

This document details how X4UI downloads, validates, and processes external media in the background using `MediaPipeline` and `MediaManager` without blocking the main render thread.

*(For UI widget usage, see `components/multimedia.md`.)*

---

## 1. Problem & Design Decisions

Downloading images or decoding video on the main thread freezes the Minecraft client. Likewise, bundling `ffmpeg` binaries inside `.minecraft/` causes permission issues, bloated installations, and OS-specific maintenance problems.

X4UI addresses both issues with three design decisions:
1. **Decoupled promise-based asynchrony:** `MediaPipeline` returns `CompletableFuture<File>` and never triggers global toast popups (`GuiToastManager`).
2. **OS-level `ffmpeg` discovery (`MediaManager`):** X4UI locates `ffmpeg` installed on the user's operating system (**Linux, macOS, and Windows 10/11**) via `PATH` and standard system locations, falling back automatically to pure-Java **JCodec** when `ffmpeg` is unavailable.
3. **Cache isolation & security (`SecurityUtils`):** Remote downloads are validated against SSRF and path traversal, then stored under `.minecraft/x4ui/cache/<modid>/`.

---

## 2. Using `MediaPipeline`

```java
import com.x4yi.x4ui.impl.client.media.MediaPipeline;

CompletableFuture<File> mediaFuture = MediaPipeline.getInstance().obtainMedia(
    "mymodid",
    "https://example.com/video.mp4",
    (status, percent) -> {
        // Progress callback (non-blocking)
    }
);

mediaFuture.thenAccept(file -> {
    // Cached file ready for playback
}).exceptionally(ex -> {
    // Explicit handling for network, validation, or codec errors
    return null;
});
```

---

## 3. Network & Filesystem Security (`SecurityUtils`)

Before executing downloads or writing cache files, `SecurityUtils` enforces:
- `validateHttpUrl(...)`: restricts schemes to `http`/`https` and blocks loopback, link-local, and private network ranges (SSRF protection).
- `resolveSafeChild(...)`: canonicalizes paths and rejects directory traversal (`..`) outside the mod's cache directory.

---

## 4. Supported Media Formats

| Platform | Supported Video | Supported Audio | Notes |
| :--- | :--- | :--- | :--- |
| **PC (Linux, macOS, Windows 10/11)** | MP4, MKV, WebM, AVI, FLV | MP3, AAC, OGG, WAV, FLAC | Decoded via OS `ffmpeg` with automatic JCodec fallback. |
| **Android (PojavLauncher, FoldCraft)** | **MP4 (H.264) only** | MP3, AAC | Use *Baseline* or *Main* profile. WebM/MKV fail on Android. |

For cross-platform distribution with `GuiVideo`, provide an `.mp4` (H.264) fallback source.

---

## 5. What to Avoid with Remote Media

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Calling `future.join()` or `future.get()` in `update()` or `render()` | Blocks the Minecraft client thread until network/decoding finishes | Use `.thenAccept()` or rely on `GuiVideo` / `GuiImage` |
| Invoking `GuiToastManager` from background managers | Couples I/O pipelines to global UI overlays | Render status inside `GuiVideo` or handle notifications in the screen callback |
| Ignoring failure paths on external URLs | Unreachable hosts or unsupported codecs leave silent failures | Register `withOnError(...)` on `GuiVideo` and `.exceptionally(...)` on futures |
