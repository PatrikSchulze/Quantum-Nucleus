package game;

import java.util.ArrayList;

import whitealchemy.SpriteData;

import com.badlogic.gdx.math.Vector2;

public class SerializableMapLayerData
{
	public int width;
	public int height;
	public String infoName;
	public String meta;
	public ArrayList<SpriteData> spriteData;
	public boolean spritesAbove;
	
	public int tilesize;
	public String tileset; // one tilelayer one tileset
	public Vector2[][] indicies;
	
	public SerializableMapLayerData()
	{
		//kryo only
	}
	
	public SerializableMapLayerData(int _width, int _height, int _tilesize, boolean _spritesAbove, String _tileset, Vector2[][] _indicies, String _infoName, String _meta, ArrayList<SpriteData> _spriteData)
	{
		width = _width;
		height = _height;
		tilesize = _tilesize;
		spritesAbove = _spritesAbove;
		tileset = _tileset;
		indicies = _indicies;
		infoName = _infoName;
		meta	 = _meta;
		spriteData = _spriteData;
	}
}
