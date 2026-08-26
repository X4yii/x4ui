# Margin vs Padding

To adjust spacing, all components have `margin` and `padding`, working just like web CSS.

## Padding (Inner Space)

`padding` pushes the content inward. It only has an effect on containers that organize elements, like `GuiPanel`.

If a panel has `padding(5)`, it will start placing its children 5 pixels away from its edges, leaving an empty inner margin.

## Margin (Outer Space)

`margin` pushes surrounding elements away. It works like a protective shield outside the component.

If you have two stacked buttons in a panel, and the first button has `margin(2)`, the panel will add 2 extra pixels of space before and after that button.

## Performance Notes

Changing `margin` or `padding` in real-time automatically marks the component as modified (`markDirty()`). This tells the parent panel that it needs to recalculate positions efficiently on the next frame.
