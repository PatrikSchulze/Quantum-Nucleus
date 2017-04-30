package game;

import game.scripts.BasicScript1;

public class ScriptControl
{
	public static void executeScript(String id, QNGame game)
	{
		if (id.equalsIgnoreCase("basic_script_1"))
		{
			new BasicScript1().execute(game);
		}
	}
	
}
