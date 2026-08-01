package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;

public class GuiPanel extends GuiComponent {
    protected FlexDirection flexDirection = FlexDirection.ABSOLUTE;
    protected int gap = 0;

    public GuiPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setFlexDirection(FlexDirection flexDirection) {
        this.flexDirection = flexDirection;
    }
    
    public void setGap(int gap) {
        this.gap = gap;
    }

    @Override
    public void update() {
        super.update();
        layoutChildren();
    }

    protected void layoutChildren() {
        if (flexDirection == FlexDirection.ABSOLUTE) return;
        
        int currentX = padding;
        int currentY = padding;
        
        for (GuiComponent child : children) {
            if (!child.isVisible()) continue;
            
            if (flexDirection == FlexDirection.VERTICAL) {
                child.setX(currentX + child.getMargin());
                child.setY(currentY + child.getMargin());
                currentY += child.getHeight() + child.getMargin() * 2 + gap;
            } else if (flexDirection == FlexDirection.HORIZONTAL) {
                child.setX(currentX + child.getMargin());
                child.setY(currentY + child.getMargin());
                currentX += child.getWidth() + child.getMargin() * 2 + gap;
            }
        }
        
        if (flexDirection == FlexDirection.VERTICAL) {
            this.height = currentY + padding;
        } else if (flexDirection == FlexDirection.HORIZONTAL) {
            this.width = currentX + padding;
        }
    }

    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        // Basic panels are invisible containers. Subclasses can override to draw backgrounds.
    }
}
