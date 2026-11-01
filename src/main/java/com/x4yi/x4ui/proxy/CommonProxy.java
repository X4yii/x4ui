package com.x4yi.x4ui.proxy;

import com.x4yi.x4ui.api.client.media.IMediaService;
import com.x4yi.x4ui.common.network.ClientSessionTracker;
import com.x4yi.x4ui.common.network.UINetwork;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        UINetwork.init();
        MinecraftForge.EVENT_BUS.register(ClientSessionTracker.INSTANCE);
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
    }

    public IMediaService getMediaService() {
        return null;
    }

    public void handleClientStateSync(String key, Object value) {
    }

    public void handleClientCommand(byte command, String targetId, NBTTagCompound payload) {
    }
}