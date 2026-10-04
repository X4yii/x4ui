package com.x4yi.x4ui.client.gui.component.slider;
import com.x4yi.x4ui.common.State;
public class GuiSliderInt extends GuiSlider<Integer> {
    public GuiSliderInt(int x, int y, int width, int height, State<Integer> state, Integer min, Integer max, Integer step) {
        super(x, y, width, height, state, min, max, step);
    }
    @Override
    protected double getPercentage(Integer value) {
        if (value == null || max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (double) (value - min) / (max - min)));
    }
    @Override
    protected Integer getValueFromPercentage(double percentage) {
        double val = min + percentage * (max - min);
        if (step > 0) {
            val = min + Math.round((val - min) / step) * step;
        }
        int intVal = (int) Math.round(val);
        return Math.max(min, Math.min(max, intVal));
    }
}