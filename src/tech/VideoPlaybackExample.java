package tech;

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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

 /**
  *
  * @author Patrik Schulze, ENMA Games
  * Twitter: @ENMA_Pat
  * www.enmagames.com
  */
public class VideoPlaybackExample implements ApplicationListener
{
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	
	private OrthographicCamera camera;
	
	private boolean skipDialog = false;
	private TextureRegion imgPixel;
	private Color prevColor = new Color(0,0,0,0);
	private Color curColor = new Color(0,0,0,0);
	GlyphLayout glyphL = new GlyphLayout();
    
    GStreamerPlayer videoplayer	= null;
 
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
		
		imgPixel          = new TextureRegion(new Texture(Gdx.files.internal("content/white.png")));
		imgPixel.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		camera = new OrthographicCamera();
		camera.setToOrtho(true);
	}
	
	public static void main(String[] args)
    {
		System.setProperty("gstreamer.dir", "vlib_gst");
		
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Test";
		cfg.width = 1280;
		cfg.height = 720;
		
		new LwjglApplication(new VideoPlaybackExample(), cfg);
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
				if (videoplayer == null)
	        	{
	        		videoplayer = new GStreamerPlayer(Gdx.files.internal("video/infamous.ogv").file());
	        		try{Thread.sleep(1300);	}catch (InterruptedException e)	{}
	        		videoplayer.play();
	        	}
				else
				{
					if (skipDialog)
					{
						videoplayer.destroy();
						videoplayer = null;
						skipDialog = false;
					}
					else
					{
						videoplayer.pause();
					}
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
			else if (keycode == Keys.BACKSPACE)
			{
				skipDialog = !skipDialog;
				
				if (skipDialog == false) curColor.a = 0f;
			}
			
			return true;
		}
	}

 
	@Override
	public void render()
	{
		if (skipDialog)
		{
			if (curColor.a < 0.80f)
			{
				curColor.a+=0.033333f;
			}
		}
		
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		spriteBatch.setProjectionMatrix(camera.combined);
		
//		//RENDERING
		
		if (videoplayer != null)
		{
			if(!videoplayer.isDone())
            {
            	videoplayer.updateAndRender();
            }
    		else
    		{
    			videoplayer.destroy();
				videoplayer = null;
    		}
		}
		
		spriteBatch.begin();
			font.setColor(Color.WHITE);
			if (videoplayer == null)
			{
				glyphL.setText(font, "Press Enter to start playing video.");
				font.draw(spriteBatch, "Press Enter to start playing video.", (Gdx.graphics.getWidth()/2)-(glyphL.width/2),  (Gdx.graphics.getHeight()/2)-(font.getLineHeight()/2));
			}
			else
			{
				if (skipDialog)
				{
					prevColor = spriteBatch.getColor();
					spriteBatch.setColor(curColor);
					spriteBatch.draw(imgPixel, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
					spriteBatch.setColor(prevColor);
					
					glyphL.setText(font,   "Press Enter to skip the Video");
					font.draw(spriteBatch, "Press Enter to skip the Video", (Gdx.graphics.getWidth()/2)-(glyphL.width/2),  (Gdx.graphics.getHeight()/2)-(font.getLineHeight()/2));
					
					glyphL.setText(font,   "Press Backspace to continue watching");
					font.draw(spriteBatch, "Press Backspace to continue watching", (Gdx.graphics.getWidth()/2)-(glyphL.width/2),  (Gdx.graphics.getHeight()/2)-(font.getLineHeight()/2)+(font.getLineHeight()*1.5f));
				}
				else
				{
					glyphL.setText(font,   "Press Backspace to Skip the video");
					font.draw(spriteBatch, "Press Backspace to Skip the video", Gdx.graphics.getWidth()-glyphL.width-15,  10);
				}
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
	public void dispose() {
	}
	
	
	
	
}