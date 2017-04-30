package tech;

import java.util.concurrent.TimeUnit;

import gst.GStreamerLibrary;
import gst.GStreamerPlayer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

 /**
  *
  * @author Patrik Schulze, ENMA Games
  * Twitter: @ENMA_Pat
  * www.enmagames.com
  */
public class VideoLoopExample implements ApplicationListener
{
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private OrthographicCamera camera;
    
	boolean loopStarted = false;
    GStreamerPlayer videoplayer	= null;
    GlyphLayout glyphL = new GlyphLayout();
 
	@Override
	public void create()
	{
		try {
          GStreamerLibrary.init();
	      } catch (Exception e) {
	          e.printStackTrace();
	          throw new RuntimeException(e);
	      }
		
		Gdx.input.setInputProcessor(new OurInputListener());
		
		spriteBatch 	= new SpriteBatch();
		font 			= new BitmapFont(true);
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		camera = new OrthographicCamera();
		camera.setToOrtho(true);
		
		videoplayer = new GStreamerPlayer(Gdx.files.internal("video/loop.ogv").file());
//		videoplayer.play();
	}
	
	public static void main(String[] args)
    {
		System.setProperty("gstreamer.dir", "vlib_gst");
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Test";
		cfg.width = 1280;
		cfg.height = 720;
		
		new LwjglApplication(new VideoLoopExample(), cfg);
	}
 
	@Override
	public void resume()
	{
	}
	
	private class OurInputListener extends InputAdapter
	{
		@Override public boolean keyDown(int keycode)
		{
			if (keycode == Keys.ENTER)
			{
				if (!loopStarted)
				{
					videoplayer.play();
					loopStarted = true;
				}
			}
			else if (keycode == Keys.ESCAPE)
			{
				if (videoplayer != null)
				{
					videoplayer.destroy();
					videoplayer = null;
				}
				Gdx.app.exit();
			}
			else if (keycode == Keys.SPACE)
			{
				if (videoplayer != null)
				{
					videoplayer.pause();
				}
			}
			
			return true;
		}
	}

 
	@Override
	public void render()
	{
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		spriteBatch.setProjectionMatrix(camera.combined);
		
//		//RENDERING
		
		
		
		if (videoplayer != null)
		{
			videoplayer.updateAndRender();
			
			if(videoplayer.isDone())
            {
    			videoplayer.getPlayBin().seek(0, TimeUnit.NANOSECONDS);
    			videoplayer.done = false;
    		}
		}
		
		spriteBatch.begin();
			font.setColor(Color.WHITE);
			if (!loopStarted)
			{
				glyphL.setText(font, "Press Enter to start looping");
				font.draw(spriteBatch, "Press Enter to start looping", (Gdx.graphics.getWidth()/2)-(glyphL.width/2),  (Gdx.graphics.getHeight()/2)-(font.getLineHeight()/2));
			}
			else
			{
				glyphL.setText(font, "Use Space to pause");
				font.draw(spriteBatch, "Use Space to pause", (Gdx.graphics.getWidth()/2)-(glyphL.width/2),  (Gdx.graphics.getHeight()/2)-(font.getLineHeight()/2));
			}
			font.draw(spriteBatch, "FPS: "+Gdx.graphics.getFramesPerSecond(), 12,  10);
		spriteBatch.end();
	}
 
	@Override
	public void resize(int width, int height)
	{
	}
 
	@Override
	public void pause()
	{
		 
	}
 
	@Override
	public void dispose()
	{
		if (videoplayer != null)
		{
			videoplayer.stop();
			videoplayer.destroy();
			videoplayer = null;
		}
	}

	
}
