package game;

import com.badlogic.gdx.math.Rectangle;

public class MapScript
{
	public static enum TRIGGER { MAP_LAUNCH, COLLISION, ACTION }
	
	/*
	 * int lifespan; 0 = infinite ? and otherwise a number like 1 in most cases
	 * however if you want to create a npc talk says different things, it could go like this:
	 * 
	 * int ageFrame = 1;
	 * after execute: ageFrame++; if (ageFrame > lifespan) remove script from map
	 * 
	 * 
	 * id+"#"+ageFrame
	 * 
	 * eg:
	 * "blahblibo#1" -> "hello"
	 * "blahblibo#2" -> "yeah talking to me the second time"
	 * "blahblibo#3" -> "buzz off"
	 * 
	 * 
	 * 
	 * This would solve lifespan issue.
	 * 
	 * 
	 * However: QUESTION: what about script that should progress but then stop after wards at a certain point.
	 * Take example above but then it should say "buzz off" forever after that.
	 * 
	 * 
	 * Solution:
	 * Never increment ageFrame, never kill a script. Both of these are done via in script commands, you just put them in, if they are not in, it doesnt happen
	 * 
	 * 
	 */

	public TRIGGER trigger;
	public Rectangle rect;
	public String idprefix; //every script adds _#<ageFrame>
	public int ageFrame;
	
	public MapScript() { /*kryo*/ }
	
	public MapScript(Rectangle _rect, TRIGGER _trigger, String _id)
	{
		trigger = _trigger;
		rect = new Rectangle(_rect);
		idprefix = _id; 
		ageFrame = 1;
	}
	
	public MapScript(float x, float y, float width, float height, TRIGGER _trigger, String _id)
	{
		trigger = _trigger;
		rect = new Rectangle(x, y, width, height);
		idprefix = _id;
		ageFrame = 1;
	}
	
	public String getID()
	{
		return idprefix+"_#"+ageFrame;
	}
}
