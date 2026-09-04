# Multimedia

Este documento cubre los componentes de visualización de medios: `GuiImage`, `GuiSprite`, `GuiVideo` y `GuiAudio`.

Para cargar medios desde URLs remotas, consulta `02-remote-assets.md`.

---

## GuiImage

Renderiza una imagen estática desde un `ResourceLocation` o URL. Soporta transparencia alpha y tinción de color.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiImage`

### Constructor

```java
new GuiImage(int x, int y, int width, int height)
```

### Recurso Local

```java
import com.x4yi.x4ui.client.gui.component.GuiImage;
import net.minecraft.util.ResourceLocation;

GuiImage logo = new GuiImage(10, 10, 200, 100);
logo.setResource(new ResourceLocation("mymod", "textures/gui/logo.png"));

rootPanel.addChild(logo);
```

### URL Remota

```java
GuiImage avatar = new GuiImage(10, 120, 64, 64);
avatar.setUrl("https://example.com/avatar.png");
```

La imagen se descarga de forma asíncrona mediante `ThreadDownloadImageData` de Minecraft y se almacena en caché en `.minecraft/x4ui_cache/`. Se dibuja un rectángulo de placeholder hasta que la imagen se carga.

### Alpha y Tinción de Color

```java
GuiImage faded = new GuiImage(10, 200, 150, 50);
faded.setResource(new ResourceLocation("mymod", "textures/gui/banner.png"));
faded.setAlpha(0.5f);
faded.setColor(1.0f, 0.5f, 0.0f);
```

### Mapeo UV (Región de Textura)

Mostrar solo una porción de una textura:

```java
GuiImage icon = new GuiImage(10, 260, 16, 16);
icon.setResource(new ResourceLocation("mymod", "textures/gui/icons.png"));
icon.setUV(0.0f, 0.0f, 0.25f, 0.25f);
```

### API Fluent

Todos los setters devuelven `this` para encadenamiento:

```java
GuiImage image = new GuiImage(0, 0, 100, 50)
    .setResource(new ResourceLocation("mymod", "tex.png"))
    .setAlpha(0.8f)
    .setColor(1.0f, 1.0f, 1.0f)
    .setUV(0, 0, 1, 1);
```

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `setResource(ResourceLocation)` | `GuiImage` | Establece la textura local |
| `setUrl(String)` | `GuiImage` | Carga imagen remota desde URL |
| `setAlpha(float)` | `GuiImage` | Establece la transparencia alpha (0-1) |
| `setColor(float, float, float)` | `GuiImage` | Establece la tinción de color RGB |
| `setUV(float, float, float, float)` | `GuiImage` | Establece la región de textura (u1, v1, u2, v2) |

---

## GuiSprite

Renderiza una textura usando la técnica de 9-slice (9-parche). Las esquinas mantienen su relación de aspecto, los bordes se estiran en una dirección y el centro se estira uniformemente.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiSprite`

### Constructor

```java
new GuiSprite(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight)
```

- `textureWidth` y `textureHeight` son las dimensiones originales en píxeles del archivo de textura.

### Uso Básico

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

### Tamaño del Slice

El `sliceSize` define el ancho del borde en píxeles. Las 9 regiones son:

```
+---+-------------------+---+
| 1 |         2         | 3 |   <- fila superior
+---+-------------------+---+
|   |                   |   |
| 4 |         5         | 6 |   <- fila media (estirada)
|   |                   |   |
+---+-------------------+---+
| 7 |         8         | 9 |   <- fila inferior
+---+-------------------+---+
```

- Esquinas (1, 3, 7, 9): Dibujadas en tamaño original
- Bordes (2, 4, 6, 8): Estirados en una dirección
- Centro (5): Estirado en ambas direcciones

```java
frame.setSliceSize(12);
```

### Renderizado

- Usa `Tessellator` y `BufferBuilder` con formato de vértice `POSITION_TEX`
- Dibuja 9 quads usando `GL_QUADS` (modo begin 7)
- Retrocede a un quad simple estirado si `sliceSize * 2` excede las dimensiones del componente

### Utilidad Estática

`GuiSprite.drawTexturedRect()` es un método público estático para dibujar quads con textura:

```java
GuiSprite.drawTexturedRect(x, y, width, height, u1, v1, u2, v2);
```

Esto se usa internamente por `GuiImage` para el renderizado.

---

## GuiVideo

Reproduce video MP4 con selección automática del decodificador. Usa FFmpeg por defecto, recurriendo a JCodec (puro Java) cuando FFmpeg no está disponible.

