package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.utils.ITheme;
import com.x4yi.x4ui.client.gui.utils.Insets;
public class GuiCard extends GuiPanel {
    protected GuiLabel titleLabel;
    public GuiCard(int x, int y, int width, int height) {
        this(x, y, width, height, null);
    }
    public GuiCard(int x, int y, int width, int height, String title) {
        super(x, y, width, height);
        this.setCornerRadius(8);
        this.setBorderWidth(1);
        if (title != null && !title.isEmpty()) {
            titleLabel = new GuiLabel(10, 8, title, 0xFFFFFFFF);
            this.addChild(titleLabel);
            this.setPadding(new Insets(30, 10, 10, 10));
        } else {
            this.setPadding(new Insets(10, 10, 10, 10));
        }
    }
    public GuiLabel getTitleLabel() {
        return titleLabel;
    }
    @Override
    protected void onAttached(GuiComponent parent) {
        super.onAttached(parent);
        applyTheme();
    }
    public void applyTheme() {
        ITheme theme = getTheme();
        this.setBackgroundColor(theme.getCardBackgroundColor());
        this.setBorderColor(theme.getCardBorderColor());
        if (titleLabel != null) {
            titleLabel.setColor(theme.getTextColor());
        }
    }
}