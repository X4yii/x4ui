package com.x4yi.x4ui.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientSessionTracker {
    public static final ClientSessionTracker INSTANCE = new ClientSessionTracker();
    private final Map<UUID, String> clientVersions = new ConcurrentHashMap<>();

    private ClientSessionTracker() {}

    public void markClientSupported(UUID playerId, String version) {
        if (playerId != null) {
            clientVersions.put(playerId, version != null ? version : "unknown");
        }
    }

    public boolean hasClient(EntityPlayerMP player) {
        return player != null && clientVersions.containsKey(player.getUniqueID());
    }

    public String getClientVersion(EntityPlayerMP player) {
        return player != null ? clientVersions.get(player.getUniqueID()) : null;
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) event.player;
            UINetwork.CHANNEL.sendTo(new PacketHandshake(com.x4yi.x4ui.X4UI.VERSION), mp);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player != null) {
            UUID id = event.player.getUniqueID();
            clientVersions.remove(id);
            UINetwork.clearRateLimits(id);
            if (event.player instanceof EntityPlayerMP) {
                UIChannels.unsubscribeAll((EntityPlayerMP) event.player);
            }
        }
    }
}

