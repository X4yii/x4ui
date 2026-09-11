package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.component.GuiImage;
import com.x4yi.x4ui.client.gui.component.image.NineSliceConfig;
import com.x4yi.x4ui.client.gui.utils.Insets;
import net.minecraft.util.ResourceLocation;
import org.junit.Assert;
import org.junit.Test;
public class NineSliceConfigTest {
    @Test
    public void testNineSliceConfig() {
        NineSliceConfig config = new NineSliceConfig(8, 128, 128);
        Assert.assertEquals(8, config.getTextureInsets().top);
        Assert.assertEquals(8, config.getTextureInsets().right);
        Assert.assertEquals(8, config.getTextureInsets().bottom);
        Assert.assertEquals(8, config.getTextureInsets().left);
        Assert.assertEquals(8, config.getBorderInsets().left);
        Assert.assertTrue(config.isFillCenter());
        Assert.assertEquals(128, config.getTextureWidth());
        Assert.assertEquals(128, config.getTextureHeight());
        Insets asymmetric = new Insets(10, 20, 30, 40);
        NineSliceConfig custom = new NineSliceConfig(asymmetric, new Insets(5, 10, 15, 20), 256, 512, false);
        Assert.assertEquals(10, custom.getTextureInsets().top);
        Assert.assertEquals(20, custom.getTextureInsets().right);
        Assert.assertEquals(30, custom.getTextureInsets().bottom);
        Assert.assertEquals(40, custom.getTextureInsets().left);
        Assert.assertEquals(5, custom.getBorderInsets().top);
        Assert.assertEquals(10, custom.getBorderInsets().right);
        Assert.assertEquals(15, custom.getBorderInsets().bottom);
        Assert.assertEquals(20, custom.getBorderInsets().left);
        Assert.assertFalse(custom.isFillCenter());
        Assert.assertEquals(256, custom.getTextureWidth());
        Assert.assertEquals(512, custom.getTextureHeight());
        NineSliceConfig chained = config.withBorder(16).withFillCenter(false);
        Assert.assertEquals(8, chained.getTextureInsets().left);
        Assert.assertEquals(16, chained.getBorderInsets().left);
        Assert.assertFalse(chained.isFillCenter());
    }
    @Test
    public void testHighResDefaultScaling() {
        NineSliceConfig highRes = new NineSliceConfig(new Insets(180), 1024, 1024);
        Assert.assertEquals(180, highRes.getTextureInsets().left);
        Assert.assertEquals(45, highRes.getBorderInsets().left);
        NineSliceConfig explicit = highRes.withBorder(18);
        Assert.assertEquals(180, explicit.getTextureInsets().left);
        Assert.assertEquals(18, explicit.getBorderInsets().left);
    }
    @Test
    public void testGuiImageNineSliceIntegration() {
        ResourceLocation tex = new ResourceLocation("x4ui", "textures/frame.png");
        GuiImage img = new GuiImage(0, 0, 100, 50, tex);
        Assert.assertFalse(img.isNineSliceEnabled());
        Assert.assertNull(img.getNineSliceConfig());
        img.withNineSlice(12, 64, 64);
        Assert.assertTrue(img.isNineSliceEnabled());
        Assert.assertNotNull(img.getNineSliceConfig());
        Assert.assertEquals(12, img.getNineSliceConfig().getTextureInsets().top);
        img.withNineSlice(new Insets(180), 18, 1024, 1024);
        Assert.assertEquals(180, img.getNineSliceConfig().getTextureInsets().left);
        Assert.assertEquals(18, img.getNineSliceConfig().getBorderInsets().left);
        img.setNineSliceEnabled(false);
        Assert.assertFalse(img.isNineSliceEnabled());
    }
    @Test
    public void testGuiImageRenderBranching() {
        ResourceLocation tex = new ResourceLocation("x4ui", "textures/frame.png");
        GuiImage img = new GuiImage(0, 0, 100, 50, tex);
        class TrackingGraphics extends HeadlessGraphics {
            boolean nineSliceCalled = false;
            boolean regionCalled = false;
            @Override
            public void drawNineSlice(ResourceLocation texture, int x, int y, int width, int height, Insets sliceInsets, int texWidth, int texHeight) {
                super.drawNineSlice(texture, x, y, width, height, sliceInsets, texWidth, texHeight);
                this.nineSliceCalled = true;
            }
            @Override
            public void drawNineSlice(ResourceLocation texture, int x, int y, int width, int height, Insets textureInsets, Insets borderInsets, int texWidth, int texHeight, boolean fillCenter) {
                super.drawNineSlice(texture, x, y, width, height, textureInsets, borderInsets, texWidth, texHeight, fillCenter);
                this.nineSliceCalled = true;
            }
            @Override
            public void drawTextureRegion(ResourceLocation texture, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) {
                super.drawTextureRegion(texture, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight);
                this.regionCalled = true;
            }
        }
        TrackingGraphics gfx = new TrackingGraphics();
        img.render(gfx, 0, 0, 0.0f);
        Assert.assertTrue(gfx.regionCalled);
        Assert.assertFalse(gfx.nineSliceCalled);
        gfx.regionCalled = false;
        gfx.nineSliceCalled = false;
        img.withNineSlice(8, 64, 64);
        img.render(gfx, 0, 0, 0.0f);
        Assert.assertTrue(gfx.nineSliceCalled);
        Assert.assertFalse(gfx.regionCalled);
    }
}