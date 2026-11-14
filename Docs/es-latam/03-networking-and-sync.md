# 03 - Redes, Sidedness y Sincronización Servidor/Cliente

Este documento detalla la arquitectura de red flexible de `x4ui` (`r1.0b6`), diseñada para funcionar **tanto con `Container` vanilla como sin ningún `Container`** (paneles administrativos, tiendas, diálogos, gremios, HUDs remotos o máquinas personalizadas).

---

## 1. Arquitectura de Sidedness y Handshake (`UINetwork`)

- **`ClientProxy`:** Inicializa renderizado (`IGraphics`), fuentes (`FontRegistry`), multimedia (`MediaService`) y overlays (`GuiOverlayManager`).
- **`CommonProxy`:** Punto de entrada en servidor dedicado. No carga ninguna clase de OpenGL, LWJGL ni `client.*`.
- **Server-Optional (`@NetworkCheckHandler`) + Handshake automático (`PacketHandshake`):**
  - Un cliente con `x4ui` puede entrar a servidores vanilla o sin `x4ui`.
  - En cliente, `UINetwork.isServerSupported()` indica si el servidor tiene `x4ui`.
  - En servidor, `UINetwork.hasClient(player)` indica si el jugador completó el handshake.

---

## 2. Registro y Apertura Remota de Pantallas (`ScreenRegistry` + `UINetwork`)

### Registro en Cliente (`@SideOnly(Side.CLIENT)`)
```java
import com.x4yi.x4ui.client.gui.ScreenRegistry;

ScreenRegistry.register("mimod:shop", payload -> new ShopScreen(payload));
```

### Apertura desde Servidor
```java
import com.x4yi.x4ui.common.network.UINetwork;

if (UINetwork.hasClient(player)) {
    NBTTagCompound payload = new NBTTagCompound();
    payload.setString("shopId", "blacksmith");
    UINetwork.openScreen(player, "mimod:shop", payload);
}
```
- También puedes cerrar la pantalla (`UINetwork.closeScreen(player)`) o enviar notificaciones toast (`UINetwork.sendToast(player, "Título", "Mensaje", 4.0f)`).

---

## 3. Estado Reactivo por Pantalla, Namespaces y Lotes (`UIStateBus` + `PacketStateBatch`)

No necesitas crear un `Container` para sincronizar datos del servidor hacia la interfaz:

### Servidor → Cliente
```java
// Un valor puntual
UINetwork.syncState(player, UIStateBus.scopedKey("shop:blacksmith", "gold"), 1500);

// Múltiples llaves en un solo paquete Netty
Map<String, Object> batch = new LinkedHashMap<>();
batch.put("shop:blacksmith:gold", 1500);
batch.put("shop:blacksmith:open", true);
UINetwork.syncBatch(player, batch);
```

### Cliente (`GuiBaseScreen` / `GuiBaseContainer`)
- Usa `syncedState(key, valorInicial)` para crear estados con **`Scope.SCREEN`**: se limpian automáticamente en `onGuiClosed()`.
- También existen `Scope.SESSION` (toda la sesión en el servidor) y `Scope.GLOBAL` (persiste entre desconexiones).
- `UIStateBus.scopedKey(namespace, key)` y `UIStateBus.clearNamespace(namespace)` evitan colisiones entre múltiples bloques, entidades o menús.

---

## 4. Acciones Cliente → Servidor y Request–Response (RPC)

### Acciones unidireccionales (`sendActionToServer` / `sendActionOrLocal`)
En servidor:
```java
UINetwork.registerActionHandler(
    "mimod:buy_item",
    ActionGuard.withinDistance(pos, 8.0), // opcional
    (player, actionId, data) -> {
        // Lógica autoritativa en servidor
    }
);
```
En cliente (`GuiBaseScreen` o `GuiBaseContainer`):
```java
// Envía al servidor si tiene x4ui, o ejecuta el fallback local en modo client-only
sendActionOrLocal("mimod:buy_item", data, () -> showLocalPreview());
```

