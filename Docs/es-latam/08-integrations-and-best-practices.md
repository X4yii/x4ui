# 08 - Integraciones y Mejores Prácticas

Este documento detalla las directrices estandarizadas para trabajar con X4UI en proyectos de gran escala, evitando deuda arquitectónica y garantizando consistencia.

---

## 1. Reglas de Nomenclatura (Naming Conventions)

Para mantener la claridad arquitectónica y evitar el síndrome del "God Object", el prefijo `Gui` en X4UI está estrictamente reservado para **componentes visuales evidentes** que heredan de `GuiComponent`.

### Lo que SÍ debe llamarse `Gui`
- Componentes interactivos: `GuiButton`, `GuiTextField`, `GuiCheckbox`.
- Contenedores de layouts: `GuiPanel`, `GuiScrollPanel`, `GuiVirtualList`.
- Gráficos y multimedia: `GuiImage`, `GuiVideo`, `GuiSprite`.

### Lo que NO debe llamarse `Gui` (Antipatrón)
- Gestores y Controladores: No llames `GuiToastManager` a un gestor de colas si éste no se dibuja a sí mismo. Lrámalo `ToastManager`. *(Nota: si el gestor hereda de `GuiOverlay` y renderiza contenido en pantalla de forma autónoma, el prefijo `Gui` sí es justificable).*
- Estructuras de Datos y Estados: `UIStateBus` no se llama `GuiStateBus`. `State<T>` no se llama `GuiState`.
- Modelos de Red: No llames `GuiPacket` a un paquete de sincronización.

---

## 2. Inyección de Dependencias y Modelos

Nunca pases objetos gigantes del juego (como el jugador entero, mundos o bases de datos) directamente al constructor de los componentes visuales.
Las Vistas (Views) deben ser estúpidas (*Dumb Views*) y reactivas.

**Antipatrón (Acoplamiento Fuerte):**
```java
GuiPanel playerStats = new GuiPlayerStatsPanel(Minecraft.getMinecraft().player);
```

**Buena Práctica (Reactivo, Desacoplado):**
Pasa contenedores de estado reactivo o utiliza el `UIStateBus`.

```java
State<PlayerStatsData> stats = new State<>(new PlayerStatsData());
GuiPanel playerStats = new PlayerStatsPanel(stats); // El panel se suscribe internamente
```

---

## 3. Prácticas de Inicialización

Al crear un `Screen` completo en Minecraft que utilice X4UI, instancia tus componentes una sola vez en el constructor o en un método de inicialización llamado *antes* de adjuntarlo a la pantalla.

Nunca inicies descargas pesadas del `MediaPipeline` dentro de `update()` o `render()`. Hazlo en la fase de inicialización.

---

## 4. Auditorías de Memoria

Recuerda que en Java 8, abusar de lambdas sin cuidado (capturando referencias del `Screen` entero) puede evitar que el Garbage Collector libere pantallas cerradas.
- Usa `bindState` siempre en los componentes, ya que X4UI los desvinculará automáticamente cuando el componente sea destruido al cerrar el `Screen`.
- Si utilizas `State.addListener()` manual, asegúrate de desuscribirlo en el `onDetached()` de tu componente raíz.
