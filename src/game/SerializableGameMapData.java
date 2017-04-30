package game;

import physics.QSlope;
import box2dLight.Light;

public class SerializableGameMapData
{
	public int width;
	public int height;
	public String name; // will be at content/maps/name.map
	public String displayName;
	public String spriteAtlasFileName;
	public QSlope[] slopes;
	public SerializableMapLayerData[] mapData;
	public MapScript[] scripts;
	public LightData[] lights;
	
	public SerializableGameMapData()
	{
		//kryo only
	}
	
	public SerializableGameMapData(int _width, int _height, String _name, String _displayName, String _spriteAtlasName, QSlope[] _slopes, SerializableMapLayerData[] _mapData, MapScript[] _scripts, LightData[] _lights)
	{
		width = _width;
		height = _height;
		name = _name;
		displayName = _displayName;
		spriteAtlasFileName = _spriteAtlasName;
		slopes = _slopes;
		mapData = _mapData;
		scripts = _scripts;
		lights = _lights;
	}
}
