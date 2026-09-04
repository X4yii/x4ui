package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.common.State;
public class GuiSliderFloat extends GuiSlider<Float> {
    public GuiSliderFloat(int x, int y, int width, int height, State<Float> state, Float min, Float max, Float step) {
        super(x, y, width, height, state, min, max, step);
    }
    @Override
    protected double getPercentage(Float value) {
        if (max <= min) return 0.0;
        return (double) (value - min) / (max - min);
    }
    @Override
    protected Float getValueFromPercentage(double percentage) {
        double val = min + percentage * (max - min);
        if (step > 0) {
            val = Math.round(val / step) * step;
        }
        float floatVal = (float) val;
        return Math.max(min, Math.min(max, floatVal));
    }
}