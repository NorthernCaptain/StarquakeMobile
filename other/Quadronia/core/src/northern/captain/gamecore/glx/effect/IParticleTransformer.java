package northern.captain.gamecore.glx.effect;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;

/**
 * Copyright 2013 by Northern Captain Software
 * User: leo
 * Date: 06.09.13
 * Time: 22:45
 * This code is a private property of its owner Northern Captain.
 * Any copying or redistribution of this source code is strictly prohibited.
 */
public interface IParticleTransformer
{
    /**
     * Starts the transformer if needed
     */
    void start();

    /**
     * Update (extend) transformer for a given number of seconds
     * @param delta
     */
    void update(float delta);

    /**
     * Transform the effect according to the current transformer state
     * @param effect
     * @param actor - could be null
     */
    void transform(ParticleEffect effect, ParticleEffectActor actor);

    /**
     * Gets total duration of this transformation in milliseconds
     * @return
     */
    int getDurationMillis();

    void clear();
}
