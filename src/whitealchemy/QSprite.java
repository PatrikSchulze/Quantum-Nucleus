package whitealchemy;

import game.Shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class QSprite extends QElement
{
	public Body body;
	public TextureRegion treg;
	public Animation animation;
	public Color color = null;
	private Color prevColor = null;
	public Color flash = null;

	public QSprite(TextureRegion texr, float _x , float _y, World world)
	{
		super(_x, _y, texr.getRegionWidth(), texr.getRegionHeight());
		treg   = texr;
		
		buildLightBox(world);
	}
	
	private void buildLightBox(World world)
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		body = world.createBody(bodyDef);
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(this.width*0.5f, this.height*0.5f);
		body.setFixedRotation(false);
		body.createFixture(shape, 0f);
//		body.setGravityScale(8f);
		
//		MassData md = body.getMassData();
//		float scaleFactor = 2000f / md.mass;
//		md.mass *= scaleFactor;
//		md.I 	*= scaleFactor;
//		body.setMassData(md);
		
		
	}
	
	public void compute()
	{
		if (animation != null) animation.update();
		speed.add(acceleration);
		x+=speed.x;
		y+=speed.y;
		
		if (flash != null)
		{
			if (flash.a <= 0f) flash = null;
			else
			{
				flash.a-=0.01f;
			}
		}
		
		body.setTransform(x+(width*0.5f), y+(height*0.5f), 0);
	}
	
	private void setPositionAccordingToBox2D()
	{
		x = body.getPosition().x - width*0.5f;
		y = body.getPosition().y - height*0.5f;
	}
	
	public void render(SpriteBatch spriteBatch)
	{
//		body.setTransform(x+(width*0.5f), y+(height*0.5f), 0);
		
		spriteBatch.setShader(Shaders.defaultShader);
		draw(spriteBatch);
		
		if (flash != null)
		{
			spriteBatch.end();
			
			Shaders.flash.begin();
				Shaders.flash.setUniformf("in_r", flash.r);
				Shaders.flash.setUniformf("in_g", flash.g);
				Shaders.flash.setUniformf("in_b", flash.b);
				Shaders.flash.setUniformf("amount", flash.a);
			Shaders.flash.end();
			spriteBatch.setShader(Shaders.flash);
			spriteBatch.begin();
				draw(spriteBatch);
			spriteBatch.end();
			
			spriteBatch.setShader(Shaders.defaultShader);
			spriteBatch.begin();
		}
	}
	
	private void draw(SpriteBatch spriteBatch)
	{
		if (color == null)
		{
			spriteBatch.draw(treg, x, y);
		}
		else
		{
			prevColor = spriteBatch.getColor();
			spriteBatch.setColor(color);
			spriteBatch.draw(treg, x, y);
			spriteBatch.setColor(prevColor);
		}	
	}
	
	public void createDefaultRedFlash()
	{
		flash = new Color(1.0f, 0.0f, 0.0f, 0.65f);
	}
	
}
