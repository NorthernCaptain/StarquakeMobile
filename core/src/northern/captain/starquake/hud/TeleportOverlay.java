package northern.captain.starquake.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.TeleportRegistry;

/**
 * Teleport destination chooser overlay. Shows a 3×5 grid of teleporter names.
 * Uses teleportng.png background with a large black area for the name grid.
 */
public class TeleportOverlay implements Overlay {
    private static final float SLIDE_TIME = 0.5f;
    private static final float FLASH_FREQ = 5f;
    private static final int VIEWPORT_H = 168;
    private static final int VIEWPORT_W = 256;

    // 3 columns × 5 rows grid in the black area
    private static final int COLS = 3;
    private static final int ROWS = 5;
    private static final float[] COL_X = {29, 99, 169};
    private static final float ROW_TOP = 61;
    private static final float ROW_STEP = 10;

    private static final Color COLOR_VISITED = Color.WHITE;
    private static final Color COLOR_CURRENT = new Color(1f, 1f, 0.2f, 1f);
    private static final Color COLOR_YELLOW = new Color(1f, 1f, 0.2f, 1f);

    // Lightning arc between the two balls: src (143,13)-(174,13) → viewport
    private static final float LIGHTNING_X = 116;
    private static final float LIGHTNING_Y = 152;
    private static final float LIGHTNING_W = 25;
    private static final float LIGHTNING_H = 8;

    // Typing text positions
    private static final float TEXT_TELEPORT_X = 19;
    private static final float TEXT_TELEPORT_Y = 94;
    private static final float TEXT_NAME_X = 185;
    private static final float TEXT_NAME_Y = 94;
    private static final String LABEL_TELEPORT = "TELEPORT";
    private static final float TYPE_SPEED = 18f; // chars per second

    private static final float CONFIRM_TIME = 0.5f;

    enum State { SLIDE_IN, ACTIVE, CONFIRM, SLIDE_OUT, DONE }

    private final Viewport viewport;
    private final TeleportRegistry registry;
    private final int currentRoom;
    private final int currentTeleportIdx;
    private final BitmapFont font;

    private State state = State.SLIDE_IN;
    private float timer;
    private float offsetY;

    // Selection: grid position (col, row) mapping to teleport index = col * ROWS + row
    private int selCol;
    private int selRow;

    private int targetRoom = -1;

    private final TextureRegion background;
    private final TextureRegion pixel;
    private final ShaderProgram lightningShader;
    private final String currentName;
    private float totalTime; // accumulates across all states for lightning

    // Touch
    private final Vector2 touchPos = new Vector2();
    private boolean touchHandled;

    public TeleportOverlay(Assets assets, Viewport viewport, TeleportRegistry registry, int currentRoom) {
        this.viewport = viewport;
        this.registry = registry;
        this.currentRoom = currentRoom;
        this.currentTeleportIdx = registry.indexOf(currentRoom);
        this.font = assets.font;
        this.background = assets.teleportngScreen;
        this.pixel = assets.whitePixel;
        this.lightningShader = assets.lightningShader;
        this.currentName = registry.getName(currentTeleportIdx);
        this.offsetY = -VIEWPORT_H;

        // Default selection: current teleporter
        selCol = currentTeleportIdx / ROWS;
        selRow = currentTeleportIdx % ROWS;
    }

    @Override
    public void update(float delta, InputManager input) {
        timer += delta;
        totalTime += delta;
        switch (state) {
            case SLIDE_IN:  updateSlideIn(); break;
            case ACTIVE:    updateActive(input); break;
            case CONFIRM:   if (timer >= CONFIRM_TIME) setState(State.SLIDE_OUT); break;
            case SLIDE_OUT: updateSlideOut(); break;
            default: break;
        }
    }

    private void updateSlideIn() {
        float t = Math.min(timer / SLIDE_TIME, 1f);
        offsetY = -VIEWPORT_H * (1f - Interpolation.pow2Out.apply(t));
        if (t >= 1f) { offsetY = 0; setState(State.ACTIVE); }
    }

    private void updateActive(InputManager input) {
        if (input.isJustPressed(InputManager.Action.UP)) {
            moveSelection(0, -1);
        }
        if (input.isJustPressed(InputManager.Action.DOWN)) {
            moveSelection(0, 1);
        }
        if (input.isJustPressed(InputManager.Action.LEFT)) {
            moveSelection(-1, 0);
        }
        if (input.isJustPressed(InputManager.Action.RIGHT)) {
            moveSelection(1, 0);
        }
        if (input.isJustPressed(InputManager.Action.ACTION_A)) {
            acceptSelection();
        }
        if (input.isJustPressed(InputManager.Action.ACTION_B)) {
            setState(State.SLIDE_OUT);
        }
        checkTouch();
    }

    /** Move selection in the given direction, skipping unvisited cells. */
    private void moveSelection(int dc, int dr) {
        int startCol = selCol, startRow = selRow;
        for (int step = 0; step < TeleportRegistry.COUNT; step++) {
            selCol = (selCol + dc + COLS) % COLS;
            selRow = (selRow + dr + ROWS) % ROWS;
            int idx = selCol * ROWS + selRow;
            if (idx < TeleportRegistry.COUNT && registry.isVisited(idx)) return;
        }
        // No visited cell found in that direction — stay put
        selCol = startCol;
        selRow = startRow;
    }

