# 07 - Inyección de Overlays y Anclaje (`IGuiOverlay`)

Este documento describe el sistema de inyección de overlays de X4UI (`IGuiOverlay`, `GuiOverlayManager`, `OverlayDockManager` y `ContainerAnchor`), diseñado para agregar paneles o controles sobre pantallas vanilla o de otros mods sin modificar sus clases.

---

## 1. Problema y Decisiones de Diseño

Modificar pantallas vanilla o de terceros mediante mixins/ASM para añadir paneles laterales genera conflictos entre mods, coordenadas desfasadas respecto a `guiLeft`/`guiTop` y colisiones con los paneles de JEI.

X4UI resuelve esto mediante:
1. **Inyección declarativa por eventos (`GuiOverlayManager`):** Escucha eventos de pantalla de Forge (`GuiOpenEvent`, `InitGuiEvent`, `DrawScreenEvent`, `MouseInputEvent`, `KeyboardInputEvent`) y activa únicamente los overlays cuyo `matches(screen)` retorne `true`.
2. **Posicionamiento relativo por anclas (`ContainerAnchor`):** `OverlayDockManager` calcula coordenadas respecto al `GuiContainer` (`CONTAINER_LEFT`, `CONTAINER_RIGHT`, `CONTAINER_TOP`, `CONTAINER_BOTTOM`) o a la ventana (`WINDOW_TOP_LEFT`, `SCREEN_CENTER`, `FULLSCREEN`), apilando múltiples overlays sin solapamiento.
3. **Áreas de exclusión automáticas:** `getExclusionAreas()` expone los rectángulos ocupados para que `JeiPlugin` desplace recetas e ítems automáticamente.

---

## 2. Contrato `IGuiOverlay` y `ContainerAnchor`

| Ancla (`ContainerAnchor`) | Comportamiento |
| :--- | :--- |
| `CONTAINER_LEFT` / `CONTAINER_RIGHT` | Se alinea a los costados del `GuiContainer` usando `guiLeft` y `xSize`. |
| `CONTAINER_TOP` / `CONTAINER_BOTTOM` | Se alinea encima o debajo del contenedor. |
| `WINDOW_TOP_LEFT` / `TOP_RIGHT` / `BOTTOM_LEFT` / `BOTTOM_RIGHT` | Se fija respecto a los bordes de la ventana lógica. |
| `SCREEN_CENTER` | Centra el componente raíz en pantalla. |
| `FULLSCREEN` | Cede control total de coordenadas y dimensiones al overlay (ideal para gestores con animaciones libres o toasts). |

### Ejemplo de Implementación

```java
package com.example.mymod.client.overlay;

import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.overlay.ContainerAnchor;
import com.x4yi.x4ui.client.gui.overlay.IGuiOverlay;
import com.x4yi.x4ui.client.gui.overlay.OverlayContext;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class ContainerSideOverlay implements IGuiOverlay {

    private GuiPanel root;

    @Override
    public String getId() {
        return "mymod:side_tools";
    }

    @Override
    public boolean matches(GuiScreen screen) {
        return screen instanceof GuiContainer;
    }

    @Override
    public void init(GuiScreen screen, OverlayContext context) {
        root = GuiPanel.card(6, 4);
        root.setSize(80, 32);
        root.addChild(new GuiButton(0, 0, 0, 20, "Ordenar", () -> {}).withPercentWidth(1.0f));
    }

    @Override
    public GuiComponent getRootComponent() {
        return root;
    }

    @Override
    public ContainerAnchor getAnchor() {
        return ContainerAnchor.CONTAINER_RIGHT;
    }
}
```

---

## 3. Registro en Cliente

Registrar el overlay durante la inicialización del cliente (`ClientProxy` / `FMLInitializationEvent`):

```java
import com.x4yi.x4ui.client.gui.overlay.GuiOverlayManager;

GuiOverlayManager.INSTANCE.registerOverlay(new ContainerSideOverlay());
```

`GuiOverlayManager` ya está suscrito al event bus en `ClientProxy`, por lo que ejecuta automáticamente `init`, `update`, `renderBackground`, `renderForeground`, entrada de mouse/teclado y `onClosed` (que destruye el `root` y libera listeners reactivos).

---

## 4. Qué Evitar en Overlays

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Calcular `guiLeft` o posiciones manuales fijas en `renderForeground()` | Se desalinea con distintos tamaños de contenedor o GUI Scale | Seleccionar un `ContainerAnchor` apropiado y delegar en `OverlayDockManager` |
| Omitir `getExclusionAreas()` en overlays junto a inventarios | JEI superpone la lista de ítems sobre el overlay | Retornar el rectángulo del componente raíz (comportamiento por defecto de `IGuiOverlay`) |
| Retener referencias a `GuiScreen` tras `onClosed()` | Impide la recolección de basura de pantallas cerradas | Limpiar referencias en `onClosed(GuiScreen)` y depender de `root.destroy()` |
