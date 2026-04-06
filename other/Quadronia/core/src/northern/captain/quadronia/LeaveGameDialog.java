package northern.captain.quadronia;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.core.Game;

public class LeaveGameDialog extends EndGameDialog
{
    Table otab = new Table();

	public LeaveGameDialog(Game game)
	{
        super(game);
        xmlName = "backgame_dlg";
	}

    @Override
    protected Table createSecond(XMLLayoutLoader xmlLoader, Window win, boolean hasAds)
    {
        final Table tbl = xmlLoader.newTable("endbut", textureAtlas);

        Label lbl = xmlLoader.newLabel("endbutlbl1");
        Image img = xmlLoader.newImage("endbutimg", textureAtlas);

        tbl.add(img).center();
        tbl.row();

        tbl.add(lbl).center();

        if(!hasAds)
        {
            tbl.setHeight(tbl.getHeight()*2);
        }

        win.add(otab).expandX().fillX().left().top().height(otab.getHeight()).width(otab.getWidth()).padTop(otab.getY());

        otab.add(tbl).center().top().colspan(2).width(tbl.getWidth()).height(tbl.getHeight())
            .expandX().padLeft(tbl.getX());
        win.row();

        return tbl;
    }

    @Override
    protected Table createFirst(XMLLayoutLoader xmlLoader, Window win, boolean hasAds)
    {
        if(!hasAds) return null;

        otab = xmlLoader.newTable("otab", textureAtlas);

        Table tbl2 = xmlLoader.newTable("adbut", textureAtlas);
        Label lbl2 = xmlLoader.newLabel("adbutlbl1");
        Image img = xmlLoader.newImage("adbutimg", textureAtlas);

        tbl2.add(img).center();
        tbl2.row();
        tbl2.add(lbl2).center();
        otab.add(tbl2).center().top().colspan(2).width(tbl2.getWidth()).height(tbl2.getHeight()).padRight(tbl2.getX())
                .expandX();

        return tbl2;
    }

    @Override
    protected boolean isNeedFirst()
    {
        return true;
    }

    @Override
    protected void backClicked()
    {
        clickedButton = 0;
        dismiss();
    }

    @Override
    protected String getBonusValue()
    {
        return String.valueOf(game.getContext().getFragments());
    }

    @Override
    protected String getPerkValue()
    {
        return "0";
    }
}
