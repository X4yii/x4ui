package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.layout.FlexAlign;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.component.layout.FlexLayout;
import com.x4yi.x4ui.client.gui.utils.BorderSides;
import com.x4yi.x4ui.client.gui.utils.CornerRadii;
import com.x4yi.x4ui.client.gui.utils.ITheme;
import com.x4yi.x4ui.client.gui.utils.Insets;

public class GuiPanel extends GuiComponent {
    protected int backgroundColor = 0x00000000;
    protected int borderColor = 0x00000000;
    protected int borderWidth = 0;
    protected int borderSides = BorderSides.ALL;
    protected CornerRadii cornerRadii = CornerRadii.ZERO;
    protected boolean flexSpacer = false;
    protected boolean themedCard = false;
    protected boolean suppressChrome = false;
    private boolean customBackgroundSet = false;
    private boolean customBorderSet = false;

    public GuiPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.layoutManager = new FlexLayout(FlexDirection.ABSOLUTE, 0, false);
    }

    public static GuiPanel column(int gap) {
        GuiPanel panel = new GuiPanel(0, 0, 0, 0);
        panel.setFlexDirection(FlexDirection.VERTICAL);
        panel.setGap(gap);
        panel.setHugContent(true);
        panel.withPercentWidth(1.0f);
        return panel;
    }

    public static GuiPanel row(int gap) {
        GuiPanel panel = new GuiPanel(0, 0, 0, 0);
        panel.setFlexDirection(FlexDirection.HORIZONTAL);
        panel.setGap(gap);
        panel.setAlignItems(FlexAlign.CENTER);
        panel.setHugContent(true);
        panel.withPercentWidth(1.0f);
        return panel;
    }

    public static GuiPanel card(int padding, int gap) {
        GuiPanel panel = column(gap);
        panel.themedCard = true;
        panel.setPadding(new Insets(padding));
        panel.setCornerRadius(6);
        panel.setBorderWidth(1);
        panel.applyCardTheme(panel.getTheme());
        return panel;
    }

    public static GuiPanel spacer() {
        GuiPanel spacer = new GuiPanel(0, 0, 0, 0);
        spacer.flexSpacer = true;
        spacer.setFlexGrow(1.0f);
        return spacer;
    }

    public boolean isFlexSpacer() {
        return flexSpacer;
    }

    private void applyCardTheme(ITheme theme) {
        if (theme == null) return;
        if (!customBackgroundSet) {
            this.backgroundColor = theme.getCardBackgroundColor();
        }
        if (!customBorderSet) {
            this.borderColor = theme.getCardBorderColor();
        }
    }

    @Override
    protected void onAttached(GuiComponent parent) {
        super.onAttached(parent);
        if (themedCard) {
            applyCardTheme(getTheme());
        }
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

    public GuiPanel withFlexDirection(FlexDirection flexDirection) {
        setFlexDirection(flexDirection);
        return this;
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

    public GuiPanel withGap(int gap) {
        setGap(gap);
        return this;
    }

    public void setFlexWrap(boolean flexWrap) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setFlexWrap(flexWrap);
        requestLayout();
    }

    public void setHugContent(boolean hugContent) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setHugContent(hugContent);
        requestLayout();
    }

    public GuiPanel withHugContent(boolean hugContent) {
        setHugContent(hugContent);
        return this;
    }

    public boolean isHugContent() {
        FlexLayout flex = getFlexLayout();
        return flex != null && flex.isHugContent();
    }

    public void setAlignItems(FlexAlign alignItems) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setAlignItems(alignItems);
        requestLayout();
    }

    public GuiPanel withAlignItems(FlexAlign alignItems) {
        setAlignItems(alignItems);
        return this;
    }

    public void setJustifyContent(FlexAlign justifyContent) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setJustifyContent(justifyContent);
        requestLayout();
    }

    public GuiPanel withJustifyContent(FlexAlign justifyContent) {
        setJustifyContent(justifyContent);
        return this;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.customBackgroundSet = true;
    }

    public GuiPanel withBackgroundColor(int backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        this.customBorderSet = true;
    }

    public int getBorderColor() {
        return this.borderColor;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public GuiPanel withBorder(int borderWidth, int borderColor) {
        setBorderWidth(borderWidth);
        setBorderColor(borderColor);
        return this;
    }

    public int getBorderWidth() {
        return this.borderWidth;
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadii = CornerRadii.all(cornerRadius);
    }

    public GuiPanel withCornerRadius(int cornerRadius) {
        setCornerRadius(cornerRadius);
        return this;
    }

    public int getCornerRadius() {
        return this.cornerRadii.topLeft;
    }

    public void setCornerRadii(CornerRadii cornerRadii) {
        this.cornerRadii = cornerRadii != null ? cornerRadii : CornerRadii.ZERO;
    }

    public GuiPanel withCornerRadii(CornerRadii cornerRadii) {
        setCornerRadii(cornerRadii);
        return this;
    }

    public GuiPanel withCornerRadii(int tl, int tr, int br, int bl) {
        setCornerRadii(CornerRadii.of(tl, tr, br, bl));
        return this;
    }

    public CornerRadii getCornerRadii() {
        return this.cornerRadii;
    }

    public void setBorderSides(int borderSides) {
        this.borderSides = borderSides;
    }

    public GuiPanel withBorderSides(int borderSides) {
        setBorderSides(borderSides);
        return this;
    }

    public int getBorderSides() {
        return this.borderSides;
    }

    public GuiPanel withPadding(int padding) {
        setPadding(new Insets(padding));
        return this;
    }

    public GuiPanel withPadding(Insets padding) {
        setPadding(padding);
        return this;
    }

    @Override
    public GuiPanel withPercentWidth(float percentWidth) {
        super.withPercentWidth(percentWidth);
        return this;
    }

    @Override
    public GuiPanel withPercentHeight(float percentHeight) {
        super.withPercentHeight(percentHeight);
        return this;
    }

    @Override
    public GuiPanel withPercentSize(float percentWidth, float percentHeight) {
        super.withPercentSize(percentWidth, percentHeight);
        return this;
    }

    public void setSuppressChrome(boolean suppressChrome) {
        this.suppressChrome = suppressChrome;
    }

    public boolean isSuppressChrome() {
        return suppressChrome;
    }

    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        if (suppressChrome) return;
        if (themedCard && !customBackgroundSet && !customBorderSet) {
            applyCardTheme(getTheme());
        }
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if ((backgroundColor & 0xFF000000) != 0) {
            if (!cornerRadii.isZero()) {
                gfx.drawRoundedRect(absX, absY, width, height, cornerRadii, backgroundColor);
            } else {
                gfx.drawRect(absX, absY, width, height, backgroundColor);
            }
        }
        if (borderWidth > 0 && (borderSides & BorderSides.ALL) != 0 && (borderColor & 0xFF000000) != 0) {
            if (!cornerRadii.isZero()) {
                gfx.drawRoundedBorder(absX, absY, width, height, cornerRadii, borderWidth, borderSides, borderColor);
            } else {
                gfx.drawBorder(absX, absY, width, height, borderWidth, borderSides, borderColor);
            }
        }
    }
}
