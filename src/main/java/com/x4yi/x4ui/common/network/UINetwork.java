package com.x4yi.x4ui.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public final class UINetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("x4ui");
    public static final String INTERNAL_CHANNEL_KEY = "__x4ui_channel";
    public static final String ACTION_UNSUBSCRIBE_CHANNEL = "__x4ui:unsubscribe_channel";

    private static final class ActionEntry {
        final IServerActionHandler handler;
        final ActionGuard guard;
        ActionEntry(IServerActionHandler handler, @Nullable ActionGuard guard) {
            this.handler = handler;
            this.guard = guard;
        }
    }

    private static final class RpcEntry {
        final IServerRpcHandler handler;
        final ActionGuard guard;
        RpcEntry(IServerRpcHandler handler, @Nullable ActionGuard guard) {
            this.handler = handler;
            this.guard = guard;
        }
    }

    private static final class PendingRpc {
        final Consumer<RpcResult> callback;
        final long expiresAtMs;
        PendingRpc(Consumer<RpcResult> callback, long expiresAtMs) {
            this.callback = callback;
            this.expiresAtMs = expiresAtMs;
        }
    }

    private static final Map<String, ActionEntry> ACTION_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<String, RpcEntry> RPC_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<String, Long> RATE_LIMITS = new ConcurrentHashMap<>();
    private static final Map<Long, PendingRpc> PENDING_RPCS = new ConcurrentHashMap<>();
    private static final AtomicLong NEXT_RPC_ID = new AtomicLong(1L);

    private static volatile boolean initialized = false;
    private static volatile boolean serverSupported = false;
    private static volatile String remoteServerVersion = null;
    private static volatile long defaultRateLimitMs = 50L;

    private UINetwork() {}

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        int id = 0;
        CHANNEL.registerMessage(PacketHandshake.Handler.class, PacketHandshake.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketHandshake.Handler.class, PacketHandshake.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketGuiAction.Handler.class, PacketGuiAction.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketStateSync.Handler.class, PacketStateSync.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketServerCommand.Handler.class, PacketServerCommand.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketStateBatch.Handler.class, PacketStateBatch.class, id++, Side.CLIENT);
        CHANNEL.registerMessage(PacketGuiRpc.Handler.class, PacketGuiRpc.class, id++, Side.SERVER);
        CHANNEL.registerMessage(PacketGuiRpc.Handler.class, PacketGuiRpc.class, id++, Side.CLIENT);

        registerActionHandler(ACTION_UNSUBSCRIBE_CHANNEL, (player, actionId, data) -> {
            if (data != null && data.hasKey(INTERNAL_CHANNEL_KEY)) {
                UIChannels.unsubscribe(player, data.getString(INTERNAL_CHANNEL_KEY));
            }
        });
    }

    public static void setServerSupported(boolean supported, @Nullable String version) {
        serverSupported = supported;
        remoteServerVersion = supported ? version : null;
        if (!supported) {
            cancelAllPendingRpcs();
        }
    }

    public static boolean isServerSupported() {
        return serverSupported;
    }

    public static String getRemoteServerVersion() {
        return remoteServerVersion;
    }

    public static boolean hasClient(EntityPlayerMP player) {
        return ClientSessionTracker.INSTANCE.hasClient(player);
    }

    public static void setDefaultRateLimitMs(long cooldownMs) {
        defaultRateLimitMs = Math.max(0L, cooldownMs);
    }

    public static long getDefaultRateLimitMs() {
        return defaultRateLimitMs;
    }

    public static void registerActionHandler(String actionId, IServerActionHandler handler) {
        registerActionHandler(actionId, null, handler);
    }

    public static void registerActionHandler(String actionId, @Nullable ActionGuard guard, IServerActionHandler handler) {
        if (actionId != null && !actionId.isEmpty() && handler != null) {
            ACTION_HANDLERS.put(actionId, new ActionEntry(handler, guard));
        }
    }

    public static IServerActionHandler getActionHandler(String actionId) {
        ActionEntry entry = actionId != null ? ACTION_HANDLERS.get(actionId) : null;
        return entry != null ? entry.handler : null;
    }

    public static void onRpc(String rpcId, IServerRpcHandler handler) {
        onRpc(rpcId, null, handler);
    }

    public static void onRpc(String rpcId, @Nullable ActionGuard guard, IServerRpcHandler handler) {
        if (rpcId != null && !rpcId.isEmpty() && handler != null) {
            RPC_HANDLERS.put(rpcId, new RpcEntry(handler, guard));
        }
    }

    public static boolean checkRateLimit(EntityPlayerMP player, String actionOrRpcId) {
        if (player == null || actionOrRpcId == null) return false;
        if (ACTION_UNSUBSCRIBE_CHANNEL.equals(actionOrRpcId)) return true;
        long cooldown = defaultRateLimitMs;
        if (cooldown <= 0L) return true;
        String key = player.getUniqueID().toString() + "|" + actionOrRpcId;
        long now = System.currentTimeMillis();
        Long last = RATE_LIMITS.get(key);
        if (last != null && (now - last) < cooldown) {
            return false;
        }
        RATE_LIMITS.put(key, now);
        return true;
    }

    public static void clearRateLimits(UUID playerId) {
        if (playerId == null) return;
        String prefix = playerId.toString() + "|";
        RATE_LIMITS.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public static void dispatchActionOnServer(EntityPlayerMP player, String actionId, @Nullable NBTTagCompound data) {
        if (player == null || actionId == null || actionId.isEmpty()) return;
        if (!checkRateLimit(player, actionId)) return;
        if (player.openContainer instanceof IServerActionHandler) {
            ((IServerActionHandler) player.openContainer).onAction(player, actionId, data);
        }
        ActionEntry entry = ACTION_HANDLERS.get(actionId);
        if (entry != null) {
            if (entry.guard == null || entry.guard.allow(player, data)) {
                entry.handler.onAction(player, actionId, data);
            }
        }
    }

    public static RpcResult dispatchRpcOnServer(EntityPlayerMP player, String rpcId, @Nullable NBTTagCompound params) {
        if (player == null || rpcId == null || rpcId.isEmpty()) {
            return RpcResult.error("INVALID_REQUEST", "Missing player or rpcId");
        }
        if (!checkRateLimit(player, rpcId)) {
            return RpcResult.error("RATE_LIMITED", "Too many requests");
        }
        RpcEntry entry = RPC_HANDLERS.get(rpcId);
        if (entry == null) {
            return RpcResult.error("UNKNOWN_RPC", "No RPC handler registered for: " + rpcId);
        }
        if (entry.guard != null && !entry.guard.allow(player, params)) {
            return RpcResult.error("FORBIDDEN", "ActionGuard rejected request");
        }
        try {
            RpcResult result = entry.handler.handle(player, params != null ? params : new NBTTagCompound());
            return result != null ? result : RpcResult.ok();
        } catch (Exception e) {
            return RpcResult.error("INTERNAL_ERROR", e.getMessage() != null ? e.getMessage() : "Server error");
        }
    }

    public static void sendActionToServer(String actionId, @Nullable NBTTagCompound data) {
        if (!serverSupported || actionId == null || actionId.isEmpty()) {
            return;
        }
        CHANNEL.sendToServer(new PacketGuiAction(actionId, data));
    }

    public static void sendActionOrLocal(String actionId, @Nullable NBTTagCompound data, @Nullable Runnable localFallback) {
        if (serverSupported) {
            sendActionToServer(actionId, data);
        } else if (localFallback != null) {
            localFallback.run();
        }
    }

    public static long request(String rpcId, @Nullable NBTTagCompound params, Consumer<RpcResult> callback) {
        return request(rpcId, params, 5000L, callback);
    }

    public static long request(String rpcId, @Nullable NBTTagCompound params, long timeoutMs, Consumer<RpcResult> callback) {
        if (callback == null || rpcId == null || rpcId.isEmpty()) {
            return -1L;
        }
        if (!serverSupported) {
            callback.accept(RpcResult.error("NO_SERVER_SUPPORT", "Server does not have x4ui installed"));
            return -1L;
        }
        long id = NEXT_RPC_ID.getAndIncrement();
        long expires = System.currentTimeMillis() + Math.max(100L, timeoutMs);
        PENDING_RPCS.put(id, new PendingRpc(callback, expires));
        CHANNEL.sendToServer(PacketGuiRpc.request(id, rpcId, params));
        return id;
    }

    public static void cancelPendingRpc(long requestId) {
        if (requestId > 0L) {
            PENDING_RPCS.remove(requestId);
        }
    }

    public static void cancelAllPendingRpcs() {
        PENDING_RPCS.clear();
    }

    public static void sweepExpiredRpcs() {
        if (PENDING_RPCS.isEmpty()) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Long, PendingRpc>> it = PENDING_RPCS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, PendingRpc> entry = it.next();
            if (now >= entry.getValue().expiresAtMs) {
                it.remove();
                entry.getValue().callback.accept(RpcResult.error("TIMEOUT", "RPC request timed out"));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    static void completeRpcOnClient(long requestId, RpcResult result) {
        PendingRpc pending = PENDING_RPCS.remove(requestId);
        if (pending == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.addScheduledTask(() -> pending.callback.accept(result));
        } else {
            pending.callback.accept(result);
        }
    }

    public static void syncState(EntityPlayerMP player, String key, Object value) {
        if (player != null && key != null && !key.isEmpty()) {
            CHANNEL.sendTo(new PacketStateSync(key, value), player);
        }
    }

    public static void syncBatch(EntityPlayerMP player, Map<String, Object> entries) {
        if (player != null && entries != null && !entries.isEmpty()) {
            CHANNEL.sendTo(new PacketStateBatch(entries), player);
        }
    }

    public static void openScreen(EntityPlayerMP player, String screenId, @Nullable NBTTagCompound data) {
        if (player != null && screenId != null && !screenId.isEmpty()) {
            CHANNEL.sendTo(new PacketServerCommand(PacketServerCommand.CMD_OPEN_SCREEN, screenId, data), player);
        }
    }

    public static void openChannelScreen(EntityPlayerMP player, String screenId, String channelId, @Nullable NBTTagCompound data) {
        if (player == null || screenId == null || screenId.isEmpty()) return;
        NBTTagCompound payload = data != null ? data.copy() : new NBTTagCompound();
        if (channelId != null && !channelId.isEmpty()) {
            UIChannels.subscribe(player, channelId);
            payload.setString(INTERNAL_CHANNEL_KEY, channelId);
        }
        openScreen(player, screenId, payload);
    }

    public static void closeScreen(EntityPlayerMP player) {
        if (player != null) {
            CHANNEL.sendTo(new PacketServerCommand(PacketServerCommand.CMD_CLOSE_SCREEN, "", null), player);
        }
    }

    public static void sendToast(EntityPlayerMP player, String title, String message, float durationSeconds) {
        if (player == null) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("title", title != null ? title : "");
        tag.setString("message", message != null ? message : "");
        tag.setFloat("duration", durationSeconds);
        CHANNEL.sendTo(new PacketServerCommand(PacketServerCommand.CMD_SHOW_TOAST, "toast", tag), player);
    }
}

