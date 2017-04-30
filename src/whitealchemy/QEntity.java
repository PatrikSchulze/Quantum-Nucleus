package whitealchemy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class QEntity extends QSprite
{
	public static final float TERMINAL_VELOCITY = 29.0f; //weight ?
	public static final float MAX_WALK_SPEED = 5.0f;
	public static final long  MAX_TIME_JUMP  = 700;
	
	public boolean goLeft = false;
	public boolean goRight = false;
	public boolean facingRight = true;
	public long timeJumped = -1;
	
	public QEntity(TextureRegion texr, float _x , float _y, World world)
	{
		super(texr, _x, _y, world);
	}
	
	@Override public void compute()
	{
		super.compute();
		
		if (goLeft)
		{
			if (!treg.isFlipX())
			{
				treg.flip(true, false);
			}
			facingRight = false;
		}
		else if (goRight)
		{
			if (treg.isFlipX())
			{
				treg.flip(true, false);
			}
			facingRight = true;
		}
	}
}
