package com.x4yi.x4ui.client.gui.base;

import com.x4yi.x4ui.client.gui.bus.UIStateBus;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.layout.FlexAlign;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.utils.Insets;
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.network.RpcResult;
import com.x4yi.x4ui.common.network.UINetwork;
import com.x4yi.x4ui.impl.client.gui.GLGraphics;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Shared controller encapsulating lifecycle, networking, window centering,
 * delta-time animation, and cached tooltip rendering for X4UI screens.
 */
public final class ScreenHost {
    public GuiPanel rootPanel;
    public GuiPanel managedWindow;
    public int managedMaxWidth = 0;
    public int managedMaxHeight = 0;

    public String boundChannelId;
    public final List<Long> pendingRpcIds = new ArrayList<>();

    private long lastRenderTime = 0L;
    private float deltaTime = 0.016f;
    private boolean animateOpen = false;
    private float animationProgress = 1.0f;

    private String lastTooltipText = null;
    private List<String> cachedTooltipLines = Collections.emptyList();

    public void setAnimateOpen(boolean animateOpen) {
        this.animateOpen = animateOpen;
        if (animateOpen) {
            this.animationProgress = 0.0f;
        } else {
            this.animationProgress = 1.0f;
        }
    }

    public boolean isAnimateOpen() {
        return animateOpen;
    }

    public float getAnimationProgress() {
        return animationProgress;
    }

    public void bindChannel(String channelId) {
        this.boundChannelId = channelId;
    }

    public void sendActionToServer(String actionId, @Nullable NBTTagCompound data) {
        UINetwork.sendActionToServer(actionId, data);
    }

    public void sendActionOrLocal(String actionId, @Nullable NBTTagCompound data, @Nullable Runnable localFallback) {
        UINetwork.sendActionOrLocal(actionId, data, localFallback);
    }

    public <T> State<T> syncedState(String key, T initialValue) {
        return UIStateBus.getOrCreate(key, initialValue, UIStateBus.Scope.SCREEN);
    }

    public long request(String rpcId, @Nullable NBTTagCompound params, Consumer<RpcResult> callback) {
        final long[] reqId = { -1L };
        long id = UINetwork.request(rpcId, params, res -> {
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

    public GuiPanel createCenteredWindow(int maxWidth, int maxHeight, int screenW, int screenH) {
        if (rootPanel == null) return null;
        rootPanel.setFlexDirection(FlexDirection.VERTICAL);
        rootPanel.setAlignItems(FlexAlign.CENTER);
        rootPanel.setJustifyContent(FlexAlign.CENTER);
        rootPanel.setPadding(new Insets(16));

        this.managedMaxWidth = maxWidth;
        this.managedMaxHeight = maxHeight;
        int targetW = Math.min(maxWidth, Math.max(200, screenW - 32));
        int targetH = maxHeight > 0 ? Math.min(maxHeight, Math.max(140, screenH - 32)) : 0;

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

    public void resizeManagedWindow(int screenW, int screenH) {
        if (managedWindow != null && managedMaxWidth > 0) {
            managedWindow.setWidth(Math.min(managedMaxWidth, Math.max(200, screenW - 32)));
            if (managedMaxHeight > 0) {
                managedWindow.setHeight(Math.min(managedMaxHeight, Math.max(140, screenH - 32)));
            }
        }
    }

    public float beginFrame() {
        UINetwork.sweepExpiredRpcs();
        long currentTime = System.currentTimeMillis();
        if (lastRenderTime != 0L) {
            deltaTime = (currentTime - lastRenderTime) / 1000.0f;
            if (deltaTime > 0.1f) deltaTime = 0.1f;
            if (deltaTime < 0.0f) deltaTime = 0.0f;
        }
        lastRenderTime = currentTime;
        GuiBaseScreen.deltaTime = deltaTime;

        if (animateOpen && animationProgress < 1.0f) {
            animationProgress = Math.min(1.0f, animationProgress + deltaTime * 8.0f);
        }
        return deltaTime;
    }

    public void renderTooltip(int mouseX, int mouseY, int screenW, int screenH) {
        if (rootPanel == null) return;
        GLGraphics gfx = GLGraphics.INSTANCE;
        GuiComponent tooltipSource = rootPanel.getTooltipSource(mouseX, mouseY);
        if (tooltipSource == null) return;

        if (tooltipSource.getTooltipComponent() != null) {
            rootPanel.getTheme().drawTooltipComponent(gfx, tooltipSource.getTooltipComponent(), mouseX, mouseY, screenW, screenH);
        } else if (tooltipSource.getTooltipText() != null) {
            String text = tooltipSource.getTooltipText();
            if (!text.equals(lastTooltipText)) {
                lastTooltipText = text;
                cachedTooltipLines = (text.indexOf('\n') == -1)
                        ? Collections.singletonList(text)
                        : Arrays.asList(text.split("\n"));
            }
            rootPanel.getTheme().drawTooltip(gfx, cachedTooltipLines, mouseX, mouseY, screenW, screenH);
        }
    }

    public void cleanupOnClose() {
        for (Long id : pendingRpcIds) {
            UINetwork.cancelPendingRpc(id);
        }
        pendingRpcIds.clear();
        if (boundChannelId != null && !boundChannelId.isEmpty()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(UINetwork.INTERNAL_CHANNEL_KEY, boundChannelId);
            UINetwork.sendActionToServer(UINetwork.ACTION_UNSUBSCRIBE_CHANNEL, tag);
            boundChannelId = null;
        }
        UIStateBus.clearScreenStates();
        if (rootPanel != null) {
            rootPanel.destroy();
        }
        GLGraphics.INSTANCE.clearClip();
        lastTooltipText = null;
        cachedTooltipLines = Collections.emptyList();
    }
}
