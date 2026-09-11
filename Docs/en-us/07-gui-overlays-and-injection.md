# 07 - Overlay Injection & GuiContainerAnchor

This document describes the architecture of Overlays (HUDs, Toasts, global notifications) in X4UI and how they hook into the Minecraft Forge rendering loop.

---

## 1. The Fixed Positioning Problem

If an individual manager (like the `GuiToastManager`) wants to animate its toasts from the center of the screen or slide them smoothly between each other, a strict manager would force the toasts to suddenly snap back to a fixed corner every time the user resizes the screen, causing severe visual flickering.

---

## 2. The Solution: GuiContainerAnchor.FULLSCREEN

To solve this jurisdictional clash, X4UI employs the structural anchor `GuiContainerAnchor.FULLSCREEN`.

When an Overlay or an Overlay Manager returns `FULLSCREEN` instead of a static corner, the master manager (`OverlayDockManager`) **yields 100% control of its coordinates and dimensions** to it.

This means the Overlay will logically span the entire screen and can position its children freely, solving the repositioning glitch forever.

### Example: Creating a Flicker-Free Manager

```java
import com.x4yi.x4ui.client.gui.overlay.GuiOverlay;
import com.x4yi.x4ui.client.gui.overlay.GuiContainerAnchor;

public class MyCustomToastManager extends GuiOverlay {

    public MyCustomToastManager() {
        super(GuiContainerAnchor.FULLSCREEN); // <-- The key to total freedom!
    }

    @Override
    public void renderOverlay(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        // Since it's FULLSCREEN, this.width and this.height equal the full screen.
        // Here you can animate your custom popups freely.
    }
}
```

---

## 3. Injection into the Forge Event Bus

For X4UI to draw your overlay on top of the native Minecraft HUD, you must register it using `GuiOverlayRegistry` during the client initialization:

```java
import com.x4yi.x4ui.client.gui.overlay.GuiOverlayRegistry;

// Register the manager during FMLInitializationEvent (Client Side only)
GuiOverlayRegistry.register(new MyCustomToastManager());
```

The registry automatically handles subscribing to Forge's `RenderGameOverlayEvent.Post` event and ensures that `update()` ticks are called properly every frame so your `GuiAnimator` animations keep running smoothly even outside of a `GuiScreen`.
