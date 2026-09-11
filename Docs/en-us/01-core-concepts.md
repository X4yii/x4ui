# 01 - Core Concepts & Architecture

This document outlines the core architecture of X4UI: the framework's philosophy, the hierarchical component tree, the standardized lifecycle, the flexbox layout system, event propagation, reactive state (`State<T>`), and the critical anti-patterns you must avoid.

---



## 2. Hierarchical Component Tree

X4UI organizes the user interface into a retained-mode hierarchical tree (Composite pattern). Every visual element inherits from `GuiComponent`. The root of the hierarchy is `rootPanel` (a `GuiPanel` covering the entire screen).

```
rootPanel (GuiPanel - Flex Vertical, Width 100%)
├── headerPanel (GuiPanel - Flex Horizontal)
│   ├── GuiLabel "Title"
│   └── GuiButton "[X]" (Close)
├── contentPanel (GuiScrollPanel - Responsive Viewport)
│   ├── card1 (GuiPanel)
│   └── card2 (GuiPanel)
└── footerPanel (GuiPanel - Bottom bar)
    └── GuiSliderInt
```

### Tree Operations

```java
GuiPanel panel = new GuiPanel(0, 0, 0, 300).withPercentWidth(1.0f);
GuiButton btn = (GuiButton) new GuiButton(0, 0, 0, 20, "Accept", () -> {}).withPercentWidth(0.5f);

panel.addChild(btn);       // Adds btn and triggers onAttached()
panel.removeChild(btn);    // Removes btn and triggers onDetached()
panel.clearChildren();     // Removes and destroys all children
```

---

## 3. Component Lifecycle (`GuiComponent`)

The component lifecycle in X4UI is standardized into deterministic stages:

1. **Construction:** Initial properties are set in the constructor or via the fluent API (`.withPercentWidth()`, `.withVariant()`).
2. **Attachment (`onAttached()`):** Invoked when added to a parent container.
3. **Frame Update (`update(float deltaTime)`):** Executed every frame before rendering. If dirty flags (`requestLayout()`) are present, the layout and parent-based percentage widths are resolved and propagated to all children.
4. **Rendering (`render(IGraphics gfx, int mouseX, int mouseY, float partialTicks)`):**
   - Calls `renderSelf(gfx, mouseX, mouseY, partialTicks)`.
   - Sorts children by `layer` and calls `renderChildren(gfx, ...)` applying clipping.
5. **Detachment (`onDetached()`):** Invoked when removed from the parent.
6. **Destruction (`onDestroy()` / `destroy()`):**
   - Automatically unbinds all reactive listeners attached via `bindState()` or `mapState()`.
   - Releases weak references in `GuiAnimator` and propagates to children.

---

## 4. Reactive State (`State<T>`)

`State<T>` (in `com.x4yi.x4ui.common`) is a thread-safe reactive container that notifies subscribers when its value mutates. It deduplicates updates via `Objects.equals()`.

```java
import com.x4yi.x4ui.common.State;

State<Integer> score = new State<>(0);

// Safe binding to component lifecycle
label.bindState(score, val -> label.setText("Score: " + val));

score.set(10); // Automatically updates the label
```

---

## 5. Global State Bus (`UIStateBus`)

To communicate between decoupled modules or interfaces, X4UI provides `UIStateBus`:

```java
// Publish an event or state
UIStateBus.publish(new UserProfileUpdatedEvent(userId, newName));

// Subscribe to an event
UIStateBus.subscribe(UserProfileUpdatedEvent.class, event -> {
    System.out.println("Profile updated: " + event.getName());
});
```

---

## 6. Theme System (`ITheme` & `ThemeRegistry`)

Visual styles are encapsulated in the `ITheme` interface. `DefaultTheme.INSTANCE` provides a standard dark palette. Components search for their theme by climbing up the ancestor chain until reaching the default theme.

```java
ThemeRegistry.register("my_dark_theme", customTheme);
rootPanel.setTheme(ThemeRegistry.get("my_dark_theme"));
```

---

## 7. Dirty Flags

- `requestLayout()`: Requests recalculation of dimensions and positions on the next frame.
- `requestRender()`: Requests resorting of visual `layer`s.
- Purely visual changes (color, alpha) DO NOT require calling `requestLayout()`.

---

## What To Avoid (Critical Anti-Patterns)

> [!CAUTION]
> **1. NEVER add children (`addChild()`) inside `renderSelf()` or `render()`.**
> Instantiate and add components only when initializing the view, never during a draw frame.

> [!WARNING]
> **2. NEVER call `requestLayout()` unconditionally on every tick.**
> Only do this when a structural property (width, height, visibility, gap) actually changes.

> [!WARNING]
> **3. NEVER manually subscribe via `State.addListener()` without unsubscribing.**
> Always use `component.bindState()`, which guarantees listener cleanup when the component dies.

> [!IMPORTANT]
> **4. NEVER block the main Minecraft thread.**
> Do not perform blocking network or disk requests in button callbacks. Use `MediaPipeline` (with `CompletableFuture`) or background threads.
