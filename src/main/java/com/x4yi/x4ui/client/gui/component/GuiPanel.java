package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.component.layout.FlexLayout;
public class GuiPanel extends GuiComponent {
    public GuiPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.layoutManager = new FlexLayout(FlexDirection.ABSOLUTE, 0, false);
    }
    private FlexLayout getFlexLayout() {
        if (this.layoutManager instanceof FlexLayout) {
            return (FlexLayout) this.layoutManager;
        }
        return null;
    }
    public void setFlexDirection(FlexDirection flexDirection) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setFlexDirection(flexDirection);
        requestLayout();
    }
    public FlexDirection getFlexDirection() {
        FlexLayout flex = getFlexLayout();
        return flex != null ? flex.flexDirection : FlexDirection.ABSOLUTE;
    }
    public void setGap(int gap) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setGap(gap);
        requestLayout();
    }
    public void setFlexWrap(boolean flexWrap) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setFlexWrap(flexWrap);
        requestLayout();
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
    }
}