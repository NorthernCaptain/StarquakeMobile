package northern.captain.quadronia.gfx;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.quadronia.game.perks.ElePerk;
import northern.captain.quadronia.game.perks.IFieldPerk;

/**
 * Created by leo on 05.04.15.
 */
public interface IWidgetBoard
{
    Label getScoreLbl();
    void animatedUpdateScoreDisplay(float fromX, float fromY, int newScore);
    void updateScoreDisplay(int newTotal);
    void explosionEffect(float x, float y, float delay);
    void animatedUpdateBackstepDisplay(float fromX, float fromY);
    void animatedUpdateSwapDisplay(float fromX, float fromY);
    IDraw generatePerkDraw(IFieldPerk perk);
    void addDrawableToScene(IDraw draw, boolean init);
    void removeDrawable(IDraw draw);
}
