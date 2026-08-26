# Containers (Contenedores)

Los contenedores se encargan de organizar y limitar el tamaño de los elementos que guardan en su interior.

## GuiPanel

Es el contenedor principal. Funciona como un `<div>` en diseño web.

### Organización (Layout)
Usa `setFlexDirection()` para cambiar cómo organiza a sus hijos:
- `ABSOLUTE`: No organiza a los hijos. Cada hijo decide dónde va.
- `HORIZONTAL`: Coloca a los hijos en fila, de izquierda a derecha.
- `VERTICAL`: Apila a los hijos de arriba hacia abajo.

Si activas `setFlexWrap(true)`, el panel saltará a la siguiente línea o columna si los elementos ya no caben en la pantalla.

## GuiScrollPanel

Es una versión avanzada de `GuiPanel` que añade una barra de desplazamiento (Scroll) y oculta visualmente lo que quede fuera de su caja.

### ¿Cómo funciona el Scroll?
Cuando añades un componente a un `GuiScrollPanel`, en realidad se añade a un panel oculto más grande en su interior (`contentPanel`). 

### Recorte (Clipping)
Para evitar que los textos o botones se salgan de la caja al hacer scroll, el panel usa una función de OpenGL llamada `GL_SCISSOR_TEST`. Esto corta visualmente todo lo que quede fuera de los bordes.

### Uso del Ratón
Si el contenido es más alto que la caja, el panel dibuja automáticamente una barra a la derecha y permite usar la rueda del ratón para subir o bajar el contenido de forma suave (Smooth Scrolling).
