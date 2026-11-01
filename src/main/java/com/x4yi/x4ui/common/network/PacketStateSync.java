package com.x4yi.x4ui.common.network;

import com.x4yi.x4ui.X4UI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketStateSync implements IMessage {
    public static final byte TYPE_INT = 0;
    public static final byte TYPE_LONG = 1;
    public static final byte TYPE_FLOAT = 2;
    public static final byte TYPE_DOUBLE = 3;
    public static final byte TYPE_BOOLEAN = 4;
    public static final byte TYPE_STRING = 5;
    public static final byte TYPE_NBT = 6;

    private String key;
    private byte type;
    private Object value;

    public PacketStateSync() {}

    public PacketStateSync(String key, Object value) {
        this.key = key != null ? key : "";
        this.value = value;
        if (value instanceof Integer) this.type = TYPE_INT;
        else if (value instanceof Long) this.type = TYPE_LONG;
        else if (value instanceof Float) this.type = TYPE_FLOAT;
        else if (value instanceof Double) this.type = TYPE_DOUBLE;
        else if (value instanceof Boolean) this.type = TYPE_BOOLEAN;
        else if (value instanceof NBTTagCompound) this.type = TYPE_NBT;
        else {
            this.type = TYPE_STRING;
            this.value = value != null ? value.toString() : "";
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.key = ByteBufUtils.readUTF8String(buf);
        this.type = buf.readByte();
        switch (this.type) {
            case TYPE_INT: this.value = buf.readInt(); break;
            case TYPE_LONG: this.value = buf.readLong(); break;
            case TYPE_FLOAT: this.value = buf.readFloat(); break;
            case TYPE_DOUBLE: this.value = buf.readDouble(); break;
            case TYPE_BOOLEAN: this.value = buf.readBoolean(); break;
            case TYPE_NBT: this.value = ByteBufUtils.readTag(buf); break;
            case TYPE_STRING:
            default:
                this.value = ByteBufUtils.readUTF8String(buf);
                break;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, key != null ? key : "");
        buf.writeByte(type);
        switch (type) {
            case TYPE_INT: buf.writeInt((Integer) value); break;
            case TYPE_LONG: buf.writeLong((Long) value); break;
            case TYPE_FLOAT: buf.writeFloat((Float) value); break;
            case TYPE_DOUBLE: buf.writeDouble((Double) value); break;
            case TYPE_BOOLEAN: buf.writeBoolean((Boolean) value); break;
            case TYPE_NBT: ByteBufUtils.writeTag(buf, (NBTTagCompound) value); break;
            case TYPE_STRING:
            default:
                ByteBufUtils.writeUTF8String(buf, value != null ? value.toString() : "");
                break;
        }
    }

    public static class Handler implements IMessageHandler<PacketStateSync, IMessage> {
        @Override
        public IMessage onMessage(PacketStateSync message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT && message.key != null && !message.key.isEmpty()) {
                X4UI.proxy.handleClientStateSync(message.key, message.value);
            }
            return null;
        }
    }
}

