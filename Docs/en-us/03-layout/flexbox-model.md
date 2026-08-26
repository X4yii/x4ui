# FlexBox System (`GuiPanel`)

X4UI includes an automatic layout system similar to Flexbox on the web, using the `GuiPanel` container.

## Directions (`FlexDirection`)

There are three ways a panel can organize its children:

* `ABSOLUTE`: The panel does not organize its children. Each child uses its own `x` and `y` coordinates.
* `HORIZONTAL`: Children are automatically organized in a row, from left to right. They ignore their own `x` and `y` coordinates.
* `VERTICAL`: Children are automatically stacked from top to bottom.

## Automatic Spacing (`Gap`)

When using `VERTICAL` or `HORIZONTAL`, you can add space between elements with the `gap` property.

```java
GuiPanel menu = new GuiPanel(0, 0, 100, 200);
menu.setFlexDirection(FlexDirection.VERTICAL);
menu.setGap(5); // 5 pixels of separation between each child
```

## Line Breaks (`FlexWrap`)

If you add too many elements, they will overflow the panel's size. To prevent this, use `flexWrap`.

```java
menu.setFlexWrap(true);
```

When an element no longer fits in the panel, it automatically jumps to the next column (if `VERTICAL`) or the next row (if `HORIZONTAL`). This is very useful for making grids or inventories.

## Panel Auto-Sizing

After organizing the children, the `GuiPanel` will adjust its own width and height to exactly wrap the content inside it. This allows menus to grow dynamically based on the data they contain.
