/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package northern.captain.gamecore.glx.tools;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import northern.captain.gamecore.glx.NContext;

/**
 * Draws a {@link TextureRegion} repeatedly to fill the area, instead of
 * stretching it.
 * 
 * @author Leonid Khramov
 */
public class TiledDrawable2 extends TextureRegionDrawable
{
	protected float scaleFactor = 1.0f;
	
	protected float deltaX = 0, deltaY = 0;
	
	public TiledDrawable2 setDelta(float dx, float dy)
	{
		if(dx > 0)
		{
			dx = dx - getRegion().getRegionWidth();
		}
		
		if(dy > 0)
		{
			dy = dy - getRegion().getRegionHeight();
		}
		
		deltaX = dx * scaleFactor;
		deltaY = dy * scaleFactor;
		return this;
	}
	
	public TiledDrawable2 setScaleFactor(float factor)
	{
		scaleFactor = factor;
		return this;
	}
	
	public TiledDrawable2()
	{
		super();
		scaleFactor = NContext.current.getScaleFactor();
	}

	public TiledDrawable2(TextureRegion region)
	{
		super(region);
	}

	public TiledDrawable2(TextureRegionDrawable drawable)
	{
		super(drawable);
	}

    @Override
	public void draw(Batch batch, float x, float y, float width, float height)
	{
		TextureRegion region = getRegion();
		float regionWidth = region.getRegionWidth()*scaleFactor, regionHeight = region.getRegionHeight()*scaleFactor;
		float startX = x, startY = y;
		float endX = x + width, endY = y + height;
		
		float origU = region.getU();
		float origV = region.getV();
		float origU2 =region.getU2();
		float origV2 =region.getV2();
		float origUL = origU2 - origU;
		float origVL = origV2 - origV;
		
		int xblocks = (int) (width - deltaX + regionWidth);
		xblocks = (xblocks - 1)/(int)regionWidth;
		
		int yblocks = (int) (height - deltaY + regionHeight);
		yblocks = (yblocks - 1)/(int)regionHeight;
		
		float toX = startX + deltaX;
		float toY;
		float tileW, tileH;
		float u, u2, v, v2;
		Texture texture = region.getTexture();
		
		for(int i=0;i<xblocks;i++)
		{
			toY = startY + deltaY;
			tileW = regionWidth;
			if(toX < startX)
			{
				u = origU - deltaX / regionWidth * origUL;
				tileW += deltaX;
				toX = startX;
			} else
			{
				u = origU;
			}
			
			if(toX + regionWidth > endX)
			{
				float len = toX + regionWidth - endX;
				u2 = origU2 - len /regionWidth * origUL;
				tileW -= len;
			} else
			{
				u2 = origU2;
			}
			
			for(int j=0;j<yblocks;j++)
			{
				tileH = regionHeight;
				
				v = origV;
				v2 = origV2;
				
				if(toY < startY)
				{
					v2 = origV2 + deltaY / regionHeight * origVL;
					tileH += deltaY;
					toY = startY;
				}
				if(toY + regionHeight > endY)
				{
					float hei = toY + regionHeight - endY;
					v = origV + hei / regionHeight * origVL;
					tileH -= hei;
				}
				
				
				batch.draw(texture, toX, toY, tileW, tileH, u, v2, u2, v);
				
				toY += tileH;
			}
			toX += tileW;
		}
	}
}
