package northern.captain.starquake.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;

/**
 * Unified input manager — merges keyboard, game controller, and touch into
 * a single set of action flags. Event-driven: receives callbacks, sets state,
 * game logic reads state each frame.
 *
 * Call {@link #update()} at the END of each frame to clear justPressed flags.
 */
public class InputManager {

    public enum Action {
        LEFT, RIGHT, UP, DOWN, ACTION_A, ACTION_B
    }

    private static final int COUNT = Action.values().length;

    private final boolean[] pressed     = new boolean[COUNT];
    private final boolean[] justPressed = new boolean[COUNT];

    private final KeyboardListener keyboardListener = new KeyboardListener();
    private final GamepadListener  gamepadListener  = new GamepadListener();

    /** Returns the InputProcessor for keyboard events. Register via InputMultiplexer. */
    public InputAdapter getKeyboardListener() {
        return keyboardListener;
    }

    /** Returns the ControllerListener for gamepad events. */
    public ControllerAdapter getGamepadListener() {
        return gamepadListener;
    }

    /** Called by TouchControls when a touch zone is pressed/released. */
    public void setTouchState(Action action, boolean down) {
        int i = action.ordinal();
        if (down && !pressed[i]) justPressed[i] = true;
        pressed[i] = down;
    }

    public boolean isPressed(Action action) {
        return pressed[action.ordinal()];
    }

    public boolean isJustPressed(Action action) {
        return justPressed[action.ordinal()];
    }

    /** Call at end of each frame to clear one-shot flags. */
    public void update() {
        for (int i = 0; i < COUNT; i++)
            justPressed[i] = false;
    }

    // --- internal: set/clear from events ---

    private void press(Action action) {
        int i = action.ordinal();
        if (!pressed[i]) justPressed[i] = true;
        pressed[i] = true;
    }

    private void release(Action action) {
        pressed[action.ordinal()] = false;
    }

    // ---- Keyboard ----

    private class KeyboardListener extends InputAdapter {
        @Override
        public boolean keyDown(int keycode) {
            Action a = mapKey(keycode);
            if (a != null) { press(a); return true; }
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            Action a = mapKey(keycode);
            if (a != null) { release(a); return true; }
            return false;
        }

        private Action mapKey(int keycode) {
            switch (keycode) {
                case Input.Keys.LEFT:  return Action.LEFT;
                case Input.Keys.RIGHT: return Action.RIGHT;
                case Input.Keys.UP:    return Action.UP;
                case Input.Keys.DOWN:  return Action.DOWN;
                case Input.Keys.Z:     return Action.ACTION_A;
                case Input.Keys.X:     return Action.ACTION_B;
                default: return null;
            }
        }
    }

    // ---- Game Controller ----

    private class GamepadListener extends ControllerAdapter {
        private static final float DEAD_ZONE = 0.4f;

        @Override
        public boolean buttonDown(Controller controller, int buttonIndex) {
            Action a = mapButton(controller, buttonIndex);
            if (a != null) { press(a); return true; }
            return false;
        }

        @Override
        public boolean buttonUp(Controller controller, int buttonIndex) {
            Action a = mapButton(controller, buttonIndex);
            if (a != null) { release(a); return true; }
            return false;
        }

        @Override
        public boolean axisMoved(Controller controller, int axisIndex, float value) {
            ControllerMapping m = controller.getMapping();
            if (axisIndex == m.axisLeftX) {
                release(Action.LEFT);
                release(Action.RIGHT);
                if (value < -DEAD_ZONE) press(Action.LEFT);
                else if (value > DEAD_ZONE) press(Action.RIGHT);
                return true;
            }
            if (axisIndex == m.axisLeftY) {
                release(Action.UP);
                release(Action.DOWN);
                if (value < -DEAD_ZONE) press(Action.UP);
                else if (value > DEAD_ZONE) press(Action.DOWN);
                return true;
            }
            return false;
        }

        private Action mapButton(Controller controller, int buttonIndex) {
            ControllerMapping m = controller.getMapping();
            if (buttonIndex == m.buttonDpadLeft)  return Action.LEFT;
            if (buttonIndex == m.buttonDpadRight) return Action.RIGHT;
            if (buttonIndex == m.buttonDpadUp)    return Action.UP;
            if (buttonIndex == m.buttonDpadDown)  return Action.DOWN;
            if (buttonIndex == m.buttonA)          return Action.ACTION_A;
            if (buttonIndex == m.buttonB)          return Action.ACTION_B;
            return null;
        }
    }

    /** Register this manager's gamepad listener with all connected controllers. */
    public void connectControllers() {
        Controllers.addListener(gamepadListener);
    }

    public void disconnectControllers() {
        Controllers.removeListener(gamepadListener);
    }
}
