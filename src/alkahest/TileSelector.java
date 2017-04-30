package alkahest;

import whitealchemy.TileSheet;
import game.Colors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

public class TileSelector
{
	public static final int TILESIZE = 32;
	public static Texture texWhite;
//	public static TileLayer tileSelectorMap;
	public static ShapeRenderer shapeRenderer;
	public static TileSheet tileSheet;
	public static SpriteCache spriteCache;
	public static SpriteBatch spriteBatch;
	public static boolean visible = false;
	public static OrthographicCamera tileSheetCamera;
	public static float targetZoom = 1f;
	public static Vector2 selectedTileAreaStart  = new Vector2(0,0);
	public static Vector2 selectedTileAreaEnd    = new Vector2(0,0);
	public static int cacheID = -1;
	
	public static void init(Texture _texWhite, SpriteBatch _spriteBatch, TileSheet _tileSheet, SpriteCache _spriteCache)
	{
		texWhite = _texWhite;
		shapeRenderer = new ShapeRenderer();
		spriteCache = _spriteCache;//new SpriteCache(1200, false);
		spriteBatch = _spriteBatch;
		tileSheet = _tileSheet;
		
		tileSheetCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		tileSheetCamera.setToOrtho(true);
	}
	
	public static void compute()
	{
		if (Math.abs(targetZoom-tileSheetCamera.zoom) > 0.01f)
		{
			tileSheetCamera.zoom+=(targetZoom-tileSheetCamera.zoom)/20.0f;
		}
		
		tileSheetCamera.update();
	}
	
	public static void toggleVisible()
	{
		visible = !visible;
		tileSheetCamera.position.x = 0+tileSheet.getWidthInPixels()/2f;
        tileSheetCamera.position.y = 0+tileSheet.getHeightInPixels()/2f;
	}
	
	public static void setTileSheet(TileSheet _tileSheet)
	{
		tileSheet = _tileSheet;
		
		cacheTileSheetGraphics();
	}
	
	public static void cacheTileSheetGraphics()
	{
		spriteCache.beginCache();
		for (int y=0;y<tileSheet.getHeightInIndicies();y++)
		{
			for (int x=0;x<tileSheet.getWidthInIndicies();x++)
			{
					spriteCache.add(tileSheet.getTile(x, y), x*TILESIZE, y*TILESIZE, TILESIZE, TILESIZE);
			}
		}
		cacheID = spriteCache.endCache();
	}
	
	public static int getTileSheetMouseX()
	{
		int rx = getRelativeMouseX(tileSheetCamera);
		
		if (rx >= 0 && rx < tileSheet.getWidthInPixels())
		{
			return (rx/TILESIZE);
		}
		
		return -1;
	}

	public static int getTileSheetMouseY()
	{
		int ry = getRelativeMouseY(tileSheetCamera);
		
		if (ry >= 0 && ry < tileSheet.getHeightInPixels())
		{
			return (ry/TILESIZE);
		}
		
		return -1;
	}
	
	private static int getRelativeMouseX(OrthographicCamera cam)
	{
		return (int)(Gdx.input.getX()*cam.zoom)+(int)(cam.position.x)-(int)((cam.viewportWidth*cam.zoom)/2.0f);
	}
	
	private static int getRelativeMouseY(OrthographicCamera cam)
	{
		return (int)(Gdx.input.getY()*cam.zoom)+(int)(cam.position.y)-(int)((cam.viewportHeight*cam.zoom)/2.0f);
	}
	
	public static int getSelectionWidth()  { return ((int)TileSelector.selectedTileAreaEnd.x-(int)TileSelector.selectedTileAreaStart.x+1); }
	public static int getSelectionHeight() { return ((int)TileSelector.selectedTileAreaEnd.y-(int)TileSelector.selectedTileAreaStart.y+1); }
	
	public static void draw()
	{
		if (!visible) return;
		
		spriteBatch.setProjectionMatrix(tileSheetCamera.combined);
		spriteBatch.begin();
			spriteBatch.setColor(Colors.black65);
			spriteBatch.draw(texWhite, 0-(TILESIZE*0.5f), 0-(TILESIZE*0.5f), (tileSheet.getWidthInPixels())+TILESIZE, (tileSheet.getHeightInPixels())+TILESIZE);
			spriteBatch.setColor(Color.WHITE);
		spriteBatch.end();
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		spriteCache.setProjectionMatrix(tileSheetCamera.combined);
		spriteCache.begin();
			spriteCache.draw(cacheID);
		spriteCache.end();

		int tix = (int)TileSelector.selectedTileAreaStart.x;
		int tiy = (int)TileSelector.selectedTileAreaStart.y;
		int esx = (int)TileSelector.selectedTileAreaEnd.x-tix+1;
		int esy = (int)TileSelector.selectedTileAreaEnd.y-tiy+1;
		if (tix > -1 && tiy > -1)
		{
			float px = (tileSheetCamera.viewportWidth/2f)-(tileSheetCamera.position.x/tileSheetCamera.zoom)+(tix*TILESIZE/tileSheetCamera.zoom);
			float py = (tileSheetCamera.viewportHeight/2f)+(tileSheetCamera.position.y/tileSheetCamera.zoom)-(tiy*TILESIZE/tileSheetCamera.zoom);
			float rsx = esx*(TILESIZE/tileSheetCamera.zoom);
			float rsy = esy*(TILESIZE/tileSheetCamera.zoom);
			
			shapeRenderer.begin(ShapeType.Filled);
				shapeRenderer.setColor(Colors.white5);
				shapeRenderer.rect(px, py, rsx, -rsy);
			shapeRenderer.end();
			
			shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setColor(Color.YELLOW);
				shapeRenderer.rect(px, py, rsx, -rsy);
			shapeRenderer.end();
		}

	}
	
}
