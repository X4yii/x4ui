package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.utils.State;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import java.util.List;
import java.util.ArrayList;
public class GuiDropdown<T> extends GuiComponent {
    protected State<T> state;
    protected List<T> options;
    protected boolean open = false;
    protected int itemHeight = 15;
    protected int maxVisibleItems = 5;
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
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        drawRect(absX, absY, absX + width, absY + height, bgColor);
        drawBorder(absX, absY, absX + width, absY + height, borderColor);
        String currentText = state.get() != null ? state.get().toString() : "";
        drawString(currentText, absX + 4, absY + (height - fontRenderer.FONT_HEIGHT) / 2, textColor);
        drawString(open ? "▲" : "▼", absX + width - 12, absY + (height - fontRenderer.FONT_HEIGHT) / 2, textColor);
    }
    @Override
    protected void drawChildren(int mouseX, int mouseY, float partialTicks) {
        super.drawChildren(mouseX, mouseY, partialTicks);
        if (open && !options.isEmpty()) {
            drawPopup(mouseX, mouseY);
        }
    }
    protected void drawPopup(int mouseX, int mouseY) {
        boolean scissorWasEnabled = GL11.glGetBoolean(GL11.GL_SCISSOR_TEST);
        if (scissorWasEnabled) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        int absX = getAbsoluteX();
        int absY = getAbsoluteY() + height;
        int listHeight = Math.min(options.size(), maxVisibleItems) * itemHeight;
        drawRect(absX, absY, absX + width, absY + listHeight, bgColor);
        drawBorder(absX, absY, absX + width, absY + listHeight, borderColor);
        for (int i = 0; i < options.size(); i++) {
            if (i >= maxVisibleItems) break;
            T opt = options.get(i);
            int itemY = absY + i * itemHeight;
            boolean hovered = mouseX >= absX && mouseX <= absX + width && mouseY >= itemY && mouseY <= itemY + itemHeight;
            if (hovered) {
                drawRect(absX + 1, itemY, absX + width - 1, itemY + itemHeight, hoverColor);
            }
            drawString(opt.toString(), absX + 4, itemY + (itemHeight - fontRenderer.FONT_HEIGHT) / 2, textColor);
        }
        if (scissorWasEnabled) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!enabled) return false;
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (open) {
            int listHeight = Math.min(options.size(), maxVisibleItems) * itemHeight;
            if (mouseX >= absX && mouseX <= absX + width && mouseY >= absY + height && mouseY <= absY + height + listHeight) {
                int index = (mouseY - (absY + height)) / itemHeight;
                if (index >= 0 && index < options.size()) {
                    state.set(options.get(index));
                    open = false;
                    return true;
                }
            } else if (!isMouseOver(mouseX, mouseY)) {
                open = false;
                return true;
            }
        }
        if (isMouseOver(mouseX, mouseY)) {
            open = !open;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}