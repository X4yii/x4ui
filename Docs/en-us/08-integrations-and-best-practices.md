# 08 - Integrations & Best Practices

This document covers X4UI's architectural conventions (`r1.0b6`), native Just Enough Items (JEI) integration, and recommended practices for consumer mods.

---

## 1. Native JEI Integration (`JeiPlugin`)

X4UI automatically activates `JeiPlugin` and `JeiAdvancedGuiHandler` when JEI is loaded:
- Reports exclusion rectangles from `GuiBaseContainer` and all active `IGuiOverlay` instances in `GuiOverlayManager`.
- Prevents JEI's ingredient list from overlapping injected overlays or extended modal containers.
- Requires no extra setup beyond keeping accurate `getExclusionAreas()` bounds on custom overlays.

---

## 2. Naming & Architectural Conventions

The `Gui` prefix is reserved for **visual components** extending `GuiComponent` or screen contracts (`GuiBaseScreen`, `GuiBaseContainer`, `IGuiOverlay`):

| Category | Standard Naming | Avoid |
| :--- | :--- | :--- |
| Widgets & Containers | `GuiButton`, `GuiTextInput`, `GuiToggle`, `GuiPanel`, `GuiScrollPanel` | Ambiguous names without visual role |
| Non-visual Managers | `OverlayDockManager`, `MediaManager`, `ScreenRegistry` | Prefixing pure logic/registry classes with `Gui` |
| State & Networking | `State<T>`, `UIStateBus`, `UINetwork`, `PacketGuiRpc`, `ActionGuard` | Coupling packets or shared state with `client.gui.*` |

---

## 3. Decoupled Views (*Dumb Views*)

Visual components should not accept heavy game objects (`World`, `EntityPlayer`, `TileEntity`) in their constructors when they only display or mutate specific fields. Passing `State<T>` or reading from `UIStateBus` keeps views decoupled and server-optional friendly:

```java
// Recommended: view decoupled via State<T>
public class EnergyCard extends GuiPanel {
    public EnergyCard(State<Integer> energyState) {
        super(0, 0, 0, 40);
        GuiLabel label = new GuiLabel(0, 0, "0 RF", 0xFFFFFFFF);
        label.bindState(energyState, rf -> label.setText(rf + " RF"));
        addChild(label);
    }
}
```

---

## 4. Memory & Lifecycle Discipline

1. **Reactive bindings:** always use `component.bindState(state, callback)` so `GuiComponent.destroy()` unbinds listeners automatically on screen close.
2. **Lambda captures:** avoid capturing `GuiScreen` instances inside `Scope.GLOBAL` or `Scope.SESSION` listeners; prefer `Scope.SCREEN` or `syncedState(...)` in `GuiBaseScreen`.
3. **Single-pass initialization:** instantiate components, register fonts (`FontRegistry`), and trigger media fetches (`MediaPipeline`) during initialization, never inside `update()` or `renderSelf()`.
4. **Structured logging:** use `org.apache.logging.log4j.LogManager` instead of `System.out.println` or `printStackTrace()`.
