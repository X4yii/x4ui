package com.x4yi.x4ui.client.gui.base;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import java.io.IOException;
public abstract class GuiBaseScreen extends GuiScreen {
    protected final GuiScreen parentScreen;
    protected final String title;
    protected GuiPanel rootPanel;
    protected float animationProgress = 0.0f;
    protected boolean isAnimating = true;
    protected static final float ANIMATION_SPEED = 0.15f;
    protected com.x4yi.x4ui.common.sync.IGuiActionSender actionSender;
    public void setActionSender(com.x4yi.x4ui.common.sync.IGuiActionSender actionSender) {
        this.actionSender = actionSender;
    }
    public void setTheme(com.x4yi.x4ui.client.gui.utils.ITheme theme) {
        if (rootPanel != null) rootPanel.setTheme(theme);
    }
    @Deprecated
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
    public static int currentScaleFactor = 1;
    public static float deltaTime = 0.0f;
    private long lastRenderTime = 0;
    @Override
    public void initGui() {
        super.initGui();
        currentScaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        if (rootPanel == null) {
            rootPanel = new GuiPanel(0, 0, width, height);
            initComponents();
        } else {
            rootPanel.setWidth(width);
            rootPanel.setHeight(height);
            rootPanel.requestLayout();
            onScreenResize(width, height);
        }
    }
    protected void onScreenResize(int newWidth, int newHeight) {
    }
    protected abstract void initComponents();
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long currentTime = System.currentTimeMillis();
        if (lastRenderTime != 0) {
            deltaTime = (currentTime - lastRenderTime) / 1000f;
            if (deltaTime > 0.1f) deltaTime = 0.1f;
            if (deltaTime < 0) deltaTime = 0;
        }
        lastRenderTime = currentTime;
        if (rootPanel != null) {
            rootPanel.update(deltaTime);
            com.x4yi.x4ui.impl.client.gui.GLGraphics gfx = com.x4yi.x4ui.impl.client.gui.GLGraphics.INSTANCE;
            gfx.reset();
            rootPanel.render(gfx, mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (rootPanel != null) {
            com.x4yi.x4ui.impl.client.gui.GLGraphics gfx = com.x4yi.x4ui.impl.client.gui.GLGraphics.INSTANCE;
            com.x4yi.x4ui.client.gui.component.GuiComponent hovered = rootPanel.getHoveredComponent(mouseX, mouseY);
            com.x4yi.x4ui.client.gui.component.GuiComponent tooltipSource = hovered;
            while (tooltipSource != null && tooltipSource.getTooltipText() == null && tooltipSource.getTooltipComponent() == null) {
                tooltipSource = tooltipSource.getParent();
            }
            if (tooltipSource != null) {
                if (tooltipSource.getTooltipComponent() != null) {
                    rootPanel.getTheme().drawTooltipComponent(gfx, tooltipSource.getTooltipComponent(), mouseX, mouseY, this.width, this.height);
                } else if (tooltipSource.getTooltipText() != null) {
                    String text = tooltipSource.getTooltipText();
                    java.util.List<String> lines = (text.indexOf('\n') == -1) ?
                            java.util.Collections.singletonList(text) : java.util.Arrays.asList(text.split("\n"));
                    rootPanel.getTheme().drawTooltip(gfx, lines, mouseX, mouseY, this.width, this.height);
                }
            }
        }
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
        if (rootPanel != null) {
            if (rootPanel.onKeyPress(typedChar, keyCode)) {
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
                if (rootPanel.onMouseScroll(mouseX, mouseY, wheel)) return;
            }
            rootPanel.handleMouseInput();
        }
    }
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (rootPanel != null) {
            rootPanel.destroy();
        }
        com.x4yi.x4ui.client.gui.utils.ScissorHelper.clear();
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