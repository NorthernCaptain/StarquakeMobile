package northern.captain.starquake.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import northern.captain.starquake.Assets;
import northern.captain.starquake.event.CoreDeliveredEvent;
import northern.captain.starquake.event.EventBus;
import northern.captain.starquake.event.GameEvent;
import northern.captain.starquake.event.GameOverEvent;
import northern.captain.starquake.world.items.ItemType;

import java.util.Random;

/**
 * Tracks the 3×3 core assembly grid and renders it in the core room.
 * Handles the animated delivery sequence when BLOB brings core parts.
 */
public class CoreAssembly {
    public static final int CORE_ROOM = 199;
    private static final int GRID_SIZE = 3;
    private static final int MISSING_COUNT = 9;

    // Animation timing
    private static final float DISASSEMBLE_TIME = 0.5f;
    private static final float PAUSE_AFTER_DISASSEMBLE = 0.5f;
    private static final float DELIVER_TIME = 0.8f;
    private static final float LIGHTNING_TIME = 0.8f;
    private static final float FADE_IN_TIME = 0.3f;
    private static final float PAUSE_AFTER_MERGE = 0.3f;
    private static final float FLASH_RED_TIME = 1.0f;
    private static final float ASSEMBLE_TIME = 0.5f;
    private static final float GAP_SIZE = 20f; // 16px item + 2px gap each side

    // Disassembly offsets per cell: (col-1, row-1) in grid coords → pixel direction
    private static final int[] OFFSET_DX = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
    private static final int[] OFFSET_DY = { 1, 1, 1,  0, 0, 0, -1,-1,-1}; // libGDX Y-up

    enum Phase { IDLE, DISASSEMBLE, PAUSE_OPEN, SCAN, DELIVER, LIGHTNING, FADE_IN, PAUSE_MERGE, FLASH_RED, ASSEMBLE }

    // Core data
    private final ItemType[] displayPieces = new ItemType[9];
    private final ItemType[] requiredParts = new ItemType[9];
    private final boolean[] restored = new boolean[9];
    private int restoredCount;

    // Rendering
    private static final float GRID_X = (256 - 48) / 2f;
    private static final float GRID_Y = (144 - 48) / 2f;
    private static final int CELL_SIZE = 16;
    private final TextureRegion[] icons;
    private final Assets assets;

    // Animation state
    private Phase phase = Phase.IDLE;
    private float phaseTimer;
    private float totalTime; // for lightning shader
    private Inventory inventory;
    private Blob blob;

    // Per-cell animation offsets
    private final float[] cellOffsetX = new float[9];
    private final float[] cellOffsetY = new float[9];

    // Delivery animation
    private int currentSlot;        // inventory slot index being delivered
    private int matchedGridIdx;     // grid position of matched required part
    private ItemType matchedPart;   // the part type being delivered
    private boolean anyDelivered;   // true if at least one item was delivered this animation
    private int fadeInIdx = -1;       // grid index of cell currently fading in
    private float deliverX, deliverY; // current position of flying item
    private float deliverStartX, deliverStartY;
    private float deliverEndX, deliverEndY;

    // HUD inventory slot positions (viewport coords)
    private static final float HUD_SLOT_BASE_X = 256 - 4 * 17 - 2;
    private static final float HUD_SLOT_Y = 144 + (24 - 16) / 2f;
    private static final float HUD_SLOT_STEP = 17;

    public CoreAssembly(Assets assets) {
        this.assets = assets;
        this.icons = new TextureRegion[35];
        for (int i = 0; i < icons.length; i++) {
            icons[i] = assets.itemsAtlas.findRegion("item", i);
        }

        displayPieces[0] = ItemType.CORE_TL;
        displayPieces[1] = ItemType.CORE_TC;
        displayPieces[2] = ItemType.CORE_TR;
        displayPieces[3] = ItemType.CORE_ML;
        displayPieces[4] = ItemType.CORE_MC;
        displayPieces[5] = ItemType.CORE_MR;
        displayPieces[6] = ItemType.CORE_BL;
        displayPieces[7] = ItemType.CORE_BC;
        displayPieces[8] = ItemType.CORE_BR;
    }

    public void initialize(Random rng, ItemType[] partPool) {
        for (int i = 0; i < 9; i++) {
            restored[i] = true;
            requiredParts[i] = null;
        }
        restoredCount = 9 - MISSING_COUNT;

        int[] positions = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        for (int i = positions.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = positions[i]; positions[i] = positions[j]; positions[j] = tmp;
        }

        ItemType[] shuffledParts = partPool.clone();
        for (int i = shuffledParts.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            ItemType tmp = shuffledParts[i]; shuffledParts[i] = shuffledParts[j]; shuffledParts[j] = tmp;
        }

        for (int i = 0; i < MISSING_COUNT; i++) {
            int pos = positions[i];
            restored[pos] = false;
            requiredParts[pos] = shuffledParts[i];
        }
    }

    // ---- Animation control ----

    public boolean isAnimating() { return phase != Phase.IDLE; }

