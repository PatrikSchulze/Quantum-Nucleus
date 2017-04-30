package game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders
{
	public static ShaderProgram swirl 				= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/swirl2.frag"));
	public static ShaderProgram grayscale 			= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/grayscale.frag"));
	public static ShaderProgram defaultShader 		= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/default.frag"));
	public static ShaderProgram motionblur 			= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/motionblur.frag"));
	public static ShaderProgram flash 				= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/flash.frag"));
	public static ShaderProgram pinch 				= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/pinch.frag"));
	public static ShaderProgram grayscaleVignette	= new ShaderProgram(Gdx.files.internal("content/shaders/default.vert"), Gdx.files.internal("content/shaders/grayscale_vign.frag"));
	
	static
	{
		grayscaleVignette.begin();
			grayscaleVignette.setUniformf("resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		grayscaleVignette.end();
		
		motionblur.begin();
			motionblur.setUniformf("screenSize", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		motionblur.end();
	}
	
}
