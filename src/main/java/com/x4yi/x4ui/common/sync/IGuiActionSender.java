package com.x4yi.x4ui.common.sync;
import net.minecraft.nbt.NBTTagCompound;
import javax.annotation.Nullable;
public interface IGuiActionSender {
    void sendActionToServer(String actionId, @Nullable NBTTagCompound data);
}