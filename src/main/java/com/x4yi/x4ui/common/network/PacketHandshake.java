package com.x4yi.x4ui.common.network;

import com.x4yi.x4ui.X4UI;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandshake implements IMessage {
    private String version;

    public PacketHandshake() {}

    public PacketHandshake(String version) {
        this.version = version;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.version = UINetwork.readBoundedUtf8(buf, 64);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, version != null ? version : X4UI.VERSION);
    }

    public static class Handler implements IMessageHandler<PacketHandshake, IMessage> {
        @Override
        public IMessage onMessage(PacketHandshake message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                UINetwork.setServerSupported(true, message.version);
                return new PacketHandshake(X4UI.VERSION);
            } else {
                EntityPlayerMP player = ctx.getServerHandler().player;
                if (player != null && player.world != null) {
                    player.getServerWorld().addScheduledTask(() -> {
                        if (!player.hasDisconnected()) {
                            ClientSessionTracker.INSTANCE.markClientSupported(player.getUniqueID(), message.version);
                        }
                    });
                }
                return null;
            }
        }
    }
}

