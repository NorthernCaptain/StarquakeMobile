package northern.captain.quadronia;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import northern.captain.gamecore.glx.tools.ButtonImage;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.game.core.Game;

public class EndGameDialog extends BaseDialog
{
    protected Game game;

	public EndGameDialog(Game game)
	{
        this.game = game;
        xmlName = "endgame_dlg";
        clickedButton = 2;
	}


    protected void secondClicked()
    {
        clickedButton = 2;
        dismiss();
    }

    @Override
    protected void firstClicked()
    {
        clickedButton = 1;
        dismiss();
    }



    @Override
    protected Table createSecond(XMLLayoutLoader xmlLoader, Window win, boolean hasAds)
    {
        final Table tbl = xmlLoader.newTable("endbut", textureAtlas);

        Label lbl = xmlLoader.newLabel("endbutlbl1");

        tbl.add(lbl).center();

        if(!hasAds)
        {
            tbl.setHeight(tbl.getHeight()*2);
        }

        win.add(tbl).center().top().colspan(2).width(tbl.getWidth()).height(tbl.getHeight())
            .padTop(tbl.getY()).expandX().padBottom(tbl.getY());
        win.row();

        return tbl;
    }

    @Override
    protected Table createFirst(XMLLayoutLoader xmlLoader, Window win, boolean hasAds)
    {
        if(!hasAds) return null;

        Table tbl = xmlLoader.newTable("adbut", textureAtlas);
        Label lbl = xmlLoader.newLabel("adbutlbl1");

        tbl.add(lbl).center();
        tbl.row();
        Label lbl2 = xmlLoader.newLabel("adbutlbl2");
        tbl.add(lbl2).center().top().padTop(lbl2.getY());

        win.add(tbl).center().top().colspan(2).width(tbl.getWidth()).height(tbl.getHeight())
            .padTop(tbl.getY()).expandX();
        win.row();

        return tbl;
    }

