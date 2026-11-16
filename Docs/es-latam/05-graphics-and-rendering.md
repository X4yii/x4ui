# 05 - Abstracción Gráfica y Renderizado (`IGraphics`)

Este documento describe la capa de renderizado inmediato `IGraphics` (`com.x4yi.x4ui.api.client.gui.IGraphics`) y su implementación `GLGraphics`, diseñada para aislar los componentes de llamadas directas a OpenGL/LWJGL.

---

## 1. Problema y Decisión de Diseño

Emitir llamadas crudas a `GL11`, `GlStateManager` o `Tessellator` dentro de widgets provoca estados de OpenGL corruptos (blending, depth, scissor, matrices) y rompe la portabilidad entre **Forge 1.12.2 (LWJGL 2)** y **CleanroomMC (LWJGL 3)**.

X4UI encapsula todo el dibujado detrás de `IGraphics`:
1. **Pila jerárquica determinista:** Cada `save()`, `pushAlpha()`, `pushColorTint()` y `pushClip()` se apila y restaura de forma controlada.
2. **Recorte anidado por hardware (`ScissorHelper`):** `pushClip()` calcula la intersección con el área de recorte activa del padre, evitando que hijos dentro de un `GuiScrollPanel` sobrescriban límites externos.
3. **Geometría portátil:** Las primitivas redondeadas, gradientes, carga de texturas BGRA y limpieza de profundidad (`clearDepthBuffer()`) residen en `GLGraphics` sin exponer OpenGL a los componentes.

---

## 2. El Contexto Gráfico en `renderSelf()`

```java
@Override
protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
    int absX = getAbsoluteX();
    int absY = getAbsoluteY();

    gfx.drawRect(absX, absY, width, height, 0xFF1E1E28);
    gfx.drawBorder(absX, absY, width, height, 1, 0xFF3E3E50);
    gfx.drawString("Panel de Control", absX + 8, absY + 6, 0xFFFFFFFF, true);
}
```

---

## 3. Pila Jerárquica de Estados

### Transformaciones (`save()` / `restore()`)

```java
gfx.save();
gfx.translate(absX + 50, absY + 50);
gfx.scale(1.5f, 1.5f);
// Dibujo transformado...
gfx.restore();
```

### Opacidad y Tinte (`pushAlpha()` / `pushColorTint()`)

```java
gfx.pushAlpha(0.5f);
gfx.pushColorTint(0xFFFF8888);
gfx.drawTexture(myTexture, absX, absY, 64, 64);
gfx.popColorTint();
gfx.popAlpha();
```

### Recorte Hardware (`pushClip()` / `popClip()`)

```java
gfx.pushClip(absX, absY, width, height);
renderOverflowingContent(gfx);
gfx.popClip();
```

---

## 4. Catálogo de Primitivas

| Método | Propósito |
| :--- | :--- |
| `drawRect(x, y, w, h, color)` | Rectángulo sólido ARGB. |
| `drawRoundedRect(x, y, w, h, radius, color)` | Rectángulo con esquinas redondeadas. |
| `drawBorder(x, y, w, h, thickness, color)` | Borde rectangular de grosor configurable. |
| `drawRoundedBorder(x, y, w, h, radius, thickness, color)` | Borde exterior con esquinas redondeadas. |
| `drawLine(x1, y1, x2, y2, thickness, color)` | Línea continua entre dos puntos. |
| `drawCircle(centerX, centerY, radius, color)` | Círculo relleno. |
| `drawGradient(x, y, w, h, topColor, bottomColor)` | Gradiente vertical entre dos colores. |
| `drawGradient4Corners(x, y, w, h, tl, tr, br, bl)` | Gradiente bilineal por esquina. |
| `drawString(text, x, y, color, shadow)` | Texto con la fuente activa (`IFont`). |
| `drawTexture(texture, x, y, w, h)` | Textura completa escalada al rectángulo. |
| `drawTexturedRect(x, y, w, h, u1, v1, u2, v2)` | Subregión UV de textura activa. |
| `clearDepthBuffer()` | Limpieza segura del buffer de profundidad (ej. modelos 3D). |

---

## 5. Tipografía Activa en `IGraphics`

El contexto gráfico mantiene la fuente activa (`IFont`) para las operaciones de medición y dibujado de texto:

```java
IFont customFont = FontRegistry.get("roboto_regular");
gfx.setFont(customFont);

int textW = gfx.getStringWidth("Texto de prueba");
int textH = gfx.getFontHeight();
gfx.drawString("Texto de prueba", absX, absY, 0xFFFFFFFF, true);
```

---

## 6. Qué Evitar en Renderizado

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Llamadas directas a `GL11`, `GL12` o `Tessellator` en componentes | Desincroniza la pila interna de estados y rompe portabilidad LWJGL 2/3 | Utilizar exclusivamente los métodos de `IGraphics` |
| Dejar desbalanceados `save()`, `pushClip()`, `pushAlpha()` o `pushColorTint()` | Corrompe las transformaciones y recortes de componentes posteriores | Emparejar cada `push`/`save` con su `pop`/`restore` en el mismo bloque |
| Recalcular polígonos o particionado de textos extensos en cada fotograma | Penaliza el rendimiento por frame (FPS drop) | Cachear resultados geométricos y recalcular solo al mutar datos |
