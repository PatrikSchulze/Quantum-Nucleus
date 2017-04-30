package game;

import com.badlogic.gdx.graphics.Color;

public class LightData
{
	public int rays;
	public Color color;
	public float distance;
	public float x,y;
//	public float directionDegree;
//	public float coneDegree;
	public boolean xray;
	public boolean staticLight;
	
	public LightData() { /* kryo */ }
	
//	public LightData(int rays, Color color, float distance, float x, float y, float directionDegree, float coneDegre, boolean xray)
//	{
//		this.rays = rays;
//		this.color = color;
//		this.distance = distance;
//		this.x = x;
//		this.y = y;
//		this.directionDegree = directionDegree;
//		this.coneDegree = coneDegree;
//		this.xray = xray;
//	}
	
	public LightData(int rays, Color color, float distance, float x, float y, boolean xray, boolean _static)
	{
		this.rays = rays;
		this.color = color;
		this.distance = distance;
		this.x = x;
		this.y = y;
		this.xray = xray;
		staticLight = _static;
	}
	
}
