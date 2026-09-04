# Multimedia

This document covers the media display components: `GuiImage`, `GuiSprite`, `GuiVideo`, and `GuiAudio`.

For loading media from remote URLs, see `02-remote-assets.md`.

---

## GuiImage

Renders a static image from a `ResourceLocation` or URL. Supports alpha transparency and color tinting.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiImage`

### Constructor

```java
new GuiImage(int x, int y, int width, int height)
```

### Local Resource

```java
import com.x4yi.x4ui.client.gui.component.GuiImage;
import net.minecraft.util.ResourceLocation;

GuiImage logo = new GuiImage(10, 10, 200, 100);
logo.setResource(new ResourceLocation("mymod", "textures/gui/logo.png"));

rootPanel.addChild(logo);
```

### Remote URL

```java
GuiImage avatar = new GuiImage(10, 120, 64, 64);
avatar.setUrl("https://example.com/avatar.png");
```

The image is downloaded asynchronously via Minecraft's `ThreadDownloadImageData` and cached to `.minecraft/x4ui_cache/`. A placeholder rect is drawn until the image loads.

### Alpha and Color Tint

```java
GuiImage faded = new GuiImage(10, 200, 150, 50);
faded.setResource(new ResourceLocation("mymod", "textures/gui/banner.png"));
faded.setAlpha(0.5f);
faded.setColor(1.0f, 0.5f, 0.0f);
```

### UV Mapping (Texture Region)

Display only a portion of a texture:

```java
GuiImage icon = new GuiImage(10, 260, 16, 16);
icon.setResource(new ResourceLocation("mymod", "textures/gui/icons.png"));
icon.setUV(0.0f, 0.0f, 0.25f, 0.25f);
```

### Fluent API

All setters return `this` for chaining:

```java
GuiImage image = new GuiImage(0, 0, 100, 50)
    .setResource(new ResourceLocation("mymod", "tex.png"))
    .setAlpha(0.8f)
    .setColor(1.0f, 1.0f, 1.0f)
    .setUV(0, 0, 1, 1);
```

### Properties

| Method | Type | Description |
|--------|------|-------------|
| `setResource(ResourceLocation)` | `GuiImage` | Sets local texture |
| `setUrl(String)` | `GuiImage` | Loads remote image from URL |
| `setAlpha(float)` | `GuiImage` | Sets alpha transparency (0-1) |
| `setColor(float, float, float)` | `GuiImage` | Sets RGB color tint |
| `setUV(float, float, float, float)` | `GuiImage` | Sets texture region (u1, v1, u2, v2) |

---

## GuiSprite

Renders a texture using 9-slice (9-patch) technique. Corners maintain their aspect ratio, edges stretch in one direction, and the center stretches uniformly.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiSprite`

### Constructor

```java
new GuiSprite(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight)
```

- `textureWidth` and `textureHeight` are the original pixel dimensions of the texture file.

### Basic Usage

```java
import com.x4yi.x4ui.client.gui.component.GuiSprite;
import net.minecraft.util.ResourceLocation;

GuiSprite frame = new GuiSprite(0, 0, 300, 200,
    new ResourceLocation("mymod", "textures/gui/frame.png"),
    64, 64
);
frame.setSliceSize(8);

rootPanel.addChild(frame);
```

### Slice Size

The `sliceSize` defines the border width in pixels. The 9 regions are:

```
+---+-------------------+---+
| 1 |         2         | 3 |   <- top row
+---+-------------------+---+
|   |                   |   |
| 4 |         5         | 6 |   <- middle row (stretched)
|   |                   |   |
+---+-------------------+---+
| 7 |         8         | 9 |   <- bottom row
+---+-------------------+---+
```

- Corners (1, 3, 7, 9): Drawn at original size
- Edges (2, 4, 6, 8): Stretched in one direction
- Center (5): Stretched in both directions

```java
frame.setSliceSize(12);
```

### Rendering

- Uses `Tessellator` and `BufferBuilder` with `POSITION_TEX` vertex format
- Draws 9 quads using `GL_QUADS` (begin mode 7)
- Falls back to a single stretched quad if `sliceSize * 2` exceeds the component dimensions

### Static Utility

`GuiSprite.drawTexturedRect()` is a public static method for drawing textured quads:

```java
GuiSprite.drawTexturedRect(x, y, width, height, u1, v1, u2, v2);
```

