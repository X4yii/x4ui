package com.x4yi.x4ui.test;

import com.x4yi.x4ui.client.gui.bus.UIStateBus;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import com.x4yi.x4ui.client.gui.component.layout.FlexAlign;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderDouble;
import com.x4yi.x4ui.client.gui.component.slider.GuiSliderInt;
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.platform.PlatformBridge;
import com.x4yi.x4ui.common.sync.StateContainerListener;
import com.x4yi.x4ui.impl.client.resource.network.SecurityUtils;
import net.minecraft.inventory.Slot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class R10b6ImprovementsTest {

    @After
    public void cleanupBus() {
        UIStateBus.clear();
    }

    @Test
    public void testAtomic32BitStateContainerListenerWaitsForBothHalves() {
        StateContainerListener listener = new StateContainerListener();
        State<Integer> state = new State<>(0);
        listener.bind32BitProperty(0, 1, state);

        listener.sendWindowProperty(null, 0, 0xBEEF);
        Assert.assertEquals(Integer.valueOf(0), state.get());

        listener.sendWindowProperty(null, 1, 0x1234);
        Assert.assertEquals(Integer.valueOf(0x1234BEEF), state.get());
    }

    @Test
    public void testGuiSlotHidesOnAncestorInvisible() {
        GuiPanel root = new GuiPanel(0, 0, 200, 200);
        GuiPanel tab = new GuiPanel(0, 0, 100, 100);
        Slot mcSlot = new Slot(null, 0, 10, 10);
        GuiSlot guiSlot = new GuiSlot(mcSlot, null);

        root.addChild(tab);
        tab.addChild(guiSlot);

        Assert.assertTrue(guiSlot.isEffectivelyVisible());

        tab.setVisible(false);
        Assert.assertFalse(guiSlot.isEffectivelyVisible());
        Assert.assertEquals(-9999, mcSlot.xPos);
        Assert.assertEquals(-9999, mcSlot.yPos);
    }

    @Test
    public void testSecurityUtilsPathTraversalAndSsrf() throws IOException {
        Assert.assertEquals("my_mod_1", SecurityUtils.sanitizeModId("My Mod 1"));
        Assert.assertEquals("___", SecurityUtils.sanitizeModId("../.."));
        Assert.assertEquals("common", SecurityUtils.sanitizeModId("   "));

        File root = new File(System.getProperty("java.io.tmpdir"), "x4ui_sec_test");
        File ok = SecurityUtils.resolveSafeChild(root, "assets/video.mp4");
        Assert.assertTrue(ok.getCanonicalPath().startsWith(root.getCanonicalPath()));

        try {
            SecurityUtils.resolveSafeChild(root, "../../etc/passwd");
            Assert.fail("Expected SecurityException for path traversal");
        } catch (SecurityException expected) {
        }

        try {
            SecurityUtils.validateHttpUrl("file:///etc/passwd");
            Assert.fail("Expected SecurityException for file:// protocol");
        } catch (SecurityException expected) {
        }

        try {
            SecurityUtils.validateHttpUrl("http://127.0.0.1:8080/secret");
            Assert.fail("Expected SecurityException for loopback SSRF");
        } catch (SecurityException expected) {
        }

        try {
            SecurityUtils.validateHttpUrl("http://169.254.169.254/latest/meta-data/");
            Assert.fail("Expected SecurityException for link-local SSRF");
        } catch (SecurityException expected) {
        }

        Assert.assertEquals(".mp4", SecurityUtils.sanitizeExtension("https://cdn.example.com/v/test.mp4?token=123#t=1", ".bin"));
        Assert.assertEquals(".bin", SecurityUtils.sanitizeExtension("https://cdn.example.com/noext", ".bin"));
    }

    @Test
    public void testGuiSliderStepRelativeToMin() {
        State<Integer> intState = new State<>(10);
        GuiSliderInt intSlider = new GuiSliderInt(0, 0, 100, 20, intState, 10, 30, 5);
        intSlider.onMouseClick(25, 10, 0);
        Assert.assertEquals(Integer.valueOf(15), intState.get());

        State<Double> dblState = new State<>(0.5);
        GuiSliderDouble dblSlider = new GuiSliderDouble(0, 0, 100, 20, dblState, 0.5, 2.5, 0.5);
        dblSlider.onMouseClick(50, 10, 0);
        Assert.assertEquals(1.5, dblState.get(), 0.0001);
    }

    @Test
    public void testUIStateBusSessionVsGlobalScope() {
        State<String> sessionState = UIStateBus.getOrCreate("test:session", "s1", UIStateBus.Scope.SESSION);
        State<String> globalState = UIStateBus.getOrCreate("test:global", "g1", UIStateBus.Scope.GLOBAL);

        Assert.assertSame(sessionState, UIStateBus.get("test:session", String.class));
        Assert.assertSame(globalState, UIStateBus.get("test:global", String.class));

        UIStateBus.clearSessionStates();

        Assert.assertNull(UIStateBus.get("test:session", String.class));
        Assert.assertSame(globalState, UIStateBus.get("test:global", String.class));
    }

    @Test
    public void testPlatformBridgeCleanDirectBuffer() {
        ByteBuffer direct = ByteBuffer.allocateDirect(4096);
        direct.putInt(0, 42);
        PlatformBridge.cleanDirectBuffer(direct);
        PlatformBridge.cleanDirectBuffer(null);

        int javaVer = PlatformBridge.getJavaFeatureVersion();
        Assert.assertTrue(javaVer >= 8);
        Assert.assertEquals(javaVer >= 21, PlatformBridge.isVirtualThreadsSupported());
        Assert.assertNotNull(PlatformBridge.getRuntimeDescription());
        java.util.concurrent.ExecutorService exec = PlatformBridge.newIoExecutor("x4ui-test", 2);
        try {
            Assert.assertNotNull(exec);
        } finally {
            exec.shutdownNow();
        }
    }

    @Test
    public void testFlexAlignCenterAndSpacer() {
        GuiPanel root = new GuiPanel(0, 0, 200, 100);
        root.setFlexDirection(com.x4yi.x4ui.client.gui.component.layout.FlexDirection.VERTICAL);
        root.setAlignItems(FlexAlign.CENTER);
        root.setJustifyContent(FlexAlign.CENTER);

        GuiPanel row = GuiPanel.row(4);
        row.withPercentWidth(-1.0f);
        row.setWidth(100);

        GuiPanel left = new GuiPanel(0, 0, 20, 10);
        GuiPanel right = new GuiPanel(0, 0, 30, 20);
        row.addChild(left);
        row.addChild(GuiPanel.spacer());
        row.addChild(right);

        root.addChild(row);
        root.update(0.016f);

        Assert.assertEquals(50, row.getX());
        Assert.assertEquals(40, row.getY());
        Assert.assertEquals(0, left.getX());
        Assert.assertEquals(5, left.getY());
        Assert.assertEquals(70, right.getX());
    }

    @Test
    public void testUIStateBusScreenScopeAndNamespace() {
        State<String> screenState = UIStateBus.getOrCreate(
                UIStateBus.scopedKey("tile:1,2,3", "mode"), "auto", UIStateBus.Scope.SCREEN
        );
        State<String> sessionState = UIStateBus.getOrCreate("session:token", "abc", UIStateBus.Scope.SESSION);

        Assert.assertEquals("auto", screenState.get());
        UIStateBus.clearScreenStates();
        Assert.assertNull(UIStateBus.get("tile:1,2,3:mode", String.class));
        Assert.assertSame(sessionState, UIStateBus.get("session:token", String.class));

        UIStateBus.getOrCreate(UIStateBus.scopedKey("faction:red", "power"), 100, UIStateBus.Scope.GLOBAL);
        UIStateBus.clearNamespace("faction:red");
        Assert.assertNull(UIStateBus.get("faction:red:power", Integer.class));
    }

    @Test
    public void testNetworkBatchRpcAndClientOnlyFallback() {
        java.util.Map<String, Object> batch = new java.util.LinkedHashMap<>();
        batch.put("k_int", 42);
        batch.put("k_bool", true);
        batch.put("k_str", "hello");
        com.x4yi.x4ui.common.network.PacketStateBatch outBatch = new com.x4yi.x4ui.common.network.PacketStateBatch(batch);
        io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
        outBatch.toBytes(buf);
        com.x4yi.x4ui.common.network.PacketStateBatch inBatch = new com.x4yi.x4ui.common.network.PacketStateBatch();
        inBatch.fromBytes(buf);
        buf.release();

        net.minecraft.nbt.NBTTagCompound params = new net.minecraft.nbt.NBTTagCompound();
        params.setString("q", "iron");
        com.x4yi.x4ui.common.network.PacketGuiRpc req = com.x4yi.x4ui.common.network.PacketGuiRpc.request(7L, "test:search", params);
        io.netty.buffer.ByteBuf rpcBuf = io.netty.buffer.Unpooled.buffer();
        req.toBytes(rpcBuf);
        com.x4yi.x4ui.common.network.PacketGuiRpc decodedReq = new com.x4yi.x4ui.common.network.PacketGuiRpc();
        decodedReq.fromBytes(rpcBuf);
        rpcBuf.release();

        com.x4yi.x4ui.common.network.UINetwork.setServerSupported(false, null);
        final String[] errCode = {null};
        com.x4yi.x4ui.common.network.UINetwork.request("test:search", params, res -> errCode[0] = res.getErrorCode());
        Assert.assertEquals("NO_SERVER_SUPPORT", errCode[0]);

        final boolean[] ranFallback = {false};
        com.x4yi.x4ui.common.network.UINetwork.sendActionOrLocal("test:act", null, () -> ranFallback[0] = true);
        Assert.assertTrue(ranFallback[0]);

        com.x4yi.x4ui.client.gui.ScreenRegistry.register("test:dummy", tag -> null);
        Assert.assertTrue(com.x4yi.x4ui.client.gui.ScreenRegistry.has("test:dummy"));
        com.x4yi.x4ui.client.gui.ScreenRegistry.unregister("test:dummy");
        Assert.assertFalse(com.x4yi.x4ui.client.gui.ScreenRegistry.has("test:dummy"));
    }

    @Test
    public void testUIInputNormalizationAndZeroDisplaySafety() {
        Assert.assertEquals(0, com.x4yi.x4ui.client.gui.input.UIInput.normalizeWheelDelta(0));
        Assert.assertEquals(1, com.x4yi.x4ui.client.gui.input.UIInput.normalizeWheelDelta(120));
        Assert.assertEquals(-1, com.x4yi.x4ui.client.gui.input.UIInput.normalizeWheelDelta(-120));
        Assert.assertEquals(2, com.x4yi.x4ui.client.gui.input.UIInput.normalizeWheelDelta(240));
        Assert.assertEquals(1, com.x4yi.x4ui.client.gui.input.UIInput.normalizeWheelDelta(15));
        Assert.assertEquals(-1, com.x4yi.x4ui.client.gui.input.UIInput.normalizeWheelDelta(-15));

        Assert.assertEquals(0, com.x4yi.x4ui.client.gui.input.UIInput.safeMouseX(null, 400));
        Assert.assertEquals(0, com.x4yi.x4ui.client.gui.input.UIInput.safeMouseY(null, 300));
        Assert.assertEquals(28, com.x4yi.x4ui.client.gui.input.KeyCodes.ENTER);
    }

    @Test
    public void testMediaManagerOsDiscoveryAndOverride() throws IOException {
        File tmpDir = java.nio.file.Files.createTempDirectory("x4ui_ffmpeg_test").toFile();
        File fakeExe = new File(tmpDir, "ffmpeg");
        byte[] payload = new byte[2048];
        java.nio.file.Files.write(fakeExe.toPath(), payload);
        fakeExe.setExecutable(true);

        String prevProp = System.getProperty("x4ui.ffmpeg.path");
        try {
            System.setProperty("x4ui.ffmpeg.path", fakeExe.getAbsolutePath());
            File resolved = com.x4yi.x4ui.impl.client.media.MediaManager.resolveFFmpegExecutable();
            Assert.assertNotNull(resolved);
            Assert.assertEquals(fakeExe.getAbsolutePath(), resolved.getAbsolutePath());
            Assert.assertEquals("system-property", com.x4yi.x4ui.impl.client.media.MediaManager.getFFmpegSource());
            Assert.assertTrue(com.x4yi.x4ui.impl.client.media.MediaManager.isFFmpegAvailable());
        } finally {
            if (prevProp == null) {
                System.clearProperty("x4ui.ffmpeg.path");
            } else {
                System.setProperty("x4ui.ffmpeg.path", prevProp);
            }
            com.x4yi.x4ui.impl.client.media.MediaManager.resolveFFmpegExecutable();
            fakeExe.delete();
            tmpDir.delete();
        }
    }
}

