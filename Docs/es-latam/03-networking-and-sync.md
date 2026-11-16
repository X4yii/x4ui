# 03 - Redes, Sidedness y Sincronización Servidor/Cliente

Este documento describe la arquitectura de red de X4UI (`r1.0b6`), diseñada para operar **tanto con `Container` vanilla como sin ningún `Container`** (paneles administrativos, tiendas, diálogos, gremios, HUDs remotos y máquinas personalizadas).

---

## 1. Problema y Decisiones de Diseño

En Minecraft 1.12.2, las interfaces servidor–cliente suelen requerir `Container`s rígidos, paquetes Netty repetitivos y validación manual de seguridad. Al mismo tiempo, un mod de UI instalado en cliente no debe impedir conectarse a servidores vanilla sin el mod.

X4UI resuelve esto mediante:
1. **Aislamiento estricto de Sidedness:** `CommonProxy` no referencia ninguna clase gráfica (`OpenGL`, `LWJGL` o `client.*`); `ClientProxy` concentra todo el subsistema visual.
2. **Arquitectura Server-Optional (`@NetworkCheckHandler` + `PacketHandshake`):** Un cliente con X4UI puede conectarse a servidores vanilla; `UINetwork.isServerSupported()` en cliente y `UINetwork.hasClient(player)` en servidor permiten degradación elegante.
3. **Protocolo unificado sobre NBT/ByteBuf:** `UINetwork` provee apertura remota de pantallas, lotes de estado, RPC request–response y canales Pub/Sub sin registrar canales Netty adicionales por cada pantalla.

---

## 2. Registro y Apertura Remota de Pantallas (`ScreenRegistry` + `UINetwork`)

### Registro en Cliente (`@SideOnly(Side.CLIENT)`)

```java
import com.x4yi.x4ui.client.gui.ScreenRegistry;

ScreenRegistry.register("mymod:shop", payload -> new ShopScreen(payload));
```

### Apertura, Cierre y Toast desde Servidor

```java
import com.x4yi.x4ui.common.network.UINetwork;

if (UINetwork.hasClient(player)) {
    NBTTagCompound payload = new NBTTagCompound();
    payload.setString("shopId", "blacksmith");
    UINetwork.openScreen(player, "mymod:shop", payload);
}
```

También están disponibles `UINetwork.closeScreen(player)` y `UINetwork.sendToast(player, "Título", "Mensaje", 4.0f)`.

---

## 3. Estado Reactivo por Pantalla, Namespaces y Lotes (`UIStateBus` + `PacketStateBatch`)

### Servidor → Cliente

```java
// Actualización individual
UINetwork.syncState(player, UIStateBus.scopedKey("shop:blacksmith", "gold"), 1500);

// Múltiples llaves en un único paquete Netty
Map<String, Object> batch = new LinkedHashMap<>();
batch.put("shop:blacksmith:gold", 1500);
batch.put("shop:blacksmith:open", true);
UINetwork.syncBatch(player, batch);
```

### Cliente (`GuiBaseScreen` / `GuiBaseContainer`)

`syncedState(key, valorInicial)` crea o recupera un `State<T>` en `Scope.SCREEN`, el cual se limpia automáticamente en `onGuiClosed()`. Para persistencia entre pantallas se dispone de `Scope.SESSION` (limpieza al desconectarse) y `Scope.GLOBAL`.

`UIStateBus.scopedKey(namespace, key)` y `UIStateBus.clearNamespace(namespace)` evitan colisiones entre múltiples bloques, entidades o menús simultáneos.

---

## 4. Acciones Cliente → Servidor y Consultas RPC (`PacketGuiRpc`)

### Acciones Unidireccionales (`sendActionToServer` / `sendActionOrLocal`)

```java
// Servidor
UINetwork.registerActionHandler(
    "mymod:buy_item",
    ActionGuard.withinDistance(pos, 8.0),
    (player, actionId, data) -> {
        // Lógica autoritativa en servidor
    }
);

// Cliente (dentro de GuiBaseScreen / GuiBaseContainer)
sendActionOrLocal("mymod:buy_item", data, () -> showLocalPreview());
```

