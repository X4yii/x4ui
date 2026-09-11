package com.x4yi.x4ui.client.gui.component.video;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.api.client.media.IVideoControlsLayout;
import com.x4yi.x4ui.client.gui.component.GuiVideo;
public class DefaultVideoControlsLayout implements IVideoControlsLayout {
    private GuiVideo video;
    private float controlsAlpha = 0.0f;
    private float idleTimer = 0.0f;
    private boolean isScrubbing = false;
    private double scrubTargetTime = 0.0;
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    private static final int BAR_PADDING = 8;
    private static final int CONTROLS_HEIGHT = 44;
    @Override
    public void attach(GuiVideo video) {
        this.video = video;
        this.controlsAlpha = 1.0f;
        this.idleTimer = 0.0f;
    }
    @Override
    public void detach() {
        this.video = null;
        this.isScrubbing = false;
    }
    @Override
    public void update(float deltaTime) {
        if (video == null) return;
        if (video.isPlaying() && !isScrubbing) {
            idleTimer += deltaTime;
        } else {
            idleTimer = 0.0f;
        }
        float targetAlpha = 0.0f;
        if (isScrubbing || !video.isPlaying() || idleTimer < 2.5f) {
            targetAlpha = 1.0f;
        }
        if (controlsAlpha < targetAlpha) {
            controlsAlpha = Math.min(targetAlpha, controlsAlpha + deltaTime * 6.0f);
        } else if (controlsAlpha > targetAlpha) {
            controlsAlpha = Math.max(targetAlpha, controlsAlpha - deltaTime * 3.0f);
        }
    }
    @Override
    public void render(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        if (video == null || controlsAlpha <= 0.02f) return;
        if (mouseX != lastMouseX || mouseY != lastMouseY) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            if (isInsideVideo(mouseX, mouseY)) {
                idleTimer = 0.0f;
            }
        }
        int absX = video.getAbsoluteX();
        int absY = video.getAbsoluteY();
        int width = video.getWidth();
        int height = video.getHeight();
        gfx.pushAlpha(controlsAlpha);
        int gradientHeight = Math.min(height, 56);
        gfx.drawGradient(absX, absY + height - gradientHeight, width, gradientHeight, 
                0x00000000, 0xCC0B0E17);
        int barX = absX + BAR_PADDING;
        int barW = Math.max(20, width - BAR_PADDING * 2);
        int barBaseY = absY + height - 28;
        boolean barHovered = (mouseX >= barX && mouseX <= barX + barW && mouseY >= barBaseY - 4 && mouseY <= barBaseY + 8);
        int barHeight = (barHovered || isScrubbing) ? 5 : 3;
        int barY = barBaseY - (barHeight - 3) / 2;
        gfx.drawRoundedRect(barX, barY, barW, barHeight, 2, 0x55FFFFFF);
        double duration = video.getMediaPlayer().getDuration();
        double current = isScrubbing ? scrubTargetTime : video.getMediaPlayer().getPlaybackTimeSec();
        float progress = (duration > 0) ? (float) Math.max(0.0, Math.min(1.0, current / duration)) : 0.0f;
        int playedW = (int) (barW * progress);
        if (playedW > 0) {
            gfx.drawRoundedRect(barX, barY, playedW, barHeight, 2, 0xFF00E5FF);
        }
        int thumbX = barX + playedW;
        int thumbY = barY + barHeight / 2;
        int thumbRadius = (barHovered || isScrubbing) ? 5 : 3;
        gfx.drawCircle(thumbX, thumbY, thumbRadius, 0xFF00E5FF);
        if (barHovered && duration > 0) {
            float hoverRatio = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - barX) / barW));
            double hoverSec = hoverRatio * duration;
            int hM = (int) (hoverSec / 60);
            int hS = (int) (hoverSec % 60);
            String hoverStr = String.format("%02d:%02d", hM, hS);
            int ttW = 32;
            int ttH = 12;
            int ttX = Math.max(absX, Math.min(absX + width - ttW, mouseX - ttW / 2));
            int ttY = barY - 16;
            gfx.drawRoundedRect(ttX, ttY, ttW, ttH, 3, 0xEE121520);
            gfx.drawRoundedBorder(ttX, ttY, ttW, ttH, 3, 1, 0xFF2A3048);
            gfx.drawString(hoverStr, ttX + 3, ttY + 2, 0xFFFFFFFF, false);
        }
        int rowY = absY + height - 20;
        boolean isPlaying = video.isPlaying();
        String playGlyph = isPlaying ? "||" : ">";
        boolean playHovered = (mouseX >= absX + 8 && mouseX <= absX + 24 && mouseY >= rowY - 2 && mouseY <= rowY + 14);
        int playColor = playHovered ? 0xFF00E5FF : 0xFFFFFFFF;
        gfx.drawString(playGlyph, absX + 10, rowY, playColor, false);
        int curM = (int) (Math.max(0, current) / 60);
        int curS = (int) (Math.max(0, current) % 60);
        int durM = (duration > 0) ? (int) (duration / 60) : 0;
        int durS = (duration > 0) ? (int) (duration % 60) : 0;
        String timeStr = String.format("%02d:%02d / %02d:%02d", curM, curS, durM, durS);
        gfx.drawString(timeStr, absX + 28, rowY + 1, 0xFFD0D0D0, false);
        int volBtnX = absX + width - 36;
        boolean volHovered = (mouseX >= volBtnX && mouseX <= volBtnX + 28 && mouseY >= rowY - 2 && mouseY <= rowY + 14);
        String volStr = video.isMuted() ? "[MUTE]" : String.format("[%d%%]", (int)(video.getVolume() * 100));
        int volColor = volHovered ? 0xFF00E5FF : (video.isMuted() ? 0xFFFF5555 : 0xFFB0C0E0);
        gfx.drawString(volStr, volBtnX - 10, rowY + 1, volColor, false);
        gfx.popAlpha();
    }
    private boolean isInsideVideo(int mouseX, int mouseY) {
        if (video == null) return false;
        int absX = video.getAbsoluteX();
        int absY = video.getAbsoluteY();
        return mouseX >= absX && mouseX <= absX + video.getWidth() &&
               mouseY >= absY && mouseY <= absY + video.getHeight();
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (video == null || controlsAlpha < 0.25f || mouseButton != 0) return false;
        int absX = video.getAbsoluteX();
        int absY = video.getAbsoluteY();
        int width = video.getWidth();
        int height = video.getHeight();
        int barX = absX + BAR_PADDING;
        int barW = Math.max(20, width - BAR_PADDING * 2);
        int barBaseY = absY + height - 28;
        if (mouseX >= barX && mouseX <= barX + barW && mouseY >= barBaseY - 6 && mouseY <= barBaseY + 10) {
            double duration = video.getMediaPlayer().getDuration();
            if (duration > 0) {
                isScrubbing = true;
                float ratio = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - barX) / barW));
                scrubTargetTime = ratio * duration;
                video.getMediaPlayer().seek(scrubTargetTime);
                idleTimer = 0.0f;
                return true;
            }
        }
        int rowY = absY + height - 20;
        if (mouseX >= absX + 6 && mouseX <= absX + 26 && mouseY >= rowY - 3 && mouseY <= rowY + 15) {
            if (video.isPlaying()) {
                video.pause();
            } else {
                video.play();
            }
            idleTimer = 0.0f;
            return true;
        }
        int volBtnX = absX + width - 46;
        if (mouseX >= volBtnX && mouseX <= absX + width - 4 && mouseY >= rowY - 3 && mouseY <= rowY + 15) {
            video.setMuted(!video.isMuted());
            idleTimer = 0.0f;
            return true;
        }
        return false;
    }
    @Override
    public boolean onMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (video == null || mouseButton != 0) return false;
        if (isScrubbing) {
            int absX = video.getAbsoluteX();
            int width = video.getWidth();
            int barX = absX + BAR_PADDING;
            int barW = Math.max(20, width - BAR_PADDING * 2);
            double duration = video.getMediaPlayer().getDuration();
            if (duration > 0) {
                float ratio = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - barX) / barW));
                scrubTargetTime = ratio * duration;
                video.getMediaPlayer().seek(scrubTargetTime);
                idleTimer = 0.0f;
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int state) {
        if (isScrubbing) {
            isScrubbing = false;
            idleTimer = 0.0f;
            return true;
        }
        return false;
    }
}