# 02 - Remote Assets (`RemoteResourceManager`)

X4UI includes a remote asset manager for downloading and caching media from HTTP/HTTPS URLs. It uses NIO channels for zero-copy transfers on background threads without impacting game performance.

## Creating a Namespace

A `RemoteResourceManager` instance is scoped to a specific mod ID. Each mod should create its own instance to isolate cache files.

```java
import com.x4yi.x4ui.api.client.resource.RemoteResourceManager;

public class MyClientProxy {
    public static final RemoteResourceManager ASSETS = RemoteResourceManager.create("mymod");
}
```

Files are stored in `.minecraft/x4ui/cache/<modid>/` with SHA-256 hashed filenames.

## Loading Remote Media

Use `getMedia(url)` to obtain a local file path. If the file is not cached, it is downloaded asynchronously and `null` is returned on the first call. Subsequent calls return the cached path.

```java
String cachedPath = MyClientProxy.ASSETS.getMedia("https://example.com/image.png");

if (cachedPath != null) {
    // File is ready, use it
    GuiImage image = new GuiImage(0, 0, 200, 100);
    image.setResource(new ResourceLocation("file://" + cachedPath));
} else {
    // File is downloading, show a placeholder
    GuiLabel loading = new GuiLabel(0, 0, "Loading...", 0xFFAAAAAA);
}
```

## Using Remote URLs with GuiImage

`GuiImage` has built-in URL support that handles downloading and caching internally:

```java
import com.x4yi.x4ui.client.gui.component.GuiImage;

GuiImage logo = new GuiImage(10, 10, 200, 100);
logo.setUrl("https://example.com/logo.png");
```

The image is downloaded asynchronously via Minecraft's `ThreadDownloadImageData` and cached to `.minecraft/x4ui_cache/`. The `setUrl()` method can be called before the component is added to the tree.

## Using Remote URLs with GuiVideo

```java
import com.x4yi.x4ui.client.gui.component.GuiVideo;

GuiVideo video = new GuiVideo(0, 0, 640, 480);
video.setVideoUrl("https://example.com/intro.mp4");
video.play();

// Stop playback when closing the screen
@Override
public void onGuiClosed() {
    super.onGuiClosed();
    video.stop();
}
```

`GuiVideo` lazily initializes the decoder on the first `play()` call. The decoder is either `FFmpegProcessDecoder` (native FFmpeg) or `JCodecVideoDecoder` (pure-Java fallback).

## Cache Management

Delete cached files older than a specified number of days:

```java
// Remove files not accessed in 30 days
MyClientProxy.ASSETS.cleanOldCache(30);
```

Call this during `FMLPostInitializationEvent` or at any point during the mod lifecycle.

## Implementation Details

- **Download Manager**: `DownloadManager` is a singleton with a 2-thread executor pool. Downloads use NIO `FileChannel` for zero-copy transfers.
- **Cache Key**: Files are named using the SHA-256 hash of the URL, ensuring deterministic paths and deduplication.
- **Thread Safety**: `getMedia()` is safe to call from any thread. The download runs on a background thread.
- **Error Handling**: Failed downloads do not crash the game. The file is simply not cached, and `getMedia()` returns `null`.
