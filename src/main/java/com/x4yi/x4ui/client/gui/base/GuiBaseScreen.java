package com.x4yi.x4ui.client.gui.base;

import com.x4yi.x4ui.client.gui.component.GuiPanel;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;

public abstract class GuiBaseScreen extends GuiScreen {
    protected final GuiScreen parentScreen;
    protected final String title;
    
    // The root UI panel that contains everything
    protected GuiPanel rootPanel;
    
    // Compatibility
    protected float animationProgress = 0.0f;
    protected boolean isAnimating = true;
    protected static final float ANIMATION_SPEED = 0.15f;
    protected java.util.List<com.x4yi.x4ui.client.gui.component.GuiComponent> components = new java.util.ArrayList<com.x4yi.x4ui.client.gui.component.GuiComponent>() {
        @Override
        public void clear() {
            super.clear();
            if (rootPanel != null) rootPanel.clearChildren();
        }
        @Override
        public boolean add(com.x4yi.x4ui.client.gui.component.GuiComponent e) {
            boolean r = super.add(e);
            if (rootPanel != null) rootPanel.addChild(e);
            return r;
        }
    };

    public GuiBaseScreen(GuiScreen parent, String title) {
        this.parentScreen = parent;
        this.title = title;
    }

    @Override
    public void initGui() {
        super.initGui();
        // The root panel always spans the entire screen
        rootPanel = new GuiPanel(0, 0, width, height);
        initComponents();
    }

    /**
     * Override this to add your components to rootPanel
     */
    protected abstract void initComponents();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (rootPanel != null) {
            rootPanel.update(); // Update animations and layouts
            rootPanel.drawComponent(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (rootPanel != null) {
            rootPanel.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (rootPanel != null) {
            rootPanel.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (rootPanel != null) {
            rootPanel.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (rootPanel != null) {
            if (rootPanel.keyTyped(typedChar, keyCode)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (rootPanel != null) {
            if (wheel != 0) {
                if (rootPanel.handleMouseScroll(wheel)) return;
            }
            rootPanel.handleMouseInput();
        }
    }

    public void closeScreen() {
        mc.displayGuiScreen(parentScreen);
    }
    
    protected void drawTitle() {}
    protected void drawBackground(int mouseX, int mouseY, float partialTicks) {}
    
    protected void updateAnimation() {
        if (isAnimating) {
            animationProgress += ANIMATION_SPEED;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                isAnimating = false;
            }
        }
    }

    // Compatibility with old framework
    protected void addComponent(com.x4yi.x4ui.client.gui.component.GuiComponent component) {
        if (rootPanel != null) {
            rootPanel.addChild(component);
        }
    }

    protected void removeComponent(com.x4yi.x4ui.client.gui.component.GuiComponent component) {
        if (rootPanel != null) {
            rootPanel.removeChild(component);
        }
    }
}
