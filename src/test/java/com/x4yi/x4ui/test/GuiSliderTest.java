package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderDouble;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderFloat;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderInt;
import com.x4yi.x4ui.common.State;
import org.junit.Assert;
import org.junit.Test;
public class GuiSliderTest {
    @Test
    public void testHorizontalSliderInteraction() {
        State<Integer> valState = new State<>(20);
        GuiSliderInt slider = new GuiSliderInt(0, 0, 100, 20, valState, 0, 100, 5);
        Assert.assertTrue(slider.isHorizontal());
        Assert.assertEquals(Integer.valueOf(20), valState.get());
        boolean handled = slider.onMouseClick(50, 10, 0);
        Assert.assertTrue(handled);
        Assert.assertEquals(Integer.valueOf(50), valState.get());
        slider.onMouseDrag(90, 10, 0, 100);
        Assert.assertEquals(Integer.valueOf(90), valState.get());
        slider.onMouseRelease(90, 10, 0);
    }
    @Test
    public void testVerticalSliderInteraction() {
        State<Integer> valState = new State<>(0);
        GuiSliderInt slider = new GuiSliderInt(0, 0, 20, 100, valState, 0, 100, 10);
        slider.withHorizontal(false);
        Assert.assertFalse(slider.isHorizontal());
        boolean handled = slider.onMouseClick(10, 20, 0);
        Assert.assertTrue(handled);
        Assert.assertEquals(Integer.valueOf(80), valState.get());
        slider.onMouseClick(10, 70, 0);
        Assert.assertEquals(Integer.valueOf(30), valState.get());
        slider.onMouseClick(10, 100, 0);
        Assert.assertEquals(Integer.valueOf(0), valState.get());
        slider.onMouseClick(10, 0, 0);
        Assert.assertEquals(Integer.valueOf(100), valState.get());
    }
    @Test
    public void testFloatAndDoubleSliders() {
        State<Float> floatState = new State<>(0.5f);
        GuiSliderFloat floatSlider = new GuiSliderFloat(0, 0, 200, 20, floatState, 0.0f, 1.0f, 0.05f);
        floatSlider.onMouseClick(150, 10, 0);
        Assert.assertEquals(0.75f, floatState.get(), 0.01f);
        State<Double> doubleState = new State<>(10.0);
        GuiSliderDouble doubleSlider = new GuiSliderDouble(0, 0, 100, 20, doubleState, 0.0, 50.0, 1.0);
        doubleSlider.onMouseClick(40, 10, 0);
        Assert.assertEquals(20.0, doubleState.get(), 0.01);
    }
}