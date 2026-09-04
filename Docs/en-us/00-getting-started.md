# 00 - Getting Started

X4UI is a retained-mode GUI framework for Minecraft Forge 1.12.2. It replaces vanilla's immediate-mode `GuiScreen` rendering with a component tree architecture, flexbox layouts, event bubbling, reactive state binding, and theme support.

## Requirements

- Minecraft Forge 1.12.2 (`1.12.2-14.23.5.2847`)
- Java 8

## Installation

Add the X4UI dependency to `build.gradle` using one of the following repositories:

### Modrinth (Recommended)
```gradle
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    deobfCompile "maven.modrinth:x4ui:1.0b4"
}
```

### CurseForge
```gradle
repositories {
    maven { url "https://cursemaven.com" }
}

dependencies {
    // Replace FILE_ID with the actual file ID from the CurseForge project page for latest release.
    deobfCompile "curse.maven:x4ui-PROJECT_ID:FILE_ID"
}
```

### GitHub Packages / JitPack
```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    deobfCompile "com.github.x4yi:X4UI:1.0b4"
}
```

## Package Structure

All X4UI classes reside under `com.x4yi.x4ui`. The relevant packages for client-side GUI development are:

| Package | Purpose |
|---------|---------|
| `com.x4yi.x4ui.client.gui.base` | Abstract screen base classes (`GuiBaseScreen`, `GuiBaseContainer`) |
| `com.x4yi.x4ui.client.gui.component` | All UI components (`GuiPanel`, `GuiButton`, `GuiLabel`, etc.) |
| `com.x4yi.x4ui.client.gui.component.layout` | Layout engines (`FlexLayout`, `FlexDirection`) |
| `com.x4yi.x4ui.client.gui.component.slider` | Typed sliders (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`) |
| `com.x4yi.x4ui.client.gui.utils` | Utilities (`GuiBuilder`, `ITheme`, `DefaultTheme`, `Insets`) |
| `com.x4yi.x4ui.common` | Shared state primitives (`State<T>`) |
| `com.x4yi.x4ui.api.client.resource` | Remote asset management (`RemoteResourceManager`) |

## Creating a Standalone Screen

Extend `GuiBaseScreen` for screens that do not interact with inventory containers (e.g., settings menus, info panels, video players).

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import net.minecraft.client.gui.GuiScreen;

public class MySettingsScreen extends GuiBaseScreen {

    public MySettingsScreen(GuiScreen parent) {
        super(parent, "Settings");
    }

    @Override
    protected void initComponents() {
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(5);

        rootPanel.addChild(new GuiLabel(0, 0, "Settings", 0xFFFFFFFF).setCentered(true));

        rootPanel.addChild(new GuiButton(0, 0, 150, 20, "Option A", () -> {
            System.out.println("Option A clicked");
        }));

        rootPanel.addChild(new GuiButton(0, 0, 150, 20, "Option B", () -> {
            System.out.println("Option B clicked");
        }));

        rootPanel.addChild(new GuiButton(0, 0, 150, 20, "Close", () -> this.closeScreen()));
    }
}
```

Key points:
- `rootPanel` is created automatically in `initGui()` and fills the entire screen (`width` x `height`).
- `initComponents()` is called after `rootPanel` is created. All component setup happens here.
- `closeScreen()` returns to the parent screen passed in the constructor.

### Opening the Screen

```java
Minecraft.getMinecraft().displayGuiScreen(new MySettingsScreen(Minecraft.getMinecraft().currentScreen));
```

### Overriding Background Rendering

Override `drawBackground` to render custom backgrounds before the component tree:

```java
@Override
protected void drawBackground(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
}
```

## Creating a Container Screen

Extend `GuiBaseContainer` for screens that wrap a Minecraft `Container` (e.g., chest GUIs, crafting tables, custom inventories).

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class MyContainerScreen extends GuiBaseContainer {

    public MyContainerScreen(Container container) {
        super(container);
    }

    @Override
    protected void initComponents() {
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(2);

        rootPanel.addChild(new GuiLabel(0, 0, "My Inventory", 0xFFFFFFFF).setCentered(true));

        for (Slot slot : inventorySlots.inventorySlots) {
            rootPanel.addChild(new GuiSlot(slot, this));
        }
    }
}
```

Key points:
- The constructor receives a `Container` instance, passed to `GuiContainer`'s super constructor.
- `getGuiLeft()` and `getGuiTop()` expose the vanilla container offsets for slot positioning.
- `GuiSlot` automatically syncs its position with the vanilla `Slot.xPos`/`Slot.yPos` each tick.
- JEI recipe areas can be registered via `getJeiRecipeAreas()`.

## Using the Builder API

`GuiBuilder<T>` provides a fluent API for constructing components without verbose constructors:

```java
import com.x4yi.x4ui.client.gui.utils.GuiBuilder;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.common.State;

// Panel with flex layout
GuiPanel panel = GuiBuilder.createPanel()
    .position(10, 10)
    .size(200, 300)
    .flexDirection(FlexDirection.VERTICAL)
    .gap(5)
    .padding(new Insets(10))
    .build();

// Button with tooltip and click handler
GuiButton btn = GuiBuilder.createButton("Save")
    .position(0, 0)
    .size(120, 25)
    .tooltip("Save configuration")
    .onClick(() -> System.out.println("Saved!"))
    .build();

// Label bound to a reactive State
State<String> nameState = new State<>("Player");
GuiLabel label = GuiBuilder.createLabel("")
    .position(0, 0)
    .bindLabelText(nameState)
    .build();

// Slider with reactive state
State<Integer> volume = new State<>(50);
GuiSliderInt slider = GuiBuilder.createSliderInt(volume, 0, 100, 5)
    .position(0, 0)
    .size(150, 15)
    .build();
```

## Applying a Theme

Themes control all visual styles (colors, borders, tooltips). Apply a theme to the root panel to style the entire screen:

```java
import com.x4yi.x4ui.client.gui.utils.DefaultTheme;

@Override
protected void initComponents() {
    setTheme(DefaultTheme.INSTANCE);
    // ... add components
}
```

Custom themes implement `ITheme` and can be applied per-component or per-subtree:

```java
import com.x4yi.x4ui.client.gui.utils.ITheme;

ITheme myTheme = new ITheme() {
    @Override public int getPrimaryColor() { return 0xFFFF5722; }
    @Override public int getBackgroundColor() { return 0xFF263238; }
    @Override public int getTextColor() { return 0xFFFFFFFF; }
    // ... implement all methods
};

panel.setTheme(myTheme);
```

## Sending Actions to the Server

For client-to-server communication, set an `IGuiActionSender` on the screen:

```java
import com.x4yi.x4ui.common.sync.IGuiActionSender;
import net.minecraft.nbt.NBTTagCompound;

IGuiActionSender sender = (actionId, data) -> {
    // Send network packet to server
    MyModNetwork.CHANNEL.sendToServer(new MyPacket(actionId, data));
};

setActionSender(sender);
```
