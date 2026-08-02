package com.x4yi.x4ui;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
@Mod(modid = X4UI.MODID, name = X4UI.NAME, version = X4UI.VERSION)
public class X4UI {
    public static final String MODID = "x4ui";
    public static final String NAME = "X4yi's UI";
    public static final String VERSION = "1.0.0";
    private static Logger logger;
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("X4UI Pre-Initialization...");
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("X4UI Initialization...");
    }
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("X4UI Post-Initialization...");
    }
}