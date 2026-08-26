# Base Screens

In Minecraft, interfaces are loaded using `GuiScreen`. X4UI replaces this with its own base screens to manage components automatically.

## GuiBaseScreen

Use this class for any normal menu that **does not** have a real inventory with items (like a pause menu or configuration screen).

### How it works
When creating your screen, X4UI automatically creates a main `GuiPanel` called `rootPanel`, which takes up the entire screen.
You must write your code inside `initComponents()`. That's where you add your buttons, texts, etc., to the `rootPanel`.

## GuiBaseContainer

Use this class when you need to create an interface with inventories (Chests, Machines, etc.). It replaces the classic Minecraft `GuiContainer`.

### How it works
- It automatically draws the `rootPanel` behind the real game items.
- It includes basic support to integrate with mods like JEI (Just Enough Items) through `getJeiRecipeAreas()`.
- It has a small zoom-in animation when the screen opens, for a smoother appearance.

### Key Methods
- `getGuiLeft()` / `getGuiTop()`: Return the inventory's top-left offset. Useful for positioning `GuiSlot` components correctly.
- `getJeiRecipeAreas()`: Returns a list of `Rectangle` areas that JEI can use for recipe overlays.
