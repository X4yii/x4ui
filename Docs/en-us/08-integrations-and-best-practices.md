# 08 - Integrations and Best Practices

This document details standardized guidelines for working with X4UI in large-scale projects, avoiding architectural debt, and ensuring consistency.

---

## 1. Naming Conventions

To maintain architectural clarity and avoid the "God Object" syndrome, the `Gui` prefix in X4UI is strictly reserved for **evident visual components** that inherit from `GuiComponent`.

### What SHOULD be named `Gui`
- Interactive components: `GuiButton`, `GuiTextField`, `GuiCheckbox`.
- Layout containers: `GuiPanel`, `GuiScrollPanel`, `GuiVirtualList`.
- Graphics and multimedia: `GuiImage`, `GuiVideo`, `GuiSprite`.

### What SHOULD NOT be named `Gui` (Anti-pattern)
- Managers and Controllers: Do not name a queue manager `GuiToastManager` if it doesn't draw itself. Name it `ToastManager`. *(Note: if the manager inherits from `GuiOverlay` and renders content autonomously, the `Gui` prefix is justifiable).*
- Data Structures and States: `UIStateBus` is not called `GuiStateBus`. `State<T>` is not called `GuiState`.
- Network Models: Do not name a synchronization packet `GuiPacket`.

---

## 2. Dependency Injection and Models

Never pass massive game objects (like the entire player, worlds, or databases) directly into the constructor of visual components.
Views should be *Dumb Views* and reactive.

**Anti-pattern (Tight Coupling):**
```java
GuiPanel playerStats = new GuiPlayerStatsPanel(Minecraft.getMinecraft().player);
```

**Best Practice (Reactive, Decoupled):**
Pass reactive state containers or use the `UIStateBus`.

```java
State<PlayerStatsData> stats = new State<>(new PlayerStatsData());
GuiPanel playerStats = new PlayerStatsPanel(stats); // The panel subscribes internally
```

---

## 3. Initialization Practices

When creating an entire `Screen` in Minecraft that uses X4UI, instantiate your components exactly once in the constructor or in an initialization method called *before* attaching it to the screen.

Never start heavy downloads from the `MediaPipeline` inside `update()` or `render()`. Do it during the initialization phase.

---

## 4. Memory Auditing

Remember that in Java 8, carelessly abusing lambdas (capturing references of the entire `Screen`) can prevent the Garbage Collector from freeing closed screens.
- Always use `bindState` on components, as X4UI will automatically unbind them when the component is destroyed upon closing the `Screen`.
- If you use manual `State.addListener()`, make sure to unsubscribe it in the `onDetached()` of your root component.
