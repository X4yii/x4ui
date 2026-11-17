package com.x4yi.x4ui.impl.client.gui;
import com.x4yi.x4ui.api.client.font.IFont;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.utils.BorderSides;
import com.x4yi.x4ui.client.gui.utils.CornerRadii;
import com.x4yi.x4ui.client.gui.utils.Insets;
import com.x4yi.x4ui.impl.client.font.VanillaFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import javax.annotation.Nullable;
import java.util.function.Consumer;
@SideOnly(Side.CLIENT)
public class GLGraphics implements IGraphics {
    public static final GLGraphics INSTANCE = new GLGraphics();
    private final Minecraft mc = Minecraft.getMinecraft();
    private static final int CIRCLE_SEGMENTS = 32;
    private static final double[] COS_TABLE = new double[CIRCLE_SEGMENTS + 1];
    private static final double[] SIN_TABLE = new double[CIRCLE_SEGMENTS + 1];
    static {
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = (Math.PI * 2.0 * i) / CIRCLE_SEGMENTS;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }
    public static int getScaleFactor(Minecraft mc) {
        if (mc == null || mc.displayWidth <= 0 || mc.displayHeight <= 0) return 1;
        int scale = (mc.gameSettings != null) ? mc.gameSettings.guiScale : 0;
        if (scale == 0) {
            scale = 1000;
        }
        int factor = 1;
        while (factor < scale && mc.displayWidth / (factor + 1) >= 320 && mc.displayHeight / (factor + 1) >= 240) {
            factor++;
        }
        if (mc.isUnicode() && factor % 2 != 0 && factor != 1) {
            factor--;
        }
        return factor;
    }
    private static final int MAX_DEPTH = 32;
    private int depth = 0;
    private final float[] alphaStack = new float[MAX_DEPTH];
    private final int[] colorTintStack = new int[MAX_DEPTH];
    private final IFont[] fontStack = new IFont[MAX_DEPTH];
    private final int[] clipStack = new int[MAX_DEPTH * 4];
    private float currentAlpha = 1.0f;
    private int currentColorTint = 0xFFFFFFFF;
    private IFont activeFont = VanillaFont.INSTANCE;
    private int clipDepth = 0;
    public GLGraphics() {
        alphaStack[0] = 1.0f;
        colorTintStack[0] = 0xFFFFFFFF;
        fontStack[0] = VanillaFont.INSTANCE;
    }
    public void reset() {
        depth = 0;
        clipDepth = 0;
        currentAlpha = 1.0f;
        currentColorTint = 0xFFFFFFFF;
        activeFont = VanillaFont.INSTANCE;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        resetColor();
    }
    @Override
    public void save() {
        if (depth >= MAX_DEPTH - 1) {
            GlStateManager.pushMatrix();
            return;
        }
        GlStateManager.pushMatrix();
        alphaStack[depth] = currentAlpha;
        colorTintStack[depth] = currentColorTint;
        fontStack[depth] = activeFont;
        depth++;
    }
    @Override
    public void restore() {
        if (depth > 0) {
            depth--;
            currentAlpha = alphaStack[depth];
            currentColorTint = colorTintStack[depth];
            activeFont = fontStack[depth];
        }
        GlStateManager.popMatrix();
    }
    @Override
    public void translate(float x, float y) {
        GlStateManager.translate(x, y, 0.0f);
    }
    @Override
    public void scale(float sx, float sy) {
        GlStateManager.scale(sx, sy, 1.0f);
    }
    @Override
    public void scale(float sx, float sy, float originX, float originY) {
        GlStateManager.translate(originX, originY, 0.0f);
        GlStateManager.scale(sx, sy, 1.0f);
        GlStateManager.translate(-originX, -originY, 0.0f);
    }
    @Override
    public void rotate(float angleDegrees) {
        GlStateManager.rotate(angleDegrees, 0.0f, 0.0f, 1.0f);
    }
    @Override
    public void rotate(float angleDegrees, float originX, float originY) {
        GlStateManager.translate(originX, originY, 0.0f);
        GlStateManager.rotate(angleDegrees, 0.0f, 0.0f, 1.0f);
        GlStateManager.translate(-originX, -originY, 0.0f);
    }
    @Override
    public void setColor(int argb) {
        float a = ((argb >> 24) & 0xFF) / 255.0f * currentAlpha;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, a);
    }
    @Override
    public void setColor(float r, float g, float b, float a) {
        GlStateManager.color(r, g, b, a * currentAlpha);
    }
    @Override
    public void setAlpha(float alpha) {
        this.currentAlpha = alpha;
    }
    @Override
    public void pushAlpha(float alpha) {
        save();
        this.currentAlpha *= alpha;
    }
    @Override
    public void popAlpha() {
        restore();
    }
    @Override
    public void pushColorTint(int argb) {
        save();
        this.currentColorTint = multiplyColors(this.currentColorTint, argb);
    }
    @Override
    public void popColorTint() {
        restore();
    }
    @Override
    public void resetColor() {
        float a = (((currentColorTint >> 24) & 0xFF) / 255.0f) * currentAlpha;
        float r = ((currentColorTint >> 16) & 0xFF) / 255.0f;
        float g = ((currentColorTint >> 8) & 0xFF) / 255.0f;
        float b = (currentColorTint & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, a);
    }
    private int multiplyColors(int c1, int c2) {
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (a1 * a2) / 255;
        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    private int applyTintAndAlpha(int color) {
        if (currentAlpha < 1.0f) {
            int a = (int) (((color >> 24) & 0xFF) * currentAlpha);
            color = (a << 24) | (color & 0x00FFFFFF);
        }
        if (currentColorTint != 0xFFFFFFFF) {
            color = multiplyColors(color, currentColorTint);
        }
        return color;
    }
    @Override
    public void drawRect(int x, int y, int width, int height, int argb) {
        if (width <= 0 || height <= 0) return;
        argb = applyTintAndAlpha(argb);
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.pos(x, y + height, 0.0D).endVertex();
        buffer.pos(x + width, y + height, 0.0D).endVertex();
        buffer.pos(x + width, y, 0.0D).endVertex();
        buffer.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
    }
    @Override
    public void drawRoundedRect(int x, int y, int width, int height, int radius, int argb) {
        drawRoundedRect(x, y, width, height, CornerRadii.all(radius), argb);
    }

    @Override
    public void drawRoundedRect(int x, int y, int width, int height, CornerRadii radii, int argb) {
        if (width <= 0 || height <= 0) return;
        if (radii == null || radii.isZero()) {
            drawRect(x, y, width, height, argb);
            return;
        }
        CornerRadii r = radii.clamp(width, height);
        argb = applyTintAndAlpha(argb);
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float red = ((argb >> 16) & 0xFF) / 255.0f;
        float green = ((argb >> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, a);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        buffer.pos(x + width / 2.0, y + height / 2.0, 0.0D).endVertex();

        if (r.topLeft > 0) {
            int cxTL = x + r.topLeft;
            int cyTL = y + r.topLeft;
            for (int i = 24; i >= 16; i--) {
                buffer.pos(cxTL + COS_TABLE[i] * r.topLeft, cyTL + SIN_TABLE[i] * r.topLeft, 0.0D).endVertex();
            }
        } else {
            buffer.pos(x, y, 0.0D).endVertex();
        }

        if (r.bottomLeft > 0) {
            int cxBL = x + r.bottomLeft;
            int cyBL = y + height - r.bottomLeft;
            for (int i = 16; i >= 8; i--) {
                buffer.pos(cxBL + COS_TABLE[i] * r.bottomLeft, cyBL + SIN_TABLE[i] * r.bottomLeft, 0.0D).endVertex();
            }
        } else {
            buffer.pos(x, y + height, 0.0D).endVertex();
        }

        if (r.bottomRight > 0) {
            int cxBR = x + width - r.bottomRight;
            int cyBR = y + height - r.bottomRight;
            for (int i = 8; i >= 0; i--) {
                buffer.pos(cxBR + COS_TABLE[i] * r.bottomRight, cyBR + SIN_TABLE[i] * r.bottomRight, 0.0D).endVertex();
            }
        } else {
            buffer.pos(x + width, y + height, 0.0D).endVertex();
        }

        if (r.topRight > 0) {
            int cxTR = x + width - r.topRight;
            int cyTR = y + r.topRight;
            for (int i = 32; i >= 24; i--) {
                buffer.pos(cxTR + COS_TABLE[i] * r.topRight, cyTR + SIN_TABLE[i] * r.topRight, 0.0D).endVertex();
            }
        } else {
            buffer.pos(x + width, y, 0.0D).endVertex();
        }

        if (r.topLeft > 0) {
            buffer.pos(x + r.topLeft + COS_TABLE[24] * r.topLeft, y + r.topLeft + SIN_TABLE[24] * r.topLeft, 0.0D).endVertex();
        } else {
            buffer.pos(x, y, 0.0D).endVertex();
        }
        tessellator.draw();
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
    }

    @Override
    public void drawBorder(int x, int y, int width, int height, int borderWidth, int argb) {
        drawBorder(x, y, width, height, borderWidth, BorderSides.ALL, argb);
    }

    @Override
    public void drawBorder(int x, int y, int width, int height, int borderWidth, int borderSides, int argb) {
        if (borderWidth <= 0 || width <= 0 || height <= 0 || (borderSides & BorderSides.ALL) == 0) return;
        int bw = Math.min(borderWidth, Math.min(width, height));
        boolean hasTop = BorderSides.has(borderSides, BorderSides.TOP);
        boolean hasBottom = BorderSides.has(borderSides, BorderSides.BOTTOM);
        boolean hasLeft = BorderSides.has(borderSides, BorderSides.LEFT);
        boolean hasRight = BorderSides.has(borderSides, BorderSides.RIGHT);
        int vertY = y + (hasTop ? bw : 0);
        int vertH = height - (hasTop ? bw : 0) - (hasBottom ? bw : 0);
        argb = applyTintAndAlpha(argb);
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        if (hasTop) addPosQuad(buffer, x, y, width, bw);
        if (hasBottom) addPosQuad(buffer, x, y + height - bw, width, bw);
        if (vertH > 0) {
            if (hasLeft) addPosQuad(buffer, x, vertY, bw, vertH);
            if (hasRight) addPosQuad(buffer, x + width - bw, vertY, bw, vertH);
        }
        tessellator.draw();
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
    }

    private static void addPosQuad(BufferBuilder buffer, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;
        buffer.pos(x, y + h, 0.0D).endVertex();
        buffer.pos(x + w, y + h, 0.0D).endVertex();
        buffer.pos(x + w, y, 0.0D).endVertex();
        buffer.pos(x, y, 0.0D).endVertex();
    }

    @Override
    public void drawRoundedBorder(int x, int y, int width, int height, int radius, int borderWidth, int argb) {
        drawRoundedBorder(x, y, width, height, CornerRadii.all(radius), borderWidth, BorderSides.ALL, argb);
    }

    @Override
    public void drawRoundedBorder(int x, int y, int width, int height, CornerRadii radii, int borderWidth, int borderSides, int argb) {
        if (width <= 0 || height <= 0 || borderWidth <= 0 || (borderSides & BorderSides.ALL) == 0) return;
        if (radii == null || radii.isZero()) {
            drawBorder(x, y, width, height, borderWidth, borderSides, argb);
            return;
        }
        CornerRadii r = radii.clamp(width, height);
        boolean hasTop = BorderSides.has(borderSides, BorderSides.TOP);
        boolean hasRight = BorderSides.has(borderSides, BorderSides.RIGHT);
        boolean hasBottom = BorderSides.has(borderSides, BorderSides.BOTTOM);
        boolean hasLeft = BorderSides.has(borderSides, BorderSides.LEFT);

        int effTL = (hasTop && hasLeft) ? r.topLeft : 0;
        int effTR = (hasTop && hasRight) ? r.topRight : 0;
        int effBR = (hasBottom && hasRight) ? r.bottomRight : 0;
        int effBL = (hasBottom && hasLeft) ? r.bottomLeft : 0;

        if (effTL == 0 && effTR == 0 && effBR == 0 && effBL == 0) {
            drawBorder(x, y, width, height, borderWidth, borderSides, argb);
            return;
        }

        if (borderSides == BorderSides.ALL && effTL == effTR && effTR == effBR && effBR == effBL) {
            int radius = effTL;
            int bw = Math.min(borderWidth, radius);
            argb = applyTintAndAlpha(argb);
            float a = ((argb >> 24) & 0xFF) / 255.0f;
            float red = ((argb >> 16) & 0xFF) / 255.0f;
            float green = ((argb >> 8) & 0xFF) / 255.0f;
            float blue = (argb & 0xFF) / 255.0f;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(red, green, blue, a);
            buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
            int innerR = Math.max(0, radius - bw);
            int cxTL = x + radius, cyTL = y + radius;
            for (int i = 24; i >= 16; i--) {
                buffer.pos(cxTL + COS_TABLE[i] * innerR, cyTL + SIN_TABLE[i] * innerR, 0.0D).endVertex();
                buffer.pos(cxTL + COS_TABLE[i] * radius, cyTL + SIN_TABLE[i] * radius, 0.0D).endVertex();
            }
            int cxBL = x + radius, cyBL = y + height - radius;
            for (int i = 16; i >= 8; i--) {
                buffer.pos(cxBL + COS_TABLE[i] * innerR, cyBL + SIN_TABLE[i] * innerR, 0.0D).endVertex();
                buffer.pos(cxBL + COS_TABLE[i] * radius, cyBL + SIN_TABLE[i] * radius, 0.0D).endVertex();
            }
            int cxBR = x + width - radius, cyBR = y + height - radius;
            for (int i = 8; i >= 0; i--) {
                buffer.pos(cxBR + COS_TABLE[i] * innerR, cyBR + SIN_TABLE[i] * innerR, 0.0D).endVertex();
                buffer.pos(cxBR + COS_TABLE[i] * radius, cyBR + SIN_TABLE[i] * radius, 0.0D).endVertex();
            }
            int cxTR = x + width - radius, cyTR = y + radius;
            for (int i = 32; i >= 24; i--) {
                buffer.pos(cxTR + COS_TABLE[i] * innerR, cyTR + SIN_TABLE[i] * innerR, 0.0D).endVertex();
                buffer.pos(cxTR + COS_TABLE[i] * radius, cyTR + SIN_TABLE[i] * radius, 0.0D).endVertex();
            }
            buffer.pos(cxTL + COS_TABLE[24] * innerR, cyTL + SIN_TABLE[24] * innerR, 0.0D).endVertex();
            buffer.pos(cxTL + COS_TABLE[24] * radius, cyTL + SIN_TABLE[24] * radius, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            return;
        }

        int bw = borderWidth;
        if (hasTop) {
            int xStart = x + (effTL > 0 ? effTL : 0);
            int xEnd = x + width - (effTR > 0 ? effTR : 0);
            if (xEnd > xStart) drawRect(xStart, y, xEnd - xStart, bw, argb);
        }
        if (hasBottom) {
            int xStart = x + (effBL > 0 ? effBL : 0);
            int xEnd = x + width - (effBR > 0 ? effBR : 0);
            if (xEnd > xStart) drawRect(xStart, y + height - bw, xEnd - xStart, bw, argb);
        }
        if (hasLeft) {
            int yStart = y + (effTL > 0 ? effTL : (hasTop ? bw : 0));
            int yEnd = y + height - (effBL > 0 ? effBL : (hasBottom ? bw : 0));
            if (yEnd > yStart) drawRect(x, yStart, bw, yEnd - yStart, argb);
        }
        if (hasRight) {
            int yStart = y + (effTR > 0 ? effTR : (hasTop ? bw : 0));
            int yEnd = y + height - (effBR > 0 ? effBR : (hasBottom ? bw : 0));
            if (yEnd > yStart) drawRect(x + width - bw, yStart, bw, yEnd - yStart, argb);
        }

        if (effTL > 0) drawCornerArc(x + effTL, y + effTL, effTL, bw, 24, 16, argb);
        if (effBL > 0) drawCornerArc(x + effBL, y + height - effBL, effBL, bw, 16, 8, argb);
        if (effBR > 0) drawCornerArc(x + width - effBR, y + height - effBR, effBR, bw, 8, 0, argb);
        if (effTR > 0) drawCornerArc(x + width - effTR, y + effTR, effTR, bw, 32, 24, argb);
    }

    private void drawCornerArc(int cx, int cy, int radius, int borderWidth, int startIdx, int endIdx, int argb) {
        int bw = Math.min(borderWidth, radius);
        int innerR = Math.max(0, radius - bw);
        argb = applyTintAndAlpha(argb);
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float red = ((argb >> 16) & 0xFF) / 255.0f;
        float green = ((argb >> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(red, green, blue, a);
        buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        for (int i = startIdx; i >= endIdx; i--) {
            buffer.pos(cx + COS_TABLE[i] * innerR, cy + SIN_TABLE[i] * innerR, 0.0D).endVertex();
            buffer.pos(cx + COS_TABLE[i] * radius, cy + SIN_TABLE[i] * radius, 0.0D).endVertex();
        }
        tessellator.draw();
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
    }
    @Override
    public void drawLine(float x1, float y1, float x2, float y2, float thickness, int argb) {
        argb = applyTintAndAlpha(argb);
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(thickness);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        buffer.pos(x1, y1, 0.0D).endVertex();
        buffer.pos(x2, y2, 0.0D).endVertex();
        tessellator.draw();
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
    }
    @Override
    public void drawCircle(float centerX, float centerY, float radius, int argb) {
        argb = applyTintAndAlpha(argb);
        float a = ((argb >> 24) & 0xFF) / 255.0f;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        buffer.pos(centerX, centerY, 0.0D).endVertex();
        for (int i = CIRCLE_SEGMENTS; i >= 0; i--) {
            buffer.pos(centerX + COS_TABLE[i] * radius, centerY + SIN_TABLE[i] * radius, 0.0D).endVertex();
        }
        tessellator.draw();
        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
    }
    @Override
    public void drawGradient(int x, int y, int width, int height, int colorTop, int colorBottom) {
        drawGradient4Corners(x, y, width, height, colorTop, colorTop, colorBottom, colorBottom);
    }
    @Override
    public void drawGradientHorizontal(int x, int y, int width, int height, int colorLeft, int colorRight) {
        drawGradient4Corners(x, y, width, height, colorLeft, colorRight, colorLeft, colorRight);
    }
    @Override
    public void drawGradient4Corners(int x, int y, int width, int height, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        if (width <= 0 || height <= 0) return;
        topLeft = applyTintAndAlpha(topLeft);
        topRight = applyTintAndAlpha(topRight);
        bottomLeft = applyTintAndAlpha(bottomLeft);
        bottomRight = applyTintAndAlpha(bottomRight);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y + height, 0.0D)
                .color((bottomLeft >> 16) & 0xFF, (bottomLeft >> 8) & 0xFF, bottomLeft & 0xFF, (bottomLeft >> 24) & 0xFF).endVertex();
        buffer.pos(x + width, y + height, 0.0D)
                .color((bottomRight >> 16) & 0xFF, (bottomRight >> 8) & 0xFF, bottomRight & 0xFF, (bottomRight >> 24) & 0xFF).endVertex();
        buffer.pos(x + width, y, 0.0D)
                .color((topRight >> 16) & 0xFF, (topRight >> 8) & 0xFF, topRight & 0xFF, (topRight >> 24) & 0xFF).endVertex();
        buffer.pos(x, y, 0.0D)
                .color((topLeft >> 16) & 0xFF, (topLeft >> 8) & 0xFF, topLeft & 0xFF, (topLeft >> 24) & 0xFF).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    @Override
    public void drawTexture(ResourceLocation texture, int x, int y, int width, int height) {
        drawTextureRegion(texture, x, y, width, height, 0.0f, 0.0f, width, height, width, height);
    }
    @Override
    public void drawTextureRegion(ResourceLocation texture, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
        if (width <= 0 || height <= 0 || texWidth <= 0 || texHeight <= 0) return;
        mc.getTextureManager().bindTexture(texture);
        resetColor();
        float minU = u / (float) texWidth;
        float maxU = (u + uWidth) / (float) texWidth;
        float minV = v / (float) texHeight;
        float maxV = (v + vHeight) / (float) texHeight;
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0.0D).tex(minU, maxV).endVertex();
        buffer.pos(x + width, y + height, 0.0D).tex(maxU, maxV).endVertex();
        buffer.pos(x + width, y, 0.0D).tex(maxU, minV).endVertex();
        buffer.pos(x, y, 0.0D).tex(minU, minV).endVertex();
        tessellator.draw();
    }
    @Override
    public void drawNineSlice(ResourceLocation texture, int x, int y, int width, int height, Insets sliceInsets, int texWidth, int texHeight) {
        drawNineSlice(texture, x, y, width, height, sliceInsets, sliceInsets, texWidth, texHeight, true);
    }
    @Override
    public void drawNineSlice(ResourceLocation texture, int x, int y, int width, int height, Insets textureInsets, Insets borderInsets, int texWidth, int texHeight, boolean fillCenter) {
        if (width <= 0 || height <= 0) return;
        if (textureInsets == null) textureInsets = Insets.ZERO;
        if (borderInsets == null) borderInsets = textureInsets;
        mc.getTextureManager().bindTexture(texture);
        resetColor();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int bLeft = borderInsets.left;
        int bRight = borderInsets.right;
        int bTop = borderInsets.top;
        int bBottom = borderInsets.bottom;
        int totalBorderW = bLeft + bRight;
        if (totalBorderW > width && totalBorderW > 0) {
            float scaleX = (float) width / totalBorderW;
            bLeft = Math.round(bLeft * scaleX);
            bRight = width - bLeft;
        }
        int totalBorderH = bTop + bBottom;
        if (totalBorderH > height && totalBorderH > 0) {
            float scaleY = (float) height / totalBorderH;
            bTop = Math.round(bTop * scaleY);
            bBottom = height - bTop;
        }
        int x0 = x;
        int x1 = x + bLeft;
        int x2 = x + width - bRight;
        int x3 = x + width;
        int y0 = y;
        int y1 = y + bTop;
        int y2 = y + height - bBottom;
        int y3 = y + height;
        float u0 = 0.0f;
        float u1 = (float) textureInsets.left / texWidth;
        float u2 = (float) (texWidth - textureInsets.right) / texWidth;
        float u3 = 1.0f;
        float v0 = 0.0f;
        float v1 = (float) textureInsets.top / texHeight;
        float v2 = (float) (texHeight - textureInsets.bottom) / texHeight;
        float v3 = 1.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        addQuad(buffer, x0, y0, x1, y1, u0, v0, u1, v1);
        addQuad(buffer, x2, y0, x3, y1, u2, v0, u3, v1);
        addQuad(buffer, x0, y2, x1, y3, u0, v2, u1, v3);
        addQuad(buffer, x2, y2, x3, y3, u2, v2, u3, v3);
        addQuad(buffer, x1, y0, x2, y1, u1, v0, u2, v1);
        addQuad(buffer, x1, y2, x2, y3, u1, v2, u2, v3);
        addQuad(buffer, x0, y1, x1, y2, u0, v1, u1, v2);
        addQuad(buffer, x2, y1, x3, y2, u2, v1, u3, v2);
        if (fillCenter) {
            addQuad(buffer, x1, y1, x2, y2, u1, v1, u2, v2);
        }
        tessellator.draw();
    }
    private void addQuad(BufferBuilder buffer, int xA, int yA, int xB, int yB, float uA, float vA, float uB, float vB) {
        if (xA >= xB || yA >= yB) return;
        buffer.pos(xA, yB, 0.0D).tex(uA, vB).endVertex();
        buffer.pos(xB, yB, 0.0D).tex(uB, vB).endVertex();
        buffer.pos(xB, yA, 0.0D).tex(uB, vA).endVertex();
        buffer.pos(xA, yA, 0.0D).tex(uA, vA).endVertex();
    }
    @Override
    public void setFont(IFont font) {
        this.activeFont = font != null ? font : VanillaFont.INSTANCE;
    }
    @Override
    public IFont getFont() {
        return this.activeFont;
    }
    @Override
    public void resetFont() {
        this.activeFont = VanillaFont.INSTANCE;
    }
    @Override
    public int drawString(String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return 0;
        color = applyTintAndAlpha(color);
        return activeFont.draw(text, x, y, color, shadow);
    }
    @Override
    public int drawStringCentered(String text, float centerX, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return 0;
        float width = getStringWidth(text);
        return drawString(text, centerX - width / 2.0f, y, color, shadow);
    }
    @Override
    public int drawStringRightAligned(String text, float rightX, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return 0;
        float width = getStringWidth(text);
        return drawString(text, rightX - width, y, color, shadow);
    }
    @Override
    public void drawSplitString(String text, float x, float y, int wrapWidth, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return;
        color = applyTintAndAlpha(color);
        activeFont.drawSplit(text, x, y, wrapWidth, color, shadow);
    }
    @Override
    public int getStringWidth(String text) {
        return activeFont.getWidth(text);
    }
    @Override
    public int getFontHeight() {
        return activeFont.getHeight();
    }
    @Override
    public String trimStringToWidth(String text, int width) {
        return activeFont.trimStringToWidth(text, width);
    }
    @Override
    public void pushClip(int x, int y, int width, int height) {
        if (clipDepth >= MAX_DEPTH - 1) return;
        int finalX = x;
        int finalY = y;
        int finalW = width;
        int finalH = height;
        if (clipDepth > 0) {
            int parentIdx = (clipDepth - 1) * 4;
            int px = clipStack[parentIdx];
            int py = clipStack[parentIdx + 1];
            int pw = clipStack[parentIdx + 2];
            int ph = clipStack[parentIdx + 3];
            int nx = Math.max(x, px);
            int ny = Math.max(y, py);
            int nx2 = Math.min(x + width, px + pw);
            int ny2 = Math.min(y + height, py + ph);
            finalX = nx;
            finalY = ny;
            finalW = Math.max(0, nx2 - nx);
            finalH = Math.max(0, ny2 - ny);
        }
        int idx = clipDepth * 4;
        clipStack[idx] = finalX;
        clipStack[idx + 1] = finalY;
        clipStack[idx + 2] = finalW;
        clipStack[idx + 3] = finalH;
        clipDepth++;
        applyScissor(finalX, finalY, finalW, finalH);
    }
    @Override
    public void popClip() {
        if (clipDepth > 0) {
            clipDepth--;
            if (clipDepth == 0) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            } else {
                int idx = (clipDepth - 1) * 4;
                applyScissor(clipStack[idx], clipStack[idx + 1], clipStack[idx + 2], clipStack[idx + 3]);
            }
        }
    }

    public void clearClip() {
        clipDepth = 0;
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    @Override
    public boolean isClipped(int x, int y, int width, int height) {
        if (clipDepth == 0) return false;
        int idx = (clipDepth - 1) * 4;
        int cx = clipStack[idx];
        int cy = clipStack[idx + 1];
        int cw = clipStack[idx + 2];
        int ch = clipStack[idx + 3];
        return x + width <= cx || x >= cx + cw || y + height <= cy || y >= cy + ch;
    }
    private void applyScissor(int x, int y, int w, int h) {
        int scale = getScaleFactor(mc);
        int scissorX = x * scale;
        int scissorY = mc.displayHeight - (y + h) * scale;
        int scissorW = Math.max(0, w * scale);
        int scissorH = Math.max(0, h * scale);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
    }
    @Override
    public void drawItemStack(ItemStack stack, int x, int y) {
        drawItemStackWithOverlay(stack, x, y, null);
    }
    @Override
    public void drawItemStackWithOverlay(ItemStack stack, int x, int y, @Nullable String altText) {
        if (stack == null || stack.isEmpty()) return;
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, altText);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
    }
    @Override
    public void renderCustom(Consumer<Minecraft> customRenderer) {
        GlStateManager.pushMatrix();
        try {
            customRenderer.accept(mc);
        } finally {
            GlStateManager.popMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            resetColor();
        }
    }
    @Override
    public void clearDepthBuffer() {
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
    }
    @Override
    public int createBgraTexture(int width, int height) {
        int texId = GlStateManager.generateTexture();
        GlStateManager.bindTexture(texId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, org.lwjgl.opengl.GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        return texId;
    }
    @Override
    public void uploadBgraSubImage(int textureId, int width, int height, java.nio.ByteBuffer buffer) {
        if (textureId == -1 || buffer == null) return;
        GlStateManager.bindTexture(textureId);
        buffer.position(0);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, org.lwjgl.opengl.GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buffer);
    }
    @Override
    public void uploadBgraSubImage(int textureId, int width, int height, java.nio.IntBuffer buffer) {
        if (textureId == -1 || buffer == null) return;
        GlStateManager.bindTexture(textureId);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, org.lwjgl.opengl.GL12.GL_BGRA, org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
    }
}