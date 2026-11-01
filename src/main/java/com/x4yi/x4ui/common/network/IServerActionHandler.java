package com.x4yi.x4ui.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import javax.annotation.Nullable;

@FunctionalInterface
public interface IServerActionHandler {
    void onAction(EntityPlayerMP player, String actionId, @Nullable NBTTagCompound data);
}

