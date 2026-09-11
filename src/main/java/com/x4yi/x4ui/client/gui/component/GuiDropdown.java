package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.gui.IGraphics;
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
    protected int bgColor = 0xFF16161E;
    protected int borderColor = 0xFF2C2C36;
    protected int textColor = 0xFFFFFFFF;
    protected int hoverColor = 0xFF2C2C36;
    public GuiDropdown(int x, int y, int width, int height, State<T> state, List<T> options) {
        super(x, y, width, height);
        this.state = state;
        this.options = new ArrayList<>(options);
        this.layer = 100;
    }
    public void setOptions(List<T> options) {
        this.options = new ArrayList<>(options);
        this.scrollOffset = 0;
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
            } else {
                this.scrollOffset = 0;
                if (layerSaved) {
                    this.setLayer(originalLayer);
                    layerSaved = false;
                }
            }
        }
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
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        gfx.drawRect(absX, absY, width, height, bgColor);
        gfx.drawBorder(absX, absY, width, height, 1, borderColor);
        String currentText = state.get() != null ? state.get().toString() : "";
        int fontHeight = gfx.getFontHeight();
        gfx.drawString(currentText, absX + 4, absY + (height - fontHeight) / 2.0f, textColor, false);
        gfx.drawString(open ? (opensUpward() ? "▼" : "▲") : "▼", absX + width - 12, absY + (height - fontHeight) / 2.0f, textColor, false);
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
        gfx.drawRect(absX, popupY, width, listHeight, bgColor);
        gfx.drawBorder(absX, popupY, width, listHeight, 1, borderColor);
        int fontHeight = gfx.getFontHeight();
        int visibleCount = Math.min(options.size() - scrollOffset, maxVisibleItems);
        for (int i = 0; i < visibleCount; i++) {
            int optIdx = scrollOffset + i;
            if (optIdx >= options.size()) break;
            T opt = options.get(optIdx);
            int itemY = popupY + i * itemHeight;
            boolean hovered = mouseX >= absX && mouseX <= absX + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
            if (hovered) {
                gfx.drawRect(absX + 1, itemY, width - 2, itemHeight, hoverColor);
            }
            gfx.drawString(opt != null ? opt.toString() : "", absX + 4, itemY + (itemHeight - fontHeight) / 2.0f, textColor, false);
        }
        if (options.size() > maxVisibleItems) {
            int maxOffset = options.size() - maxVisibleItems;
            int trackX = absX + width - 3;
            int thumbHeight = Math.max(6, (int) ((float) maxVisibleItems / options.size() * listHeight));
            int thumbY = popupY + (int) ((float) scrollOffset / maxOffset * (listHeight - thumbHeight));
            gfx.drawRect(trackX, thumbY, 2, thumbHeight, 0xFF64B5F6);
        }
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!enabled) return false;
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (open) {
            int popupY = getPopupY();
            int listHeight = getPopupHeight();
            if (mouseX >= absX && mouseX <= absX + width && mouseY >= popupY && mouseY <= popupY + listHeight) {
                int clickedIndex = scrollOffset + (mouseY - popupY) / itemHeight;
                if (clickedIndex >= 0 && clickedIndex < options.size()) {
                    state.set(options.get(clickedIndex));
                    setOpen(false);
                    return true;
                }
            } else if (!isMouseOver(mouseX, mouseY)) {
                setOpen(false);
                return false;
            }
        }
        if (isMouseOver(mouseX, mouseY)) {
            setOpen(!open);
            return true;
        }
        return super.onMouseClick(mouseX, mouseY, mouseButton);
    }
    @Override
    public boolean onMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!enabled || !open || options.size() <= maxVisibleItems) {
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
}