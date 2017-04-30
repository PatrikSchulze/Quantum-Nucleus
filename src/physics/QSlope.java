package physics;

import whitealchemy.Trigonometrics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class QSlope
{
	public enum TYPE {RIGHT_UP, LEFT_UP}
	public TYPE type;
	private Vector2 p1 = new Vector2(0,0);
	private Vector2 p2 = new Vector2(0,0);
	private static Vector2 cP3 = new Vector2(0,0);
	private static Vector2 cP4 = new Vector2(0,0);
	public float length = 0;
	
	public int halfID = -1; // 0 = far most. in LEFT type thats the left most slope, in Right the rightmost
	
	private Vector2 intersectionPoint = new Vector2(0,0);

	public QSlope() { /* Kryo */ }
	
	/*
	 * slopes are just lines nor triangles, just draw a line. whatever point is up more determines type
	 */
	
	public QSlope(TYPE _type, float x1, float y1, float x2, float y2)
	{
		p1.set(x1,y1);
		p2.set(x2,y2);
		type = _type;
		length = Trigonometrics.getDistanceAccurate(p1, p2);
	}
	
	public QSlope(TYPE _type, float x1, float y1, float x2, float y2, int _halfID)
	{
		p1.set(x1,y1);
		p2.set(x2,y2);
		type = _type;
		halfID = _halfID;
		length = Trigonometrics.getDistanceAccurate(p1, p2);
	}
	
	public QSlope(QSlope inslope)
	{
		p1.set(inslope.getP1());
		p2.set(inslope.getP2());
		type = inslope.type; // TODO REFERENCE BUG, enum != primitives
		halfID = inslope.halfID;
		length = inslope.length;
	}
	
	public Vector2 getP1() { return p1; }
	public Vector2 getP2() { return p2;	}

	public boolean overlaps(float _x1, float _y1, float _x2, float _y2)
	{
		//return Intersector.intersectLines(p1.x, p1.y, p2.x, p2.y, _x1, _y1, _x2, _y2, intersectionPoint);
		cP3.set(_x1, _y1);
		cP4.set(_x2, _y2);
		return Intersector.intersectSegments(p1, p2, cP3, cP4, intersectionPoint);
	}
	
	public Vector2 getIntersectionPoint() { return intersectionPoint; }
	
//	public boolean overlapsRect(Rectangle rect)
//	{
//		float[] v = new float[8];
//		v[0] = rect.x;  				v[1] = rect.y;
//		v[2] = rect.x+rect.width;  		v[3] = rect.y;
//		v[4] = rect.x+rect.width;  		v[5] = rect.y+rect.height;
//		v[6] = rect.x;  				v[7] = rect.y+rect.height;
////		v[0] = rect.x+(rect.width/2f)-2;	v[1] = rect.y;
////		v[2] = rect.x+(rect.width/2f)+2;  	v[3] = rect.y;
////		v[4] = rect.x+(rect.width/2f)+2;  	v[5] = rect.y+rect.height;
////		v[6] = rect.x+(rect.width/2f)-2;  	v[7] = rect.y+rect.height;
//		calcPoly.setVertices(v);
//		
//		Intersector.
//		
//		return Intersector.overlapConvexPolygons(this, calcPoly);
//	}
	
	public boolean overlapsSensorLeft(Rectangle rect)
	{
		boolean re = overlaps(rect.x, rect.y+rect.height, rect.x+2, rect.y+rect.height);
		if (!re) re = overlaps(rect.x, rect.y+rect.height-4, rect.x+2, rect.y+rect.height-4);
		if (!re) re = overlaps(rect.x, rect.y+rect.height-8, rect.x+2, rect.y+rect.height-8);
		if (!re) re = overlaps(rect.x, rect.y+rect.height-8, rect.x+2, rect.y+rect.height-8);
		
		return re;
	}
	
	public boolean overlapsSensorRight(Rectangle rect)
	{
		boolean re = overlaps(rect.x+rect.width-2, rect.y+rect.height, rect.x+rect.width, rect.y+rect.height);
		if (!re) re = overlaps(rect.x+rect.width-2, rect.y+rect.height-4, rect.x+rect.width, rect.y+rect.height-4);
		if (!re) re = overlaps(rect.x+rect.width-2, rect.y+rect.height-8, rect.x+rect.width, rect.y+rect.height-8);
		if (!re) re = overlaps(rect.x+rect.width-2, rect.y+rect.height-8, rect.x+rect.width, rect.y+rect.height-8);
		
		return re;
	}
	
	public boolean overlaps(Rectangle rect)
	{
		boolean r = overlaps(rect.x, rect.y+rect.height, rect.x+rect.width, rect.y+rect.height);
		if (!r) r = overlaps(rect.x, rect.y+rect.height-4, rect.x+rect.width, rect.y+rect.height-4);
		if (!r) r = overlaps(rect.x, rect.y+rect.height-8, rect.x+rect.width, rect.y+rect.height-8);
		if (!r) r = overlaps(rect.x, rect.y+rect.height-12, rect.x+rect.width, rect.y+rect.height-12);
		return r;
	}

}
