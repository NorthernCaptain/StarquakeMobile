package northern.captain.starquake.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * Singleton sound effect manager. Loads all game sounds, handles playback
 * with volume/mute control.
 *
 * Usage: SoundManager.play(SoundType.FIRE_WALK);
 */
public class SoundManager {
    private static SoundManager instance;

    public enum SoundType {
        STEP,
        FIRE_WALK,
        FIRE_FLY,
        DEATH,
        PLATFORM,
        ELECTRIC,
        EXPLOSION,
        TELEPORT,        // teleport transition (disintegrate/reassemble)
        TELEPORT_ENTER,  // entering teleport booth (overlay)
        ACCESS_OK,       // space lock / door unlock
        ACCESS_DENIED,   // no card / no key
        PICKUP_ITEM,     // inventory item (core part, key, card)
        PICKUP_ENERGY,   // health boost
        PICKUP_PLATFORM, // platform boost
        PICKUP_AMMO,     // laser boost
        PICKUP_MULTI,    // universal boost / extra life
        SPAWN,           // blob spawn/respawn
        UI_TEXT,         // typewriter text tick
        CORE_A,          // core delivery sequence phases
        CORE_B,
        CORE_C,
        GAME_OVER
    }

    private static final String SFX_PATH = "audio/sfx/";

    private final Sound[] sounds = new Sound[SoundType.values().length];
    private boolean enabled = true;
    private float volume = 1f;

    public static void init() {
        if (instance != null) instance.dispose();
        instance = new SoundManager();
    }

    public static SoundManager get() {
        return instance;
    }

    private SoundManager() {
        load(SoundType.STEP,            "blob_step.mp3");
        load(SoundType.FIRE_WALK,       "sfx_01_game_blob_fire_walk.mp3");
        load(SoundType.FIRE_FLY,        "sfx_00_game_blob_fire_fly.mp3");
        load(SoundType.DEATH,           "sfx_08_game_blob_death.mp3");
        load(SoundType.PLATFORM,        "sfx_06_game_platform_place.mp3");
        load(SoundType.ELECTRIC,        "electic.mp3");
        load(SoundType.EXPLOSION,       "sfx_11_game_explosion_burst.mp3");
        load(SoundType.TELEPORT,        "sfx_10_game_teleport_console.mp3");
        load(SoundType.TELEPORT_ENTER,  "teleport.mp3");
        load(SoundType.ACCESS_OK,       "sfx_12_game_access_authorised.mp3");
        load(SoundType.ACCESS_DENIED,   "sfx_13_game_access_denied.mp3");
        load(SoundType.PICKUP_ITEM,     "sfx_14_game_inventory_pickup.mp3");
        load(SoundType.PICKUP_ENERGY,   "sfx_17_game_energy_pickup.mp3");
        load(SoundType.PICKUP_PLATFORM, "sfx_18_game_platform_pickup.mp3");
        load(SoundType.PICKUP_AMMO,     "sfx_19_game_ammo_pickup.mp3");
        load(SoundType.PICKUP_MULTI,    "sfx_20_game_multi_pickup.mp3");
        load(SoundType.SPAWN,           "sfx_24_game_player_spawn.mp3");
        load(SoundType.UI_TEXT,         "sfx_16_game_ui_text_input.mp3");
        load(SoundType.CORE_A,          "sfx_26_game_core_sequence_a.mp3");
        load(SoundType.CORE_B,          "sfx_27_game_core_sequence_b.mp3");
        load(SoundType.CORE_C,          "sfx_28_game_core_sequence_c.mp3");
        load(SoundType.GAME_OVER,       "sfx_29_game_over_scoreboard.mp3");
    }

    private void load(SoundType type, String filename) {
        try {
            sounds[type.ordinal()] = Gdx.audio.newSound(Gdx.files.internal(SFX_PATH + filename));
        } catch (Exception e) {
            Gdx.app.error("SoundManager", "Failed to load " + filename, e);
        }
    }

    public static void play(SoundType type) {
        if (instance != null) instance.playInternal(type, instance.volume);
    }

    public static void play(SoundType type, float vol) {
        if (instance != null) instance.playInternal(type, vol * instance.volume);
    }

    private void playInternal(SoundType type, float vol) {
        if (!enabled) return;
        Sound sound = sounds[type.ordinal()];
        if (sound != null) {
            sound.play(vol);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0, Math.min(1, volume));
    }

    public float getVolume() {
        return volume;
    }

    public void dispose() {
        for (Sound s : sounds) {
            if (s != null) s.dispose();
        }
        instance = null;
    }
}
