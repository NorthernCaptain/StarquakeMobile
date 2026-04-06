package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.utils.Pool;

import northern.captain.gamecore.glx.NContext;
import northern.captain.tools.IAnimationFactory;

public class Animations
{
    private static boolean actionsRegistered = false;

    /**
     * Register custom action classes with the libGDX Actions pool.
     * Must be called early during game initialization (before using any actions).
     * Required for libGDX 1.14+ to avoid reflection.
     */
    public static void registerActions() {
        if (actionsRegistered) return;
        actionsRegistered = true;
        Actions.registerAction(MoveDeltaAction::new);
        Actions.registerAction(ScaleDeltaAction::new);
        Actions.registerAction(SeqAction::new);
        Actions.registerAction(TextDigitsChangeAction::new);
    }

	public static class AlwaysOne extends Interpolation
	{
		@Override
		public float apply(float a)
		{
			return 1;
		}		
	}
	
	public static final AlwaysOne ALWAYS_ONE = new AlwaysOne();
	
	public static final float MIN_DUR = 0.01f;

    public static class Spin60 implements IAnimationFactory
    {
        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(Actions.scaleTo(1.3f, 1.3f, 0.2f));
            action.addAction
                (
                    Actions.rotateBy(60, 0.5f, Interpolation.swingOut)
                );
            action.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f));
            action.addAction(Actions.scaleTo(1.3f, 1.3f, 0.2f));
            action.addAction
                (
                    Actions.rotateBy(60, 0.5f, Interpolation.swingOut)
                );
            action.addAction(Actions.scaleTo(1.0f, 1.0f, 0.2f));

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class HeliMove implements IAnimationFactory
    {
        float toX, toY;

        public HeliMove(float x, float y)
        {
            toX = x;
            toY = y;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(Actions.scaleTo(1.3f, 1.3f, 0.2f));
            action.addAction
                (
                    Actions.parallel(
                        Actions.rotateBy(720, 0.8f, Interpolation.swingOut),
                        Actions.moveTo(toX, toY, 0.8f, Interpolation.exp5In))
                );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class FadeZeroFull implements IAnimationFactory
    {
        float duration = 0.8f;

        public FadeZeroFull() {}
        public FadeZeroFull(float duration)
        {
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq(
                        Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction
                   (
                    Actions.fadeIn(duration)
                   );
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class FadeToFull implements IAnimationFactory
    {
        float duration = 0.8f;
        float from = 0.8f;

        public FadeToFull() {}
        public FadeToFull(float duration, float from)
        {
            this.duration = duration;
            this.from = from;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq(
                            Actions.alpha(from, MIN_DUR, ALWAYS_ONE)
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction
                    (
                            Actions.alpha(1.0f, duration)
                    );
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class FadeFullZero implements IAnimationFactory
    {
        float duration = 0.8f;

        public FadeFullZero()
        {

        }

        public  FadeFullZero(float duration)
        {
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction
                    (
                            Actions.fadeOut(duration)
                    );
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class Rotate implements IAnimationFactory
    {
        float duration = 0.8f;
        float angle = 360;
        float moveX = 0;

        public Rotate()
        {

        }

        public  Rotate(float duration, float angle, float moveX)
        {
            this.duration = duration;
            this.angle = angle;
            this.moveX = moveX;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq();

            action.addAction(Actions.fadeOut(MIN_DUR, ALWAYS_ONE));
            action.addAction(Actions.moveBy(-moveX, 0, MIN_DUR, ALWAYS_ONE));

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(Actions.fadeIn(MIN_DUR, ALWAYS_ONE));
            action.addAction
                    (
                            Actions.parallel(
                                Actions.rotateBy(angle, duration),
                                Actions.moveBy(moveX, 0, duration)
                            )
                    );
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class RotateNoHide implements IAnimationFactory
    {
        float duration = 0.8f;
        float angle = 360;
        float moveX = 0;

        public RotateNoHide()
        {

        }

        public  RotateNoHide(float duration, float angle, float moveX)
        {
            this.duration = duration;
            this.angle = angle;
            this.moveX = moveX;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction
                    (
                            Actions.parallel(
                                    Actions.rotateBy(angle, duration),
                                    Actions.moveBy(moveX, 0, duration)
                            )
                    );
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static Action getScreenFadeIn()
	{
		return seq(//Actions.fadeOut(MIN_DUR, ALWAYS_ONE),
				Actions.fadeIn(0.7f));
	}
	
	public static Action getScreenFadeOut(float delayBefore, Runnable runAfter)
	{
		return seq(Actions.delay(delayBefore), Actions.fadeOut(0.7f), Actions.run(runAfter));
	}

    public static Action getScreenFadeOut(Runnable runAfter)
    {
        return seq(Actions.fadeOut(0.7f), Actions.run(runAfter));
    }

    public static class MoveInFromOutsideLeft implements IAnimationFactory
	{
        float deltaX = NContext.current.screenWidth;

        public MoveInFromOutsideLeft()
        {

        }

        public MoveInFromOutsideLeft(float deltaX)
        {
            this.deltaX = deltaX;
        }

		@Override
		public SequenceAction create(float delayAtStart)
		{
			float scaleF = 0.99f;
			SequenceAction action =
				seq(
                        Actions.fadeOut(MIN_DUR, ALWAYS_ONE),
    					Animations.moveDelta(-deltaX, 0, MIN_DUR, ALWAYS_ONE),
    					Animations.scaleDelta(-scaleF, MIN_DUR, ALWAYS_ONE)
					);
			
			if(delayAtStart > 0.0f)
			{
				action.addAction(Actions.delay(delayAtStart));
			}
			
			action.addAction(
                    Actions.fadeIn(MIN_DUR, ALWAYS_ONE));
            action.addAction(
					Actions.parallel(
							Animations.moveDelta(deltaX, 0, 1.8f, Interpolation.elasticOut),
							Animations.scaleDelta(scaleF, 0.3f)
							)
						);
			
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}
		
	}

    public static class TVScaleUp implements IAnimationFactory
    {
        float totalSec;
        public TVScaleUp(float totalSec)
        {
            this.totalSec = totalSec;
        }

        @Override
        public SequenceAction create(float delta)
        {
            SequenceAction action =
                    seq();

            action.addAction(
                    Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Actions.scaleTo(1f, 0.02f, MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Actions.parallel(
                            Actions.fadeIn(totalSec*0.1f),
                            Actions.scaleTo(1f, 1f, totalSec, Interpolation.swingOut)
                    )
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class TVScaleDown implements IAnimationFactory
    {
        float totalSec;
        public TVScaleDown(float totalSec)
        {
            this.totalSec = totalSec;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Actions.parallel(
                            seq(Actions.delay(totalSec * 0.9f), Actions.fadeOut(totalSec * 0.1f)),
                            Actions.scaleTo(1f, 0.02f, totalSec, Interpolation.swingIn)
                    )
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ScaleZeroFull implements IAnimationFactory
    {
        @Override
        public SequenceAction create(float delayAtStart)
        {
            float deltaX = NContext.current.screenWidth;
            float scaleF = 0.8f;
            SequenceAction action =
                    seq(
                          Animations.scaleDelta(-scaleF, MIN_DUR, ALWAYS_ONE)
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                          Animations.scaleDelta(scaleF, 0.4f)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ScaleZeroFullElastic implements IAnimationFactory
    {
        float scaleF = 0.8f;
        boolean doPrescale = true;
        float duration = 0.6f;

        public ScaleZeroFullElastic(float scale)
        {
            scaleF = scale;
        }

        public ScaleZeroFullElastic(float scale, float duration, boolean noPrescale)
        {
            scaleF = scale;
            doPrescale = !noPrescale;
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();

            if(doPrescale)
            {
                action.addAction(
                                    Animations.scaleDelta(-scaleF, MIN_DUR, ALWAYS_ONE)
                                );
            }

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                            Animations.scaleDelta(scaleF, duration, Interpolation.swingOut)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }


    public static class ScaleFullZeroAndBackElastic implements IAnimationFactory
    {
        float scaleF = 0.8f;
        float duration = 0.6f;
        float delayDuration = 1f;

        public ScaleFullZeroAndBackElastic(float scale)
        {
            scaleF = scale;
        }

        public ScaleFullZeroAndBackElastic(float scale, float delayDuration)
        {
            scaleF = scale;
            this.delayDuration = delayDuration;
        }

        public ScaleFullZeroAndBackElastic(float scale, float middleDelayDuration, float duration)
        {
            scaleF = scale;
            this.delayDuration = middleDelayDuration;
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Animations.scaleDelta(-scaleF, duration, Interpolation.swingIn)
            );
            action.addAction(Actions.delay(delayDuration));
            action.addAction(
                    Animations.scaleDelta(scaleF, duration, Interpolation.swingOut)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ScaleFullZeroAndBackAbs implements IAnimationFactory
    {
        float scaleF = 0.8f;
        float duration = 0.6f;
        float delayDuration = 1f;

        public ScaleFullZeroAndBackAbs(float scale)
        {
            scaleF = scale;
        }

        public ScaleFullZeroAndBackAbs(float scale, float delayDuration)
        {
            scaleF = scale;
            this.delayDuration = delayDuration;
        }

        public ScaleFullZeroAndBackAbs(float scale, float middleDelayDuration, float duration)
        {
            scaleF = scale;
            this.delayDuration = middleDelayDuration;
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Actions.scaleTo(scaleF, scaleF, duration, Interpolation.pow2In)
            );
            action.addAction(Actions.delay(delayDuration));
            action.addAction(
                    Actions.scaleTo(1f, 1f, duration, Interpolation.pow2In)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class MoveFromAnywhere implements IAnimationFactory
    {
        float deltaX, deltaY;

        public MoveFromAnywhere(float deltaX, float deltaY)
        {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq(
                            Actions.parallel(
                                    Animations.moveDelta(-deltaX, -deltaY, MIN_DUR, ALWAYS_ONE),
                                    Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
                            )
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Actions.parallel(
                            Actions.fadeIn(MIN_DUR, ALWAYS_ONE),
                            Animations.moveDelta(deltaX, deltaY, 1.4f, Interpolation.pow3Out)
                    )
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class MoveToAnywhereAndBack implements IAnimationFactory
    {
        float deltaX, deltaY;

        public MoveToAnywhereAndBack(float deltaX, float deltaY)
        {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq(
                         Animations.moveDelta(-deltaX, -deltaY, 0.6f, Interpolation.swingIn)
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                            Animations.moveDelta(deltaX, deltaY, 0.8f, Interpolation.pow3Out)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ScaleMoveHugeFull implements IAnimationFactory
    {
        float scaleVal;
        float deltaX, deltaY;

        public ScaleMoveHugeFull(float scaleVal, float deltaX, float deltaY)
        {
            this.scaleVal = scaleVal;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq(
                            Actions.parallel(
                                    Animations.scaleDelta(scaleVal, MIN_DUR, ALWAYS_ONE),
                                    Animations.moveDelta(deltaX, deltaY, MIN_DUR, ALWAYS_ONE),
                                    Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
                            )
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Actions.parallel(
                            Actions.fadeIn(MIN_DUR, ALWAYS_ONE),
                            Animations.scaleDelta(-scaleVal, 0.5f, Interpolation.pow3Out),
                            Animations.moveDelta(-deltaX, -deltaY, 0.4f, Interpolation.pow3Out)
                    )
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ScaleMoveAbs implements IAnimationFactory
    {
        float scaleVal;
        float fromY, fromX, toX, toY;

        public ScaleMoveAbs(float scaleVal, float fromX, float fromY, float toX, float toY)
        {
            this.scaleVal = scaleVal;
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq(
                            Actions.parallel(
                                    Actions.scaleTo(scaleVal, scaleVal, MIN_DUR, ALWAYS_ONE),
                                    Actions.moveTo(fromX, fromY, MIN_DUR, ALWAYS_ONE),
                                    Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
                            )
                    );

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Actions.parallel(
                            Actions.fadeIn(0.2f),
                            Actions.scaleTo(1, 1, 0.7f, Interpolation.swingOut),
                            Actions.moveTo(toX, toY, 0.6f, Interpolation.pow3Out)
                    )
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }


    public static class MoveOutsideRight implements IAnimationFactory
	{
		@Override
		public SequenceAction create(float delayAtStart)
		{
			float deltaX = NContext.current.screenWidth;
			SequenceAction action =
				seq();
			
			if(delayAtStart > 0.0f)
			{
				action.addAction(Actions.delay(delayAtStart));
			}
			
			action.addAction(					
							Animations.moveDelta(deltaX, 0, 0.8f, Interpolation.pow2In)
							);
			
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}
		
	}

    static public final Interpolation bounceOut = new Interpolation.BounceOut(3);


	public static class DropDownOffScreen implements IAnimationFactory
	{
		@Override
		public SequenceAction create(float delayAtStart)
		{
			float deltaX = NContext.current.screenHeight;
			SequenceAction action =
				seq();
			
			action.addAction(
					Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
			);
			action.addAction(
							Animations.moveDelta(0, deltaX, MIN_DUR, ALWAYS_ONE)
					);
            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
			action.addAction(
					Actions.fadeIn(MIN_DUR, ALWAYS_ONE)
			);
			action.addAction(
									Animations.moveDelta(0, -deltaX, 1f, bounceOut)
							);
			
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}
		
	}

    public static class DropDownOffScreenPower3 implements IAnimationFactory
    {
        float deltaY = 0;

        public DropDownOffScreenPower3()
        {

        }

        public DropDownOffScreenPower3(float deltaY)
        {
            this.deltaY = deltaY;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            if(deltaY <= 0)
                deltaY = NContext.current.screenHeight;

            SequenceAction action =
                    seq();

            action.addAction(
                    Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Animations.moveDelta(0, deltaY, MIN_DUR, ALWAYS_ONE)
            );
            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(
                    Actions.fadeIn(MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Animations.moveDelta(0, -deltaY, 1f, Interpolation.pow3Out)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class PullUpOffScreenPower3 implements IAnimationFactory
    {
        float deltaY = 0;

        public PullUpOffScreenPower3()
        {

        }

        public PullUpOffScreenPower3(float deltaY)
        {
            this.deltaY = deltaY;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            if(deltaY <= 0)
                deltaY = NContext.current.screenHeight;

            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(
                    Actions.fadeIn(MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Animations.moveDelta(0, deltaY, 1f, Interpolation.pow3Out)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class DropDownOutOfScreen implements IAnimationFactory
	{
		@Override
		public SequenceAction create(float delayAtStart)
		{
			float deltaX = NContext.current.screenHeight;
			SequenceAction action =
				seq();
			
			if(delayAtStart > 0.0f)
			{
				action.addAction(Actions.delay(delayAtStart));
			}
			action.addAction(
							Actions.parallel(									
									Animations.moveDelta(0, -deltaX, 0.6f, Interpolation.swingIn)
										)
							);
			
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}
		
	}

    public static class PopUpOutOfScreen implements IAnimationFactory
    {
        @Override
        public SequenceAction create(float delayAtStart)
        {
            float deltaX = NContext.current.screenHeight;
            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(
                    Actions.fadeOut(MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Animations.moveDelta(0, -deltaX, MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Actions.fadeIn(MIN_DUR, ALWAYS_ONE)
            );
            action.addAction(
                    Animations.moveDelta(0, deltaX, 0.8f, Interpolation.swingOut)
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class MoveOutsideLeft implements IAnimationFactory
	{
		@Override
		public SequenceAction create(float delayAtStart)
		{
			float deltaX = NContext.current.screenWidth;
			SequenceAction action =
				seq();
			
			if(delayAtStart > 0.0f)
			{
				action.addAction(Actions.delay(delayAtStart));
			}
			
			action.addAction(
							Actions.parallel(									
									Animations.moveDelta(-deltaX, 0, 0.6f, Interpolation.swingIn),
									Actions.sequence(
											Actions.delay(0.3f),
											Animations.scaleDelta(-0.3f, 0.5f)
											)
										)
							);
			
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}
		
	}

    public static class MoveOutsideRightSwing implements IAnimationFactory
    {
        @Override
        public SequenceAction create(float delayAtStart)
        {
            float deltaX = NContext.current.screenWidth;
            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(
                    Actions.parallel(
                            Animations.moveDelta(deltaX, 0, 0.8f, Interpolation.swingIn),
                            Actions.sequence(
                                    Actions.delay(0.3f),
                                    Animations.scaleDelta(-0.3f, 0.5f)
                            )
                    )
            );

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ShakeLeftRight implements IAnimationFactory
    {
        float deltaX = NContext.current.screenWidth;
        float duration = 0.3f;

        public ShakeLeftRight()
        {

        }

        public ShakeLeftRight(float delta, float duration)
        {
            deltaX = delta;
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action =
                    seq();

            if(delayAtStart > 0.0f)
            {
                action.addAction(Actions.delay(delayAtStart));
            }

            action.addAction(Animations.moveDelta(-deltaX, 0, duration/6f));
//            action.addAction(Animations.moveDelta(2*deltaX, 0, duration/3));
            action.addAction(Animations.moveDelta(deltaX, 0, duration, Interpolation.elasticOut));

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }

    }

    public static class ScaleUp13 implements IAnimationFactory
	{

		@Override
		public SequenceAction create(float delayAtStart)
		{
			float scale = 1.2f;
			SequenceAction action = seq();
			if(delayAtStart > 0)
			{
				action.addAction(Actions.delay(delayAtStart));
			}
			action.addAction(Actions.scaleTo(scale, scale, 0.2f, Interpolation.pow3Out));
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}		
	}
	
	public static class ScaleDown1 implements IAnimationFactory
	{

		@Override
		public SequenceAction create(float delayAtStart)
		{
			float scale = 1f;
			SequenceAction action = seq();
			if(delayAtStart > 0)
			{
				action.addAction(Actions.delay(delayAtStart));
			}
			action.addAction(Actions.scaleTo(scale, scale, 0.25f, Interpolation.pow3Out));
			return action;
		}

		@Override
		public SequenceAction create(float delayAtStart, Action onFinishAction)
		{
			SequenceAction action = create(delayAtStart);
			action.addAction(onFinishAction);
			return action;
		}		
	}


    public static class WaveScale2x implements IAnimationFactory
    {

        @Override
        public SequenceAction create(float delayAtStart)
        {
            float scale = 2f;
            SequenceAction action = seq();
            if(delayAtStart > 0)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(Actions.scaleTo(scale, scale, 0.3f, Interpolation.pow3Out));
            action.addAction(Actions.scaleTo(1, 1, 0.3f, Interpolation.pow3Out));
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }
    }

    public static class WaveScaleWithDelay implements IAnimationFactory
    {
    	private float topDelay;
    	private float scale;
    	
    	public WaveScaleWithDelay(float scale, float topDelay)
    	{
    		this.topDelay = topDelay;
    		this.scale = scale;
    	}

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();
            if(delayAtStart > 0)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(Actions.scaleTo(scale, scale, 0.3f, Interpolation.pow3Out));
            action.addAction(Actions.delay(topDelay));
            action.addAction(Actions.scaleTo(1, 1, 0.3f, Interpolation.pow3Out));
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }
    }

    public static class MoveDeltaXParam implements IAnimationFactory
    {
        float value;
        public MoveDeltaXParam(float value)
        {
            this.value = value;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();
            if(delayAtStart > 0)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(Animations.moveDelta(value, 0, 0.6f, Interpolation.pow3Out));
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }
    }

    public static class MoveToXYParam implements IAnimationFactory
    {
        float valueX, valueY;
        float duration = 0.7f;

        public MoveToXYParam(float valueX, float valueY)
        {
            this.valueX = valueX;
            this.valueY = valueY;
        }

        public MoveToXYParam(float valueX, float valueY, float duration)
        {
            this(valueX, valueY);
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();
            if(delayAtStart > 0)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(Actions.moveTo(valueX, valueY, duration, Interpolation.pow3Out));
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }
    }

    public static class RotateCircle implements IAnimationFactory
    {
        float degrees, timeSec;
        public RotateCircle(float degree, float time)
        {
            degrees = degree;
            timeSec = time;
        }

        @Override
        public SequenceAction create(float delayAtStart)
        {
            SequenceAction action = seq();
            if(delayAtStart > 0)
            {
                action.addAction(Actions.delay(delayAtStart));
            }
            action.addAction(Actions.forever(Actions.rotateBy(degrees, timeSec)));
            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }
    }

    public static class DigitsRotator implements IAnimationFactory
    {
        private float duration;

        public DigitsRotator(float duration)
        {
            this.duration = duration;
        }

        @Override
        public SequenceAction create(float endValue)
        {
            SequenceAction action = seq();
            TextDigitsChangeAction changeAction = textDigitsChangeAction(duration, Interpolation.pow2In);
            changeAction.setEndValue((int)endValue);

            action.addAction(changeAction);

            return action;
        }

        @Override
        public SequenceAction create(float delayAtStart, Action onFinishAction)
        {
            SequenceAction action = create(delayAtStart);
            action.addAction(onFinishAction);
            return action;
        }
    }




    //========================== Actions ============================
	
	public static class MoveDeltaAction extends TemporalAction {
		private float startX, startY;
		private float deltaX, deltaY;

		protected void begin () {
			startX = actor.getX();
			startY = actor.getY();
		}

		protected void update (float percent) 
		{
			actor.setPosition(startX + deltaX * percent, startY + deltaY * percent);
		}

		public void setPosition (float x, float y) {
			deltaX = x;
			deltaY = y;
		}

		public float getX () {
			return deltaX;
		}

		public void setX (float x) {
			deltaX = x;
		}

		public float getY () {
			return deltaY;
		}

		public void setY (float y) {
			deltaY = y;
		}
	}

	static public MoveDeltaAction moveDelta (float x, float y, float duration) 
	{
		return moveDelta(x, y, duration, null);
	}

	static public MoveDeltaAction moveDelta (float x, float y, float duration, Interpolation interpolation) {
		MoveDeltaAction action = Actions.action(MoveDeltaAction.class);
		action.setPosition(x, y);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

	public static class ScaleDeltaAction extends TemporalAction
    {
		private float startX, startY;
		private float deltaX, deltaY;

		protected void begin () {
			startX = actor.getScaleX();
			startY = actor.getScaleY();
		}

		protected void update (float percent) 
		{
			actor.setScale(startX + deltaX * percent, startY + deltaY * percent);
		}

		public void setScale (float scale) 
		{
			deltaX = scale;
			deltaY = scale;
		}

		public float getScaleX () {
			return deltaX;
		}

		public void setScaleX (float x) {
			deltaX = x;
		}

		public float getScaleY () {
			return deltaY;
		}

		public void setScaleY (float y) {
			deltaY = y;
		}
	}

	static public ScaleDeltaAction scaleDelta (float scale, float duration) 
	{
		return scaleDelta(scale, duration, null);
	}

	static public ScaleDeltaAction scaleDelta (float scale, float duration, Interpolation interpolation) {
		ScaleDeltaAction action = Actions.action(ScaleDeltaAction.class);
		action.setScale(scale);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}
	
	static public ScaleDeltaAction scaleDelta (float scaleX, float scaleY, float duration, Interpolation interpolation) {
		ScaleDeltaAction action = Actions.action(ScaleDeltaAction.class);
		action.setScaleX(scaleX);
		action.setScaleY(scaleY);
		action.setDuration(duration);
		action.setInterpolation(interpolation);
		return action;
	}

    public static class SeqAction extends SequenceAction
    {
        private int myComplete = 0;
        private int startScreenId = 0;

        public SeqAction()
        {
            start();
        }

        @Override
        public boolean act (float delta)
        {
            if(myComplete == 0)
            {
                start();
            }
            boolean ret = super.act(delta);
            if(ret && myComplete == 1)
            {
                finish();
            }
            return ret;
        }

        private void start()
        {
//            Log.i("ncgamer", "SEQSTA start " + this + this.hashCode());
            if(myComplete == 0)
            {
                myComplete = 1;
                if(!NContext.current.willRefresh())
                    NContext.current.resetLastNano();
//                Log.i("ncgamer", "SEQSTA do add " + this + this.hashCode());
                NContext.current.addRefresh();
            }
        }

        private void finish()
        {
            if(myComplete == 1)
            {
                myComplete = 2;
//                Log.i("ncgamer", "SEQFIN do sub " + this + this.hashCode());
                NContext.current.subRefresh();
            }
        }

        @Override
        public void restart ()
        {
//            Log.i("ncgamer", "SEQRST restart " + this + this.hashCode());
            super.restart();
            finish();
            myComplete = 0;
            if(!NContext.current.willRefresh())
                NContext.current.resetLastNano();

            NContext.current.addTempRefresh();
//            start();
        }

        @Override
        public void reset()
        {
//            Log.i("ncgamer", "SEQRST reset " + this + this.hashCode());
            super.reset();
            finish();
        }

        @Override
        public void setActor(Actor actor)
        {
            super.setActor(actor);
            if(actor == null)
            {
//                Log.i("ncgamer", "SEQACT null " + this + this.hashCode());
                finish();
            }
        }
    }

    static public SeqAction seq (Action action1)
    {
        SeqAction action = Actions.action(SeqAction.class);
        action.addAction(action1);
        return action;
    }

    static public SeqAction seq (Action action1, Action action2)
    {
        SeqAction action = Actions.action(SeqAction.class);
        action.addAction(action1);
        action.addAction(action2);
        return action;
    }

    static public SeqAction seq (Action action1, Action action2, Action action3)
    {
        SeqAction action = Actions.action(SeqAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        return action;
    }

    static public SeqAction seq (Action action1, Action action2, Action action3, Action action4)
    {
        SeqAction action = Actions.action(SeqAction.class);
        action.addAction(action1);
        action.addAction(action2);
        action.addAction(action3);
        action.addAction(action4);
        return action;
    }

    static public SeqAction seq ()
    {
        return Actions.action(SeqAction.class);
    }

    public static class TextDigitsChangeAction extends TemporalAction
    {
        MyLabel changeLabel;
        int startValue;
        int endValue;
        boolean startValueSet = false;

        @Override
        protected void begin ()
        {
            changeLabel = (MyLabel)actor;
            if(!startValueSet)
            {
                startValue = Integer.parseInt(changeLabel.getText().toString());
            }
        }

        @Override
        protected void update (float percent)
        {
            int value = startValue + (int)(percent * (endValue - startValue));
            changeLabel.setText(Integer.toString(value));
        }

        /**
         * Called the last time {@link #act(float)} is called.
         */
        @Override
        protected void end()
        {
            changeLabel.setText(Integer.toString(endValue));
        }

        public void setEndValue(int endValue)
        {
            this.endValue = endValue;
        }

        public void setValues(int startValue, int endValue)
        {
            this.startValue = startValue;
            startValueSet = true;
            this.endValue = endValue;
        }
    }

    public static TextDigitsChangeAction textDigitsChangeAction(float duration, Interpolation interpolation)
    {
        TextDigitsChangeAction action = Actions.action(TextDigitsChangeAction.class);
        action.setDuration(duration);
        action.setInterpolation(interpolation);
        return action;
    }
}
