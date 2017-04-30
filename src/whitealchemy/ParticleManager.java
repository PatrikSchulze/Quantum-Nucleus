package whitealchemy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class ParticleManager
{
	/*
	 * Effects need Hitboxes
	 * 
	 */
	
	private static HashMap<String, ParticleEffect> 		templateEffects;
	private static HashMap<String, ParticleEffectPool> 	pools;
	
	private static Array<PooledEffect> liveEffects = new Array<PooledEffect>();
	
	public static void initialize()
	{
		//load all icons
		
		templateEffects	= new HashMap<String, ParticleEffect>();
		pools 	= new HashMap<String, ParticleEffectPool>();
		
		ArrayList<String> fileList = new ArrayList<String>();
		fileList.add("dmgsmoke");
		fileList.add("expl01");
		fileList.add("expl02");
		fileList.add("expl03");
		fileList.add("expl04");
		fileList.add("rocketdust");
		fileList.add("rocketfireball");
		fileList.add("rocketsmoke");
		fileList.add("rocketsmokelong");
		fileList.add("rocketspark");
		fileList.add("spark");
		
		for (String eName : fileList)
		{
			ParticleEffect e = new ParticleEffect();
	    	e.setFlip(false, true);
	    	e.load(Gdx.files.internal("content/particles/"+eName+".p"), Gdx.files.internal("content/particles"));
	    	
	    	templateEffects.put(eName, e);
	    	
	    	ParticleEffectPool ePool = new ParticleEffectPool(e, 3, 8);
	    	pools.put(eName, ePool);
		}
	}
	
	public static PooledEffect getNewPooledEffect(String name)
	{
		ParticleEffectPool neededPool = pools.get(name);
		if (neededPool == null)
		{
			System.out.println("Error: Particle Effect \""+name+"\" not found.");
			return null;
		}
		
		PooledEffect e = neededPool.obtain();
		
//		//e.setFlip(false, true); //not sure if needed
		return e;
	}
	
	public static void addEffect(String name, float x, float y)
	{
		ParticleEffectPool neededPool = pools.get(name);
		if (neededPool == null)
		{
			System.out.println("Error: Particle Effect \""+name+"\" not found.");
			return;
		}
		
		PooledEffect e = neededPool.obtain();
		
//		//e.setFlip(false, true); //not sure if needed
		e.setPosition(x, y);
		liveEffects.add(e);
	}
	
	public static void addEffect(String name, float x, float y, boolean additiveLight)
	{
		ParticleEffectPool neededPool = pools.get(name);
		if (neededPool == null)
		{
			System.out.println("Error: Particle Effect \""+name+"\" not found.");
			return;
		}
		
		PooledEffect e = neededPool.obtain();
		
		//one effect may have some emitters which are additive and some which aren't
		for (int i=0;i<e.getEmitters().size; i++)
		{
			e.getEmitters().get(i).setAdditive(additiveLight);
		}
		
//		//e.setFlip(false, true); //not sure if needed
		e.setPosition(x, y);
		liveEffects.add(e);
	}
	
	public static void updateAndRender(SpriteBatch spriteBatch, float delta)
	{
		for (int i = liveEffects.size - 1; i >= 0; i--)
		{
	        PooledEffect effect = liveEffects.get(i);

	        effect.draw(spriteBatch, delta);
	        
	        if (effect.isComplete())
	        {
                effect.free();
                liveEffects.removeIndex(i);
	        }
		}
	}
	
	//performance intensive, only when needed
//	public static int getAllAliveCount()
//	{
//		int rr = 0;
//		for (int i = effects.size - 1; i >= 0; i--)
//		{
//	        PooledEffect effect = effects.get(i);
//	        if (!effect.isComplete())
//	        {
//	        	for (int j = 0; j < effect.getEmitters().size; j++)
//	        	{
//	        		rr+=effect.getEmitters().get(j).getActiveCount();
//	        	}
//	        }
//		}
//		
//		return rr;
//	}
	
	public static int getAliveEmitterCount()
	{
		return liveEffects.size;
	}
	
	public static void clear()
	{
		// Reset all effects:
		for (int i = liveEffects.size - 1; i >= 0; i--)
		{
			liveEffects.get(i).free();
		}
		liveEffects.clear();
	}
}
