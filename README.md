<div align="center">
  <h1>X4UI Framework</h1>
  <p><b>Retained-mode UI, reactive state, and server–client framework for Minecraft Forge 1.12.2 &amp; CleanroomMC.</b></p>
  <p>
    <a href="#-english">English</a> &bull; <a href="#-espanol">Espa&ntilde;ol</a>
  </p>
</div>

---

<h2 id="-english">English</h2>

### What X4UI Solves

Vanilla `GuiScreen` and `GuiContainer` rely on immediate-mode drawing inside `drawScreen()`, manual pixel coordinates, raw OpenGL state mutation, and tight coupling between rendering and game logic. Interfaces built directly on vanilla primitives break under `GUI Scale` 3–4, leak listeners or native resources, and fail across **Forge 1.12.2 (Java 8 + LWJGL 2)** and **CleanroomMC (Java 25 + LWJGL 3)**.

X4UI (`r1.0b7`) replaces ad-hoc GUI code with a deterministic, retained-mode UI framework engineered around strict runtime portability, reactive state management, and decoupled server–client communication.

### Core Paradigm & Design Decisions

- **Retained-Mode Component Tree:** Every UI is a hierarchy of `GuiComponent` nodes rooted in `rootPanel`. Layout and render-order sorting execute lazily via dirty flags (`requestLayout()`, `requestRender()`), rather than recalculating geometry on every frame.
- **API / Implementation Isolation:** Public interfaces (`IGraphics`, `IFont`, `IVideoDecoder`, `IAudioDecoder`) live in `com.x4yi.x4ui.api.*`, while OpenGL, TrueType rasterization, and FFmpeg/JCodec backends reside in `com.x4yi.x4ui.impl.*`.
- **Dual-Runtime Abstraction (`PlatformBridge`):** Packaged as a **Multi-Release JAR** (`Java 8` base + `META-INF/versions/21/`) so `Cleaner`, I/O executors, `KeyCodes`, `UIInput`, and `IGraphics` run transparently on both Forge 1.12.2 (Java 8) and CleanroomMC (Java 25).
- **Reactive State with Lifecycle Cleanup:** `State<T>` and `UIStateBus` (`Scope.SCREEN`, `Scope.SESSION`, `Scope.GLOBAL`) decouple views from data sources; `component.bindState(...)` unsubscribes automatically on `destroy()`.
- **Server-Optional Networking:** `UINetwork` provides remote screen opening (`ScreenRegistry`), batched state sync (`PacketStateBatch`), request–response RPC (`PacketGuiRpc`), and multi-player channels (`UIChannels`) protected by `ActionGuard` and per-action rate limiting, while allowing client-only operation on vanilla servers.

### Architecture & Systems Overview

| System | Key Classes | Responsibility |
| :--- | :--- | :--- |
| **Component Tree & Screens** | `GuiBuilder`, `GuiBaseScreen`, `GuiBaseContainer`, `ScreenHost`, `GuiComponent`, `GuiPanel`, `GuiUnionPanel` | Deterministic lifecycle, fluent `GuiBuilder.screen`/`modal`, cast-free `add(...)` & `.of(...)` factories, z-layer sorting, seamless docked wings (`attachSidePanel`) |
| **Responsive Layout** | `FlexLayout`, `FlexDirection`, `FlexAlign`, `GuiGridPanel`, `GuiScrollPanel` | `flexGrow`/`flexShrink` water-filling, `min`/`max` bounds, `autoColumns` grids, scrollbar reservation |
| **Graphics Abstraction** | `IGraphics`, `GLGraphics`, `CornerRadii`, `BorderSides` | Matrix/alpha/tint/scissor stack, per-corner radii, per-side borders, `clearDepthBuffer()`, BGRA textures |
| **Vector Typography** | `IFont`, `TrueTypeFont`, `VanillaFont`, `FontRegistry` | TrueType/OpenType glyph atlas, vanilla `§` formatting, LRU width cache, `F3+T` reload |
| **Reactive State** | `State<T>`, `UIStateBus` | Thread-safe observable values with listener exception isolation, `SCREEN`/`SESSION`/`GLOBAL` scopes, type-checked lookups, and namespaces |
| **Networking & Security** | `UINetwork`, `ScreenRegistry`, `UIChannels`, `ActionGuard` | Handshake, remote screens, RPC, Pub/Sub channels, distance/OP guards, rate-limiting & disconnect cleanup |
| **Media Pipeline** | `MediaPipeline`, `MediaManager`, `SecurityUtils`, `GuiVideo`, `GuiImage` | Async downloads, SSRF/redirect/path-traversal guards, OS `ffmpeg` discovery (Linux/macOS/Win) + JCodec fallback |
| **Runtime Compatibility** | `PlatformBridge`, `KeyCodes`, `UIInput` | Java 8/25 Multi-Release support, LWJGL 2/3 key & wheel normalization, `displayWidth == 0` guard |
| **Overlays & Integration** | `GuiOverlayManager`, `OverlayDockManager`, `IGuiOverlay`, `JeiPlugin` | Non-intrusive screen injection, container anchoring, JEI exclusion areas |

