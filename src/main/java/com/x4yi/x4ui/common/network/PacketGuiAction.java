package com.x4yi.x4ui.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketGuiAction implements IMessage {
    private String actionId;
    private NBTTagCompound data;

    public PacketGuiAction() {}

    public PacketGuiAction(String actionId, NBTTagCompound data) {
        this.actionId = actionId != null ? actionId : "";
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.actionId = UINetwork.readBoundedUtf8(buf, 128);
        if (buf.readBoolean()) {
            this.data = ByteBufUtils.readTag(buf);
        } else {
            this.data = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, actionId != null ? actionId : "");
        if (data != null) {
            buf.writeBoolean(true);
            ByteBufUtils.writeTag(buf, data);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static class Handler implements IMessageHandler<PacketGuiAction, IMessage> {
        @Override
        public IMessage onMessage(PacketGuiAction message, MessageContext ctx) {
            if (ctx.side != Side.SERVER) return null;
            final EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null || player.world == null || message.actionId == null || message.actionId.isEmpty()) return null;
            player.getServerWorld().addScheduledTask(() -> {
                if (!player.hasDisconnected()) {
                    UINetwork.dispatchActionOnServer(player, message.actionId, message.data);
                }
            });
            return null;
        }
    }
}

