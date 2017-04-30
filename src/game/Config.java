package game;

import java.io.File;
import java.io.IOException;

import whitealchemy.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Config
{
	public final static String RESOFILE = System.getProperty("user.home")+"/.qnreso";
	public final static String PREF_ID = "QN_CONFIG";
	public static Preferences prefs;
	
	public static int width;
	public static int height;
	public static boolean fullscreen;
	public static boolean vsync;
	
	public static boolean drawLighting;
	
	public static void init()
	{
		prefs			= Gdx.app.getPreferences(PREF_ID);
		
		width			= prefs.getInteger("width");
		height			= prefs.getInteger("height");
		fullscreen 		= prefs.getBoolean("fullscreen");
		vsync			= prefs.getBoolean("vsync");
		
		drawLighting			= prefs.getBoolean("drawLighting");
		
		fixFirstTimeUse();
	}
	
	public static String getResolutionString()
	{
		if (new File(RESOFILE).exists())
		{
			return Util.readFromFile(RESOFILE);
		}
		else
		{
			return null;
		}
	}
	
	public static void save(QNGame game)
	{
		width			= Gdx.graphics.getWidth();
		height			= Gdx.graphics.getHeight();
		fullscreen 		= Gdx.graphics.isFullscreen();
		
		prefs.putInteger("width", width);
		prefs.putInteger("height", height);
		prefs.putBoolean("fullscreen", fullscreen);
		
		prefs.putBoolean("drawLighting", drawLighting);
		
		prefs.flush();
		
		File resoFile = new File(RESOFILE);
		if (!resoFile.exists())
		{
			try	{resoFile.createNewFile(); } catch (IOException e)	{e.printStackTrace();}
		}
		Util.writeToFile(""+Gdx.graphics.getWidth()+","+Gdx.graphics.getHeight()+","+Gdx.graphics.isFullscreen(), RESOFILE);
	}
	
	/*
	 * When the prefs are new, some number values may be 0. So lets detect that and set appropriate defaults.
	 */
	private static void fixFirstTimeUse()
	{
		boolean doneChanges = false;
		
		if (width == 0 || height == 0)
		{
//			width = 1024;
//			height = 576;
			width = 1600;
			height = 900;
			fullscreen = false;
			drawLighting = true;
			doneChanges = true;
		}
		
		
		
		
		
		if (doneChanges) prefs.flush();
	}
	
	public static boolean getBoolean(String in)
	{
		return prefs.getBoolean(in);
	}
	
	public static void putBoolean(String n, boolean in)
	{
		prefs.putBoolean(n, in);
	}
	

	public static int getInteger(String in)
	{
		return prefs.getInteger(in);
	}
	
	public static void putInteger(String n, int in)
	{
		prefs.putInteger(n, in);
	}

}
