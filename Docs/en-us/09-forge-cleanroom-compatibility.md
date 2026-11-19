# 09 - Forge 1.12.2 ↔ CleanroomMC Compatibility & Abstractions

X4UI (`r1.0b7`) officially supports **Forge 1.12.2 (Java 8 + LWJGL 2)** and **CleanroomMC (Java 25 + LWJGL 3 / `lwjgly`)**. This document explains the Multi-Release architecture and portability rules for consumer mods.

---

## 1. Problem & Design Decisions (`PlatformBridge`)

Between Forge 1.12.2 and CleanroomMC, the JVM version (Java 8 vs Java 25), window/input stack (LWJGL 2 vs LWJGL 3/GLFW), and bundled dependencies (Guava 21 / Netty 4.1 vs Guava 33.x / Netty 4.2.x) differ significantly. Direct access to internal APIs (`sun.misc.Unsafe`, `DirectBuffer.cleaner()`, `org.lwjgl.input.*`) breaks at runtime in one of the two environments.

X4UI ships **`PlatformBridge` as a Multi-Release JAR** (`Java 8` base + `META-INF/versions/21/`):
- On **Java 25 (CleanroomMC)**, it uses `java.lang.ref.Cleaner` and modern executors without restricted reflection.
- On **Java 8 (Forge 1.12.2)**, it falls back cleanly to a Java 8-compatible implementation.

---

## 2. X4UI Abstraction Matrix

| Need | Recommended X4UI API | Avoid Calling Directly |
| :--- | :--- | :--- |
| Drawing, scissor clipping, BGRA textures, depth | `IGraphics` (`GLGraphics`) | `org.lwjgl.opengl.GL11` / `GL12`, `GL11.glScissor` |
| Key codes, modifiers, normalized wheel & mouse | `KeyCodes` + `UIInput` (`normalizeWheelDelta`, `safeMouseX/Y`) | `org.lwjgl.input.Keyboard`, `org.lwjgl.input.Mouse` |
| Direct buffers & I/O executors | `PlatformBridge.cleanDirectBuffer`, `PlatformBridge.newIoExecutor` | `sun.misc.Unsafe`, `DirectBuffer.cleaner()`, raw threads |
| Runtime detection | `PlatformBridge.isCleanroom()`, `isLwjgl3()`, `getJavaFeatureVersion()` | Ad-hoc `Class.forName(...)` checks |
| Server-client networking & remote screens | `UINetwork`, `ScreenRegistry`, `UIChannels`, `UIStateBus`, `ActionGuard` | Raw Netty channels or manual packet boilerplate |
| Safe remote downloads & cache paths | `SecurityUtils` (`validateHttpUrl`, `resolveSafeChild`) | Unchecked `new URL(...).openStream()` |

---

## 3. Critical Rules for Consumer Mods

1. **Always target Java 8 bytecode:** keep `sourceCompatibility = 1.8` and `targetCompatibility = 1.8` in `build.gradle`. Compiling for Java 25 crashes on Forge 1.12.2 with `UnsupportedClassVersionError`.
2. **Avoid version-skewed Guava / Netty APIs:** avoid methods removed in modern Guava (e.g. 2-arg `Futures.addCallback`) as well as APIs introduced only in Guava 33 or Netty 4.2. Prefer standard Java 8 APIs, `NBTTagCompound`, and `ByteBufUtils`.
3. **Guard input & `displayWidth`:** under LWJGL 3 / GLFW, scroll wheel deltas may differ from `±120` and `mc.displayWidth` / `displayHeight` can be `0` while minimized. Use `KeyCodes` and `UIInput`.
4. **Always ship the `reobfJar` artifact:** never place `*-dev.jar` in `mods/`. Both Forge 1.12.2 and CleanroomMC require SRG-reobfuscated JARs; `-dev.jar` causes `NoSuchMethodError` on Minecraft methods.
5. **Support Server-Optional mode:** check `UINetwork.isServerSupported()`, use `sendActionOrLocal(...)`, and handle `NO_SERVER_SUPPORT` in RPC callbacks.