    public void startAnimation(Inventory inv, Blob b) {
        if (phase != Phase.IDLE) return;
        this.inventory = inv;
        this.blob = b;
        this.anyDelivered = false;
        if (blob != null) blob.startLifting(); // freeze BLOB but keep visible
        setPhase(Phase.DISASSEMBLE);
    }

    private void setPhase(Phase p) {
        phase = p;
        phaseTimer = 0;
    }

    public void update(float delta) {
        if (phase == Phase.IDLE) return;
        phaseTimer += delta;
        totalTime += delta;

        switch (phase) {
            case DISASSEMBLE:  updateDisassemble(); break;
            case PAUSE_OPEN:   if (phaseTimer >= PAUSE_AFTER_DISASSEMBLE) setPhase(Phase.SCAN); break;
            case SCAN:         updateScan(); break;
            case DELIVER:      updateDeliver(); break;
            case LIGHTNING:    updateLightning(); break;
            case FADE_IN:      if (phaseTimer >= FADE_IN_TIME) { fadeInIdx = -1; setPhase(Phase.PAUSE_MERGE); } break;
            case PAUSE_MERGE:  if (phaseTimer >= PAUSE_AFTER_MERGE) { currentSlot = 0; setPhase(Phase.SCAN); } break;
            case FLASH_RED:    updateFlashRed(); break;
            case ASSEMBLE:     updateAssemble(); break;
            default: break;
        }
    }

    private void updateDisassemble() {
        float t = Math.min(phaseTimer / DISASSEMBLE_TIME, 1f);
        float ease = Interpolation.pow2Out.apply(t);
        for (int i = 0; i < 9; i++) {
            cellOffsetX[i] = OFFSET_DX[i] * GAP_SIZE * ease;
            cellOffsetY[i] = OFFSET_DY[i] * GAP_SIZE * ease;
        }
        if (t >= 1f) {
            currentSlot = 0;
            setPhase(Phase.PAUSE_OPEN);
        }
    }

    private void updateScan() {
        // Search inventory from currentSlot onward
        while (currentSlot < Inventory.MAX_SLOTS) {
            ItemType item = inventory.getSlot(currentSlot);
            if (item != null && item.isCorePart()) {
                // Check if it matches any required part
                for (int i = 0; i < 9; i++) {
                    if (!restored[i] && requiredParts[i] == item) {
                        matchedGridIdx = i;
                        matchedPart = item;
                        setupDelivery();
                        setPhase(Phase.DELIVER);
                        return;
                    }
                }
            }
            currentSlot++;
        }
        // No more matches — flash red only if nothing was delivered at all
        setPhase(anyDelivered ? Phase.ASSEMBLE : Phase.FLASH_RED);
    }

    private void setupDelivery() {
        // Start: HUD inventory slot position
        deliverStartX = HUD_SLOT_BASE_X + currentSlot * HUD_SLOT_STEP;
        deliverStartY = HUD_SLOT_Y;

        // End: adjacent to the matched grid cell
        int gridCol = matchedGridIdx % GRID_SIZE;
        float cellX = getCellX(matchedGridIdx);
        float cellY = getCellY(matchedGridIdx);

        if (gridCol == 2) {
            // Right column: arrive from the right, 2px gap from cell
            deliverEndX = cellX + CELL_SIZE + 2;
        } else {
            // Left or center column: arrive from the left, 2px gap from cell
            deliverEndX = cellX - CELL_SIZE - 2;
        }
        deliverEndY = cellY;

        deliverX = deliverStartX;
        deliverY = deliverStartY;
    }

    private void updateDeliver() {
        float t = Math.min(phaseTimer / DELIVER_TIME, 1f);
        float ease = Interpolation.pow2.apply(t);
        deliverX = deliverStartX + (deliverEndX - deliverStartX) * ease;
        deliverY = deliverStartY + (deliverEndY - deliverStartY) * ease;
        if (t >= 1f) setPhase(Phase.LIGHTNING);
    }

    private void updateLightning() {
        if (phaseTimer >= LIGHTNING_TIME) {
            // Both items disappear, core piece fades in
            restored[matchedGridIdx] = true;
            requiredParts[matchedGridIdx] = null;
            restoredCount++;
            anyDelivered = true;
            inventory.remove(matchedPart);
            EventBus.get().post(new CoreDeliveredEvent(restoredCount));
            matchedPart = null;
            fadeInIdx = matchedGridIdx;
            setPhase(Phase.FADE_IN);
        }
    }

    private void updateFlashRed() {
        if (phaseTimer >= FLASH_RED_TIME) {
            setPhase(Phase.ASSEMBLE);
        }
    }

