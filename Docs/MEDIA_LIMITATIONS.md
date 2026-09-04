# Limitaciones del Motor Multimedia (X4UI Media Engine)

El componente `GuiVideo` y `GuiAudio` de X4UI está diseñado para ofrecer la máxima compatibilidad posible entre entornos de PC tradicionales y launchers de Android (como PojavLauncher o FoldCraftLauncher). Sin embargo, debido a las restricciones de seguridad y arquitectura de estos sistemas operativos, existen diferencias fundamentales en el funcionamiento.

## PC (Windows, Linux, macOS)
En entornos de escritorio, X4UI utiliza un motor **Híbrido de Alto Rendimiento** basado en FFmpeg.
- **Rendimiento:** Soporte completo de Aceleración y decodificación multi-hilo nativa.
- **Formatos de Vídeo:** Soporte casi universal (MP4, MKV, WebM, AVI, FLV, etc).
- **Formatos de Audio:** Soporte universal (MP3, AAC, OGG, WAV, FLAC, etc).
- **Resoluciones:** 1080p a 60fps (y superior, dependiendo del procesador).

## Android (PojavLauncher, FoldCraft)
En dispositivos Android (especialmente Android 10 y superior), el sistema operativo impone políticas estrictas de seguridad (SELinux) y marca el almacenamiento externo (donde suele estar la carpeta `.minecraft`) con la bandera `noexec`. Esto impide la ejecución de binarios de decodificación externos.

Para garantizar que el mod nunca cause un *Crash* (cierre forzoso) bajo estas restricciones, X4UI activa automáticamente un mecanismo de defensa (**Graceful Degradation**) y cambia a un decodificador **Pure-Java** interno.

### Limitaciones en Modo Pure-Java (Android):
1. **Formatos Soportados:** **SÓLO MP4 con codificación H.264**. 
   * *Si intentas reproducir WebM o MKV en Android, no funcionará o lanzará error.*
2. **Audio:** Depende de los bindings nativos de OpenAL del launcher. Si el launcher no expone correctamente AL10 a la JVM, el audio podría fallar.
3. **Rendimiento (CPU Software Decoding):** El decodificador Java no tiene aceleración por hardware nativa. La fluidez del vídeo (FPS) estará estrictamente limitada a la potencia bruta del procesador del teléfono. Se recomiendan vídeos de 720p a 30fps para dispositivos de gama media-baja.
4. **Perfil H.264:** Se recomienda encarecidamente usar el perfil *Baseline* o *Main* al codificar vídeos para que el decodificador Pure-Java los procese sin errores de memoria.

---
**Nota para Desarrolladores:**
Si tu mod hace uso intensivo de `GuiVideo`, asegúrate de incluir versiones comprimidas en `.mp4` (H.264) de tus cinemáticas o menús animados, de modo que los jugadores de Android no se queden sin ver tu contenido.
