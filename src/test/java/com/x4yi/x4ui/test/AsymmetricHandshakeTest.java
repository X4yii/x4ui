package com.x4yi.x4ui.test;
import com.x4yi.x4ui.X4UI;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.Assert;
import org.junit.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
public class AsymmetricHandshakeTest {
    @Test
    public void testClientCheckingRemoteServer() {
        X4UI mod = new X4UI();
        boolean allowedWithoutX4UI = mod.checkRemoteVersions(Collections.emptyMap(), Side.SERVER);
        Assert.assertTrue("Client should connect even if server lacks X4UI", allowedWithoutX4UI);
        Map<String, String> serverWithX4UI = Collections.singletonMap(X4UI.MODID, X4UI.VERSION);
        boolean allowedWithX4UI = mod.checkRemoteVersions(serverWithX4UI, Side.SERVER);
        Assert.assertTrue("Client should connect if server has X4UI", allowedWithX4UI);
    }
    @Test
    public void testServerCheckingRemoteClient() {
        X4UI mod = new X4UI();
        boolean rejectedWithoutClientMod = mod.checkRemoteVersions(Collections.emptyMap(), Side.CLIENT);
        Assert.assertFalse("Dedicated server should reject client without X4UI", rejectedWithoutClientMod);
        Map<String, String> clientWithX4UI = new HashMap<>();
        clientWithX4UI.put(X4UI.MODID, X4UI.VERSION);
        boolean acceptedWithClientMod = mod.checkRemoteVersions(clientWithX4UI, Side.CLIENT);
        Assert.assertTrue("Dedicated server should accept client with X4UI", acceptedWithClientMod);
    }
}