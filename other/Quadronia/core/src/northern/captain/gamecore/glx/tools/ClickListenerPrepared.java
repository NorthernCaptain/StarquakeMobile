package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public abstract class ClickListenerPrepared extends ClickListener
{

	public ClickListenerPrepared()
	{
	}

	public ClickListenerPrepared(int button)
	{
		super(button);
	}

	
	public abstract boolean prepareClicked(InputEvent evt);
}
