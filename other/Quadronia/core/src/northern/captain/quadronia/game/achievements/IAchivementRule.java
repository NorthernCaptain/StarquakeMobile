package northern.captain.quadronia.game.achievements;

/**
 * Created by leo on 21.08.15.
 */
public interface IAchivementRule
{
    /**
     * The key describing game type, mode and achievement type this rule is applied to
     * @return
     */
    int getKey();

    /**
     * Type of achievement that this rule can produce
     * @return
     */
    int getAchievementType();

    boolean processEvent(int eventType, int... params);
}
