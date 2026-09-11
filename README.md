<div align="center">
  <h1>X4UI Framework</h1>
  <p><b>A modern, high-performance GUI framework for Minecraft Forge 1.12.2.</b></p>
  <p>
    <a href="#-english">English</a> &bull; <a href="#-espanol">Espa&ntilde;ol</a>
  </p>
</div>

---

<h2 id="-english">English</h2>

### What is X4UI?

X4UI is a retained-mode GUI framework for Minecraft Forge 1.12.2. It replaces vanilla's immediate-mode `GuiScreen` rendering with a component tree architecture, flexbox layouts, event bubbling, reactive state binding, and theme support.

### When to Use X4UI

- **Interactive guidebooks or manuals**: `GuiMarkdown` renders formatted text with headings, bold, italic, code, links, blockquotes, bullet lists, images, and separators.
- **RPG skill trees and complex menus**: Nested panels, buttons, toggles, sliders, and dropdowns with automatic flexbox layout.
- **Media-rich interfaces**: Embed MP4 video playback (`GuiVideo`), static images from URLs (`GuiImage`), and 9-slice scalable backgrounds (`GuiSprite`).
- **Custom inventory screens**: Wrap Minecraft containers with `GuiBaseContainer` and `GuiSlot` for flex-layout inventory grids.

### Key Features

| Feature | Description |
|---------|-------------|
| **Component Tree** | Retained-mode DOM-like hierarchy. Components are added via `addChild()` and automatically managed. |
| **Flexbox Layout** | `GuiPanel` with `VERTICAL`, `HORIZONTAL`, or `ABSOLUTE` direction. Supports gaps, padding, margin, and wrapping. |
| **Event Bubbling** | Mouse and keyboard events propagate through the tree. Z-index layer ordering determines which component receives the event first. |
| **Reactive State** | `State<T>` provides observable data containers. Components bind to state via `bindState()` and update automatically. |
| **Theme System** | `ITheme` interface for full visual customization. Apply per-screen or per-component. Built-in `DefaultTheme` dark theme. |
| **Dirty Flag Optimization** | Layout and render sorting only execute when marked dirty. Avoids unnecessary recalculations. |
| **Video Playback** | Zero-Copy Dual-decoder architecture: DirectMemoryVideoDecoder (native NIO) with JCodec (pure-Java) fallback. Supports up to 1280x720 natively without GC overhead. |
| **Media Service** | Centralized API for pre-loading and asynchronous streaming of multimedia assets via `X4UI.getMediaService()`. |
| **Remote Assets** | `RemoteResourceManager` downloads and caches media from HTTP/HTTPS URLs with SHA-256 file hashing. |
| **Fluent Builder** | `GuiBuilder<T>` provides chainable API for declarative component construction. |
| **Memory Safety** | `FontWidthCache` uses per-mod LRU eviction (10,000 entries) to prevent memory leaks in text-heavy UIs. |
| **Nested Scissor** | `ScissorHelper` manages nested OpenGL scissor regions for scroll clipping. |
| **Animation Engine** | `GuiAnimator` with LINEAR, EASE_IN, EASE_OUT, and EASE_IN_OUT easing functions. |

### Installation

Add to `build.gradle`:

```gradle
repositories {
    maven { url "https://maven.x4yi.com/" }
}

dependencies {
    deobfCompile "com.x4yi:X4UI-1.12.2:1.0b5"
}
```

### Quick Start

```java
import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import net.minecraft.client.gui.GuiScreen;

public class MyScreen extends GuiBaseScreen {

    public MyScreen(GuiScreen parent) {
        super(parent, "My Screen");
    }

    @Override
    protected void initComponents() {
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(5);

        rootPanel.addChild(new GuiLabel(0, 0, "Hello X4UI", 0xFFFFFFFF).setCentered(true));
        rootPanel.addChild(new GuiButton(0, 0, 120, 20, "Click Me", () -> System.out.println("Clicked!")));
        rootPanel.addChild(new GuiButton(0, 0, 120, 20, "Close", () -> this.closeScreen()));
    }
}
```

Open the screen:

```java
Minecraft.getMinecraft().displayGuiScreen(new MyScreen(Minecraft.getMinecraft().currentScreen));
```

### Documentation

