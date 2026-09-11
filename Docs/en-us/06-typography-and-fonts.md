# 06 - Vector Typography & Fonts (`IFont`)

X4UI r1.0b5 features a hardware-accelerated vector typography engine centered on the `IFont` interface (`com.x4yi.x4ui.api.client.font.IFont`) and the centralized `FontRegistry`. It enables crisp TrueType/OpenType (`.ttf`/`.otf`) text rendering with continuous anti-aliasing, native support for vanilla formatting codes (`§0`-`§f`, `§l`, etc.), and dynamic hot-reloading.

---

## 1. The `IFont` Abstraction

Visual components in X4UI seamlessly interact with either vanilla bitmap fonts or custom vector typefaces via `IFont`:

| Method | Return | Description |
|--------|--------|-------------|
| `drawString(text, x, y, color, shadow)` | `float` | Renders text string and returns horizontal advance in X |
| `getStringWidth(text)` | `int` | Computes pixel width while stripping formatting codes (`§`) |
| `getFontHeight()` | `int` | Returns font line height |
| `splitText(text, maxWrapWidth)` | `List<String>` | Wraps string into lines based on word boundaries |
| `trimStringToWidth(text, width)` | `String` | Truncates text string to fit target pixel width |

---

## 2. Built-in Implementations

### `VanillaFont`
Wraps Minecraft's native `FontRenderer`, ensuring exact visual fidelity and pixel-perfect backward compatibility with vanilla text rendering.

### `TrueTypeFont`
Hardware-accelerated dynamic vector font engine:
- On-demand glyph rasterization into dynamic OpenGL texture atlases in VRAM.
- Bicubic anti-aliasing for superior legibility on HiDPI and 4K displays.
- Full support for vanilla color codes (`§a`, `§c`, etc.) and styles (`§l` bold, `§o` italic, `§n` underline).
- Batched drop-shadow rendering in a single draw pass with 1 px offset.

---

## 3. Central Registry (`FontRegistry`)

`FontRegistry` centralizes loading, caching, and hot-reloading:

### Registering TrueType Fonts from Mod Assets
Place your `.ttf` or `.otf` files under `src/main/resources/assets/mymod/fonts/`:

```java
// Inside your ClientProxy (during FMLPreInitializationEvent)
FontRegistry.registerTrueType(
    "roboto_regular",
    new ResourceLocation("mymod", "fonts/roboto-regular.ttf"),
    14.0f // Point size
);

FontRegistry.registerTrueType(
    "jetbrains_mono",
    new ResourceLocation("mymod", "fonts/jetbrains-mono.ttf"),
    12.0f
);
```

### Registering Operating System Fonts
```java
FontRegistry.registerSystem("arial", "Arial", Font.PLAIN, 14.0f);
```

### Querying Registered Fonts
```java
IFont roboto = FontRegistry.get("roboto_regular");
IFont mono = FontRegistry.getOrDefault("jetbrains_mono", FontRegistry.getDefault());
```

---

## 4. Applying Fonts to Components

Fonts can be assigned to individual components or propagated down entire container subtrees using the fluent `withFont()` builder:

```java
// Assign font to a label
GuiLabel title = new GuiLabel(10, 10, "System Settings", 0xFFFFFFFF)
    .withFont(FontRegistry.get("roboto_regular"));

// Assign font to a button
GuiButton actionBtn = new GuiButton(10, 40, 120, 24, "Apply", this::onApply)
    .withFont(FontRegistry.get("roboto_regular"));

// Setting a font on a parent container propagates to all children by default
headerPanel.withFont(FontRegistry.get("roboto_regular"));
```

---

## 5. Resource Pack Hot-Reloading (`F3 + T`)

`FontRegistry` subscribes to Minecraft's resource reload bus. When players press `F3 + T` to reload resource packs, all fonts registered via `ResourceLocation` automatically dispose their GPU texture atlases and reload from the newly active resource packs.

---

## What to Avoid (Font Pitfalls & Memory Leaks)

> [!CAUTION]
> **1. NEVER instantiate `new TrueTypeFont(...)` per tick or inside `renderSelf()`.**
> Always register fonts once in `FontRegistry`. Re-creating fonts continuously will exhaust GPU memory and crash the client.

> [!WARNING]
> **2. NEVER depend solely on OS system fonts (`registerSystem`).**
> If a font is absent on a player's machine, the UI spacing will break. Always bundle `.ttf` files in your mod.

> [!WARNING]
> **3. NEVER register excessively large font sizes (e.g., above 72 pt).**
> Huge sizes consume significant VRAM. For massive headings, prefer a 24 pt font scaled via `gfx.scale(2.0f, 2.0f)`.

> [!IMPORTANT]
> **4. NEVER forget to invoke `dispose()` on unmanaged fonts.**
> If you manually instantiate a font outside `FontRegistry`, call `font.dispose()` when it's no longer used.
