# GuiMarkdown

A powerful component that lets you display large formatted texts (titles, bold, lists, and images) without having to program dozens of separate small text elements. It is perfect for creating modding tutorials or in-game manuals.

## Basic Usage (Raw Text)

```java
GuiMarkdown manual = new GuiMarkdown(0, 0, boxWidth, 
    "# Ore Generator\n" + 
    "This block **requires energy** to extract `diamonds`\n\n" + 
    "> Danger: It can explode if it overheats.\n\n" + 
    "![200x150](mymod:textures/gui/energy_diagram.png)"
);
```

## Loading a .md file from Assets

The cleanest and most recommended way is to save your texts in `.md` files inside your mod's resources folder (e.g. `src/main/resources/assets/mymod/docs/manual.md`), and read them using Minecraft's native system.

```java
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.IResource;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;

// 1. Get the file from the Minecraft ResourcePack
ResourceLocation mdLocation = new ResourceLocation("mymod", "docs/manual.md");
String mdContent = "";

try {
    IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(mdLocation);
    mdContent = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
} catch (Exception e) {
    mdContent = "# Error\nCould not load the manual.";
}

// 2. Create the component passing the loaded content
GuiMarkdown loadedManual = new GuiMarkdown(0, 0, 200, mdContent);
```

### Supported Elements

- **Titles (`#`)**: Displayed larger, bold, and add more space with the text below.
- **Lists (`* ` or `- `)**: Automatically add a bullet point and indentation. Useful for item lists.
- **Quotes (`> `)**: Draw a gray background rectangle to highlight warnings.
- **Images (`![WidthXHeight](texture)`)**: Shows Minecraft assets inside the text.

### Word Wrapping

You don't have to worry about putting line breaks (`\n`) when a sentence is too long. The component automatically wraps text to the next line when it exceeds the available width. Wrapping happens per-word (by spaces), so very long single words without spaces may overflow.

### Clickable Links

If you write `[Activate Machine](action_activate)`, the text will be blue and underlined. You can detect the click to execute code in your mod:

```java
manual.setOnLinkClicked(link -> {
    if (link.equals("action_activate")) {
        NetworkHandler.sendToServer(new PacketActivateMachine());
    }
});
```
