# Containers and Panels

Containers are the structural building blocks of X4UI. Their primary purpose is to host other components and manage their arrangement using `FlexLayout`.

## GuiPanel

The basic quintessential container. By default, it does not draw a background unless explicitly assigned a `GuiSprite` or if it has a theme with a background panel assigned.

```java
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

GuiPanel panel = new GuiPanel(0, 0, 0, 0).withPercentWidth(1.0f);
panel.setFlexDirection(FlexDirection.VERTICAL); // Stack vertically
panel.setGap(5); // 5 pixels of spacing
panel.setPadding(new Insets(10)); // 10 pixels of internal padding
```

## GuiScrollPanel

This panel includes all the behavior of `GuiPanel` but encapsulates its children within a **scrolling viewport**. It possesses its own sub-container called `contentPanel`.

### The Width Rule in GuiScrollPanel
By a principle of Layout responsibility, the internal `contentPanel` automatically inherits 100% of the width (`.withPercentWidth(1.0f)`) from its constructor if its parent `GuiScrollPanel` receives a width.
Even so, when initializing the parent responsively, the correct syntax is:

```java
GuiScrollPanel scroll = (GuiScrollPanel) new GuiScrollPanel(0, 0, 0, 250).withPercentWidth(1.0f);
scroll.getContentPanel().setFlexDirection(FlexDirection.VERTICAL); // Configure internal layout

scroll.addChild(card1); // Note: GuiScrollPanel redirects addChildren to its contentPanel
scroll.addChild(card2);
```

> [!WARNING]
> Do not confuse `scroll.setFlexDirection(...)` with `scroll.getContentPanel().setFlexDirection(...)`. The former affects how the outer panel arranges its elements (usually useless, since its only direct internal child is the `contentPanel`), while the latter defines how the elements you add to the scroll are organized.
