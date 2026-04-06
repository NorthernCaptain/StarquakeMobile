package northern.captain.quadronia.gfx.widget;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import northern.captain.gamecore.glx.INameDialog;
import northern.captain.gamecore.glx.NContext;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.res.SharedRes;
import northern.captain.gamecore.glx.tools.loaders.XMLLayoutLoader;
import northern.captain.gamecore.gplus.GoogleGamesFactory;
import northern.captain.gamecore.gplus.IGoogleGamesProcessor;
import northern.captain.quadronia.game.core.Game;
import northern.captain.quadronia.game.profile.UserBase;
import northern.captain.quadronia.game.profile.UserManager;
import northern.captain.quadronia.gfx.GraphicsInitContext;
import northern.captain.quadronia.gfx.IGraphicsInit;
import northern.captain.tools.sharing.ShareManager;

public class MainMenuUserWidget extends Group implements IGraphicsInit
{
    private TripleButton gplusBut;
    private TripleButton gLeaderBut;
    private TripleButton gAchivBut;
    private TripleButton shareBut;
    private TripleButton coinsBut;
    private TripleButton userNameBut;
    private Label gameTypeLbl;
    private Label subtitleLbl;
    private Label levelLbl;
    private Label fragmentsLbl;
    private Label scoreLbl;
    private DecimalFormat formatter;
    private int gameMode = Game.TYPE_ARCADE;

    public MainMenuUserWidget()
    {
        gplusBut = new TripleButton("mnu_gplus");
        gLeaderBut = new TripleButton("mnu_gleader");
        gAchivBut = new TripleButton("mnu_gachiv");
        shareBut = new TripleButton("mnu_share");
        coinsBut = new TripleButton("mnu_coins");
        userNameBut = new TripleButton("mnu_name");

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator('.');
        formatter = new DecimalFormat("#,###,###", symbols);

        gLeaderBut.setOnClick(() ->
            NContext.current.postDelayed(()->GoogleGamesFactory.instance().getProcessor().openLeaderboard(), 500));

        gAchivBut.setOnClick(() ->
                NContext.current.postDelayed(()->GoogleGamesFactory.instance().getProcessor().openAchievements(), 500));

        shareBut.setOnClick(() ->
                NContext.current.postDelayed(()-> ShareManager.instance().doShareMyProgress(gameMode), 200));

        userNameBut.setOnClick(()-> {
            INameDialog dialog = NCore.instance().newNameDialog();
            if(dialog != null) {
                dialog.show(value -> {
                    setNewName(value);
                    return true;
                });
            }
        });

        gplusBut.setOnClick(() -> {
            IGoogleGamesProcessor processor = GoogleGamesFactory.instance().getProcessor();

            if (!processor.isSignedIn())
            {
                processor.doSignIn(null);
            } else
            {
                processor.doSignOut();
            }
        });
    }

    @Override
    public void initGraphics(XMLLayoutLoader loader, GraphicsInitContext gContext)
    {
        setTransform(false);
        loader.applyTo(this, "mnu_user_panel");

//        gplusBut.initGraphics(loader, gContext);
//        addActor(gplusBut);

        gLeaderBut.initGraphics(loader, gContext);
        addActor(gLeaderBut);

        gAchivBut.initGraphics(loader, gContext);
        addActor(gAchivBut);

        shareBut.initGraphics(loader, gContext);
        addActor(shareBut);

        coinsBut.initGraphics(loader, gContext);
        addActor(coinsBut);

        userNameBut.initGraphics(loader, gContext);
        addActor(userNameBut);

        gameTypeLbl = loader.newLabel("mnu_gametypetext");
        addActor(gameTypeLbl);

        subtitleLbl = loader.newLabel("mnu_subtitletext");
        addActor(subtitleLbl);

        levelLbl = loader.newLabel("mnu_leveltext");
        addActor(levelLbl);

        fragmentsLbl = loader.newLabel("mnu_fragstext");
        addActor(fragmentsLbl);

        scoreLbl = loader.newLabel("mnu_scoretext");
        addActor(scoreLbl);

        initValues();
    }

    private String formatNum(int value)
    {
        return formatter.format(value);
    }

    public void initValues()
    {
        if(gameTypeLbl == null) return;

        int mode = gameMode;
        UserBase user = UserManager.instance.getCurrentUser();
        coinsBut.setText(Integer.toString(user.getCoins()));
        userNameBut.setText(user.getName());
        scoreLbl.setText(formatNum(Game.defaultGameContext.getMaxScore(mode)) + " |");
        fragmentsLbl.setText(formatNum(Game.defaultGameContext.getMaxFragments(mode)) + " |");
        levelLbl.setText(formatNum(Game.defaultGameContext.getMaxLevel(mode)));
        gameTypeLbl.setText(mode == Game.TYPE_ARCADE ? "CLASSIC" : "SPRINT");
    }

    public void setGameMode(int gameMode)
    {
        this.gameMode = gameMode;
        initValues();
    }

    private void setNewName(String newName)
    {
        UserBase user = UserManager.instance.getCurrentUser();
        user.setName(newName);
        userNameBut.setText(newName);
    }

    public void setOnShop(Runnable callback)
    {
        coinsBut.setOnClick(callback);
    }
}
