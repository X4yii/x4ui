# 04 - Responsive Design & Relative Positioning (GUI Scale 1x to 4x)

This document describes the official responsive design standard in X4UI. By enforcing the use of `FlexLayout` and percentage-based sizing, we guarantee that interfaces remain perfectly readable and aligned under any resolution and Minecraft Forge 1.12.2 scale factor (GUI Scale 1, 2, 3, 4, and Auto).

---

## 1. The Minecraft Scaling Problem

Minecraft calculates logical screen dimensions (`ScaledResolution`) by dividing the actual pixel resolution of the window by the scale factor:

$$\text{scaledWidth} = \left\lfloor \frac{\text{displayWidth}}{\text{scaleFactor}} \right\rfloor, \quad \text{scaledHeight} = \left\lfloor \frac{\text{displayHeight}}{\text{scaleFactor}} \right\rfloor$$

If you use absolute dimensions and fixed `(X, Y)` coordinates, your interface will fail and clip out of bounds on large scales (Scale 3 and 4). This is why fixed absolute positioning is considered a **severe anti-pattern**.

---

## 2. The Four Pillars of the X4UI Responsive Standard

To build truly robust and scalable UIs, these four golden rules must be followed:

---

### Pillar 1: Fluent Percentage Sizing in `GuiComponent`

`GuiComponent` features native support for dimensions and coordinates dynamically relative to the parent component via the fluent API:

```java
// Occupy 100% of the parent container's width
card.withPercentWidth(1.0f);

// Divide into two 50% width columns
colLeft.withPercentWidth(0.5f);
colRight.withPercentWidth(0.5f);

// Set both percentage width and height simultaneously
modal.withPercentSize(0.8f, 0.6f); // 80% width, 60% height

// Manual percentage positioning (centered at 50% X)
badge.withPercentPosition(0.5f, 0.1f);
```

#### Responsive Fluent Methods:
- `.withPercentWidth(float percent)`
- `.withPercentHeight(float percent)`
- `.withPercentSize(float percentWidth, float percentHeight)`
- `.withPercentPosition(float percentX, float percentY)`

*Note: Passing `-1.0f` disables percentage calculations.*

---

### Pillar 2: `FlexLayout` as the Core Layout Engine

Whenever grouping multiple elements, use a container with `FlexLayout` instead of assigning `.withPercentPosition()` to every single element. The `FlexLayout` will intelligently stack and arrange components elastically.

```java
GuiPanel gridContainer = new GuiPanel(0, 0, 0, 0).withPercentWidth(1.0f);
gridContainer.setFlexDirection(FlexDirection.VERTICAL);
gridContainer.setGap(8); // Automatic spacing between items
```

#### Parent Container Sizing Control (`hugContent`):
Starting in `r1.0b5`, `FlexLayout` is **non-destructive** by default (`hugContent = false`): it respects the explicit or responsive percentage dimensions of the parent container (`withPercentWidth`/`withPercentHeight`).

If you want the parent container to **automatically hug and adapt its size** to the combined dimensions of its visible children ("Hug / Fit Content" behavior), enable it explicitly:

```java
// Parent container will resize its height/width to fit its children exactly
FlexLayout layout = new FlexLayout(FlexDirection.VERTICAL, 8, false)
    .withHugContent(true);

panel.setLayoutManager(layout);
```

> [!NOTE]
> - When `hugContent` is `false` (default): the parent panel preserves its defined size (or percentage), making it ideal for centered dialogs, fixed sidebars, or split panes.
> - When `hugContent` is `true`: the parent panel dynamically expands or contracts depending on its visible children, ideal for dynamic cards, floating popups, or item containers inside a scroll panel.

> [!WARNING]
> **The FlexLayout Sizing Loop with Hug Content:**
> If a container uses `FlexDirection.VERTICAL` with `hugContent(true)`, the container calculates its height by summing all child heights. Therefore, children **cannot** use `.withPercentHeight(...)` (a child cannot take 100% height of a parent whose height depends on that child).
> 
> **Universal Rule for VERTICAL FlexLayout:**
> Containers should use `.withPercentWidth(1.0f)` to expand horizontally. Their internal children should also use `.withPercentWidth(1.0f)` (or fractions). **The height of the children must be absolute (in pixels)** so the engine can properly stack them vertically.

---

### Pillar 3: Mandatory Containment in `GuiScrollPanel`

**Golden Rule:** *All vertical content (lists, forms, panel flows) must reside inside a `GuiScrollPanel`.*

In GUI Scale 1x and 2x, screen height is large and the `GuiScrollPanel` won't display a scrollbar. In GUI Scale 4x, the available height can be minuscule, and scrolling will automatically activate, ensuring 100% content accessibility.

**Beware of ScrollPanel Content Widths:** 
The internal container of a `GuiScrollPanel` is exposed via `.getContentPanel()`. To ensure everything inside it grows responsively, apply the rule from Pillar 2:

```java
GuiScrollPanel scrollPanel = (GuiScrollPanel) new GuiScrollPanel(0, 0, 0, 300).withPercentWidth(1.0f);
scrollPanel.getContentPanel().setFlexDirection(FlexDirection.VERTICAL);
scrollPanel.getContentPanel().withPercentWidth(1.0f); // Mandatory!
```

---

### Pillar 4: Never Destroy Hierarchy in OnScreenResize

**Strictly prohibited anti-pattern:** Overwriting `onScreenResize()` in the main *Screen* class and executing methods like `initComponents()` which clear all components and re-instantiate them from scratch using the new `this.width`.

Thanks to `FlexLayout` and `.withPercentWidth()`, containers will automatically recalculate and arrange absolutely all of their children when the player resizes the screen, preserving text input states, scroll positions, and keyboard focus without interruption.

---

## What To Avoid (Critical Responsive Anti-Patterns)

> [!CAUTION]
> **1. NEVER use Absolute Coordinates (X,Y) for structural positioning.**
> Statements like `new GuiButton(340, 150, ...)` will cause clipping and broken screens. Use `0` for widths and coordinates, and delegate the responsibility to `FlexLayout` and `.withPercentWidth()`.

> [!CAUTION]
> **2. NEVER purge and recreate the UI during `onScreenResize`.**
> Recreating thousands of components interrupts lifecycles, cancels `GuiAnimator` instances, and causes state leaks if not handled perfectly. Trust the layout engine.

> [!WARNING]
> **3. NEVER create a Horizontal layout with Wrap (`FlexWrap = true`) without explicit Heights.**
> If your components wrap to a new line, the entire row will visually collapse if you haven't defined the base absolute height of each element in the row.
