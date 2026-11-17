package com.x4yi.x4ui.client.gui.utils;

import com.x4yi.x4ui.client.gui.ScreenRegistry;
import com.x4yi.x4ui.client.gui.base.GuiBaseScreen;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.input.KeyCodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Official X4UI screen entry point and builder API.
 * Allows consumer mods to build, register, open, and close X4UI screens without
 * importing Minecraft's {@link GuiScreen} or {@link Minecraft#displayGuiScreen}.
 */
public final class GuiBuilder {
    private GuiBuilder() {}

    public static ScreenBuilder screen(String title) {
        return new ScreenBuilder(title);
    }

    public static void register(String screenId, Function<NBTTagCompound, GuiBaseScreen> factory) {
        if (screenId == null || factory == null) return;
        ScreenRegistry.register(screenId, factory::apply);
    }

    public static GuiBaseScreen modal(String title, int maxWidth, int maxHeight, Consumer<GuiPanel> bodyBuilder) {
        GuiBaseScreen screen = screen(title)
                .window(maxWidth, maxHeight)
                .onInit((s, root) -> {
                    int textColor = root.getTheme() != null ? root.getTheme().getTextColor() : 0xFFFFFFFF;
                    GuiPanel header = GuiPanel.row(8);
                    header.add(GuiLabel.of(title != null ? title : "", textColor));
                    header.add(GuiPanel.spacer());
                    header.add(new GuiButton(0, 0, 20, 20, "X", s::closeScreen));
                    root.addChild(header);
                    if (bodyBuilder != null) {
                        bodyBuilder.accept(root);
                    }
                })
                .build();
        open(screen);
        return screen;
    }

    public static void open(GuiBaseScreen screen) {
        if (screen == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(screen);
        }
    }

    public static void close() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            if (mc.currentScreen instanceof GuiBaseScreen) {
                ((GuiBaseScreen) mc.currentScreen).closeScreen();
            } else {
                mc.displayGuiScreen(null);
            }
        }
    }

    public static final class ScreenBuilder {
        private final String title;
        private GuiScreen parentScreen;
        private boolean hasExplicitParent = false;
        private int windowMaxWidth = 0;
        private int windowMaxHeight = 0;
        private boolean dimBackground = true;
        private Integer dimColor = null;
        private boolean animateOpen = false;
        private boolean escCloses = true;
        private boolean pauseGame = false;
        private String channelId = null;
        private ITheme theme;
        private BiConsumer<GuiBaseScreen, GuiPanel> onInit;
        private BiConsumer<Integer, Integer> onResize;
        private Runnable onClose;

        private ScreenBuilder(String title) {
            this.title = title != null ? title : "";
        }

        public ScreenBuilder parent(GuiScreen parentScreen) {
            this.parentScreen = parentScreen;
            this.hasExplicitParent = true;
            return this;
        }

        public ScreenBuilder window(int maxWidth, int maxHeight) {
            this.windowMaxWidth = maxWidth;
            this.windowMaxHeight = maxHeight;
            return this;
        }

        public ScreenBuilder dimBackground(boolean dimBackground) {
            this.dimBackground = dimBackground;
            return this;
        }

        public ScreenBuilder dimColor(int argb) {
            this.dimColor = argb;
            return this;
        }

        public ScreenBuilder animateOpen(boolean animateOpen) {
            this.animateOpen = animateOpen;
            return this;
        }

        public ScreenBuilder escCloses(boolean escCloses) {
            this.escCloses = escCloses;
            return this;
        }

        public ScreenBuilder pauseGame(boolean pauseGame) {
            this.pauseGame = pauseGame;
            return this;
        }

        public ScreenBuilder bindChannel(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public ScreenBuilder theme(ITheme theme) {
            this.theme = theme;
            return this;
        }

        public ScreenBuilder onInit(BiConsumer<GuiBaseScreen, GuiPanel> onInit) {
            this.onInit = onInit;
            return this;
        }

        public ScreenBuilder onResize(BiConsumer<Integer, Integer> onResize) {
            this.onResize = onResize;
            return this;
        }

        public ScreenBuilder onClose(Runnable onClose) {
            this.onClose = onClose;
            return this;
        }

        public GuiBaseScreen build() {
            GuiScreen resolvedParent = parentScreen;
            if (!hasExplicitParent) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) {
                    resolvedParent = mc.currentScreen;
                }
            }
            final int wMax = windowMaxWidth;
            final int hMax = windowMaxHeight;
            final boolean dim = dimBackground;
            final Integer customDimColor = dimColor;
            final boolean anim = animateOpen;
            final boolean allowEsc = escCloses;
            final boolean pauses = pauseGame;
            final String ch = channelId;
            final ITheme customTheme = theme;
            final BiConsumer<GuiBaseScreen, GuiPanel> initCb = onInit;
            final BiConsumer<Integer, Integer> resizeCb = onResize;
            final Runnable closeCb = onClose;

            GuiBaseScreen screen = new GuiBaseScreen(resolvedParent, title) {
                @Override
                protected void initComponents() {
                    if (customTheme != null) {
                        setTheme(customTheme);
                    }
                    if (ch != null && !ch.isEmpty()) {
                        bindChannel(ch);
                    }
                    GuiPanel container = wMax > 0 ? createCenteredWindow(wMax, hMax) : getRootPanel();
                    if (initCb != null) {
                        initCb.accept(this, container);
                    }
                }

                @Override
                protected void onScreenResize(int newWidth, int newHeight) {
                    super.onScreenResize(newWidth, newHeight);
                    if (resizeCb != null) {
                        resizeCb.accept(newWidth, newHeight);
                    }
                }

                @Override
                protected void keyTyped(char typedChar, int keyCode) throws IOException {
                    if (!allowEsc && keyCode == KeyCodes.ESCAPE) {
                        if (getRootPanel() != null) {
                            getRootPanel().onKeyPress(typedChar, keyCode);
                        }
                        return;
                    }
                    super.keyTyped(typedChar, keyCode);
                }

                @Override
                public boolean doesGuiPauseGame() {
                    return pauses;
                }

                @Override
                public void onGuiClosed() {
                    super.onGuiClosed();
                    if (closeCb != null) {
                        closeCb.run();
                    }
                }
            };
            screen.setDimBackground(dim);
            if (customDimColor != null) {
                screen.setDimColor(customDimColor);
            }
            screen.setAnimateOpen(anim);
            return screen;
        }

        public GuiBaseScreen open() {
            GuiBaseScreen screen = build();
            GuiBuilder.open(screen);
            return screen;
        }
    }
}
