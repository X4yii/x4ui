package com.x4yi.x4ui.client.gui.base;

import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.input.UIInput;
import com.x4yi.x4ui.client.gui.utils.ITheme;
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.network.RpcResult;
import com.x4yi.x4ui.impl.client.gui.GLGraphics;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;

public abstract class GuiBaseScreen extends GuiScreen {
    public static int currentScaleFactor = 1;
    public static float deltaTime = 0.0f;

    protected final GuiScreen parentScreen;
    protected final String title;
    protected final ScreenHost host = new ScreenHost();
    protected GuiPanel rootPanel;
    protected boolean dimBackground = true;
    protected int dimColor = 0xB0080A10;

    public GuiBaseScreen(GuiScreen parent, String title) {
        this.parentScreen = parent;
        this.title = title;
    }

    public GuiPanel getRootPanel() {
        return rootPanel;
    }

    public void bindChannel(String channelId) {
        host.bindChannel(channelId);
    }

    public void sendActionToServer(String actionId, @Nullable NBTTagCompound data) {
        host.sendActionToServer(actionId, data);
    }

    public void sendActionOrLocal(String actionId, @Nullable NBTTagCompound data, @Nullable Runnable localFallback) {
        host.sendActionOrLocal(actionId, data, localFallback);
    }

    public <T> State<T> syncedState(String key, T initialValue) {
        return host.syncedState(key, initialValue);
    }

    public long request(String rpcId, @Nullable NBTTagCompound params, Consumer<RpcResult> callback) {
        return host.request(rpcId, params, callback);
    }

    public void setTheme(ITheme theme) {
        if (rootPanel != null) rootPanel.setTheme(theme);
    }

    public void setDimBackground(boolean dimBackground) {
        this.dimBackground = dimBackground;
    }

    public void setDimColor(int dimColor) {
        this.dimColor = dimColor;
    }

    public void setAnimateOpen(boolean animateOpen) {
        host.setAnimateOpen(animateOpen);
    }

    protected GuiPanel createCenteredWindow(int maxWidth, int maxHeight) {
        return host.createCenteredWindow(maxWidth, maxHeight, this.width, this.height);
    }

    @Override
    public void initGui() {
        if (this.mc != null) {
            super.initGui();
            currentScaleFactor = new ScaledResolution(mc).getScaleFactor();
        }
        if (rootPanel == null) {
            rootPanel = new GuiPanel(0, 0, width, height);
            host.rootPanel = rootPanel;
            initComponents();
        } else {
            rootPanel.setWidth(width);
            rootPanel.setHeight(height);
            host.resizeManagedWindow(this.width, this.height);
            rootPanel.requestLayout();
            onScreenResize(width, height);
        }
    }

    protected void onScreenResize(int newWidth, int newHeight) {}

    protected abstract void initComponents();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float dt = host.beginFrame();
        if (rootPanel != null) {
            rootPanel.update(dt);
            GLGraphics gfx = GLGraphics.INSTANCE;
            gfx.reset();
            if (dimBackground) {
                gfx.drawRect(0, 0, this.width, this.height, dimColor);
            }
            float anim = host.getAnimationProgress();
            boolean scaled = anim < 1.0f;
            if (scaled) {
                GlStateManager.pushMatrix();
                float scale = 0.9f + (0.1f * anim);
                GlStateManager.translate(width / 2.0f, height / 2.0f, 0);
                GlStateManager.scale(scale, scale, 1.0f);
                GlStateManager.translate(-width / 2.0f, -height / 2.0f, 0);
            }
            rootPanel.render(gfx, mouseX, mouseY, partialTicks);
            if (scaled) {
                GlStateManager.popMatrix();
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        host.renderTooltip(mouseX, mouseY, this.width, this.height);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (rootPanel != null) {
            rootPanel.onMouseClick(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (rootPanel != null) {
            rootPanel.onMouseRelease(mouseX, mouseY, state);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (rootPanel != null) {
            rootPanel.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (rootPanel != null && rootPanel.onKeyPress(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = UIInput.getEventDWheel();
        if (rootPanel != null) {
            if (wheel != 0) {
                int mouseX = UIInput.safeMouseX(this.mc, this.width);
                int mouseY = UIInput.safeMouseY(this.mc, this.height);
                if (rootPanel.onMouseScroll(mouseX, mouseY, wheel)) return;
            }
            rootPanel.handleMouseInput();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        host.cleanupOnClose();
        this.rootPanel = null;
        this.host.rootPanel = null;
    }

    public void closeScreen() {
        if (mc != null) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    protected void addComponent(GuiComponent component) {
        if (rootPanel != null) {
            rootPanel.addChild(component);
        }
    }

    protected void removeComponent(GuiComponent component) {
        if (rootPanel != null) {
            rootPanel.removeChild(component);
        }
    }
}
