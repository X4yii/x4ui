package com.x4yi.x4ui.common.network;

import com.x4yi.x4ui.X4UI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketServerCommand implements IMessage {
    public static final byte CMD_OPEN_SCREEN = 0;
    public static final byte CMD_CLOSE_SCREEN = 1;
    public static final byte CMD_SHOW_TOAST = 2;

    private byte command;
    private String targetId;
    private NBTTagCompound payload;

    public PacketServerCommand() {}

    public PacketServerCommand(byte command, String targetId, NBTTagCompound payload) {
        this.command = command;
        this.targetId = targetId != null ? targetId : "";
        this.payload = payload;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.command = buf.readByte();
        this.targetId = ByteBufUtils.readUTF8String(buf);
        if (buf.readBoolean()) {
            this.payload = ByteBufUtils.readTag(buf);
        } else {
            this.payload = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(command);
        ByteBufUtils.writeUTF8String(buf, targetId != null ? targetId : "");
        if (payload != null) {
            buf.writeBoolean(true);
            ByteBufUtils.writeTag(buf, payload);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static class Handler implements IMessageHandler<PacketServerCommand, IMessage> {
        @Override
        public IMessage onMessage(PacketServerCommand message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                X4UI.proxy.handleClientCommand(message.command, message.targetId, message.payload);
            }
            return null;
        }
    }
}

