package whitealchemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteEffect extends Sprite
{
	public float rotationSpeed = 0f;
	
	private float calcRota = 0;
	
	public SpriteEffect(Texture texture)
	{
		super(texture);
	}
	
	public SpriteEffect(TextureRegion region)
	{
		super(region);
	}
	
	public void update()
	{
		calcRota = getRotation()+rotationSpeed;
		if (calcRota >= 360f) calcRota-=360f;
		setRotation(calcRota);
		
		
//		setScale(gss);
//		setAlpha(0.94f*gss);
//		setRotation(gsr);
	}
	
}
