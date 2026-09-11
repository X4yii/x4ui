# 02 - Recursos Remotos y Asincronía (MediaPipeline)

Este documento describe cómo X4UI maneja operaciones asíncronas, como descargas de imágenes o procesamiento de videos FFmpeg en segundo plano, mediante el sistema desacoplado `MediaPipeline`.

---

## 1. El Paradigma de Asincronía Desacoplada

El `MediaPipeline` está completamente aislado de la UI. Retorna promesas en forma de `CompletableFuture`s, permitiéndole al invocador (o al componente GUI específico) decidir **cuándo** y **cómo** notificar al usuario.

---

## 2. Uso de MediaPipeline

Cuando necesites descargar o procesar recursos externos de manera controlada y aislada en caché por Mod ID, invoca al `MediaPipeline`:

```java
import com.x4yi.x4ui.impl.client.media.MediaPipeline;

// Obtener un video o recurso remoto de forma aislada para tu mod
CompletableFuture<File> mediaFuture = MediaPipeline.getInstance().obtainMedia("mimodid", "https://example.com/video.mp4", (status, percent) -> {
    System.out.println("Progreso [" + status + "]: " + percent + "%");
});

mediaFuture.thenAccept(file -> {
    // El archivo se descargó y optimizó con éxito en .minecraft/x4ui/cache/mimodid/
    System.out.println("Disponible en: " + file.getAbsolutePath());
}).exceptionally(ex -> {
    // Manejar errores de red o fallo de FFmpeg
    ex.printStackTrace();
    return null;
});
```

---

## 3. Integración en UI (`GuiVideo` y `GuiImage`)

No necesitas manejar `CompletableFuture`s manualmente si solo quieres mostrar multimedia; los componentes nativos ya incorporan lógica asíncrona no bloqueante con estados de carga internos.

### GuiVideo
`GuiVideo` actúa como un componente asíncrono auto-contenido. Al asignarle una URL mediante `setVideoUrl(url)`, solicita el recurso al `MediaPipeline` y despliega el estado de carga exclusivamente dentro de sus límites.

```java
GuiVideo videoPlayer = (GuiVideo) new GuiVideo(0, 0, 0, 200).withPercentWidth(1.0f);
videoPlayer.setVideoUrl("https://example.com/trailer.mp4");
videoPlayer.play();
```

### GuiImage
De igual forma, `GuiImage` soporta tanto recursos locales (`ResourceLocation`) como texturas web seguras mediante `setUrl(url)`:

```java
GuiImage banner = (GuiImage) new GuiImage(0, 0, 0, 100).withPercentWidth(1.0f);
banner.setUrl("https://example.com/banner.png");
```

---

## 4. Qué Evitar Hacer (Antipatrones Críticos)

> [!CAUTION]
> **1. NUNCA bloquear el hilo esperando un CompletableFuture.**
> Jamás llames a `future.join()` ni `future.get()` dentro de un método de UI (como `update()` o `render()`). Congelarás el cliente de Minecraft entero, forzando un reinicio.

> [!CAUTION]
> **2. NUNCA inyectar Toasts globales desde gestores de fondo.**
> El `MediaPipeline` nunca debe referenciar a `GuiToastManager`. Si deseas notificar la finalización de una descarga, hazlo en el callback `.thenAccept()` situado exclusivamente dentro de la lógica de tu *Screen*.
