package tech;

public class DelayedThing
{
	Code code;
	int age = 0;
	int waitTime = 0; //in fames
	
	boolean done;
	
	public DelayedThing(int framesToWait, Code _code)
	{
		code = _code;
		done = false;
		waitTime = framesToWait;
		
		age = 0;
	}
	
	public void update()
	{
		if (age == waitTime)
		{
			code.execute();
			done = true;
		}
		
		age++;
	}
	
	static abstract class Code
	{
		public abstract void execute();
	}
	
	
}
