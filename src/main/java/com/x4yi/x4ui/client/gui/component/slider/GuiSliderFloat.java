package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.common.State;
public class GuiSliderFloat extends GuiSlider<Float> {
    public GuiSliderFloat(int x, int y, int width, int height, State<Float> state, Float min, Float max, Float step) {
        super(x, y, width, height, state, min, max, step);
    }
    public static GuiSliderFloat of(State<Float> state, float min, float max, float step) {
        GuiSliderFloat s = new GuiSliderFloat(0, 0, 140, 20, state, min, max, step);
        s.withPercentWidth(1.0f);
        return s;
    }
    @Override
    protected double getPercentage(Float value) {
        if (value == null || max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) (value - min) / (max - min)));
    }
    @Override
    protected Float getValueFromPercentage(double percentage) {
        double val = min + percentage * (max - min);
        if (step > 0) {
            val = min + Math.round((val - min) / step) * step;
        }
        float floatVal = (float) val;
        return Math.max(min, Math.min(max, floatVal));
    }
}