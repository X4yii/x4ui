# Event System

X4UI automatically distributes clicks, keyboard input, and mouse interactions through the component tree.

## Propagation and Priority

When you click the screen, the event travels from the main element downwards.
To prevent pressing buttons that are hidden behind others, the child list is checked in reverse order (elements with a higher `layer` are checked first).

### Cancellation
If a component handles the click event (meaning the method returns `true`), the process stops immediately. This prevents the click from "passing through" the interface.

## Main Interactions

Components can override these methods:

- `mouseClicked`: When a mouse button is pressed.
- `mouseReleased`: When the button is released.
- `mouseClickMove`: When the mouse is dragged while holding a button. Useful for scrollbars.
- `handleMouseScroll`: When the mouse wheel is used.
- `keyTyped`: When a key is pressed.

## Requirements for Intercepting Events

For a component to detect clicks:
1. It must be visible (`visible = true`).
2. It must be enabled (`enabled = true`).
3. The mouse must be hovering over its area (`isMouseOver`).
