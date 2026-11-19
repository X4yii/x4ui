# Containers & Panels (`GuiPanel`, `GuiUnionPanel`, `GuiScrollPanel`, `GuiGridPanel`, `GuiSlot`)

Containers structure the visual hierarchy, arrange children through `ILayoutManager` (`FlexLayout`), and enforce hardware clipping bounds.

---

## 1. `GuiPanel` & Semantic Builders

`GuiPanel` is the base structural container. It supports per-corner radii (`withCornerRadii`), per-side border bitmasks (`withBorderSides`), and static semantic factories aligned with the responsive standard:

```java
// Vertical stack (VERTICAL, hugContent=true, 100% width)
GuiPanel column = GuiPanel.column(8);

// Horizontal row (HORIZONTAL, 100% width) with flexible spacer
GuiPanel row = GuiPanel.row(8);
row.addChild(new GuiLabel(0, 0, "Title", 0xFFFFFFFF));
row.addChild(GuiPanel.spacer());
row.addChild(new GuiButton(0, 0, 20, 20, "X", this::closeScreen));

// Themed card with padding and automatic hugContent height
GuiPanel card = GuiPanel.card(10, 6);

// Top-docked tab with rounded top corners and no bottom border
GuiPanel tab = new GuiPanel(0, 0, 80, 22)
    .withCornerRadii(CornerRadii.top(6))
    .withBorderSides(BorderSides.without(BorderSides.ALL, BorderSides.BOTTOM));
```

---

## 2. `GuiUnionPanel` (Seamless Docked Wings)

`GuiUnionPanel` extends `GuiPanel` to attach docked wing panels (`attachWing(DockSide, GuiPanel, int offset)`) that merge into **one continuous disjoint background** (no alpha overdraw) and an **outer-only border** (eliminating shared seam edges and flattening touching corners):

```java
GuiUnionPanel union = new GuiUnionPanel(0, 0, 176, 166);
union.setBackgroundColor(0xFF1A1D24);
union.setBorderWidth(1);
union.setBorderColor(0xFF2D333F);
union.setCornerRadius(6);

GuiPanel configWing = new GuiPanel(0, 0, 64, 96);
union.attachWing(DockSide.RIGHT, configWing, 12);
```

---

## 3. Cards (`GuiPanel.card`), `GuiScrollPanel` & `GuiGridPanel`

- **`GuiPanel.card(...)`:** creates a themed card container with optional header title and default padding (replacing the legacy `GuiCard` removed in `r1.0b7`).
- **`GuiScrollPanel`:** wraps `contentPanel` inside a scissored viewport with normalized scroll wheel support (`GuiScrollPanel.vertical(220, 6)`).
- **`GuiGridPanel`:** extends `GuiScrollPanel` with `autoColumns` calculation, 1-pass scrollbar reservation, and `addSlots(...)`:

```java
GuiGridPanel grid = GuiGridPanel.slotGrid(120, 2);
grid.addSlots(inventorySlots.inventorySlots, this);
```

---

## 4. `GuiSlot` & `GuiBaseContainer.attachSidePanel`

`GuiSlot` binds a vanilla inventory `Slot` inside `GuiBaseContainer` and re-syncs immediately on layout updates (`onBoundsChanged()`). On `GuiBaseContainer`, `attachSidePanel(DockSide, GuiComponent)` docks side panels to the main window while clamping within viewport bounds, registering `jeiRecipeAreas`, and protecting `hasClickedOutside`.

---

## 5. What to Avoid with Containers

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Calling `scroll.setFlexDirection(...)` instead of `scroll.getContentPanel().setFlexDirection(...)` | Configures the outer viewport wrapper instead of the inner content container | Use `GuiScrollPanel.vertical(...)` or configure `getContentPanel()` |
| Placing tall lists or forms directly in `GuiPanel` without `GuiScrollPanel` | Content clips off-screen at GUI Scale 3x and 4x | Wrap all tall vertical flows inside `GuiScrollPanel` |
