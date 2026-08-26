package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
public class GuiModelRender extends GuiComponent {
    private EntityLivingBase entity;
    private ItemStack itemStack;
    private float scale = 30f;
    private float rotationX = 0f;
    private float rotationY = 0f;
    private float rotationZ = 0f;
    private float offsetX = 0f;
    private float offsetY = 0f;
    private float offsetZ = 0f;
    private boolean enableEntityAnimations = true;
    private boolean allowDragRotation = false;
    private boolean lookAtCursor = false;
    private boolean dragging = false;
    private int lastMouseX;
    private int lastMouseY;
    public GuiModelRender(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public GuiModelRender setEntity(EntityLivingBase entity) {
        this.entity = entity;
        this.itemStack = null;
        return this;
    }
    public GuiModelRender setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.entity = null;
        return this;
    }
    public GuiModelRender setScale(float scale) {
        this.scale = scale;
        return this;
    }
    public GuiModelRender setEnableEntityAnimations(boolean enable) {
        this.enableEntityAnimations = enable;
        return this;
    }
    public GuiModelRender setAllowDragRotation(boolean allow) {
        this.allowDragRotation = allow;
        return this;
    }
    public GuiModelRender setLookAtCursor(boolean lookAtCursor) {
        this.lookAtCursor = lookAtCursor;
        return this;
    }
    public GuiModelRender setRotation(float rx, float ry, float rz) {
        this.rotationX = rx;
        this.rotationY = ry;
        this.rotationZ = rz;
        return this;
    }
    public GuiModelRender setTranslation(float ox, float oy, float oz) {
        this.offsetX = ox;
        this.offsetY = oy;
        this.offsetZ = oz;
        return this;
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int centerX = getAbsoluteX() + width / 2;
        int centerY = getAbsoluteY() + height / 2;
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableColorMaterial();
        GlStateManager.translate(centerX + offsetX, centerY + offsetY, 50.0F + offsetZ);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = entity != null ? entity.renderYawOffset : 0;
        float fp = entity != null ? entity.prevRenderYawOffset : 0;
        float f1 = entity != null ? entity.rotationYaw : 0;
        float f1p = entity != null ? entity.prevRotationYaw : 0;
        float f2 = entity != null ? entity.rotationPitch : 0;
        float f2p = entity != null ? entity.prevRotationPitch : 0;
        float f3 = entity != null ? entity.prevRotationYawHead : 0;
        float f4 = entity != null ? entity.rotationYawHead : 0;
        if (entity != null && lookAtCursor && !dragging) {
            float mouseRelX = (float) centerX - mouseX;
            float mouseRelY = (float) centerY - (scale * entity.getEyeHeight()) - mouseY;
            GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-((float) Math.atan((double) (mouseRelY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
            entity.renderYawOffset = (float) Math.atan((double) (mouseRelX / 40.0F)) * 20.0F;
            entity.prevRenderYawOffset = entity.renderYawOffset;
            entity.rotationYaw = (float) Math.atan((double) (mouseRelX / 40.0F)) * 40.0F;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.rotationPitch = -((float) Math.atan((double) (mouseRelY / 40.0F))) * 20.0F;
            entity.prevRotationPitch = entity.rotationPitch;
            entity.rotationYawHead = entity.rotationYaw;
            entity.prevRotationYawHead = entity.rotationYaw;
        } else {
            GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        }
        GlStateManager.rotate(rotationX, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(rotationZ, 0.0F, 0.0F, 1.0F);
        if (entity != null) {
            if (!enableEntityAnimations && !lookAtCursor) {
                entity.renderYawOffset = 0;
                entity.rotationYaw = 0;
                entity.rotationPitch = 0;
                entity.rotationYawHead = 0;
                entity.prevRotationYawHead = 0;
            }
            RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
            rendermanager.setPlayerViewY(180.0F);
            rendermanager.setRenderShadow(false);
            rendermanager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, enableEntityAnimations ? partialTicks : 1.0F, false);
            rendermanager.setRenderShadow(true);
            entity.renderYawOffset = f;
            entity.prevRenderYawOffset = fp;
            entity.rotationYaw = f1;
            entity.prevRotationYaw = f1p;
            entity.rotationPitch = f2;
            entity.prevRotationPitch = f2p;
            entity.prevRotationYawHead = f3;
            entity.rotationYawHead = f4;
        } else if (itemStack != null && !itemStack.isEmpty()) {
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(itemStack, -8, -8);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.popMatrix();
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        if (allowDragRotation && isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }
    @Override
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) return true;
        if (dragging && allowDragRotation) {
            int dx = mouseX - lastMouseX;
            int dy = mouseY - lastMouseY;
            rotationY -= dx * 0.5f;
            rotationX += dy * 0.5f;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }
    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        if (super.mouseReleased(mouseX, mouseY, state)) return true;
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
}