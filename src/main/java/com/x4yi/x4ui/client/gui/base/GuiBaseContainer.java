package com.x4yi.x4ui.client.gui.base;

import com.x4yi.x4ui.client.gui.component.GuiPanel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public abstract class GuiBaseContainer extends GuiContainer {
    
    // The root UI panel that contains everything
    protected GuiPanel rootPanel;
    
    // Compatibility
    protected float animationProgress = 0.0f;
    protected boolean isAnimating = true;
    protected static final float ANIMATION_SPEED = 0.15f;
    
    public GuiBaseContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void initGui() {
        super.initGui();
        GuiBaseScreen.currentScaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        // The root panel always spans the entire screen
        rootPanel = new GuiPanel(0, 0, width, height);
        initComponents();
    }

    /**
     * Override this to add your components to rootPanel
     */
    protected abstract void initComponents();

    @Override
    public void drawDefaultBackground() {
        // Do nothing to prevent super.drawScreen from double-darkening and scaling the background quad
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (rootPanel != null) {
            rootPanel.update(); // Update animations and layouts
        }
        
        updateAnimation();
        
        super.drawDefaultBackground(); // Draw it ONCE, full screen, unscaled
        
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        if (animationProgress < 1.0f) {
            float scale = 0.9f + (0.1f * animationProgress);
            net.minecraft.client.renderer.GlStateManager.translate(width / 2.0f, height / 2.0f, 0);
            net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
            net.minecraft.client.renderer.GlStateManager.translate(-width / 2.0f, -height / 2.0f, 0);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
        
        net.minecraft.client.renderer.GlStateManager.popMatrix();
        
        this.renderHoveredToolTip(mouseX, mouseY); // Draw tooltips unscaled, on top
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (rootPanel != null) {
            rootPanel.drawComponent(mouseX, mouseY, partialTicks);
        }
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
                int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
                int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
                if (rootPanel.handleMouseScroll(mouseX, mouseY, wheel)) return;
            }
            rootPanel.handleMouseInput();
        }
    }

    protected void updateAnimation() {
        if (isAnimating) {
            animationProgress += ANIMATION_SPEED;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                isAnimating = false;
            }
        }
    }

    // Convenience method
    protected void addComponent(com.x4yi.x4ui.client.gui.component.GuiComponent component) {
        if (rootPanel != null) {
            rootPanel.addChild(component);
        }
    }
}
