# Componentes Multimedia (Videos e Imágenes)

En X4UI, los componentes multimedia están diseñados bajo el estricto Principio de Sustitución de Liskov.

## GuiImage (Imágenes Estáticas y 9-Slice)

`GuiImage` maneja tanto texturas empaquetadas (ResourceLocation) como imágenes descargadas de la web, con soporte de dibujado normal y **NineSlice**.

### Carga de Texturas Web

```java
GuiImage banner = (GuiImage) new GuiImage(0, 0, 0, 150).withPercentWidth(1.0f);
// Descarga asíncrona; el juego no se congela
banner.setImage("https://ejemplo.com/fondo.jpg");
```

### Configuraciones 9-Slice
La manera de aplicar divisiones es instanciar un `NineSliceConfig`:

```java
// Configurar los márgenes 9-slice de la textura
banner.setNineSliceConfig(new NineSliceConfig(4, 4, 4, 4));
// Indicarle a GuiImage que dibuje usando los bordes protegidos
banner.setMode(GuiImage.ImageMode.NINESLICE);
```

## GuiVideo (Reproductor de FFmpeg)

`GuiVideo` es una entidad independiente que extiende de `GuiComponent`.
Gestiona su propio renderizador de *frames* asíncronos enlazado a las promesas del `MediaPipeline`.

```java
GuiVideo introVideo = (GuiVideo) new GuiVideo(0, 0, 0, 240).withPercentWidth(1.0f);

introVideo.setVideo("https://ejemplo.com/mi_intro.mp4");
introVideo.setLooping(true); // Repetir al acabar
introVideo.setVolume(0.5f);  // Volumen al 50%
```

### Barra de Carga Integrada
Al iniciar la descarga del video, el componente mostrará internamente un indicador de carga. No inyectará `Toasts` en el gestor global, manteniendo tu pantalla limpia de popups si estás iterando sobre menús pesados.