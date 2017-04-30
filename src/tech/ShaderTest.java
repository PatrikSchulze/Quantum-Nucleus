package tech;

import whitealchemy.FlippedTextureRegion;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderTest extends ApplicationAdapter
{
	SpriteBatch spriteBatch;
	SpriteCache spriteCache;
	OrthographicCamera cam;
	FlippedTextureRegion tex;
	ShaderProgram shader;
	BitmapFont font;
	int spc = -1;
	
	float fa = 0.001f;

	public static void main(String args[])
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 0;
		config.vSyncEnabled = true;	
		config.useGL30 = true;
		config.width  = 1600;
		config.height = 900;
		config.fullscreen = false;
		new LwjglApplication(new ShaderTest(), config);
	}
	
	@Override public void create()
	{
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(true);
		tex = new FlippedTextureRegion(Gdx.files.internal("content/onebig.png"));
		font = new BitmapFont(true);
		
		ShaderProgram.pedantic = false;
		shader = new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/swirl2.frag"));
//		shader = new ShaderProgram(Gdx.files.internal("content/shaders/swirl2.vert"), Gdx.files.internal("content/shaders/default.frag"));
		System.out.println(shader.getLog());
		System.out.println("Compiled: "+shader.isCompiled());
		
		spriteBatch = new SpriteBatch();
		
		spriteCache = new SpriteCache(1000, false);
		spriteCache.setShader(shader);
		
		spriteCache.beginCache();
			spriteCache.add(tex, (Gdx.graphics.getWidth()*0.5f)-256, (Gdx.graphics.getHeight()*0.5f)-256, 512, 512);
		spc = spriteCache.endCache();
	}
	
	@Override public void render()
	{
		fa+=0.002f;
		System.out.println(fa);
		
		shader.begin();
		shader.setUniformf("amount", fa);
		shader.end();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		cam.update();
		spriteBatch.setProjectionMatrix(cam.combined);
		spriteCache.setProjectionMatrix(cam.combined);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		spriteCache.begin();
			spriteCache.draw(spc);
		spriteCache.end();
		
		spriteBatch.begin();
			font.draw(spriteBatch, "FA: "+fa, 10, 10);
		spriteBatch.end();
	}
	
}
