# Margin vs Padding

Para ajustar el espaciado, todos los componentes tienen `margin` (margen) y `padding` (relleno), funcionando igual que en CSS web.

## Padding (Espacio Interior)

El `padding` empuja el contenido hacia adentro del componente. Solo tiene efecto en contenedores que organizan elementos, como el `GuiPanel`.

Si un panel tiene `padding(5)`, empezará a colocar a sus hijos a 5 píxeles de distancia de sus bordes, dejando un margen interno vacío.

## Margin (Espacio Exterior)

El `margin` empuja a los elementos que están alrededor. Funciona como un escudo protector fuera del componente.

Si tienes dos botones apilados en un panel, y el primer botón tiene `margin(2)`, el panel añadirá 2 píxeles extra de espacio antes y después de ese botón.

## Notas de Rendimiento

Cambiar el `margin` o el `padding` en tiempo real marca automáticamente al componente como modificado (`markDirty()`). Esto le dice al panel que debe recalcular las posiciones en el siguiente frame de forma eficiente.
