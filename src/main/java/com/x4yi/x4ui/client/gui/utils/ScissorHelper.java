package com.x4yi.x4ui.client.gui.utils;

import org.lwjgl.opengl.GL11;
import java.util.Stack;

public class ScissorHelper {
    
    private static class ScissorBox {
        int x, y, width, height;
        ScissorBox(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    private static final Stack<ScissorBox> scissorStack = new Stack<>();
    
    /**
     * Pushes a new scissor box to the stack and enables OpenGL clipping.
     * If a scissor box is already active, it geometrically intersects the new box
     * with the parent box to ensure children do not render outside their parent's bounds.
     *
     * @param x The X coordinate of the bottom-left corner of the box (OpenGL coordinates)
     * @param y The Y coordinate of the bottom-left corner of the box (OpenGL coordinates)
     * @param w The width of the box
     * @param h The height of the box
     */
    public static void pushScissor(int x, int y, int w, int h) {
        if (!scissorStack.isEmpty()) {
            ScissorBox parent = scissorStack.peek();
            
            // Calculate intersection
            int nx = Math.max(x, parent.x);
            int ny = Math.max(y, parent.y);
            int nx2 = Math.min(x + w, parent.x + parent.width);
            int ny2 = Math.min(y + h, parent.y + parent.height);
            
            x = nx;
            y = ny;
            w = Math.max(0, nx2 - nx);
            h = Math.max(0, ny2 - ny);
        }
        
        scissorStack.push(new ScissorBox(x, y, w, h));
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, w, h);
    }
    
    /**
     * Pops the current scissor box from the stack.
     * If the stack is empty after popping, OpenGL clipping is disabled.
     * Otherwise, it restores the parent's scissor box.
     */
    public static void popScissor() {
        if (!scissorStack.isEmpty()) {
            scissorStack.pop();
        }
        
        if (scissorStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            ScissorBox parent = scissorStack.peek();
            GL11.glScissor(parent.x, parent.y, parent.width, parent.height);
        }
    }
}
