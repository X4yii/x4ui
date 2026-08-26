package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.utils.State;
public abstract class GuiSlider<T extends Number> extends GuiComponent {
    protected State<T> state;
    protected T min;
    protected T max;
    protected T step;
    protected boolean dragging = false;
    protected boolean horizontal = true;
    protected int trackColor = 0xFF2C2C36;
    protected int fillColor = 0xFF29B6F6;
    protected int handleColor = 0xFFFFFFFF;
    protected int handleHoverColor = 0xFFE0E0E6;
    public GuiSlider(int x, int y, int width, int height, State<T> state, T min, T max, T step) {
        super(x, y, width, height);
        this.state = state;
        this.min = min;
        this.max = max;
        this.step = step;
        this.state.addListener(val -> markDirty());
    }
    protected abstract double getPercentage(T value);
    protected abstract T getValueFromPercentage(double percentage);
    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }
    public void setTrackColor(int trackColor) { this.trackColor = trackColor; }
    public void setFillColor(int fillColor) { this.fillColor = fillColor; }
    public void setHandleColor(int handleColor) { this.handleColor = handleColor; }
    public void setHandleHoverColor(int handleHoverColor) { this.handleHoverColor = handleHoverColor; }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        drawRect(absX, absY + height / 2 - 2, absX + width, absY + height / 2 + 2, trackColor);
        double perc = getPercentage(state.get());
        int fillWidth = (int) (width * perc);
        drawRect(absX, absY + height / 2 - 2, absX + fillWidth, absY + height / 2 + 2, fillColor);
        boolean hovered = isMouseOver(mouseX, mouseY);
        int hColor = (hovered || dragging) ? handleHoverColor : handleColor;
        int handleX = absX + fillWidth - 3;
        drawRect(handleX, absY, handleX + 6, absY + height, hColor);
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!super.mouseClicked(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && enabled && mouseButton == 0) {
                dragging = true;
                updateValueFromMouse(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int stateId) {
        if (dragging && stateId == 0) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, stateId);
    }
    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            updateValueFromMouse(mouseX, mouseY);
            return true;
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
    protected void updateValueFromMouse(int mouseX, int mouseY) {
        int absX = getAbsoluteX();
        double perc = (double) (mouseX - absX) / (double) width;
        perc = Math.max(0.0, Math.min(1.0, perc));
        state.set(getValueFromPercentage(perc));
    }
}