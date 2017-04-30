package tech;

import java.util.ArrayList;

import whitealchemy.Util;

public class ConsoleTests
{
	static ArrayList<DelayedThing> codes = new ArrayList<DelayedThing>(); 
	
	public static void main(String[] args)
	{
		DelayedThing thing = new DelayedThing(9, new DelayedThing.Code(){@Override public void execute(){System.out.println("CODE MAN !");}} );
		codes.add(thing);
		
		int iter = 0;
		while (true)
		{
			System.out.println("Iteration: "+iter);
			for (int i = codes.size() - 1; i >= 0; i--)
			{
				codes.get(i).update();
				if (codes.get(i).done)		codes.remove(i);
			}
			iter++;
			Util.sleep(200);
		}
		
	}

}
