# First UI (Hello World)

In Vanilla, you extend `GuiScreen`. In X4UI, you use `GuiBaseScreen`, which automatically manages a root container (`rootPanel`).

## 1. Create the Screen

Create a class extending `GuiBaseScreen` and override `initComponents()`:

```java
import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.Minecraft;

public class GuiModOptions extends GuiBaseScreen {

    public GuiModOptions(GuiScreen parent) {
        // Pass null if there is no previous screen
        super(parent, "Mod Options");
    }

    @Override
    protected void initComponents() {
        // Stack children vertically
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(10);
        
        // Create components
        GuiLabel title = new GuiLabel(0, 0, "My Mod Options", 0xFFFFFF);
        
        GuiButton btnClose = new GuiButton(0, 0, 100, 20, "Back to Game", () -> {
            Minecraft.getMinecraft().player.closeScreen();
        });
        
        // Add children to the root panel
        rootPanel.addChild(title);
        rootPanel.addChild(btnClose);
    }
}
```

## 2. Show the Screen

Open the interface from your mod, for example from a KeyBinding event handler:

```java
import net.minecraft.client.Minecraft;

Minecraft.getMinecraft().displayGuiScreen(new GuiModOptions(null));
```

## What happens here?

1. `GuiBaseScreen` automatically creates `rootPanel` (a `GuiPanel`) the size of the screen.
2. Since we used `FlexDirection.VERTICAL`, the title and the button are automatically stacked one below the other, ignoring their initial local coordinates.
