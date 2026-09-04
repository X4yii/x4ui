package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
public class GuiTextInput extends GuiComponent {
    protected String text = "";
    protected String placeholder = "";
    protected int maxLength = 256;
    protected int cursorPosition = 0;
    protected int selectionEnd = 0;
    protected int cursorCounter = 0;
    protected int submitKeyCode = Keyboard.KEY_RETURN;
    protected Runnable onSubmit = null;
    public GuiTextInput(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public GuiTextInput setSubmitKey(int keyCode) {
        this.submitKeyCode = keyCode;
        return this;
    }
    public GuiTextInput onEnter(Runnable onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }
    public void setText(String text) {
        this.text = text;
        this.cursorPosition = text.length();
        this.selectionEnd = this.cursorPosition;
    }
    public String getText() { return text; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public void setMaxLength(int max) { this.maxLength = max; }
    
    @Override
    protected void onFocusGained() {
        cursorCounter = 0;
    }
    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        if (isFocused()) cursorCounter++;
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
        
        drawRect(absX, absY, absX + width, absY + height, theme.getTextInputBackgroundColor());
        drawBorder(absX, absY, absX + width, absY + height, isFocused() ? theme.getTextInputFocusedBorderColor() : theme.getTextInputBorderColor());
        int textY = absY + (height - fontRenderer.FONT_HEIGHT) / 2;
        int textX = absX + 4;
        if (text.isEmpty() && !isFocused() && !placeholder.isEmpty()) {
            fontRenderer.drawString(placeholder, textX, textY, theme.getDisabledTextColor());
            return;
        }
        String renderedText = fontRenderer.trimStringToWidth(text, width - 8);
        fontRenderer.drawString(renderedText, textX, textY, theme.getTextColor());
        if (isFocused() && (cursorCounter / 6) % 2 == 0) {
            int cursorX = textX + fontRenderer.getStringWidth(renderedText.substring(0, Math.min(cursorPosition, renderedText.length())));
            Gui.drawRect(cursorX, textY - 1, cursorX + 1, textY + fontRenderer.FONT_HEIGHT + 1, 0xFFFFFFFF);
        }
        if (selectionEnd != cursorPosition) {
            int startX = textX + fontRenderer.getStringWidth(renderedText.substring(0, Math.min(Math.min(cursorPosition, selectionEnd), renderedText.length())));
            int endX = textX + fontRenderer.getStringWidth(renderedText.substring(0, Math.min(Math.max(cursorPosition, selectionEnd), renderedText.length())));
            int selColor = (theme.getTextInputFocusedBorderColor() & 0x00FFFFFF) | 0x88000000;
            Gui.drawRect(startX, textY - 1, endX, textY + fontRenderer.FONT_HEIGHT + 1, selColor);
        }
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
            boolean wasFocused = isFocused();
            boolean clickedOnUs = isMouseOver(mouseX, mouseY) && enabled;
            if (clickedOnUs) {
                this.requestFocus();
                cursorPosition = text.length();
                selectionEnd = cursorPosition;
                return true;
            } else if (wasFocused) {
                this.clearFocus();
            }
        }
        return false;
    }
    @Override
    public boolean onKeyPress(char typedChar, int keyCode) {
        if (!isFocused() || !enabled) return false;
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            cursorPosition = text.length();
            selectionEnd = 0;
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            GuiScreen.setClipboardString(getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            writeText(GuiScreen.getClipboardString());
            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            GuiScreen.setClipboardString(getSelectedText());
            writeText("");
            return true;
        }
        if (keyCode == submitKeyCode) {
            if (onSubmit != null) {
                onSubmit.run();
                return true;
            }
        }
        switch (keyCode) {
            case Keyboard.KEY_BACK:
                if (GuiScreen.isCtrlKeyDown()) {
                    deleteWords(-1);
                } else {
                    deleteFromCursor(-1);
                }
                return true;
            case Keyboard.KEY_DELETE:
                if (GuiScreen.isCtrlKeyDown()) {
                    deleteWords(1);
                } else {
                    deleteFromCursor(1);
                }
                return true;
            case Keyboard.KEY_LEFT:
                if (GuiScreen.isShiftKeyDown()) {
                    if (cursorPosition > 0) cursorPosition--;
                } else {
                    if (cursorPosition > 0) cursorPosition--;
                    selectionEnd = cursorPosition;
                }
                return true;
            case Keyboard.KEY_RIGHT:
                if (GuiScreen.isShiftKeyDown()) {
                    if (cursorPosition < text.length()) cursorPosition++;
                } else {
                    if (cursorPosition < text.length()) cursorPosition++;
                    selectionEnd = cursorPosition;
                }
                return true;
            case Keyboard.KEY_HOME:
                cursorPosition = 0;
                if (!GuiScreen.isShiftKeyDown()) selectionEnd = 0;
                return true;
            case Keyboard.KEY_END:
                cursorPosition = text.length();
                if (!GuiScreen.isShiftKeyDown()) selectionEnd = cursorPosition;
                return true;
            default:
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    writeText(Character.toString(typedChar));
                    return true;
                }
                return false;
        }
    }
    private String getSelectedText() {
        int min = Math.min(cursorPosition, selectionEnd);
        int max = Math.max(cursorPosition, selectionEnd);
        return text.substring(min, max);
    }
    private void writeText(String input) {
        int min = Math.min(cursorPosition, selectionEnd);
        int max = Math.max(cursorPosition, selectionEnd);
        String result = text.substring(0, min) + input + text.substring(max);
        if (result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }
        text = result;
        cursorPosition = min + input.length();
        selectionEnd = cursorPosition;
    }
    private void deleteFromCursor(int dir) {
        if (cursorPosition != selectionEnd) {
            writeText("");
            return;
        }
        if (dir < 0 && cursorPosition > 0) {
            text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
            cursorPosition--;
            selectionEnd = cursorPosition;
        } else if (dir > 0 && cursorPosition < text.length()) {
            text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
        }
    }
    private void deleteWords(int dir) {
        deleteFromCursor(dir);
    }
}