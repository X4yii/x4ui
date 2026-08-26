<div align="center">
  <h1>X4UI Framework</h1>
  <p><b>A modern, high-performance, object-oriented GUI framework for Minecraft Forge 1.12.2.</b></p>
  <p>
    <a href="#-english">English</a> • <a href="#-español">Español</a>
  </p>
</div>

---

<h2 id="-english">🇬🇧 English</h2>

### About X4UI
Tired of dealing with hardcoded coordinates, messy `drawScreen` overrides, and infinite bugs in the archaic Vanilla Minecraft 1.12.2 GUI system? 

**X4UI** is a standalone, flexible, and dynamic user interface framework extracted from *Hammers Unbound*. It brings modern web-like development paradigms (like FlexBox layouts, DOM-tree structures, and event bubbling) straight into Forge 1.12.2.

### When should I use X4UI?
You should use X4UI if you are building:
- **Interactive Guidebooks or Manuals:** Need to render formatted text? X4UI includes a native `GuiMarkdown` parser that automatically converts Markdown text into beautiful, structured UI elements (Headers, blockquotes, bullets, and images).
- **RPG Skill Trees & Complex Menus:** Nested panels, buttons, toggles, and sliders are handled automatically.
- **Dynamic Inventories / Custom Forges:** `GuiScrollPanel` provides out-of-the-box OpenGL clipping (`GL_SCISSOR_TEST`), allowing you to create smooth, scrollable lists of ingredients without items rendering outside the box. Nested scroll panels automatically intersect their scissor boxes using `ScissorHelper`.

### Key Features
- **DOM Architecture:** Build your UI by nesting components (`rootPanel.addChild(new GuiButton(...))`).
- **FlexBox Layouts:** `GuiPanel` supports automatic `VERTICAL` and `HORIZONTAL` stacking with gaps and margins. No more manual math for UI placement!
- **High Performance (Dirty Flag System):** Layouts are only recalculated when a component physically changes, saving massive amounts of CPU and preventing FPS drops even in screens with hundreds of elements.
- **Event Bubbling:** Clicks and scroll events accurately respect Z-index. Clicking an overlapping button will trigger the top-most one and stop propagating.
- **Micro-Animations:** Built-in lerp logic for buttery-smooth hover and click transitions. All animations are FPS-independent and scaled by delta-time to ensure the same speed on any hardware.
- **Memory Safety (LRU Caching):** The `FontWidthCache` system ensures complex text parsing (like Markdown) won't cause memory leaks by limiting cached strings using an automatic LRU eviction policy.

### Installation (For Developers)
Add the following to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Replace r1.0b3 with the latest release tag
    compile 'com.github.X4yi:X4UI:r1.0b3'
}
```

### Quick Start
To use X4UI, simply extend `GuiBaseScreen` instead of Vanilla's `GuiScreen`:

```java
public class MyCustomScreen extends GuiBaseScreen {
    
    public MyCustomScreen() {
        super(null, "My Title");
    }

    @Override
    protected void initComponents() {
        // rootPanel is automatically created by GuiBaseScreen
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(5);
        
        GuiLabel titleLabel = new GuiLabel(0, 0, "Welcome to X4UI!");
        GuiButton closeButton = new GuiButton(0, 0, 100, 20, "Close", () -> this.closeScreen());
        
        rootPanel.addChild(titleLabel);
        rootPanel.addChild(closeButton);
    }
}
```

---

<h2 id="-español">🇪🇸 Español</h2>

### Sobre X4UI
¿Cansado de lidiar con coordenadas estáticas (hardcodeadas), sobreescrituras desordenadas de `drawScreen` e infinitos bugs en el arcaico sistema de interfaces de Vanilla Minecraft 1.12.2?

**X4UI** es un framework de interfaces de usuario dinámico, flexible e independiente, extraído del mod *Hammers Unbound*. Trae consigo los paradigmas de desarrollo web modernos (como diseños FlexBox, estructuras en árbol DOM y propagación de eventos) directamente a Forge 1.12.2.

### ¿Cuándo debería usar X4UI?
Deberías considerar usar X4UI si estás creando:
- **Guías o Manuales Interactivos:** ¿Necesitas renderizar texto con formato? X4UI incluye un analizador nativo `GuiMarkdown` que convierte automáticamente texto en elementos de UI estructurados y hermosos (Encabezados, citas, viñetas e imágenes).
- **Árboles de Habilidades RPG y Menús Complejos:** Los paneles anidados, botones, interruptores y deslizadores (sliders) se manejan automáticamente.
- **Inventarios Dinámicos / Forjas Customizadas:** `GuiScrollPanel` proporciona recorte OpenGL nativo (`GL_SCISSOR_TEST`), permitiéndote crear listas de ingredientes desplazables y suaves sin que los ítems se rendericen fuera de la caja. Los paneles anidados calcularán sus áreas de recorte perfectamente gracias a `ScissorHelper`.

### Características Principales
- **Arquitectura DOM:** Construye tu interfaz anidando componentes (`rootPanel.addChild(new GuiButton(...))`).
- **Diseños FlexBox:** `GuiPanel` soporta apilamiento automático `VERTICAL` y `HORIZONTAL` con márgenes y separaciones. ¡Se acabó calcular matemáticas manuales para posicionar interfaces!
- **Alto Rendimiento (Dirty Flag System):** Los diseños sólo se recalcularán matemáticamente cuando un componente cambia físicamente. Esto ahorra inmensas cantidades de procesamiento y previene caídas de FPS, incluso en menús con cientos de elementos.
- **Propagación de Eventos:** Los clics y el desplazamiento (scroll) respetan con precisión el índice Z. Hacer clic en un botón superpuesto activará el que esté por encima y detendrá la propagación a los de abajo.
- **Micro-Animaciones:** Lógica de interpolación (lerp) integrada para transiciones de "hover" y clics fluidas. Las animaciones son independientes de los FPS (escaladas por DeltaTime) asegurando la misma velocidad visual en cualquier PC.
- **Seguridad de Memoria (Caché LRU):** El sistema `FontWidthCache` asegura que el análisis de textos complejos (como Markdown) no cause fugas de memoria al limitar las cadenas cacheadas usando una política de limpieza automática LRU.

### Instalación (Para Desarrolladores)
Añade lo siguiente a tu `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // Reemplaza r1.0b3 con la versión más reciente
    compile 'com.github.X4yi:X4UI:r1.0b3'
}
```

### Inicio Rápido
Para usar X4UI, simplemente extiende `GuiBaseScreen` en lugar del clásico `GuiScreen` de Vanilla:

```java
public class MyCustomScreen extends GuiBaseScreen {
    
    public MyCustomScreen() {
        super(null, "Mi Título");
    }

    @Override
    protected void initComponents() {
        // rootPanel es creado automáticamente por GuiBaseScreen
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(5);
        
        GuiLabel titleLabel = new GuiLabel(0, 0, "¡Bienvenido a X4UI!");
        GuiButton closeButton = new GuiButton(0, 0, 100, 20, "Cerrar", () -> this.closeScreen());
        
        rootPanel.addChild(titleLabel);
        rootPanel.addChild(closeButton);
    }
}
```

---

<div align="center">
  <i>X4UI is open-source and available under the GNU General Public License v3 (GPL-3.0).</i>
</div>
