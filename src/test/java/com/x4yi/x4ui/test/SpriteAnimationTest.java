package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.component.GuiSprite;
import com.x4yi.x4ui.client.gui.component.sprite.LoopMode;
import com.x4yi.x4ui.client.gui.component.sprite.SequenceSource;
import com.x4yi.x4ui.client.gui.component.sprite.SpriteSheetSource;
import net.minecraft.util.ResourceLocation;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class SpriteAnimationTest {
    @Test
    public void testSpriteSheetUVGrid() {
        ResourceLocation tex = new ResourceLocation("x4ui", "textures/sheet.png");
        SpriteSheetSource source = new SpriteSheetSource(tex, 256, 256, 64, 64, 16);
        Assert.assertEquals(16, source.getFrameCount());
        float[] uv = new float[4];
        source.getUV(0, uv);
        Assert.assertEquals(0.0f, uv[0], 0.001f);
        Assert.assertEquals(0.0f, uv[1], 0.001f);
        Assert.assertEquals(64.0f, uv[2], 0.001f);
        Assert.assertEquals(64.0f, uv[3], 0.001f);
        source.getUV(1, uv);
        Assert.assertEquals(64.0f, uv[0], 0.001f);
        Assert.assertEquals(0.0f, uv[1], 0.001f);
        source.getUV(5, uv);
        Assert.assertEquals(64.0f, uv[0], 0.001f);
        Assert.assertEquals(64.0f, uv[1], 0.001f);
        source.getUV(15, uv);
        Assert.assertEquals(192.0f, uv[0], 0.001f);
        Assert.assertEquals(192.0f, uv[1], 0.001f);
    }
    @Test
    public void testSequenceSource() {
        ResourceLocation[] frames = new ResourceLocation[]{
                new ResourceLocation("test", "frame0.png"),
                new ResourceLocation("test", "frame1.png"),
                new ResourceLocation("test", "frame2.png")
        };
        SequenceSource source = new SequenceSource(frames, 32, 32);
        Assert.assertEquals(3, source.getFrameCount());
        Assert.assertEquals(frames[0], source.getTexture(0));
        Assert.assertEquals(frames[1], source.getTexture(1));
        Assert.assertEquals(frames[2], source.getTexture(2));
        Assert.assertEquals(frames[2], source.getTexture(99));
        Assert.assertEquals(frames[0], source.getTexture(-5));
    }
    @Test
    public void testLoopModeLoop() {
        ResourceLocation tex = new ResourceLocation("test", "anim.png");
        SpriteSheetSource source = new SpriteSheetSource(tex, 128, 32, 32, 32, 4);
        GuiSprite sprite = new GuiSprite(0, 0, 32, 32, source);
        sprite.setFPS(10.0f);
        sprite.setLoopMode(LoopMode.LOOP);
        Assert.assertEquals(0, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(1, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(2, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(3, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(0, sprite.getCurrentFrame());
    }
    @Test
    public void testLoopModeOnceAndCallbacks() {
        ResourceLocation tex = new ResourceLocation("test", "anim.png");
        SpriteSheetSource source = new SpriteSheetSource(tex, 96, 32, 32, 32, 3);
        GuiSprite sprite = new GuiSprite(0, 0, 32, 32, source);
        sprite.setFPS(10.0f);
        sprite.setLoopMode(LoopMode.ONCE);
        AtomicInteger frameChanges = new AtomicInteger(0);
        AtomicBoolean finished = new AtomicBoolean(false);
        sprite.setOnFrameChanged(f -> frameChanges.incrementAndGet());
        sprite.setOnFinished(() -> finished.set(true));
        sprite.update(0.105f);
        Assert.assertEquals(1, sprite.getCurrentFrame());
        Assert.assertFalse(finished.get());
        sprite.update(0.105f);
        Assert.assertEquals(2, sprite.getCurrentFrame());
        Assert.assertFalse(finished.get());
        sprite.update(0.105f);
        Assert.assertEquals(2, sprite.getCurrentFrame());
        Assert.assertTrue(finished.get());
        Assert.assertFalse(sprite.isPlaying());
    }
    @Test
    public void testLoopModePingPong() {
        ResourceLocation tex = new ResourceLocation("test", "anim.png");
        SpriteSheetSource source = new SpriteSheetSource(tex, 96, 32, 32, 32, 3);
        GuiSprite sprite = new GuiSprite(0, 0, 32, 32, source);
        sprite.setFPS(10.0f);
        sprite.setLoopMode(LoopMode.PING_PONG);
        Assert.assertEquals(0, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(1, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(2, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(1, sprite.getCurrentFrame());
        sprite.update(0.105f);
        Assert.assertEquals(0, sprite.getCurrentFrame());
    }
    @Test
    public void testFlipAndUVScroll() {
        ResourceLocation tex = new ResourceLocation("test", "anim.png");
        SpriteSheetSource source = new SpriteSheetSource(tex, 32, 32, 32, 32, 1);
        GuiSprite sprite = new GuiSprite(0, 0, 32, 32, source);
        Assert.assertFalse(sprite.isFlipX());
        Assert.assertFalse(sprite.isFlipY());
        sprite.setFlipX(true);
        sprite.setFlipY(true);
        Assert.assertTrue(sprite.isFlipX());
        Assert.assertTrue(sprite.isFlipY());
        sprite.setUVScrollSpeed(10.0f, 20.0f);
        sprite.update(0.5f);
    }
}