# 00 - Primeros Pasos

X4UI (`r1.0b7`) es un framework de interfaz gráfica en modo retenido, estado reactivo y red servidor–cliente para **Minecraft Forge 1.12.2 (Java 8 + LWJGL 2)** y **CleanroomMC (Java 25 + LWJGL 3)**.

> *Nota: Únicamente la documentación de X4UI fue redactada con IA (Gemini), siendo revisada y aprobada mediante pruebas humanas (X4yi).*

---

## 1. Requisitos y Compatibilidad

- **Entorno de ejecución:** Minecraft Forge `1.12.2` (`14.23.5.2847` o superior, Java 8) o CleanroomMC (Java 25).
- **Compilación de mods consumidores:** `sourceCompatibility = 1.8` y `targetCompatibility = 1.8` para garantizar portabilidad de bytecode en ambos runtimes.
- **Artefacto de distribución:** utilizar siempre el JAR reobfuscado generado por `reobfJar` (`x4ui-r1.0b7.jar`) en entornos de producción o servidores; `*-dev.jar` es exclusivo para desarrollo desofuscado.

### Dependencia en `build.gradle`

```gradle
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    // RetroFuturaGradle (recomendado)
    implementation rfg.deobf("maven.modrinth:x4ui:r1.0b7")
}
```

---

## 2. Arquitectura de Paquetes

X4UI separa estrictamente contratos públicos (`api`), código seguro para servidor dedicado (`common`), capa de cliente (`client`) e implementaciones internas (`impl`):

| Paquete | Responsabilidad |
| :--- | :--- |
| `com.x4yi.x4ui.api.client.gui` | Contrato de renderizado inmediato (`IGraphics`). |
| `com.x4yi.x4ui.api.client.font` | Contrato de tipografía (`IFont`). |
| `com.x4yi.x4ui.api.client.media` | Contratos de decodificadores multimedia (`IVideoDecoder`, `IAudioDecoder`, `MediaRegistry`). |
| `com.x4yi.x4ui.client.gui` | Registro remoto de pantallas en cliente (`ScreenRegistry`). |
| `com.x4yi.x4ui.client.gui.base` | Pantallas base y host compartido (`GuiBaseScreen`, `GuiBaseContainer`, `ScreenHost`). |
| `com.x4yi.x4ui.client.gui.component` | Widgets (`GuiPanel`, `GuiButton`, `GuiLabel`, `GuiImage`, `GuiVideo`, `GuiVirtualList`, etc.). |
| `com.x4yi.x4ui.client.gui.component.layout` | Motor flexbox (`FlexLayout`, `FlexDirection`, `FlexAlign`, `ILayoutManager`). |
| `com.x4yi.x4ui.client.gui.component.slider` | Sliders tipados (`GuiSlider`, `GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`). |
| `com.x4yi.x4ui.client.gui.component.sprite` | Animación 2D (`ISpriteSource`, `SpriteSheetSource`, `SequenceSource`, `LoopMode`). |
| `com.x4yi.x4ui.client.gui.component.image` | Configuración 9-slice (`NineSliceConfig`). |
| `com.x4yi.x4ui.client.gui.component.toast` | Notificaciones toast (`GuiToast`, `GuiToastManager`, `ToastAnchor`). |
| `com.x4yi.x4ui.client.gui.animation` | Interpolación temporal (`GuiAnimator`). |
| `com.x4yi.x4ui.client.gui.input` | Abstracción de entrada LWJGL 2/3 (`KeyCodes`, `UIInput`). |
| `com.x4yi.x4ui.client.gui.overlay` | Inyección en GUIs (`IGuiOverlay`, `GuiOverlayManager`, `OverlayDockManager`, `ContainerAnchor`). |
| `com.x4yi.x4ui.client.gui.bus` | Bus reactivo de estados (`UIStateBus`). |
| `com.x4yi.x4ui.client.gui.utils` | Temas y constructores (`ITheme`, `DefaultTheme`, `ThemeRegistry`, `GuiBuilder`, `Insets`). |
| `com.x4yi.x4ui.client.gui.compat.jei` | Integración con Just Enough Items (`JeiPlugin`, `JeiAdvancedGuiHandler`). |
| `com.x4yi.x4ui.common` | Estado reactivo portable (`State<T>`). |
| `com.x4yi.x4ui.common.network` | Red server-optional (`UINetwork`, `UIChannels`, `ActionGuard`, `PacketGuiRpc`, `PacketStateBatch`). |
| `com.x4yi.x4ui.common.platform` | Puente Multi-Release Java 8 ↔ Java 25 (`PlatformBridge`). |
| `com.x4yi.x4ui.common.sync` | Sincronización de `Container` (`NetworkSyncHelper`, `StateContainerListener`). |
| `com.x4yi.x4ui.impl.client.*` | Implementaciones (`GLGraphics`, `TrueTypeFont`, `FontRegistry`, `MediaPipeline`, `SecurityUtils`). |
| `com.x4yi.x4ui.proxy` | Aislamiento estricto de sidedness (`ClientProxy`, `CommonProxy`). |

