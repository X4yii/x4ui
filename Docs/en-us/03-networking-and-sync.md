# 03 - Networking, Sidedness & Server/Client Sync

This document covers `x4ui` (`r1.0b6`) networking and synchronization, designed to work **both with vanilla `Container`s and completely without `Container`s** (admin panels, shops, dialogs, guild boards, remote HUDs, or custom screens).

---

## 1. Sidedness & Automatic Handshake (`UINetwork`)

- **`ClientProxy`:** Initializes rendering (`IGraphics`), fonts (`FontRegistry`), multimedia (`MediaService`), and overlays (`GuiOverlayManager`).
- **`CommonProxy`:** Dedicated server entry point. Loads zero OpenGL, LWJGL, or `client.*` classes.
- **Server-Optional (`@NetworkCheckHandler`) + Handshake (`PacketHandshake`):**
  - Clients with `x4ui` can join vanilla or modded servers without requiring `x4ui` on the server.
  - Client-side: `UINetwork.isServerSupported()` reports whether the server has `x4ui`.
  - Server-side: `UINetwork.hasClient(player)` reports whether the player completed the handshake.

---

## 2. Remote Screen Registration & Opening (`ScreenRegistry` + `UINetwork`)

### Client Registration (`@SideOnly(Side.CLIENT)`)
```java
import com.x4yi.x4ui.client.gui.ScreenRegistry;

ScreenRegistry.register("mymod:shop", payload -> new ShopScreen(payload));
```

### Opening from Server
```java
import com.x4yi.x4ui.common.network.UINetwork;

if (UINetwork.hasClient(player)) {
    NBTTagCompound payload = new NBTTagCompound();
    payload.setString("shopId", "blacksmith");
    UINetwork.openScreen(player, "mymod:shop", payload);
}
```
- You can also close the active screen (`UINetwork.closeScreen(player)`) or send toast notifications (`UINetwork.sendToast(player, "Title", "Message", 4.0f)`).

---

## 3. Screen-Scoped State, Namespaces & Batching (`UIStateBus` + `PacketStateBatch`)

You do not need a `Container` to sync server state to a UI:

### Server → Client
```java
// Single key
UINetwork.syncState(player, UIStateBus.scopedKey("shop:blacksmith", "gold"), 1500);

// Multiple keys in a single Netty packet
Map<String, Object> batch = new LinkedHashMap<>();
batch.put("shop:blacksmith:gold", 1500);
batch.put("shop:blacksmith:open", true);
UINetwork.syncBatch(player, batch);
```

### Client (`GuiBaseScreen` / `GuiBaseContainer`)
- Use `syncedState(key, initialValue)` to create **`Scope.SCREEN`** states that are automatically cleaned up in `onGuiClosed()`.
- `Scope.SESSION` (cleared on disconnect) and `Scope.GLOBAL` (persists across disconnects) are also available.
- `UIStateBus.scopedKey(namespace, key)` and `UIStateBus.clearNamespace(namespace)` prevent key collisions across multiple blocks, entities, or menus.

---

## 4. Client → Server Actions & Request–Response (RPC)

### Fire-and-Forget Actions (`sendActionToServer` / `sendActionOrLocal`)
Server-side:
```java
UINetwork.registerActionHandler(
    "mymod:buy_item",
    ActionGuard.withinDistance(pos, 8.0), // optional guard
    (player, actionId, data) -> {
        // Server-authoritative logic
    }
);
```
Client-side (`GuiBaseScreen` or `GuiBaseContainer`):
```java
// Sends to server if supported, otherwise executes localFallback immediately
sendActionOrLocal("mymod:buy_item", data, () -> showLocalPreview());
```

### Request–Response Queries (`PacketGuiRpc`)
Ideal for paginated lists, search queries, or server validations without manual packet correlation:

```java
// Server
UINetwork.onRpc("mymod:search_catalog", ActionGuard.opLevel(0), (player, params) -> {
    NBTTagCompound res = new NBTTagCompound();
    res.setInteger("total", 42);
    return RpcResult.ok(res);
});

// Client (inside GuiBaseScreen / GuiBaseContainer)
request("mymod:search_catalog", queryNbt, result -> {
    if (result.isOk()) {
        int total = result.getData().getInteger("total");
    }
});
```
- The framework runs the handler on the server main thread, delivers the result on the client main thread, enforces a default 5s timeout, returns `NO_SERVER_SUPPORT` immediately on client-only servers, and **auto-cancels** pending callbacks when the screen closes.

---

## 5. Multi-Player Pub/Sub Channels (`UIChannels`)

For shared UIs without a `Container` (faction panels, auction houses, shared dashboards):

```java
// Server: opens the screen and automatically subscribes the player to the channel
UINetwork.openChannelScreen(player, "mymod:faction_panel", "faction:alpha", data);

// Broadcast updates to all subscribed players with compatible clients:
UIChannels.broadcastState("faction:alpha", "treasury", 9800);
UIChannels.broadcastBatch("faction:alpha", entries);
UIChannels.broadcastToast("faction:alpha", "Treasury", "+500 gold", 3.0f);
```
- Closing the screen (`onGuiClosed()`) or logging out (`ClientSessionTracker`) automatically unsubscribes the player.

---

## 6. Security & Validation (`ActionGuard` + Rate-Limiting)

- **Built-in rate-limiting:** `UINetwork` enforces a default `50ms` cooldown per player and `actionId`/`rpcId` (configurable via `UINetwork.setDefaultRateLimitMs(ms)`).
- **Composable `ActionGuard` helpers:**
  - `ActionGuard.opLevel(int level)`
  - `ActionGuard.withinDistance(BlockPos pos, double maxDist)`
  - `ActionGuard.withinEntityDistance(int entityId, double maxDist)`
  - `ActionGuard.require(Predicate<EntityPlayerMP>)`
  - Combine guards with `.and(...)`.

---

## 7. Vanilla `Container` Sync (`NetworkSyncHelper`)

When your screen is backed by an inventory `Container` (`GuiBaseContainer`), you can bind window properties (16-bit or atomic 32-bit `int`/`float`) to `State<T>`:

```java
// Server (Container.detectAndSendChanges)
NetworkSyncHelper.send32BitProperty(listener, this, 0, 1, energy);
NetworkSyncHelper.sendFloat32Property(listener, this, 2, 3, temperature);

// Client (GuiBaseContainer.initComponents)
NetworkSyncHelper.bindContainerPropertyTo32Bit(getContainer(), 0, 1, energyState);
NetworkSyncHelper.bindContainerPropertyToFloat32(getContainer(), 2, 3, tempState);
```

---

## What to Avoid

> [!CAUTION]
> **1. NEVER import `com.x4yi.x4ui.client.*` in common or server classes.**
> Keep `ScreenRegistry` and `GuiBaseScreen` isolated inside `@SideOnly(Side.CLIENT)`.

> [!CAUTION]
> **2. NEVER trust client payloads without `ActionGuard` or server-side validation.**
> Always validate distance, dimension, permissions, and numeric bounds on the server.
