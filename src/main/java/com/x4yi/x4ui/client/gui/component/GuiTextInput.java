package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
public class GuiTextInput extends GuiComponent {
    protected String text = "";
    protected String placeholder = "";
    protected int maxLength = 256;
    protected int cursorPosition = 0;
    protected int selectionEnd = 0;
    protected int lineScrollOffset = 0;
    protected int cursorCounter = 0;
    protected int submitKeyCode = Keyboard.KEY_RETURN;
    protected Runnable onSubmit = null;
    protected java.util.function.Consumer<String> onTextChanged = null;
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
    public GuiTextInput setOnTextChanged(java.util.function.Consumer<String> onTextChanged) {
        this.onTextChanged = onTextChanged;
        return this;
    }
    public void setText(String text) {
        this.text = text != null ? text : "";
        if (this.text.length() > maxLength) {
            this.text = this.text.substring(0, maxLength);
        }
        setCursorPosition(this.text.length());
        if (this.onTextChanged != null) {
            this.onTextChanged.accept(this.text);
        }
    }
    public String getText() { return text; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public void setMaxLength(int max) { this.maxLength = max; }
    public void setCursorPosition(int pos) {
        this.cursorPosition = MathHelper.clamp(pos, 0, this.text.length());
        this.selectionEnd = this.cursorPosition;
        clampScrollOffset();
    }
    private void clampScrollOffset() {
        if (this.lineScrollOffset > this.text.length()) {
            this.lineScrollOffset = this.text.length();
        }
        int innerWidth = Math.max(1, this.width - 8);
        if (this.cursorPosition < this.lineScrollOffset) {
            this.lineScrollOffset = this.cursorPosition;
        }
        String visible = getFont().trimStringToWidth(this.text.substring(this.lineScrollOffset), innerWidth);
        int visibleEnd = this.lineScrollOffset + visible.length();
        if (this.cursorPosition > visibleEnd) {
            this.lineScrollOffset += this.cursorPosition - visibleEnd;
        }
    }
    @Override
    protected void onFocusGained() {
        cursorCounter = 0;
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (isFocused()) cursorCounter++;
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
        gfx.drawRect(absX, absY, width, height, theme.getTextInputBackgroundColor());
        gfx.drawBorder(absX, absY, width, height, 1, isFocused() ? theme.getTextInputFocusedBorderColor() : theme.getTextInputBorderColor());
        int fontHeight = gfx.getFontHeight();
        int textY = absY + (height - fontHeight) / 2;
        int textX = absX + 4;
        int innerWidth = Math.max(1, width - 8);
        if (text.isEmpty() && !isFocused() && !placeholder.isEmpty()) {
            gfx.drawString(placeholder, textX, textY, theme.getDisabledTextColor(), false);
            return;
        }
        clampScrollOffset();
        String visibleText = gfx.trimStringToWidth(text.substring(lineScrollOffset), innerWidth);
        gfx.drawString(visibleText, textX, textY, theme.getTextColor(), false);
        int cursorRel = cursorPosition - lineScrollOffset;
        int selEndRel = selectionEnd - lineScrollOffset;
        boolean cursorVisible = isFocused() && (cursorCounter / 6) % 2 == 0 && cursorRel >= 0 && cursorRel <= visibleText.length();
        if (cursorVisible) {
            int cursorX = textX + gfx.getStringWidth(visibleText.substring(0, cursorRel));
            gfx.drawRect(cursorX, textY - 1, 1, fontHeight + 2, 0xFFFFFFFF);
        }
        if (selectionEnd != cursorPosition) {
            int selStart = Math.min(cursorRel, selEndRel);
            int selEnd = Math.max(cursorRel, selEndRel);
            selStart = MathHelper.clamp(selStart, 0, visibleText.length());
            selEnd = MathHelper.clamp(selEnd, 0, visibleText.length());
            if (selStart != selEnd) {
                int startX = textX + gfx.getStringWidth(visibleText.substring(0, selStart));
                int endX = textX + gfx.getStringWidth(visibleText.substring(0, selEnd));
                int selColor = (theme.getTextInputFocusedBorderColor() & 0x00FFFFFF) | 0x88000000;
                gfx.drawRect(startX, textY - 1, endX - startX, fontHeight + 2, selColor);
            }
        }
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
            boolean wasFocused = isFocused();
            boolean clickedOnUs = isMouseOver(mouseX, mouseY) && enabled;
            if (clickedOnUs) {
                this.requestFocus();
                int textX = getAbsoluteX() + 4;
                int clickedRelX = mouseX - textX;
                String visibleText = getFont().trimStringToWidth(text.substring(lineScrollOffset), Math.max(1, width - 8));
                int clickedCharOffset = getFont().trimStringToWidth(visibleText, clickedRelX).length();
                setCursorPosition(lineScrollOffset + clickedCharOffset);
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
            clampScrollOffset();
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
                clampScrollOffset();
                return true;
            case Keyboard.KEY_RIGHT:
                if (GuiScreen.isShiftKeyDown()) {
                    if (cursorPosition < text.length()) cursorPosition++;
                } else {
                    if (cursorPosition < text.length()) cursorPosition++;
                    selectionEnd = cursorPosition;
                }
                clampScrollOffset();
                return true;
            case Keyboard.KEY_HOME:
                cursorPosition = 0;
                if (!GuiScreen.isShiftKeyDown()) selectionEnd = 0;
                clampScrollOffset();
                return true;
            case Keyboard.KEY_END:
                cursorPosition = text.length();
                if (!GuiScreen.isShiftKeyDown()) selectionEnd = cursorPosition;
                clampScrollOffset();
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
        String sanitized = ChatAllowedCharacters.filterAllowedCharacters(input);
        int min = Math.min(cursorPosition, selectionEnd);
        int max = Math.max(cursorPosition, selectionEnd);
        int spaceLeft = maxLength - (text.length() - (max - min));
        if (spaceLeft > 0) {
            if (sanitized.length() > spaceLeft) {
                sanitized = sanitized.substring(0, spaceLeft);
            }
            text = text.substring(0, min) + sanitized + text.substring(max);
            cursorPosition = min + sanitized.length();
            selectionEnd = cursorPosition;
            clampScrollOffset();
            if (onTextChanged != null) onTextChanged.accept(this.text);
        }
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
            clampScrollOffset();
            if (onTextChanged != null) onTextChanged.accept(this.text);
        } else if (dir > 0 && cursorPosition < text.length()) {
            text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
            clampScrollOffset();
            if (onTextChanged != null) onTextChanged.accept(this.text);
        }
    }
    private void deleteWords(int dir) {
        if (cursorPosition != selectionEnd) {
            writeText("");
            return;
        }
        int wordBoundary = getNthWordFromCursor(dir);
        if (wordBoundary != cursorPosition) {
            int min = Math.min(cursorPosition, wordBoundary);
            int max = Math.max(cursorPosition, wordBoundary);
            text = text.substring(0, min) + text.substring(max);
            cursorPosition = min;
            selectionEnd = cursorPosition;
            clampScrollOffset();
            if (onTextChanged != null) onTextChanged.accept(this.text);
        }
    }
    private int getNthWordFromCursor(int dir) {
        int pos = cursorPosition;
        if (dir < 0) {
            while (pos > 0 && Character.isWhitespace(text.charAt(pos - 1))) {
                pos--;
            }
            while (pos > 0 && !Character.isWhitespace(text.charAt(pos - 1))) {
                pos--;
            }
        } else {
            int len = text.length();
            while (pos < len && !Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
            while (pos < len && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }
        return pos;
    }
}