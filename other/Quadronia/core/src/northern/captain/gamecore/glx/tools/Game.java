package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import northern.captain.gamecore.glx.NContext;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 31.03.13
 * Time: 21:29
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public abstract class Game implements ApplicationListener
{
    private Screen screen;

    @Override
    public void dispose () {
        if (screen != null) screen.hide();
    }

    @Override
    public void pause () {
        if (screen != null) screen.pause();
    }

    @Override
    public void resume () {
        if (screen != null) screen.resume();
    }

    @Override
    public void render () {
        if (screen != null) screen.render(NContext.current.getDeltaTime());
    }

    @Override
    public void resize (int width, int height) {
        if (screen != null) screen.resize(width, height);
    }

    /** Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
     * screen, if any.
     * @param screen may be {@code null}
     */
    public void setScreen (Screen screen) {
        if (this.screen != null) this.screen.hide();
        this.screen = screen;
        if (this.screen != null) {
            this.screen.show();
            this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    /** @return the currently active {@link Screen}. */
    public Screen getScreen () {
        return screen;
    }
}

