package com.x4yi.x4ui.client.gui.component.layout;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import java.util.List;
public interface ILayoutManager {
    void layout(GuiComponent parent, List<GuiComponent> children);
}