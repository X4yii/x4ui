package com.x4yi.x4ui.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public class PacketGuiRpc implements IMessage {
    private boolean isResponse;
    private long requestId;
    private String rpcId;
    private boolean ok;
    private String errorCode;
    private String errorMessage;
    private NBTTagCompound payload;

    public PacketGuiRpc() {}

    public static PacketGuiRpc request(long requestId, String rpcId, @Nullable NBTTagCompound params) {
        PacketGuiRpc msg = new PacketGuiRpc();
        msg.isResponse = false;
        msg.requestId = requestId;
        msg.rpcId = rpcId != null ? rpcId : "";
        msg.payload = params;
        return msg;
    }

    public static PacketGuiRpc response(long requestId, RpcResult result) {
        PacketGuiRpc msg = new PacketGuiRpc();
        msg.isResponse = true;
        msg.requestId = requestId;
        msg.ok = result != null && result.isOk();
        msg.errorCode = result != null ? result.getErrorCode() : "INTERNAL_ERROR";
        msg.errorMessage = result != null ? result.getErrorMessage() : "Null RPC result";
        msg.payload = result != null ? result.getData() : null;
        return msg;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isResponse = buf.readBoolean();
        this.requestId = buf.readLong();
        if (!this.isResponse) {
            this.rpcId = UINetwork.readBoundedUtf8(buf, 128);
            this.payload = buf.readBoolean() ? ByteBufUtils.readTag(buf) : null;
        } else {
            this.ok = buf.readBoolean();
            this.errorCode = UINetwork.readBoundedUtf8(buf, 64);
            this.errorMessage = UINetwork.readBoundedUtf8(buf, 512);
            this.payload = buf.readBoolean() ? ByteBufUtils.readTag(buf) : null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isResponse);
        buf.writeLong(requestId);
        if (!isResponse) {
            ByteBufUtils.writeUTF8String(buf, rpcId != null ? rpcId : "");
            if (payload != null) {
                buf.writeBoolean(true);
                ByteBufUtils.writeTag(buf, payload);
            } else {
                buf.writeBoolean(false);
            }
        } else {
            buf.writeBoolean(ok);
            ByteBufUtils.writeUTF8String(buf, errorCode != null ? errorCode : "");
            ByteBufUtils.writeUTF8String(buf, errorMessage != null ? errorMessage : "");
            if (payload != null) {
                buf.writeBoolean(true);
                ByteBufUtils.writeTag(buf, payload);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public static class Handler implements IMessageHandler<PacketGuiRpc, IMessage> {
        @Override
        public IMessage onMessage(PacketGuiRpc message, MessageContext ctx) {
            if (ctx.side == Side.SERVER && !message.isResponse) {
                final EntityPlayerMP player = ctx.getServerHandler().player;
                if (player == null || player.world == null || message.rpcId == null || message.rpcId.isEmpty()) {
                    return null;
                }
                player.getServerWorld().addScheduledTask(() -> {
                    if (player.hasDisconnected()) return;
                    RpcResult res = UINetwork.dispatchRpcOnServer(player, message.rpcId, message.payload);
                    UINetwork.CHANNEL.sendTo(PacketGuiRpc.response(message.requestId, res), player);
                });
            } else if (ctx.side == Side.CLIENT && message.isResponse) {
                RpcResult res = message.ok
                        ? RpcResult.ok(message.payload)
                        : RpcResult.error(message.errorCode, message.errorMessage);
                com.x4yi.x4ui.X4UI.proxy.handleClientRpcResponse(message.requestId, res);
            }
            return null;
        }
    }
}

