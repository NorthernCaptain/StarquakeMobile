package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

public class MyTextButton extends Button
{
	private final MyLabel label;
	private TextButtonStyle style;

	public MyTextButton(String text, TextButtonStyle style)
	{
		super(style);
		this.style = style;
		label = new MyLabel(text, new LabelStyle(style.font, style.fontColor));
		label.setAlignment(Align.center);
		add(label).expand().fill().padBottom(style.textPadBottom);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public void setStyle(ButtonStyle style)
	{
		if (!(style instanceof TextButtonStyle))
			throw new IllegalArgumentException(
					"style must be a TextButtonStyle.");
		super.setStyle(style);
		this.style = (TextButtonStyle) style;
		if (label != null)
		{
			TextButtonStyle textButtonStyle = (TextButtonStyle) style;
			LabelStyle labelStyle = label.getStyle();
			labelStyle.font = textButtonStyle.font;
			labelStyle.fontColor = textButtonStyle.fontColor;
			label.setStyle(labelStyle);
		}
	}

    public void setFontScale(float scale)
    {
        label.setFontScale(scale);
    }

	public TextButtonStyle getStyle()
	{
		return style;
	}

    @Override
	public void draw(Batch batch, float parentAlpha)
	{
		Color fontColor;
		if (isDisabled() && style.disabledFontColor != null)
			fontColor = style.disabledFontColor;
		else if (isPressed() && style.downFontColor != null)
			fontColor = style.downFontColor;
		else if (isChecked() && style.checkedFontColor != null)
			fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor
					: style.checkedFontColor;
		else if (isOver() && style.overFontColor != null)
			fontColor = style.overFontColor;
		else
			fontColor = style.fontColor;
		if (fontColor != null)
			label.getStyle().fontColor = fontColor;
		super.draw(batch, parentAlpha);
	}

	public Label getLabel()
	{
		return label;
	}

	public Cell<MyLabel> getLabelCell()
	{
		return getCell(label);
	}

	public void setText(String text)
	{
		label.setText(text);
	}

	public CharSequence getText()
	{
		return label.getText();
	}

	/**
	 * The style for a text button, see {@link TextButton}.
	 * 
	 * @author Nathan Sweet
	 */
	static public class TextButtonStyle extends ButtonStyle
	{
		public BitmapFont font;
		/** Optional. */
		public Color fontColor, downFontColor, overFontColor, checkedFontColor,
				checkedOverFontColor, disabledFontColor;

        public float textPadBottom = 0f;

		public TextButtonStyle()
		{
		}

		public TextButtonStyle(Drawable up, Drawable down, Drawable checked)
		{
			super(up, down, checked);
		}

		public TextButtonStyle(TextButtonStyle style)
		{
			super(style);
			this.font = style.font;
			if (style.fontColor != null)
				this.fontColor = new Color(style.fontColor);
			if (style.downFontColor != null)
				this.downFontColor = new Color(style.downFontColor);
			if (style.overFontColor != null)
				this.overFontColor = new Color(style.overFontColor);
			if (style.checkedFontColor != null)
				this.checkedFontColor = new Color(style.checkedFontColor);
			if (style.checkedOverFontColor != null)
				this.checkedFontColor = new Color(style.checkedOverFontColor);
			if (style.disabledFontColor != null)
				this.disabledFontColor = new Color(style.disabledFontColor);
		}
	}
}
