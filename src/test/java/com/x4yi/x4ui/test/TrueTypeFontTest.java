package com.x4yi.x4ui.test;
import com.x4yi.x4ui.api.client.font.IFont;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.impl.client.font.FontRegistry;
import com.x4yi.x4ui.impl.client.font.TrueTypeFont;
import com.x4yi.x4ui.impl.client.font.VanillaFont;
import org.junit.Test;
import java.awt.Font;
import static org.junit.Assert.*;
public class TrueTypeFontTest {
    @Test
    public void testVanillaFontFallback() {
        VanillaFont font = VanillaFont.INSTANCE;
        assertEquals("vanilla", font.getName());
        assertEquals(9, font.getHeight());
        assertEquals(30, font.getWidth("Hello"));
        assertEquals(30, font.getWidth("§aHello"));
        assertEquals(30, font.getWidth("§c§lHello§r"));
        String trimmed = font.trimStringToWidth("Hello World", 30);
        assertEquals("Hello", trimmed);
    }
    @Test
    public void testTrueTypeFontMetricsAndCodes() {
        Font awtFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
        TrueTypeFont font = new TrueTypeFont("test_sans", awtFont, 14.0f, true);
        assertEquals("test_sans", font.getName());
        assertEquals(14, font.getHeight());
        int wPlain = font.getWidth("Hello World");
        assertTrue("Plain text width should be positive", wPlain > 0);
        int wColored = font.getWidth("§6Hello §bWorld");
        assertEquals("Color codes should not affect measured width", wPlain, wColored);
        int wBold = font.getWidth("§lHello World");
        assertTrue("Bold text should be wider than plain text", wBold > wPlain);
        int wHello = font.getWidth("Hello");
        String trimmed = font.trimStringToWidth("Hello World", wHello);
        assertEquals("Hello", trimmed);
        int drawnWidth = font.draw("Hello World", 0, 0, 0xFFFFFFFF, true);
        assertEquals(wPlain, drawnWidth);
    }
    @Test
    public void testFontRegistry() {
        FontRegistry registry = FontRegistry.INSTANCE;
        assertSame(VanillaFont.INSTANCE, registry.get("vanilla"));
        assertSame(VanillaFont.INSTANCE, registry.get("unknown_font_id"));
        Font awtFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        TrueTypeFont custom = new TrueTypeFont("custom_mono", awtFont, 10.0f, false);
        registry.register("mono", custom);
        assertTrue(registry.has("mono"));
        assertSame(custom, registry.get("mono"));
    }
    @Test
    public void testGraphicsFontStack() {
        HeadlessGraphics gfx = new HeadlessGraphics();
        assertEquals(VanillaFont.INSTANCE, gfx.getFont());
        Font awtFont = new Font(Font.DIALOG, Font.PLAIN, 18);
        TrueTypeFont font = new TrueTypeFont("dialog", awtFont, 16.0f, true);
        gfx.setFont(font);
        assertSame(font, gfx.getFont());
        assertEquals(16, gfx.getFontHeight());
        gfx.save();
        gfx.resetFont();
        assertSame(VanillaFont.INSTANCE, gfx.getFont());
        gfx.restore();
        assertSame(font, gfx.getFont());
    }
    @Test
    public void testGuiLabelFontPropagation() {
        Font awtFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        TrueTypeFont font = new TrueTypeFont("label_font", awtFont, 18.0f, true);
        GuiLabel label = new GuiLabel(0, 0, "Dynamic Title", 0xFFFFFFFF);
        label.withFont(font);
        assertSame(font, label.getFont());
        assertEquals(font.getWidth("Dynamic Title"), label.getWidth());
        assertEquals(18, label.getHeight());
    }
}