package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Function;
public class GuiVirtualList<T> extends GuiScrollPanel {
    private final List<T> items = new ArrayList<>();
    private final Function<T, GuiComponent> componentBuilder;
    private final BiConsumer<GuiComponent, T> itemBinder;
    private final int itemHeight;
    private final Map<Integer, GuiComponent> activeComponents = new HashMap<>();
    private final Queue<GuiComponent> recyclePool = new ArrayDeque<>();
    private int lastStartIndex = -1;
    private int lastEndIndex = -1;
    private int lastWidth = -1;
    private int lastHeight = -1;
    public GuiVirtualList(int x, int y, int width, int height, int itemHeight, Function<T, GuiComponent> builder) {
        this(x, y, width, height, itemHeight, builder, null);
    }
    public GuiVirtualList(int x, int y, int width, int height, int itemHeight, Function<T, GuiComponent> builder, BiConsumer<GuiComponent, T> binder) {
        super(x, y, width, height);
        this.itemHeight = Math.max(1, itemHeight);
        this.componentBuilder = builder;
        this.itemBinder = binder;
        this.contentPanel.setFlexDirection(FlexDirection.ABSOLUTE);
    }
    public static <T> GuiVirtualList<T> of(int height, int itemHeight, Function<T, GuiComponent> builder, BiConsumer<GuiComponent, T> binder) {
        GuiVirtualList<T> list = new GuiVirtualList<>(0, 0, 0, height, itemHeight, builder, binder);
        list.withPercentWidth(1.0f);
        return list;
    }
    public void setItems(List<T> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        for (GuiComponent comp : activeComponents.values()) {
            if (itemBinder != null) {
                comp.setVisible(false);
                recyclePool.offer(comp);
            } else {
                this.contentPanel.removeChild(comp);
            }
        }
        activeComponents.clear();
        if (itemBinder == null) {
            for (GuiComponent comp : recyclePool) {
                this.contentPanel.removeChild(comp);
            }
            recyclePool.clear();
        }
        this.lastStartIndex = -1;
        this.lastEndIndex = -1;
        int totalHeight = items.size() * itemHeight;
        this.contentPanel.setHeight(totalHeight);
        this.maxScrollY = Math.max(0, totalHeight - this.height);
        if (targetScrollY > maxScrollY) {
            targetScrollY = maxScrollY;
            scrollY = maxScrollY;
        }
    }
    public List<T> getItems() {
        return items;
    }
    public int getItemHeight() {
        return itemHeight;
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (items.isEmpty()) return;
        int scrollViewY = (int) scrollY;
        int startIndex = Math.max(0, scrollViewY / itemHeight);
        int endIndex = Math.min(items.size(), (scrollViewY + this.height) / itemHeight + 2);
        if (startIndex == lastStartIndex && endIndex == lastEndIndex
                && this.width == lastWidth && this.height == lastHeight) {
            return;
        }
        boolean widthChanged = this.width != lastWidth;
        this.lastWidth = this.width;
        this.lastHeight = this.height;
        Iterator<Map.Entry<Integer, GuiComponent>> it = activeComponents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, GuiComponent> entry = it.next();
            int idx = entry.getKey();
            if (idx < startIndex || idx >= endIndex) {
                GuiComponent comp = entry.getValue();
                it.remove();
                if (itemBinder != null) {
                    comp.setVisible(false);
                    recyclePool.offer(comp);
                } else {
                    this.contentPanel.removeChild(comp);
                }
            } else if (widthChanged) {
                entry.getValue().requestLayout();
            }
        }
        for (int i = startIndex; i < endIndex; i++) {
            if (!activeComponents.containsKey(i)) {
                T item = items.get(i);
                GuiComponent comp = null;
                if (itemBinder != null && !recyclePool.isEmpty()) {
                    comp = recyclePool.poll();
                    comp.setVisible(true);
                    comp.setY(i * itemHeight);
                    itemBinder.accept(comp, item);
                } else if (componentBuilder != null) {
                    comp = componentBuilder.apply(item);
                    if (comp != null) {
                        comp.setY(i * itemHeight);
                        this.contentPanel.addChild(comp);
                        if (itemBinder != null) {
                            itemBinder.accept(comp, item);
                        }
                    }
                }
                if (comp != null) {
                    activeComponents.put(i, comp);
                }
            }
        }
        this.lastStartIndex = startIndex;
        this.lastEndIndex = endIndex;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        activeComponents.clear();
        recyclePool.clear();
    }
}