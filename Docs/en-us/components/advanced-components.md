# Advanced Components

This document covers the advanced components: `GuiMarkdown`, `GuiModelRender`, `GuiDropdown`, and the tooltip system.

---

## GuiMarkdown

A lightweight markdown parser and renderer for in-game documentation. Parses raw markdown text into a tree of renderable nodes.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiMarkdown`

### Constructor

```java
new GuiMarkdown(int x, int y, int width, String text)
```

The height is automatically calculated based on the parsed content.

### Basic Usage

```java
import com.x4yi.x4ui.client.gui.component.GuiMarkdown;

String markdown = "# Title\n\nSome **bold** and _italic_ text.\n\n- Item 1\n- Item 2\n\n> This is a blockquote";

GuiMarkdown display = new GuiMarkdown(10, 10, 300, markdown);
rootPanel.addChild(display);
```

### In a Scroll Panel

```java
import com.x4yi.x4ui.client.gui.component.GuiScrollPanel;

GuiScrollPanel scroll = new GuiScrollPanel(10, 10, 350, 250);
GuiMarkdown text = new GuiMarkdown(0, 0, 330, loadMarkdownFile());
scroll.addChild(text);
rootPanel.addChild(scroll);
```

### Updating Content

```java
display.setText("# New Title\n\nUpdated content.");
```

### Supported Syntax

#### Headings

```markdown
# Heading 1
## Heading 2
### Heading 3
```

Headings are rendered in bold with distinct colors:
- H1: `0xFF00E5FF` (cyan)
- H2: `0xFF00E676` (green)
- H3: `0xFFFFD600` (yellow)

#### Bold and Italic

```markdown
**bold text**
__also bold__
*italic text*
_and also italic_
```

#### Inline Code

```markdown
`code snippet`
```

Rendered in `0xFFFFAB40` (amber).

#### Links

```markdown
[link text](https://example.com)
```

Links are rendered in `0xFF29B6F6` (light blue) with an underline. Click detection is handled via `onLinkClicked`.

#### Blockquotes

```markdown
> This is a blockquote
```

Rendered with a left bar (`0xFFAAAAAA`) and indented text.

#### Bullet Lists

```markdown
- Item 1
- Item 2
  - Nested item
    - Deeply nested
```

Supports up to 3 levels of nesting with colored bullet markers:
- Level 0: `0xFF00C853` (green)
- Level 1: `0xFF888892` (gray)
- Level 2: `0xFF555560` (dark gray)

#### Separators

```markdown
---
***
___
```

Rendered as a horizontal line in `0xFF444450`.

#### Images

```markdown
![alt text](mymod:textures/gui/image.png)
```

The `alt` text can specify dimensions: `![64x64](mymod:textures/icon.png)`. Default size is 64x64.

### Link Click Handling

```java
import com.x4yi.x4ui.client.gui.component.GuiMarkdown;

GuiMarkdown md = new GuiMarkdown(0, 0, 300, "Click [here](https://example.com) to learn more.");
md.onLinkClicked = url -> {
    System.out.println("Link clicked: " + url);
    // Open URL, navigate, etc.
};
```

### Anchor Navigation

Headings are automatically registered as anchors. The anchor ID is the heading text lowercased with non-alphanumeric characters replaced by hyphens.

```java
// For heading: ## Getting Started
int yPosition = md.getAnchorY("getting-started");
```

### Public Fields

| Field | Type | Description |
|-------|------|-------------|
| `onLinkClicked` | `Consumer<String>` | Callback for link clicks |
| `textColor` | `int` | Default text color (0xFFE0E0E6) |
| `colorH1` | `int` | H1 color (0xFF00E5FF) |
| `colorH2` | `int` | H2 color (0xFF00E676) |
| `colorH3` | `int` | H3 color (0xFFFFD600) |

### Rendering

- Text is word-wrapped at word boundaries
- Each word is measured via `FontWidthCache` for performance
- Links are underlined with a 1px line
- Empty lines add 6px vertical spacing
- Line height is 10px

---

## GuiModelRender

Renders a 3D entity or item stack inside the 2D GUI. Supports drag rotation, cursor-following rotation, and configurable scale/offset.

> **Note:** This component is in a very early stage of development. Its API may change without notice in future versions.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiModelRender`

### Constructor

```java
new GuiModelRender(int x, int y, int width, int height)
```

### Rendering an Entity

```java
import com.x4yi.x4ui.client.gui.component.GuiModelRender;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntitySheep;

GuiModelRender entityRender = new GuiModelRender(50, 50, 100, 100);

EntitySheep sheep = new EntitySheep(Minecraft.getMinecraft().world);
entityRender.setEntity(sheep);
entityRender.setScale(30f);

rootPanel.addChild(entityRender);
```

### Rendering an Item Stack

```java
import com.x4yi.x4ui.client.gui.component.GuiModelRender;
import net.minecraft.init.Items;

GuiModelRender itemRender = new GuiModelRender(200, 50, 64, 64);
itemRender.setItemStack(new ItemStack(Items.DIAMOND_SWORD));
itemRender.setScale(25f);

rootPanel.addChild(itemRender);
```

### Cursor-Following Rotation

The entity rotates to face the mouse cursor:

```java
entityRender.setLookAtCursor(true);
```

### Drag Rotation

The user can click and drag to rotate the entity:

```java
entityRender.setAllowDragRotation(true);
```

### Static Rotation

```java
entityRender.setRotation(0f, 45f, 0f);  // yaw, pitch, roll
```

### Translation Offset

```java
entityRender.setTranslation(0f, -10f, 0f);  // x, y, z offset in pixels
```

### Disabling Entity Animations

```java
entityRender.setEnableEntityAnimations(false);
```

When disabled, the entity's idle animations (breathing, blinking) are frozen.

### Fluent API

```java
import com.x4yi.x4ui.client.gui.utils.GuiBuilder;

GuiModelRender render = GuiBuilder.createModelRender()
    .position(50, 50)
    .size(100, 100)
    .entity(sheep)
    .modelScale(30f)
    .modelRotation(0f, 45f, 0f)
    .modelDragRotation(true)
    .modelLookAtCursor(false)
    .build();
```

### Properties

| Method | Type | Description |
|--------|------|-------------|
| `setEntity(EntityLivingBase)` | `GuiModelRender` | Sets entity to render (clears itemStack) |
| `setItemStack(ItemStack)` | `GuiModelRender` | Sets item to render (clears entity) |
| `setScale(float)` | `GuiModelRender` | Sets render scale (default: 30) |
| `setRotation(float, float, float)` | `GuiModelRender` | Sets yaw, pitch, roll |
| `setTranslation(float, float, float)` | `GuiModelRender` | Sets x, y, z offset |
| `setEnableEntityAnimations(boolean)` | `GuiModelRender` | Enables/disables entity animations |
| `setAllowDragRotation(boolean)` | `GuiModelRender` | Enables drag-to-rotate |
| `setLookAtCursor(boolean)` | `GuiModelRender` | Enables cursor-following rotation |

### Rendering Details

- Uses `RenderManager.renderEntity()` for entities
- Uses `RenderItem.renderItemIntoGUI()` for items
- Properly saves and restores entity rotation state to prevent side effects
- Applies standard item lighting via `RenderHelper.enableStandardItemLighting()`
- Shadow is disabled during render (`setRenderShadow(false)`)

---

## GuiDropdown

A generic dropdown/select component. Opens a popup list of options below the button when clicked.

**Package:** `com.x4yi.x4ui.client.gui.component.GuiDropdown`

### Constructor

```java
new GuiDropdown<T>(int x, int y, int width, int height, State<T> state, List<T> options)
```

### Basic Usage

```java
import com.x4yi.x4ui.client.gui.component.GuiDropdown;
import com.x4yi.x4ui.common.State;
import java.util.Arrays;

State<String> selected = new State<>("Option 1");

GuiDropdown<String> dropdown = new GuiDropdown<>(
    10, 10, 150, 20,
    selected,
    Arrays.asList("Option 1", "Option 2", "Option 3")
);

rootPanel.addChild(dropdown);

// Listen for selection changes
selected.addListener(value -> System.out.println("Selected: " + value));
```

### Enum Dropdown

```java
import com.x4yi.x4ui.client.gui.component.GuiDropdown;
import com.x4yi.x4ui.common.State;

enum Difficulty { EASY, NORMAL, HARD }

State<Difficulty> difficulty = new State<>(Difficulty.NORMAL);

GuiDropdown<Difficulty> diffDropdown = new GuiDropdown<>(
    10, 40, 150, 20,
    difficulty,
    Arrays.asList(Difficulty.values())
);

rootPanel.addChild(diffDropdown);
```

### Updating Options

```java
dropdown.setOptions(Arrays.asList("New A", "New B", "New C", "New D"));
```

### Behavior

- Clicking the button toggles the popup open/closed
- Clicking an option selects it and closes the popup
- Clicking outside the popup closes it
- The popup temporarily disables OpenGL scissor to render outside parent bounds
- Maximum 5 visible items in the popup (scrolling not supported in popup)
- The popup is rendered at `layer = 100` by default (above most components)

### Rendering

- Button: Dark background (`0xFF16161E`) with border (`0xFF2C2C36`)
- Current value: Drawn at the left of the button
- Arrow: Unicode arrow character at the right ("▼" when closed, "▲" when open)
- Popup: Same styling as button, with hover highlight (`0xFF2C2C36`)
- Text color: `0xFFFFFFFF`

### Properties

| Method | Type | Description |
|--------|------|-------------|
| `setOptions(List<T>)` | `void` | Replaces the option list |
| `state` | `State<T>` | The bound state (public field) |

---

## Tooltip System

Tooltips are rendered by the theme and support both text and custom component tooltips.

### Text Tooltips

Any component can display a text tooltip on hover:

```java
button.setTooltip("Click to save");
```

Multi-line tooltips use `\n`:

```java
button.setTooltip("Line 1\nLine 2\nLine 3");
```

### Custom Component Tooltips

For rich tooltips with formatting, icons, or interactive elements:

```java
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiImage;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

GuiPanel tooltip = new GuiPanel(0, 0, 180, 60);
tooltip.setFlexDirection(FlexDirection.VERTICAL);

tooltip.addChild(new GuiLabel(0, 0, "Diamond Sword", 0xFF00E5FF));
tooltip.addChild(new GuiLabel(0, 0, "Damage: 7", 0xFFAAAAAA));
tooltip.addChild(new GuiLabel(0, 0, "Durability: 1561", 0xFFAAAAAA));

button.setTooltipComponent(tooltip);
```

### Tooltip Resolution

Tooltips are resolved by walking up the parent chain. If a child does not have a tooltip, the parent's tooltip is used:

```java
GuiPanel panel = new GuiPanel(0, 0, 200, 200);
panel.setTooltip("Panel tooltip");

GuiButton btn = new GuiButton(0, 0, 100, 20, "Button", null);
// btn has no tooltip, so panel's tooltip is displayed when hovering btn
panel.addChild(btn);
```

### Theme Integration

Tooltips are rendered via `ITheme.drawTooltip()` and `ITheme.drawTooltipComponent()`. The `DefaultTheme` implementation:

- Draws a dark background with border
- Positions the tooltip near the cursor
- Clamps to screen edges to prevent off-screen rendering
- Caches and reuses a `GuiTooltip` panel for text tooltips
