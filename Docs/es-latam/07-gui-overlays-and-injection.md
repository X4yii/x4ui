# 07 - Inyección de Overlays y GuiContainerAnchor

Este documento describe la arquitectura de los Overlays (HUDs, Toasts, notificaciones globales) en X4UI y cómo interactúan con el bucle de renderizado de Minecraft Forge.

---

## 1. El Problema del Posicionamiento Fijo

Si un gestor individual (como el `GuiToastManager`) quiere animar sus toasts desde el centro de la pantalla, o deslizarlos suavemente entre sí, un gestor estricto forzaría a los toasts a saltar repentinamente hacia una esquina fija cada vez que el usuario redimensiona la pantalla, causando un severo "flickering" (parpadeo visual).

---

## 2. La Solución: GuiContainerAnchor.FULLSCREEN

Para solucionar este conflicto de jurisdicciones, X4UI emplea el ancla estructural `GuiContainerAnchor.FULLSCREEN`.

Cuando un Overlay o un Gestor de Overlays devuelve `FULLSCREEN` en lugar de una esquina estática, el gestor maestro (`OverlayDockManager`) le **cede el 100% del control de sus coordenadas y dimensiones**.

Esto significa que el Overlay abarcará lógicamente toda la pantalla y podrá posicionar a sus hijos libremente, solucionando para siempre el glitch de recolocamiento.

### Ejemplo: Creando un Gestor Libre de Flickering

```java
import com.x4yi.x4ui.client.gui.overlay.GuiOverlay;
import com.x4yi.x4ui.client.gui.overlay.GuiContainerAnchor;

public class MyCustomToastManager extends GuiOverlay {

    public MyCustomToastManager() {
        super(GuiContainerAnchor.FULLSCREEN); // <-- ¡La clave para libertad total!
    }

    @Override
    public void renderOverlay(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        // Al ser FULLSCREEN, this.width y this.height igualan la pantalla completa.
        // Aquí puedes animar tus popups personalizados libremente.
    }
}
```

---

## 3. Inyección en el Forge Event Bus

Para que X4UI dibuje tu overlay sobre el HUD nativo de Minecraft, debes registrarlo usando `GuiOverlayRegistry` durante la inicialización del cliente:

```java
import com.x4yi.x4ui.client.gui.overlay.GuiOverlayRegistry;

// Registrar el gestor durante el evento FMLInitializationEvent (solo lado Cliente)
GuiOverlayRegistry.register(new MyCustomToastManager());
```

El registro maneja automáticamente la suscripción al evento `RenderGameOverlayEvent.Post` de Forge y asegura que los `update()` ticks se llamen adecuadamente en cada fotograma para que tus animaciones de `GuiAnimator` sigan corriendo fluidamente incluso fuera de un `GuiScreen`.
