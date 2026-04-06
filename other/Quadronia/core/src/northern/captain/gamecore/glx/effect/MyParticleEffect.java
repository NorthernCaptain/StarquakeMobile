package northern.captain.gamecore.glx.effect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.utils.Array;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 07.09.13
 * Time: 1:08
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public class MyParticleEffect extends ParticleEffect
{
    public MyParticleEffect()
    {
    }

    public MyParticleEffect(ParticleEffect effect)
    {
        super(effect);
    }

    /**
     * Disposes the texture for each sprite for each ParticleEmitter.
     */
    @Override
    public void dispose()
    {
        Array<ParticleEmitter> emitters = getEmitters();

        for (int i = 0, n = emitters.size; i < n; i++)
        {
            ParticleEmitter emitter = emitters.get(i);
            emitter.setSprites(new Array<>());
        }
    }
}
