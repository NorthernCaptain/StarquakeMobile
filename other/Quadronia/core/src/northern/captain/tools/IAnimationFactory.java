package northern.captain.tools;

import com.badlogic.gdx.scenes.scene2d.Action;

public interface IAnimationFactory
{
	/**
	 * Creates new action or sequence of actions and return it
	 * @return
	 */
	Action create(float delayAtStart);
	Action create(float delayAtStart, Action onFinishAction);
}
