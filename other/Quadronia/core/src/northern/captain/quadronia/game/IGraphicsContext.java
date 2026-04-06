package northern.captain.quadronia.game;

import com.badlogic.gdx.math.Vector2;

import northern.captain.quadronia.gfx.IWidgetBoard;

/**
 * Created by leo on 05.04.15.
 */
public interface IGraphicsContext
{
    float getScoreCenterX();

    float getScoreCenterY();

    void setScoreCenter(float x, float y);

    float getFieldCenterX();
    float getFieldCenterY();

    Vector2 convertFieldCoordToGlobal(float x, float y);

    void animateUpdateScoreDisplay(float fromX, float fromY, int newScore);
    void animateUpdateBackstep(float fromX, float fromY);
    void animateUpdateSwap(float fromX, float fromY);

    void explosionEffect(float x, float y, float delay);

    void removeFromCycle(IGraphicObject drawObject);

    IGraphicObject findGraphicObject(Object logicObject);

    void onFieldRebuild();

    IWidgetBoard getWidgetBoard();
}
