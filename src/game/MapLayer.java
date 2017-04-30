package game;

import java.util.ArrayList;

import whitealchemy.SpriteData;
import whitealchemy.TileSheet;
import whitealchemy.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class MapLayer
{
	int cacheID = -1;
	public String infoName;
	public String meta;
	public int width;
	public int height;
	int baseWidth, baseHeight;
	//some metadata to identify WHAT this is, purpose n stuff
	
	public boolean spritesAbove = true;
	public static int TILESIZE = 32;
	private TextureRegion[][] tiles;
	private Vector2[][] tileIndicies;
	public TileSheet tileSheet;
	public OrthographicCamera parallaxCamera = null;
	public ArrayList<Body> bodies = new ArrayList<Body>();
	
	ArrayList<SpriteWithName> sprites = new ArrayList<SpriteWithName>();
	
	private float minX, minY, maxX, maxY, minXpara, minYpara, maxXpara, maxYpara;
	
	//parallax info
	
	
	public MapLayer()
	{
		//kryo only
	}
	
	public MapLayer(SerializableMapLayerData inData, TileSheet _tileSheet, TextureAtlas spriteAtlas, int _baseWidth, int _baseHeight, OrthographicCamera normalCamera)
	{
		infoName 		= inData.infoName;
		meta	 		= inData.meta;
		width 			= inData.width;
		height 			= inData.height;
		tileSheet 		= _tileSheet;
		TILESIZE 		= inData.tilesize;
		tileIndicies 	= inData.indicies;
		tiles 			= new TextureRegion[width][height];
		spritesAbove	= inData.spritesAbove;
		
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
					tiles[x][y] = tileSheet.getTile((int)tileIndicies[x][y].x, (int)tileIndicies[x][y].y);
			}
		}
		
		for(SpriteData sdata : inData.spriteData)
		{
			Sprite sp = spriteAtlas.createSprite(sdata.textureRegionName);
			sp.setPosition(sdata.x, sdata.y);
			sp.setRotation(sdata.rotation);
			sp.setScale(sdata.scale);
			sp.flip(sdata.flippedX, sdata.flippedY);
			
			addSprite(sp, sdata.textureRegionName);
		}
		
		baseWidth  = _baseWidth;
		baseHeight = _baseHeight;
		if (width != baseWidth || height != baseHeight)
		{
			initParallaxCamera(normalCamera);
		}
	}
	
	public MapLayer(int _w, int _h, TileSheet _tileSheet, String _infoName, String _meta, int _baseWidth, int _baseHeight, OrthographicCamera normalCamera)
	{
		width			= _w;
		height			= _h;
		tileIndicies 	= new Vector2[width][height];
		tiles 			= new TextureRegion[width][height];
		tileSheet 		= _tileSheet;
		infoName		= _infoName;
		meta 			= _meta;
		
		baseWidth  = _baseWidth;
		baseHeight = _baseHeight;
		if (width != baseWidth || height != baseHeight)
		{
			initParallaxCamera(normalCamera);
		}
	}
	
	public void initParallaxCamera(OrthographicCamera normalCamera)
	{
		parallaxCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		parallaxCamera.setToOrtho(true);
		
		minX = (Gdx.graphics.getWidth()*0.5f)*normalCamera.zoom;
		maxX = (baseWidth*TILESIZE)  - ((Gdx.graphics.getWidth()*0.5f)*normalCamera.zoom);
		minXpara = (Gdx.graphics.getWidth()*0.5f)*parallaxCamera.zoom;
		maxXpara = (width*TILESIZE)  - ((Gdx.graphics.getWidth()*0.5f)*parallaxCamera.zoom);
		
		minY = (Gdx.graphics.getHeight()*0.5f)*normalCamera.zoom;
		maxY = (baseHeight*TILESIZE)  - ((Gdx.graphics.getHeight()*0.5f)*normalCamera.zoom);
		minYpara = (Gdx.graphics.getHeight()*0.5f)*parallaxCamera.zoom;
		maxYpara = (height*TILESIZE)  - ((Gdx.graphics.getHeight()*0.5f)*parallaxCamera.zoom);
	}
	
	public void createLightWall(World world)
	{
//		if (1==1) return;
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
				{
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.StaticBody;
					Body body = world.createBody(bodyDef);
					PolygonShape shape = new PolygonShape();
					shape.setAsBox(TILESIZE*0.5f, TILESIZE*0.5f);
					body.createFixture(shape, 0);
					body.setTransform((x*TILESIZE)+(TILESIZE*0.5f), (y*TILESIZE)+(TILESIZE*0.5f), 0);
					
					bodies.add(body);
				}
			}
		}
	}
	
	public void createSlopeLULightWall(World world)
	{
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
				{
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.StaticBody;
					Body body = world.createBody(bodyDef);
//					PolygonShape shape = new PolygonShape();
//
//					Vector2[] vertices = new Vector2[3];
//					vertices[0] = new Vector2(0, 0);
//					vertices[1] = new Vector2(TILESIZE, TILESIZE);
//					vertices[2] = new Vector2(0, TILESIZE);
//					shape.set(vertices);
					
					EdgeShape shape = new EdgeShape();
//					shape.set( new Vector2((x*TILESIZE),(y*TILESIZE)), new Vector2((x*TILESIZE)+TILESIZE,(y*TILESIZE)) ); //ends of the line
					shape.set( new Vector2(0, 0), new Vector2(TILESIZE , TILESIZE ) ); //ends of the line
					
					body.createFixture(shape, 0);
					body.setTransform((x*TILESIZE), (y*TILESIZE), 0);
					
					bodies.add(body);
				}
			}
		}
	}
	
	public void createSlopeRULightWall(World world)
	{
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
				{
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.StaticBody;
					Body body = world.createBody(bodyDef);
					PolygonShape shape = new PolygonShape();

					Vector2[] vertices = new Vector2[3];
					vertices[0] = new Vector2(TILESIZE, 0);
					vertices[1] = new Vector2(TILESIZE, TILESIZE);
					vertices[2] = new Vector2(0, TILESIZE);
					shape.set(vertices);
					
					body.createFixture(shape, 0);
					body.setTransform((x*TILESIZE), (y*TILESIZE), 0);
					
					bodies.add(body);
				}
			}
		}
	}
	
	public void createSlopeRDLightWall(World world)
	{
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
				{
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.StaticBody;
					Body body = world.createBody(bodyDef);
					PolygonShape shape = new PolygonShape();

					Vector2[] vertices = new Vector2[3];
					vertices[0] = new Vector2(0, 0);
					vertices[1] = new Vector2(TILESIZE, 0);
					vertices[2] = new Vector2(TILESIZE, TILESIZE);
					shape.set(vertices);
					
					body.createFixture(shape, 0);
					body.setTransform((x*TILESIZE), (y*TILESIZE), 0);
					
					bodies.add(body);
				}
			}
		}
	}
	
	public void createSlopeLDLightWall(World world)
	{
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
				{
					BodyDef bodyDef = new BodyDef();
					bodyDef.type = BodyDef.BodyType.StaticBody;
					Body body = world.createBody(bodyDef);
					PolygonShape shape = new PolygonShape();

					Vector2[] vertices = new Vector2[3];
					vertices[0] = new Vector2(0, 0);
					vertices[1] = new Vector2(TILESIZE, 0);
					vertices[2] = new Vector2(0, TILESIZE);
					shape.set(vertices);
					
					body.createFixture(shape, 0);
					body.setTransform((x*TILESIZE), (y*TILESIZE), 0);
					
					bodies.add(body);
				}
			}
		}
	}
	
	public void clearLightWall(World world)
	{
		for (Body body : bodies)
		{
			world.destroyBody(body);
		}
		bodies.clear();
	}
	
	public int getSpriteCount()
	{
		return sprites.size();
	}
	
	public void clearSprites()
	{
		sprites.clear();
	}
	
	public int getCacheID() { return cacheID; }

	public void draw(SpriteCache spriteCache, OrthographicCamera camera)
	{
		if (cacheID == -1)
		{
			System.err.println("This layer has not been cached yet");
			Util.traceThis();
			return;
		}
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		if (parallaxCamera != null)
		{
			updateParallaxCamera(camera);
			spriteCache.setProjectionMatrix(parallaxCamera.combined);
		}
		else
		{
			spriteCache.setProjectionMatrix(camera.combined);
		}
		
		spriteCache.begin();
			spriteCache.draw(cacheID);
		spriteCache.end();
	}
	
	public boolean hasParallaxCamera() { return (parallaxCamera != null);}
	
	public OrthographicCamera getParallaxCamera(OrthographicCamera normalCamera)
	{
		if (parallaxCamera == null)
		{
			return null;
		}
		else
		{
			updateParallaxCamera(normalCamera);
			
			return parallaxCamera;
		}
	}
	
	public void updateParallaxCamera(OrthographicCamera normalCamera)
	{
		parallaxCamera.zoom = normalCamera.zoom;
		
		// YOU ONLY NEED THIS IF ZOOMING IS USED------
		minX = (Gdx.graphics.getWidth()*0.5f)*normalCamera.zoom;
		maxX = (baseWidth*TILESIZE)  - ((Gdx.graphics.getWidth()*0.5f)*normalCamera.zoom);
		minXpara = (Gdx.graphics.getWidth()*0.5f)*parallaxCamera.zoom;
		maxXpara = (width*TILESIZE)  - ((Gdx.graphics.getWidth()*0.5f)*parallaxCamera.zoom);

		minY = (Gdx.graphics.getHeight()*0.5f)*normalCamera.zoom;
		maxY = (baseHeight*TILESIZE)  - ((Gdx.graphics.getHeight()*0.5f)*normalCamera.zoom);
		minYpara = (Gdx.graphics.getHeight()*0.5f)*parallaxCamera.zoom;
		maxYpara = (height*TILESIZE)  - ((Gdx.graphics.getHeight()*0.5f)*parallaxCamera.zoom);
		
		parallaxCamera.position.x = minXpara + (((maxXpara-minXpara)/100f)*((100.0f/(maxX-minX))*(normalCamera.position.x - minX)));
		parallaxCamera.position.y = minYpara + (((maxYpara-minYpara)/100f)*((100.0f/(maxY-minY))*(normalCamera.position.y - minY)));
		
		parallaxCamera.update();
	}
	
	public void addSprite(Sprite sp, String textureRegionName)
	{
		sprites.add(new SpriteWithName(sp, textureRegionName));
	}
	
	public void eraseSprite(Sprite sp)
	{
		for (SpriteWithName spWName : sprites)
		{
			if (spWName.sprite == sp)
			{
				sprites.remove(spWName);
				break;
			}
		}
	}
	
	public void setTileSheet(TileSheet _tileSheet, SpriteCache spriteCache)
	{
		tileSheet = _tileSheet;
		
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
					tiles[x][y] = tileSheet.getTile((int)tileIndicies[x][y].x, (int)tileIndicies[x][y].y);
			}
		}
	}
	
	public boolean isEmpty()
	{
		boolean out = true;
		
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
				{
					out = false;
					break;
				}
			}
		}
		
		return out;
	}
	
	public boolean isTileEmpty(int x, int y)
	{
		return (tileIndicies[x][y] == null);
	}
	
	public void setTile(int x, int y, int targetIX, int targetIY)
	{
		tileIndicies[x][y] 	= new Vector2(targetIX, targetIY);
		tiles[x][y] 		= tileSheet.getTile(targetIX, targetIY);
	}
	
	public void wipeAll(SpriteCache spriteCache)
	{
		wipeTiles(spriteCache);
		wipeSprites(spriteCache);
	}
	
	public void wipeSprites(SpriteCache spriteCache)
	{
		sprites.clear();
		
		cacheIt(spriteCache);
	}
	
	public void wipeTiles(SpriteCache spriteCache)
	{
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				eraseTile(x, y);
			}
		}
		
		cacheIt(spriteCache);
	}
	
	public Sprite getSpriteOnPosition(int x, int y)
	{
		for (int i = sprites.size() - 1; i >= 0; i--)
		{
			if (sprites.get(i).sprite.getBoundingRectangle().contains(x, y))
			{
				return sprites.get(i).sprite;
			}
		}
		return null;
	}
	
	public void eraseTile(int x, int y)
	{
		try{
			tileIndicies[x][y] 	= null;
			tiles[x][y] 		= null;
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e)
		{
			//dont care, just skip
		}
	}
	
	public Vector2 getTileIndex(int x, int y)
	{
		return tileIndicies[x][y];
	}
	
	public TextureRegion getTile(int x, int y)
	{
		return tiles[x][y];
	}

