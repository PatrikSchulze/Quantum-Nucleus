package gfx;

public class Effects
{
	public static QNEffect getEffect(String in, float _x, float _y)
	{
		return new GravityEffect(_x, _y, 8, 1f);
	}
}
