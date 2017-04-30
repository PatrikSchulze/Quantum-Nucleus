package tech;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;


public class DoTexturePacking
{
	public static void main(String[] args)
	{
		
    	TexturePacker.Settings settings = new TexturePacker.Settings();
    	settings.maxWidth = 512;
    	settings.maxHeight = 512;
    	settings.filterMag = TextureFilter.Linear;
    	settings.filterMin = TextureFilter.Linear;
    	
    	//TexturePacker.process(settings, "raw_sprites", "content/atlas", "big");
    	
    	TexturePacker.process(settings, "content/physics", "content/atlas", "physics");
    	
    	
    	
    	
    	
    	
//    	
//		TexturePacker.Settings settings = new TexturePacker.Settings();
//    	settings.maxWidth = 1024;
//    	settings.maxHeight = 1024;
//    	settings.filterMag = TextureFilter.Linear;
//    	settings.filterMin = TextureFilter.Linear;
//    	
//    	TexturePacker.process(settings, "content/my_samus", "content/atlas", "my_samus");
    	
	}
}
