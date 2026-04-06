package northern.captain.gamecore.glx.effect;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import northern.captain.gamecore.glx.NContext;

import java.io.File;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 06.09.13
 * Time: 22:33
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class ParticleEffectActor extends Actor implements Runnable
{
    Runnable runOnComplete;

    ParticleEffect effect;

    IParticleTransformer transformer;

    float lastDelta = 0;

    private int effectX, effectY;

    public ParticleEffectActor(ParticleEffect effect)
    {
        this.effect = effect;
        this.setTouchable(Touchable.disabled);
    }

    public IParticleTransformer getTransformer()
    {
        return transformer;
    }

    public void setTransformer(IParticleTransformer transformer)
    {
        this.transformer = transformer;
    }

    public void setRunOnComplete(Runnable runOnComplete)
    {
        this.runOnComplete = runOnComplete;
    }

    @Override
    public void draw(Batch batch, float parentAlpha)
    {
        effect.draw(batch, lastDelta); //define behavior when stage calls Actor.draw()
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if(effect.isComplete())
        {
            if(runOnComplete != null)
            {
                runOnComplete.run();
                runOnComplete = null;
            }
            hide();
            return;
        }

        if(transformer == null)
        {
            effect.setPosition(effectX, effectY); //set to whatever x/y you prefer
        } else
        {
            transformer.update(delta);
            transformer.transform(effect, this);
        }
        lastDelta = delta;
    }

    public ParticleEffect getEffect()
    {
        return effect;
    }

    public void loadSprites(TextureAtlas atlas)
    {
        Array<ParticleEmitter> emitters = effect.getEmitters();

        for (int i = 0, n = emitters.size; i < n; i++)
        {
            ParticleEmitter emitter = emitters.get(i);
            Array<String> imagePath = emitter.getImagePaths();
            if (imagePath == null || imagePath.isEmpty())
            {
                continue;
            }

            String imageName = new File(imagePath.get(0).replace('\\', '/')).getName();
            int lastDotIndex = imageName.lastIndexOf('.');

            if (lastDotIndex != -1)
            {
                imageName = imageName.substring(0, lastDotIndex);
            }

            Array<Sprite> sprites = new Array<>();
            sprites.add(NContext.current.newSprite(atlas, imageName));
            emitter.setSprites(sprites);
        }
    }

    public void show()
    {
        if(this.hasParent())
            return;

        if(transformer != null)
        {
            effect.setDuration(transformer.getDurationMillis());
        }
        effect.reset();
        if(NContext.current.currentStage != null)
        {
            NContext.current.currentStage.addActor(this);
            NContext.current.addRefresh();
        }
    }

    public void hide()
    {
        if(this.hasParent())
        {
            this.remove();
            NContext.current.subRefresh();
            setParent(null);
        }
        if(transformer != null) transformer.clear();
    }

    /**
     * Sets the position of the effect center emitter
     * @param x
     * @param y
     */
    public void setEffectPosition(int x, int y)
    {
        effectX = x;
        effectY = y;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        show();
    }
}
