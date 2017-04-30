package whitealchemy;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;

public class QOpenPlatform extends QPlatform
{
	/*
	 * OpenPlatform pseudocode:
	 * Never check for collisions if character speed is negative(going up)
	 * After that ONLY check the bottom line of the character for collision. because a player might jump only halfway through and has to fall again
	 * 
	 * holding down will simply push the character a few pixels and then the aforementioned code will do the rest
	 */
	
	public QOpenPlatform(TextureRegion texr, float _x, float _y, World world)
	{
		super(texr, _x, _y, world);
	}
	
}
