# 03 - Redes y Sincronización con el Servidor

Los componentes de X4UI son estrictamente del lado del cliente (`@SideOnly(Side.CLIENT)`). Las clases de X4UI nunca deben ser referenciadas desde código de `common` o del servidor, ya que esto causa `ClassNotFoundException` en servidores dedicados.

> **Nota:** El sistema de sincronización servidor-cliente se encuentra en una fase muy temprana de desarrollo. La API de `NetworkSyncHelper` y `StateContainerListener` puede cambiar sin previo aviso.

## Abrir una GUI desde el Servidor

Para abrir una pantalla X4UI desde el servidor, use el sistema de red `SimpleImpl` de Forge para enviar un paquete que instruya al cliente a abrir la GUI.

### 1. Definir el Paquete

```java
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketOpenUI implements IMessage {
    public String screenId;

    public PacketOpenUI() {}

    public PacketOpenUI(String screenId) {
        this.screenId = screenId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        this.screenId = new String(bytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        byte[] bytes = this.screenId.getBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
```

### 2. Manejador del Lado del Cliente

El manejador se ejecuta exclusivamente en el cliente. Las clases de X4UI pueden ser referenciadas aquí.

```java
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenUIHandler implements IMessageHandler<PacketOpenUI, IMessage> {
    @Override
    public IMessage onMessage(PacketOpenUI message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft.getMinecraft().displayGuiScreen(
                new MyCustomScreen(Minecraft.getMinecraft().currentScreen)
            );
        });
        return null;
    }
}
```

### 3. Registrar el Paquete

Registre durante `FMLPreInitializationEvent`:

```java
import net.minecraftforge.fml.common.network.NetworkRegistry;

public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("mymod");

@EventHandler
public void preInit(FMLPreInitializationEvent event) {
    CHANNEL.registerMessage(
        PacketOpenUIHandler.class,
        PacketOpenUI.class,
        0,
        Side.CLIENT
    );
}
```

### 4. Enviar desde el Servidor

```java
MyModNetwork.CHANNEL.sendTo(new PacketOpenUI("settings"), (EntityPlayerMP) player);
```

## Sincronización de Datos Servidor-a-Cliente (`State<T>` + Propiedades de Container)

El sistema `State<T>` de X4UI puede sincronizarse del servidor al cliente usando el mecanismo de propiedades `Container` de Minecraft. Esto es útil para sincronizar datos relacionados con inventarios.

### StateContainerListener

`StateContainerListener` implementa `IContainerListener` y vincula IDs de propiedades de container a objetos `State`.

```java
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.sync.StateContainerListener;

State<Integer> progress = new State<>(0);
State<Boolean> isActive = new State<>(false);

StateContainerListener listener = new StateContainerListener();
listener.bindProperty(0, progress);          // Propiedad de container 0 -> Estado Integer
listener.bindBooleanProperty(1, isActive);   // Propiedad de container 1 -> Estado Boolean

container.addListener(listener);
```

### NetworkSyncHelper

`NetworkSyncHelper` proporciona métodos estáticos de conveniencia que gestionan automáticamente instancias de `StateContainerListener` por `Container`. Usa un `WeakHashMap` para prevenir fugas de memoria.

```java
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.sync.NetworkSyncHelper;

State<Integer> fuelLevel = new State<>(0);
State<Boolean> isBurning = new State<>(false);
State<Float> temperature = new State<>(0f);

// Vincular propiedades de container a objetos State
NetworkSyncHelper.bindContainerPropertyToState(container, 0, fuelLevel);
NetworkSyncHelper.bindContainerPropertyToBoolean(container, 1, isBurning);
NetworkSyncHelper.bindContainerPropertyToFloat(container, 2, temperature, 100f);
// El parámetro de escala (100f) divide el valor entero crudo para producir un float.
// Así que un valor crudo de 50 se convierte en 0.5f.
```

### Lado del Servidor: Enviar Propiedades

En su subclase de `Container`, sobrescriba `detectAndSendChanges()` para enviar valores:

```java
@Override
public void detectAndSendChanges() {
    super.detectAndSendChanges();
    for (IContainerListener listener : listeners) {
        listener.sendWindowProperty(this, 0, fuelLevel);    // ID de propiedad 0
        listener.sendWindowProperty(this, 1, isBurning ? 1 : 0);  // ID de propiedad 1
        listener.sendWindowProperty(this, 2, (int)(temperature * 100f));  // ID de propiedad 2, escalado
    }
}
```

### Lado del Cliente: Usar el Estado Sincronizado

En el cliente, vincule el `State` sincronizado a los componentes UI:

```java
@Override
protected void initComponents() {
    State<Integer> fuelLevel = new State<>(0);
    State<Boolean> isBurning = new State<>(false);

    NetworkSyncHelper.bindContainerPropertyToState(getContainer(), 0, fuelLevel);
    NetworkSyncHelper.bindContainerPropertyToBoolean(getContainer(), 1, isBurning);

    GuiLabel fuelLabel = new GuiLabel(0, 0, "Fuel: 0", 0xFFFFFFFF);
    fuelLabel.bindState(fuelLevel, value -> fuelLabel.setText("Fuel: " + value));

    GuiToggle burningToggle = new GuiToggle(0, 0, 120, 20, "Burning", false, null);
    burningToggle.bindState(isBurning, burning -> burningToggle.setState(burning));
}
```

## IGuiActionSender

`IGuiActionSender` es una interfaz para despachar acciones estructuradas desde la GUI del cliente al servidor. Configúrelo en la pantalla e invóquelo desde callbacks de componentes.

```java
import com.x4yi.x4ui.common.sync.IGuiActionSender;
import net.minecraft.nbt.NBTTagCompound;

// Configurar en la pantalla
IGuiActionSender sender = (actionId, data) -> {
    MyModNetwork.CHANNEL.sendToServer(new PacketAction(actionId, data));
};
setActionSender(sender);

// Usar en un callback de botón
NBTTagCompound data = new NBTTagCompound();
data.setString("item", "diamond");
sender.sendActionToServer("craft", data);
```

## Restricciones Importantes

1. **Nunca referencie clases de X4UI en paquetes `common` o del servidor.** Siempre mantenga los imports de X4UI solo en código del lado del cliente.
2. **Use `Minecraft.getMinecraft().addScheduledTask()`** en manejadores de paquetes para garantizar la seguridad de hilos.
3. **Las propiedades de container están limitadas a valores `int`.** Use `sendWindowProperty()` con escala entera para floats.
4. **`State<T>` en `com.x4yi.x4ui.common`** es seguro tanto para cliente como para servidor. Solo los componentes GUI son exclusivos del cliente.
