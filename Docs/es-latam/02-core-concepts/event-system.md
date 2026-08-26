# Sistema de Eventos

X4UI distribuye los clics, el teclado y el ratón automáticamente a través del árbol de componentes.

## Propagación y Prioridad

Cuando haces clic en la pantalla, el evento viaja desde el elemento principal hacia abajo.
Para evitar que presiones botones que están ocultos detrás de otros, se revisa la lista de hijos en orden inverso (los elementos con mayor capa o `layer` se comprueban primero).

### Cancelación
Si un componente maneja el evento de clic (es decir, el método devuelve `true`), el proceso se detiene inmediatamente. Esto evita que el clic "atraviese" la interfaz.

## Interacciones Principales

Los componentes pueden usar estos métodos:

- `mouseClicked`: Cuando se presiona un botón del ratón.
- `mouseReleased`: Cuando se suelta el botón.
- `mouseClickMove`: Cuando se arrastra el ratón presionado. Útil para barras de desplazamiento.
- `handleMouseScroll`: Cuando se usa la rueda del ratón.
- `keyTyped`: Cuando se presiona una tecla.

## Requisitos para Interceptar Eventos

Para que un componente detecte clics:
1. Debe estar visible (`visible = true`).
2. Debe estar habilitado (`enabled = true`).
3. El ratón debe estar dentro de su área (`isMouseOver`).
