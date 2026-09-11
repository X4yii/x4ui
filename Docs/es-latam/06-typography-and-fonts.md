# 06 - Sistema de Tipografía y Fuentes Vectoriales (`IFont`)

X4UI r1.0b5 introduce un sistema completo de tipografía vectorial acelerado por hardware centrado en la interfaz `IFont` (`com.x4yi.x4ui.api.client.font.IFont`) y el registro central `FontRegistry`. Permite renderizar fuentes TrueType/OpenType (`.ttf`/`.otf`) con anti-aliasing continuo, soporte nativo de códigos de formato vanilla (`§0`-`§f`, `§l`, etc.) y recarga dinámica en caliente.

---

## 1. La Abstracción `IFont`

Cualquier componente visual de X4UI puede trabajar indistintamente con fuentes de Minecraft o con fuentes vectoriales personalizadas a través de `IFont`:

| Método | Retorno | Descripción |
|--------|---------|-------------|
| `drawString(text, x, y, color, shadow)` | `float` | Dibuja la cadena de texto y retorna el avance horizontal en X |
| `getStringWidth(text)` | `int` | Calcula el ancho en píxeles ignorando códigos de formato (`§`) |
| `getFontHeight()` | `int` | Retorna la altura de línea de la tipografía |
| `splitText(text, maxWrapWidth)` | `List<String>` | Divide el texto en múltiples líneas respetando saltos de palabra |
| `trimStringToWidth(text, width)` | `String` | Trunca la cadena de texto hasta que quepa en el ancho especificado |

---

## 2. Implementaciones Integradas

### `VanillaFont`
Envuelve el `FontRenderer` nativo de Minecraft, asegurando retrocompatibilidad absoluta con el estilo gráfico tradicional del juego.

### `TrueTypeFont`
Motor de rasterización tipográfica acelerada por hardware:
- Rasteriza glifos bajo demanda en un atlas de texturas dinámico en VRAM.
- Anti-aliasing bicúbico para alta legibilidad en pantallas de alta densidad (HiDPI / 4K).
- Procesa integralmente códigos de color vanilla (`§a`, `§c`, etc.) y estilos de negrita (`§l`), cursiva (`§o`) o subrayado (`§n`).
- Dibuja sombras en una única llamada combinada por lotes con desplazamiento de 1 px.

---

## 3. Registro Central (`FontRegistry`)

El registro `FontRegistry` centraliza la carga, almacenamiento en caché y recarga dinámica de fuentes:

### Registrar una Fuente TrueType desde Recursos del Mod
Coloque su archivo `.ttf` u `.otf` en `src/main/resources/assets/mymod/fonts/`:

```java
// En su ClientProxy (durante FMLPreInitializationEvent)
FontRegistry.registerTrueType(
    "roboto_regular",
    new ResourceLocation("mymod", "fonts/roboto-regular.ttf"),
    14.0f // Tamaño en puntos
);

FontRegistry.registerTrueType(
    "jetbrains_mono",
    new ResourceLocation("mymod", "fonts/jetbrains-mono.ttf"),
    12.0f
);
```

### Registrar una Fuente del Sistema Operativo
```java
FontRegistry.registerSystem("arial", "Arial", Font.PLAIN, 14.0f);
```

### Obtener Fuentes Registradas
```java
IFont roboto = FontRegistry.get("roboto_regular");
IFont mono = FontRegistry.getOrDefault("jetbrains_mono", FontRegistry.getDefault());
```

---

## 4. Asignar Fuentes a Componentes

Puede aplicar fuentes a nivel de componente individual o propagarlas a todo un contenedor mediante el método fluido `withFont()`:

```java
// Asignar tipografía a una etiqueta
GuiLabel title = new GuiLabel(10, 10, "Configuración del Sistema", 0xFFFFFFFF)
    .withFont(FontRegistry.get("roboto_regular"));

// Asignar tipografía a un botón
GuiButton actionBtn = new GuiButton(10, 40, 120, 24, "Aplicar", this::onApply)
    .withFont(FontRegistry.get("roboto_regular"));

// Al asignar una fuente a un GuiPanel, todos los hijos la heredan por defecto
headerPanel.withFont(FontRegistry.get("roboto_regular"));
```

---

## 5. Recarga en Caliente de Recursos (`F3 + T`)

`FontRegistry` está conectado al bus de eventos de Minecraft. Cuando el jugador recarga los paquetes de recursos (`F3 + T`), todas las fuentes registradas mediante `ResourceLocation` se liberan y vuelven a cargar desde los nuevos paquetes de recursos activos de forma transparente.

---

## Qué Evitar Hacer (Antipatrones y Fugas de Memoria en Fuentes)

> [!CAUTION]
> **1. NUNCA instanciar `new TrueTypeFont(...)` en cada tick o dentro de `renderSelf()`.**
> Registre siempre las fuentes una sola vez en `FontRegistry`. Crearlas repetidamente colapsará la memoria gráfica por recarga de texturas.

> [!WARNING]
> **2. NUNCA depender exclusivamente de fuentes del sistema operativo (`registerSystem`).**
> Si la fuente no existe en la PC del jugador, se verá mal. Incluya siempre fuentes `.ttf` en su mod.

> [!WARNING]
> **3. NUNCA registrar tamaños de fuente desproporcionados (ej. mayores a 72 pt).**
> Consumirán demasiada VRAM. Para títulos gigantes, registre una fuente normal (ej. 24pt) y use `gfx.scale(2.0f, 2.0f)` al dibujar.

> [!IMPORTANT]
> **4. NUNCA omitir el método `dispose()` si gestiona fuentes manualmente.**
> Si crea fuentes fuera de `FontRegistry`, invoque `font.dispose()` cuando ya no las use.
