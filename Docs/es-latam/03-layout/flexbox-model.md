# Sistema FlexBox (`GuiPanel`)

X4UI incluye un sistema de diseño (layout) automático similar a Flexbox en la web, usando el contenedor `GuiPanel`.

## Direcciones (`FlexDirection`)

Existen tres formas en las que un panel puede organizar a sus hijos:

* `ABSOLUTE`: El panel no organiza a sus hijos. Cada hijo usa sus propias coordenadas `x` e `y`.
* `HORIZONTAL`: Los hijos se organizan automáticamente en fila, de izquierda a derecha. Ignoran sus propias coordenadas `x` e `y`.
* `VERTICAL`: Los hijos se apilan automáticamente de arriba hacia abajo.

## Espaciado Automático (`Gap`)

Cuando usas `VERTICAL` o `HORIZONTAL`, puedes añadir espacio entre los elementos con la propiedad `gap`.

```java
GuiPanel menu = new GuiPanel(0, 0, 100, 200);
menu.setFlexDirection(FlexDirection.VERTICAL);
menu.setGap(5); // 5 píxeles de separación entre cada hijo
```

## Saltos de Línea (`FlexWrap`)

Si añades muchos elementos, se saldrán del tamaño del panel. Para evitar esto, usa `flexWrap`.

```java
menu.setFlexWrap(true);
```

Cuando un elemento ya no cabe en el panel, automáticamente salta a la siguiente columna (si es `VERTICAL`) o a la siguiente fila (si es `HORIZONTAL`). Es muy útil para hacer cuadrículas o inventarios.

## Auto-Ajuste del Panel

Al terminar de organizar a los hijos, el `GuiPanel` ajustará su propio ancho y alto para envolver exactamente el contenido que tiene dentro. Esto permite que los menús crezcan solos según los datos que contengan.
