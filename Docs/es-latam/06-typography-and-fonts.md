# 06 - Tipografía Vectorial y Fuentes (`IFont`)

Este documento detalla el sistema de tipografía de X4UI (`r1.0b6`), centrado en la interfaz `IFont` (`com.x4yi.x4ui.api.client.font.IFont`) y el registro central `FontRegistry`.

---

## 1. Problema y Decisión de Diseño

El `FontRenderer` de vanilla está limitado a texturas bitmap fijas que se pixelan en resoluciones HiDPI y escalas grandes. Por otro lado, instanciar fuentes TrueType sin control agota la VRAM y recalcular anchos de texto en listas o markdown degrada los FPS.

X4UI resuelve esto mediante:
1. **Abstracción `IFont`:** Unifica `VanillaFont` y `TrueTypeFont` bajo un mismo contrato de medición y dibujado.
2. **Atlas dinámico en VRAM (`TrueTypeFont`):** Rasteriza glifos bajo demanda con anti-aliasing y soporte completo de códigos vanilla (`§0`–`§f`, `§l`, `§o`, `§n`).
3. **Caché LRU de anchos (`FontWidthCache`):** Almacena mediciones repetidas con evicción acotada para evitar fugas de memoria en interfaces ricas en texto.
4. **Ciclo de vida centralizado (`FontRegistry`):** Gestiona carga única y recarga transparente en caliente al pulsar `F3 + T`.

---

## 2. Contrato `IFont` e Implementaciones

| Método | Retorno | Descripción |
| :--- | :--- | :--- |
| `drawString(text, x, y, color, shadow)` | `float` | Renderiza texto y retorna el avance horizontal en X. |
| `getStringWidth(text)` | `int` | Calcula el ancho lógico en píxeles ignorando códigos `§`. |
| `getFontHeight()` | `int` | Retorna la altura de línea. |
| `splitText(text, maxWrapWidth)` | `List<String>` | Divide el texto en líneas respetando saltos de palabra. |
| `trimStringToWidth(text, width)` | `String` | Trunca el texto al ancho máximo especificado. |

- **`VanillaFont`:** Envuelve el `FontRenderer` nativo de Minecraft.
- **`TrueTypeFont`:** Rasterizador vectorial para archivos `.ttf` y `.otf`.

---

## 3. Registro Central (`FontRegistry`)

Registrar las fuentes una única vez durante la inicialización del cliente (`ClientProxy` / `FMLPreInitializationEvent`):

```java
// Desde recursos del mod (src/main/resources/assets/mymod/fonts/)
FontRegistry.registerTrueType(
    "roboto_regular",
    new ResourceLocation("mymod", "fonts/roboto-regular.ttf"),
    14.0f
);

// Fuente del sistema operativo (usar solo como respaldo)
FontRegistry.registerSystem("sans_fallback", "SansSerif", Font.PLAIN, 14.0f);

// Recuperación en componentes
IFont font = FontRegistry.getOrDefault("roboto_regular", FontRegistry.getDefault());
```

---

## 4. Asignación en Componentes y Recarga (`F3 + T`)

Los componentes y contenedores heredan la fuente asignada mediante `withFont(...)`:

```java
GuiLabel title = new GuiLabel(0, 0, "Panel", 0xFFFFFFFF)
    .withFont(FontRegistry.get("roboto_regular"));

headerPanel.withFont(FontRegistry.get("roboto_regular")); // Propaga a los hijos
```

`FontRegistry` escucha la recarga de paquetes de recursos de Minecraft (`F3 + T`): al ejecutarse, libera las texturas previas y recarga automáticamente las fuentes registradas mediante `ResourceLocation`.

---

## 5. Qué Evitar en Tipografía

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Instanciar `new TrueTypeFont(...)` en `initComponents()` o `renderSelf()` | Crea atlas de texturas duplicados en VRAM en cada apertura o frame | Registrar una vez en `FontRegistry` y consultar por clave |
| Depender únicamente de `registerSystem(...)` | La fuente puede no existir en el sistema operativo del jugador | Empaquetar el `.ttf`/`.otf` en `assets/<modid>/fonts/` |
| Registrar tamaños extremos (> 72 pt) | Consume VRAM innecesaria para el atlas de glifos | Registrar tamaño estándar (ej. 16–24 pt) y escalar con `gfx.scale(...)` |
| Omitir `dispose()` en fuentes manuales | Deja texturas OpenGL huérfanas fuera del ciclo de `FontRegistry` | Delegar en `FontRegistry` o invocar `font.dispose()` al liberar |