### Installation

Requirements:
- **Runtime:** Minecraft Forge `1.12.2` (`14.23.5.2847+`, Java 8) or CleanroomMC (Java 25)
- **Consumer Build Target:** `sourceCompatibility = 1.8`, `targetCompatibility = 1.8`

Add the dependency from **Modrinth Maven** (recommended) or download the JARs from [GitHub Releases](https://github.com/X4yii/x4ui/releases):

```gradle
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    // RetroFuturaGradle (recommended)
    implementation rfg.deobf("maven.modrinth:x4ui:r1.0b7")
}
```

### Quick Start

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import net.minecraft.client.gui.GuiScreen;

public class ExampleScreen extends GuiBaseScreen {

    public ExampleScreen(GuiScreen parent) {
        super(parent, "Example");
    }

    @Override
    protected void initComponents() {
        GuiPanel window = createCenteredWindow(360, 220);

        GuiPanel header = GuiPanel.row(8);
        header.add(GuiLabel.of("Control Panel", 0xFFFFFFFF));
        header.add(GuiPanel.spacer());
        header.add(GuiButton.of(20, 20, "X", this::closeScreen));

        GuiPanel body = GuiPanel.card(10, 6);
        body.add(GuiButton.of(0, 22, "Execute Action", () -> {
            sendActionOrLocal("mymod:ping", null, () -> {});
        }).withPercentWidth(1.0f).withVariant(GuiButton.Variant.PRIMARY));

        window.add(header);
        window.add(body);
        rootPanel.add(window);
    }
}
```

### Documentation

> *Note: Only the X4UI documentation was drafted with AI (Gemini); all technical specifications and examples were reviewed and approved through human testing (X4yi).*

| Document | Scope |
| :--- | :--- |
| [00 - Getting Started](Docs/en-us/00-getting-started.md) | Requirements, installation, package map, `GuiBaseScreen`, `GuiBaseContainer`, fluent API |
| [01 - Core Concepts](Docs/en-us/01-core-concepts.md) | Retained-mode paradigm, component tree, lifecycle, `State<T>`, `UIStateBus`, themes, dirty flags |
| [02 - Remote Assets](Docs/en-us/02-remote-assets.md) | `MediaPipeline`, OS `ffmpeg` discovery, JCodec fallback, `SecurityUtils`, supported formats |
| [03 - Networking & Sync](Docs/en-us/03-networking-and-sync.md) | Sidedness, `UINetwork`, `ScreenRegistry`, `PacketGuiRpc`, `UIChannels`, `ActionGuard`, `Container` sync |
| [04 - Responsive Layout](Docs/en-us/04-responsive-layout-and-scaling.md) | `ScaledResolution`, GUI Scale 1x–4x standard, `FlexLayout`, `FlexAlign`, `GuiScrollPanel` |
| [05 - Graphics & Rendering](Docs/en-us/05-graphics-and-rendering.md) | `IGraphics` abstraction, state stack, scissor clipping, drawing primitives, depth & BGRA textures |
| [06 - Typography & Fonts](Docs/en-us/06-typography-and-fonts.md) | `IFont`, `TrueTypeFont`, `VanillaFont`, `FontRegistry`, `F3+T` hot-reload, VRAM discipline |
| [07 - GUI Overlays & Injection](Docs/en-us/07-gui-overlays-and-injection.md) | `IGuiOverlay`, `GuiOverlayManager`, `OverlayDockManager`, `ContainerAnchor` |
| [08 - Integrations & Best Practices](Docs/en-us/08-integrations-and-best-practices.md) | JEI integration, naming conventions, decoupled views, memory safety, anti-pattern reference |
| [09 - Forge & CleanroomMC Compatibility](Docs/en-us/09-forge-cleanroom-compatibility.md) | Java 8 ↔ Java 25 portability, `PlatformBridge`, `KeyCodes`, `UIInput`, `reobfJar` rules |
| [Buttons & Inputs](Docs/en-us/components/buttons-and-inputs.md) | `GuiButton`, `GuiTextInput`, `GuiToggle`, `GuiSliderInt` / `Float` / `Double` |
| [Containers & Panels](Docs/en-us/components/containers-and-panels.md) | `GuiPanel`, `GuiUnionPanel`, `GuiGridPanel`, `GuiScrollPanel`, `GuiSlot` |
| [Multimedia](Docs/en-us/components/multimedia.md) | `GuiImage` (URL, `ResourceLocation`, 9-slice), `GuiVideo` (async player & error handling), `GuiAudio`, `GuiSprite` |
| [Advanced Components](Docs/en-us/components/advanced-components.md) | `GuiVirtualList`, `GuiDropdown`, `GuiMarkdown`, `GuiModelRender`, `GuiToastManager` |

### License

X4UI is licensed under the GNU General Public License v3 (GPL-3.0).

---

<h2 id="-espanol">Espa&ntilde;ol</h2>

### Qu&eacute; Soluciona X4UI

El sistema nativo de `GuiScreen` y `GuiContainer` depende de renderizado en modo inmediato dentro de `drawScreen()`, coordenadas absolutas en píxeles, mutación directa de OpenGL y acoplamiento entre vista y lógica. Las interfaces construidas directamente sobre vanilla se rompen en `GUI Scale` 3–4, filtran listeners o memoria nativa y fallan entre **Forge 1.12.2 (Java 8 + LWJGL 2)** y **CleanroomMC (Java 25 + LWJGL 3)**.

X4UI (`r1.0b7`) reemplaza el código ad-hoc por un framework de modo retenido diseñado con portabilidad dual de runtime, estado reactivo con limpieza automática y comunicación servidor–cliente desacoplada.

### Paradigma y Decisiones de Dise&ntilde;o

- **Árbol de Componentes en Modo Retenido:** Toda interfaz es una jerarquía de nodos `GuiComponent` bajo `rootPanel`. El cálculo de diseño y el ordenamiento de capas se ejecutan bajo demanda mediante banderas sucias (`requestLayout()`, `requestRender()`), evitando recálculos por fotograma.
- **Separación `api` / `impl`:** Las interfaces públicas (`IGraphics`, `IFont`, `IVideoDecoder`, `IAudioDecoder`) residen en `com.x4yi.x4ui.api.*`, mientras que OpenGL, rasterización TrueType y backends FFmpeg/JCodec se aíslan en `com.x4yi.x4ui.impl.*`.
- **Abstracción Dual de Runtime (`PlatformBridge`):** Empaquetado como **Multi-Release JAR** (`Java 8` base + `META-INF/versions/21/`) para que `Cleaner`, executors de I/O, `KeyCodes`, `UIInput` e `IGraphics` operen de forma transparente en Forge 1.12.2 (Java 8) y CleanroomMC (Java 25).
- **Estado Reactivo Ligado al Ciclo de Vida:** `State<T>` y `UIStateBus` (`Scope.SCREEN`, `Scope.SESSION`, `Scope.GLOBAL`) desacoplan la vista de los datos; `component.bindState(...)` desuscribe los listeners automáticamente al ejecutar `destroy()`.
- **Red Server-Optional:** `UINetwork` ofrece apertura remota de pantallas (`ScreenRegistry`), lotes de estado (`PacketStateBatch`), RPC request–response (`PacketGuiRpc`) y canales multi-jugador (`UIChannels`) protegidos con `ActionGuard` y rate-limiting, sin impedir la ejecución client-only en servidores vanilla.

### Resumen de Arquitectura y Sistemas

| Sistema | Clases Clave | Responsabilidad |
| :--- | :--- | :--- |
| **Árbol y Pantallas** | `GuiBuilder`, `GuiBaseScreen`, `GuiBaseContainer`, `ScreenHost`, `GuiComponent`, `GuiPanel`, `GuiUnionPanel` | Ciclo de vida determinista, fachada fluida `GuiBuilder.screen`/`modal`, `add(...)` y factories `.of(...)` sin casts, orden Z, alas acopladas (`attachSidePanel`) |
| **Diseño Responsivo** | `FlexLayout`, `FlexDirection`, `FlexAlign`, `GuiGridPanel`, `GuiScrollPanel` | `flexGrow`/`flexShrink` water-filling, cotas `min`/`max`, grillas `autoColumns`, reserva de scrollbar |
| **Abstracción Gráfica** | `IGraphics`, `GLGraphics`, `CornerRadii`, `BorderSides` | Pila de matrices/alfa/tinte/scissor, radios por esquina, bordes por lado, `clearDepthBuffer()`, texturas BGRA |
| **Tipografía Vectorial** | `IFont`, `TrueTypeFont`, `VanillaFont`, `FontRegistry` | Atlas TrueType/OpenType, códigos `§`, caché LRU de anchos, recarga `F3+T` |
| **Estado Reactivo** | `State<T>`, `UIStateBus` | Contenedores thread-safe con aislamiento de excepciones, alcances `SCREEN`/`SESSION`/`GLOBAL`, validación de tipo y namespaces |
| **Red y Seguridad** | `UINetwork`, `ScreenRegistry`, `UIChannels`, `ActionGuard` | Handshake, pantallas remotas, RPC, canales Pub/Sub, guardas, rate-limiting y limpieza al desconectar |
| **Pipeline Multimedia** | `MediaPipeline`, `MediaManager`, `SecurityUtils`, `GuiVideo`, `GuiImage` | Descargas asíncronas, protección SSRF/redirecciones/path-traversal, `ffmpeg` en OS + fallback JCodec |
| **Compatibilidad Dual** | `PlatformBridge`, `KeyCodes`, `UIInput` | Soporte Java 8/25 Multi-Release, normalización LWJGL 2/3, guarda `displayWidth == 0` |
| **Overlays e Inyección** | `GuiOverlayManager`, `OverlayDockManager`, `IGuiOverlay`, `JeiPlugin` | Inyección no intrusiva, anclaje a contenedores, áreas de exclusión JEI |

### Instalaci&oacute;n

Requisitos:
- **Runtime:** Minecraft Forge `1.12.2` (`14.23.5.2847+`, Java 8) o CleanroomMC (Java 25)
- **Target de compilación para mods consumidores:** `sourceCompatibility = 1.8`, `targetCompatibility = 1.8`

Agrega la dependencia desde **Modrinth Maven** (recomendado) o descarga los JARs desde [GitHub Releases](https://github.com/X4yii/x4ui/releases):

```gradle
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
    }
}

