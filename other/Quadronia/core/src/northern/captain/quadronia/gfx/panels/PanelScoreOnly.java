package northern.captain.quadronia.gfx.panels;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.gamecore.glx.tools.Animations;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.events.EGameLevelUp;
import northern.captain.quadronia.game.events.EScoreChange;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.ISimpleDrawable;
import northern.captain.tools.IAnimationFactory;

/**
 * Created by leo on 19.04.15.
 */
public class PanelScoreOnly implements ISimpleDrawable
{
    private Game game;
    public Label scoreLabel;
    public Label levelLabel;
    public Label levelUpLabel;
    public Label fragmentsLabel;

    private Label levelCap;
    private Label leftCap;
    private Label fragCap;
    private Label scoreCap;

    private IAnimationFactory scoreRotatorFactory;

    public PanelScoreOnly(Game game)
    {
        this.game = game;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        levelCap.draw(batch, parentAlpha);
        levelCap.draw(batch, parentAlpha);
        leftCap.draw(batch, parentAlpha);
        leftCap.draw(batch, parentAlpha);
        fragCap.draw(batch, parentAlpha);
        fragCap.draw(batch, parentAlpha);
        scoreCap.draw(batch, parentAlpha);
        scoreCap.draw(batch, parentAlpha);

        levelLabel.draw(batch, parentAlpha);
        levelUpLabel.draw(batch, parentAlpha);
        scoreLabel.draw(batch, parentAlpha);
        fragmentsLabel.draw(batch, parentAlpha);
    }

    @Override
    public void drawFBO(Batch fboBatch, float parentAlpha)
    {
        levelCap.draw(fboBatch, parentAlpha);
        levelCap.draw(fboBatch, parentAlpha);
        leftCap.draw(fboBatch, parentAlpha);
        leftCap.draw(fboBatch, parentAlpha);
        fragCap.draw(fboBatch, parentAlpha);
        fragCap.draw(fboBatch, parentAlpha);
        scoreCap.draw(fboBatch, parentAlpha);
        scoreCap.draw(fboBatch, parentAlpha);
    }

    @Override
    public void initGraphics(XMLLayoutLoader layXml, GraphicsInitContext gContext)
    {
        levelLabel = layXml.newLabel("user_lvl_lbl");
        levelLabel.setText("01");
        levelUpLabel = layXml.newLabel("user_up_lbl");
        levelUpLabel.setText("--");
        scoreLabel = layXml.newLabel("user_score_lbl");
        scoreLabel.setText("0");
        fragmentsLabel = layXml.newLabel("user_frag_lbl");
        fragmentsLabel.setText("0");

        levelCap = layXml.newLabel("score_lbl1");
        leftCap  = layXml.newLabel("score_lbl2");
        fragCap  = layXml.newLabel("score_lbl4");
        scoreCap  = layXml.newLabel("score_lbl6");

        if(gContext.deltaY != 0 || gContext.deltaX != 0)
        {
            levelCap.setPosition(levelCap.getX() - gContext.deltaX, levelCap.getY() + gContext.deltaY);
            leftCap.setPosition(leftCap.getX() - gContext.deltaX, leftCap.getY() + gContext.deltaY);
            fragCap.setPosition(fragCap.getX() - gContext.deltaX, fragCap.getY() + gContext.deltaY);
            scoreCap.setPosition(scoreCap.getX() - gContext.deltaX, scoreCap.getY() + gContext.deltaY);
            levelLabel.setPosition(levelLabel.getX() - gContext.deltaX, levelLabel.getY() + gContext.deltaY);
            levelUpLabel.setPosition(levelUpLabel.getX() - gContext.deltaX, levelUpLabel.getY() + gContext.deltaY);
            scoreLabel.setPosition(scoreLabel.getX() - gContext.deltaX, scoreLabel.getY() + gContext.deltaY);
            fragmentsLabel.setPosition(fragmentsLabel.getX() - gContext.deltaX, fragmentsLabel.getY() + gContext.deltaY);
        }

        scoreRotatorFactory = new Animations.DigitsRotator(1);
    }

    public void act(float delta)
    {
        scoreLabel.act(delta);
    }

    private int currentScore = 0;
    public void onScoreChange(EScoreChange event)
    {
        int newTotal = event.score.getTotalScore();
        fragmentsLabel.setText(Integer.toString(game.getContext().getFragments()));
        if(newTotal == currentScore)
        {
            scoreLabel.setText(Integer.toString(newTotal));
        } else
        {
            scoreLabel.setText(Integer.toString(currentScore));
            scoreLabel.addAction(scoreRotatorFactory.create(newTotal));
            currentScore = newTotal;
        }
        onLevelChanged(null);
    }

    public void onLevelChanged(EGameLevelUp event)
    {
        IGameContext context = game.getContext();
        levelLabel.setText(String.format("%02d", context.getLevel()));
        levelUpLabel.setText(game.getBehaviour().levelUpsAllowed() ? String.format("%02d", context.getTillLevelUp()) : "--");
    }
}
