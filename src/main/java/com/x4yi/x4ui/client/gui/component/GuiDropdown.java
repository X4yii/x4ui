package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.input.KeyCodes;
import com.x4yi.x4ui.client.gui.utils.ITheme;
import com.x4yi.x4ui.common.State;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

public class GuiDropdown<T> extends GuiComponent {
    protected State<T> state;
    protected List<T> options;
    protected boolean open = false;
    protected int itemHeight = 15;
    protected int maxVisibleItems = 5;
    protected int scrollOffset = 0;
    protected int highlightedIndex = -1;
    protected int bgColor = 0;
    protected int borderColor = 0;
    protected int textColor = 0;
    protected int hoverColor = 0;

    public GuiDropdown(int x, int y, int width, int height, State<T> state, List<T> options) {
        super(x, y, width, height);
        this.state = state;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.layer = 100;
    }

    public static <T> GuiDropdown<T> of(State<T> state, List<T> options) {
        GuiDropdown<T> dd = new GuiDropdown<>(0, 0, 140, 22, state, options);
        dd.withPercentWidth(1.0f);
        return dd;
    }

    public void setOptions(List<T> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.scrollOffset = 0;
        this.highlightedIndex = -1;
    }

    public int getMaxVisibleItems() {
        return maxVisibleItems;
    }

    public void setMaxVisibleItems(int maxVisibleItems) {
        this.maxVisibleItems = Math.max(1, maxVisibleItems);
    }

