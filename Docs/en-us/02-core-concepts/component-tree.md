# Component Tree

All elements in X4UI are instances of `GuiComponent` and are organized as a hierarchical tree through Parent and Child relationships.

## Hierarchy

Every component can hold multiple children. When using `B.addChild(A)`:

1. `A` is assigned its parent (`B`).
2. `A` is added to the parent's internal child list.
3. `markDirty()` is invoked, forcing the system to recalculate positions on the next frame.

## Processing Phases

The tree is processed in three phases starting from the root panel:

### 1. Update (`update()`)
Executed every frame from top to bottom. If a component changed (marked as "dirty"), it recalculates its math before updating its children.

### 2. Draw (`drawComponent()`)
Sorts the child list by their layer property (`layer` or Z-Index) from lowest to highest. It draws the parent, then the children. Elements with a higher layer are drawn on top.

### 3. Events (Clicks and Keys)
Evaluated in reverse order (from highest to lowest layer). This ensures that the component visually on top intercepts the click before background components do.
