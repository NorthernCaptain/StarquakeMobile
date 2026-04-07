package northern.captain.starquake.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * On-screen touch controls: D-pad + action buttons.
 *
 * Layout is swappable (left-handed vs right-handed). Default: D-pad on right.
 * Renders semi-transparent shapes via a ScreenViewport covering the full
 * physical screen. Reports touch state to {@link InputManager} via events.
 */
public class TouchControls {
    private static final float BUTTON_SIZE = 80;
    private static final float PADDING = 16;
    private static final Color BTN_COLOR = new Color(1, 1, 1, 0.2f);
    private static final Color BTN_PRESSED_COLOR = new Color(1, 1, 1, 0.5f);

    private final InputManager inputManager;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final ScreenViewport viewport = new ScreenViewport();
    private boolean leftHanded;

    // Touch zones in screen coordinates
    private final Rectangle zoneUp    = new Rectangle();
    private final Rectangle zoneDown  = new Rectangle();
    private final Rectangle zoneLeft  = new Rectangle();
    private final Rectangle zoneRight = new Rectangle();
    private final Rectangle zoneA     = new Rectangle();
    private final Rectangle zoneB     = new Rectangle();

    public TouchControls(InputManager inputManager, boolean leftHanded) {
        this.inputManager = inputManager;
        this.leftHanded = leftHanded;
        layoutZones(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void setLeftHanded(boolean leftHanded) {
        this.leftHanded = leftHanded;
        layoutZones(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int screenWidth, int screenHeight) {
        viewport.update(screenWidth, screenHeight, true);
        layoutZones(screenWidth, screenHeight);
    }

    private void layoutZones(float w, float h) {
        float density = Math.max(1f, Gdx.graphics.getDensity());
        float bs = Math.min(BUTTON_SIZE * density, h / 5f);
        float pad = PADDING;

        // D-pad: 3-button cross arrangement
        float dpadCenterX, actionCenterX;
        if (leftHanded) {
            dpadCenterX = pad + bs * 1.5f;
            actionCenterX = w - pad - bs * 0.5f;
        } else {
            dpadCenterX = w - pad - bs * 1.5f;
            actionCenterX = pad + bs * 0.5f;
        }
        float dpadCenterY = h * 0.35f;

        zoneUp.set(dpadCenterX - bs / 2, dpadCenterY + bs * 0.6f, bs, bs);
        zoneDown.set(dpadCenterX - bs / 2, dpadCenterY - bs * 1.6f, bs, bs);
        zoneLeft.set(dpadCenterX - bs * 1.6f, dpadCenterY - bs / 2, bs, bs);
        zoneRight.set(dpadCenterX + bs * 0.6f, dpadCenterY - bs / 2, bs, bs);

        // Action buttons: stacked vertically
        float actionY = h * 0.35f;
        zoneA.set(actionCenterX - bs / 2, actionY + bs * 0.1f, bs, bs);
        zoneB.set(actionCenterX - bs / 2, actionY - bs * 1.2f, bs, bs);
    }

    /** Poll touch state and push to InputManager. Call once per frame before game logic. */
    public void poll() {
        boolean up = false, down = false, left = false, right = false;
        boolean a = false, b = false;

        for (int pointer = 0; pointer < 5; pointer++) {
            if (!Gdx.input.isTouched(pointer)) continue;
            // Screen coordinates: origin top-left, Y down
            float tx = Gdx.input.getX(pointer);
            float ty = Gdx.graphics.getHeight() - Gdx.input.getY(pointer); // flip to bottom-left origin

            if (zoneUp.contains(tx, ty))    up = true;
            if (zoneDown.contains(tx, ty))  down = true;
            if (zoneLeft.contains(tx, ty))  left = true;
            if (zoneRight.contains(tx, ty)) right = true;
            if (zoneA.contains(tx, ty))     a = true;
            if (zoneB.contains(tx, ty))     b = true;
        }

        inputManager.setTouchState(InputManager.Action.UP, up);
        inputManager.setTouchState(InputManager.Action.DOWN, down);
        inputManager.setTouchState(InputManager.Action.LEFT, left);
        inputManager.setTouchState(InputManager.Action.RIGHT, right);
        inputManager.setTouchState(InputManager.Action.ACTION_A, a);
        inputManager.setTouchState(InputManager.Action.ACTION_B, b);
    }

    /** Render semi-transparent button shapes. Call with no SpriteBatch active. */
    public void render() {
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawButton(zoneUp,    InputManager.Action.UP);
        drawButton(zoneDown,  InputManager.Action.DOWN);
        drawButton(zoneLeft,  InputManager.Action.LEFT);
        drawButton(zoneRight, InputManager.Action.RIGHT);
        drawButton(zoneA,     InputManager.Action.ACTION_A);
        drawButton(zoneB,     InputManager.Action.ACTION_B);
        shapes.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private void drawButton(Rectangle zone, InputManager.Action action) {
        shapes.setColor(inputManager.isPressed(action) ? BTN_PRESSED_COLOR : BTN_COLOR);
        shapes.rect(zone.x, zone.y, zone.width, zone.height);
    }

    public void dispose() {
        shapes.dispose();
    }
}
