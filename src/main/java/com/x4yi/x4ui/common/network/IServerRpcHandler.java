package com.x4yi.x4ui.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

@FunctionalInterface
public interface IServerRpcHandler {
    RpcResult handle(EntityPlayerMP player, NBTTagCompound params);
}