    @Override
    protected void createBody(XMLLayoutLoader xmlLoader, Window win)
    {
        {
            Table tbl = xmlLoader.newTable("backfill1", textureAtlas);
            
            Label scorelbl = xmlLoader.newLabel("scorelbl");
            tbl.add(scorelbl).colspan(2).top().left().padTop(scorelbl.getY()).padLeft(scorelbl.getX()).expandX().fillX();
            Label scoreval = xmlLoader.newLabel("scoreval");
            scoreval.setText(String.valueOf(game.getContext().getScore()));
            tbl.add(scoreval).colspan(2).top().right().padTop(scoreval.getY()).padRight(scoreval.getX()).expandX().fillX();
            tbl.row();
            
            Label bonuslbl = xmlLoader.newLabel("bonuslbl");
            tbl.add(bonuslbl).colspan(2).top().left().padTop(bonuslbl.getY()).padLeft(bonuslbl.getX()).expandX().fillX();
            Label bonusval = xmlLoader.newLabel("bonusval");
            bonusval.setText(getBonusValue());
            tbl.add(bonusval).colspan(2).top().right().padTop(bonusval.getY()).padRight(bonusval.getX()).expandX().fillX();
            tbl.row();

            Image sep = xmlLoader.newImage("hline2", textureAtlas);
            tbl.add(sep).expandX().fillX().colspan(2).center().padTop(sep.getY()).padLeft(sep.getX());
            Image sep2 = xmlLoader.newImage("hline2", textureAtlas);
            tbl.add(sep2).expandX().fillX().colspan(2).center().padTop(sep.getY()).padRight(sep.getX());
            tbl.row();

            Label totallbl = xmlLoader.newLabel("totallbl");
            tbl.add(totallbl).colspan(2).top().left().padTop(totallbl.getY()).padLeft(totallbl.getX()).expandX().fillX();
            Label totalval = xmlLoader.newLabel("totalval");
            totalval.setText(String.valueOf(game.getContext().getScore()));
            tbl.add(totalval).colspan(2).top().right().padTop(totalval.getY()).padRight(totalval.getX()).expandX().fillX();

            win.add(tbl).expandX().fillX().left().top().height(tbl.getHeight()).width(tbl.getWidth());
            win.row();
        }

        {
            Table tbl = xmlLoader.newTable("backfill2", textureAtlas);
            Label lbl = xmlLoader.newLabel(titleResName);
            tbl.add(lbl).center();
            win.add(tbl).expandX().fillX().left().top().padTop(tbl.getY()).height(tbl.getHeight());
            win.row();
        }

//        {
//            Table otab = new Table();
//            Label lbl = xmlLoader.newLabel(titleResName);
//            otab.add(lbl).center().left().padLeft(lbl.getX()).padTop(lbl.getY()).padBottom(lbl.getY());
//            ButtonImage img = xmlLoader.newImageButton("close", textureAtlas);
//            Table dummytab = new Table();
//            otab.add(dummytab).expandX().center().right().width(img.getWidth()).height(img.getHeight())
//                .padRight(img.getX()).padLeft(img.getX()).padTop(img.getY());
//            win.add(otab).expandX().fillX().left().top().height(img.getHeight()).colspan(2);
//            win.row();
//
//        }
//        {
//            Image img = xmlLoader.newImage("hline", textureAtlas);
//            win.add(img).expandX().fillX().colspan(2).center().height(img.getHeight()).bottom()
//                .padRight(img.getX()).padLeft(img.getX()).padBottom(img.getY()).width(img.getWidth());
//            win.row();
//        }
//
//        {
//            Label lbl = xmlLoader.newLabel("scorelbl");
//            win.add(lbl).left().top().width(lbl.getWidth()).height(lbl.getHeight())
//                .padLeft(lbl.getX()).padTop(lbl.getY());
//
//            lbl = xmlLoader.newLabel("scoreval");
//            lbl.setText(String.valueOf(game.getContext().getScore()));
//            win.add(lbl).right().top().expandX().width(lbl.getWidth()).height(lbl.getHeight())
//                .padRight(lbl.getX()).padTop(lbl.getY());
//            win.row();
//        }
//        {
//            Label lbl = xmlLoader.newLabel("bonuslbl");
//            win.add(lbl).left().top().width(lbl.getWidth()).height(lbl.getHeight())
//                .padLeft(lbl.getX()).padTop(lbl.getY());
//
//            lbl = xmlLoader.newLabel("bonusval");
//            lbl.setText(getBonusValue());
//            win.add(lbl).right().top().expandX().width(lbl.getWidth()).height(lbl.getHeight())
//                .padRight(lbl.getX()).padTop(lbl.getY());
//            win.row();
//        }
//        {
//            Image img = xmlLoader.newImage("hline2", textureAtlas);
//            win.add(img).expandX().fillX().colspan(2).center().height(img.getHeight())
//                .padRight(img.getX()).padLeft(img.getX()).padTop(img.getY()).width(img.getWidth());
//            win.row();
//        }
//        {
//            Label lbl = xmlLoader.newLabel("totallbl");
//            win.add(lbl).left().top().width(lbl.getWidth()).height(lbl.getHeight())
//                .padLeft(lbl.getX()).padTop(lbl.getY());
//
//            lbl = xmlLoader.newLabel("totalval");
//            lbl.setText(String.valueOf(game.getContext().getScore()));
//            win.add(lbl).right().top().expandX().width(lbl.getWidth()).height(lbl.getHeight())
//                .padRight(lbl.getX()).padTop(lbl.getY()).padBottom(lbl.getY());
//            win.row();
//        }
    }

    @Override
    protected boolean isNeedFirst()
    {
        return false;
//        return game.getBehaviour().canContinueOnFail() && AdsProcessor.instance.isVideoAvailable();
    }

    @Override
    protected void backClicked()
    {
        secondClicked();
    }

    protected String getBonusValue()
    {
        return "";
    }

    protected String getPerkValue()
    {
        return "";
    }
}