### Consultas Request–Response (`PacketGuiRpc`)
Ideal para búsquedas, listas paginadas o validaciones sin coordinar paquetes manualmente:

```java
// Servidor
UINetwork.onRpc("mimod:search_catalog", ActionGuard.opLevel(0), (player, params) -> {
    NBTTagCompound res = new NBTTagCompound();
    res.setInteger("total", 42);
    return RpcResult.ok(res);
});

// Cliente (dentro de GuiBaseScreen / GuiBaseContainer)
request("mimod:search_catalog", queryNbt, result -> {
    if (result.isOk()) {
        int total = result.getData().getInteger("total");
    }
});
```
- El framework ejecuta el handler en el hilo del servidor, entrega la respuesta en el hilo principal del cliente, aplica timeout (5s por defecto), responde `NO_SERVER_SUPPORT` al instante si el servidor no tiene `x4ui`, y **cancela automáticamente** los callbacks pendientes si el jugador cierra la pantalla.

---

## 5. Canales Multi-Jugador con Ciclo de Vida Automático (`UIChannels`)

Para UIs compartidas entre varios jugadores sin `Container` (tableros de facción, subastas, paneles compartidos):

```java
// Servidor: abre la pantalla y suscribe automáticamente al jugador al canal
UINetwork.openChannelScreen(player, "mimod:faction_panel", "faction:alpha", data);

// Cuando cambia el estado compartido, transmite a todos los suscritos con cliente compatible:
UIChannels.broadcastState("faction:alpha", "treasury", 9800);
UIChannels.broadcastBatch("faction:alpha", entries);
UIChannels.broadcastToast("faction:alpha", "Tesorería", "+500 oro", 3.0f);
```
- Al cerrar la pantalla (`onGuiClosed()`) o desconectarse (`ClientSessionTracker`), el jugador se desuscribe automáticamente del canal.

---

## 6. Seguridad y Validación (`ActionGuard` + Rate-Limiting)

- **Rate-limiting integrado:** `UINetwork` aplica por defecto un cooldown de `50ms` por jugador y `actionId`/`rpcId` (configurable vía `UINetwork.setDefaultRateLimitMs(ms)`).
- **Guardas componibles (`ActionGuard`):**
  - `ActionGuard.opLevel(int level)`
  - `ActionGuard.withinDistance(BlockPos pos, double maxDist)`
  - `ActionGuard.withinEntityDistance(int entityId, double maxDist)`
  - `ActionGuard.require(Predicate<EntityPlayerMP>)`
  - Combinables con `.and(...)`.

---

## 7. Sincronización de `Container` Vanilla (`NetworkSyncHelper`)

Si tu pantalla sí usa un `Container` de inventario (`GuiBaseContainer`), puedes seguir vinculando propiedades de ventana (16-bit o 32-bit atómicas) a `State<T>`:

```java
// Servidor (Container.detectAndSendChanges)
NetworkSyncHelper.send32BitProperty(listener, this, 0, 1, energy);
NetworkSyncHelper.sendFloat32Property(listener, this, 2, 3, temperature);

// Cliente (GuiBaseContainer.initComponents)
NetworkSyncHelper.bindContainerPropertyTo32Bit(getContainer(), 0, 1, energyState);
NetworkSyncHelper.bindContainerPropertyToFloat32(getContainer(), 2, 3, tempState);
```

---

## Qué Evitar Hacer

> [!CAUTION]
> **1. NUNCA importar `com.x4yi.x4ui.client.*` en clases comunes o de servidor.**
> Mantén `ScreenRegistry` y `GuiBaseScreen` aislados con `@SideOnly(Side.CLIENT)`.

> [!CAUTION]
> **2. NUNCA confiar en payloads de acciones o RPCs sin `ActionGuard` o validación en servidor.**
> Verifica distancia, dimensión, permisos y rangos numéricos en el servidor.
