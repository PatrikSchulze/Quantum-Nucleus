package whitealchemy;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

public class QPlatform extends QEntity
{
	public enum MOVEMENT_DIRECTION { HORIZONTAL, VERTICAL, NONE; }
	public final float DEFAULT_MOVEMENT_SPEED = 2f;
	
	public MOVEMENT_DIRECTION movementDirection = MOVEMENT_DIRECTION.NONE;
	public float firstWaypoint, secondWaypoint;
	
	public QPlatform(TextureRegion texr, float _x, float _y, World world)
	{
		super(texr, _x, _y, world);
	}
	
	public void setMovement(MOVEMENT_DIRECTION movDir, float pixelAmount)
	{
		setMovement(movDir, pixelAmount, DEFAULT_MOVEMENT_SPEED);
	}
	
	public void setMovement(MOVEMENT_DIRECTION movDir, float pixelAmount, float _speed)
	{
		movementDirection = movDir;
		
		if (movementDirection == MOVEMENT_DIRECTION.HORIZONTAL)
		{
			speed.x = _speed;
			
			firstWaypoint  = centerX() - (pixelAmount/2f);
			secondWaypoint = centerX() + (pixelAmount/2f);
		}
		else if (movementDirection == MOVEMENT_DIRECTION.VERTICAL)
		{
			speed.y = _speed;
			
			firstWaypoint  = centerY() - (pixelAmount/2f);
			secondWaypoint = centerY() + (pixelAmount/2f);
		}
	}
	
	public void compute()
	{
		if (movementDirection == MOVEMENT_DIRECTION.HORIZONTAL)
		{
			if (speed.x > 0.0f) // going right
			{
				if (centerX() >= secondWaypoint)
				{
					speed.x = -speed.x;
				}
			}
			else if (speed.x < 0.0f) // going left
			{
				if (centerX() <= firstWaypoint)
				{
					speed.x = -speed.x;
				}
			}
		}
		else if (movementDirection == MOVEMENT_DIRECTION.VERTICAL)
		{
			if (speed.y > 0.0f) // going down
			{
				if (centerY() >= secondWaypoint)
				{
					speed.y = -speed.y;
				}
			}
			else if (speed.y < 0.0f) // going up
			{
				if (centerY() <= firstWaypoint)
				{
					speed.y = -speed.y;
				}
			}
		}
		
		super.compute();
	}

}
