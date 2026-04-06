package northern.captain.quadronia.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface IPagable
{
    void setPageActive(boolean isActive);
    void setPageCurrent(boolean isCurrent);
    void setFromCurrentIdx(int deltaIdx);
    void setPagePosition(float x, float y);
    Actor getPageActor();
}
