package com.x4yi.x4ui.client.gui.component;

import java.util.function.Consumer;

public class GuiSlider extends GuiComponent {
    protected String label;
    protected float minValue;
    protected float maxValue;
    protected float value;
    protected boolean isInt = false;
    
    protected Consumer<Float> onValueChanged;
    protected boolean isDragging = false;
    
    // Compatibility fields
    protected int bgColor = 0x333333;
    protected int sliderBgColor = 0x666666;
    protected int hoverSliderBgColor = 0x888888;
    protected int textColor = 0xFFFFFF;

    public GuiSlider(int x, int y, int width, int height, String label, float min, float max, float current, Consumer<Float> onValueChanged) {
        super(x, y, width, height);
        this.label = label;
        this.minValue = min;
        this.maxValue = max;
        this.value = current;
        this.onValueChanged = onValueChanged;
    }

    // Secondary constructor for compatibility if they used isInt in newer setups
    public GuiSlider(int x, int y, int width, int height, String label, float min, float max, float current, boolean isInt, Consumer<Float> onValueChanged) {
        this(x, y, width, height, label, min, max, current, onValueChanged);
        this.isInt = isInt;
    }

    public void setOnValueChanged(Consumer<Float> onValueChanged) {
        this.onValueChanged = onValueChanged;
    }

    public float getValue() { return value; }
    public void setValue(float value) { this.value = Math.max(minValue, Math.min(maxValue, value)); }
    
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        
        int labelColor = (isMouseOver(mouseX, mouseY) && enabled) ? 0xFFFFFFFF : 0xFFB0B0BB;
        
        String valStr = isInt ? String.format("%.0f", value) : String.format("%.1f", value);
        valStr = valStr.replace(",", ".");
        
        drawString(label + ": " + valStr, absX, absY + (height - 8) / 2, labelColor);

        int sliderW = 70;
        int sliderH = 6;
        int sliderX = absX + width - sliderW;
        int sliderY = absY + (height - sliderH) / 2;

        drawRect(sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, 0xFF25252B);

        float ratio = (value - minValue) / (maxValue - minValue);
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        
        int fillWidth = (int) (ratio * sliderW);
        drawRect(sliderX, sliderY, sliderX + fillWidth, sliderY + sliderH, 0xFF00C853);
        
        // Thumb
        drawRect(sliderX + fillWidth - 1, sliderY - 2, sliderX + fillWidth + 1, sliderY + sliderH + 2, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!super.mouseClicked(mouseX, mouseY, mouseButton)) {
            int absX = getAbsoluteX();
            int sliderW = 70;
            int sliderX = absX + width - sliderW;
            
            if (isMouseOver(mouseX, mouseY) && mouseX >= sliderX && enabled && mouseButton == 0) {
                isDragging = true;
                updateDrag(mouseX);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isDragging) {
            updateDrag(mouseX);
            return true;
        }
        return super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        if (isDragging) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }

    protected void updateDrag(int mouseX) {
        int absX = getAbsoluteX();
        int sliderW = 70;
        int sliderX = absX + width - sliderW;
        
        float ratio = (float) (mouseX - sliderX) / sliderW;
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        
        float val = minValue + ratio * (maxValue - minValue);
        if (isInt) {
            val = Math.round(val);
        } else {
            val = Math.round(val * 10f) / 10f;
        }
        
        if (this.value != val) {
            this.value = val;
            if (onValueChanged != null) {
                onValueChanged.accept(this.value);
            }
        }
    }
}
