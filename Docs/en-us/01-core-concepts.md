# 01 - Core Concepts & Architecture

This document defines X4UI's retained-mode paradigm, the design decisions behind its component tree, deterministic lifecycle, reactive state management, and dirty-flag optimization model.

---

## 1. The X4UI Paradigm & What It Solves

Vanilla Minecraft's `GuiScreen` relies on immediate-mode imperative code: every frame recalculates positions, emits raw OpenGL geometry, and mixes business logic with rendering. This causes tight coupling, lost UI state on window resize, and high maintenance overhead.

X4UI adopts a **DOM-like retained-mode paradigm**:
1. **Structure decoupled from rendering:** Components are instantiated once in `initComponents()` and arranged in a persistent tree (`rootPanel`).
2. **Lazy layout resolution:** Coordinates and sizes are recalculated only when structure or viewport dimensions change, never unconditionally per frame.
3. **Passive, reactive views (*Dumb Views*):** Data lives in observable containers (`State<T>`) or `UIStateBus`; views subscribe to state and reflect updates automatically.
4. **Lifecycle-managed cleanup:** Destroying a component or closing a screen automatically unbinds reactive listeners and clears associated animations.

---

## 2. Hierarchical Component Tree

Every visual element extends `GuiComponent` (Composite pattern). The root of every screen is `rootPanel`, a `GuiPanel` spanning the logical viewport:

```
rootPanel (GuiPanel)
├── headerPanel (GuiPanel.row)
│   ├── GuiLabel
│   ├── spacer (GuiPanel.spacer)
│   └── GuiButton ("X")
├── contentPanel (GuiScrollPanel)
│   ├── card1 (GuiPanel.card)
│   └── card2 (GuiPanel.card)
└── footerPanel (GuiPanel.row)
    └── GuiSliderInt
```

### Tree Operations

```java
GuiPanel panel = new GuiPanel(0, 0, 0, 300).withPercentWidth(1.0f);
GuiButton btn = (GuiButton) new GuiButton(0, 0, 0, 20, "Accept", () -> {}).withPercentWidth(0.5f);

panel.addChild(btn);       // Attaches btn and invokes onAttached()
panel.removeChild(btn);    // Detaches btn and invokes onDetached()
panel.clearChildren();     // Detaches and destroys all children
```

---

## 3. Component Lifecycle (`GuiComponent`)

The lifecycle follows six deterministic stages:

1. **Construction:** Initial properties are set via constructor or fluent API (`withPercentWidth`, `withMargin`, `withLayer`).
2. **Attachment (`onAttached()`):** Invoked when added to a parent container.
3. **Update (`update(float deltaTime)`):** Executed before rendering. If layout dirty flags (`requestLayout()`) are active, resolves percentage dimensions and delegates positioning to `ILayoutManager`, propagating down to children.
4. **Rendering (`render(IGraphics gfx, int mouseX, int mouseY, float partialTicks)`):**
   - Calls `renderSelf(gfx, mouseX, mouseY, partialTicks)`.
   - If render dirty flag (`requestRender()`) is set, sorts children by `layer` (Z-index).
   - Calls `renderChildren(gfx, ...)` with scissor clipping when enabled.
5. **Detachment (`onDetached()`):** Invoked when removed from the parent.
6. **Destruction (`destroy()` / `onDestroy()`):**
   - Automatically unbinds all listeners registered via `bindState()` or `mapState()`.
   - Releases weak references in `GuiAnimator` and propagates recursively to children.

---

## 4. Event Propagation

Mouse events (`mouseClicked`, `mouseReleased`, `mouseDragged`, `mouseScrolled`) and keyboard events (`keyTyped`) traverse the tree respecting visibility and `layer` ordering:
- Higher-layer components receive capture priority.
- Returning `true` consumes the event and halts propagation to lower layers.

---

## 5. Reactive State (`State<T>`) & State Bus (`UIStateBus`)

### `State<T>`
`com.x4yi.x4ui.common.State<T>` is a thread-safe, dedicated-server-compatible observable container. It deduplicates updates using `Objects.equals()` to prevent redundant UI work:

```java
State<Integer> score = new State<>(0);

// Lifecycle-safe binding tied to the component
label.bindState(score, val -> label.setText("Score: " + val));

score.set(10); // Updates the label automatically
```

### `UIStateBus`
Enables decoupled cross-module state sharing and server-to-client data sync without direct object references. Supports three scopes:
- `Scope.SCREEN`: automatically cleared on screen close (`GuiBaseScreen.onGuiClosed()`). Used by `syncedState(key, initialValue)`.
- `Scope.SESSION`: survives screen closes and is cleared on server disconnect.
- `Scope.GLOBAL`: persists for the entire client session.

```java
String key = UIStateBus.scopedKey("machine:10,64,10", "energy");
State<Integer> energy = UIStateBus.getOrCreate(key, 0, UIStateBus.Scope.SCREEN);
```

---

## 6. Theme System (`ITheme` & `ThemeRegistry`)

Visual styling is decoupled from component logic through `ITheme`. `DefaultTheme.INSTANCE` provides a solid, minimalist dark palette. Components inherit the theme from their ancestor chain or fall back to `ThemeRegistry.getCurrentTheme()`:

```java
ThemeRegistry.registerTheme("custom_dark", customTheme);
rootPanel.setTheme(ThemeRegistry.getTheme("custom_dark"));
```

---

## 7. Dirty-Flag Optimization

- `requestLayout()`: marks that dimensions or structural hierarchy changed; layout runs once on the next `update()`.
- `requestRender()`: requests resorting of visual `layer`s.
- Purely visual mutations (color, alpha, or text changes that do not alter structural bounds) do not require `requestLayout()`.

---

## 8. What to Avoid When Building a GUI

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Calling `addChild()` inside `renderSelf()` or `render()` | Allocates unbounded components every frame and exhausts memory/GC | Instantiate hierarchy in `initComponents()` |
| Calling `requestLayout()` unconditionally every tick | Defeats lazy layout and forces full tree recalculation every frame | Call only when dimensions, visibility, or children actually change |
| Subscribing via `state.addListener(...)` without unsubscribing | Retains strong references to closed `GuiComponent` instances (memory leak) | Use `component.bindState(state, callback)` |
| Running blocking I/O or network calls in button callbacks | Freezes the Minecraft client main thread | Delegate to `MediaPipeline`, `CompletableFuture`, or `UINetwork` |
