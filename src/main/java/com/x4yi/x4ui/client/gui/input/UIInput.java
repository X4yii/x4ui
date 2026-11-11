package com.x4yi.x4ui.client.gui.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Input abstraction isolating LWJGL 2 / LWJGL 3 (lwjgly) differences for screens, overlays, and consumer mods.
 */
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

    /**
     * Normalizes mouse wheel deltas across LWJGL 2 (+/-120 per notch) and LWJGL 3/GLFW trackpads (smaller deltas).
     * Returns 0 when idle, or signed notch steps (+/-1 or more).
     */
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

    /**
     * Computes scaled GUI mouse X safely, guarding against {@code displayWidth == 0} when minimized in LWJGL 3.
     */
    public static int safeMouseX(Minecraft mc, int guiWidth) {
        if (mc == null || mc.displayWidth <= 0) {
            return 0;
        }
        return Mouse.getEventX() * guiWidth / mc.displayWidth;
    }

    /**
     * Computes scaled GUI mouse Y safely, guarding against {@code displayHeight == 0} when minimized in LWJGL 3.
     */
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

