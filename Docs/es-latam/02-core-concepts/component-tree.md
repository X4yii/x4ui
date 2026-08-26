# Component Tree (Árbol de Componentes)

Todos los elementos en X4UI son instancias de `GuiComponent` y se organizan como un árbol jerárquico mediante relaciones Padre e Hijo.

## Jerarquía

Todo componente puede contener múltiples hijos. Al usar `B.addChild(A)`:

1. Se asigna a `A` quién es su padre (`B`).
2. Se añade `A` a la lista interna del padre.
3. Se invoca `markDirty()`, obligando al sistema a recalcular posiciones en el siguiente frame.

## Fases de Procesamiento

El árbol se procesa en tres fases desde el panel raíz:

### 1. Update (`update()`)
Ejecutado cada frame de arriba hacia abajo. Si un componente cambió (marcado como "sucio"), recalcula sus matemáticas antes de actualizar a sus hijos.

### 2. Draw (`drawComponent()`)
Ordena la lista de hijos según su propiedad de capa (`layer` o Z-Index) de menor a mayor. Dibuja el padre, luego los hijos. Los elementos con capa mayor se dibujan por encima.

### 3. Eventos (Clics y Teclas)
Se evalúan en orden inverso (de mayor a menor capa). Esto garantiza que el componente que se ve por encima de los demás intercepte el clic antes que los componentes de fondo.
