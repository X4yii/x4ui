# 06 - Vector Typography & Fonts (`IFont`)

This document covers X4UI's typography system (`r1.0b7`), centered on the `IFont` contract (`com.x4yi.x4ui.api.client.font.IFont`) and the central `FontRegistry`.

---

## 1. Problem & Design Decisions

Vanilla's `FontRenderer` relies on fixed bitmap sheets that blur or alias under HiDPI displays and large GUI scales. Conversely, naive TrueType integration exhausts VRAM with duplicate glyph atlases and stalls frames when measuring long strings repeatedly.

X4UI solves this with four design decisions:
1. **`IFont` abstraction:** Unifies `VanillaFont` and `TrueTypeFont` behind one measurement and rendering contract.
2. **Dynamic VRAM glyph atlas (`TrueTypeFont`):** Rasterizes glyphs on demand with anti-aliasing and full vanilla formatting code support (`§0`–`§f`, `§l`, `§o`, `§n`). Once uploaded to GPU (`DynamicTexture`), the CPU-side `BufferedImage` atlas (~4 MB per font) is immediately released from heap.
3. **Bounded width cache (`FontWidthCache`):** LRU eviction caches repeated width lookups without unbounded memory growth.
4. **Centralized lifecycle (`FontRegistry`):** Ensures single-load registration and transparent hot-reloading on `F3 + T`.

---

## 2. `IFont` Contract & Built-In Implementations

| Method | Return | Description |
| :--- | :--- | :--- |
| `drawString(text, x, y, color, shadow)` | `float` | Renders the string and returns horizontal X advance. |
| `getStringWidth(text)` | `int` | Computes logical pixel width ignoring `§` formatting codes. |
| `getFontHeight()` | `int` | Returns line height. |
| `splitText(text, maxWrapWidth)` | `List<String>` | Word-wraps text to fit within `maxWrapWidth`. |
| `trimStringToWidth(text, width)` | `String` | Truncates text to fit within `width`. |

- **`VanillaFont`:** Wraps Minecraft's built-in `FontRenderer`.
- **`TrueTypeFont`:** Hardware-accelerated vector rasterizer for `.ttf` and `.otf` assets.

---

## 3. Central Registry (`FontRegistry`)

Register fonts once during client initialization (`ClientProxy` / `FMLPreInitializationEvent`):

```java
// From mod assets (src/main/resources/assets/mymod/fonts/)
FontRegistry.registerTrueType(
    "roboto_regular",
    new ResourceLocation("mymod", "fonts/roboto-regular.ttf"),
    14.0f
);

// OS font (use only as fallback)
FontRegistry.registerSystem("sans_fallback", "SansSerif", Font.PLAIN, 14.0f);

// Retrieval in components
IFont font = FontRegistry.getOrDefault("roboto_regular", FontRegistry.getDefault());
```

---

## 4. Component Assignment & Hot-Reloading (`F3 + T`)

Components and containers inherit fonts assigned via `withFont(...)`:

```java
GuiLabel title = new GuiLabel(0, 0, "Panel", 0xFFFFFFFF)
    .withFont(FontRegistry.get("roboto_regular"));

headerPanel.withFont(FontRegistry.get("roboto_regular")); // Propagates to children
```

`FontRegistry` hooks into Minecraft's resource reload pipeline (`F3 + T`), releasing previous textures and reloading `ResourceLocation`-backed fonts automatically.

---

## 5. What to Avoid in Typography

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Instantiating `new TrueTypeFont(...)` in `initComponents()` or `renderSelf()` | Allocates duplicate glyph atlases in VRAM on every open or frame | Register once in `FontRegistry` and look up by key |
| Relying solely on `registerSystem(...)` | Target font may be absent on the player's OS | Bundle `.ttf`/`.otf` files inside `assets/<modid>/fonts/` |
| Registering oversized point sizes (> 72 pt) | Wastes VRAM for large glyph textures | Register a moderate size (e.g. 16–24 pt) and scale with `gfx.scale(...)` |
| Omitting `dispose()` on manually managed fonts | Leaks OpenGL textures outside `FontRegistry` lifecycle | Manage via `FontRegistry` or call `font.dispose()` on teardown |
