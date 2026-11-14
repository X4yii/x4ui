# 09 - Forge 1.12.2 ↔ CleanroomMC Compatibility & Abstractions

`x4ui` (`r1.0b6`) officially supports **Forge 1.12.2 (Java 8 + LWJGL 2)** and **CleanroomMC (Java 25 + LWJGL 3 / `lwjgly`)**.  
To keep consumer mods portable across both runtimes, use `x4ui`'s abstractions instead of depending directly on low-level JVM, LWJGL, or library-specific APIs.

---

## 1. `x4ui` Abstraction Matrix

| Need | Use this `x4ui` API | Avoid calling directly |
| :--- | :--- | :--- |
| Drawing, clipping, text, textures, depth clear | `IGraphics` (`GLGraphics`) | `org.lwjgl.opengl.GL11` / `GL12`, `GL11.glScissor` |
| Key codes, modifiers, normalized wheel & mouse | `KeyCodes` + `UIInput` (`normalizeWheelDelta`, `safeMouseX/Y`) | `org.lwjgl.input.Keyboard`, `org.lwjgl.input.Mouse` |
| Direct buffers & I/O executors | `PlatformBridge.cleanDirectBuffer`, `PlatformBridge.newIoExecutor` | `sun.misc.Unsafe`, `DirectBuffer.cleaner()`, raw threads |
| Runtime detection | `PlatformBridge.isCleanroom()`, `isLwjgl3()`, `getJavaFeatureVersion()` | Ad-hoc `Class.forName(...)` checks |
| Server-client networking & remote screens | `UINetwork`, `ScreenRegistry`, `UIChannels`, `UIStateBus`, `ActionGuard` | Raw Netty channels or manual packet boilerplate |
| Safe remote downloads & file paths | `SecurityUtils` (`validateHttpUrl`, `resolveSafeChild`) | Unchecked `new URL(...).openStream()` |

---

## 2. Critical Rules for Consumer Mods

### A. Always target Java 8 bytecode
- Keep `sourceCompatibility = 1.8` and `targetCompatibility = 1.8` in `build.gradle`.
- Compiling for Java 25 will crash on Forge 1.12.2 (Java 8) with `UnsupportedClassVersionError`.
- Delegate native buffer cleanup and async executors to `PlatformBridge`, which handles Java 8 and Java 25 transparently via Multi-Release JAR support.

### B. Avoid version-skewed Guava / Netty APIs
- Forge 1.12.2 ships **Guava 21** and **Netty 4.1**, while CleanroomMC ships **Guava 33.x** and **Netty 4.2.x**.
- Avoid methods removed in modern Guava (e.g., 2-arg `Futures.addCallback`) as well as APIs introduced only in Guava 33 or Netty 4.2. Prefer standard Java 8 APIs, `NBTTagCompound`, and `ByteBufUtils`.

### C. Avoid direct `org.lwjgl.input.*` and unguarded `mc.displayWidth`
- Under LWJGL 3 / GLFW (CleanroomMC), scroll wheel deltas may differ from `±120`, and `mc.displayWidth` / `displayHeight` can temporarily be `0` when minimized.
- Use `KeyCodes` (e.g. `input.setSubmitKey(KeyCodes.ENTER)`) and `UIInput` (`UIInput.getEventDWheel()`, `UIInput.safeMouseX(...)`).

### D. Always ship and test the `reobfJar` artifact
- Never place `*-dev.jar` into `mods/`: both Forge 1.12.2 and CleanroomMC require SRG-reobfuscated JARs (`reobfJar`). Using `-dev.jar` causes `NoSuchMethodError` on Minecraft methods.

### E. Design for Server-Optional Fallback
- Never assume the remote server has `x4ui` or your mod installed: use `UINetwork.isServerSupported()`, `UINetwork.sendActionOrLocal(...)`, and handle `RpcResult.error("NO_SERVER_SUPPORT", ...)`.

