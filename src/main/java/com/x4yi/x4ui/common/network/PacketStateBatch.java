package com.x4yi.x4ui.common.network;

import com.x4yi.x4ui.X4UI;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PacketStateBatch implements IMessage {
    private static final int MAX_ENTRIES = 1024;
    private Map<String, Object> entries;

    public PacketStateBatch() {
        this.entries = Collections.emptyMap();
    }

    public PacketStateBatch(Map<String, Object> entries) {
        this.entries = entries != null ? new LinkedHashMap<>(entries) : Collections.emptyMap();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = Math.min(Math.max(0, buf.readInt()), MAX_ENTRIES);
        Map<String, Object> map = new LinkedHashMap<>(count);
        for (int i = 0; i < count; i++) {
            String key = ByteBufUtils.readUTF8String(buf);
            byte type = buf.readByte();
            Object val = readTypedValue(buf, type);
            if (!key.isEmpty()) {
                map.put(key, val);
            }
        }
        this.entries = map;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int count = Math.min(entries.size(), MAX_ENTRIES);
        buf.writeInt(count);
        int written = 0;
        for (Map.Entry<String, Object> entry : entries.entrySet()) {
            if (written >= count) break;
            String key = entry.getKey() != null ? entry.getKey() : "";
            Object val = entry.getValue();
            byte type = resolveType(val);
            ByteBufUtils.writeUTF8String(buf, key);
            buf.writeByte(type);
            writeTypedValue(buf, type, val);
            written++;
        }
    }

    private static byte resolveType(Object value) {
        if (value instanceof Integer) return PacketStateSync.TYPE_INT;
        if (value instanceof Long) return PacketStateSync.TYPE_LONG;
        if (value instanceof Float) return PacketStateSync.TYPE_FLOAT;
        if (value instanceof Double) return PacketStateSync.TYPE_DOUBLE;
        if (value instanceof Boolean) return PacketStateSync.TYPE_BOOLEAN;
        if (value instanceof NBTTagCompound) return PacketStateSync.TYPE_NBT;
        return PacketStateSync.TYPE_STRING;
    }

    private static Object readTypedValue(ByteBuf buf, byte type) {
        switch (type) {
            case PacketStateSync.TYPE_INT: return buf.readInt();
            case PacketStateSync.TYPE_LONG: return buf.readLong();
            case PacketStateSync.TYPE_FLOAT: return buf.readFloat();
            case PacketStateSync.TYPE_DOUBLE: return buf.readDouble();
            case PacketStateSync.TYPE_BOOLEAN: return buf.readBoolean();
            case PacketStateSync.TYPE_NBT: return ByteBufUtils.readTag(buf);
            case PacketStateSync.TYPE_STRING:
            default:
                return ByteBufUtils.readUTF8String(buf);
        }
    }

    private static void writeTypedValue(ByteBuf buf, byte type, Object value) {
        switch (type) {
            case PacketStateSync.TYPE_INT: buf.writeInt((Integer) value); break;
            case PacketStateSync.TYPE_LONG: buf.writeLong((Long) value); break;
            case PacketStateSync.TYPE_FLOAT: buf.writeFloat((Float) value); break;
            case PacketStateSync.TYPE_DOUBLE: buf.writeDouble((Double) value); break;
            case PacketStateSync.TYPE_BOOLEAN: buf.writeBoolean((Boolean) value); break;
            case PacketStateSync.TYPE_NBT: ByteBufUtils.writeTag(buf, (NBTTagCompound) value); break;
            case PacketStateSync.TYPE_STRING:
            default:
                ByteBufUtils.writeUTF8String(buf, value != null ? value.toString() : "");
                break;
        }
    }

    public static class Handler implements IMessageHandler<PacketStateBatch, IMessage> {
        @Override
        public IMessage onMessage(PacketStateBatch message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT && message.entries != null && !message.entries.isEmpty()) {
                for (Map.Entry<String, Object> e : message.entries.entrySet()) {
                    X4UI.proxy.handleClientStateSync(e.getKey(), e.getValue());
                }
            }
            return null;
        }
    }
}

