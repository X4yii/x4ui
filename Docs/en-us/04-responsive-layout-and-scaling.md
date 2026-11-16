# 04 - Responsive Design & Relative Positioning (GUI Scale 1x to 4x)

This document defines X4UI's official responsive design standard, ensuring interfaces remain readable, aligned, and accessible across any window resolution and scale factor (`GUI Scale` 1, 2, 3, 4, and Auto) on Forge 1.12.2 and CleanroomMC.

---

## 1. The Minecraft Scaling Problem

Minecraft computes logical screen dimensions (`ScaledResolution`) by dividing actual window pixel dimensions by the active scale factor:

$$\text{scaledWidth} = \left\lfloor \frac{\text{displayWidth}}{\text{scaleFactor}} \right\rfloor, \quad \text{scaledHeight} = \left\lfloor \frac{\text{displayHeight}}{\text{scaleFactor}} \right\rfloor$$

A `1920x1080` window shrinks from `960x540` at Scale 2 to `480x270` at Scale 4. Interfaces built with fixed `(X, Y)` coordinates or wide hard-coded widths clip out of bounds or overlap.

---

## 2. The Four Pillars of the X4UI Responsive Standard

### Pillar 1: Percentage Sizing on `GuiComponent`

`GuiComponent` resolves dimensions and positions relative to its parent via fluent methods (passing `-1.0f` disables percentage sizing):

```java
card.withPercentWidth(1.0f);         // 100% of parent available width
colLeft.withPercentWidth(0.5f);      // 50% of parent available width
modal.withPercentSize(0.8f, 0.6f);   // 80% width, 60% height
badge.withPercentPosition(0.5f, 0.1f);
```

### Pillar 2: `FlexLayout`, Alignment (`FlexAlign`) & `GuiPanel` Primitives

Group elements with `GuiPanel` semantic builders and `GuiBaseScreen` helpers instead of computing manual coordinates:

```java
// Centered modal window with automatic background dimming
GuiPanel window = createCenteredWindow(420, 300);

// Header row with title on the left and close button on the right
GuiPanel header = GuiPanel.row(8);
header.addChild(new GuiLabel(0, 0, "Control Panel", 0xFFFFFFFF));
header.addChild(GuiPanel.spacer());
header.addChild(new GuiButton(0, 0, 20, 20, "X", this::closeScreen));

// Themed section card with padding and automatic height (hugContent)
GuiPanel section = GuiPanel.card(10, 6);
```

- **Alignment (`FlexAlign`):** Supports `START`, `CENTER`, `END`, and `STRETCH` on cross axis (`setAlignItems`) and main axis (`setJustifyContent`), paired with `GuiPanel.spacer()`.
- **`hugContent` behavior:** By default `FlexLayout` is **non-destructive** (`hugContent = false`), preserving explicit or percentage parent bounds. Enabling `.withHugContent(true)` fits the container to its visible children.
- **Circular sizing rule (`VERTICAL` + `hugContent(true)`):** When a container's height is summed from its children, children must specify pixel heights (not `withPercentHeight(...)`) and use `withPercentWidth(...)` for horizontal responsiveness.

### Pillar 3: Mandatory Containment in `GuiScrollPanel`

All tall vertical content (lists, forms, stacked sections) must reside inside a `GuiScrollPanel`:

```java
// Preconfigured vertical scroll (VERTICAL contentPanel, hugContent=true, 100% width)
GuiScrollPanel scrollPanel = GuiScrollPanel.vertical(180, 6);
```

`GuiScrollPanel` automatically reserves scrollbar width when content overflows the viewport, preventing right-edge clipping at GUI Scale 3x and 4x.

### Pillar 4: Hierarchy Preservation on `onScreenResize`

Never clear and recreate the component hierarchy inside `onScreenResize()`. When the window resizes, `FlexLayout` and `withPercentWidth` recalculate bounds automatically while preserving text input values, focus, and scroll offsets.

---

## 3. What to Avoid in Layout Design

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Fixed `(X, Y)` coordinates for structural layout | Clips or overlaps at GUI Scale 3x and 4x | Pass `0` for `X, Y`, use `FlexLayout` and `withPercentWidth()` |
| Calling `initComponents()` or `clearChildren()` in `onScreenResize()` | Destroys input focus, text state, scroll offsets, and active animations | Rely on automatic `FlexLayout` recalculation |
| Using `withPercentHeight()` inside a `VERTICAL` `hugContent(true)` container | Creates a circular height dependency between parent and child | Specify pixel heights on children and use `withPercentWidth()` |
| Enabling `FlexWrap = true` without explicit element heights | Wrapped rows collapse visually to height `0` | Assign an explicit height to each item in the row |
