package com.x4yi.x4ui.client.gui.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

public class GuiVirtualList<T> extends GuiScrollPanel {
    private final List<T> items = new ArrayList<>();
    private final Function<T, GuiComponent> componentBuilder;
    private final int itemHeight;
    private final Map<Integer, GuiComponent> activeComponents = new HashMap<>();
    private int lastStartIndex = -1;
    private int lastEndIndex = -1;

    public GuiVirtualList(int x, int y, int width, int height, int itemHeight, Function<T, GuiComponent> builder) {
        super(x, y, width, height);
        this.itemHeight = itemHeight;
        this.componentBuilder = builder;
        this.contentPanel.setFlexDirection(FlexDirection.ABSOLUTE);
    }

    public void setItems(List<T> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        
        for (GuiComponent comp : activeComponents.values()) {
            this.contentPanel.removeChild(comp);
            comp.destroy();
        }
        activeComponents.clear();
        this.lastStartIndex = -1;
        
        int totalHeight = items.size() * itemHeight;
        this.contentPanel.setHeight(totalHeight);
        this.maxScrollY = Math.max(0, totalHeight - this.height);
        
        if (targetScrollY > maxScrollY) {
            targetScrollY = maxScrollY;
            scrollY = maxScrollY;
        }
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        if (items.isEmpty()) return;

        int scrollViewY = (int) scrollY;
        int startIndex = Math.max(0, scrollViewY / itemHeight);
        int endIndex = Math.min(items.size(), (scrollViewY + this.height) / itemHeight + 2);

        if (startIndex == lastStartIndex && endIndex == lastEndIndex) {
            return;
        }

        List<Integer> toRemove = new ArrayList<>();
        for (Integer index : activeComponents.keySet()) {
            if (index < startIndex || index >= endIndex) {
                toRemove.add(index);
            }
        }

        for (Integer index : toRemove) {
            GuiComponent comp = activeComponents.remove(index);
            this.contentPanel.removeChild(comp);
            comp.destroy();
        }

        for (int i = startIndex; i < endIndex; i++) {
            if (!activeComponents.containsKey(i)) {
                T item = items.get(i);
                GuiComponent comp = componentBuilder.apply(item);
                comp.setY(i * itemHeight);
                this.contentPanel.addChild(comp);
                activeComponents.put(i, comp);
            }
        }

        this.lastStartIndex = startIndex;
        this.lastEndIndex = endIndex;
    }
}
