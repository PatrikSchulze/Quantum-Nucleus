package tech;

import whitealchemy.FlippedTextureRegion;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class FBOTest extends ApplicationAdapter {
	 
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.useGL30 = true;
		cfg.width = 1600;
		cfg.height = 900;
		cfg.resizable = false;
 
		new LwjglApplication(new FBOTest(), cfg);
	}
 
	FlippedTextureRegion tex;
	FrameBuffer fbo;
	SpriteCache cache;
	SpriteBatch batch;
	OrthographicCamera cam;
 
	@Override
	public void create()
	{
		tex = new FlippedTextureRegion(Gdx.files.internal("content/onebig.png"));

		fbo = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 
		batch = new SpriteBatch();
		cache = new SpriteCache(8000, false);
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(true);
 
		renderOnFbo();
	}
 
	protected void renderOnFbo()
	{
		// make our offscreen FBO the current buffer
		fbo.begin();
			Gdx.gl.glClearColor(1f, 0f, 0f, 1f);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
			int b = -1;
			cache.beginCache();
				cache.add(tex, 0, 0, 512, 512);
			b = cache.endCache();
			
			cache.begin();
				cache.draw(b);
			cache.end();
		fbo.end();
	}
 
	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
 
		batch.begin();
			batch.draw(fbo.getColorBufferTexture(), 0, 0);
		batch.end();
	}
 
}