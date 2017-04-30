package game;

import java.util.HashMap;
import java.util.Iterator;

import whitealchemy.TileSheet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader.TextureAtlasParameter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Content
{
	public static AssetManager assetLoader;
	public static BitmapFont font;
	public static TextureAtlas physicsAtlas;
	public static TextureAtlas samusAtlas;
	public static HashMap<String, TileSheet> tileSheets;
	public static HashMap<String, TextureAtlas> spriteAtlases;
	public static Texture texWhite;
	public static TextureRegion texGravity;
	public static Sound sndBlackHole2;
	public static HashMap<String, OpenALMusic> musicSongs;
	
	public static void init()
	{
		TextureAtlasParameter atlasParam = new TextureAtlasParameter();
		atlasParam.flip = true;
		
		loadFonts();
		
		assetLoader = new AssetManager();
		assetLoader.load("content/atlas/physics.atlas", TextureAtlas.class, atlasParam);
		assetLoader.load("content/atlas/my_samus.atlas", TextureAtlas.class, atlasParam);
		
//		int ble = Integer.parseInt("jhfgb");
		
		spriteAtlases = new HashMap<String, TextureAtlas>();
		FileHandle atlasFH = Gdx.files.internal("content/spriteatlas");
		for (FileHandle cfh : atlasFH.list())
		{
			if (cfh.extension().equals("atlas"))
			{
				spriteAtlases.put(cfh.nameWithoutExtension(), new TextureAtlas(cfh, true));
			}
		}
		
		//Load tileset textures
		HashMap<String, Texture> tilesetTextures;
		tilesetTextures = new HashMap<String, Texture>();
		String tilesetPath = "content/tilesheets";
		FileHandle fh = Gdx.files.internal(tilesetPath);
		
		tileSheets = new HashMap<String, TileSheet>();
		
		for (FileHandle cfh : fh.list())
		{
			tilesetTextures.put(cfh.nameWithoutExtension(), new Texture(cfh, true));
			tileSheets.put(cfh.nameWithoutExtension(), new TileSheet(tilesetTextures.get(cfh.nameWithoutExtension()), cfh.nameWithoutExtension()));
		}
		
		
		texGravity = new TextureRegion(new Texture(Gdx.files.internal("content/gravity.png")));
		texGravity.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		sndBlackHole2 = Gdx.audio.newSound(Gdx.files.internal("content/audio/blackhole2.ogg"));
	}
	
	public static void minimalInit()
	{
		font = new BitmapFont(Gdx.files.internal("font/sansation.fnt"), true);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font.setColor(Color.WHITE);
		
		texWhite = new Texture(Gdx.files.internal("content/white.png"));
		texWhite.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
	
	public static void dispose()
	{
		assetLoader.dispose();
		font.dispose();
		texWhite.dispose();
		Iterator iterator = tileSheets.values().iterator();
	    while(iterator.hasNext())
	    {
	    	((TileSheet)(iterator.next())).dispose();
	    }
	    iterator = spriteAtlases.values().iterator();
	    while(iterator.hasNext())
	    {
	    	((TextureAtlas)(iterator.next())).dispose();
	    }
	}
	
	public static void setupReferences()
	{
		physicsAtlas = assetLoader.get("content/atlas/physics.atlas");
		samusAtlas   = assetLoader.get("content/atlas/my_samus.atlas");
	}
	
	//There seems to be no option to load Bitmap Fonts flipped with asset Manager
	//So We are doing it manually, whatever
	public static void loadFonts()
	{
//		font = new BitmapFont(Gdx.files.internal("font/sansation.fnt"), true);
//		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
//		font.setColor(Color.WHITE);
	}
}
