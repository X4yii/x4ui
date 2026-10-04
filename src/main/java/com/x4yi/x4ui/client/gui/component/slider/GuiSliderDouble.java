package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.common.State;
public class GuiSliderDouble extends GuiSlider<Double> {
    public GuiSliderDouble(int x, int y, int width, int height, State<Double> state, Double min, Double max, Double step) {
        super(x, y, width, height, state, min, max, step);
    }
    @Override
    protected double getPercentage(Double value) {
        if (value == null || max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
    @Override
    protected Double getValueFromPercentage(double percentage) {
        double val = min + percentage * (max - min);
        if (step > 0) {
            val = min + Math.round((val - min) / step) * step;
        }
        return Math.max(min, Math.min(max, val));
    }
}