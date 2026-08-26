# Componentes Visuales

Elementos creados simplemente para mostrar información en pantalla. 

## GuiLabel

Dibuja un texto simple de una sola línea.

### Uso
No necesitas decirle el ancho exacto; el texto calcula su propio tamaño automáticamente.

```java
GuiLabel cordY = new GuiLabel(0, 0, "Coordenada Y: 64", 0xFF00FF00); // Texto verde
cordY.setCentered(true);
```
Si usas `setCentered(true)`, el texto se dibujará centrado horizontalmente desde la coordenada que le diste.

## GuiSprite

Dibuja imágenes (texturas).

### Bordes Inteligentes (9-Slice)
Normalmente, si estiras una imagen en Minecraft, los bordes se deforman y se ven borrosos o alargados.
`GuiSprite` soluciona esto permitiéndote definir esquinas estáticas.

```java
ResourceLocation textura = new ResourceLocation("mimod", "textures/gui/panel_maquina.png");
GuiSprite fondo = new GuiSprite(x, y, anchoMenu, altoMenu, textura, 256, 256);
fondo.setSliceSize(8); // Mantener 8 píxeles de los bordes sin estirar
```
Esto estirará solo el centro de la imagen, manteniendo los bordes perfectos sin importar de qué tamaño sea el menú.

## GuiModelRender

Dibuja un modelo 3D (como un jugador o un mob) o un ítem en pantalla.

### Controles
```java
GuiModelRender mr = new GuiModelRender(x, y, ancho, alto);
// Muestra a un Creeper o la entidad que necesites
mr.setEntity(new EntityCreeper(Minecraft.getMinecraft().world)); 
mr.setScale(30f); // Tamaño del modelo en el menú
mr.setAllowDragRotation(true); // Permite al usuario rotarlo con el ratón
mr.setLookAtCursor(true); // Hace que la cabeza del modelo siga al ratón
```

Este componente se encarga de todo de manera segura: la iluminación, la rotación, y evita que se rompa el cuello del modelo real cuando cierras el menú.

## GuiSlot

Es un puente especial que sirve para que puedas usar Slots reales de Minecraft dentro de un menú de X4UI.

Normalmente los slots son difíciles de mover en Vanilla, pero si metes un `GuiSlot` dentro de un `GuiPanel`, X4UI se encargará de mover el slot real a las coordenadas exactas de la pantalla para que funcione perfectamente con tu layout.

## GuiImage

Muestra una imagen desde un `ResourceLocation` o la descarga desde una URL con caché en disco.

### Uso
```java
// Desde un ResourceLocation
GuiImage img = new GuiImage(x, y, 64, 64);
img.setResource(new ResourceLocation("mimod", "textures/gui/logo.png"));

// Desde una URL (se almacena en caché automáticamente)
GuiImage avatar = new GuiImage(x, y, 32, 32);
avatar.setUrl("https://ejemplo.com/avatar.png");

// Con mapeo UV (mostrar una porción de la textura)
GuiImage crop = new GuiImage(x, y, 16, 16);
crop.setResource(texture);
crop.setUV(0.0f, 0.0f, 0.5f, 0.5f); // Cuarto superior izquierdo
```

## GuiVideo

Un componente de reproductor de video simulado. Extiende `GuiImage` y muestra una superposición de play/pause con una barra de progreso. Actualmente es un placeholder para soporte de video real en el futuro.

### Uso
```java
GuiVideo video = new GuiVideo(x, y, 200, 150);
video.setVideoUrl("https://ejemplo.com/intro.mp4");
video.play();

// El toggle play/pause al hacer clic es automático
```
