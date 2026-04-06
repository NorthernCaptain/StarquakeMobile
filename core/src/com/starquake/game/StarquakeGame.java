package com.starquake.game;

import com.badlogic.gdx.Game;
import com.starquake.game.screen.LoadingScreen;

public class StarquakeGame extends Game {
    public Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        setScreen(new LoadingScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        assets.dispose();
    }
}
