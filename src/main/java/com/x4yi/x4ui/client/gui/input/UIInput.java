package com.x4yi.x4ui.client.gui.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public final class UIInput {
    private UIInput() {}

    public static boolean isCtrlDown() {
        return GuiScreen.isCtrlKeyDown();
    }

    public static boolean isShiftDown() {
        return GuiScreen.isShiftKeyDown();
    }

    public static boolean isAltDown() {
        return GuiScreen.isAltKeyDown();
    }

    public static int normalizeWheelDelta(int rawWheel) {
        if (rawWheel == 0) {
            return 0;
        }
        int abs = Math.abs(rawWheel);
        if (abs >= 120) {
            return rawWheel / 120;
        }
        return rawWheel > 0 ? 1 : -1;
    }

    public static int getEventDWheel() {
        return normalizeWheelDelta(Mouse.getEventDWheel());
    }

    public static int safeMouseX(Minecraft mc, int guiWidth) {
        if (mc == null || mc.displayWidth <= 0) {
            return 0;
        }
        return Mouse.getEventX() * guiWidth / mc.displayWidth;
    }

    public static int safeMouseY(Minecraft mc, int guiHeight) {
        if (mc == null || mc.displayHeight <= 0) {
            return 0;
        }
        return guiHeight - Mouse.getEventY() * guiHeight / mc.displayHeight - 1;
    }

    public static boolean getEventKeyState() {
        return Keyboard.getEventKeyState();
    }

    public static int getEventKey() {
        return Keyboard.getEventKey();
    }

    public static char getEventCharacter() {
        return Keyboard.getEventCharacter();
    }

    public static int getEventButton() {
        return Mouse.getEventButton();
    }

    public static boolean getEventButtonState() {
        return Mouse.getEventButtonState();
    }

    public static boolean isMouseButtonDown(int button) {
        return Mouse.isButtonDown(button);
    }
}