### Consultas Request–Response (`PacketGuiRpc` + `RpcResult`)

```java
// Servidor
UINetwork.onRpc("mymod:search_catalog", ActionGuard.opLevel(0), (player, params) -> {
    NBTTagCompound res = new NBTTagCompound();
    res.setInteger("total", 42);
    return RpcResult.ok(res);
});

// Cliente (dentro de GuiBaseScreen / GuiBaseContainer)
request("mymod:search_catalog", queryNbt, result -> {
    if (result.isOk()) {
        int total = result.getData().getInteger("total");
    }
});
```

El framework ejecuta el handler en el hilo del servidor, entrega la respuesta en el hilo principal del cliente, aplica timeout (`5s` por defecto), responde `NO_SERVER_SUPPORT` de inmediato si el servidor no tiene X4UI y cancela automáticamente los callbacks pendientes al cerrar la pantalla.

---

## 5. Canales Multi-Jugador (`UIChannels`)

Para interfaces compartidas entre varios jugadores sin `Container` (tableros de facción, subastas, paneles globales):

```java
// Servidor: abre la pantalla y suscribe al jugador al canal
UINetwork.openChannelScreen(player, "mymod:faction_panel", "faction:alpha", data);

// Transmisión a todos los suscritos con cliente compatible
UIChannels.broadcastState("faction:alpha", "treasury", 9800);
UIChannels.broadcastBatch("faction:alpha", entries);
UIChannels.broadcastToast("faction:alpha", "Tesorería", "+500 oro", 3.0f);
```

Al cerrar la pantalla (`onGuiClosed()`) o desconectarse (`ClientSessionTracker`), el jugador se desuscribe automáticamente.

---

## 6. Seguridad en Red (`ActionGuard` y Rate-Limiting)

- **Rate-limiting integrado:** `UINetwork` aplica por defecto un cooldown de `50ms` por jugador y por `actionId`/`rpcId` (configurable mediante `UINetwork.setDefaultRateLimitMs(ms)`).
- **Guardas componibles (`ActionGuard`):**
  - `ActionGuard.opLevel(int level)`
  - `ActionGuard.withinDistance(BlockPos pos, double maxDist)`
  - `ActionGuard.withinEntityDistance(int entityId, double maxDist)`
  - `ActionGuard.require(Predicate<EntityPlayerMP>)`
  - Combinables mediante `.and(...)`.

---

## 7. Sincronización de `Container` Vanilla (`NetworkSyncHelper`)

Cuando la pantalla utiliza un `Container` (`GuiBaseContainer`), `NetworkSyncHelper` y `StateContainerListener` sincronizan propiedades de ventana de 16-bit o 32-bit atómicas (`int` y `float`):

```java
// Servidor (Container.detectAndSendChanges)
NetworkSyncHelper.send32BitProperty(listener, this, 0, 1, energy);
NetworkSyncHelper.sendFloat32Property(listener, this, 2, 3, temperature);

// Cliente (GuiBaseContainer.initComponents)
NetworkSyncHelper.bindContainerPropertyTo32Bit(getContainer(), 0, 1, energyState);
NetworkSyncHelper.bindContainerPropertyToFloat32(getContainer(), 2, 3, tempState);
```

---

## 8. Qué Evitar en Redes

| Antipatrón | Causa Técnica del Problema | Solución Correcta |
| :--- | :--- | :--- |
| Importar `com.x4yi.x4ui.client.*` en clases comunes o servidor | Carga clases `@SideOnly(Side.CLIENT)` y falla con `ClassNotFoundException` en servidor dedicado | Aislar `ScreenRegistry` y `GuiBaseScreen` en `@SideOnly(Side.CLIENT)` |
| Procesar acciones/RPC sin `ActionGuard` ni validación | Permite spoofing de paquetes, interacción a distancia infinita o escalada de privilegios | Aplicar `ActionGuard` y validar permisos, dimensión y rangos en servidor |
| Asumir presencia obligatoria del servidor para vistas locales | Rompe pantallas client-only en servidores vanilla | Usar `UINetwork.isServerSupported()`, `sendActionOrLocal(...)` y verificar `result.isOk()` |