//	@Override
	public void cacheIt(SpriteCache spriteCache)
	{
		spriteCache.beginCache();
		
			if (!spritesAbove)
			{
				for (SpriteWithName spr : sprites)
				{
					spriteCache.add(spr.sprite);
				}
			}
			
			for (int y=0;y<height;y++)
			{
				for (int x=0;x<width;x++)
				{
					if (tileIndicies[x][y] != null)
						spriteCache.add(tiles[x][y], x*TILESIZE, y*TILESIZE, TILESIZE, TILESIZE);
				}
			}
			
			if (spritesAbove)
			{
				for (SpriteWithName spr : sprites)
				{
					spriteCache.add(spr.sprite);
				}
			}
			
		cacheID = spriteCache.endCache();
	}
	
	public void drawWithBatchFBO(SpriteBatch batch, OrthographicCamera camera)
	{
		if (parallaxCamera != null)
		{
			updateParallaxCamera(camera);
			batch.setProjectionMatrix(parallaxCamera.combined);
		}
		else
		{
			batch.setProjectionMatrix(camera.combined);
		}
		
		batch.begin();
		
		if (!spritesAbove)
		{
			for (SpriteWithName spr : sprites)
			{
				spr.sprite.draw(batch);
			}
		}
		
		for (int y=0;y<height;y++)
		{
			for (int x=0;x<width;x++)
			{
				if (tileIndicies[x][y] != null)
					batch.draw(tiles[x][y], x*TILESIZE, y*TILESIZE, TILESIZE, TILESIZE);
			}
		}
		
		if (spritesAbove)
		{
			for (SpriteWithName spr : sprites)
			{
				spr.sprite.draw(batch);
			}
		}
		
		batch.end();
	}
	
	public SerializableMapLayerData getSerializableData()
	{
		ArrayList<SpriteData> spriteData = new ArrayList<SpriteData>();
		for (SpriteWithName sp : sprites)
		{
			spriteData.add(new SpriteData(sp.sprite.getX(), sp.sprite.getY(), sp.sprite.getScaleX(), sp.sprite.getRotation(), sp.sprite.isFlipX(), !sp.sprite.isFlipY(),  sp.textureRegionName));
		}
		
		return new SerializableMapLayerData(width, height, TILESIZE, spritesAbove, tileSheet.name, tileIndicies, infoName, meta, spriteData);
	}
	
	class SpriteWithName
	{
		public String textureRegionName;
		public Sprite sprite;
		
		public SpriteWithName(Sprite _sprite, String _texRegName)
		{
			sprite = _sprite;
			textureRegionName = _texRegName;
		}
	}
}
