# 00 - Getting Started

X4UI is a modern, retained-mode user interface framework for Minecraft Forge 1.12.2. It provides a complete component tree architecture, responsive flexbox layout managers, an immediate graphics abstraction layer (`IGraphics`), hardware-accelerated TrueType/OpenType vector typography (`IFont`), hierarchical event propagation, reactive state binding (`State<T>`), smooth animations, and decoupled theming.

## Requirements

- Minecraft Forge 1.12.2 (`1.12.2-14.23.5.2847` or later)
- Java 8 (OpenJDK or Oracle JDK)

## Installation

Add the X4UI dependency to your `build.gradle` using any of the following repositories:

### Modrinth (Recommended)
```gradle
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    deobfCompile "maven.modrinth:x4ui:1.0b5"
}
```

### CurseForge
```gradle
repositories {
    maven { url "https://cursemaven.com" }
}

dependencies {
    deobfCompile "curse.maven:x4ui-PROJECT_ID:FILE_ID"
}
```

### GitHub Packages / JitPack
```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    deobfCompile "com.github.x4yi:X4UI:1.0b5"
}
```

## Package Structure

All X4UI classes reside under `com.x4yi.x4ui`. The architecture cleanly isolates public APIs from internal implementations:

| Package | Purpose |
|---------|---------|
| `com.x4yi.x4ui.api.client.gui` | Immediate rendering abstraction (`IGraphics`) |
| `com.x4yi.x4ui.impl.client.gui` | Low-level OpenGL graphics implementation (`GLGraphics`) |
| `com.x4yi.x4ui.api.client.font` | Typography abstraction interface (`IFont`) |
| `com.x4yi.x4ui.impl.client.font` | Vector rasterizer (`TrueTypeFont`), registry (`FontRegistry`) |
| `com.x4yi.x4ui.client.gui.base` | Abstract screen bases (`GuiBaseScreen`, `GuiBaseContainer`) |
| `com.x4yi.x4ui.client.gui.component` | UI widgets (`GuiPanel`, `GuiButton`, `GuiLabel`, `GuiImage`, etc.) |
| `com.x4yi.x4ui.client.gui.component.layout` | Layout managers (`FlexLayout`, `FlexDirection`) |
| `com.x4yi.x4ui.client.gui.component.slider` | Modular typed sliders (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`) |
| `com.x4yi.x4ui.client.gui.component.sprite` | 2D animation engine (`ISpriteSource`, `SpriteSheetSource`, `SequenceSource`) |
| `com.x4yi.x4ui.client.gui.component.image` | 9-slice border configurations (`NineSliceConfig`) |
| `com.x4yi.x4ui.client.gui.overlay` | Non-intrusive screen overlays (`GuiOverlayManager`, `OverlayDockManager`) |
| `com.x4yi.x4ui.client.gui.compat.jei` | Native JEI integration (`JeiPlugin`) |
| `com.x4yi.x4ui.client.gui.bus` | Reactive inter-mod state bus (`UIStateBus`) |
| `com.x4yi.x4ui.client.gui.utils` | Utilities (`GuiBuilder`, `ITheme`, `DefaultTheme`, `ThemeRegistry`, `Insets`) |
| `com.x4yi.x4ui.common` | Server-safe reactive state primitives (`State<T>`) |
| `com.x4yi.x4ui.common.sync` | Container window property synchronization (`NetworkSyncHelper`) |
| `com.x4yi.x4ui.proxy` | Strict sided proxy separation (`ClientProxy`, `CommonProxy`) |

---

## Creating a Standalone Screen (`GuiBaseScreen`)

Extend `GuiBaseScreen` for screens that do not interact with inventory containers (e.g., configuration menus, dialogs, media players):

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.utils.Insets;
import net.minecraft.client.gui.GuiScreen;

public class MySettingsScreen extends GuiBaseScreen {

    public MySettingsScreen(GuiScreen parent) {
        super(parent, "Settings");
    }

    @Override
    protected void initComponents() {
        // rootPanel is automatically created spanning the full display window
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(8);
        rootPanel.setPadding(new Insets(16));

        rootPanel.addChild(new GuiLabel(0, 0, "Main Settings", 0xFFFFFFFF)
            .setCentered(true));

        rootPanel.addChild(new GuiButton(0, 0, 160, 24, "Save Changes", () -> {
            System.out.println("Settings saved");
        }));

        rootPanel.addChild(new GuiButton(0, 0, 160, 24, "Close", this::closeScreen));
    }
}
```

### Opening the Screen

```java
Minecraft.getMinecraft().displayGuiScreen(
    new MySettingsScreen(Minecraft.getMinecraft().currentScreen)
);
```

---

## Creating an Inventory Container Screen (`GuiBaseContainer`)

Extend `GuiBaseContainer` for screens bound to a Minecraft `Container` (chest GUIs, machine inventories, crafting tables):

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class MyContainerScreen extends GuiBaseContainer {

    public MyContainerScreen(Container container) {
        super(container);
    }

    @Override
    protected void initComponents() {
        rootPanel.addChild(new GuiLabel(8, 6, "Machine Inventory", 0xFF404040));

        // GuiSlot automatically mirrors screen coordinates with the vanilla Slot
        for (Slot slot : inventorySlots.inventorySlots) {
            rootPanel.addChild(new GuiSlot(slot, this));
        }
    }
}
```

---

## Fluent Methods on `GuiComponent`

In X4UI r1.0b5, components expose chainable builder methods directly on `GuiComponent`:

```java
GuiButton btn = new GuiButton(0, 0, 120, 20, "Confirm", this::onConfirm)
    .withPosition(10, 20)
    .withSize(140, 24)
    .withMargin(new Insets(4))
    .withLayer(5)
    .withPercentWidth(0.8f); // 80% of parent width
```

Static declarative creation is also available via `GuiBuilder`:

```java
GuiPanel panel = GuiBuilder.createPanel()
    .position(10, 10)
    .size(200, 300)
    .flexDirection(FlexDirection.VERTICAL)
    .gap(5)
    .padding(new Insets(8))
    .build();
```

---

## What to Avoid (Pitfalls & Anti-Patterns)

> [!CAUTION]
> **1. NEVER import `client.gui` or `impl.client` classes in common or server code.**
> All X4UI graphical components are `@SideOnly(Side.CLIENT)`. Using them in common packages will crash dedicated servers with `ClassNotFoundException`. In common code, only use `com.x4yi.x4ui.common.State<T>`.

> [!WARNING]
> **2. NEVER execute raw OpenGL calls (`GL11`, `GlStateManager`, `Tessellator`).**
> Always use `IGraphics` drawing methods. Emitting raw OpenGL vertices disrupts X4UI's internal rendering state (matrices, scissor bounds, etc).

> [!WARNING]
> **3. NEVER hardcode absolute positions exceeding 320 px without testing in GUI Scale 4.**
> Always encapsulate tall layouts in a `GuiScrollPanel` and use relative sizing (`withPercentWidth`) to support smaller resolutions.

> [!IMPORTANT]
> **4. NEVER create new `TrueTypeFont` instances repeatedly or per tick.**
> Register fonts once in `FontRegistry` at load time. Doing this during rendering will severely impact performance.

> [!IMPORTANT]
> **5. NEVER leave manual `State<T>` listeners uncleaned.**
> Prefer `component.bindState(state, callback)` over `state.addListener()`. This ensures the listener is automatically unbound upon component destruction.
