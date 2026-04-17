package northern.captain.starquake.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * On-screen touch controls.
 *
 * Walk mode: 4 rounded rectangle buttons (D-pad cross) + 1 action button.
 * Fly mode:  circular analog pad (360° direction) + 1 action button.
 */
public class TouchControls {
    private static final float BUTTON_SIZE = 80;
    private static final float PADDING = 16;
    private static final Color BTN_COLOR = new Color(1, 1, 1, 0.1f);
    private static final Color BTN_PRESSED_COLOR = new Color(1, 1, 1, 0.125f);
    private static final Color CIRCLE_COLOR = new Color(1, 1, 1, 0.075f);
    private static final Color CIRCLE_ACTIVE_COLOR = new Color(1, 1, 1, 0.15f);

    private static final float DEAD_ZONE = 0.15f;
    private static final float CORNER_RADIUS_FRAC = 0.15f; // rounded corner as fraction of button size

    private final InputManager inputManager;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final ScreenViewport viewport = new ScreenViewport();
    private boolean leftHanded;

    // D-pad button zones (walk mode)
    private final Rectangle zoneUp    = new Rectangle();
    private final Rectangle zoneDown  = new Rectangle();
    private final Rectangle zoneLeft  = new Rectangle();
    private final Rectangle zoneRight = new Rectangle();

    // Circle pad (fly mode)
    private float circleCenterX, circleCenterY, circleRadius;
    private float circleInnerRadius;
    private int circlePointer = -1;
    private boolean walkMode = true;

    // D-pad center for layout
    private float dpadCenterX, dpadCenterY;

    // Single action button (zoneA = touch area, zoneADraw = visible button)
    private final Rectangle zoneA = new Rectangle();
    private final Rectangle zoneADraw = new Rectangle();

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

        // Circle pad (shared center with D-pad)
        circleRadius = bs * 1.5f;
        circleInnerRadius = circleRadius * DEAD_ZONE;

        float actionCenterX;
        if (leftHanded) {
            dpadCenterX = pad + circleRadius;
            actionCenterX = w - pad - bs * 0.7f;
        } else {
            dpadCenterX = w - pad - circleRadius;
            actionCenterX = pad + bs * 0.7f;
        }
        dpadCenterY = h * 0.28f;
        circleCenterX = dpadCenterX;
        circleCenterY = dpadCenterY;

        // D-pad: 4 buttons in cross arrangement
        float sp = bs * 0.08f;
        zoneUp.set(dpadCenterX - bs / 2, dpadCenterY + bs / 2 + sp, bs, bs);
        zoneDown.set(dpadCenterX - bs / 2, dpadCenterY - bs * 1.5f - sp, bs, bs);
        zoneLeft.set(dpadCenterX - bs * 1.5f - sp, dpadCenterY - bs / 2, bs, bs);
        zoneRight.set(dpadCenterX + bs / 2 + sp, dpadCenterY - bs / 2, bs, bs);

