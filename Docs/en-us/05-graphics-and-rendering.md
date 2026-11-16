# 05 - Graphics Abstraction & Rendering (`IGraphics`)

This document describes the immediate rendering contract `IGraphics` (`com.x4yi.x4ui.api.client.gui.IGraphics`) and its `GLGraphics` backend, isolating UI components from direct OpenGL and LWJGL calls.

---

## 1. Problem & Design Decisions

Issuing raw `GL11`, `GlStateManager`, or `Tessellator` calls inside widgets corrupts shared OpenGL state (blending, depth, scissor, matrices) and breaks portability between **Forge 1.12.2 (LWJGL 2)** and **CleanroomMC (LWJGL 3)**.

X4UI encapsulates all drawing behind `IGraphics`:
1. **Deterministic state stack:** Every `save()`, `pushAlpha()`, `pushColorTint()`, and `pushClip()` call is tracked and restored cleanly.
2. **Nested hardware clipping (`ScissorHelper`):** `pushClip()` intersects new bounds with the parent's active clip rectangle so nested scroll panels never bleed outward.
3. **Portable primitives:** Rounded geometry, gradients, BGRA texture uploads, and depth buffer clearing (`clearDepthBuffer()`) live inside `GLGraphics` without exposing OpenGL to widgets.

---

## 2. Graphics Context in `renderSelf()`

```java
@Override
protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
    int absX = getAbsoluteX();
    int absY = getAbsoluteY();

    gfx.drawRect(absX, absY, width, height, 0xFF1E1E28);
    gfx.drawBorder(absX, absY, width, height, 1, 0xFF3E3E50);
    gfx.drawString("Control Panel", absX + 8, absY + 6, 0xFFFFFFFF, true);
}
```

---

## 3. Hierarchical State Stack

### Matrix Transforms (`save()` / `restore()`)

```java
gfx.save();
gfx.translate(absX + 50, absY + 50);
gfx.scale(1.5f, 1.5f);
// Transformed draw calls...
gfx.restore();
```

### Opacity & Color Tint (`pushAlpha()` / `pushColorTint()`)

```java
gfx.pushAlpha(0.5f);
gfx.pushColorTint(0xFFFF8888);
gfx.drawTexture(myTexture, absX, absY, 64, 64);
gfx.popColorTint();
gfx.popAlpha();
```

### Hardware Scissor Clipping (`pushClip()` / `popClip()`)

```java
gfx.pushClip(absX, absY, width, height);
renderOverflowingContent(gfx);
gfx.popClip();
```

---

## 4. Drawing Primitives Catalog

| Method | Purpose |
| :--- | :--- |
| `drawRect(x, y, w, h, color)` | Solid ARGB rectangle. |
| `drawRoundedRect(x, y, w, h, radius, color)` | Rounded-corner rectangle. |
| `drawBorder(x, y, w, h, thickness, color)` | Rectangular border with configurable thickness. |
| `drawRoundedBorder(x, y, w, h, radius, thickness, color)` | Rounded outer border. |
| `drawLine(x1, y1, x2, y2, thickness, color)` | Line segment between two points. |
| `drawCircle(centerX, centerY, radius, color)` | Filled circle. |
| `drawGradient(x, y, w, h, topColor, bottomColor)` | Vertical two-color gradient. |
| `drawGradient4Corners(x, y, w, h, tl, tr, br, bl)` | Bilinear per-corner gradient. |
| `drawString(text, x, y, color, shadow)` | Text rendering with the active `IFont`. |
| `drawTexture(texture, x, y, w, h)` | Full texture stretched to bounds. |
| `drawTexturedRect(x, y, w, h, u1, v1, u2, v2)` | UV-mapped subregion of the bound texture. |
| `clearDepthBuffer()` | Safe depth buffer clear (e.g. for 3D models). |

---

## 5. Active Typography on `IGraphics`

`IGraphics` propagates the active `IFont` for text measurement and rendering:

```java
IFont customFont = FontRegistry.get("roboto_regular");
gfx.setFont(customFont);

int textW = gfx.getStringWidth("Sample Text");
int textH = gfx.getFontHeight();
gfx.drawString("Sample Text", absX, absY, 0xFFFFFFFF, true);
```

---

## 6. What to Avoid in Rendering

| Anti-Pattern | Technical Root Cause | Correct Approach |
| :--- | :--- | :--- |
| Direct `GL11`, `GL12`, or `Tessellator` calls inside widgets | Desynchronizes internal render state and breaks LWJGL 2/3 portability | Use `IGraphics` methods exclusively |
| Unbalanced `save()`, `pushClip()`, `pushAlpha()`, or `pushColorTint()` | Corrupts transforms and clipping for subsequent components | Always pair every `push`/`save` with its `pop`/`restore` |
| Recomputing complex geometry or word-wrapping large text every frame | Wastes CPU cycles and degrades frame rate | Cache computed geometry/text lines and update only when source data changes |
