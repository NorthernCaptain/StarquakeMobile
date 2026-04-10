package northern.captain.starquake.hud;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import northern.captain.starquake.input.InputManager;

/**
 * Full-screen overlay that takes over input and rendering.
 * Only one overlay can be active at a time. While active,
 * normal gameplay is paused and BLOB is frozen.
 */
public interface Overlay {
    void update(float delta, InputManager input);
    void render(SpriteBatch batch);
    boolean isDone();
}
