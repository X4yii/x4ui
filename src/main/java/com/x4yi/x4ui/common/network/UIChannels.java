package com.x4yi.x4ui.common.network;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class UIChannels {
    private static final Map<String, Set<EntityPlayerMP>> CHANNELS = new ConcurrentHashMap<>();

    private UIChannels() {}

    public static void subscribe(EntityPlayerMP player, String channelId) {
        if (player == null || channelId == null || channelId.isEmpty()) {
            return;
        }
        CHANNELS.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(player);
    }

    public static void unsubscribe(EntityPlayerMP player, String channelId) {
        if (player == null || channelId == null || channelId.isEmpty()) {
            return;
        }
        Set<EntityPlayerMP> subs = CHANNELS.get(channelId);
        if (subs != null) {
            subs.remove(player);
            if (subs.isEmpty()) {
                CHANNELS.remove(channelId, subs);
            }
        }
    }

    public static void unsubscribeAll(EntityPlayerMP player) {
        if (player == null) return;
        for (Map.Entry<String, Set<EntityPlayerMP>> entry : CHANNELS.entrySet()) {
            Set<EntityPlayerMP> subs = entry.getValue();
            subs.remove(player);
            if (subs.isEmpty()) {
                CHANNELS.remove(entry.getKey(), subs);
            }
        }
    }

    public static Set<EntityPlayerMP> getSubscribers(String channelId) {
        if (channelId == null) return Collections.emptySet();
        Set<EntityPlayerMP> subs = CHANNELS.get(channelId);
        return subs != null ? Collections.unmodifiableSet(subs) : Collections.emptySet();
    }

    public static void broadcastState(String channelId, String key, Object value) {
        Set<EntityPlayerMP> subs = CHANNELS.get(channelId);
        if (subs == null || subs.isEmpty() || key == null || key.isEmpty()) return;
        for (EntityPlayerMP player : subs) {
            if (UINetwork.hasClient(player)) {
                UINetwork.syncState(player, key, value);
            }
        }
    }

    public static void broadcastBatch(String channelId, Map<String, Object> entries) {
        Set<EntityPlayerMP> subs = CHANNELS.get(channelId);
        if (subs == null || subs.isEmpty() || entries == null || entries.isEmpty()) return;
        for (EntityPlayerMP player : subs) {
            if (UINetwork.hasClient(player)) {
                UINetwork.syncBatch(player, entries);
            }
        }
    }

    public static void broadcastToast(String channelId, String title, String message, float durationSeconds) {
        Set<EntityPlayerMP> subs = CHANNELS.get(channelId);
        if (subs == null || subs.isEmpty()) return;
        for (EntityPlayerMP player : subs) {
            if (UINetwork.hasClient(player)) {
                UINetwork.sendToast(player, title, message, durationSeconds);
            }
        }
    }

    public static void closeChannelScreens(String channelId) {
        Set<EntityPlayerMP> subs = CHANNELS.get(channelId);
        if (subs == null || subs.isEmpty()) return;
        for (EntityPlayerMP player : subs) {
            if (UINetwork.hasClient(player)) {
                UINetwork.closeScreen(player);
            }
        }
        CHANNELS.remove(channelId);
    }

    public static void clearAll() {
        CHANNELS.clear();
    }
}

