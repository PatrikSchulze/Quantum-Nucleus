package whitealchemy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class QElement extends QBox
{
	public Vector2 speed = new Vector2(0,0);
	public Vector2 acceleration = new Vector2(0,0);
	
	public QElement(float _x, float _y, float _w, float _h)
	{
		super(_x, _y, _w, _h);
	}
	
}