This is used internally by `GuiImage` for rendering.

---

## GuiVideo

Plays MP4 video with automatic decoder selection. Uses FFmpeg by default, falling back to JCodec (pure-Java) when FFmpeg is unavailable.

> **Note:** This component is in a very early stage of development. Its API may change without notice in future versions.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiVideo`

### Constructor

```java
new GuiVideo(int x, int y, int width, int height)
```

Extends `GuiImage`. Inherits all image methods plus video-specific methods.

### Basic Usage

```java
import com.x4yi.x4ui.client.gui.component.GuiVideo;

GuiVideo video = new GuiVideo(0, 0, 640, 480);
video.setVideoUrl("https://example.com/intro.mp4");
video.play();

rootPanel.addChild(video);
```

### Local File

```java
GuiVideo localVideo = new GuiVideo(0, 0, 320, 240);
localVideo.setVideoUrl("C:/path/to/video.mp4");
localVideo.play();
```

### Playback Controls

```java
video.play();     // Start or resume playback
video.pause();    // Pause playback
video.stop();     // Stop and release resources
```

### Click to Toggle

Clicking the video component toggles play/pause (implemented via `onMouseClick`).

### Cleanup on Screen Close

Video decoders run on background threads. Always call `stop()` when closing the screen:

```java
@Override
public void onGuiClosed() {
    super.onGuiClosed();
    video.stop();
}
```

### Alpha and Color

```java
video.setAlpha(0.8f);
video.setColor(1.0f, 1.0f, 1.0f);
```

### Decoder Selection

Decoder selection and instantiation are automatically delegated to the central media service (`X4UI.getMediaService()`).

1. **DirectMemoryVideoDecoder** (primary, Zero-Copy): Native decoder that executes an FFmpeg subprocess and pipes raw BGRA frames directly into a memory-mapped buffer (`DirectByteBuffer`). Bypasses the Java Heap entirely, achieving `0 MB` extra footprint and zero Garbage Collection lag, supporting up to 1280x720 natively.
2. **JCodecVideoDecoder** (fallback): Pure-Java H.264 decoder using JCodec. Handles MP4 demuxing, H.264 NAL decoding, and YUV420 to ARGB conversion. Activates when FFmpeg is unavailable (Android, non-executable filesystem, missing binary).

The fallback is automatic and transparent.

### Rendering

- Each frame is fetched from the decoder based on the master clock (`MediaPlayer.getPlaybackTimeSec()`)
- **PCIe Optimization:** The system skips rendering if the video's PTS does not require a new frame.
- **Zero-Copy Upload:** If FFmpeg is used, the pixels in native memory are uploaded directly to VRAM via `glTexSubImage2D(..., byteBuffer.asIntBuffer())`.
- Displays "Loading..." text while the decoder initializes
- Displays error message in red if the decoder fails

### Properties

| Method | Type | Description |
|--------|------|-------------|
| `setVideoUrl(String)` | `GuiVideo` | Sets the video URL and resets state |
| `play()` | `void` | Starts playback |
| `pause()` | `void` | Pauses playback |
| `stop()` | `void` | Stops and releases resources |
| `setAlpha(float)` | `GuiVideo` | Sets alpha (inherited from GuiImage) |
| `setColor(float, float, float)` | `GuiVideo` | Sets color tint (inherited from GuiImage) |

---

## GuiAudio

Audio playback component. Currently a stub -- the audio decoder implementation is planned for a future phase.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiAudio`

### Constructor

```java
new GuiAudio(int x, int y, int width, int height)
```

### Current Status

`GuiAudio` has the public API defined but the internal `initAudio()` method is empty. The component will not produce audio output in the current version.

```java
import com.x4yi.x4ui.client.gui.component.GuiAudio;

GuiAudio audio = new GuiAudio(0, 0, 200, 50);
audio.setAudioUrl("https://example.com/music.mp3");
audio.play();
```

### Properties

| Method | Type | Description |
|--------|------|-------------|
| `setAudioUrl(String)` | `GuiAudio` | Sets the audio URL |
| `play()` | `void` | Starts playback (no-op in current version) |
| `pause()` | `void` | Pauses playback |
| `stop()` | `void` | Stops playback |

### Rendering

- If the decoder reports an error, a dark rect with an error message is drawn
- Otherwise, the component is invisible (audio has no visual representation)
