package com.x4yi.x4ui.client.gui.component.toast;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.overlay.IGuiOverlay;
import com.x4yi.x4ui.client.gui.overlay.OverlayContext;
import com.x4yi.x4ui.impl.client.gui.GLGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class GuiToastManager implements IGuiOverlay {
    public static final GuiToastManager INSTANCE = new GuiToastManager();
    private final List<GuiToast> activeToasts = new ArrayList<>();
    private final List<GuiToast> pendingToasts = new ArrayList<>();
    private final GuiComponent rootComponent = new com.x4yi.x4ui.client.gui.component.GuiPanel(0, 0, 0, 0);
    private GuiToastManager() {}
    @Override
    public String getId() {
        return "x4ui_toasts";
    }
    @Override
    public com.x4yi.x4ui.client.gui.overlay.ContainerAnchor getAnchor() {
        return com.x4yi.x4ui.client.gui.overlay.ContainerAnchor.FULLSCREEN;
    }
    public void showToast(GuiToast toast) {
        pendingToasts.add(toast);
    }
    private long lastHudFrameNanos = 0L;

    public void update(float deltaTime) {
        if (pendingToasts.isEmpty() && activeToasts.isEmpty()) {
            return;
        }
        if (!pendingToasts.isEmpty()) {
            for (GuiToast toast : pendingToasts) {
                rootComponent.addChild(toast);
                activeToasts.add(toast);
            }
            pendingToasts.clear();
        }
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = mc != null ? new ScaledResolution(mc) : null;
        int screenW = sr != null ? sr.getScaledWidth() : 0;
        int screenH = sr != null ? sr.getScaledHeight() : 0;
        calculateLayouts(screenW, screenH);
        Iterator<GuiToast> it = activeToasts.iterator();
        while (it.hasNext()) {
            GuiToast toast = it.next();
            toast.update(deltaTime);
            float progress = toast.getSlideProgress();
            if (Float.isNaN(toast.currentBaseX)) {
                toast.currentBaseX = toast.targetX;
                toast.currentBaseY = toast.targetY;
            } else {
                float lerp = Math.min(1.0f, deltaTime * 12.0f);
                toast.currentBaseX += (toast.targetX - toast.currentBaseX) * lerp;
                toast.currentBaseY += (toast.targetY - toast.currentBaseY) * lerp;
            }
            int offX = (int) toast.currentBaseX;
            int offY = (int) toast.currentBaseY;
            switch (toast.anchor) {
                case TOP_LEFT:
                case TOP_CENTER:
                case TOP_RIGHT:
                    offY = -toast.getHeight() - 10;
                    break;
                case BOTTOM_LEFT:
                case BOTTOM_CENTER:
                case BOTTOM_RIGHT:
                    offY = screenH + 10;
                    break;
                case LEFT_CENTER:
                    offX = -toast.getWidth() - 10;
                    break;
                case RIGHT_CENTER:
                    offX = screenW + 10;
                    break;
            }
            int currentX = (int) (offX + (toast.currentBaseX - offX) * progress);
            int currentY = (int) (offY + (toast.currentBaseY - offY) * progress);
            toast.setX(currentX);
            toast.setY(currentY);
            if (toast.isDead()) {
                toast.notifyDismissed();
                rootComponent.removeChild(toast);
                it.remove();
            }
        }
    }
    private void calculateLayouts(int screenWidth, int screenHeight) {
        int paddingX = 10;
        int paddingY = 10;
        int gap = 8;
        int topLeftY = paddingY;
        int topRightY = paddingY;
        int topCenterY = paddingY;
        int bottomLeftY = screenHeight - paddingY;
        int bottomRightY = screenHeight - paddingY;
        int bottomCenterY = screenHeight - paddingY;
        int leftCenterY = screenHeight / 2 - 50;
        int rightCenterY = screenHeight / 2 - 50;
        for (GuiToast toast : activeToasts) {
            int x = 0;
            int y = 0;
            switch (toast.anchor) {
                case TOP_LEFT:
                    x = paddingX;
                    y = topLeftY;
                    topLeftY += toast.getHeight() + gap;
                    break;
                case TOP_RIGHT:
                    x = screenWidth - toast.getWidth() - paddingX;
                    y = topRightY;
                    topRightY += toast.getHeight() + gap;
                    break;
                case TOP_CENTER:
                    x = (screenWidth - toast.getWidth()) / 2;
                    y = topCenterY;
                    topCenterY += toast.getHeight() + gap;
                    break;
                case BOTTOM_LEFT:
                    x = paddingX;
                    bottomLeftY -= toast.getHeight();
                    y = bottomLeftY;
                    bottomLeftY -= gap;
                    break;
                case BOTTOM_RIGHT:
                    x = screenWidth - toast.getWidth() - paddingX;
                    bottomRightY -= toast.getHeight();
                    y = bottomRightY;
                    bottomRightY -= gap;
                    break;
                case BOTTOM_CENTER:
                    x = (screenWidth - toast.getWidth()) / 2;
                    bottomCenterY -= toast.getHeight();
                    y = bottomCenterY;
                    bottomCenterY -= gap;
                    break;
                case LEFT_CENTER:
                    x = paddingX;
                    y = leftCenterY;
                    leftCenterY += toast.getHeight() + gap;
                    break;
                case RIGHT_CENTER:
                    x = screenWidth - toast.getWidth() - paddingX;
                    y = rightCenterY;
                    rightCenterY += toast.getHeight() + gap;
                    break;
            }
            toast.setTargetPosition(x, y);
        }
    }
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        if (pendingToasts.isEmpty() && activeToasts.isEmpty()) {
            lastHudFrameNanos = 0L;
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.currentScreen != null) return;
        long now = System.nanoTime();
        float dt = (lastHudFrameNanos == 0L) ? 0.016f : Math.min(0.1f, (now - lastHudFrameNanos) / 1_000_000_000.0f);
        lastHudFrameNanos = now;
        update(dt);
        if (activeToasts.isEmpty()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        GLGraphics gfx = GLGraphics.INSTANCE;
        rootComponent.setWidth(sr.getScaledWidth());
        rootComponent.setHeight(sr.getScaledHeight());
        rootComponent.render(gfx, -1, -1, event.getPartialTicks());
    }
    @Override
    public boolean matches(GuiScreen screen) {
        return true;
    }
    @Override
    public void init(GuiScreen screen, OverlayContext context) {
        rootComponent.setWidth(screen.width);
        rootComponent.setHeight(screen.height);
    }
    @Override
    public void update(GuiScreen screen, float deltaTime) {
        update(deltaTime);
    }
    @Override
    public void renderBackground(GuiScreen screen, IGraphics gfx, int mouseX, int mouseY, float partialTicks) {}
    @Override
    public void renderForeground(GuiScreen screen, IGraphics gfx, int mouseX, int mouseY, float partialTicks) {}
    @Override
    public void onClosed(GuiScreen screen) {}
    @Override
    public GuiComponent getRootComponent() {
        return rootComponent;
    }
    @Override
    public List<Rectangle> getExclusionAreas() {
        return null;
    }
}