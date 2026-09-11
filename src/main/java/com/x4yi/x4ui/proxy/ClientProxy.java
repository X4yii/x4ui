package com.x4yi.x4ui.proxy;
import com.x4yi.x4ui.api.client.media.IMediaService;
import com.x4yi.x4ui.client.gui.animation.GuiAnimator;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import com.x4yi.x4ui.impl.client.media.MediaService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    private IMediaService mediaService;
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MediaManager.setBinDir(event.getModConfigurationDirectory().getParentFile());
        this.mediaService = new MediaService();
        scanModMediaManifests();
        com.x4yi.x4ui.impl.client.media.MediaPipeline.getInstance().enqueuePreloads(
                com.x4yi.x4ui.api.client.media.MediaRegistry.getAllRegisteredMedia()
        );
    }
    private void scanModMediaManifests() {
        try {
            for (net.minecraftforge.fml.common.ModContainer mod : net.minecraftforge.fml.common.Loader.instance().getActiveModList()) {
                String modId = mod.getModId().toLowerCase(java.util.Locale.ROOT);
                String path = "assets/" + modId + "/x4ui/media.json";
                java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                if (is != null) {
                    try (java.io.InputStreamReader reader = new java.io.InputStreamReader(is, "UTF-8")) {
                        com.google.gson.JsonElement elem = new com.google.gson.JsonParser().parse(reader);
                        if (elem.isJsonArray()) {
                            java.util.List<String> list = new java.util.ArrayList<>();
                            for (com.google.gson.JsonElement item : elem.getAsJsonArray()) {
                                if (item.isJsonPrimitive()) list.add(item.getAsString());
                            }
                            com.x4yi.x4ui.api.client.media.MediaRegistry.registerMedia(modId, list);
                        } else if (elem.isJsonObject()) {
                            com.google.gson.JsonObject obj = elem.getAsJsonObject();
                            if (obj.has("requireFFmpeg") && obj.get("requireFFmpeg").getAsBoolean()) {
                                com.x4yi.x4ui.api.client.media.MediaRegistry.requireFFmpeg(modId);
                            }
                            if (obj.has("videos") && obj.get("videos").isJsonArray()) {
                                java.util.List<String> list = new java.util.ArrayList<>();
                                for (com.google.gson.JsonElement item : obj.getAsJsonArray("videos")) {
                                    if (item.isJsonPrimitive()) list.add(item.getAsString());
                                }
                                com.x4yi.x4ui.api.client.media.MediaRegistry.registerMedia(modId, list);
                            }
                        }
                    } catch (Throwable t) {
                        System.err.println("[X4UI Media] Failed to parse manifest for " + modId + ": " + t.getMessage());
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(com.x4yi.x4ui.client.gui.overlay.GuiOverlayManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(com.x4yi.x4ui.client.gui.component.toast.GuiToastManager.INSTANCE);
        com.x4yi.x4ui.client.gui.overlay.GuiOverlayManager.INSTANCE.registerOverlay(com.x4yi.x4ui.client.gui.component.toast.GuiToastManager.INSTANCE);
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc != null && mc.getResourceManager() instanceof net.minecraft.client.resources.IReloadableResourceManager) {
            ((net.minecraft.client.resources.IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(com.x4yi.x4ui.impl.client.font.FontRegistry.INSTANCE);
        }
    }
    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        super.loadComplete(event);
        java.util.List<com.x4yi.x4ui.impl.client.media.MediaPipeline.PreloadTask> tasks = 
                com.x4yi.x4ui.impl.client.media.MediaPipeline.getInstance().getPendingPreloadTasks();
        boolean ffmpegRequired = com.x4yi.x4ui.api.client.media.MediaRegistry.isFFmpegRequired() || !tasks.isEmpty();
        int totalSteps = (ffmpegRequired ? 1 : 0) + tasks.size();
        if (totalSteps > 0) {
            net.minecraftforge.fml.common.ProgressManager.ProgressBar bar = 
                    net.minecraftforge.fml.common.ProgressManager.push("X4UI Media Engine", totalSteps);
            long startTime = System.currentTimeMillis();
            long maxWaitMs = 35000;
            if (ffmpegRequired) {
                bar.step("X4UI: FFmpeg Media Engine");
                while (!MediaManager.isFFmpegAvailable() && (System.currentTimeMillis() - startTime < maxWaitMs)) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) { break; }
                }
            }
            for (int i = 0; i < tasks.size(); i++) {
                com.x4yi.x4ui.impl.client.media.MediaPipeline.PreloadTask task = tasks.get(i);
                bar.step("X4UI: [" + task.modId + "] " + task.fileName + " (" + (i + 1) + "/" + tasks.size() + ")");
                while (!task.finished && !task.failed && (System.currentTimeMillis() - startTime < maxWaitMs)) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) { break; }
                }
            }
            net.minecraftforge.fml.common.ProgressManager.pop(bar);
        }
    }
    @Override
    public IMediaService getMediaService() {
        return mediaService;
    }
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GuiAnimator.update();
        }
    }
    @SubscribeEvent
    public void onClientDisconnect(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        com.x4yi.x4ui.client.gui.bus.UIStateBus.clearSessionStates();
        GuiAnimator.clearAnimations();
    }
}