        // Single action button — bigger
        float abSize = bs * 1.4f;
        float actionY = h * 0.28f;
        zoneADraw.set(actionCenterX - abSize / 2, actionY - abSize / 2, abSize, abSize);
        // Touch area is 2x wider and 2x taller, centered on the same point
        zoneA.set(actionCenterX - abSize, actionY - abSize, abSize * 2, abSize * 2);
    }

    public void poll() {
        boolean a = false;
        float padX = 0, padY = 0;
        boolean padActive = false;
        circlePointer = -1;

        boolean up = false, down = false, left = false, right = false;

        for (int pointer = 0; pointer < 5; pointer++) {
            if (!Gdx.input.isTouched(pointer)) continue;
            float tx = Gdx.input.getX(pointer);
            float ty = Gdx.graphics.getHeight() - Gdx.input.getY(pointer);

            if (!walkMode) {
                // Fly mode: circle pad
                float dx = tx - circleCenterX;
                float dy = ty - circleCenterY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist <= circleRadius * 1.3f && !padActive) {
                    if (dist > circleInnerRadius) {
                        float normDist = Math.min(dist / circleRadius, 1f);
                        padX = (dx / dist) * normDist;
                        padY = (dy / dist) * normDist;
                        padActive = true;
                        circlePointer = pointer;
                    }
                }
            } else {
                // Walk mode: rectangle D-pad buttons
                if (zoneUp.contains(tx, ty))    up = true;
                if (zoneDown.contains(tx, ty))  down = true;
                if (zoneLeft.contains(tx, ty))  left = true;
                if (zoneRight.contains(tx, ty)) right = true;
            }

            if (zoneA.contains(tx, ty)) a = true;
        }

        // Set analog (fly mode only)
        inputManager.setAnalog(padX, padY);

        // Digital directions: from D-pad in walk mode, from analog thresholds in fly mode
        if (!walkMode) {
            left = padX < -0.3f;
            right = padX > 0.3f;
            up = padY > 0.3f;
            down = padY < -0.3f;
        }

        inputManager.setTouchState(InputManager.Action.LEFT, left);
        inputManager.setTouchState(InputManager.Action.RIGHT, right);
        inputManager.setTouchState(InputManager.Action.UP, up);
        inputManager.setTouchState(InputManager.Action.DOWN, down);
        inputManager.setTouchState(InputManager.Action.ACTION_A, a);
        inputManager.setTouchState(InputManager.Action.ACTION_B, false);
    }

    public void render() {
        viewport.apply();
        shapes.setProjectionMatrix(viewport.getCamera().combined);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);

        if (walkMode) {
            // D-pad: 4 rounded rect buttons
            drawRoundedButton(zoneUp,    InputManager.Action.UP);
            drawRoundedButton(zoneDown,  InputManager.Action.DOWN);
            drawRoundedButton(zoneLeft,  InputManager.Action.LEFT);
            drawRoundedButton(zoneRight, InputManager.Action.RIGHT);
        } else {
            // Fly mode: circle pad
            shapes.setColor(circlePointer >= 0 ? CIRCLE_ACTIVE_COLOR : CIRCLE_COLOR);
            shapes.circle(circleCenterX, circleCenterY, circleRadius, 32);
            shapes.setColor(0, 0, 0, 0.3f);
            shapes.circle(circleCenterX, circleCenterY, circleInnerRadius, 16);

            if (circlePointer >= 0) {
                float ax = inputManager.getAnalogX();
                float ay = inputManager.getAnalogY();
                float dotX = circleCenterX + ax * circleRadius * 0.7f;
                float dotY = circleCenterY + ay * circleRadius * 0.7f;
                shapes.setColor(1, 1, 1, 0.5f);
                shapes.circle(dotX, dotY, circleRadius * 0.12f, 12);
            }
        }

        // Action button (rounded) — draw at visible size, touch area is larger
        drawRoundedButton(zoneADraw, InputManager.Action.ACTION_A);

        shapes.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private void drawRoundedButton(Rectangle zone, InputManager.Action action) {
        shapes.setColor(inputManager.isPressed(action) ? BTN_PRESSED_COLOR : BTN_COLOR);
        drawRoundedRect(zone.x, zone.y, zone.width, zone.height);
    }

    /** Draw a filled rounded rectangle using ShapeRenderer primitives. */
    private void drawRoundedRect(float x, float y, float w, float h) {
        float r = Math.min(w, h) * CORNER_RADIUS_FRAC;
        // Main body (3 non-overlapping rects)
        shapes.rect(x + r, y, w - 2 * r, h);
        shapes.rect(x, y + r, r, h - 2 * r);
        shapes.rect(x + w - r, y + r, r, h - 2 * r);
        // 4 corner arcs (quarter circles)
        shapes.arc(x + r, y + r, r, 180, 90, 8);         // bottom-left
        shapes.arc(x + w - r, y + r, r, 270, 90, 8);     // bottom-right
        shapes.arc(x + r, y + h - r, r, 90, 90, 8);      // top-left
        shapes.arc(x + w - r, y + h - r, r, 0, 90, 8);   // top-right
    }

    public void setWalkMode(boolean walkMode) {
        this.walkMode = walkMode;
    }

    public void dispose() {
        shapes.dispose();
    }
}
