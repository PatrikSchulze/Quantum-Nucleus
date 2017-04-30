package whitealchemy;

import game.Colors;
import game.LightData;
import game.MapLayer;
import game.MapScript;
import game.SerializableGameMapData;
import game.SerializableMapLayerData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import physics.QSlope;
import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;


public class GameMap
{
	public ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
	public ArrayList<MapScript> mapScripts = new ArrayList<MapScript>();
	private TextureAtlas spriteAtlas; // lets have one spriteAtlas per GameMap ?
//	private SerializableMapLayerData[] serialData;
	private boolean allCached = false;
	private int baseSizeWidthInTiles;
	private int baseSizeHeightInTiles;
	public String name; // will be at content/maps/name.map
	public String displayName;
	public String spriteAtlasFileName;
	public ArrayList<QSlope> slopes;
	//spritedata
	//private ArrayList<QPlatform> platforms;
	//private ArrayList<QOpenPlatform> openPlatforms;
	//serialzable stuff here
	public RayHandler rayHandler;
	public World world;
	public ArrayList<PointLight> lights;
	private Box2DDebugRenderer box2DRenderer;
	
	
	public GameMap(int _baseSizeWidth, int _baseSizeHeight, String inname, String _displayName, TextureAtlas _spriteAtlas, String _atlasName)
	{
		baseSizeWidthInTiles	= _baseSizeWidth;
		baseSizeHeightInTiles	= _baseSizeHeight;
		name = inname;
		displayName = _displayName;
		spriteAtlas = _spriteAtlas;
		spriteAtlasFileName = _atlasName;
		slopes = new ArrayList<QSlope>();
		
		world = new World(new Vector2(0, 0), true);
		lights = new ArrayList<PointLight>();
		RayHandler.setGammaCorrection(false);
		RayHandler.useDiffuseLight(true);
		rayHandler = new RayHandler(world);
		rayHandler.setCulling(true);
		rayHandler.setAmbientLight(Colors.defaultAmbientLight2);
		rayHandler.setBlurNum(3);
		
		box2DRenderer = new Box2DDebugRenderer();
	}
	
	public void dispose()
	{
		for (MapLayer l : layers) {	l.clearLightWall(world);}
		
		rayHandler.dispose();
		world.dispose();
		box2DRenderer.dispose();
	}
	
	public World getWorld() { return world; }
	
	public void addSlope(QSlope.TYPE type, float x1, float y1, float x2, float y2)
	{
		slopes.add(new QSlope(type, x1, y1, x2, y2));
	}
	
	public void addSlope(QSlope.TYPE type, float x1, float y1, float x2, float y2, int _hid)
	{
		slopes.add(new QSlope(type, x1, y1, x2, y2, _hid));
	}
	
	public void setSpriteAtlas(TextureAtlas _spriteAtlas, String _atlasName)
	{
		spriteAtlas = _spriteAtlas;
		spriteAtlasFileName = _atlasName;
		clearAllSprites();
	}
	
	public void clearAllSprites()
	{
		for (MapLayer l : layers)
		{
			l.clearSprites();
		}
	}
	
	public int getTotalSpriteCount()
	{
		int out = 0;
		for (MapLayer l : layers)
		{
			out += l.getSpriteCount();
		}
		return out;
	}
	
	public int getBaseSizeWidthInPixels()  { return baseSizeWidthInTiles*MapLayer.TILESIZE; }
	public int getBaseSizeHeightInPixels() { return baseSizeHeightInTiles*MapLayer.TILESIZE; }
	public int getBaseSizeWidthInTiles()  { return baseSizeWidthInTiles; }
	public int getBaseSizeHeightInTiles() { return baseSizeHeightInTiles; }
	
	public void setBaseSize(int w, int h)
	{
		baseSizeWidthInTiles  = w;
		baseSizeHeightInTiles = h;
	}
	
	public MapLayer getLayer(int i)
	{
		return layers.get(i);
	}
	
	public void addLayer(MapLayer layer)
	{
		layers.add(layer);
		allCached = false;
	}
	
	public void addLayer(MapLayer layer, int index)
	{
		layers.add(index, layer);
		allCached = false;
	}
	
	public void cacheAll(SpriteCache spriteCache)
	{
//		long before = System.nanoTime()/1000L/1000L;
		for (MapLayer layer : layers)
		{
			layer.cacheIt(spriteCache);
		}
		allCached = true;
//		System.out.println("Cached all in "+((System.nanoTime()/1000L/1000L)-before)+"ms");
	}
	
