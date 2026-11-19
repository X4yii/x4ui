# 02 - Recursos Remotos y Asincronía (`MediaPipeline`)

Este documento detalla cómo X4UI descarga, valida y procesa recursos multimedia externos en segundo plano mediante `MediaPipeline` y `MediaManager`, manteniendo el hilo de renderizado libre de bloqueos.

*(Para el uso de widgets visuales en pantalla, consultar `components/multimedia.md`.)*

---

## 1. Problema y Decisión de Diseño

Descargar imágenes o decodificar video desde el hilo principal congela el cliente de Minecraft. Al mismo tiempo, empaquetar binarios de `ffmpeg` dentro de `.minecraft/` genera problemas de permisos, tamaño excesivo y conflictos entre sistemas operativos.

X4UI resuelve ambos problemas con tres decisiones de diseño:
1. **Asincronía desacoplada por promesas:** `MediaPipeline` retorna `CompletableFuture<File>` y nunca acopla notificaciones globales (`GuiToastManager`).
2. **Descubrimiento de `ffmpeg` en el sistema operativo (`MediaManager`):** X4UI busca `ffmpeg` instalado en el OS del usuario (**Linux, macOS y Windows 10/11**) vía `PATH` y rutas estándar del sistema, utilizando **JCodec** como respaldo automático en Java puro cuando `ffmpeg` no está disponible.
3. **Aislamiento de caché y seguridad (`SecurityUtils`):** Los archivos descargados se validan contra SSRF y *path traversal*, almacenándose con hash en `.minecraft/x4ui/cache/<modid>/`.

---

## 2. Uso de `MediaPipeline`

```java
import com.x4yi.x4ui.impl.client.media.MediaPipeline;

CompletableFuture<File> mediaFuture = MediaPipeline.getInstance().obtainMedia(
    "mymodid",
    "https://example.com/video.mp4",
    (status, percent) -> {
        // Callback de progreso (no bloquear aquí)
    }
);

mediaFuture.thenAccept(file -> {
    // Archivo disponible en caché local
}).exceptionally(ex -> {
    // Manejo explícito de fallo de red, validación o códec
    return null;
});

// Mantenimiento opcional de caché: elimina archivos con más de 7 días de antigüedad para el mod
MediaPipeline.getInstance().cleanCache("mymodid", 7);
```

> **Nota `r1.0b7`:** `MediaPipeline` + `MediaManager` consolidan y reemplazan completamente a las clases legacy eliminadas (`DownloadManager`, `RemoteResourceManager`, `IMediaService` y `MediaService`).

---

## 3. Seguridad de Red y Sistema de Archivos (`SecurityUtils`)

Antes de ejecutar cualquier descarga o escritura en caché, `SecurityUtils` aplica las siguientes comprobaciones:
- `validateHttpUrl(...)`: restringe esquemas a `http`/`https`, valida redirecciones manualmente y bloquea direcciones de loopback, enlace local, multicast y rangos privados tanto IPv4 como IPv6 (protección SSRF).
- `resolveSafeChild(...)`: normaliza rutas e impide secuencias de escape (`..`) fuera del directorio de caché del mod (*path traversal*).
- Limpieza de caché: `MediaPipeline.getInstance().cleanCache(modId, daysOld)` elimina archivos antiguos dentro del directorio de caché del mod.

---

## 4. Formatos Multimedia Soportados

| Plataforma | Video Soportado | Audio Soportado | Notas |
| :--- | :--- | :--- | :--- |
| **PC (Linux, macOS, Windows 10/11)** | MP4, MKV, WebM, AVI, FLV | MP3, AAC, OGG, WAV, FLAC | Decodificación nativa vía `ffmpeg` del OS con fallback a JCodec. |
| **Android (PojavLauncher, FoldCraft)** | **Solo MP4 (H.264)** | MP3, AAC | Usar perfil *Baseline* o *Main*. WebM o MKV producen error en Android. |

Cuando un mod utilice `GuiVideo` para público multiplataforma, se recomienda proveer una variante `.mp4` (H.264) como respaldo.

---

## 5. Qué Evitar al Trabajar con Multimedia

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Llamar `future.join()` o `future.get()` en `update()` o `render()` | Bloquea el hilo principal de Minecraft hasta completar descarga/decodificación | Usar `.thenAccept()` o delegar en `GuiVideo` / `GuiImage` |
| Disparar `GuiToastManager` desde gestores en segundo plano | Acopla el pipeline de I/O a la UI global e interrumpe pantallas activas | Mostrar estado en el propio widget (`GuiVideo`) o en el callback de la pantalla |
| Omitir manejo de errores en recursos externos | URLs caídas o ausencia de códec dejan componentes sin diagnóstico | Configurar `withOnError(...)` en `GuiVideo` y manejar `.exceptionally(...)` |
