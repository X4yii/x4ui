package com.x4yi.x4ui.client.gui.overlay;
import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.utils.DefaultTheme;
import com.x4yi.x4ui.impl.client.gui.GLGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.x4yi.x4ui.client.gui.input.UIInput;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@SideOnly(Side.CLIENT)
public final class GuiOverlayManager {
    public static final GuiOverlayManager INSTANCE = new GuiOverlayManager();
    private final List<IGuiOverlay> registeredOverlays = new CopyOnWriteArrayList<>();
    private GuiScreen currentScreen = null;
    private long lastRenderTime = 0;
    private boolean needsLayout = true;
    private OverlayContext cachedContext = null;
    public void requestLayout() {
        this.needsLayout = true;
    }
    private GuiOverlayManager() {}
    public void registerOverlay(IGuiOverlay overlay) {
        if (overlay != null && !registeredOverlays.contains(overlay)) {
            registeredOverlays.add(overlay);
        }
    }
    public void unregisterOverlay(IGuiOverlay overlay) {
        if (overlay != null) {
            registeredOverlays.remove(overlay);
        }
    }
    public List<IGuiOverlay> getRegisteredOverlays() {
        return Collections.unmodifiableList(registeredOverlays);
    }
    public List<IGuiOverlay> getActiveOverlays(GuiScreen screen) {
        if (screen == null || registeredOverlays.isEmpty()) {
            return Collections.emptyList();
        }
        List<IGuiOverlay> matches = new ArrayList<>();
        for (IGuiOverlay overlay : registeredOverlays) {
            if (overlay.matches(screen)) {
                matches.add(overlay);
            }
        }
        return matches;
    }
    public List<Rectangle> getExclusionAreas(GuiContainer container) {
        if (container == null) return Collections.emptyList();
        List<IGuiOverlay> active = getActiveOverlays(container);
        if (active.isEmpty()) return Collections.emptyList();
        List<Rectangle> areas = new ArrayList<>();
        for (IGuiOverlay overlay : active) {
            List<Rectangle> overlayAreas = overlay.getExclusionAreas();
            if (overlayAreas != null && !overlayAreas.isEmpty()) {
                areas.addAll(overlayAreas);
            }
        }
        return areas;
    }
    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen screen = event.getGui();
        if (screen == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            GuiBaseScreen.currentScaleFactor = new ScaledResolution(mc).getScaleFactor();
        }
        this.currentScreen = screen;
        List<IGuiOverlay> active = getActiveOverlays(screen);
        if (active.isEmpty()) return;
        this.cachedContext = new OverlayContext(screen);
        for (IGuiOverlay overlay : active) {
            overlay.init(screen, cachedContext);
        }
        OverlayDockManager.layout(active, cachedContext);
        this.needsLayout = false;
    }
    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        GuiScreen screen = event.getGui();
        if (screen == null) return;
        List<IGuiOverlay> active = getActiveOverlays(screen);
        if (active.isEmpty()) return;
        GLGraphics gfx = GLGraphics.INSTANCE;
        for (IGuiOverlay overlay : active) {
            overlay.renderBackground(screen, gfx, event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
        }
    }
    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen screen = event.getGui();
        if (screen == null) return;
        List<IGuiOverlay> active = getActiveOverlays(screen);
        if (active.isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        float deltaTime = 0.016f;
        if (lastRenderTime != 0) {
            deltaTime = (currentTime - lastRenderTime) / 1000f;
            if (deltaTime > 0.1f) deltaTime = 0.1f;
            if (deltaTime < 0) deltaTime = 0;
        }
        lastRenderTime = currentTime;
        for (IGuiOverlay overlay : active) {
            overlay.update(screen, deltaTime);
        }
        if (needsLayout || cachedContext == null || cachedContext.getScreen() != screen) {
            this.cachedContext = new OverlayContext(screen);
            OverlayDockManager.layout(active, cachedContext);
            this.needsLayout = false;
        }
        GLGraphics gfx = GLGraphics.INSTANCE;
        int mouseX = event.getMouseX();
        int mouseY = event.getMouseY();
        float partialTicks = event.getRenderPartialTicks();
        for (IGuiOverlay overlay : active) {
            GuiComponent root = overlay.getRootComponent();
            if (root != null && root.isVisible()) {
                gfx.reset();
                root.render(gfx, mouseX, mouseY, partialTicks);
            }
            overlay.renderForeground(screen, gfx, mouseX, mouseY, partialTicks);
        }
        for (IGuiOverlay overlay : active) {
            GuiComponent root = overlay.getRootComponent();
            if (root != null && root.isVisible()) {
                GuiComponent tooltipSource = root.getTooltipSource(mouseX, mouseY);
                if (tooltipSource != null) {
                    com.x4yi.x4ui.client.gui.utils.ITheme activeTheme = root.getTheme() != null ? root.getTheme() : com.x4yi.x4ui.client.gui.utils.ThemeRegistry.getCurrentTheme();
                    if (tooltipSource.getTooltipComponent() != null) {
                        activeTheme.drawTooltipComponent(gfx, tooltipSource.getTooltipComponent(), mouseX, mouseY, screen.width, screen.height);
                    } else if (tooltipSource.getTooltipText() != null) {
                        String text = tooltipSource.getTooltipText();
                        List<String> lines = (text.indexOf('\n') == -1) ?
                                Collections.singletonList(text) : Arrays.asList(text.split("\n"));
                        activeTheme.drawTooltip(gfx, lines, mouseX, mouseY, screen.width, screen.height);
                    }
                }
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        GuiScreen screen = event.getGui();
        if (screen == null) return;
        List<IGuiOverlay> active = getActiveOverlays(screen);
        if (active.isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return;
        int mouseX = UIInput.safeMouseX(mc, screen.width);
        int mouseY = UIInput.safeMouseY(mc, screen.height);
        int button = UIInput.getEventButton();
        boolean buttonState = UIInput.getEventButtonState();
        int wheel = UIInput.getEventDWheel();
        if (wheel != 0) {
            for (IGuiOverlay overlay : active) {
                GuiComponent root = overlay.getRootComponent();
                if (root != null && root.isVisible() && root.contains(mouseX, mouseY)) {
                    if (root.onMouseScroll(mouseX, mouseY, wheel)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
        if (button != -1) {
            if (buttonState) {
                for (IGuiOverlay overlay : active) {
                    GuiComponent root = overlay.getRootComponent();
                    if (root != null && root.isVisible() && root.contains(mouseX, mouseY)) {
                        if (root.onMouseClick(mouseX, mouseY, button)) {
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
            } else {
                for (IGuiOverlay overlay : active) {
                    GuiComponent root = overlay.getRootComponent();
                    if (root != null && root.isVisible()) {
                        if (root.onMouseRelease(mouseX, mouseY, button)) {
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
            }
        } else if (UIInput.isMouseButtonDown(0)) {
            for (IGuiOverlay overlay : active) {
                GuiComponent root = overlay.getRootComponent();
                if (root != null && root.isVisible() && root.contains(mouseX, mouseY)) {
                    root.onMouseDrag(mouseX, mouseY, 0, 0);
                }
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        GuiScreen screen = event.getGui();
        if (screen == null) return;
        List<IGuiOverlay> active = getActiveOverlays(screen);
        if (active.isEmpty()) return;
        if (UIInput.getEventKeyState()) {
            char typedChar = UIInput.getEventCharacter();
            int keyCode = UIInput.getEventKey();
            for (IGuiOverlay overlay : active) {
                GuiComponent root = overlay.getRootComponent();
                if (root != null && root.isVisible()) {
                    if (root.onKeyPress(typedChar, keyCode)) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen nextScreen = event.getGui();
        if (currentScreen != null && currentScreen != nextScreen) {
            List<IGuiOverlay> active = getActiveOverlays(currentScreen);
            for (IGuiOverlay overlay : active) {
                overlay.onClosed(currentScreen);
            }
            GLGraphics.INSTANCE.clearClip();
        }
        this.currentScreen = nextScreen;
    }
}