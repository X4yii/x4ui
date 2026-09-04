package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.common.State;
public abstract class GuiSlider<T extends Number> extends GuiComponent {
    protected State<T> state;
    protected T min;
    protected T max;
    protected T step;
    protected boolean dragging = false;
    protected boolean horizontal = true;
    public GuiSlider(int x, int y, int width, int height, State<T> state, T min, T max, T step) {
        super(x, y, width, height);
        this.state = state;
        this.min = min;
        this.max = max;
        this.step = step;
        this.bindState(this.state, val -> requestRender());
    }
    protected abstract double getPercentage(T value);
    protected abstract T getValueFromPercentage(double percentage);
    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
        drawRect(absX, absY + height / 2 - 2, absX + width, absY + height / 2 + 2, theme.getSliderTrackColor());
        double perc = getPercentage(state.get());
        int fillWidth = (int) (width * perc);
        drawRect(absX, absY + height / 2 - 2, absX + fillWidth, absY + height / 2 + 2, theme.getSliderFillColor());
        boolean hovered = isMouseOver(mouseX, mouseY);
        int hColor = (hovered || dragging) ? theme.getSliderHandleHoverColor() : theme.getSliderHandleColor();
        int handleX = absX + fillWidth - 3;
        drawRect(handleX, absY, handleX + 6, absY + height, hColor);
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && enabled && mouseButton == 0) {
                dragging = true;
                updateValueFromMouse(mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int stateId) {
        if (dragging && stateId == 0) {
            dragging = false;
            return true;
        }
        return super.onMouseRelease(mouseX, mouseY, stateId);
    }
    @Override
    public boolean onMouseDrag(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (dragging) {
            updateValueFromMouse(mouseX, mouseY);
            return true;
        }
        return super.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
    protected void updateValueFromMouse(int mouseX, int mouseY) {
        int absX = getAbsoluteX();
        double perc = (double) (mouseX - absX) / (double) width;
        perc = Math.max(0.0, Math.min(1.0, perc));
        state.set(getValueFromPercentage(perc));
    }
}