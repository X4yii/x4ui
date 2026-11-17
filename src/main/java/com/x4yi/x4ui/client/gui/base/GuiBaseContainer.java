package com.x4yi.x4ui.client.gui.base;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import com.x4yi.x4ui.client.gui.component.layout.DockSide;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.input.UIInput;
import com.x4yi.x4ui.client.gui.utils.BorderSides;
import com.x4yi.x4ui.client.gui.utils.CornerRadii;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public abstract class GuiBaseContainer extends GuiContainer {
    protected final ScreenHost host = new ScreenHost();
    protected GuiPanel rootPanel;
    public GuiPanel getRootPanel() { return rootPanel; }
    private boolean sidePanelsDirty = true;
    private int lastLayoutW = -1;
    private int lastLayoutH = -1;

    public void bindChannel(String channelId) {
        host.bindChannel(channelId);
    }

    public void sendActionToServer(String actionId, @javax.annotation.Nullable net.minecraft.nbt.NBTTagCompound data) {
        host.sendActionToServer(actionId, data);
    }

    public void sendActionOrLocal(String actionId, @javax.annotation.Nullable net.minecraft.nbt.NBTTagCompound data, @javax.annotation.Nullable Runnable localFallback) {
        host.sendActionOrLocal(actionId, data, localFallback);
    }

    public <T> com.x4yi.x4ui.common.State<T> syncedState(String key, T initialValue) {
        return host.syncedState(key, initialValue);
    }

    public long request(String rpcId, @javax.annotation.Nullable net.minecraft.nbt.NBTTagCompound params, java.util.function.Consumer<com.x4yi.x4ui.common.network.RpcResult> callback) {
        return host.request(rpcId, params, callback);
    }

    public void setAnimateOpen(boolean animateOpen) {
        host.setAnimateOpen(animateOpen);
    }

    public net.minecraft.inventory.Container getContainer() {
        return this.inventorySlots;
    }
    public void setTheme(com.x4yi.x4ui.client.gui.utils.ITheme theme) {
        if (rootPanel != null) rootPanel.setTheme(theme);
    }
    public GuiBaseContainer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
        host.setAnimateOpen(true);
    }
    private int lastMainW = -1;
    private int lastMainH = -1;
    private final java.util.List<java.awt.Rectangle> sidePanelJeiAreas = new java.util.ArrayList<>();

    private static final class SidePanelEntry {
        final DockSide side;
        final GuiComponent panel;
        final int offset;
        final int gap;
        final CornerRadii originalRadii;
        final int originalBorderSides;
        boolean lastVisible = true;
        int lastBaseW = -1;
        int lastBaseH = -1;

        SidePanelEntry(DockSide side, GuiComponent panel, int offset, int gap) {
            this.side = side;
            this.panel = panel;
            this.offset = offset;
            this.gap = gap;
            if (panel instanceof GuiPanel) {
                GuiPanel gp = (GuiPanel) panel;
                this.originalRadii = gp.getCornerRadii();
                this.originalBorderSides = gp.getBorderSides();
            } else {
                this.originalRadii = CornerRadii.ZERO;
                this.originalBorderSides = BorderSides.ALL;
            }
        }
    }

    private final List<SidePanelEntry> sidePanels = new ArrayList<>();

    public GuiBaseContainer attachSidePanel(DockSide side, GuiComponent panel) {
        return attachSidePanel(side, panel, 0, 0);
    }

    public GuiBaseContainer attachSidePanel(DockSide side, GuiComponent panel, int offset, int gap) {
        if (side == null || panel == null) return this;
        sidePanels.add(new SidePanelEntry(side, panel, offset, Math.max(0, gap)));
        this.sidePanelsDirty = true;
        if (rootPanel != null) {
            rootPanel.setFlexDirection(FlexDirection.ABSOLUTE);
            if (!rootPanel.getChildren().contains(panel)) {
                rootPanel.addChild(panel);
            }
            layoutSidePanelsAndSync();
        }
        return this;
    }

    public void requestSidePanelLayout() {
        this.sidePanelsDirty = true;
    }

    public void layoutSidePanelsAndSync() {
        if (rootPanel == null) return;
        int screenW = Math.max(1, this.width);
        int screenH = Math.max(1, this.height);
        int margin = 8;

        GuiPanel managedWindow = host.managedWindow;
        int mainW = managedWindow != null ? managedWindow.getWidth() : this.xSize;
        int mainH = managedWindow != null ? managedWindow.getHeight() : this.ySize;

        if (!sidePanels.isEmpty()) {
            rootPanel.setFlexDirection(FlexDirection.ABSOLUTE);
        }

        for (SidePanelEntry entry : sidePanels) {
            GuiComponent p = entry.panel;
            p.applyLayoutWidth(p.getBaseWidth());
            p.applyLayoutHeight(p.getBaseHeight());
        }

        int leftExtra = 0, rightExtra = 0, topExtra = 0, bottomExtra = 0;
        for (SidePanelEntry entry : sidePanels) {
            if (!entry.panel.isVisible()) continue;
            switch (entry.side) {
                case LEFT:   leftExtra = Math.max(leftExtra, entry.panel.getWidth() + entry.gap); break;
                case RIGHT:  rightExtra = Math.max(rightExtra, entry.panel.getWidth() + entry.gap); break;
                case TOP:    topExtra = Math.max(topExtra, entry.panel.getHeight() + entry.gap); break;
                case BOTTOM: bottomExtra = Math.max(bottomExtra, entry.panel.getHeight() + entry.gap); break;
            }
        }

        int maxAllowedW = Math.max(0, screenW - margin * 2);
        int overflowW = (leftExtra + mainW + rightExtra) - maxAllowedW;
        if (overflowW > 0) {
            shrinkSidePanels(true, overflowW);
            leftExtra = 0;
            rightExtra = 0;
            for (SidePanelEntry entry : sidePanels) {
                if (!entry.panel.isVisible()) continue;
                if (entry.side == DockSide.LEFT) leftExtra = Math.max(leftExtra, entry.panel.getWidth() + entry.gap);
                if (entry.side == DockSide.RIGHT) rightExtra = Math.max(rightExtra, entry.panel.getWidth() + entry.gap);
            }
        }

        int maxAllowedH = Math.max(0, screenH - margin * 2);
        int overflowH = (topExtra + mainH + bottomExtra) - maxAllowedH;
        if (overflowH > 0) {
            shrinkSidePanels(false, overflowH);
            topExtra = 0;
            bottomExtra = 0;
            for (SidePanelEntry entry : sidePanels) {
                if (!entry.panel.isVisible()) continue;
                if (entry.side == DockSide.TOP) topExtra = Math.max(topExtra, entry.panel.getHeight() + entry.gap);
                if (entry.side == DockSide.BOTTOM) bottomExtra = Math.max(bottomExtra, entry.panel.getHeight() + entry.gap);
            }
        }

        int mainX = (screenW - mainW) / 2;
        int mainY = (screenH - mainH) / 2;

        if (mainX - leftExtra < margin) {
            mainX = margin + leftExtra;
        }
        if (mainX + mainW + rightExtra > screenW - margin) {
            mainX = Math.max(margin + leftExtra, screenW - margin - rightExtra - mainW);
        }
        if (mainY - topExtra < margin) {
            mainY = margin + topExtra;
        }
        if (mainY + mainH + bottomExtra > screenH - margin) {
            mainY = Math.max(margin + topExtra, screenH - margin - bottomExtra - mainH);
        }

        if (managedWindow != null) {
            managedWindow.setX(mainX);
            managedWindow.setY(mainY);
            this.guiLeft = mainX;
            this.guiTop = mainY;
            this.xSize = mainW;
            this.ySize = mainH;
        } else {
            this.guiLeft = mainX;
            this.guiTop = mainY;
        }

        int mainBorderSides = BorderSides.ALL;
        for (SidePanelEntry entry : sidePanels) {
            GuiComponent p = entry.panel;
            if (!p.isVisible()) continue;
            int px = mainX;
            int py = mainY;
            switch (entry.side) {
                case LEFT:
                    px = mainX - entry.gap - p.getWidth();
                    py = mainY + entry.offset;
                    break;
                case RIGHT:
                    px = mainX + mainW + entry.gap;
                    py = mainY + entry.offset;
                    break;
                case TOP:
                    px = mainX + entry.offset;
                    py = mainY - entry.gap - p.getHeight();
                    break;
                case BOTTOM:
                    px = mainX + entry.offset;
                    py = mainY + mainH + entry.gap;
                    break;
            }
            p.setX(px);
            p.setY(py);
            if (p instanceof GuiPanel) {
                GuiPanel gp = (GuiPanel) p;
                if (entry.gap == 0) {
                    gp.setBorderSides(BorderSides.without(entry.originalBorderSides, entry.side.opposite().toBorderSide()));
                    gp.setCornerRadii(flattenTouchingCorners(entry.originalRadii, entry.side.opposite()));
                    mainBorderSides = BorderSides.without(mainBorderSides, entry.side.toBorderSide());
                } else {
                    gp.setBorderSides(entry.originalBorderSides);
                    gp.setCornerRadii(entry.originalRadii);
                }
            }
        }
        if (managedWindow != null && !sidePanels.isEmpty()) {
            managedWindow.setBorderSides(mainBorderSides);
        }

        sidePanelJeiAreas.clear();
        if (managedWindow != null || !sidePanels.isEmpty()) {
            sidePanelJeiAreas.add(new Rectangle(mainX, mainY, mainW, mainH));
            for (SidePanelEntry entry : sidePanels) {
                if (entry.panel.isVisible()) {
                    sidePanelJeiAreas.add(new Rectangle(
                            entry.panel.getAbsoluteX(), entry.panel.getAbsoluteY(),
                            entry.panel.getWidth(), entry.panel.getHeight()
                    ));
                }
            }
        }
        this.lastMainW = mainW;
        this.lastMainH = mainH;
        syncSlotsRecursive(rootPanel);
    }

    private void shrinkSidePanels(boolean horizontal, int overflow) {
        int remaining = overflow;
        for (SidePanelEntry entry : sidePanels) {
            if (!entry.panel.isVisible()) continue;
            if (horizontal && !entry.side.isHorizontal()) continue;
            if (!horizontal && !entry.side.isVertical()) continue;
            GuiComponent p = entry.panel;
            if (p.getFlexShrink() <= 0.0f) continue;
            int current = horizontal ? p.getWidth() : p.getHeight();
            int min = horizontal ? p.getMinWidth() : p.getMinHeight();
            int reducible = Math.max(0, current - min);
            int sub = Math.min(remaining, reducible);
            if (sub > 0) {
                if (horizontal) p.applyLayoutWidth(current - sub);
                else p.applyLayoutHeight(current - sub);
                remaining -= sub;
            }
            if (remaining <= 0) break;
        }
    }

    private static CornerRadii flattenTouchingCorners(CornerRadii radii, DockSide touchingEdge) {
        switch (touchingEdge) {
            case LEFT:   return radii.withTopLeft(0).withBottomLeft(0);
            case RIGHT:  return radii.withTopRight(0).withBottomRight(0);
            case TOP:    return radii.withTopLeft(0).withTopRight(0);
            case BOTTOM: return radii.withBottomLeft(0).withBottomRight(0);
            default:     return radii;
        }
    }

    private void syncSlotsRecursive(GuiComponent comp) {
        if (comp == null) return;
        if (comp instanceof GuiSlot) {
            ((GuiSlot) comp).update(0.0f);
            return;
        }
        for (GuiComponent child : comp.getChildren()) {
            syncSlotsRecursive(child);
        }
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeftIn, int guiTopIn) {
        boolean outsideMain = super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn);
        if (!outsideMain) return false;
        for (SidePanelEntry entry : sidePanels) {
            if (entry.panel.isVisible() && entry.panel.contains(mouseX, mouseY)) {
                return false;
            }
        }
        return true;
    }

    protected GuiPanel createCenteredWindow(int maxWidth, int maxHeight) {
        return host.createCenteredWindow(maxWidth, maxHeight, this.width, this.height);
    }

    @Override
    public void initGui() {
        if (this.mc != null) {
            super.initGui();
            GuiBaseScreen.currentScaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        }
        if (rootPanel == null) {
            rootPanel = new GuiPanel(0, 0, width, height);
            host.rootPanel = rootPanel;
            jeiRecipeAreas.clear();
            sidePanelJeiAreas.clear();
            for (SidePanelEntry entry : sidePanels) {
                if (!rootPanel.getChildren().contains(entry.panel)) {
                    rootPanel.addChild(entry.panel);
                }
            }
            initComponents();
            if (!sidePanels.isEmpty()) {
                layoutSidePanelsAndSync();
            }
        } else {
            rootPanel.setWidth(width);
            rootPanel.setHeight(height);
            host.resizeManagedWindow(this.width, this.height);
            this.sidePanelsDirty = true;
            if (!sidePanels.isEmpty()) {
                layoutSidePanelsAndSync();
            }
            rootPanel.requestLayout();
            onScreenResize(width, height);
        }
    }
    protected void onScreenResize(int newWidth, int newHeight) {
    }
    protected abstract void initComponents();
    public int getGuiLeft() { return this.guiLeft; }
    public int getGuiTop() { return this.guiTop; }
    @Override
    public void drawDefaultBackground() {
    }
    public void sendButtonClick(int buttonId) {
        if (this.mc != null && this.mc.playerController != null && this.inventorySlots != null) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, buttonId);
        }
    }
    private boolean checkSidePanelsChanged() {
        if (sidePanelsDirty || this.width != lastLayoutW || this.height != lastLayoutH) {
            return true;
        }
        int curMainW = host.managedWindow != null ? host.managedWindow.getWidth() : this.xSize;
        int curMainH = host.managedWindow != null ? host.managedWindow.getHeight() : this.ySize;
        if (curMainW != lastMainW || curMainH != lastMainH) {
            return true;
        }
        for (SidePanelEntry entry : sidePanels) {
            boolean vis = entry.panel.isVisible();
            if (vis != entry.lastVisible || (vis && (entry.panel.getBaseWidth() != entry.lastBaseW || entry.panel.getBaseHeight() != entry.lastBaseH))) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float dt = host.beginFrame();
        if (rootPanel != null) {
            rootPanel.update(dt);
            if (!sidePanels.isEmpty() && checkSidePanelsChanged()) {
                layoutSidePanelsAndSync();
                this.sidePanelsDirty = false;
                this.lastLayoutW = this.width;
                this.lastLayoutH = this.height;
                for (SidePanelEntry entry : sidePanels) {
                    entry.lastVisible = entry.panel.isVisible();
                    entry.lastBaseW = entry.panel.getBaseWidth();
                    entry.lastBaseH = entry.panel.getBaseHeight();
                }
            }
        }
        super.drawDefaultBackground();
        float anim = host.getAnimationProgress();
        boolean scaled = anim < 1.0f;
        if (scaled) {
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            float scale = 0.9f + (0.1f * anim);
            net.minecraft.client.renderer.GlStateManager.translate(width / 2.0f, height / 2.0f, 0);
            net.minecraft.client.renderer.GlStateManager.scale(scale, scale, 1.0f);
            net.minecraft.client.renderer.GlStateManager.translate(-width / 2.0f, -height / 2.0f, 0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (scaled) {
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
        this.renderHoveredToolTip(mouseX, mouseY);
        host.renderTooltip(mouseX, mouseY, this.width, this.height);
    }
    protected final java.util.List<java.awt.Rectangle> jeiRecipeAreas = new java.util.ArrayList<>();
    public java.util.List<java.awt.Rectangle> getJeiRecipeAreas() {
        if (sidePanelJeiAreas.isEmpty()) {
            return jeiRecipeAreas;
        }
        java.util.List<java.awt.Rectangle> merged = new java.util.ArrayList<>(jeiRecipeAreas.size() + sidePanelJeiAreas.size());
        merged.addAll(jeiRecipeAreas);
        merged.addAll(sidePanelJeiAreas);
        return merged;
    }
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (rootPanel != null) {
            com.x4yi.x4ui.impl.client.gui.GLGraphics gfx = com.x4yi.x4ui.impl.client.gui.GLGraphics.INSTANCE;
            gfx.reset();
            rootPanel.render(gfx, mouseX, mouseY, partialTicks);
        }
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (rootPanel != null && rootPanel.onMouseClick(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
        com.x4yi.x4ui.common.sync.NetworkSyncHelper.unbindContainer(this.inventorySlots);
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