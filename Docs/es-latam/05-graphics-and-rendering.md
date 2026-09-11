# 05 - Abstracción Gráfica y Renderizado (`IGraphics`)

X4UI r1.0b5 introduce la interfaz `IGraphics`. Esta capa desacopla el renderizado de llamadas crudas a OpenGL y LWJGL, gestionando matrices, recortes, tintes y opacidades.

---

## 1. El Contexto Gráfico Inmediato

Cada componente visual recibe una instancia de `IGraphics` en su método `renderSelf()`:

```java
@Override
protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
    int absX = getAbsoluteX();
    int absY = getAbsoluteY();

    // Dibujar fondo y borde
    gfx.drawRect(absX, absY, width, height, 0xFF1E1E28);
    gfx.drawBorder(absX, absY, width, height, 1, 0xFF3E3E50);

    // Dibujar texto con la fuente activa
    gfx.drawString("Panel de Control", absX + 8, absY + 6, 0xFFFFFFFF, true);
}
```

---

## 2. Pila Jerárquica de Estados

`IGraphics` mantiene una pila jerárquica que preserva y restaura transformaciones y atributos visuales:

### Guardado y Restauración (`save()` / `restore()`)
Permite aplicar transformaciones matriciales locales sin afectar a los componentes hermanos o padres:

```java
gfx.save();
gfx.translate(absX + 50, absY + 50);
gfx.scale(1.5f, 1.5f);
// Renderizar elemento escalado...
gfx.restore(); // Restaura la matriz y el estado anterior
```

### Modulación de Opacidad (`pushAlpha()` / `popAlpha()`)
Multiplica la transparencia global de todas las operaciones de dibujo subsiguientes:

```java
gfx.pushAlpha(0.5f); // 50% de transparencia acumulativa
gfx.drawRect(absX, absY, 100, 100, 0xFF00FF00);
gfx.drawString("Texto semitransparente", absX, absY, 0xFFFFFFFF, false);
gfx.popAlpha(); // Restaura la opacidad anterior
```

### Modulación de Tinte de Color (`pushColorTint()` / `popColorTint()`)
Aplica una modulación de color multiplicativa en formato ARGB:

```java
gfx.pushColorTint(0xFFFF8888); // Tinte rojizo
gfx.drawTexture(myTexture, absX, absY, 64, 64);
gfx.popColorTint();
```

### Recorte por Tijera Hardware (`pushClip()` / `popClip()`)
Aplica recorte mediante `GL_SCISSOR_TEST` que se auto-intersecta con los rectángulos de recorte existentes:

```java
gfx.pushClip(absX, absY, width, height);
// Todo lo que se dibuje aquí será recortado si excede los límites
renderOverflowingContent(gfx);
gfx.popClip();
```

---

## 3. Catálogo de Primitivas de Dibujo

| Método | Propósito |
|--------|-----------|
| `drawRect(x, y, w, h, color)` | Rectángulo sólido con color ARGB |
| `drawRoundedRect(x, y, w, h, radius, color)` | Rectángulo con esquinas redondeadas continuas |
| `drawBorder(x, y, w, h, thickness, color)` | Borde rectangular con grosor configurable |
| `drawRoundedBorder(x, y, w, h, radius, thickness, color)` | Borde exterior con esquinas redondeadas |
| `drawLine(x1, y1, x2, y2, thickness, color)` | Línea continua entre dos puntos |
| `drawCircle(centerX, centerY, radius, color)` | Círculo relleno suave |
| `drawGradient(x, y, w, h, topColor, bottomColor)` | Gradiente vertical de dos colores |
| `drawGradient4Corners(x, y, w, h, tl, tr, br, bl)` | Gradiente bilineal con color independiente por esquina |
| `drawString(text, x, y, color, shadow)` | Renderizado de texto con la fuente activa (`IFont`) |
| `drawTexture(texture, x, y, w, h)` | Textura completa estirada a las dimensiones |
| `drawTexturedRect(x, y, w, h, u1, v1, u2, v2)` | Subregión de textura mapeada por coordenadas UV |

---

## 4. Tipografía Activa

El contexto `IGraphics` propaga la tipografía activa (`IFont`). Si un componente padre define una fuente, todos sus descendientes la heredan automáticamente:

```java
IFont customFont = FontRegistry.get("roboto_medium");
gfx.setFont(customFont);

int textW = gfx.getStringWidth("Hola Mundo");
int textH = gfx.getFontHeight();
gfx.drawString("Hola Mundo", absX, absY, 0xFFFFFFFF, true);
```

---

## Qué Evitar Hacer (Peligros Críticos de Renderizado)

> [!CAUTION]
> **1. NUNCA emitir primitivas gráficas por su cuenta en orden horario (Clockwise).**
> Utilice siempre los métodos provistos por `IGraphics`. De lo contrario, OpenGL clasificará y descartará silenciosamente la geometría.

> [!WARNING]
> **2. NUNCA dejar pilas desbalanceadas de `save()`, `pushClip()` o `pushAlpha()`.**
> Siempre que aplique un estado u opacidad, asegúrese de invocar su respectivo `pop()` para no romper el renderizado de otros componentes.

> [!WARNING]
> **3. NUNCA activar o desactivar estados puros de OpenGL sin restaurarlos.**
> Si invoca `GL11.glEnable()` o análogos en `renderSelf()`, restaure el estado anterior obligatoriamente antes de retornar.

> [!IMPORTANT]
> **4. NUNCA recalcular geometrías matemáticas complejas en cada frame.**
> Cachee cálculos gráficos costosos (trazado de polígonos complejos o textos enormes) y actualícelos solo cuando los datos origen cambien.
