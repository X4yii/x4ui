# 03 - Networking & Server Sync

X4UI components are strictly client-side (`@SideOnly(Side.CLIENT)`). X4UI classes must never be referenced from `common` or server-side code, as this causes `ClassNotFoundException` on dedicated servers.

> **Note:** The server-client synchronization system is in a very early stage of development. The `NetworkSyncHelper` and `StateContainerListener` API may change without notice.

## Opening a GUI from the Server

To open an X4UI screen from the server, use Forge's `SimpleImpl` networking to send a packet that instructs the client to open the GUI.

### 1. Define the Packet

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

### 2. Client-Side Handler

The handler executes exclusively on the client. X4UI classes can be referenced here.

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

### 3. Register the Packet

Register during `FMLPreInitializationEvent`:

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

### 4. Send from Server

```java
MyModNetwork.CHANNEL.sendTo(new PacketOpenUI("settings"), (EntityPlayerMP) player);
```

## Server-to-Client Data Sync (`State<T>` + Container Properties)

X4UI's `State<T>` system can be synchronized from server to client using Minecraft's `Container` property mechanism. This is useful for syncing inventory-related data.

### StateContainerListener

`StateContainerListener` implements `IContainerListener` and bridges container property IDs to `State` objects.

```java
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.sync.StateContainerListener;

State<Integer> progress = new State<>(0);
State<Boolean> isActive = new State<>(false);

StateContainerListener listener = new StateContainerListener();
listener.bindProperty(0, progress);          // Container property 0 -> Integer state
listener.bindBooleanProperty(1, isActive);   // Container property 1 -> Boolean state

container.addListener(listener);
```

### NetworkSyncHelper

`NetworkSyncHelper` provides static convenience methods that automatically manage `StateContainerListener` instances per `Container`. Uses a `WeakHashMap` to prevent memory leaks.

```java
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.sync.NetworkSyncHelper;

State<Integer> fuelLevel = new State<>(0);
State<Boolean> isBurning = new State<>(false);
State<Float> temperature = new State<>(0f);

// Bind container properties to State objects
NetworkSyncHelper.bindContainerPropertyToState(container, 0, fuelLevel);
NetworkSyncHelper.bindContainerPropertyToBoolean(container, 1, isBurning);
NetworkSyncHelper.bindContainerPropertyToFloat(container, 2, temperature, 100f);
// The scale parameter (100f) divides the raw integer value to produce a float.
// So a raw value of 50 becomes 0.5f.
```

### Server-Side: Sending Properties

In your `Container` subclass, override `detectAndSendChanges()` to push values:

```java
@Override
public void detectAndSendChanges() {
    super.detectAndSendChanges();
    for (IContainerListener listener : listeners) {
        listener.sendWindowProperty(this, 0, fuelLevel);    // property ID 0
        listener.sendWindowProperty(this, 1, isBurning ? 1 : 0);  // property ID 1
        listener.sendWindowProperty(this, 2, (int)(temperature * 100f));  // property ID 2, scaled
    }
}
```

### Client-Side: Using Synced State

On the client, bind the synced `State` to UI components:

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

`IGuiActionSender` is an interface for dispatching structured actions from the client GUI to the server. Set it on the screen and invoke it from component callbacks.

```java
import com.x4yi.x4ui.common.sync.IGuiActionSender;
import net.minecraft.nbt.NBTTagCompound;

// Set on the screen
IGuiActionSender sender = (actionId, data) -> {
    MyModNetwork.CHANNEL.sendToServer(new PacketAction(actionId, data));
};
setActionSender(sender);

// Use in a button callback
NBTTagCompound data = new NBTTagCompound();
data.setString("item", "diamond");
sender.sendActionToServer("craft", data);
```

## Important Constraints

1. **Never reference X4UI classes in `common` or server packages.** Always keep X4UI imports in client-side code only.
2. **Use `Minecraft.getMinecraft().addScheduledTask()`** in packet handlers to ensure thread safety.
3. **Container properties are limited to `int` values.** Use `sendWindowProperty()` with integer scaling for floats.
4. **`State<T>` in `com.x4yi.x4ui.common`** is safe for both client and server. Only the GUI components are client-only.
