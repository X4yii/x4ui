package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.utils.ITheme;
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
    public boolean isHorizontal() {
        return horizontal;
    }
    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }
    public GuiSlider<T> withHorizontal(boolean horizontal) {
        setHorizontal(horizontal);
        return this;
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        ITheme theme = getTheme();
        boolean hovered = isMouseOver(mouseX, mouseY);
        int hColor = (hovered || dragging) ? theme.getSliderHandleHoverColor() : theme.getSliderHandleColor();
        double perc = Math.max(0.0, Math.min(1.0, getPercentage(state.get())));
        if (horizontal) {
            int trackY = absY + height / 2 - 2;
            gfx.drawRect(absX, trackY, width, 4, theme.getSliderTrackColor());
            int fillWidth = (int) (width * perc);
            gfx.drawRect(absX, trackY, fillWidth, 4, theme.getSliderFillColor());
            int handleX = absX + fillWidth - 3;
            gfx.drawRect(handleX, absY, 6, height, hColor);
        } else {
            int trackX = absX + width / 2 - 2;
            gfx.drawRect(trackX, absY, 4, height, theme.getSliderTrackColor());
            int fillHeight = (int) (height * perc);
            int fillY = absY + height - fillHeight;
            gfx.drawRect(trackX, fillY, 4, fillHeight, theme.getSliderFillColor());
            int handleY = fillY - 3;
            gfx.drawRect(absX, handleY, width, 6, hColor);
        }
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
        double perc;
        if (horizontal) {
            int absX = getAbsoluteX();
            perc = (width > 0) ? (double) (mouseX - absX) / (double) width : 0.0;
        } else {
            int absY = getAbsoluteY();
            perc = (height > 0) ? (double) (absY + height - mouseY) / (double) height : 0.0;
        }
        perc = Math.max(0.0, Math.min(1.0, perc));
        state.set(getValueFromPercentage(perc));
    }
}