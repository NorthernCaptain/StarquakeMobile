package northern.captain.starquake.event;

public class GameOverEvent extends GameEvent {
    public final boolean win;

    public GameOverEvent(boolean win) {
        super(Type.GAME_OVER);
        this.win = win;
    }
}
