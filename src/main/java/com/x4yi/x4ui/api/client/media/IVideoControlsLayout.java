package com.x4yi.x4ui.api.client.media;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.GuiVideo;
public interface IVideoControlsLayout {
    void attach(GuiVideo video);
    void detach();
    void update(float deltaTime);
    void render(IGraphics gfx, int mouseX, int mouseY, float partialTicks);
    boolean onMouseClick(int mouseX, int mouseY, int mouseButton);
    boolean onMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick);
    boolean onMouseRelease(int mouseX, int mouseY, int state);
}