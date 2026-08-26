package com.x4yi.x4ui.client.gui.utils;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiScrollPanel;
import com.x4yi.x4ui.client.gui.component.GuiMarkdown;
import com.x4yi.x4ui.client.gui.component.GuiModelRender;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import com.x4yi.x4ui.client.gui.component.GuiSprite;
import com.x4yi.x4ui.client.gui.component.GuiTextInput;
import com.x4yi.x4ui.client.gui.component.GuiDropdown;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderInt;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderFloat;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderDouble;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import com.x4yi.x4ui.client.gui.utils.State;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
public class GuiBuilder<T extends GuiComponent> {
    private final T component;
    private GuiBuilder(T component) {
        this.component = component;
    }
    public static GuiBuilder<GuiPanel> createPanel() {
        return new GuiBuilder<>(new GuiPanel(0, 0, 100, 100));
    }
    public static GuiBuilder<GuiScrollPanel> createScrollPanel() {
        return new GuiBuilder<>(new GuiScrollPanel(0, 0, 100, 100));
    }
    public static GuiBuilder<GuiButton> createButton(String text) {
        return new GuiBuilder<>(new GuiButton(0, 0, 100, 20, text, null));
    }
    public static GuiBuilder<GuiLabel> createLabel(String text) {
        return new GuiBuilder<>(new GuiLabel(0, 0, text, 0xFFFFFFFF));
    }
    public static GuiBuilder<GuiLabel> createLabel(String text, int color) {
        return new GuiBuilder<>(new GuiLabel(0, 0, text, color));
    }
    public static GuiBuilder<GuiMarkdown> createMarkdown(String text) {
        return new GuiBuilder<>(new GuiMarkdown(0, 0, 0, text));
    }
    public static GuiBuilder<GuiModelRender> createModelRender() {
        return new GuiBuilder<>(new GuiModelRender(0, 0, 0, 0));
    }
    public static GuiBuilder<GuiSlot> createSlot(Slot slot, GuiBaseContainer container) {
        return new GuiBuilder<>(new GuiSlot(slot, container));
    }
    public static GuiBuilder<GuiSprite> createSprite(ResourceLocation texture, int textureWidth, int textureHeight) {
        return new GuiBuilder<>(new GuiSprite(0, 0, 0, 0, texture, textureWidth, textureHeight));
    }
    public static GuiBuilder<GuiSliderInt> createSliderInt(State<Integer> state, int min, int max, int step) {
        return new GuiBuilder<>(new GuiSliderInt(0, 0, 100, 10, state, min, max, step));
    }
    public static GuiBuilder<GuiSliderFloat> createSliderFloat(State<Float> state, float min, float max, float step) {
        return new GuiBuilder<>(new GuiSliderFloat(0, 0, 100, 10, state, min, max, step));
    }
    public static GuiBuilder<GuiSliderDouble> createSliderDouble(State<Double> state, double min, double max, double step) {
        return new GuiBuilder<>(new GuiSliderDouble(0, 0, 100, 10, state, min, max, step));
    }
    public static GuiBuilder<GuiTextInput> createTextInput() {
        return new GuiBuilder<>(new GuiTextInput(0, 0, 100, 20));
    }
    public static <E> GuiBuilder<GuiDropdown<E>> createDropdown(State<E> state, java.util.List<E> options) {
        return new GuiBuilder<>(new GuiDropdown<>(0, 0, 100, 20, state, options));
    }
    public GuiBuilder<T> position(int x, int y) {
        component.setX(x);
        component.setY(y);
        return this;
    }
    public GuiBuilder<T> size(int width, int height) {
        component.setWidth(width);
        component.setHeight(height);
        return this;
    }
    public GuiBuilder<T> margin(int margin) {
        component.setMargin(margin);
        return this;
    }
    public GuiBuilder<T> padding(int padding) {
        component.setPadding(padding);
        return this;
    }
    public GuiBuilder<T> visible(boolean visible) {
        component.setVisible(visible);
        return this;
    }
    public GuiBuilder<T> enabled(boolean enabled) {
        component.setEnabled(enabled);
        return this;
    }
    public GuiBuilder<T> layer(int layer) {
        component.setLayer(layer);
        return this;
    }
    public GuiBuilder<T> draggable(boolean draggable) {
        component.setDraggable(draggable);
        if (draggable) component.setDragConstraint(GuiComponent.DragConstraint.NONE);
        return this;
    }
    public GuiBuilder<T> draggableConstrained(boolean draggable) {
        component.setDraggable(draggable);
        if (draggable) component.setDragConstraint(GuiComponent.DragConstraint.PARENT_BOUNDS);
        return this;
    }
    public GuiBuilder<T> flexDirection(FlexDirection direction) {
        if (component instanceof GuiPanel) {
            ((GuiPanel) component).setFlexDirection(direction);
        }
        return this;
    }
    public GuiBuilder<T> gap(int gap) {
        if (component instanceof GuiPanel) {
            ((GuiPanel) component).setGap(gap);
        }
        return this;
    }
    public GuiBuilder<T> flexWrap(boolean flexWrap) {
        if (component instanceof GuiPanel) {
            ((GuiPanel) component).setFlexWrap(flexWrap);
        }
        return this;
    }
    public GuiBuilder<T> onClick(Runnable action) {
        if (component instanceof GuiButton) {
            ((GuiButton) component).setOnClick(action);
        }
        return this;
    }
    public GuiBuilder<T> onLinkClicked(java.util.function.Consumer<String> consumer) {
        if (component instanceof GuiMarkdown) {
            ((GuiMarkdown) component).onLinkClicked = consumer;
        }
        return this;
    }
    public GuiBuilder<T> entity(EntityLivingBase entity) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setEntity(entity);
        }
        return this;
    }
    public GuiBuilder<T> itemStack(ItemStack stack) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setItemStack(stack);
        }
        return this;
    }
    public GuiBuilder<T> modelScale(float scale) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setScale(scale);
        }
        return this;
    }
    public GuiBuilder<T> modelRotation(float yaw, float pitch, float roll) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setRotation(yaw, pitch, roll);
        }
        return this;
    }
    public GuiBuilder<T> modelTranslation(float x, float y, float z) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setTranslation(x, y, z);
        }
        return this;
    }
    public GuiBuilder<T> modelDragRotation(boolean allow) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setAllowDragRotation(allow);
        }
        return this;
    }
    public GuiBuilder<T> modelLookAtCursor(boolean lookAtCursor) {
        if (component instanceof GuiModelRender) {
            ((GuiModelRender) component).setLookAtCursor(lookAtCursor);
        }
        return this;
    }
    public GuiBuilder<T> placeholder(String placeholder) {
        if (component instanceof GuiTextInput) {
            ((GuiTextInput) component).setPlaceholder(placeholder);
        }
        return this;
    }
    public GuiBuilder<T> tooltip(String text) {
        component.setTooltip(text);
        return this;
    }
    public GuiBuilder<T> bindVisible(State<Boolean> state) {
        state.addListener(component::setVisible);
        component.setVisible(state.get());
        return this;
    }
    public GuiBuilder<T> bindEnabled(State<Boolean> state) {
        state.addListener(component::setEnabled);
        component.setEnabled(state.get());
        return this;
    }
    public GuiBuilder<T> bindLabelText(State<String> state) {
        if (component instanceof GuiLabel) {
            state.addListener(((GuiLabel) component)::setText);
            ((GuiLabel) component).setText(state.get());
        }
        return this;
    }
    public GuiBuilder<T> sliceSize(int size) {
        if (component instanceof GuiSprite) {
            ((GuiSprite) component).setSliceSize(size);
        }
        return this;
    }
    public GuiBuilder<T> withChild(GuiComponent child) {
        component.addChild(child);
        return this;
    }
    public T build() {
        return component;
    }
}