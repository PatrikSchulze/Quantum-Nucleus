package whitealchemy;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class QBox extends Rectangle
{
	private Vector2 calcVector = new Vector2(0,0);
	
	public QBox(float _x, float _y, float _w, float _h)
	{
		super(_x,_y,_w,_h);
	}
	
	public float centerX()
	{
		return x+(width/2f);
	}
	
	public float centerY()
	{
		return y+(height/2f);
	}
	
	public float bottom()
	{
		return y+height;
	}
	
	public float right()
	{
		return x+width;
	}
	
	public Vector2 getPosition()
	{
		calcVector.set(x,y);
		return calcVector;
	}
	
	public Vector2 getCenterPosition()
	{
		calcVector.set(centerX(),centerY());
		return calcVector;
	}
}
