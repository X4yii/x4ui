package com.x4yi.x4ui.test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
public class HeadlessGraphicsTest {
    private HeadlessGraphics gfx;
    @Before
    public void setUp() {
        gfx = new HeadlessGraphics();
    }
    @Test
    public void testSaveAndRestoreStateStack() {
        gfx.save();
        Assert.assertEquals(1, gfx.depth);
        Assert.assertEquals(1, gfx.saveCount);
        gfx.save();
        Assert.assertEquals(2, gfx.depth);
        Assert.assertEquals(2, gfx.saveCount);
        gfx.restore();
        Assert.assertEquals(1, gfx.depth);
        Assert.assertEquals(1, gfx.restoreCount);
        gfx.restore();
        Assert.assertEquals(0, gfx.depth);
        Assert.assertEquals(2, gfx.restoreCount);
    }
    @Test
    public void testNestedClipIntersection() {
        gfx.pushClip(0, 0, 100, 100);
        int[] clip1 = gfx.getCurrentClip();
        Assert.assertArrayEquals(new int[]{0, 0, 100, 100}, clip1);
        gfx.pushClip(20, 30, 50, 50);
        int[] clip2 = gfx.getCurrentClip();
        Assert.assertArrayEquals(new int[]{20, 30, 50, 50}, clip2);
        gfx.pushClip(40, 40, 100, 20);
        int[] clip3 = gfx.getCurrentClip();
        Assert.assertArrayEquals(new int[]{40, 40, 30, 20}, clip3);
        gfx.popClip();
        Assert.assertArrayEquals(new int[]{20, 30, 50, 50}, gfx.getCurrentClip());
        gfx.popClip();
        Assert.assertArrayEquals(new int[]{0, 0, 100, 100}, gfx.getCurrentClip());
        gfx.popClip();
        Assert.assertNull(gfx.getCurrentClip());
    }
    @Test
    public void testIsClippedViewportCulling() {
        gfx.pushClip(10, 10, 80, 80);
        Assert.assertTrue(gfx.isClipped(0, 10, 5, 20));
        Assert.assertTrue(gfx.isClipped(95, 10, 20, 20));
        Assert.assertTrue(gfx.isClipped(10, 0, 20, 5));
        Assert.assertTrue(gfx.isClipped(10, 95, 20, 20));
        Assert.assertFalse(gfx.isClipped(20, 20, 10, 10));
        Assert.assertFalse(gfx.isClipped(5, 5, 20, 20));
    }
}