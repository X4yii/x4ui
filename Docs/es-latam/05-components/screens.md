# Base Screens (Pantallas Base)

En Minecraft las interfaces se cargan usando `GuiScreen`. X4UI reemplaza esto con sus propias pantallas base para gestionar los componentes.

## GuiBaseScreen

Usa esta clase para cualquier menú normal que **no** tenga un inventario real con items (como un menú de pausa o de configuraciones).

### Cómo funciona
Al crear tu pantalla, X4UI crea automáticamente un `GuiPanel` principal llamado `rootPanel`, que ocupa toda la pantalla.
Debes escribir tu código dentro de `initComponents()`. Ahí es donde añades tus botones, textos, etc., al `rootPanel`.

## GuiBaseContainer

Usa esta clase cuando necesites crear una interfaz con inventarios (Cofres, Máquinas, etc.). Reemplaza al clásico `GuiContainer` de Minecraft.

### Cómo funciona
- Dibuja automáticamente el `rootPanel` detrás de los items reales del juego.
- Incluye soporte básico para integrarse con mods como JEI (Just Enough Items) a través de `getJeiRecipeAreas()`.
- Tiene una pequeña animación de zoom al abrir la pantalla para una apariencia más fluida.

### Métodos Clave
- `getGuiLeft()` / `getGuiTop()`: Devuelven el offset de la esquina superior izquierda del inventario. Útil para posicionar componentes `GuiSlot` correctamente.
- `getJeiRecipeAreas()`: Devuelve una lista de áreas `Rectangle` que JEI puede usar para superponer recetas.
