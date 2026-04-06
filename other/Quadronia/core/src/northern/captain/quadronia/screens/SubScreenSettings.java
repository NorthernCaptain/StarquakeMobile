package northern.captain.quadronia.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.quadronia.IGameOptionsMenu;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.widget.BoolSwitch;
import northern.captain.quadronia.gfx.widget.OptionChoiceWidget;
import northern.captain.quadronia.gfx.widget.SetupRowTitle;
import northern.captain.quadronia.gfx.widget.TripleButton;

public class SubScreenSettings extends SubScreenBase
{
    BoolSwitch soundSwitch;
    BoolSwitch musicSwitch;
    BoolSwitch vibrationSwitch;
    BoolSwitch tutorialSwitch;
    OptionChoiceWidget themeOptions;

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, "setup_subscreen");
        float origHeight = getHeight();
        float height = NContext.current.screenHeight - getY();
        setHeight(height);

        float factorY = height / origHeight;

        {
            Image img = loader.newImage("setup_blot", gContext.atlas);
            img.getColor().a = 0.5f;
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("setup_icon", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("setup_topq1", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("setup_topq2", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("setup_topq3", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }
        {
            Image img = loader.newImage("setup_topq4", gContext.atlas);
            setActorY(img, factorY, height);
            this.addActor(img);
        }

        {
            Label lbl = loader.newLabel("setup_toptitle");
            setActorY(lbl, factorY, height);
            addActor(lbl);
        }

        //settings row
        IGameOptionsMenu options = NCore.instance().getGameOptionsMenu();
        {
            SetupRowTitle rowTitle = new SetupRowTitle("setup_set1");
            rowTitle.initGraphics(loader, gContext);
            setActorY(rowTitle, factorY, height);
            this.addActor(rowTitle);

            soundSwitch = new BoolSwitch("setup_set1_sw_");
            soundSwitch.initGraphics(loader, gContext);
            setActorY(soundSwitch, factorY, height);
            this.addActor(soundSwitch);
            soundSwitch.setOn(options.isSoundOn());
            soundSwitch.setOnValueChange((boolean val)->options.flipSoundOption());
        }
        {
            SetupRowTitle rowTitle = new SetupRowTitle("setup_set2");
            rowTitle.initGraphics(loader, gContext);
            setActorY(rowTitle, factorY, height);
            this.addActor(rowTitle);
            musicSwitch = new BoolSwitch("setup_set2_sw_");
            musicSwitch.initGraphics(loader, gContext);
            setActorY(musicSwitch, factorY, height);
            this.addActor(musicSwitch);
            musicSwitch.setOn(options.isMusicOn());
            musicSwitch.setOnValueChange((boolean val)->options.flipMusinOption());
        }
        {
            SetupRowTitle rowTitle = new SetupRowTitle("setup_set3");
            rowTitle.initGraphics(loader, gContext);
            setActorY(rowTitle, factorY, height);
            this.addActor(rowTitle);
            vibrationSwitch = new BoolSwitch("setup_set3_sw_");
            vibrationSwitch.initGraphics(loader, gContext);
            setActorY(vibrationSwitch, factorY, height);
            this.addActor(vibrationSwitch);
            vibrationSwitch.setOn(options.isVibrationOn());
            vibrationSwitch.setOnValueChange((boolean val)->options.flipVibroOption());
        }
        {
            SetupRowTitle rowTitle = new SetupRowTitle("setup_set5");
            rowTitle.initGraphics(loader, gContext);
            setActorY(rowTitle, factorY, height);
            this.addActor(rowTitle);
            TripleButton button = new TripleButton("setup_set5_b_");
            button.initGraphics(loader, gContext);
            setActorY(button, factorY, height);
            this.addActor(button);
            button.setOnClick(() -> options.openFeedback());
        }
        {
            SetupRowTitle rowTitle = new SetupRowTitle("setup_set6");
            rowTitle.initGraphics(loader, gContext);
            setActorY(rowTitle, factorY, height);
            this.addActor(rowTitle);

            themeOptions = new OptionChoiceWidget("setup_set6_o_", 2);
            themeOptions.initGraphics(loader, gContext);
            setActorY(themeOptions, factorY, height);
            this.addActor(themeOptions);
            themeOptions.setCurrent(options.getThemeIndex());
            themeOptions.setCallback((int newIdx)->options.setThemeIndex(newIdx));
        }
    }
}
