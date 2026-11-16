# Containers & Panels (`GuiPanel`, `GuiCard`, `GuiScrollPanel`, `GuiSlot`)

Containers structure the visual hierarchy, arrange children through `ILayoutManager` (`FlexLayout`), and enforce hardware clipping bounds.

---

## 1. `GuiPanel` & Semantic Builders

`GuiPanel` is the base structural container. In `r1.0b6` it provides static semantic factories aligned with the responsive standard:

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
```

Manual layout configuration is also supported via `FlexDirection`, `FlexAlign`, and `Insets`:

```java
GuiPanel panel = new GuiPanel(0, 0, 0, 0).withPercentWidth(1.0f);
panel.setFlexDirection(FlexDirection.VERTICAL);
panel.setGap(6);
panel.setPadding(new Insets(10));
```

---

## 2. `GuiCard`

`GuiCard` extends `GuiPanel` with a built-in header label (`getTitleLabel()`) styled by the active theme:

```java
GuiCard card = (GuiCard) new GuiCard(0, 0, 0, 120, "Statistics")
    .withPercentWidth(1.0f);
```

---

## 3. `GuiScrollPanel`

`GuiScrollPanel` wraps an internal `contentPanel` inside a scissored viewport with normalized scroll wheel support (`UIInput`):

```java
// Recommended: preconfigured vertical scroll panel
GuiScrollPanel scroll = GuiScrollPanel.vertical(220, 6);
scroll.addChild(card1); // addChild() forwards to contentPanel
scroll.addChild(card2);
```

When content overflows the viewport height, `GuiScrollPanel` reserves scrollbar width automatically to prevent right-edge clipping.

---

## 4. `GuiSlot`

`GuiSlot` integrates a vanilla inventory `Slot` inside `GuiBaseContainer`:

```java
for (Slot slot : inventorySlots.inventorySlots) {
    rootPanel.addChild(new GuiSlot(slot, this));
}
```

---

## 5. What to Avoid with Containers

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Calling `scroll.setFlexDirection(...)` instead of `scroll.getContentPanel().setFlexDirection(...)` | Configures the outer viewport wrapper instead of the inner content container | Use `GuiScrollPanel.vertical(...)` or configure `getContentPanel()` |
| Placing tall lists or forms directly in `GuiPanel` without `GuiScrollPanel` | Content clips off-screen at GUI Scale 3x and 4x | Wrap all tall vertical flows inside `GuiScrollPanel` |