| Document | Description |
|----------|-------------|
| [00 - Getting Started](Docs/en-us/00-getting-started.md) | Installation, creating screens, fluent API, theme setup, pitfalls |
| [01 - Core Concepts](Docs/en-us/01-core-concepts.md) | Component tree, lifecycle, flexbox, events, State, UIStateBus, animation |
| [02 - Remote Assets](Docs/en-us/02-remote-assets.md) | RemoteResourceManager, URL loading, cache management, limitations |
| [03 - Networking & Sync](Docs/en-us/03-networking-and-sync.md) | Sided proxies, server-optional handshake, container sync, IGuiActionSender |
| [04 - Responsive Layout](Docs/en-us/04-responsive-layout-and-scaling.md) | GUI Scale 1x-4x adaptation, percentage sizing, adaptive grid, scrollbars |
| [05 - Graphics & Rendering](Docs/en-us/05-graphics-and-rendering.md) | IGraphics abstraction, state stack, primitives, CCW vertex winding |
| [06 - Vector Typography](Docs/en-us/06-typography-and-fonts.md) | IFont, TrueTypeFont rasterization, FontRegistry, F3+T hot-reloading |
| [07 - GUI Overlays & Injection](Docs/en-us/07-gui-overlays-and-injection.md) | Non-intrusive screen injection, GuiOverlayManager, OverlayDockManager |
| [08 - Integrations & Best Practices](Docs/en-us/08-integrations-and-best-practices.md) | JEI support, UIStateBus, ThemeRegistry, definitive what to avoid table |
| [Buttons & Inputs](Docs/en-us/components/buttons-and-inputs.md) | GuiButton, GuiTextInput, GuiToggle, typed modular GuiSlider |
| [Containers & Panels](Docs/en-us/components/containers-and-panels.md) | GuiPanel, GuiScrollPanel with sticky sessions, GuiVirtualList, GuiSlot |
| [Multimedia](Docs/en-us/components/multimedia.md) | GuiImage (9-slice), animated GuiSprite engine, GuiVideo, GuiAudio |
| [Advanced Components](Docs/en-us/components/advanced-components.md) | GuiMarkdown, GuiModelRender with Z-buffer, scrollable GuiDropdown, Tooltips |

### License

X4UI is licensed under the GNU General Public License v3 (GPL-3.0).

---

<h2 id="-espanol">Espa&ntilde;ol</h2>

### &iquest;Qu&eacute; es X4UI?

X4UI es un framework de interfaz de usuario de modo retenido para Minecraft Forge 1.12.2. Reemplaza el renderizado de modo inmediato de `GuiScreen` de vanilla con una arquitectura de árbol de componentes, diseños flexbox, propagación de eventos, enlace de estado reactivo y soporte de temas.

### &iquest;Cuándo usar X4UI?

- **Guías o manuales interactivos**: `GuiMarkdown` renderiza texto formateado con encabezados, negrita, cursiva, código, enlaces, citas, listas, imágenes y separadores.
- **Árboles de habilidades RPG y menús complejos**: Paneles anidados, botones, interruptores, sliders y dropdowns con layout flexbox automático.
- **Interfaces ricas en medios**: Incrusta reproducción de video MP4 (`GuiVideo`), imágenes estáticas desde URLs (`GuiImage`) y fondos escalables 9-slice (`GuiSprite`).
- **Pantallas de inventario personalizadas**: Envuelve contenedores de Minecraft con `GuiBaseContainer` y `GuiSlot` para grids de inventario con flex layout.

### Características Principales

| Característica | Descripción |
|----------------|-------------|
| **Árbol de Componentes** | Jerarquía retenida tipo DOM. Los componentes se agregan con `addChild()` y se gestionan automáticamente. |
| **Flexbox Layout** | `GuiPanel` con dirección `VERTICAL`, `HORIZONTAL` o `ABSOLUTE`. Soporta gaps, padding, margin y wrapping. |
| **Propagación de Eventos** | Eventos de mouse y teclado se propagan por el árbol. El orden de capas Z determina qué componente recibe el evento primero. |
| **Estado Reactivo** | `State<T>` proporciona contenedores de datos observables. Los componentes se enlazan al estado con `bindState()` y se actualizan automáticamente. |
| **Sistema de Temas** | Interfaz `ITheme` para personalización visual completa. Se aplica por pantalla o por componente. `DefaultTheme` oscuro incluido. |
| **Optimización con Dirty Flags** | El layout y el orden de render solo se ejecutan cuando se marca como sucio. Evita recálculos innecesarios. |
| **Reproducción de Video** | Arquitectura de doble decodificador Zero-Copy: DirectMemoryVideoDecoder (NIO nativo) con respaldo JCodec (pure-Java). Soporta hasta 1280x720 nativo sin penalizar el GC. |
| **Media Service** | API centralizada para pre-carga y streaming asíncrono de assets multimedia vía `X4UI.getMediaService()`. |
| **Assets Remotos** | `RemoteResourceManager` descarga y almacena en caché medios desde URLs HTTP/HTTPS con hash SHA-256. |
| **Builder Fluente** | `GuiBuilder<T>` proporciona API encadenable para construcción declarativa de componentes. |
| **Seguridad de Memoria** | `FontWidthCache` usa evicción LRU por mod (10,000 entradas) para prevenir fugas de memoria en UIs con mucho texto. |
| **Scissor Anidado** | `ScissorHelper` gestiona regiones OpenGL scissor anidadas para clipping de scroll. |
| **Motor de Animación** | `GuiAnimator` con funciones de easing LINEAR, EASE_IN, EASE_OUT y EASE_IN_OUT. |

