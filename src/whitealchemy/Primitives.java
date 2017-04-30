package whitealchemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 
 * @author Patrik Schulze
 *
 */
public class Primitives
{	
	private static Color tempColor = Color.WHITE;
	
	private static TextureRegion imgPixel;
	
	public static void init(TextureAtlas atlas)
	{
		imgPixel          = atlas.findRegion("pixel");
        
        imgPixel.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}
	
	public static void setLinearFilter()
	{
		imgPixel.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
	
	public static void setNearestFilter()
	{
		imgPixel.getTexture().setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
	}
	
	public static void drawRect(SpriteBatch spriteBatch, float x, float y, float w, float h, Color col)
	{
		tempColor = spriteBatch.getColor();
		spriteBatch.setColor(col);

		drawLine(spriteBatch, x, y, x+w, y);
		drawLine(spriteBatch, x, y, x, y+h);
		
		drawLine(spriteBatch, x+w, y, x+w, y+h);
		drawLine(spriteBatch, x, y+h, x+w, y+h);
		
		spriteBatch.setColor(tempColor);
	}
	
	public static void fillRect(SpriteBatch spriteBatch, float x, float y, float w, float h)
	{
		drawImage(spriteBatch, imgPixel, x, y, w, h);
	}

	public static void fillRect(SpriteBatch spriteBatch, float x, float y, float w, float h, Color col)
	{
		tempColor = spriteBatch.getColor();
		spriteBatch.setColor(col);
		drawImage(spriteBatch, imgPixel, x, y, w, h);
		spriteBatch.setColor(tempColor);
	}
	
	public static void drawImage(SpriteBatch spriteBatch, TextureRegion img, float x, float y, float width, float height)
	{
		spriteBatch.draw(img, x, y, width, height);		
	}
	
	public static void drawLine(SpriteBatch spriteBatch, float x, float y, float x2, float y2, Color col)
	{
		tempColor = spriteBatch.getColor();
		spriteBatch.setColor(col);
		if (col != null) drawLine(spriteBatch, x, y, x2, y2);
		spriteBatch.setColor(tempColor);
	}
	
	public static void drawLine(SpriteBatch spriteBatch, float x, float y, float x2, float y2)
	{
		if (y == y2)
		{
			spriteBatch.draw(imgPixel, x, y, (x2-x), 1);
		}
		else if (x == x2)
		{
			spriteBatch.draw(imgPixel, x, y, 1, (y2-y));
		}
		else
		{
			System.out.println(""+x+" , "+y+" , "+x2+" , "+y2);
			System.out.println("AlchGraphics: Dont have Diagonal Lines");
		}
	}
}