    private void acceptSelection() {
        int idx = selCol * ROWS + selRow;
        if (idx < 0 || idx >= TeleportRegistry.COUNT) {
            setState(State.SLIDE_OUT);
            return;
        }
        if (!registry.isVisited(idx)) return; // can't select unvisited

        int room = registry.getRoomForIndex(idx);
        if (room == currentRoom) {
            setState(State.SLIDE_OUT); // same location
        } else {
            targetRoom = room;
            setState(State.CONFIRM); // show selected name briefly before closing
        }
    }

    private void checkTouch() {
        if (Gdx.input.isTouched()) {
            if (!touchHandled) {
                touchHandled = true;
                touchPos.set(Gdx.input.getX(), Gdx.input.getY());
                viewport.unproject(touchPos);
                float tx = touchPos.x;
                float ty = touchPos.y;

                for (int col = 0; col < COLS; col++) {
                    for (int row = 0; row < ROWS; row++) {
                        int idx = col * ROWS + row;
                        if (idx >= TeleportRegistry.COUNT || !registry.isVisited(idx)) continue;
                        float cx = COL_X[col];
                        float cy = getRowY(row) + offsetY;
                        if (tx >= cx - 2 && tx <= cx + 42 && ty >= cy - 10 && ty <= cy + 2) {
                            selCol = col;
                            selRow = row;
                            acceptSelection();
                            return;
                        }
                    }
                }
            }
        } else {
            touchHandled = false;
        }
    }

    private void updateSlideOut() {
        float t = Math.min(timer / SLIDE_TIME, 1f);
        offsetY = -VIEWPORT_H * Interpolation.pow2In.apply(t);
        if (t >= 1f) state = State.DONE;
    }

    private void setState(State s) {
        state = s;
        timer = 0;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (state == State.DONE) return;

        // Background
        if (background != null) {
            batch.draw(background, 0, offsetY, VIEWPORT_W, VIEWPORT_H);
        } else {
            batch.setColor(0.05f, 0.05f, 0.1f, 0.95f);
            batch.draw(pixel, 0, offsetY, VIEWPORT_W, VIEWPORT_H);
            batch.setColor(Color.WHITE);
        }

        // Lightning arc between balls
        if (lightningShader != null) {
            float pixelSize = Gdx.graphics.getHeight() / 144f;
            batch.flush();
            batch.setShader(lightningShader);
            lightningShader.setUniformf("u_time", totalTime);
            lightningShader.setUniformf("u_pixelSize", pixelSize);
            batch.setColor(Color.WHITE);
            batch.draw(pixel, LIGHTNING_X, LIGHTNING_Y + offsetY, LIGHTNING_W, LIGHTNING_H);
            batch.flush();
            batch.setShader(null);
        }

        // Typing text — only during ACTIVE state
        if (state == State.ACTIVE) {
            int teleportChars = Math.min((int) (timer * TYPE_SPEED), LABEL_TELEPORT.length());
            if (teleportChars > 0) {
                font.setColor(COLOR_YELLOW);
                font.draw(batch, LABEL_TELEPORT.substring(0, teleportChars),
                        TEXT_TELEPORT_X, TEXT_TELEPORT_Y + offsetY);
            }
            if (currentName != null) {
                // Current name starts typing after "TELEPORT" finishes
                float nameDelay = LABEL_TELEPORT.length() / TYPE_SPEED;
                float nameTime = timer - nameDelay;
                if (nameTime > 0) {
                    int nameChars = Math.min((int) (nameTime * TYPE_SPEED), currentName.length());
                    if (nameChars > 0) {
                        font.setColor(COLOR_YELLOW);
                        font.draw(batch, currentName.substring(0, nameChars),
                                TEXT_NAME_X, TEXT_NAME_Y + offsetY);
                    }
                }
            }
        }

        // Draw 3×5 grid of teleporter names
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                int idx = col * ROWS + row;
                if (idx >= TeleportRegistry.COUNT) continue;
                if (!registry.isVisited(idx)) continue;

                String name = registry.getName(idx);
                if (name == null) continue;

                boolean isSelected = (col == selCol && row == selRow);

                // In CONFIRM or SLIDE_OUT after confirm: only draw the selected name in yellow
                if (state == State.CONFIRM || (state == State.SLIDE_OUT && targetRoom >= 0)) {
                    if (!isSelected) continue;
                    font.setColor(COLOR_YELLOW);
                    font.draw(batch, name, COL_X[col], getRowY(row) + offsetY);
                    continue;
                }

                boolean isCurrent = (idx == currentTeleportIdx);

                if (isSelected && state == State.ACTIVE) {
                    boolean visible = ((int) (timer * FLASH_FREQ * 2)) % 2 == 0;
                    if (!visible) continue;
                }

                font.setColor(isCurrent ? COLOR_CURRENT : COLOR_VISITED);
                font.draw(batch, name, COL_X[col], getRowY(row) + offsetY);
            }
        }
        font.setColor(Color.WHITE);
    }

    private float getRowY(int row) {
        return ROW_TOP - row * ROW_STEP;
    }

    @Override
    public boolean isDone() {
        return state == State.DONE;
    }

    /** Room index to teleport to, or -1 if cancelled. */
    public int getTargetRoom() {
        return targetRoom;
    }
}