    private void updateAssemble() {
        float t = Math.min(phaseTimer / ASSEMBLE_TIME, 1f);
        float ease = Interpolation.pow2In.apply(t);
        for (int i = 0; i < 9; i++) {
            cellOffsetX[i] = OFFSET_DX[i] * GAP_SIZE * (1f - ease);
            cellOffsetY[i] = OFFSET_DY[i] * GAP_SIZE * (1f - ease);
        }
        if (t >= 1f) {
            for (int i = 0; i < 9; i++) { cellOffsetX[i] = 0; cellOffsetY[i] = 0; }
            phase = Phase.IDLE;
            if (blob != null) blob.stopLifting();
            blob = null;
            inventory = null;
            if (isComplete()) {
                EventBus.get().post(new GameOverEvent(true));
            }
        }
    }

    // ---- Getters ----

    private float getCellX(int idx) {
        int col = idx % GRID_SIZE;
        return GRID_X + col * CELL_SIZE + cellOffsetX[idx];
    }

    private float getCellY(int idx) {
        int row = idx / GRID_SIZE;
        return GRID_Y + (GRID_SIZE - 1 - row) * CELL_SIZE + cellOffsetY[idx];
    }

    /** Returns true if the HUD inventory area should flash red this frame. */
    public boolean isInventoryFlashRed() {
        if (phase != Phase.FLASH_RED) return false;
        int flashCount = (int) (phaseTimer / (FLASH_RED_TIME / 10f));
        return flashCount % 2 == 0;
    }

    /** Returns the inventory slot being delivered (flying), or -1 if not delivering. */
    public int getDeliveringSlot() {
        if (phase == Phase.DELIVER || phase == Phase.LIGHTNING) return currentSlot;
        return -1;
    }

    public boolean isComplete() { return restoredCount >= 9; }
    public int getRestoredCount() { return restoredCount; }

    public ItemType[] getRequiredParts() {
        ItemType[] parts = new ItemType[MISSING_COUNT];
        int idx = 0;
        for (int i = 0; i < 9; i++) {
            if (!restored[i] && requiredParts[i] != null) parts[idx++] = requiredParts[i];
        }
        return parts;
    }

    // ---- Rendering ----

    public void render(SpriteBatch batch) {
        // Draw 3×3 grid cells with animation offsets
        for (int i = 0; i < 9; i++) {
            // Skip the fading-in cell here — draw it separately with alpha
            if (phase == Phase.FADE_IN && i == fadeInIdx) continue;

            float drawX = getCellX(i);
            float drawY = getCellY(i);

            ItemType item = restored[i] ? displayPieces[i] : requiredParts[i];
            if (item != null) {
                TextureRegion icon = icons[item.spriteIndex];
                if (icon != null) {
                    batch.draw(icon, drawX, drawY, 16, 16);
                }
            }
        }

        // Fade-in the newly restored core piece
        if (phase == Phase.FADE_IN && fadeInIdx >= 0) {
            float alpha = Math.min(phaseTimer / FADE_IN_TIME, 1f);
            float drawX = getCellX(fadeInIdx);
            float drawY = getCellY(fadeInIdx);
            TextureRegion icon = icons[displayPieces[fadeInIdx].spriteIndex];
            if (icon != null) {
                batch.setColor(1, 1, 1, alpha);
                batch.draw(icon, drawX, drawY, 16, 16);
                batch.setColor(Color.WHITE);
            }
        }

        // Flying delivery item and lightning rendered in renderOverlay() — after HUD

        // Red flash is rendered by Hud (draws after game objects)
    }

    /** Render flying delivery item and lightning — call AFTER HUD so it appears on top. */
    public void renderOverlay(SpriteBatch batch) {
        if (phase == Phase.DELIVER || phase == Phase.LIGHTNING) {
            if (matchedPart != null) {
                TextureRegion icon = icons[matchedPart.spriteIndex];
                if (icon != null) {
                    batch.draw(icon, deliverX, deliverY, 16, 16);
                }
            }
        }
        if (phase == Phase.LIGHTNING) {
            renderLightning(batch);
        }
    }

    private void renderLightning(SpriteBatch batch) {
        float cellX = getCellX(matchedGridIdx);
        float cellY = getCellY(matchedGridIdx);
        float cellCenterX = cellX + 8;
        float cellCenterY = cellY + 8;
        float deliverCenterX = deliverX + 8;
        float deliverCenterY = deliverY + 8;

        float arcX = Math.min(cellCenterX, deliverCenterX);
        float arcW = Math.abs(deliverCenterX - cellCenterX);
        float arcY = Math.min(cellCenterY, deliverCenterY) - 4;
        float arcH = Math.abs(deliverCenterY - cellCenterY) + 8;
        if (arcW < 4) arcW = 4;
        if (arcH < 8) arcH = 8;

        float pixelSize = Gdx.graphics.getHeight() / 144f;

        batch.flush();
        batch.setShader(assets.lightningShader);
        assets.lightningShader.setUniformf("u_time", totalTime);
        assets.lightningShader.setUniformf("u_pixelSize", pixelSize);
        batch.setColor(1, 1, 1, 1);
        batch.draw(assets.whitePixel, arcX, arcY, arcW, arcH);
        batch.flush();
        batch.setShader(null);
        batch.setColor(Color.WHITE);
    }
}
