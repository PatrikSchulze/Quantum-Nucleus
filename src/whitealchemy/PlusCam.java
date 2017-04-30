package whitealchemy;

import com.badlogic.gdx.graphics.OrthographicCamera;

/*
 * usage:
 * 
 * camera.startQuake(3000, 0.2f); length in MS
 * 
 * call camera.update() every frame
 * 
 * this code only works properly if the camera position is set every frame, like camera.position = character bla
 * if you dont the camera will move away from the source.
 * you could fix this with a hook of course.
 * 
 */
public class PlusCam extends OrthographicCamera
{
	boolean isQuaking = false;
	float quakeStrength;
	long quakeEndPoint;
	
	public PlusCam()
	{
	}
	
	public PlusCam(float viewportWidth, float viewportHeight)
	{
		super(viewportWidth, viewportHeight);
	}
	
	public void startQuake(long length, float strengthFactor)
	{
		quakeStrength = strengthFactor;
		quakeEndPoint = System.currentTimeMillis()+length;
		isQuaking = true;
	}
	
	@Override public void update()
	{
		updateQuake();
		super.update();
		
	}
	
	@Override public void update(boolean updateFrustum)
	{
		updateQuake();
		super.update(updateFrustum);
	}
	
	private void updateQuake()
	{
		if (!isQuaking) return;
		
        position.x+= (int)(35*quakeStrength-(Math.random()*70*quakeStrength));
        position.y+= (int)(35*quakeStrength-(Math.random()*70*quakeStrength));
        
        if (System.currentTimeMillis() >= quakeEndPoint)
        {
        	isQuaking = false;
        }
	}
	
}
