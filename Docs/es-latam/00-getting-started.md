# 00 - Primeros Pasos

X4UI es un framework moderno de interfaz gráfica para **Minecraft Forge 1.12.2 (Java 8)** y **CleanroomMC (Java 25)**. Proporciona una arquitectura completa basada en árbol de componentes retenidos, diseños flexbox responsivos, una capa gráfica inmediata abstracta (`IGraphics`), soporte nativo de fuentes vectoriales TrueType/OpenType (`IFont`), propagación jerárquica de eventos, enlace reactivo de estado (`State<T>`), red flexible servidor–cliente (`UINetwork`), animaciones fluidas y soporte de temas desacoplados.

## Requisitos

- Minecraft Forge 1.12.2 (`1.12.2-14.23.5.2847` o superior) o CleanroomMC
- Java 8 (para compilar mods consumidores y ejecutar Forge) o Java 25 (para ejecutar CleanroomMC)

## Instalación

Agregue la dependencia de X4UI en `build.gradle` usando uno de los siguientes repositorios:

### Modrinth (Recomendado)
```gradle
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    deobfCompile "maven.modrinth:x4ui:r1.0b6"
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

## Estructura de Paquetes

Todas las clases de X4UI se encuentran bajo `com.x4yi.x4ui`. La arquitectura separa interfaces públicas de implementación:

| Paquete | Propósito |
|---------|-----------|
| `com.x4yi.x4ui.api.client.gui` | Abstracción gráfica inmediata (`IGraphics`) |
| `com.x4yi.x4ui.impl.client.gui` | Implementación de bajo nivel de renderizado (`GLGraphics`) |
| `com.x4yi.x4ui.api.client.font` | Abstracción del motor de tipografía (`IFont`) |
| `com.x4yi.x4ui.impl.client.font` | Rasterizador TrueType (`TrueTypeFont`), registro de fuentes (`FontRegistry`) |
| `com.x4yi.x4ui.client.gui.base` | Clases abstractas de pantalla (`GuiBaseScreen`, `GuiBaseContainer`) |
| `com.x4yi.x4ui.client.gui.component` | Componentes visuales (`GuiPanel`, `GuiButton`, `GuiLabel`, `GuiImage`, etc.) |
| `com.x4yi.x4ui.client.gui.component.layout` | Motores de diseño (`FlexLayout`, `FlexDirection`) |
| `com.x4yi.x4ui.client.gui.component.slider` | Deslizadores modulares tipados (`GuiSliderInt`, `GuiSliderFloat`, `GuiSliderDouble`) |
| `com.x4yi.x4ui.client.gui.component.sprite` | Motor de animación 2D (`ISpriteSource`, `SpriteSheetSource`, `SequenceSource`) |
| `com.x4yi.x4ui.client.gui.component.image` | Configuración de bordes 9-slice (`NineSliceConfig`) |
| `com.x4yi.x4ui.client.gui.overlay` | Superposiciones e inyecciones en GUIs (`GuiOverlayManager`, `OverlayDockManager`) |
| `com.x4yi.x4ui.client.gui.compat.jei` | Integración con Just Enough Items (`JeiPlugin`) |
| `com.x4yi.x4ui.client.gui.bus` | Bus reactivo de estados desacoplados (`UIStateBus`) |
| `com.x4yi.x4ui.client.gui.utils` | Utilidades (`GuiBuilder`, `ITheme`, `DefaultTheme`, `ThemeRegistry`, `Insets`) |
| `com.x4yi.x4ui.common` | Primitivas de estado compartidas seguras para servidor (`State<T>`) |
| `com.x4yi.x4ui.common.sync` | Sincronización de propiedades de container (`NetworkSyncHelper`) |
| `com.x4yi.x4ui.proxy` | Aislamiento estricto de sidedness (`ClientProxy`, `CommonProxy`) |

---

## Crear una Pantalla Standalone (`GuiBaseScreen`)

Extienda `GuiBaseScreen` para pantallas que no interactúan con contenedores de inventario (menús de configuración, paneles de información, visores de estadísticas):

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
        super(parent, "Configuración");
    }

    @Override
    protected void initComponents() {
        // rootPanel se inicializa automáticamente abarcando toda la ventana
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(8);
        rootPanel.setPadding(new Insets(16));

        rootPanel.addChild(new GuiLabel(0, 0, "Opciones Principales", 0xFFFFFFFF)
            .setCentered(true));

        rootPanel.addChild(new GuiButton(0, 0, 160, 24, "Guardar Cambios", () -> {
            System.out.println("Configuración guardada");
        }));

        rootPanel.addChild(new GuiButton(0, 0, 160, 24, "Cerrar", this::closeScreen));
    }
}
```

### Abrir la Pantalla

```java
Minecraft.getMinecraft().displayGuiScreen(
    new MySettingsScreen(Minecraft.getMinecraft().currentScreen)
);
```

---

## Crear una Pantalla de Contenedor (`GuiBaseContainer`)

Extienda `GuiBaseContainer` para pantallas conectadas a un `Container` de inventario (bloques con inventario, cofres, máquinas):

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
        rootPanel.addChild(new GuiLabel(8, 6, "Inventario de Máquina", 0xFF404040));

        // GuiSlot sincroniza automáticamente su posición en pantalla con el Slot vanilla
        for (Slot slot : inventorySlots.inventorySlots) {
            rootPanel.addChild(new GuiSlot(slot, this));
        }
    }
}
```

---

## Métodos Fluent en `GuiComponent`

Los componentes cuentan con métodos fluidos encadenables directamente en su jerarquía base (`GuiComponent`):

```java
GuiButton btn = new GuiButton(0, 0, 120, 20, "Aceptar", this::onConfirm)
    .withPosition(10, 20)
    .withSize(140, 24)
    .withMargin(new Insets(4))
    .withLayer(5)
    .withPercentWidth(0.8f); // 80% del ancho del contenedor padre
```

También se puede usar `GuiBuilder` para construcción declarativa estática:

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

## Siguientes Pasos

- Revisa **Conceptos Fundamentales (`01`)** para ciclo de vida, `State<T>` y `UIStateBus`.
- Revisa **Redes y Sincronización (`03`)** para pantallas remotas, RPC y `Container` sync.
- Revisa **Diseño Responsivo (`04`)** para `FlexLayout`, `FlexAlign` y `GuiScrollPanel`.
- Revisa **Compatibilidad Forge ↔ CleanroomMC (`09`)** para reglas de portabilidad (`IGraphics`, `KeyCodes`, `UIInput`, `PlatformBridge`).
