# Containers

Containers are in charge of organizing and limiting the size of the elements inside them.

## GuiPanel

This is the main container. It works like a `<div>` in web design.

### Layout
Use `setFlexDirection()` to change how it organizes its children:
- `ABSOLUTE`: Does not organize children. Each child decides where it goes.
- `HORIZONTAL`: Places children in a row, from left to right.
- `VERTICAL`: Stacks children from top to bottom.

If you enable `setFlexWrap(true)`, the panel will jump to the next line or column if the elements no longer fit on the screen.

## GuiScrollPanel

It is an advanced version of `GuiPanel` that adds a scrollbar and visually hides whatever falls outside its box.

### How does Scrolling work?
When you add a component to a `GuiScrollPanel`, it is actually added to a larger hidden panel inside it (`contentPanel`). 

### Clipping
To prevent texts or buttons from overflowing the box when scrolling, the panel uses an OpenGL function called `GL_SCISSOR_TEST`. This visually cuts off everything outside the edges.

### Mouse Usage
If the content is taller than the box, the panel automatically draws a scrollbar on the right and allows using the mouse wheel to scroll up or down smoothly.
