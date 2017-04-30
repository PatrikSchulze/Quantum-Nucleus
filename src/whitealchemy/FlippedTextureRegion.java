package whitealchemy;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class FlippedTextureRegion extends TextureRegion
{
	public FlippedTextureRegion(FileHandle in)
	{
		super(new Texture(in));
		this.flip(false, true);
	}
	
	public FlippedTextureRegion(Texture texture, int x, int y, int width, int height) 
	{
		super(texture, x, y, width, height);
		this.flip(false, true);
	}
	
	public FlippedTextureRegion(TextureRegion region, int x, int y, int width, int height)
	{
		super(region, x, y, width, height);
		this.flip(false, true);
	}
	
	public FlippedTextureRegion(TextureRegion region)
	{
		super(region);
		this.flip(false, true);
	}
	
	public FlippedTextureRegion(Texture texture, float u, float v, float u2, float v2) 
	{
		super(texture, u, v, u2, v2);
		this.flip(false, true);
	}
	
//	public TextureRegion() getTextureRegion() { return this.get
	
	public int getHeight() { return this.getRegionHeight(); }
	public int getWidth() { return this.getRegionWidth(); }
	
	public void dispose() { this.getTexture().dispose(); }
	
	public void bind() { this.getTexture().bind(); }

}