	public void renderAllLayers(SpriteCache spriteCache, OrthographicCamera camera)
	{
		if (!allCached) {System.err.println("TileMap not cached."); return; }
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		for (MapLayer layer : layers)
		{
			layer.draw(spriteCache, camera);
		}
	}
	
	public void renderAllWithBatch(SpriteBatch batch, OrthographicCamera camera)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		for (MapLayer layer : layers)
		{
			layer.drawWithBatchFBO(batch, camera);
		}
	}
	
	public void addLight(PointLight light)
	{
		createLightWall();
		lights.add(light);
	}
	
	public void initLights()
	{
		for (PointLight cl : lights)
		{
//			PointLight b = new PointLight(rayHandler, cl.getRayNum(), cl.getColor(), cl.getDistance(), cl.getX(), cl.getY(), cl.getDirection(), cl.getConeDegree());
			PointLight b = new PointLight(rayHandler, cl.getRayNum(), cl.getColor(), cl.getDistance(), cl.getX(), cl.getY());
			b.setXray(cl.isXray());
		}
	}
	
	public void createLightWall()
	{
		for (MapLayer l : layers)
		{
			if (l.meta.startsWith("STRUCTURES"))
			{
				l.clearLightWall(world);
				l.createLightWall(world);
			}
			else if (l.meta.startsWith("SLOPES_LU"))
			{
				l.clearLightWall(world);
				l.createSlopeLULightWall(world);
			}
			else if (l.meta.startsWith("SLOPES_RU"))
			{
				l.clearLightWall(world);
				l.createSlopeRULightWall(world);
			}
			else if (l.meta.startsWith("SLOPES_LD"))
			{
				l.clearLightWall(world);
				l.createSlopeLDLightWall(world);
			}
			else if (l.meta.startsWith("SLOPES_RD"))
			{
				l.clearLightWall(world);
				l.createSlopeRDLightWall(world);
			}
		}		
	}
	
	public MapLayer getStructLayer()
	{
		for (MapLayer l : layers) {	if (l.meta.startsWith("STRUCTURES"))	return l; }
		return null;
	}
	
	public void removeLight(Light light)
	{
		light.remove(); // this removes it from the actual thing rayhandler, you know
		lights.remove(light);
	}
	
	public void removeAllLights()
	{
		rayHandler.removeAll();
		lights.clear();
	}
	
	public void renderLight(OrthographicCamera mapCamera)
	{
		rayHandler.setCombinedMatrix(mapCamera.combined, mapCamera.position.x, mapCamera.position.y, mapCamera.viewportWidth * mapCamera.zoom, mapCamera.viewportHeight * mapCamera.zoom);
		rayHandler.updateAndRender();
	}
	
	public void drawBox2D(OrthographicCamera camera)
	{
		box2DRenderer.render(world, camera.combined);
	}
	
	public Sprite getSpriteOnPosition(int x, int y, MapLayer firstlayer)
	{
		Sprite sp = firstlayer.getSpriteOnPosition(x, y);
		if (sp != null) return sp;
		
		for (int i = layers.size() - 1; i >= 0; i--)
		{
			sp = layers.get(i).getSpriteOnPosition(x, y);
			if (sp != null) return sp;
		}
		
		return null;
	}
	
	public MapScript getScriptOnPosition(int x, int y)
	{
		for (int i = mapScripts.size() - 1; i >= 0; i--)
		{
			if (mapScripts.get(i).rect.contains(x, y))
			{
				return mapScripts.get(i);
			}
		}
		
		return null;
	}
	
	public Light getLightOnPosition(int x, int y)
	{
		for (int i = lights.size() - 1; i >= 0; i--)
		{
			Light l = lights.get(i);
			if (x >= l.getX()-15 && x <= l.getX()+15 && y >= l.getY()-15 && y <= l.getY()+15)
			{
				return l;
			}
		}
		return null;
	}
	
	public void saveToFile(Kryo kryo)
	{
		saveToFile(kryo, name);
	}
	
	public void saveToFile(Kryo kryo, String fileName)
	{
		QSlope[] _qslope = new QSlope[slopes.size()];
		for (int i = 0; i < slopes.size(); i++)
		{
			_qslope[i] = new QSlope(slopes.get(i));
		}
		
		SerializableMapLayerData[] tData = new SerializableMapLayerData[layers.size()];
		for (int i = 0; i < layers.size(); i++)
		{
			MapLayer layer = layers.get(i);
			tData[i] = layer.getSerializableData();
		}
		
		MapScript[] scripts = new MapScript[mapScripts.size()];
		for (int i = 0; i < mapScripts.size(); i++)
		{
			scripts[i] = new MapScript(mapScripts.get(i).rect, mapScripts.get(i).trigger, mapScripts.get(i).idprefix);
		}
		
		LightData[] lightData = new LightData[lights.size()];
		for (int i = 0; i < lights.size(); i++)
		{
			PointLight cl = lights.get(i);
//			lightData[i] = new LightData(cl.getRayNum(), cl.getColor(), cl.getDistance(), cl.getX(), cl.getY(), cl.getDirection(), cl.getConeDegree(), cl.isXray());
			lightData[i] = new LightData(cl.getRayNum(), cl.getColor(), cl.getDistance(), cl.getX(), cl.getY(), cl.isXray(), cl.isStaticLight());
		}
		
		SerializableGameMapData gameMapData = new SerializableGameMapData(baseSizeWidthInTiles, baseSizeHeightInTiles, 
				name, displayName, spriteAtlasFileName, _qslope, tData, scripts, lightData);
		
		//---------------------------------------------------------------------

    	Output output = null;
		try {
			output = new Output(new FileOutputStream("maps/"+fileName+".qmap")); //TODO wrong path
		} catch (FileNotFoundException e)	{e.printStackTrace();}
		kryo.writeObject(output, gameMapData);
		output.close();
	}
	
	public static GameMap loadCreateMap(Kryo kryo, String mapName, HashMap<String, TextureAtlas> spriteAtlases, HashMap<String, TileSheet> tileSheets, SpriteCache spriteCache, OrthographicCamera camera)
	{
		GameMap map;
		
		if (!mapName.endsWith(".qmap")) mapName = mapName + ".qmap";

		com.esotericsoftware.kryo.io.Input input = null;
		try {
			input = new com.esotericsoftware.kryo.io.Input(new FileInputStream("maps/"+mapName));
		} catch (FileNotFoundException e1)	{e1.printStackTrace(); return null;}
		
		SerializableGameMapData inGameMapData = null;
		
		try{
			inGameMapData = (SerializableGameMapData)kryo.readObject(input, SerializableGameMapData.class);
		}
		catch(com.esotericsoftware.kryo.KryoException e)	{e.printStackTrace(); return null;}
		input.close();
		
		
		map = new GameMap(inGameMapData.width, inGameMapData.height,  inGameMapData.name, inGameMapData.displayName, 
				spriteAtlases.get(inGameMapData.spriteAtlasFileName), inGameMapData.spriteAtlasFileName);
		
		map.slopes = new ArrayList<QSlope>();
		for (int i = 0; i < inGameMapData.slopes.length; i++)
		{
			map.slopes.add(new QSlope(inGameMapData.slopes[i]));
		}
		
		map.mapScripts = new ArrayList<MapScript>();
		for (int i = 0; i < inGameMapData.scripts.length; i++)
		{
			map.mapScripts.add(new MapScript(inGameMapData.scripts[i].rect, inGameMapData.scripts[i].trigger, inGameMapData.scripts[i].idprefix));
		}
		
		map.layers = new ArrayList<MapLayer>();
		for (int i = 0; i < inGameMapData.mapData.length; i++)
		{
			MapLayer layer = new MapLayer(inGameMapData.mapData[i], tileSheets.get(inGameMapData.mapData[i].tileset), map.spriteAtlas, map.baseSizeWidthInTiles, map.baseSizeHeightInTiles, camera);
			map.layers.add(layer);
		}
		
		map.createLightWall();
		
		map.lights = new ArrayList<PointLight>();
		if (inGameMapData.lights != null)
		{
			for (int i = 0; i < inGameMapData.lights.length; i++)
			{
				LightData ld = inGameMapData.lights[i];
				if (ld == null) break;
//				PointLight light = new PointLight(map.rayHandler, ld.rays, ld.color, ld.distance, ld.x, ld.y, ld.directionDegree, (ld.coneDegree > 0f ? ld.coneDegree : 90.0f) );
				PointLight light = new PointLight(map.rayHandler, ld.rays, ld.color, ld.distance, ld.x, ld.y );
				light.setXray(ld.xray);
				light.setStaticLight(ld.staticLight);
				map.lights.add(light);
			}
		}
		
		return map;
	}
	
	
}
