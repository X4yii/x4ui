package com.x4yi.x4ui.client.gui.base;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.input.UIInput;
import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;
public abstract class GuiBaseScreen extends GuiScreen {
    protected final GuiScreen parentScreen;
    protected final String title;
    protected GuiPanel rootPanel;
    protected float animationProgress = 0.0f;
    protected boolean isAnimating = true;
    protected static final float ANIMATION_SPEED = 0.15f;
    protected com.x4yi.x4ui.common.sync.IGuiActionSender actionSender;
    protected String boundChannelId;
    private final java.util.List<Long> pendingRpcIds = new java.util.ArrayList<>();

    public void setActionSender(com.x4yi.x4ui.common.sync.IGuiActionSender actionSender) {
        this.actionSender = actionSender;
    }

    public void bindChannel(String channelId) {
        this.boundChannelId = channelId;
    }

    public void sendActionToServer(String actionId, @javax.annotation.Nullable net.minecraft.nbt.NBTTagCompound data) {
        if (this.actionSender != null) {
            this.actionSender.sendActionToServer(actionId, data);
        } else {
            com.x4yi.x4ui.common.network.UINetwork.sendActionToServer(actionId, data);
        }
    }

    public void sendActionOrLocal(String actionId, @javax.annotation.Nullable net.minecraft.nbt.NBTTagCompound data, @javax.annotation.Nullable Runnable localFallback) {
        if (this.actionSender != null) {
            this.actionSender.sendActionToServer(actionId, data);
        } else {
            com.x4yi.x4ui.common.network.UINetwork.sendActionOrLocal(actionId, data, localFallback);
        }
    }

    public <T> com.x4yi.x4ui.common.State<T> syncedState(String key, T initialValue) {
        return com.x4yi.x4ui.client.gui.bus.UIStateBus.getOrCreate(
                key, initialValue, com.x4yi.x4ui.client.gui.bus.UIStateBus.Scope.SCREEN
        );
    }

    public long request(String rpcId, @javax.annotation.Nullable net.minecraft.nbt.NBTTagCompound params, java.util.function.Consumer<com.x4yi.x4ui.common.network.RpcResult> callback) {
        final long[] reqId = { -1L };
        long id = com.x4yi.x4ui.common.network.UINetwork.request(rpcId, params, res -> {
            if (reqId[0] > 0L) {
                pendingRpcIds.remove(Long.valueOf(reqId[0]));
            }
            if (callback != null) {
                callback.accept(res);
            }
        });
        reqId[0] = id;
        if (id > 0L) {
            pendingRpcIds.add(id);
        }
        return id;
    }
    public void setTheme(com.x4yi.x4ui.client.gui.utils.ITheme theme) {
        if (rootPanel != null) rootPanel.setTheme(theme);
    }
    public GuiBaseScreen(GuiScreen parent, String title) {
        this.parentScreen = parent;
        this.title = title;
    }
    public static int currentScaleFactor = 1;
    public static float deltaTime = 0.0f;
    private long lastRenderTime = 0;
    protected boolean dimBackground = true;
    protected int dimColor = 0xB0080A10;
    private GuiPanel managedWindow;
    private int managedMaxWidth = 0;
    private int managedMaxHeight = 0;

    public void setDimBackground(boolean dimBackground) {
        this.dimBackground = dimBackground;
    }

    public void setDimColor(int dimColor) {
        this.dimColor = dimColor;
    }

    protected GuiPanel createCenteredWindow(int maxWidth, int maxHeight) {
        if (rootPanel == null) return null;
        rootPanel.setFlexDirection(com.x4yi.x4ui.client.gui.component.layout.FlexDirection.VERTICAL);
        rootPanel.setAlignItems(com.x4yi.x4ui.client.gui.component.layout.FlexAlign.CENTER);
        rootPanel.setJustifyContent(com.x4yi.x4ui.client.gui.component.layout.FlexAlign.CENTER);
        rootPanel.setPadding(new com.x4yi.x4ui.client.gui.utils.Insets(16));

        this.managedMaxWidth = maxWidth;
        this.managedMaxHeight = maxHeight;
        int targetW = Math.min(maxWidth, Math.max(200, this.width - 32));
        int targetH = maxHeight > 0 ? Math.min(maxHeight, Math.max(140, this.height - 32)) : 0;

        GuiPanel window = GuiPanel.card(12, 8);
        window.setBackgroundColor(window.getTheme().getBackgroundColor());
        window.withPercentWidth(-1.0f);
        window.setWidth(targetW);
        if (maxHeight > 0) {
            window.setHugContent(false);
            window.setHeight(targetH);
        }
        this.managedWindow = window;
        rootPanel.addChild(window);
        return window;
    }

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
            if (managedWindow != null && managedMaxWidth > 0) {
                managedWindow.setWidth(Math.min(managedMaxWidth, Math.max(200, this.width - 32)));
                if (managedMaxHeight > 0) {
                    managedWindow.setHeight(Math.min(managedMaxHeight, Math.max(140, this.height - 32)));
                }
            }
            rootPanel.requestLayout();
            onScreenResize(width, height);
        }
    }
    protected void onScreenResize(int newWidth, int newHeight) {
    }
    protected abstract void initComponents();
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        com.x4yi.x4ui.common.network.UINetwork.sweepExpiredRpcs();
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
            if (dimBackground) {
                gfx.drawRect(0, 0, this.width, this.height, dimColor);
            }
            rootPanel.render(gfx, mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (rootPanel != null) {
            com.x4yi.x4ui.impl.client.gui.GLGraphics gfx = com.x4yi.x4ui.impl.client.gui.GLGraphics.INSTANCE;
            com.x4yi.x4ui.client.gui.component.GuiComponent tooltipSource = rootPanel.getTooltipSource(mouseX, mouseY);
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
        for (Long id : pendingRpcIds) {
            com.x4yi.x4ui.common.network.UINetwork.cancelPendingRpc(id);
        }
        pendingRpcIds.clear();
        if (boundChannelId != null && !boundChannelId.isEmpty()) {
            net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound();
            tag.setString(com.x4yi.x4ui.common.network.UINetwork.INTERNAL_CHANNEL_KEY, boundChannelId);
            com.x4yi.x4ui.common.network.UINetwork.sendActionToServer(
                    com.x4yi.x4ui.common.network.UINetwork.ACTION_UNSUBSCRIBE_CHANNEL, tag
            );
            boundChannelId = null;
        }
        com.x4yi.x4ui.client.gui.bus.UIStateBus.clearScreenStates();
        if (rootPanel != null) {
            rootPanel.destroy();
        }
        com.x4yi.x4ui.impl.client.gui.GLGraphics.INSTANCE.clearClip();
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