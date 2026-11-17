package com.x4yi.x4ui.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UIChannels {
    private static final Map<String, Set<UUID>> CHANNELS = new ConcurrentHashMap<>();

    private UIChannels() {}

    public static void subscribe(EntityPlayerMP player, String channelId) {
        if (player == null || channelId == null || channelId.isEmpty()) {
            return;
        }
        CHANNELS.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(player.getUniqueID());
    }

    public static void unsubscribe(EntityPlayerMP player, String channelId) {
        if (player == null || channelId == null || channelId.isEmpty()) {
            return;
        }
        unsubscribe(player.getUniqueID(), channelId);
    }

    public static void unsubscribe(UUID playerId, String channelId) {
        if (playerId == null || channelId == null || channelId.isEmpty()) {
            return;
        }
        Set<UUID> subs = CHANNELS.get(channelId);
        if (subs != null) {
            subs.remove(playerId);
            if (subs.isEmpty()) {
                CHANNELS.remove(channelId, subs);
            }
        }
    }

    public static void unsubscribeAll(EntityPlayerMP player) {
        if (player != null) {
            unsubscribeAll(player.getUniqueID());
        }
    }

    public static void unsubscribeAll(UUID playerId) {
        if (playerId == null) return;
        for (Map.Entry<String, Set<UUID>> entry : CHANNELS.entrySet()) {
            Set<UUID> subs = entry.getValue();
            subs.remove(playerId);
            if (subs.isEmpty()) {
                CHANNELS.remove(entry.getKey(), subs);
            }
        }
    }

    private static EntityPlayerMP resolveOnlinePlayer(UUID playerId) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.getPlayerList() == null) return null;
        return server.getPlayerList().getPlayerByUUID(playerId);
    }

    public static Set<EntityPlayerMP> getSubscribers(String channelId) {
        if (channelId == null) return Collections.emptySet();
        Set<UUID> ids = CHANNELS.get(channelId);
        if (ids == null || ids.isEmpty()) return Collections.emptySet();
        Set<EntityPlayerMP> result = new HashSet<>();
        for (UUID id : ids) {
            EntityPlayerMP mp = resolveOnlinePlayer(id);
            if (mp != null) {
                result.add(mp);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public static void broadcastState(String channelId, String key, Object value) {
        Set<UUID> ids = CHANNELS.get(channelId);
        if (ids == null || ids.isEmpty() || key == null || key.isEmpty()) return;
        for (UUID id : ids) {
            EntityPlayerMP player = resolveOnlinePlayer(id);
            if (player != null && UINetwork.hasClient(player)) {
                UINetwork.syncState(player, key, value);
            }
        }
    }

    public static void broadcastBatch(String channelId, Map<String, Object> entries) {
        Set<UUID> ids = CHANNELS.get(channelId);
        if (ids == null || ids.isEmpty() || entries == null || entries.isEmpty()) return;
        for (UUID id : ids) {
            EntityPlayerMP player = resolveOnlinePlayer(id);
            if (player != null && UINetwork.hasClient(player)) {
                UINetwork.syncBatch(player, entries);
            }
        }
    }

    public static void broadcastToast(String channelId, String title, String message, float durationSeconds) {
        Set<UUID> ids = CHANNELS.get(channelId);
        if (ids == null || ids.isEmpty()) return;
        for (UUID id : ids) {
            EntityPlayerMP player = resolveOnlinePlayer(id);
            if (player != null && UINetwork.hasClient(player)) {
                UINetwork.sendToast(player, title, message, durationSeconds);
            }
        }
    }

    public static void closeChannelScreens(String channelId) {
        Set<UUID> ids = CHANNELS.get(channelId);
        if (ids == null || ids.isEmpty()) return;
        for (UUID id : ids) {
            EntityPlayerMP player = resolveOnlinePlayer(id);
            if (player != null && UINetwork.hasClient(player)) {
                UINetwork.closeScreen(player);
            }
        }
        CHANNELS.remove(channelId);
    }

    public static void clearAll() {
        CHANNELS.clear();
    }
}
