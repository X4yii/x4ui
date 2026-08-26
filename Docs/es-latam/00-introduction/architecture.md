# Arquitectura

X4UI se basa en un árbol jerárquico de componentes, ocultando las llamadas complejas a OpenGL.

## Component Tree (DOM)

Toda interfaz tiene un nodo raíz que contiene componentes hijos. Estos hijos pueden contener más hijos.

La clase principal es `GuiComponent`. Todos los elementos heredan de ella:
- **Contenedores**: Agrupan elementos (`GuiPanel`, `GuiScrollPanel`).
- **Interactivos**: Reciben clics o input (`GuiButton`, `GuiToggle`).
- **Visuales**: Muestran información (`GuiSprite`, `GuiLabel`).

## Ciclo de Vida

1. **Creación**: Instancias el componente en código.
2. **Montaje**: Lo añades a un padre usando `addChild()`.
3. **Update**: Ejecutado cada frame mediante `update()`. Aquí se calculan layouts y animaciones.
4. **Draw**: Renderizado mediante `drawComponent()`. Primero el componente se dibuja a sí mismo (`drawSelf()`), luego dibuja sus hijos ordenados por `layer` de menor a mayor (así las capas más altas quedan por encima).
5. **Eventos**: Los clics se procesan de mayor a menor capa (top-down), asegurando que el elemento superior intercepte el clic primero.

## Optimización (Dirty Flags)

X4UI utiliza indicadores (*flags*) booleanos para ahorrar CPU:
- `needsLayout`: Verdadero si el tamaño, márgenes o visibilidad cambian. Obliga al padre a recalcular posiciones.
- `needsRenderSort`: Verdadero si cambia la capa (`layer`). Evita reordenar elementos en cada frame.

Llamar a `markDirty()` en un componente activa estas banderas hacia arriba en el árbol.
