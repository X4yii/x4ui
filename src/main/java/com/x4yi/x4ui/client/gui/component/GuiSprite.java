package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.sprite.IAnimatable;
import com.x4yi.x4ui.client.gui.component.sprite.ISpriteSource;
import com.x4yi.x4ui.client.gui.component.sprite.LoopMode;
import com.x4yi.x4ui.client.gui.component.sprite.SpriteSheetSource;
import com.x4yi.x4ui.client.gui.component.sprite.SequenceSource;
import com.x4yi.x4ui.client.gui.utils.Insets;
import net.minecraft.util.ResourceLocation;
import java.util.function.Consumer;
public class GuiSprite extends GuiComponent implements IAnimatable {
    private ISpriteSource source;
    private float fps = 12.0f;
    private LoopMode loopMode = LoopMode.LOOP;
    private boolean playing = true;
    private float elapsedTime = 0.0f;
    private int currentFrame = 0;
    private int pingPongDirection = 1;
    private boolean flipX = false;
    private boolean flipY = false;
    private float uvScrollU = 0.0f;
    private float uvScrollV = 0.0f;
    private float uvScrollSpeedU = 0.0f;
    private float uvScrollSpeedV = 0.0f;
    private Consumer<Integer> onFrameChanged = null;
    private Runnable onFinished = null;
    private final float[] uvBuffer = new float[4];
    public GuiSprite(int x, int y, int width, int height, ISpriteSource source) {
        super(x, y, width, height);
        this.source = source;
    }
    public static GuiSprite fromSheet(int x, int y, int width, int height, ResourceLocation texture, int texWidth, int texHeight, int frameWidth, int frameHeight) {
        return new GuiSprite(x, y, width, height, new SpriteSheetSource(texture, texWidth, texHeight, frameWidth, frameHeight));
    }
    public static GuiSprite fromSheet(int x, int y, int width, int height, ResourceLocation texture, int texWidth, int texHeight, int frameWidth, int frameHeight, int totalFrames) {
        return new GuiSprite(x, y, width, height, new SpriteSheetSource(texture, texWidth, texHeight, frameWidth, frameHeight, totalFrames));
    }
    public static GuiSprite fromSequence(int x, int y, int width, int height, ResourceLocation[] frames, int frameWidth, int frameHeight) {
        return new GuiSprite(x, y, width, height, new SequenceSource(frames, frameWidth, frameHeight));
    }
    public ISpriteSource getSource() {
        return source;
    }
    public GuiSprite setSource(ISpriteSource source) {
        this.source = source;
        reset();
        return this;
    }
    @Override
    public void play() {
        this.playing = true;
    }
    @Override
    public void pause() {
        this.playing = false;
    }
    @Override
    public void stop() {
        this.playing = false;
        reset();
    }
    @Override
    public void reset() {
        this.elapsedTime = 0.0f;
        this.pingPongDirection = 1;
        setFrameInternal(0);
    }
    @Override
    public boolean isPlaying() {
        return playing;
    }
    @Override
    public void setFPS(float fps) {
        this.fps = Math.max(0.001f, fps);
    }
    public GuiSprite withFPS(float fps) {
        setFPS(fps);
        return this;
    }
    @Override
    public float getFPS() {
        return fps;
    }
    @Override
    public void setFrame(int frame) {
        int total = getTotalFrames();
        if (total <= 0) {
            setFrameInternal(0);
            return;
        }
        setFrameInternal(Math.max(0, Math.min(frame, total - 1)));
    }
    public GuiSprite withFrame(int frame) {
        setFrame(frame);
        return this;
    }
    @Override
    public int getCurrentFrame() {
        return currentFrame;
    }
    @Override
    public int getTotalFrames() {
        return source != null ? source.getFrameCount() : 0;
    }
    @Override
    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode != null ? mode : LoopMode.LOOP;
    }
    public GuiSprite withLoopMode(LoopMode mode) {
        setLoopMode(mode);
        return this;
    }
    @Override
    public LoopMode getLoopMode() {
        return loopMode;
    }
    public boolean isFlipX() {
        return flipX;
    }
    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }
    public GuiSprite withFlipX(boolean flipX) {
        setFlipX(flipX);
        return this;
    }
    public boolean isFlipY() {
        return flipY;
    }
    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }
    public GuiSprite withFlipY(boolean flipY) {
        setFlipY(flipY);
        return this;
    }
    public void setUVScrollSpeed(float speedU, float speedV) {
        this.uvScrollSpeedU = speedU;
        this.uvScrollSpeedV = speedV;
    }
    public GuiSprite withUVScrollSpeed(float speedU, float speedV) {
        setUVScrollSpeed(speedU, speedV);
        return this;
    }
    public void setOnFrameChanged(Consumer<Integer> onFrameChanged) {
        this.onFrameChanged = onFrameChanged;
    }
    public GuiSprite withOnFrameChanged(Consumer<Integer> onFrameChanged) {
        setOnFrameChanged(onFrameChanged);
        return this;
    }
    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }
    public GuiSprite withOnFinished(Runnable onFinished) {
        setOnFinished(onFinished);
        return this;
    }
    private void setFrameInternal(int frame) {
        if (this.currentFrame != frame) {
            this.currentFrame = frame;
            if (onFrameChanged != null) {
                onFrameChanged.accept(frame);
            }
        }
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (uvScrollSpeedU != 0.0f) {
            uvScrollU += uvScrollSpeedU * deltaTime;
        }
        if (uvScrollSpeedV != 0.0f) {
            uvScrollV += uvScrollSpeedV * deltaTime;
        }
        int totalFrames = getTotalFrames();
        if (!playing || totalFrames <= 1 || fps <= 0 || loopMode == LoopMode.MANUAL) {
            return;
        }
        elapsedTime += deltaTime;
        float frameDuration = 1.0f / fps;
        while (elapsedTime >= frameDuration) {
            elapsedTime -= frameDuration;
            advanceFrame(totalFrames);
            if (!playing) break;
        }
    }
    private void advanceFrame(int totalFrames) {
        switch (loopMode) {
            case LOOP:
                int nextLoop = (currentFrame + 1) % totalFrames;
                setFrameInternal(nextLoop);
                break;
            case ONCE:
                if (currentFrame < totalFrames - 1) {
                    setFrameInternal(currentFrame + 1);
                } else {
                    playing = false;
                    if (onFinished != null) {
                        onFinished.run();
                    }
                }
                break;
            case PING_PONG:
                int nextPingPong = currentFrame + pingPongDirection;
                if (nextPingPong >= totalFrames) {
                    pingPongDirection = -1;
                    nextPingPong = Math.max(0, totalFrames - 2);
                } else if (nextPingPong < 0) {
                    pingPongDirection = 1;
                    nextPingPong = Math.min(totalFrames - 1, 1);
                }
                setFrameInternal(nextPingPong);
                break;
            case MANUAL:
            default:
                break;
        }
    }
    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (source == null || getTotalFrames() <= 0) {
            return;
        }
        ResourceLocation tex = source.getTexture(currentFrame);
        if (tex == null) {
            return;
        }
        source.getUV(currentFrame, uvBuffer);
        float u = uvBuffer[0] + uvScrollU;
        float v = uvBuffer[1] + uvScrollV;
        float uWidth = uvBuffer[2];
        float vHeight = uvBuffer[3];
        int texW = source.getTextureWidth(currentFrame);
        int texH = source.getTextureHeight(currentFrame);
        if (flipX) {
            u = u + uWidth;
            uWidth = -uWidth;
        }
        if (flipY) {
            v = v + vHeight;
            vHeight = -vHeight;
        }
        gfx.drawTextureRegion(tex, absX, absY, width, height, u, v, uWidth, vHeight, texW, texH);
    }
}