---

## 3. Pantallas Base (`GuiBaseScreen` y `GuiBaseContainer`)

### Pantalla Standalone (`GuiBaseScreen`)

Se utiliza para vistas sin inventario (menús, paneles administrativos, tiendas remotas, visores):

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.utils.GuiBuilder;
import net.minecraft.client.gui.GuiScreen;

public class SettingsScreen extends GuiBaseScreen {

    public SettingsScreen(GuiScreen parent) {
        super(parent, "Settings");
    }

    @Override
    protected void initComponents() {
        GuiPanel window = createCenteredWindow(360, 220);

        GuiPanel header = GuiPanel.row(8);
        header.add(GuiLabel.of("Configuración", 0xFFFFFFFF));
        header.add(GuiPanel.spacer());
        header.add(new GuiButton(0, 0, 20, 20, "X", this::closeScreen));

        GuiPanel card = GuiPanel.card(10, 6);
        card.add(GuiButton.of("Aplicar", this::onApply)
            .withPercentWidth(1.0f)
            .withVariant(GuiButton.Variant.PRIMARY));

        window.add(header);
        window.add(card);
    }

    private void onApply() {
        sendActionOrLocal("mymod:save_settings", null, () -> {});
    }

    public static void open(GuiScreen parent) {
        GuiBuilder.open(new SettingsScreen(parent));
    }
}
```

### Pantalla con Inventario (`GuiBaseContainer`)

Se utiliza cuando la interfaz está respaldada por un `Container` vanilla de inventario:

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class MachineScreen extends GuiBaseContainer {

    public MachineScreen(Container container) {
        super(container);
    }

    @Override
    protected void initComponents() {
        rootPanel.addChild(new GuiLabel(8, 6, "Inventario", 0xFFE6E8EC));

        for (Slot slot : inventorySlots.inventorySlots) {
            rootPanel.addChild(new GuiSlot(slot, this));
        }
    }
}
```

---

## 4. Composición sin Casts y `GuiBuilder`

En `r1.0b7`, los widgets principales ofrecen factories flex-first `.of(...)` y setters fluidos covariantes, y `GuiComponent` expone `add(T child)` / `add(T child, Consumer<T> config)` para componer interfaces sin casts manuales:

```java
GuiButton actionBtn = card.add(
    GuiButton.of("Confirmar", this::onConfirm)
        .withPercentWidth(1.0f)
        .withMargin(new Insets(4))
        .withTooltip("Guarda la configuración actual"),
    btn -> btn.withVariant(GuiButton.Variant.PRIMARY)
);
```

Para crear y abrir pantallas o modales sin importar `GuiScreen` ni `Minecraft.getMinecraft().displayGuiScreen(...)`, usa **`GuiBuilder`** como punto de entrada oficial:

```java
// Constructor fluido de pantallas (usa .build() para ScreenRegistry o .open() directamente)
GuiBuilder.screen("Ajustes Rápidos")
    .window(360, 220)
    .animateOpen(true)
    .onInit((screen, window) -> {
        window.add(GuiLabel.of("Ajustes Rápidos", 0xFFFFFFFF));
        window.add(GuiButton.of("Cerrar", screen::closeScreen).withPercentWidth(1.0f));
    })
    .open();

// Modal estándar en una sola llamada (incluye cabecera y botón cerrar)
GuiBuilder.modal("Confirmar Acción", 300, 140, body -> {
    body.add(GuiLabel.of("¿Aplicar los cambios ahora?"));
    body.add(GuiButton.of("Aceptar", GuiBuilder::close).withPercentWidth(1.0f));
});
```

---

## 5. Restricciones Iniciales

- No importar paquetes `com.x4yi.x4ui.client.*` o `com.x4yi.x4ui.impl.*` desde código común o de servidor dedicado.
- No instanciar componentes visuales dentro de `renderSelf()` ni recrear el árbol completo en `onScreenResize()`.
- Consultar la guía correspondiente según el subsistema:
  - Arquitectura y estado: `01-core-concepts.md`
  - Red y RPC: `03-networking-and-sync.md`
  - Diseño responsivo: `04-responsive-layout-and-scaling.md`
  - Compatibilidad Forge ↔ CleanroomMC: `09-forge-cleanroom-compatibility.md`
