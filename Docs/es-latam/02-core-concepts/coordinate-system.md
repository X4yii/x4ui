# Coordinate System (Sistema de Coordenadas)

A diferencia de Vanilla, X4UI usa coordenadas **relativas**.

## Relativas vs Absolutas

Las propiedades `x` e `y` de un `GuiComponent` son siempre **relativas a la esquina superior izquierda de su Padre.**

### Ejemplo:
Si el `Panel A` está en la posición `(X: 50, Y: 50)` de la pantalla, y le agregas un `Button B` en su posición local `(X: 10, Y: 10)`, X4UI dibujará el botón en la pantalla real en `(X: 60, Y: 60)`.

## Funciones Clave

Para obtener la posición real en la pantalla durante el renderizado, usa:

- `getAbsoluteX()`
- `getAbsoluteY()`

Estos métodos suman automáticamente todas las coordenadas `x` e `y` desde el componente hasta el elemento raíz. Ten en cuenta que `margin` y `padding` **no** se incluyen en este cálculo; se aplican por separado por el motor de layout del padre (`GuiPanel.layoutChildren()`).

### Reglas
- Al crear componentes dentro de paneles FlexBox, no te preocupes por sus coordenadas `x` e `y`; el panel las sobrescribirá.
- Si creas un componente personalizado y sobrescribes `drawSelf`, **siempre** usa `getAbsoluteX()` y `getAbsoluteY()` para dibujar cosas en pantalla con OpenGL.
