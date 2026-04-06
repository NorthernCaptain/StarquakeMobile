package northern.captain.tools;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

import northern.captain.gamecore.glx.ISoundMan;
import northern.captain.gamecore.glx.NCore;
import northern.captain.gamecore.glx.tools.ClickListenerPrepared;

import java.util.ArrayList;
import java.util.List;

/**
 * Animated set of buttons that we use for main menu
 * @author leo
 *
 */
public class AnimButtonSet implements Runnable
{
	public interface FindViewById
	{
		Actor findViewById(int id);
	}
	
	FindViewById finder;
	float deltaDelay = 0.1f;
	
	class AnimItem
	{
		Actor   srcView;
		int    tag;
		ClickListenerPrepared onClick;
		IAnimationFactory animateAfterInit;
		
		AnimItem(int itag, Actor iview, ClickListenerPrepared onC)
		{
			srcView = iview;
			tag = itag;
			onClick = onC;
		}

        float x, y;
        float scale;
	}

	List<AnimItem>   items = new ArrayList<AnimItem>();
	
	IAnimationFactory onPressAnim;
	IAnimationFactory onReleaseAnim;
	IAnimationFactory onClickedAnim;
	IAnimationFactory onOtherClickedAnim;
	IAnimationFactory onInitialAnim;
	
	int currentIdx = -1;
	
	public AnimButtonSet()
	{
		finder = new FindViewById()
		{
			
			@Override
			public Actor findViewById(int id)
			{
				return null;
			}
		};
	}
		
	public AnimButtonSet(FindViewById finder)
	{
		this.finder = finder;
	}
	
	public void setDeltaDelay(float valueMsec)
	{
		deltaDelay = valueMsec;
	}
	
	/**
	 * Adds a button to the set
	 * @param tag
	 * @param but
	 */
	public int add(int tag, final Actor but, ClickListenerPrepared onClick)
	{
		int idx = items.size();
		items.add(new AnimItem(tag, but, onClick));
		
		if(onClick != null)
		{
			but.addListener(new InputListener()
			{		
				
				/* (non-Javadoc)
				 * @see com.badlogic.gdx.scenes.scene2d.InputListener#touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
				 */
				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
				{
					processOnTouchDown(event.getListenerActor(), x, y);
					return true;
				}

				/* (non-Javadoc)
				 * @see com.badlogic.gdx.scenes.scene2d.InputListener#touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent, float, float, int, int)
				 */
				@Override
				public void touchUp(InputEvent event, float x, float y, int pointer, int button)
				{
					processOnTouchUp(event.getListenerActor(), x, y);
				}

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                {
                    processOnExit(event.getListenerActor(), x, y);
                    super.exit(event, x, y, pointer, toActor);
                }
            });
		}
		return idx;
	}
	
	
	private boolean animateClick = true;
	
	boolean processOnTouchDown(Actor view, float x, float y)
	{
		NCore.instance().getSoundman().playSound(ISoundMan.SND_JUST_CLICK, true);
		view.clearActions();
		view.addAction(onPressAnim.create(0));
        int currentIdx = findByView(view);
        doTouchDown(currentIdx);
		return true;
	}

    boolean processOnExit(Actor view, float x, float y)
    {
        int currentIdx = findByView(view);
        doExit(currentIdx);
        return true;
    }

	boolean processOnTouchUp(Actor view, float x, float y)
	{
		Actor hit = view.hit(x, y, true);
		if (hit == null || !hit.isDescendantOf(view))
		{
			view.clearActions();
			view.addAction(onReleaseAnim.create(0));
			return false;
		}
		
		currentIdx = findByView(view);
        boolean ret = prepareClicked();
        if(!ret)
        {
            view.clearActions();
            view.addAction(onReleaseAnim.create(0));
            return false;
        }

		if(animateClick && onClickedAnim != null)
		{
		    startClickedAnim(currentIdx);
		}
		else
        {
   			processClicked();
        }
		return true;
	}
		
	void startClickedAnim(int idx)
	{
		int len = items.size();
		float delay = 0;
		for(int i = 0; i< len; i++)
		{
			Actor view = items.get(i).srcView;
			if(i == idx)
			{
				view.clearActions();
                Action runnableA = Actions.run(this);
                runnableA.restart();
				view.addAction(onClickedAnim.create(0, runnableA));
			}
			else
			{
				if(onOtherClickedAnim == null)
				{
					view.clearActions();
					continue;
				}
				
				view.clearActions();
				view.addAction(onOtherClickedAnim.create(delay));
				delay += deltaDelay;
			}
		}
	}

    boolean doTouchDown(int currentIdx)
    {
        if(currentIdx >=0 && currentIdx < items.size())
        {
            AnimItem item = items.get(currentIdx);
            if(item.onClick != null)
            {
                InputEvent evt = new InputEvent();
                evt.setListenerActor(item.srcView);
                return item.onClick.touchDown(evt, 0, 0, 0, 0);
            }
        }
        return false;
    }

