package northern.captain.starquake.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import northern.captain.starquake.Assets;
import northern.captain.starquake.input.InputManager;
import northern.captain.starquake.world.Inventory;
import northern.captain.starquake.world.items.CheopsPyramid;
import northern.captain.starquake.world.items.ItemType;

/**
 * Full-screen trading overlay using the circuit board background.
 * Layout matches the image: 1 cell near "EXCHANGE" for the offered item,
 * 5 numbered cells below for selectable options (4 core parts + keep).
 */
public class TradingOverlay implements Overlay {
    private static final float SLIDE_TIME = 0.5f;
    private static final float SWAP_TIME = 0.5f;
    private static final float FLASH_FREQ = 5f; // Hz
    private static final int VIEWPORT_H = 168;
    private static final int VIEWPORT_W = 256;
    private static final int MAX_CORE_OPTIONS = 4;

    // HUD inventory slot coordinates (same as Hud.java)
    private static final float HUD_SLOT_BASE_X = VIEWPORT_W - 4 * 17 - 2;
    private static final float HUD_SLOT_Y = 144 + (24 - 16) / 2f;
    private static final float HUD_SLOT_STEP = 17;

    // Cell positions detected from 320x200 circuit board image, mapped to 256x168 viewport.
    // Mapping: vx = srcX * 256/320, vy = (200 - srcY - 16) * 168/200 (libGDX Y-up).
    // Offered item cell: src (141, 54) — centered near "EXCHANGE" text
    private static final float OFFERED_X = 114;
    private static final float OFFERED_Y = 107;

    // 5 exchange cells: src row y=103, x = 45, 93, 141, 189, 237
    private static final float ROW_Y = 66;
    private static final float[] CELL_X = {37, 75, 114, 152, 191};

    // Text bar: src (59,134)-(251,146) → viewport ~(47,46) left-aligned with padding
    private static final float TEXT_X = 49;
    private static final float TEXT_Y = 55; // font draws from top, so use top of bar
    private static final String PROMPT_TEXT = "PICK AN ITEM";
    private static final float TYPE_SPEED = 18f; // chars per second

    enum State { SLIDE_IN, ACTIVE, SWAP_ANIM, SLIDE_OUT, DONE }

    private final Viewport viewport;
    private final Inventory inventory;
    private final CheopsPyramid pyramid;
    private final ItemType offeredItem;
    private final int offeredSlotIndex;

    // Selectable options: up to 4 core parts, then offered item (keep) last
    private final ItemType[] options;
    private final int optionCount;
    private final int keepIndex;

    private State state = State.SLIDE_IN;
    private float timer;
    private int selectedIndex;
    private float offsetY;

    // Swap animation
    private float flyX, flyY;
    private float flyStartX, flyStartY;
    private float flyEndX, flyEndY;
    private ItemType swapItem;

    // Rendering
    private final BitmapFont font;
    private final TextureRegion background;
    private final TextureRegion[] itemIcons;
    private final TextureRegion pixel;

    private final Vector2 touchPos = new Vector2();
    private boolean touchHandled;

    public TradingOverlay(Assets assets, Viewport viewport, Inventory inventory,
                          CheopsPyramid pyramid, ItemType offeredItem, int offeredSlotIndex,
                          ItemType[] coreOptions) {
        this.viewport = viewport;
        this.inventory = inventory;
        this.pyramid = pyramid;
        this.offeredItem = offeredItem;
        this.offeredSlotIndex = offeredSlotIndex;
        this.font = assets.font;
        this.background = assets.circuitScreen;
        this.pixel = assets.whitePixel;

        // Cache item icons
        itemIcons = new TextureRegion[35];
        for (int i = 0; i < itemIcons.length; i++) {
            itemIcons[i] = assets.itemsAtlas.findRegion("item", i);
        }

        // Build options: up to 4 core parts + keep (= 5 max)
        int coreCount = 0;
        for (ItemType part : coreOptions) {
            if (part != null && coreCount < MAX_CORE_OPTIONS) coreCount++;
        }
        optionCount = coreCount + 1;
        options = new ItemType[optionCount];
        int idx = 0;
        for (ItemType part : coreOptions) {
            if (part != null && idx < MAX_CORE_OPTIONS) options[idx++] = part;
        }
        options[idx] = offeredItem; // "keep" option
        keepIndex = idx;

        selectedIndex = keepIndex;
        offsetY = -VIEWPORT_H;
    }

    @Override
    public void update(float delta, InputManager input) {
        timer += delta;

        switch (state) {
            case SLIDE_IN:
                updateSlideIn();
                break;
            case ACTIVE:
                updateActive(input);
                break;
            case SWAP_ANIM:
                updateSwapAnim();
                break;
            case SLIDE_OUT:
                updateSlideOut();
                break;
            default:
                break;
        }
    }

