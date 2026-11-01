package com.x4yi.x4ui.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public final class ScreenRegistry {
    private static final Logger LOGGER = LogManager.getLogger("x4ui");
    private static final Map<String, Function<NBTTagCompound, GuiScreen>> FACTORIES = new ConcurrentHashMap<>();

    private ScreenRegistry() {}

    public static void register(String screenId, Function<NBTTagCompound, GuiScreen> factory) {
        if (screenId == null || screenId.isEmpty() || factory == null) {
            return;
        }
        if (FACTORIES.containsKey(screenId)) {
            LOGGER.warn("Overwriting registered screen factory for id '{}'", screenId);
        }
        FACTORIES.put(screenId, factory);
    }

    public static void unregister(String screenId) {
        if (screenId != null) {
            FACTORIES.remove(screenId);
        }
    }

    public static boolean has(String screenId) {
        return screenId != null && FACTORIES.containsKey(screenId);
    }

    @Nullable
    public static GuiScreen create(String screenId, @Nullable NBTTagCompound payload) {
        if (screenId == null || screenId.isEmpty()) {
            return null;
        }
        Function<NBTTagCompound, GuiScreen> factory = FACTORIES.get(screenId);
        if (factory == null) {
            LOGGER.warn("No client screen factory registered for server openScreen id '{}'", screenId);
            return null;
        }
        return factory.apply(payload != null ? payload : new NBTTagCompound());
    }
}
