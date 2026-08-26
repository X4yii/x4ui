# Architecture

X4UI relies on a hierarchical component tree, hiding complex OpenGL calls.

## Component Tree (DOM)

Every interface has a root node that contains child components. These children can contain more children.

The main class is `GuiComponent`. Every element inherits from it:
- **Containers**: Group elements together (`GuiPanel`, `GuiScrollPanel`).
- **Interactives**: Receive clicks or input (`GuiButton`, `GuiToggle`).
- **Visuals**: Display information (`GuiSprite`, `GuiLabel`).

## Lifecycle

1. **Creation**: Instantiate the component in code.
2. **Mounting**: Add it to a parent using `addChild()`.
3. **Update**: Executed every frame via `update()`. Layouts and animations are computed here.
4. **Draw**: Rendered via `drawComponent()`. First the component draws itself (`drawSelf()`), then it draws its children sorted by `layer` from lowest to highest (so higher layers appear on top).
5. **Events**: Clicks are processed from highest to lowest layer (top-down), ensuring the top-most element intercepts the click first.

## Optimization (Dirty Flags)

X4UI uses boolean flags to save CPU time:
- `needsLayout`: True if size, margins, or visibility changes. Forces the parent to recalculate positions.
- `needsRenderSort`: True if the `layer` changes. Avoids sorting elements every single frame.

Calling `markDirty()` on a component pushes these flags up the tree.