dependencies {
    // RetroFuturaGradle (recomendado)
    implementation rfg.deobf("maven.modrinth:x4ui:r1.0b7")
}
```

### Inicio R&aacute;pido

```java
package com.example.mymod.client.gui;

import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import net.minecraft.client.gui.GuiScreen;

public class ExampleScreen extends GuiBaseScreen {

    public ExampleScreen(GuiScreen parent) {
        super(parent, "Ejemplo");
    }

    @Override
    protected void initComponents() {
        GuiPanel window = createCenteredWindow(360, 220);

        GuiPanel header = GuiPanel.row(8);
        header.add(GuiLabel.of("Panel de Control", 0xFFFFFFFF));
        header.add(GuiPanel.spacer());
        header.add(GuiButton.of(20, 20, "X", this::closeScreen));

        GuiPanel body = GuiPanel.card(10, 6);
        body.add(GuiButton.of(0, 22, "Ejecutar Acción", () -> {
            sendActionOrLocal("mymod:ping", null, () -> {});
        }).withPercentWidth(1.0f).withVariant(GuiButton.Variant.PRIMARY));

        window.add(header);
        window.add(body);
        rootPanel.add(window);
    }
}
```

### Documentaci&oacute;n

> *Nota: Únicamente la documentación de X4UI fue redactada con IA (Gemini), siendo revisada y aprobada mediante pruebas humanas (X4yi).*

| Documento | Alcance |
| :--- | :--- |
| [00 - Primeros Pasos](Docs/es-latam/00-getting-started.md) | Requisitos, instalación, mapa de paquetes, `GuiBaseScreen`, `GuiBaseContainer`, API fluida |
| [01 - Conceptos Fundamentales](Docs/es-latam/01-core-concepts.md) | Paradigma retenido, árbol de componentes, ciclo de vida, `State<T>`, `UIStateBus`, temas |
| [02 - Recursos Remotos](Docs/es-latam/02-remote-assets.md) | `MediaPipeline`, descubrimiento OS de `ffmpeg`, fallback JCodec, `SecurityUtils`, formatos |
| [03 - Red y Sincronización](Docs/es-latam/03-networking-and-sync.md) | Sidedness, `UINetwork`, `ScreenRegistry`, `PacketGuiRpc`, `UIChannels`, `ActionGuard`, `Container` |
| [04 - Diseño Responsivo](Docs/es-latam/04-responsive-layout-and-scaling.md) | `ScaledResolution`, estándar GUI Scale 1x–4x, `FlexLayout`, `FlexAlign`, `GuiScrollPanel` |
| [05 - Gráficos y Renderizado](Docs/es-latam/05-graphics-and-rendering.md) | Abstracción `IGraphics`, pila de estados, scissor, primitivas, profundidad y texturas BGRA |
| [06 - Tipografía y Fuentes](Docs/es-latam/06-typography-and-fonts.md) | `IFont`, `TrueTypeFont`, `VanillaFont`, `FontRegistry`, recarga `F3+T`, disciplina VRAM |
| [07 - Overlays e Inyección](Docs/es-latam/07-gui-overlays-and-injection.md) | `IGuiOverlay`, `GuiOverlayManager`, `OverlayDockManager`, `ContainerAnchor` |
| [08 - Integraciones y Buenas Prácticas](Docs/es-latam/08-integrations-and-best-practices.md) | Integración JEI, convenciones de nombres, vistas desacopladas, memoria y antipatrones |
| [09 - Compatibilidad Forge y CleanroomMC](Docs/es-latam/09-forge-cleanroom-compatibility.md) | Portabilidad Java 8 ↔ Java 25, `PlatformBridge`, `KeyCodes`, `UIInput`, reglas `reobfJar` |
| [Botones y Entradas](Docs/es-latam/components/buttons-and-inputs.md) | `GuiButton`, `GuiTextInput`, `GuiToggle`, `GuiSliderInt` / `Float` / `Double` |
| [Contenedores y Paneles](Docs/es-latam/components/containers-and-panels.md) | `GuiPanel`, `GuiUnionPanel`, `GuiGridPanel`, `GuiScrollPanel`, `GuiSlot` |
| [Multimedia](Docs/es-latam/components/multimedia.md) | `GuiImage` (URL, `ResourceLocation`, 9-slice), `GuiVideo` (errores/carga), `GuiAudio`, `GuiSprite` |
| [Componentes Avanzados](Docs/es-latam/components/advanced-components.md) | `GuiVirtualList`, `GuiDropdown`, `GuiMarkdown`, `GuiModelRender`, `GuiToastManager` |

### Licencia

X4UI está licenciado bajo GNU General Public License v3 (GPL-3.0).