### Instalación

Agregar a `build.gradle`:

```gradle
repositories {
    maven { url "https://maven.x4yi.com/" }
}

dependencies {
    deobfCompile "com.x4yi:X4UI-1.12.2:1.0b5"
}
```

### Inicio Rápido

```java
import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import net.minecraft.client.gui.GuiScreen;

public class MyScreen extends GuiBaseScreen {

    public MyScreen(GuiScreen parent) {
        super(parent, "Mi Pantalla");
    }

    @Override
    protected void initComponents() {
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setGap(5);

        rootPanel.addChild(new GuiLabel(0, 0, "Hola X4UI", 0xFFFFFFFF).setCentered(true));
        rootPanel.addChild(new GuiButton(0, 0, 120, 20, "Clic Aquí", () -> System.out.println("¡Clic!")));
        rootPanel.addChild(new GuiButton(0, 0, 120, 20, "Cerrar", () -> this.closeScreen()));
    }
}
```

Abrir la pantalla:

```java
Minecraft.getMinecraft().displayGuiScreen(new MyScreen(Minecraft.getMinecraft().currentScreen));
```

### Documentación

| Documento | Descripción |
|-----------|-------------|
| [00 - Primeros Pasos](Docs/es-latam/00-getting-started.md) | Instalación, creación de pantallas, API fluida, temas, qué evitar |
| [01 - Conceptos Fundamentales](Docs/es-latam/01-core-concepts.md) | Árbol de componentes, ciclo de vida, flexbox, eventos, State, UIStateBus |
| [02 - Recursos Remotos](Docs/es-latam/02-remote-assets.md) | RemoteResourceManager, carga asíncrona por URL, gestión de caché |
| [03 - Red y Sincronización](Docs/es-latam/03-networking-and-sync.md) | Proxies sided, conexión server-optional, sincronización con contenedor |
| [04 - Diseño Responsivo](Docs/es-latam/04-responsive-layout-and-scaling.md) | Adaptación a GUI Scale 1x-4x, porcentajes, cuadrícula y scrollbars |
| [05 - Abstracción Gráfica](Docs/es-latam/05-graphics-and-rendering.md) | Capa IGraphics, pila de estados, primitivas, bobinado de vértices CCW |
| [06 - Tipografía Vectorial](Docs/es-latam/06-typography-and-fonts.md) | IFont, rasterizador TrueTypeFont, FontRegistry, recarga F3+T |
| [07 - Superposiciones en GUI](Docs/es-latam/07-gui-overlays-and-injection.md) | Inyección no intrusiva en pantallas, GuiOverlayManager, acoplamiento |
| [08 - Integraciones y Mejores Prácticas](Docs/es-latam/08-integrations-and-best-practices.md) | JEI nativo, UIStateBus, ThemeRegistry, tabla definitiva de qué evitar |
| [Botones e Inputs](Docs/es-latam/components/buttons-and-inputs.md) | GuiButton, GuiTextInput, GuiToggle, GuiSlider tipado y modular |
| [Contenedores y Paneles](Docs/es-latam/components/containers-and-panels.md) | GuiPanel, GuiScrollPanel con scroll sticky, GuiVirtualList, GuiSlot |
| [Multimedia](Docs/es-latam/components/multimedia.md) | GuiImage (9-slice), motor de GuiSprite animado, GuiVideo, GuiAudio |
| [Componentes Avanzados](Docs/es-latam/components/advanced-components.md) | GuiMarkdown, GuiModelRender con Z-buffer, GuiDropdown con scroll, Tooltips |

### Licencia

X4UI está licenciado bajo la GNU General Public License v3 (GPL-3.0).

---

<div align="center">
  <i>X4UI is open-source and available under the GNU General Public License v3 (GPL-3.0).</i>
</div>
