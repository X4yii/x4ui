# 03 - Redes, Sidedness y Sincronización Servidor/Cliente

Este documento detalla la arquitectura de separación de lados (*sidedness*), compatibilidad de conexión asimétrica y sincronización reactiva de datos entre el servidor dedicado y las pantallas de cliente de X4UI.

---

## 1. Arquitectura de Sidedness (@SidedProxy)

X4UI implementa separación estricta mediante proxies de Forge:
- **`ClientProxy`:** Inicializa servicios de renderizado (`IGraphics`), rasterización de fuentes (`FontRegistry`), servicios multimedia (`MediaService`, OpenAL) y registros de superposición (`GuiOverlayManager`).
- **`CommonProxy`:** Punto de entrada en servidor dedicado. No carga ninguna clase de OpenGL, LWJGL ni interfaz visual.

### Conexión Server-Optional (@NetworkCheckHandler)
En X4UI r1.0b5, los clientes pueden ingresar a cualquier servidor (vanilla o modded) sin requerir que el servidor tenga instalado X4UI:

```java
@NetworkCheckHandler
public boolean checkModVersion(Map<String, String> mods, Side side) {
    // Retorna true sin exigir presencia en servidor
    return true;
}
```

---

## 2. Apertura de Pantallas desde el Servidor

Las pantallas de X4UI residen exclusivamente en el cliente. Para abrir una interfaz desde el servidor (por ejemplo, al interactuar con un bloque o comando), envíe un paquete estándar de Forge al cliente y programe la apertura en el hilo del cliente:

```java
// En el MessageHandler del cliente (Side.CLIENT)
@Override
public IMessage onMessage(PacketOpenUI message, MessageContext ctx) {
    Minecraft.getMinecraft().addScheduledTask(() -> {
        Minecraft.getMinecraft().displayGuiScreen(
            new MyCustomScreen(Minecraft.getMinecraft().currentScreen)
        );
    });
    return null;
}
```

---

## 3. Sincronización de Contenedores (`NetworkSyncHelper`)

Para sincronizar datos continuos de máquinas o inventarios (progreso, combustible, energía) sin crear canales de red propietarios, X4UI aprovecha el pipeline de propiedades de `Container` de Minecraft:

### Lado Servidor (`Container`)
En su subclase de `Container`, sobrescriba `detectAndSendChanges()`:

```java
@Override
public void detectAndSendChanges() {
    super.detectAndSendChanges();
    for (IContainerListener listener : listeners) {
        // Enviar propiedades de 16 bits
        listener.sendWindowProperty(this, 0, tileEntity.getProgress());
        listener.sendWindowProperty(this, 1, tileEntity.isActive() ? 1 : 0);
        listener.sendWindowProperty(this, 2, (int)(tileEntity.getTemperature() * 10.0f));
    }
}
```

### Lado Cliente (`GuiBaseContainer`)
En `initComponents()`, vincule las propiedades del contenedor a objetos `State<T>`:

```java
@Override
protected void initComponents() {
    State<Integer> progress = new State<>(0);
    State<Boolean> active = new State<>(false);
    State<Float> temperature = new State<>(0.0f);

    // Vinculación automática mediante NetworkSyncHelper
    NetworkSyncHelper.bindContainerPropertyToState(getContainer(), 0, progress);
    NetworkSyncHelper.bindContainerPropertyToBoolean(getContainer(), 1, active);
    NetworkSyncHelper.bindContainerPropertyToFloat(getContainer(), 2, temperature, 10.0f);

    // Enlazar los estados a componentes visuales
    progressBar.bindState(progress, val -> progressBar.setProgress(val / 100.0f));
    statusLabel.bindState(active, act -> statusLabel.setText(act ? "ACTIVO" : "INACTIVO"));
}
```

---

## 4. Despacho de Acciones Cliente-a-Servidor (`IGuiActionSender`)

Para enviar comandos desde la interfaz al servidor (clics en botones de configuración, cambios de pestañas), configure un `IGuiActionSender` en su pantalla:

```java
import com.x4yi.x4ui.common.sync.IGuiActionSender;
import net.minecraft.nbt.NBTTagCompound;

// Configurar el despachador en la pantalla
setActionSender((actionId, data) -> {
    MyModNetwork.CHANNEL.sendToServer(new PacketGuiAction(actionId, data));
});

// Invocar desde el callback de un botón
saveButton.setOnClick(() -> {
    NBTTagCompound data = new NBTTagCompound();
    data.setInteger("powerMode", selectedMode);
    sendActionToServer("set_power_mode", data);
});
```

---

## Qué Evitar Hacer (Errores Críticos de Red y Sidedness)

> [!CAUTION]
> **1. NUNCA importar paquetes `com.x4yi.x4ui.client.*` en clases del servidor.**
> Utilice estas clases únicamente con `@SideOnly(Side.CLIENT)`. Para paquetes comunes, use `State<T>` o `NetworkSyncHelper`.

> [!CAUTION]
> **2. NUNCA confiar ciegamente en datos recibidos mediante `IGuiActionSender`.**
> Valide siempre la información en el servidor (rango, contenedor abierto, límites de valores) antes de procesar acciones de los jugadores.

> [!WARNING]
> **3. NUNCA olvidar el factor de escala en valores de punto flotante.**
> `sendWindowProperty` solo envía enteros (`short`). Multiplique por una escala (ej. `10.0f`) en el servidor y pase la misma a `bindContainerPropertyToFloat()` en el cliente.

> [!IMPORTANT]
> **4. NUNCA ejecutar apertura de GUIs fuera de `addScheduledTask()`.**
> Los paquetes de red llegan en hilos secundarios. Abrir GUIs sin agendarlo en el hilo principal bloqueará el cliente.
