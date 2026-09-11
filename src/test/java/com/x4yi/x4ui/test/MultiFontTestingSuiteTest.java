package com.x4yi.x4ui.test;
import com.x4yi.x4ui.api.client.font.IFont;
import com.x4yi.x4ui.client.gui.component.GuiButton;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiTextInput;
import com.x4yi.x4ui.impl.client.font.FontRegistry;
import com.x4yi.x4ui.impl.client.font.TrueTypeFont;
import org.junit.BeforeClass;
import org.junit.Test;
import java.awt.Font;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.Assert.*;
public class MultiFontTestingSuiteTest {
    private static final String[] FONT_FILES = {
        "adwaita_sans.ttf",
        "audiowide.ttf",
        "bungee.ttf",
        "creepster.ttf",
        "free_mono.otf",
        "free_serif.otf",
        "meslo_nerd.ttf",
        "pacifico.ttf",
        "press_start.ttf",
        "vt323.ttf"
    };
    private static final Map<String, TrueTypeFont> LOADED_FONTS = new LinkedHashMap<>();
    @BeforeClass
    public static void setUp() throws Exception {
        for (String file : FONT_FILES) {
            String fontName = file.substring(0, file.lastIndexOf('.'));
            InputStream is = MultiFontTestingSuiteTest.class.getResourceAsStream("/fonts/showcase/" + file);
            assertNotNull("Font resource must be found: " + file, is);
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is);
            is.close();
            assertNotNull("AWT Font must parse successfully: " + file, awtFont);
            TrueTypeFont ttf = new TrueTypeFont(fontName, awtFont, 14.0f, true);
            LOADED_FONTS.put(fontName, ttf);
            FontRegistry.INSTANCE.register(fontName, ttf);
        }
        assertEquals("Must load all 10 diverse fonts", 10, LOADED_FONTS.size());
    }
    @Test
    public void testAllTenFontsLoadedInRegistry() {
        for (String file : FONT_FILES) {
            String fontName = file.substring(0, file.lastIndexOf('.'));
            assertTrue("FontRegistry must contain font: " + fontName, FontRegistry.INSTANCE.has(fontName));
            IFont retrieved = FontRegistry.INSTANCE.get(fontName);
            assertNotNull(retrieved);
            assertEquals(fontName, retrieved.getName());
            assertEquals(14, retrieved.getHeight());
        }
    }
    @Test
    public void testWidthCalculationsAndFormattingCodes() {
        String testText = "The quick brown fox jumps over 13 lazy dogs!";
        String coloredText = "§6The §aquick §bbrown §ffox §ejumps §2over §c13 §dlazy §rdogs!";
        String boldText = "§lThe quick brown fox jumps over 13 lazy dogs!";
        for (Map.Entry<String, TrueTypeFont> entry : LOADED_FONTS.entrySet()) {
            String name = entry.getKey();
            TrueTypeFont font = entry.getValue();
            int plainWidth = font.getWidth(testText);
            assertTrue("Font " + name + " must measure positive width for plain text", plainWidth > 0);
            int coloredWidth = font.getWidth(coloredText);
            assertEquals("Font " + name + " formatting color codes (§x) must not change measured width",
                    plainWidth, coloredWidth);
            int boldWidth = font.getWidth(boldText);
            assertTrue("Font " + name + " bold text (§l) must be wider than plain text",
                    boldWidth > plainWidth);
        }
    }
    @Test
    public void testAccentedAndLatin1Characters() {
        String accentedText = "¡Hola Señor! ¿Cómo estás? Café, Über, façade, año 2026";
        for (Map.Entry<String, TrueTypeFont> entry : LOADED_FONTS.entrySet()) {
            String name = entry.getKey();
            TrueTypeFont font = entry.getValue();
            int width = font.getWidth(accentedText);
            assertTrue("Font " + name + " must measure valid width for Latin-1 / Spanish text", width > 0);
            int drawn = font.draw(accentedText, 0, 0, 0xFFFFFFFF, true);
            assertEquals(width, drawn);
        }
    }
    @Test
    public void testStringTrimmingAcrossAllFonts() {
        String text = "Quick testing of trimming functionality";
        for (Map.Entry<String, TrueTypeFont> entry : LOADED_FONTS.entrySet()) {
            String name = entry.getKey();
            TrueTypeFont font = entry.getValue();
            int fullWidth = font.getWidth(text);
            int halfWidth = fullWidth / 2;
            String trimmed = font.trimStringToWidth(text, halfWidth);
            assertFalse("Font " + name + " trimmed string must not be empty", trimmed.isEmpty());
            assertTrue("Font " + name + " trimmed string must be shorter than original", trimmed.length() < text.length());
            assertTrue("Font " + name + " trimmed string width must fit in boundary", font.getWidth(trimmed) <= halfWidth);
            String revTrimmed = font.trimStringToWidth(text, halfWidth, true);
            assertFalse("Font " + name + " reverse trimmed string must not be empty", revTrimmed.isEmpty());
            assertTrue("Font " + name + " reverse trimmed width must fit in boundary", font.getWidth(revTrimmed) <= halfWidth);
        }
    }
    @Test
    public void testComponentIntegrationWithAllFonts() {
        HeadlessGraphics gfx = new HeadlessGraphics();
        for (Map.Entry<String, TrueTypeFont> entry : LOADED_FONTS.entrySet()) {
            String name = entry.getKey();
            TrueTypeFont font = entry.getValue();
            GuiLabel label = new GuiLabel(0, 0, "Test Label", 0xFFFFFFFF);
            label.withFont(font);
            assertEquals(font, label.getFont());
            assertEquals(font.getWidth("Test Label"), label.getWidth());
            assertEquals(font.getHeight(), label.getHeight());
            GuiButton button = new GuiButton(0, 0, 100, 20, "Test Button", () -> {});
            button.withFont(font);
            assertEquals(font, button.getFont());
            GuiTextInput input = new GuiTextInput(0, 0, 120, 20);
            input.withFont(font);
            input.setText("Hello World");
            assertEquals(font, input.getFont());
            gfx.reset();
            label.render(gfx, 0, 0, 0.0f);
            assertTrue("Graphics draw calls must occur during label rendering", gfx.drawCallCount > 0);
        }
    }
    @Test
    public void testDynamicFontScaling() throws Exception {
        InputStream is = MultiFontTestingSuiteTest.class.getResourceAsStream("/fonts/showcase/press_start.ttf");
        assertNotNull(is);
        Font awt = Font.createFont(Font.TRUETYPE_FONT, is);
        is.close();
        TrueTypeFont small = new TrueTypeFont("press_small", awt, 8.0f, false);
        TrueTypeFont medium = new TrueTypeFont("press_med", awt, 14.0f, false);
        TrueTypeFont large = new TrueTypeFont("press_large", awt, 24.0f, false);
        assertEquals(8, small.getHeight());
        assertEquals(14, medium.getHeight());
        assertEquals(24, large.getHeight());
        int wS = small.getWidth("GAME OVER");
        int wM = medium.getWidth("GAME OVER");
        int wL = large.getWidth("GAME OVER");
        assertTrue("Medium font must be wider than small font", wM > wS);
        assertTrue("Large font must be wider than medium font", wL > wM);
    }
}