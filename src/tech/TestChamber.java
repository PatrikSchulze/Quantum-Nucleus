package tech;

import game.Content;
import gfx.AreaElectricity;
import whitealchemy.Trigonometrics;
import whitealchemy.Util;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class TestChamber extends ApplicationAdapter
{
	SpriteBatch spriteBatch;
	OrthographicCamera cam;
	BitmapFont font;
	AreaElectricity sphereElec;
	AreaElectricity lineElec[];
	
	public static void main(String args[])
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 2;
		config.vSyncEnabled = true;	
		config.width  = 1280;
		config.height = 720;
		config.fullscreen = false;
		new LwjglApplication(new TestChamber(), config);
	}
	
	@Override public void create()
	{
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(true);
		font = new BitmapFont(true);
		Gdx.input.setInputProcessor(new LeInputs());
		
		spriteBatch = new SpriteBatch();
		Content.init();
		
		int lineNums = 6;
		lineElec = new AreaElectricity[lineNums];
		Vector2 sp = new Vector2(400, 350);
		for(int i=0;i<lineNums;i++)
		{
			lineElec[i] = new AreaElectricity(AreaElectricity.SHAPE.LINE, 38, sp, Trigonometrics.getOrbitLocationDeg(sp.x, sp.y, 45+i*(360.0f/lineNums), 190), new Color(0.1568627f, 0.5882352f, 0.901960f, 1f));
		}
		
		sphereElec = new AreaElectricity(AreaElectricity.SHAPE.CIRCLE, 160, new Vector2(500, 350), Trigonometrics.getOrbitLocationDeg(500, 350, 0, 100), new Color(0.1568627f, 0.5882352f, 0.901960f, 1f));
	}
	
	@Override public void render()
	{
		sphereElec.update();
		for(int i=0;i<lineElec.length;i++)
		{
//			lineElec[i].update();
		}
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		cam.update();
		spriteBatch.setProjectionMatrix(cam.combined);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		spriteBatch.begin();
		
		sphereElec.render(spriteBatch);
		for(int i=0;i<lineElec.length;i++)
		{
//			lineElec[i].render(spriteBatch);
		}

		
//		for (int i=0;i<360f;i+=(360f/12))
//		{
//			Vector2 v = Trigonometrics.getOrbitLocationDeg(Gdx.graphics.getWidth()*0.5f, Gdx.graphics.getHeight()*0.5f, i, 300);
//			GFX.drawP2PLightning(spriteBatch, Gdx.graphics.getWidth()*0.5f, Gdx.graphics.getHeight()*0.5f, v.x, v.y, Util.getRandom(60f, 140f), Util.getRandom(0.8f, 3.8f), 3, 2);
//		}
		
//		GFX.drawChainLightning(spriteBatch, v, 3f, 3);
			
//		spriteBatch.draw(texGear, 300, 300, 0, 0, texGear.getWidth(), texGear.getHeight(), 1f, 1f, 90, 0, 0, texGear.getWidth(), texGear.getHeight(), false, false);
//		drawLineAlt(spriteBatch, 400, 400, 800, 800, 1, texSp);
		
			font.draw(spriteBatch, "FPS: "+Gdx.graphics.getFramesPerSecond(), 10, 10);
			font.draw(spriteBatch, "Mouse: "+Gdx.input.getX()+","+Gdx.input.getY(), 10, 25);
			
			font.draw(spriteBatch, "Dissolve: "+sphereElec.dissolveMin+" ~ "+sphereElec.dissolveMax, 10, Gdx.graphics.getHeight()-30);
			font.draw(spriteBatch, "Seg Amounts: "+sphereElec.segmentsAmountMin+" ~ "+sphereElec.segmentsAmountMax, 10, Gdx.graphics.getHeight()-50);
			font.draw(spriteBatch, "Seg Length: "+sphereElec.segmentsLengthMin+" ~ "+sphereElec.segmentsLengthMax, 10, Gdx.graphics.getHeight()-70);
			font.draw(spriteBatch, "Seg Angle: "+sphereElec.segmentsAngleMin+" ~ "+sphereElec.segmentsAngleMax, 10, Gdx.graphics.getHeight()-90);
			font.draw(spriteBatch, "Thickness: "+sphereElec.boltThicknessMin+" ~ "+sphereElec.boltThicknessMax, 10, Gdx.graphics.getHeight()-110);
			
			
		spriteBatch.end();
		
		
		
//		Util.syncDisplay(60);
	}
	
	
	class LeInputs extends InputAdapter
	{
		public boolean keyDown(int keycode)
		{
			if (keycode == Keys.Y)
			{
				sphereElec.boltThicknessMin--;
			}
			if (keycode == Keys.X)
			{
				sphereElec.boltThicknessMin++;
			}
			if (keycode == Keys.C)
			{
				sphereElec.boltThicknessMax--;
			}
			if (keycode == Keys.V)
			{
				sphereElec.boltThicknessMax++;
			}
			
			
			if (keycode == Keys.Q)
			{
				sphereElec.dissolveMin-=0.005f;
			}
			if (keycode == Keys.W)
			{
				sphereElec.dissolveMin+=0.005f;
			}
			if (keycode == Keys.A)
			{
				sphereElec.dissolveMax-=0.005f;
			}
			if (keycode == Keys.S)
			{
				sphereElec.dissolveMax+=0.005f;
			}
			
			
			if (keycode == Keys.E)
			{
				sphereElec.segmentsAmountMin-=1;
			}
			if (keycode == Keys.R)
			{
				sphereElec.segmentsAmountMin+=1;
			}
			if (keycode == Keys.D)
			{
				sphereElec.segmentsAmountMax-=1;
			}
			if (keycode == Keys.F)
			{
				sphereElec.segmentsAmountMax+=1;
			}
			
			
			if (keycode == Keys.T)
			{
				sphereElec.segmentsLengthMin-=1;
			}
			if (keycode == Keys.Z)
			{
				sphereElec.segmentsLengthMin+=1;
			}
			if (keycode == Keys.G)
			{
				sphereElec.segmentsLengthMax-=1;
			}
			if (keycode == Keys.H)
			{
				sphereElec.segmentsLengthMax+=1;
			}
			
			
			if (keycode == Keys.U)
			{
				sphereElec.segmentsAngleMin-=1;
			}
			if (keycode == Keys.I)
			{
				sphereElec.segmentsAngleMin+=1;
			}
			if (keycode == Keys.J)
			{
				sphereElec.segmentsAngleMax-=1;
			}
			if (keycode == Keys.K)
			{
				sphereElec.segmentsAngleMax+=1;
			}
			
			
			
			

			
			if (keycode == Keys.UP)
			{
			}
			if (keycode == Keys.DOWN)
			{
			}

			if (keycode == Keys.PAGE_UP)
			{
			}
			if (keycode == Keys.PAGE_DOWN)
			{
			}
			
			if (keycode == Keys.SPACE)
			{
				
			}

			return true;
		}
		
		@Override public boolean mouseMoved(int screenX, int screenY)
		{
			return super.mouseMoved(screenX, screenY);
		}
		
		public boolean touchDown(int x, int y, int pointer, int button)
		{
			System.out.println("button: "+button);
			
			return super.touchDown(x, y, pointer, button);
		}
		
		@Override public boolean scrolled(int amount)
		{
//			if (amount > 0)
//			else if (amount < 0)
			
			return super.scrolled(amount);
		}
	}
	
}