    private int originalLayer = 0;
    private boolean layerSaved = false;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        if (this.open != open) {
            this.open = open;
            if (open) {
                if (!layerSaved) {
                    this.originalLayer = this.layer;
                    layerSaved = true;
                }
                this.setLayer(originalLayer + 100);
                this.highlightedIndex = findSelectedIndex();
                this.requestFocus();
            } else {
                this.scrollOffset = 0;
                this.highlightedIndex = -1;
                if (layerSaved) {
                    this.setLayer(originalLayer);
                    layerSaved = false;
                }
                if (isFocused()) {
                    this.clearFocus();
                }
            }
        }
    }

    private int findSelectedIndex() {
        if (state == null || options.isEmpty()) return -1;
        T current = state.get();
        for (int i = 0; i < options.size(); i++) {
            T opt = options.get(i);
            if (current == null ? opt == null : current.equals(opt)) {
                return i;
            }
        }
        return 0;
    }

    protected int getPopupHeight() {
        return Math.min(options.size(), maxVisibleItems) * itemHeight;
    }

    protected boolean opensUpward() {
        int listHeight = getPopupHeight();
        int absY = getAbsoluteY();
        Minecraft client = Minecraft.getMinecraft();
        int screenHeight = (client != null && client.currentScreen != null) ? client.currentScreen.height : 10000;
        return (absY + height + listHeight > screenHeight) && (absY - listHeight >= 0);
    }

    protected int getPopupY() {
        int absY = getAbsoluteY();
        return opensUpward() ? (absY - getPopupHeight()) : (absY + height);
    }

    @Override
    protected boolean isMouseOver(int mouseX, int mouseY) {
        if (super.isMouseOver(mouseX, mouseY)) return true;
        if (open && !options.isEmpty()) {
            int absX = getAbsoluteX();
            int popupY = getPopupY();
            int listHeight = getPopupHeight();
            return mouseX >= absX && mouseX <= absX + width && mouseY >= popupY && mouseY <= popupY + listHeight;
        }
        return false;
    }

    private int resolveBgColor() {
        if (bgColor != 0) return bgColor;
        ITheme t = getTheme();
        return t != null ? t.getTextInputBackgroundColor() : 0xFF16161E;
    }

    private int resolveBorderColor() {
        if (borderColor != 0) return borderColor;
        ITheme t = getTheme();
        return t != null ? t.getTextInputBorderColor() : 0xFF2C2C36;
    }

    private int resolveTextColor() {
        if (textColor != 0) return textColor;
        ITheme t = getTheme();
        return t != null ? t.getTextColor() : 0xFFFFFFFF;
    }

    private int resolveHoverColor() {
        if (hoverColor != 0) return hoverColor;
        ITheme t = getTheme();
        return t != null ? t.getButtonHoverColor() : 0xFF2C2C36;
    }

    private int resolveAccentColor() {
        ITheme t = getTheme();
        return t != null ? t.getPrimaryColor() : 0xFF64B5F6;
    }

    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        int bg = resolveBgColor();
        int border = resolveBorderColor();
        int txt = resolveTextColor();
        gfx.drawRect(absX, absY, width, height, bg);
        gfx.drawBorder(absX, absY, width, height, 1, border);
        T val = state != null ? state.get() : null;
        String currentText = val != null ? val.toString() : "";
        int maxTextW = Math.max(0, width - 18);
        String trimmed = getFont().trimStringToWidth(currentText, maxTextW);
        int fontHeight = gfx.getFontHeight();
        gfx.drawString(trimmed, absX + 4, absY + (height - fontHeight) / 2.0f, txt, false);
        gfx.drawString(open ? (opensUpward() ? "▼" : "▲") : "▼", absX + width - 12, absY + (height - fontHeight) / 2.0f, txt, false);
    }

    @Override
    protected void renderChildren(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        super.renderChildren(gfx, mouseX, mouseY, partialTicks);
        if (open && !options.isEmpty()) {
            drawPopup(gfx, mouseX, mouseY);
        }
    }

    protected void drawPopup(IGraphics gfx, int mouseX, int mouseY) {
        int absX = getAbsoluteX();
        int popupY = getPopupY();
        int listHeight = getPopupHeight();
        int bg = resolveBgColor();
        int border = resolveBorderColor();
        int txt = resolveTextColor();
        int hover = resolveHoverColor();
        gfx.drawRect(absX, popupY, width, listHeight, bg);
        gfx.drawBorder(absX, popupY, width, listHeight, 1, border);
        int fontHeight = gfx.getFontHeight();
        int maxItemW = Math.max(0, width - 10);
        int visibleCount = Math.min(options.size() - scrollOffset, maxVisibleItems);
        for (int i = 0; i < visibleCount; i++) {
            int optIdx = scrollOffset + i;
            if (optIdx >= options.size()) break;
            T opt = options.get(optIdx);
            int itemY = popupY + i * itemHeight;
            boolean hovered = mouseX >= absX && mouseX <= absX + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
            if (hovered || optIdx == highlightedIndex) {
                gfx.drawRect(absX + 1, itemY, width - 2, itemHeight, hover);
            }
            String label = opt != null ? opt.toString() : "";
            gfx.drawString(getFont().trimStringToWidth(label, maxItemW), absX + 4, itemY + (itemHeight - fontHeight) / 2.0f, txt, false);
        }
        if (options.size() > maxVisibleItems) {
            int maxOffset = options.size() - maxVisibleItems;
            int trackX = absX + width - 3;
            int thumbHeight = Math.max(6, (int) ((float) maxVisibleItems / options.size() * listHeight));
            int thumbY = popupY + (int) ((float) scrollOffset / maxOffset * (listHeight - thumbHeight));
            gfx.drawRect(trackX, thumbY, 2, thumbHeight, resolveAccentColor());
        }
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled) return false;
        int absX = getAbsoluteX();
        if (open) {
            int popupY = getPopupY();
            int listHeight = getPopupHeight();
            if (mouseX >= absX && mouseX <= absX + width && mouseY >= popupY && mouseY <= popupY + listHeight) {
                int clickedIndex = scrollOffset + (mouseY - popupY) / itemHeight;
                if (clickedIndex >= 0 && clickedIndex < options.size()) {
                    if (state != null) {
                        state.set(options.get(clickedIndex));
                    }
                    setOpen(false);
                    return true;
                }
            } else if (!super.isMouseOver(mouseX, mouseY)) {
                setOpen(false);
                return false;
            }
        }
        if (super.isMouseOver(mouseX, mouseY)) {
            setOpen(!open);
            return true;
        }
        return super.onMouseClick(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!visible || !enabled || !open || options.size() <= maxVisibleItems) {
            return super.onMouseScroll(mouseX, mouseY, wheel);
        }
        int absX = getAbsoluteX();
        int popupY = getPopupY();
        int listHeight = getPopupHeight();
        if (mouseX >= absX && mouseX <= absX + width && mouseY >= popupY && mouseY <= popupY + listHeight) {
            int maxOffset = options.size() - maxVisibleItems;
            int direction = (wheel > 0) ? -1 : 1;
            scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset + direction));
            return true;
        }
        return super.onMouseScroll(mouseX, mouseY, wheel);
    }

    @Override
    public boolean onKeyPress(char typedChar, int keyCode) {
        if (!visible || !enabled || !open) {
            return super.onKeyPress(typedChar, keyCode);
        }
        if (keyCode == KeyCodes.ESCAPE) {
            setOpen(false);
            return true;
        }
        if (options.isEmpty()) return false;
        if (keyCode == KeyCodes.UP) {
            highlightedIndex = (highlightedIndex <= 0) ? options.size() - 1 : highlightedIndex - 1;
            ensureHighlightedVisible();
            return true;
        }
        if (keyCode == KeyCodes.DOWN) {
            highlightedIndex = (highlightedIndex < 0 || highlightedIndex >= options.size() - 1) ? 0 : highlightedIndex + 1;
            ensureHighlightedVisible();
            return true;
        }
        if (keyCode == KeyCodes.ENTER) {
            if (highlightedIndex >= 0 && highlightedIndex < options.size() && state != null) {
                state.set(options.get(highlightedIndex));
            }
            setOpen(false);
            return true;
        }
        return super.onKeyPress(typedChar, keyCode);
    }

    private void ensureHighlightedVisible() {
        if (highlightedIndex < 0) return;
        if (highlightedIndex < scrollOffset) {
            scrollOffset = highlightedIndex;
        } else if (highlightedIndex >= scrollOffset + maxVisibleItems) {
            scrollOffset = Math.max(0, highlightedIndex - maxVisibleItems + 1);
        }
    }
}
