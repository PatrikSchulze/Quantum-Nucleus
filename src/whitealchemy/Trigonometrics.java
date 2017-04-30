package whitealchemy;

import whitealchemy.SquareRoot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Trigonometrics
{
	public static final int OUT_LEFT = 1;
    public static final int OUT_TOP = 2;
    public static final int OUT_RIGHT = 4;
    public static final int OUT_BOTTOM = 8;
	
	private static float dx, dy, angle, distance;
	private static Vector2 returnVector = new Vector2(0,0);
	
	public static boolean rectOverlapsLine(Rectangle rect, double x1, double y1, double x2, double y2)
	{
		int out1, out2;
		if ((out2 = outcode(rect, x2, y2)) == 0) {
		    return true;
		}
		while ((out1 = outcode(rect, x1, y1)) != 0) {
		    if ((out1 & out2) != 0) {
			return false;
		    }
		    if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
			double x = (double)rect.x;
			if ((out1 & OUT_RIGHT) != 0) {
			    x += (double)rect.width;
			}
			y1 = y1 + (x - x1) * (y2 - y1) / (x2 - x1);
			x1 = x;
		    } else {
			double y = (double)rect.y;
			if ((out1 & OUT_BOTTOM) != 0) {
			    y += (double)rect.height;
			}
			x1 = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
			y1 = y;
		    }
		}
		return true;
    }
	
	private static int outcode(Rectangle rect, double x, double y) {
	    /*
	     * Note on casts to double below.  If the arithmetic of
	     * x+w or y+h is done in float, then some bits may be
	     * lost if the binary exponents of x/y and w/h are not
	     * similar.  By converting to double before the addition
	     * we force the addition to be carried out in double to
	     * avoid rounding error in the comparison.
	     *
	     * See bug 4320890 for problems that this inaccuracy causes.
	     */
	    int out = 0;
	    if (rect.width <= 0) {
		out |= OUT_LEFT | OUT_RIGHT;
	    } else if (x < rect.x) {
		out |= OUT_LEFT;
	    } else if (x > rect.x + (double) rect.width) {
		out |= OUT_RIGHT;
	    }
	    if (rect.height <= 0) {
		out |= OUT_TOP | OUT_BOTTOM;
	    } else if (y < rect.y) {
		out |= OUT_TOP;
	    } else if (y > rect.y + (double) rect.height) {
		out |= OUT_BOTTOM;
	    }
	    return out;
	}
	
	public static float getDistanceFast(Vector2 v1, Vector2 v2)
	{
        return getDistanceFast(v1.x, v1.y, v2.x, v2.y);
	}
	
	public static float getDistanceAccurate(Vector2 v1, Vector2 v2)
	{
		return getDistanceAccurate(v1.x, v1.y, v2.x, v2.y);
	}
	
	public static float getDistanceFast(float srcX, float srcY, float relativeX, float relativeY)
	{
		dx = relativeX - srcX; 
		dy = relativeY - srcY; 
        return SquareRoot.fastSqrt((int)(dx*dx + dy*dy));
	}
	
	public static float getDistanceAccurate(float srcX, float srcY, float relativeX, float relativeY)
	{
		dx = relativeX - srcX; 
		dy = relativeY - srcY; 
        return (float)Math.sqrt((double)(dx*dx + dy*dy));
	}
	

	public static float getAngleBetweenPointsDeg(Vector2 srcv, Vector2 dstv)
	{
		return getAngleBetweenPointsDeg(srcv.x, srcv.y, dstv.x, dstv.y);
	}
	
	public static float getAngleBetweenPointsRad(Vector2 srcv, Vector2 dstv)
	{
		return getAngleBetweenPointsRad(srcv.x, srcv.y, dstv.x, dstv.y);
	}
	
	public static float getAngleBetweenPointsDeg(float srcX, float srcY, float relativeX, float relativeY)
	{
		dx = relativeX - srcX; 
		dy = relativeY - srcY; 
		angle = (180.0f/MathUtils.PI)*MathUtils.atan2((float)dy, (float)dx);
        return getAngleWithoutOverUndershootDeg(angle);
	}
	
	public static float getAngleBetweenPointsRad(float srcX, float srcY, float relativeX, float relativeY)
	{
		dx = relativeX - srcX; 
		dy = relativeY - srcY; 
		angle = MathUtils.atan2((float)dy, (float)dx);
        return getAngleWithoutOverUndershootRad(angle);
	}
	
	public static float getAngleWithoutOverUndershootDeg(float in)
	{
		in = (in % 360.0f);
		if (in < 0)in = 360.0f+in;
		return in;
	}
	
	public static float getAngleWithoutOverUndershootRad(float in)
	{
		in = (in % 2*MathUtils.PI);
		if (in < 0)in = 2*MathUtils.PI+in;
		return in;
	}

	public static Vector2 getOrbitLocationRad(float srcX, float srcY, float angle, float radius)
	{
		returnVector.set(srcX+(MathUtils.cos(angle)*radius), srcY+(MathUtils.sin(angle)*radius));
		return returnVector;
	}
	
	public static Vector2 getOrbitLocationDeg(float srcX, float srcY, float angle, float radius)
	{
		returnVector.set(srcX+(MathUtils.cosDeg(angle)*radius), srcY+(MathUtils.sinDeg(angle)*radius));
		return returnVector;
	}
	
	public static Vector2 getSpeedDeg(float srcX, float srcY, float relativeX, float relativeY, float speedFactor)
	{
		distance = getDistanceFast(srcX, srcY, relativeX, relativeY);
		
		if ( distance > speedFactor )
	    { 
	        // still got a way to go, so take a full step 
			returnVector.set((speedFactor*dx/distance), (speedFactor*dy/distance));
	    }
		else
		{
			returnVector.set(0, 0);
		}

		return returnVector;
	}
}
