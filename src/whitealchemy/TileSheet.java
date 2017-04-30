package whitealchemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class TileSheet
{
	public static final int TILESIZE = 32;
	private Texture tileSheetTexture;
	private FlippedTextureRegion regions[][];
	private int w,h;
	public String name = null;
	
	public TileSheet(Texture texture, String fname)
	{
		name = fname;
//		tileSheetTexture  = new Texture(Gdx.files.internal(path));
//        tileSheetTexture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		tileSheetTexture  = texture;
		
//		tileSheetTexture.setFilter(TextureFilter.MipMapNearestNearest, TextureFilter.MipMapNearestNearest);
		tileSheetTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        regions = new FlippedTextureRegion[tileSheetTexture.getWidth()/TILESIZE][tileSheetTexture.getHeight()/TILESIZE];
        
        for (int yi = 0; yi < (tileSheetTexture.getHeight()/TILESIZE); yi++)
        {
        	for (int xi = 0; xi < (tileSheetTexture.getWidth()/TILESIZE); xi++)
        	{
//        		regions[xi][yi] = new FlippedTextureRegion(tileSheetTexture, xi*TILESIZE, yi*TILESIZE, TILESIZE, TILESIZE);
        		
        		int x =  xi*TILESIZE;
        		int y =  yi*TILESIZE;
        		int w =  TILESIZE;
        		int h =  TILESIZE;
        		
        		float invTexWidth = 1f / texture.getWidth();
        		float invTexHeight = 1f / texture.getHeight();
        		
        		float u,v,u2,v2;
        		u  = x * invTexWidth;
        		v  = y * invTexHeight;
        		u2 = (x + w) * invTexWidth;
        		v2 = (y + h) * invTexHeight;
        		
        		float adjustX = 0.25f / texture.getWidth();
    			u += adjustX;
    			u2 -= adjustX;
    			float adjustY = 0.25f / texture.getHeight();
    			v += adjustY;
    			v2 -= adjustY;
    			
    			regions[xi][yi] = new FlippedTextureRegion(tileSheetTexture, u, v, u2, v2);
        	}
        }
        
        w = tileSheetTexture.getWidth();
        h = tileSheetTexture.getHeight();
	}
	
	public void dispose()
	{
		tileSheetTexture.dispose();
	}
	
	public FlippedTextureRegion getTile(int xi, int yi)
	{
		return regions[xi][yi];
	}
	
	public int getWidthInPixels() { return w; }
	public int getHeightInPixels() { return h; }
	
	public int getWidthInIndicies() { return w/TILESIZE; }
	public int getHeightInIndicies() { return h/TILESIZE; }
	
}