    boolean doExit(int currentIdx)
    {
        if(currentIdx >=0 && currentIdx < items.size())
        {
            AnimItem item = items.get(currentIdx);
            if(item.onClick != null)
            {
                InputEvent evt = new InputEvent();
                evt.setListenerActor(item.srcView);
                item.onClick.exit(evt, 0, 0, 0, null);
            }
        }
        return true;
    }

    boolean prepareClicked()
	{
		if(currentIdx >=0 && currentIdx < items.size())
		{
			AnimItem item = items.get(currentIdx);
			if(item.onClick != null)
			{
				InputEvent evt = new InputEvent();
				evt.setListenerActor(item.srcView);
				return item.onClick.prepareClicked(evt);
			}
		}
        return false;
	}
	
	void processClicked()
	{
		if(currentIdx >=0 && currentIdx < items.size())
		{
			AnimItem item = items.get(currentIdx);
//			for(Animation oanim : onOtherClickedAnim)
//			{
//				if(oanim != null)
//					oanim.reset();
//			}
			InputEvent evt = new InputEvent();
			evt.setListenerActor(item.srcView);
			item.onClick.clicked(evt, 0, 0);
			currentIdx = -1;
            if(onClickedAnim == null)
                item.srcView.addAction(onReleaseAnim.create(0));
		}
	}
	
	/**
	 * Finds the array entry index by the given object
	 * @param view
	 * @return
	 */
	int findByView(Actor view)
	{
		int len = items.size();
		for(int i=0; i<len; i++)
		{
			if(items.get(i).srcView == view)
				return i;
		}
		return -1;
	}
	
	/**
	 * Finds the array entry index by the given tag
	 * @param tag
	 * @return
	 */
	int findById(int tag)
	{
		int len = items.size();
		for(int i=0; i<len; i++)
		{
			if(items.get(i).tag == tag)
				return i;
		}
		return -1;
	}
	
	public void clear()
	{
		items.clear();
	}

	public void clearAll()
	{
		clear();
	}
	
	public void clear(int tag)
	{
		int len = items.size();
		for(int i=0; i<len; i++)
		{
			if(items.get(i).tag == tag)
			{
				items.remove(i);
				break;
			}
		}		
	}
	
	public void setOnClickListener(int tag, ClickListenerPrepared onClick)
	{
		int len = items.size();
		for(int i=0; i<len; i++)
		{
			if(items.get(i).tag == tag)
			{
				items.get(i).onClick = onClick;
				break;
			}
		}				
	}
	
	public void startInitialAnimation(float offset)
	{
		int len = items.size();
		if(onInitialAnim == null)
			return;
		for(int i=0;i<len;i++)
		{
			AnimItem item = items.get(i);
			item.srcView.clearActions();
			item.srcView.addAction(
				item.animateAfterInit == null ?	onInitialAnim.create(offset + i*initialAnimDelay)
						: onInitialAnim.create(offset + i*initialAnimDelay, item.animateAfterInit.create(0)));
		}
	}

    public void rememberState()
    {
        for(AnimItem item : items)
        {
            item.x = item.srcView.getX();
            item.y = item.srcView.getY();
            item.scale = item.srcView.getScaleX();
        }
    }

    public void restoreState()
    {
        for(AnimItem item : items)
        {
            item.srcView.setPosition(item.x, item.y);
            item.srcView.setScale(item.scale);
        }
    }


    public void invalidate()
    {
        for(AnimItem item : items)
        {
            if(item.srcView.getParent() instanceof WidgetGroup)
            {
                ((WidgetGroup)item.srcView.getParent()).invalidateHierarchy();
            }
        }
    }

	private float initialAnimDelay = 0.13f;

    /**
     * Delay between each item animation at startup
     * @param del
     */
	public void setInitialAnimDelay(float del)
	{
		initialAnimDelay = del;
	}
	
	/**
	 * Start the given animation on the given button after the initial one ends
	 * @param but
	 * @param anim
	 */
	public void animateAfterInit(final int but, IAnimationFactory anim)
	{
		int idx = findById(but);
		items.get(idx).animateAfterInit = anim;
	}
	
	/**
	 * Sets the animation that will be used when button is pressed
	 * @param anim
	 */
	public void setOnPressAnimation(IAnimationFactory anim)
	{
		onPressAnim = anim;
	}
	
	public void setOnReleaseAnimation(IAnimationFactory anim)
	{
		onReleaseAnim = anim;
	}
	
	public void setOnClickedAnimation(IAnimationFactory anim)
	{
		onClickedAnim = anim;
	}
	
	public void setOnOtherClickedAnimation(IAnimationFactory anim)
	{
		onOtherClickedAnim = anim;
	}

	public void setOnInitialAnimation(IAnimationFactory anim)
	{
		onInitialAnim = anim;
	}

	@Override
	public void run()
	{
		processClicked();
	}
}
