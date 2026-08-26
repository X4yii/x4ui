# Coordinate System

Unlike Vanilla, X4UI uses **relative** coordinates.

## Relative vs Absolute

The `x` and `y` properties of a `GuiComponent` are always **relative to the top-left corner of its Parent.**

### Example:
If `Panel A` is at screen position `(X: 50, Y: 50)`, and you add `Button B` at local position `(X: 10, Y: 10)`, X4UI will draw the button on the real screen at `(X: 60, Y: 60)`.

## Key Functions

To get the real screen position during rendering, use:

- `getAbsoluteX()`
- `getAbsoluteY()`

These methods automatically sum up all `x` and `y` coordinates from the component up to the root element. Note that `margin` and `padding` are **not** included in this calculation; they are applied separately by the parent's layout engine (`GuiPanel.layoutChildren()`).

### Rules
- When creating components inside FlexBox panels, don't worry about their `x` and `y` coordinates; the panel will overwrite them.
- If you create a custom component and override `drawSelf`, **always** use `getAbsoluteX()` and `getAbsoluteY()` to draw things on the screen with OpenGL.
