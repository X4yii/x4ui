# Advanced Components (`GuiVirtualList`, `GuiDropdown`, `GuiMarkdown`, `GuiModelRender`, `GuiTooltip`)

Specialized widgets for recycled large lists, dropdown selection, Markdown rendering, depth-cleared 3D models, and contextual tooltips.

---

## 1. `GuiVirtualList<T>`

Implements **View Recycling**: only visible rows inside the viewport are instantiated and rendered, keeping memory and layout cost constant even with thousands of entries:

```java
class Entry {
    final String name;
    Entry(String name) { this.name = name; }
}

GuiVirtualList<Entry> list = (GuiVirtualList<Entry>) new GuiVirtualList<Entry>(
    0, 0, 0, 240, 24,
    // Builder: instantiates a visible row (must use withPercentWidth(1.0f))
    item -> new GuiLabel(0, 0, item.name, 0xFFE6E8EC).withPercentWidth(1.0f),
    // Binder: rebinds recycled rows during scrolling
    (comp, item) -> ((GuiLabel) comp).setText(item.name)
).withPercentWidth(1.0f);

list.setItems(entries);
```

---

## 2. `GuiDropdown<T>`

Dropdown selector bound directly to a `State<T>` and an option list:

```java
State<String> selectedMode = new State<>("Normal");
List<String> modes = Arrays.asList("Normal", "Compact", "Advanced");

GuiDropdown<String> dropdown = (GuiDropdown<String>) new GuiDropdown<>(
    0, 0, 0, 22, selectedMode, modes
).withPercentWidth(1.0f);
```

---

## 3. `GuiMarkdown`

Renders formatted Markdown (headings, lists, code, links, separators) adapting to container width and assigned `IFont`:

```java
GuiMarkdown docView = (GuiMarkdown) new GuiMarkdown(0, 0, 0, "# Guide\nFormatted **content**.")
    .withFont("roboto_regular")
    .withPercentWidth(1.0f);
```

---

## 4. `GuiModelRender` & `GuiTooltip`

- **`GuiModelRender`:** renders 3D entities (`setEntity`) or item stacks (`setItemStack`) with safe depth buffer clearing (`IGraphics.clearDepthBuffer()`), scaling (`setScale`), rotation (`setRotation`), and drag interaction (`setAllowDragRotation`).
- **`GuiTooltip`:** floating tooltip widget, also configurable via `.withTooltip(...)` on any `GuiComponent`.

---

## 5. What to Avoid with Advanced Components

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Omitting `.withPercentWidth(1.0f)` in `GuiVirtualList`'s builder lambda | Row retains width `0` and renders invisible | Return each row root with `.withPercentWidth(1.0f)` |
| Populating `GuiScrollPanel` with thousands of `GuiPanel` children | Exhausts memory and slows layout passes | Use `GuiVirtualList<T>` with view recycling |