    private void updateSlideIn() {
        float t = Math.min(timer / SLIDE_TIME, 1f);
        offsetY = -VIEWPORT_H * (1f - Interpolation.pow2Out.apply(t));
        if (t >= 1f) {
            offsetY = 0;
            setState(State.ACTIVE);
        }
    }

    private void updateActive(InputManager input) {
        if (input.isJustPressed(InputManager.Action.LEFT)) {
            selectedIndex = (selectedIndex - 1 + optionCount) % optionCount;
        }
        if (input.isJustPressed(InputManager.Action.RIGHT)) {
            selectedIndex = (selectedIndex + 1) % optionCount;
        }
        if (input.isJustPressed(InputManager.Action.DOWN)) {
            setState(State.SLIDE_OUT);
        }
        if (input.isJustPressed(InputManager.Action.UP)) {
            acceptSelection();
        }

        // Touch: tap on an item cell to select + accept it
        checkTouch();
    }

    private void acceptSelection() {
        if (selectedIndex == keepIndex) {
            setState(State.SLIDE_OUT);
        } else {
            swapItem = options[selectedIndex];
            startSwapAnim();
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

                // Check each option cell (16x16 with some tap margin)
                for (int i = 0; i < optionCount; i++) {
                    float cx = CELL_X[i];
                    float cy = ROW_Y + offsetY;
                    if (tx >= cx - 4 && tx <= cx + 20 && ty >= cy - 4 && ty <= cy + 20) {
                        selectedIndex = i;
                        acceptSelection();
                        return;
                    }
                }
            }
        } else {
            touchHandled = false;
        }
    }

    private void startSwapAnim() {
        flyStartX = CELL_X[selectedIndex];
        flyStartY = ROW_Y;

        flyEndX = HUD_SLOT_BASE_X + offeredSlotIndex * HUD_SLOT_STEP;
        flyEndY = HUD_SLOT_Y;

        flyX = flyStartX;
        flyY = flyStartY;
        setState(State.SWAP_ANIM);
    }

    private void updateSwapAnim() {
        float t = Math.min(timer / SWAP_TIME, 1f);
        float ease = Interpolation.pow2.apply(t);
        flyX = flyStartX + (flyEndX - flyStartX) * ease;
        flyY = flyStartY + (flyEndY - flyStartY) * ease;

        if (t >= 1f) {
            inventory.setSlot(offeredSlotIndex, swapItem);
            pyramid.consumeAfterTrade();
            setState(State.SLIDE_OUT);
        }
    }

    private void updateSlideOut() {
        float t = Math.min(timer / SLIDE_TIME, 1f);
        offsetY = -VIEWPORT_H * Interpolation.pow2In.apply(t);
        if (t >= 1f) {
            state = State.DONE;
        }
    }

    private void setState(State s) {
        state = s;
        timer = 0;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (state == State.DONE) return;

        // Background: circuit board scaled to viewport
        if (background != null) {
            batch.draw(background, 0, offsetY, VIEWPORT_W, VIEWPORT_H);
        } else {
            batch.setColor(0.05f, 0.05f, 0.1f, 0.95f);
            batch.draw(pixel, 0, offsetY, VIEWPORT_W, VIEWPORT_H);
            batch.setColor(Color.WHITE);
        }

        // Offered item in the cell near "EXCHANGE"
        TextureRegion offeredIcon = itemIcons[offeredItem.spriteIndex];
        if (offeredIcon != null) {
            batch.draw(offeredIcon, OFFERED_X, OFFERED_Y + offsetY, 16, 16);
        }

        // 5 selectable options in the numbered cells
        for (int i = 0; i < optionCount; i++) {
            float ix = CELL_X[i];
            float iy = ROW_Y + offsetY;
            TextureRegion icon = itemIcons[options[i].spriteIndex];
            if (icon == null) continue;

            if (i == selectedIndex) {
                if (state == State.SWAP_ANIM) continue;
                boolean visible = ((int) (timer * FLASH_FREQ * 2)) % 2 == 0;
                if (visible) {
                    batch.draw(icon, ix, iy, 16, 16);
                }
            } else {
                batch.draw(icon, ix, iy, 16, 16);
            }
        }

        // Flying icon during swap animation
        if (state == State.SWAP_ANIM && swapItem != null) {
            TextureRegion flyIcon = itemIcons[swapItem.spriteIndex];
            if (flyIcon != null) {
                batch.draw(flyIcon, flyX, flyY, 16, 16);
            }
        }

        // Typewriter text in the black bar (only during ACTIVE/SWAP states)
        if (state == State.ACTIVE || state == State.SWAP_ANIM) {
            int charsToShow = Math.min((int) (timer * TYPE_SPEED), PROMPT_TEXT.length());
            if (charsToShow > 0) {
                font.setColor(Color.WHITE);
                font.draw(batch, PROMPT_TEXT.substring(0, charsToShow), TEXT_X, TEXT_Y + offsetY);
                font.setColor(Color.WHITE);
            }
        }
    }

    @Override
    public boolean isDone() {
        return state == State.DONE;
    }
}
