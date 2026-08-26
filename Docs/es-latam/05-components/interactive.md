# Interactive (Botones y Toggles)

Componentes diseñados para recibir clics y ejecutar código.

## GuiButton

El botón clásico. Muestra un fondo, un texto centrado y cambia de color al pasar el ratón por encima.

### Creación
```java
GuiButton btn = new GuiButton(x, y, width, height, "Conectar al Servidor", () -> {
    Minecraft.getMinecraft().displayGuiScreen(new GuiConnecting(null, mc, miServerData));
});
```

### Comportamiento
- Al pasar el ratón, el color de fondo transiciona suavemente.
- Al hacer clic izquierdo, reproduce automáticamente el sonido de clic de Minecraft.

## GuiToggle

Funciona como un interruptor de encendido/apagado (Switch).

### Creación
```java
GuiToggle luz = new GuiToggle(x, y, width, height, "Renderizar Partículas", true, (particulasActivas) -> {
    MiModConfigs.renderParticles = particulasActivas;
});
```

### Comportamiento
- Muestra un texto a la izquierda y un interruptor a la derecha ("ON" / "OFF").
- Al hacer clic, se mueve de lado a lado con una animación fluida y avisa a tu código.

## GuiDropdown<T>

Un menú desplegable para elegir una opción de una lista. Está conectado a un `State<T>`.

### Creación
```java
State<String> modoVuelo = new State<>("Normal");
List<String> opciones = Arrays.asList("Normal", "Jetpack", "Hover");

GuiDropdown<String> menu = new GuiDropdown<>(x, y, width, height, modoVuelo, opciones);
```

### ¿Cómo funciona?
- Muestra la opción actual. Al hacer clic, se abre una lista con las demás opciones.
- Para evitar que la lista quede escondida detrás de otras cosas, el menú aumenta su capa (`layer`) automáticamente al abrirse, dibujándose por encima de todo lo demás de forma segura.
