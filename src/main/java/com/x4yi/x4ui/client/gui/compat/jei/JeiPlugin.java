package com.x4yi.x4ui.client.gui.compat.jei;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
@JEIPlugin
public class JeiPlugin extends BlankModPlugin {
    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new JeiAdvancedGuiHandler());
    }
}