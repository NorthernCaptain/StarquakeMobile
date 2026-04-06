package northern.captain.quadronia.gfx;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import northern.captain.quadronia.game.BoundingBox;
import northern.captain.quadronia.game.IGameContext;
import northern.captain.quadronia.game.profile.UserBase;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.gamecore.glx.INameDialog;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.screens.IScreenWorkflow;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;

/**
 * Created by leo on 24.05.15.
 */
public class MainUserInfoWidget extends Actor implements IGraphicsInit, ICursorEventListener
{
    Label userNameLbl;
    Label coinLbl;

    IGameContext context;
    BoundingBox boundingBox;
    Sprite coin;
    BoundingBox coinBox;
    IScreenWorkflow workflow;

    private float deltaScreenY = 0;

    public MainUserInfoWidget(IGameContext context, IScreenWorkflow workflow)
    {
        this.context = context;
        this.workflow = workflow;
    }

    @Override
    public boolean doTouchDown(int fx, int fy)
    {
        return boundingBox.isIn(fx, fy) || coinBox.isIn(fx, fy);
    }

    @Override
    public boolean doDrag(int fx, int fy)
    {
        return false;
    }

    @Override
    public boolean doTouchUp(int fx, int fy)
    {
        if(boundingBox.isIn(fx, fy))
        {
            INameDialog dialog = NCore.instance().newNameDialog();
            dialog.show(value->{
                setNewName(value);
                return true;
            });
            return true;
        }

        if(coinBox.isIn(fx, fy))
        {
            workflow.prepare(IScreenWorkflow.WF_CHANGE);
            workflow.doAction(IScreenWorkflow.WF_CHANGE);
            return true;
        }
        return false;
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        UserBase user = UserManager.instance.getCurrentUser();
        String name = user.getName();
        userNameLbl = loader.newLabel("unamelbl");
        userNameLbl.setY(userNameLbl.getY() - deltaScreenY);
        userNameLbl.setText(name);
        boundingBox = new BoundingBox((int)userNameLbl.getX(), (int)userNameLbl.getY(),
                (int)(userNameLbl.getX() + userNameLbl.getWidth()),
                (int)(userNameLbl.getY() + userNameLbl.getHeight()));
        coinLbl = loader.newLabel("coinlbl");
        coinLbl.setY(coinLbl.getY() - deltaScreenY);
        coin = loader.newSprite("coin", gContext.atlas);
        coin.setY(coin.getY() -  deltaScreenY);
        Table tbl = loader.newTable("coinTbl", null);
        tbl.setY(tbl.getY() - deltaScreenY);
        coinBox = new BoundingBox((int)tbl.getX(), (int)tbl.getY(),
                (int)(tbl.getX()+tbl.getWidth()), (int)(tbl.getY() + tbl.getHeight()));

        initValues();
    }

    public void initValues()
    {
        UserBase user = UserManager.instance.getCurrentUser();
        coinLbl.setText(Integer.toString(user.getCoins()));
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        coin.draw(batch, parentAlpha);
        userNameLbl.draw(batch, parentAlpha);
        coinLbl.draw(batch, parentAlpha);
    }

    private void setNewName(String newName)
    {
        UserBase user = UserManager.instance.getCurrentUser();
        user.setName(newName);
        userNameLbl.setText(newName);
    }

    public void setDeltaScreenY(float deltaY)
    {
        this.deltaScreenY = deltaY;
    }
}
