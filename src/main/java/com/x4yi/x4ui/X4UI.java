package com.x4yi.x4ui;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.SidedProxy;
import com.x4yi.x4ui.proxy.CommonProxy;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import java.util.Map;
@Mod(modid = X4UI.MODID, name = X4UI.NAME, version = X4UI.VERSION, acceptableRemoteVersions = "*")
public class X4UI {
    public static final String MODID = "x4ui";
    public static final String NAME = "X4UI";
    public static final String VERSION = "r1.0b6";
    @NetworkCheckHandler
    public boolean checkRemoteVersions(Map<String, String> remoteVersions, Side side) {
        if (side == Side.SERVER) {
            return true;
        }
        return remoteVersions.containsKey(MODID);
    }
    @SidedProxy(clientSide = "com.x4yi.x4ui.proxy.ClientProxy", serverSide = "com.x4yi.x4ui.proxy.CommonProxy")
    public static CommonProxy proxy;
    private static Logger logger;
    public static Logger getLogger() {
        return logger;
    }
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("X4UI {} Pre-Initialization on {} (VirtualThreads={}, LWJGL3={})...",
                VERSION,
                com.x4yi.x4ui.common.platform.PlatformBridge.getRuntimeDescription(),
                com.x4yi.x4ui.common.platform.PlatformBridge.isVirtualThreadsSupported(),
                com.x4yi.x4ui.common.platform.PlatformBridge.isLwjgl3());
        proxy.preInit(event);
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("X4UI Initialization...");
        proxy.init(event);
    }
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("X4UI Post-Initialization...");
        proxy.postInit(event);
    }
    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        proxy.loadComplete(event);
    }
    public static com.x4yi.x4ui.api.client.media.IMediaService getMediaService() {
        return proxy.getMediaService();
    }
}