> **Nota:** Este componente se encuentra en una fase muy temprana de desarrollo. Su API puede cambiar sin previo aviso en futuras versiones.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiVideo`

### Constructor

```java
new GuiVideo(int x, int y, int width, int height)
```

Extiende `GuiImage`. Hereda todos los métodos de imagen más métodos específicos de video.

### Uso Básico

```java
import com.x4yi.x4ui.client.gui.component.GuiVideo;

GuiVideo video = new GuiVideo(0, 0, 640, 480);
video.setVideoUrl("https://example.com/intro.mp4");
video.play();

rootPanel.addChild(video);
```

### Archivo Local

```java
GuiVideo localVideo = new GuiVideo(0, 0, 320, 240);
localVideo.setVideoUrl("C:/path/to/video.mp4");
localVideo.play();
```

### Controles de Reproducción

```java
video.play();     // Iniciar o reanudar reproducción
video.pause();    // Pausar reproducción
video.stop();     // Detener y liberar recursos
```

### Clic para Alternar

Hacer clic en el componente de video alterna reproducción/pausa (implementado mediante `onMouseClick`).

### Limpieza al Cerrar Pantalla

Los decodificadores de video se ejecutan en hilos de fondo. Siempre llama a `stop()` al cerrar la pantalla:

```java
@Override
public void onGuiClosed() {
    super.onGuiClosed();
    video.stop();
}
```

### Alpha y Color

```java
video.setAlpha(0.8f);
video.setColor(1.0f, 1.0f, 1.0f);
```

### Selección del Decodificador

La selección e instanciación del decodificador se delega automáticamente al servicio central de medios (`X4UI.getMediaService()`).

1. **DirectMemoryVideoDecoder** (principal, Zero-Copy): Decodificador nativo que ejecuta un subproceso de FFmpeg y canaliza frames crudos BGRA directamente a un buffer de memoria mapeada (`DirectByteBuffer`). Evita la memoria Heap de Java, logrando `0 MB` de huella extra y cero lags por recolección de basura, soportando hasta 1280x720 nativo.
2. **JCodecVideoDecoder** (respaldo): Decodificador H.264 puro Java usando JCodec. Demuxing MP4, decodificación de NALs H.264, conversión YUV420 a ARGB. Se activa cuando FFmpeg no está disponible (Android, sistema de archivos no ejecutable, binario faltante).

El respaldo es automático y transparente.

### Renderizado

- Cada frame se obtiene del decodificador basándose en el reloj maestro (`MediaPlayer.getPlaybackTimeSec()`)
- **Optimización de PCIe:** El sistema omite el renderizado si el PTS del vídeo no requiere un nuevo frame.
- **Zero-Copy Upload:** Si se usa FFmpeg, los píxeles en la memoria nativa se suben directamente a la VRAM vía `glTexSubImage2D(..., byteBuffer.asIntBuffer())`.
- Muestra texto "Cargando..." mientras el decodificador se inicializa
- Muestra mensaje de error en rojo si el decodificador falla

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `setVideoUrl(String)` | `GuiVideo` | Establece la URL del video y resetea el estado |
| `play()` | `void` | Inicia la reproducción |
| `pause()` | `void` | Pausa la reproducción |
| `stop()` | `void` | Detiene y libera recursos |
| `setAlpha(float)` | `GuiVideo` | Establece alpha (heredado de GuiImage) |
| `setColor(float, float, float)` | `GuiVideo` | Establece tinción de color (heredado de GuiImage) |

---

## GuiAudio

Componente de reproducción de audio. Actualmente es un stub -- la implementación del decodificador de audio está planificada para una fase futura.

**Paquete:** `com.x4yi.x4ui.client.gui.component.GuiAudio`

### Constructor

```java
new GuiAudio(int x, int y, int width, int height)
```

### Estado Actual

`GuiAudio` tiene la API pública definida pero el método interno `initAudio()` está vacío. El componente no producirá salida de audio en la versión actual.

```java
import com.x4yi.x4ui.client.gui.component.GuiAudio;

GuiAudio audio = new GuiAudio(0, 0, 200, 50);
audio.setAudioUrl("https://example.com/music.mp3");
audio.play();
```

### Propiedades

| Método | Tipo | Descripción |
|--------|------|-------------|
| `setAudioUrl(String)` | `GuiAudio` | Establece la URL del audio |
| `play()` | `void` | Inicia la reproducción (sin efecto en la versión actual) |
| `pause()` | `void` | Pausa la reproducción |
| `stop()` | `void` | Detiene la reproducción |

### Renderizado

- Si el decodificador reporta un error, se dibuja un rectángulo oscuro con mensaje de error
- De lo contrario, el componente es invisible (el audio no tiene representación visual)