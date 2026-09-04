package com.x4yi.x4ui.proxy;

import com.x4yi.x4ui.api.client.media.IMediaService;
import com.x4yi.x4ui.client.gui.animation.GuiAnimator;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import com.x4yi.x4ui.impl.client.media.MediaService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private IMediaService mediaService;

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MediaManager.setBinDir(event.getModConfigurationDirectory().getParentFile());
        this.mediaService = new MediaService();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        super.loadComplete(event);
        MediaManager.blockUntilDownloaded();
    }

    @Override
    public IMediaService getMediaService() {
        return mediaService;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GuiAnimator.update();
        }
    }
}
