# 09 - Compatibilidad Forge 1.12.2 ↔ CleanroomMC y Abstracciones

`x4ui` (`r1.0b6`) soporta oficialmente **Forge 1.12.2 (Java 8 + LWJGL 2)** y **CleanroomMC (Java 25 + LWJGL 3 / `lwjgly`)**.  
Para que un mod consumidor funcione sin fallos en ambos entornos, utiliza las abstracciones de `x4ui` en lugar de depender de APIs de bajo nivel de la JVM, LWJGL o librerías cuya versión cambia entre runtimes.

---

## 1. Matriz de Abstracciones de `x4ui`

| Qué necesitas hacer | Usa esta API de `x4ui` | Evita usar directamente |
| :--- | :--- | :--- |
| Dibujar rects, bordes, clipping, texto, texturas, depth | `IGraphics` (`GLGraphics`) | `org.lwjgl.opengl.GL11` / `GL12`, `GL11.glScissor` |
| Teclas, modificadores (`Ctrl`/`Shift`/`Alt`), scroll y mouse | `KeyCodes` + `UIInput` (`normalizeWheelDelta`, `safeMouseX/Y`) | `org.lwjgl.input.Keyboard`, `org.lwjgl.input.Mouse` |
| Buffers directos y executors I/O | `PlatformBridge.cleanDirectBuffer`, `PlatformBridge.newIoExecutor` | `sun.misc.Unsafe`, `DirectBuffer.cleaner()`, threads crudos |
| Detectar entorno en runtime | `PlatformBridge.isCleanroom()`, `isLwjgl3()`, `getJavaFeatureVersion()` | `Class.forName(...)` ad-hoc |
| Red servidor–cliente y pantallas remotas | `UINetwork`, `ScreenRegistry`, `UIChannels`, `UIStateBus`, `ActionGuard` | Canales Netty crudos o paquetes manuales |
| Descargas y rutas seguras de recursos | `SecurityUtils` (`validateHttpUrl`, `resolveSafeChild`) | `new URL(...).openStream()` sin validación SSRF/path |

---

## 2. Reglas Críticas para Mods Consumidores

### A. Compilar siempre con target Java 8
- Configura `sourceCompatibility = 1.8` y `targetCompatibility = 1.8` en tu `build.gradle`.
- Compilar bytecode para Java 25 romperá en Forge 1.12.2 (Java 8) con `UnsupportedClassVersionError`.
- Para I/O asíncrono o liberación de memoria nativa, delega en `PlatformBridge` (que internamente usa el JAR Multi-Release en Java 25 y un fallback seguro en Java 8).

### B. Evitar APIs eliminadas o incompatibles en Guava / Netty
- Forge 1.12.2 usa **Guava 21** y **Netty 4.1**, mientras que CleanroomMC usa **Guava 33.x** y **Netty 4.2.x**.
- Evita métodos eliminados en Guava moderno (por ejemplo, `Futures.addCallback(future, callback)` de 2 argumentos) y métodos exclusivos de Guava 33 / Netty 4.2. Prefiere la biblioteca estándar de Java 8 y `NBTTagCompound` / `ByteBufUtils`.

### C. No usar `org.lwjgl.input.*` ni dividir por `mc.displayWidth` sin guarda
- En LWJGL 3 / GLFW (CleanroomMC), los deltas de la rueda del mouse pueden ser distintos a `±120` y `mc.displayWidth` / `displayHeight` pueden ser `0` al minimizar la ventana.
- Usa `KeyCodes` (ej. `input.setSubmitKey(KeyCodes.ENTER)`) y `UIInput` (`UIInput.getEventDWheel()`, `UIInput.safeMouseX(...)`).

### D. Distribuir y probar siempre el JAR de `reobfJar`
- Nunca pruebes ni publiques `*-dev.jar` en `mods/`: tanto Forge puro como CleanroomMC requieren nombres SRG (`reobfJar`). Usar el `-dev.jar` produce `NoSuchMethodError` en clases de Minecraft (ej. `Minecraft.getMinecraft()`).

### E. Modo Server-Optional (Client-Only Graceful Fallback)
- No asumas que el servidor tiene tu mod o `x4ui` instalado si la pantalla puede abrirse en cliente: usa `UINetwork.isServerSupported()`, `UINetwork.sendActionOrLocal(...)` y maneja `RpcResult.error("NO_SERVER_SUPPORT", ...)`.

