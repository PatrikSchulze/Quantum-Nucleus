package whitealchemy;

public class SpriteData
{
	public float x;
	public float y;
	public float scale;
	public float rotation;
	public String textureRegionName;
	public boolean flippedX;
	public boolean flippedY;
	
	public SpriteData() {}

	public SpriteData(float x, float y, float scale, float rotation, boolean flipX, boolean flipY, String textureRegionName)
	{
		this.x = x;
		this.y = y;
		this.scale = scale;
		this.rotation = rotation;
		this.textureRegionName = textureRegionName;
		flippedX = flipX;
		flippedY = flipY;
	}
	
}
