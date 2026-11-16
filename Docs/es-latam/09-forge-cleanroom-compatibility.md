# 09 - Compatibilidad Forge 1.12.2 ↔ CleanroomMC y Abstracciones

X4UI (`r1.0b6`) soporta oficialmente **Forge 1.12.2 (Java 8 + LWJGL 2)** y **CleanroomMC (Java 25 + LWJGL 3 / `lwjgly`)**. Este documento detalla las decisiones de arquitectura Multi-Release y las reglas de portabilidad para mods consumidores.

---

## 1. Problema y Decisión de Diseño (`PlatformBridge`)

Entre Forge 1.12.2 y CleanroomMC cambian la versión de la JVM (Java 8 vs Java 25), el sistema de ventanas/entrada (LWJGL 2 vs LWJGL 3/GLFW) y las versiones de librerías base (Guava 21 / Netty 4.1 vs Guava 33.x / Netty 4.2.x). El acceso directo a APIs internas (`sun.misc.Unsafe`, `DirectBuffer.cleaner()`, `org.lwjgl.input.*`) provoca fallos en tiempo de ejecución en uno u otro entorno.

X4UI empaqueta **`PlatformBridge` como Multi-Release JAR** (`Java 8` base + `META-INF/versions/21/`):
- En **Java 25 (CleanroomMC)** utiliza `java.lang.ref.Cleaner` y executors modernos sin reflexión restringida.
- En **Java 8 (Forge 1.12.2)** aplica un fallback seguro compatible con el runtime clásico.

---

## 2. Matriz de Abstracciones de X4UI

| Necesidad | API de X4UI Recomendada | Evitar Llamar Directamente |
| :--- | :--- | :--- |
| Renderizado, scissor, texturas BGRA y depth | `IGraphics` (`GLGraphics`) | `org.lwjgl.opengl.GL11` / `GL12`, `GL11.glScissor` |
| Teclas, modificadores, scroll y mouse | `KeyCodes` + `UIInput` (`normalizeWheelDelta`, `safeMouseX/Y`) | `org.lwjgl.input.Keyboard`, `org.lwjgl.input.Mouse` |
| Buffers directos y executors I/O | `PlatformBridge.cleanDirectBuffer`, `PlatformBridge.newIoExecutor` | `sun.misc.Unsafe`, `DirectBuffer.cleaner()`, threads crudos |
| Detección de entorno en runtime | `PlatformBridge.isCleanroom()`, `isLwjgl3()`, `getJavaFeatureVersion()` | `Class.forName(...)` ad-hoc |
| Red servidor–cliente y pantallas remotas | `UINetwork`, `ScreenRegistry`, `UIChannels`, `UIStateBus`, `ActionGuard` | Canales Netty crudos o paquetes manuales |
| Descargas y rutas seguras en caché | `SecurityUtils` (`validateHttpUrl`, `resolveSafeChild`) | `new URL(...).openStream()` sin validación SSRF/path |

---

## 3. Reglas Críticas para Mods Consumidores

1. **Compilar siempre con target Java 8:** configurar `sourceCompatibility = 1.8` y `targetCompatibility = 1.8` en `build.gradle`. Bytecode de Java 25 fallará en Forge 1.12.2 con `UnsupportedClassVersionError`.
2. **Evitar APIs incompatibles de Guava / Netty:** evitar métodos eliminados en Guava moderno (ej. `Futures.addCallback` de 2 argumentos) y métodos exclusivos de Guava 33 o Netty 4.2; preferir la biblioteca estándar de Java 8, `NBTTagCompound` y `ByteBufUtils`.
3. **Proteger entrada y división por `displayWidth`:** en LWJGL 3 / GLFW, los deltas de scroll pueden diferir de `±120` y `mc.displayWidth` / `displayHeight` pueden valer `0` al minimizar la ventana. Utilizar `KeyCodes` y `UIInput`.
4. **Distribuir siempre el JAR de `reobfJar`:** nunca instalar `*-dev.jar` en `mods/`. Tanto Forge como CleanroomMC requieren mapeo SRG; usar `-dev.jar` produce `NoSuchMethodError` en métodos de Minecraft.
5. **Soportar modo Server-Optional:** verificar `UINetwork.isServerSupported()`, utilizar `sendActionOrLocal(...)` y manejar `NO_SERVER_SUPPORT` en RPCs.
