package northern.captain.starquake.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Something that can be updated and drawn at a position. */
public interface Renderable {
    void update(float delta);
    void render(SpriteBatch batch, float x, float y);
}
