package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MyLabel extends Label
{

	public MyLabel(CharSequence text, Skin skin)
	{
		super(text, skin);
		// TODO Auto-generated constructor stub
	}

	public MyLabel(CharSequence text, LabelStyle style)
	{
		super(text, style);
		// TODO Auto-generated constructor stub
	}

	public MyLabel(CharSequence text, Skin skin, String styleName)
	{
		super(text, skin, styleName);
		// TODO Auto-generated constructor stub
	}

	public MyLabel(CharSequence text, Skin skin, String fontName, Color color)
	{
		super(text, skin, fontName, color);
		// TODO Auto-generated constructor stub
	}

	public MyLabel(CharSequence text, Skin skin, String fontName,
			String colorName)
	{
		super(text, skin, fontName, colorName);
		// TODO Auto-generated constructor stub
	}

	//getTextBounds is missing in gdx 1.9.9, so disabled it
//	@Override
//	public float getPrefHeight ()
//	{
//		float superHeight = super.getPrefHeight();
//		Vector2 bounds = getTextBounds();
//		float height = bounds.y - getStyle().font.getDescent();
//		return height;
//	}

    public void setTextTrimmed(String text, int maxLen)
    {
        int len = text.length();
        String textTrimmed = text;
        if(len > maxLen)
        {
            textTrimmed = text.substring(0, maxLen - 2 ) + "...";
        }

        setText(textTrimmed);
    }
	
}
