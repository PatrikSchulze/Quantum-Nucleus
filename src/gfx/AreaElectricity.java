package gfx;

import whitealchemy.Trigonometrics;
import whitealchemy.Util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class AreaElectricity
{
	public static enum SHAPE {CIRCLE, LINE};
	SHAPE shape;
	Vector2 srcPoint;
	Vector2 dstPoint;
	float angle; // only useful for line
	int size;
	
	Bolt bolt[];

	public float dissolveMax = 0.09f;
	public int segmentsAmountMin = 10;
	public int segmentsAmountMax = 14;
	public int segmentsLengthMin = 2;
	public int segmentsLengthMax = 10;
	public int segmentsAngleMin = 0;
	public int segmentsAngleMax = 160;
	public int boltThicknessMin = 2;
	public int boltThicknessMax = 5;
	public float dissolveMin = 0.06f;
	
	Color color;
	
	/*
	 * Some performance issues:
	 * Due to the fact that this graphical effect code pretty much does the work the particle system would do,
	 * while not being fine tuned to performance, it lacks speed.
	 * Object invocations and Vector invocations are used way too much.
	 * One would have to object pool this / Fine tune it.
	 * 
	 * However good lightning with the particle system is pretty hard to do.
	 * Therefore: DO use this code, however be aware of the issue, and in worst case, do come back and fine tune.
	 */
	
	public AreaElectricity(SHAPE _shape, int boltsAmount, Vector2 _srcp, Vector2 _dstp, Color _color)
	{
		color = _color;
		bolt = new Bolt[boltsAmount];
		shape = _shape;
		srcPoint = _srcp.cpy();
		dstPoint = _dstp.cpy();
		size = (int)Trigonometrics.getDistanceFast(srcPoint, dstPoint);
		angle = Trigonometrics.getAngleBetweenPointsDeg(srcPoint, dstPoint);
	}
	
	public void render(SpriteBatch batch)
	{
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
//		long before = System.nanoTime();
		for (int i=0;i<bolt.length;i++)
		{
			if (bolt[i] != null)
			{
				bolt[i].render(batch);
			}
		}
//		System.out.println("Render ALL bolts: "+((System.nanoTime()-before)/1000L)+"us");
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void update()
	{
		for (int i=0;i<bolt.length;i++)
		{
			if (bolt[i] != null)
			{
				bolt[i].update();
				if (bolt[i].nullMe) bolt[i] = null;
			}
			else
			{
				Vector2 v = Vector2.Zero;
				if (shape == SHAPE.CIRCLE)
				{
					v = Trigonometrics.getOrbitLocationDeg(srcPoint.x, srcPoint.y, i*(360.0f/bolt.length), size);
				}
				else if (shape == SHAPE.LINE)
				{
					v = Trigonometrics.getOrbitLocationDeg(srcPoint.x, srcPoint.y, angle, i*(size/bolt.length));
				}
				bolt[i] = new Bolt(v.x,v.y, this);
			}
		}
	}
	
	class Bolt
	{
		AreaElectricity parentEffect;
		float dissolveFactor;
		int numOfSegments;
		float alphaPerSegment[];
		float anglePerSegment[];
		int thickness;
		Vector2 sourcePoint;
		Vector2 segmentPoints[];
		long delayUntil; // every bolt gets a delay before reappearing, more natural organic look, unit = ms
		
		int currentSegment;
		
		boolean nullMe;
		
		
		public Bolt(float _x, float _y, AreaElectricity parent)
		{
			sourcePoint = new Vector2(_x, _y);
			parentEffect = parent;
			
			currentSegment = 0;
			nullMe = false;
			delayUntil = System.currentTimeMillis() + Util.getRandom(10, 800);
			
			dissolveFactor = Util.getRandom(parentEffect.dissolveMin, parentEffect.dissolveMax);
			numOfSegments = Util.getRandom(parentEffect.segmentsAmountMin, parentEffect.segmentsAmountMax);
			segmentPoints = new Vector2[numOfSegments];
			alphaPerSegment = new float[numOfSegments];
			anglePerSegment = new float[numOfSegments];
			thickness = Util.getRandom(parentEffect.boltThicknessMin, parentEffect.boltThicknessMax);
			
			for (int i=0;i<numOfSegments;i++)
			{
				Vector2 prevPoint;
				if (i == 0) prevPoint = sourcePoint; else prevPoint = segmentPoints[i-1];
				alphaPerSegment[i]  = 0.0f;
				if (i == 0) anglePerSegment[0] = Util.getRandom(0, 360);
				else
				{
					anglePerSegment[i] = anglePerSegment[0] + Util.getRandom(parentEffect.segmentsAngleMin, parentEffect.segmentsAngleMax);
				}
				segmentPoints[i] = Trigonometrics.getOrbitLocationDeg(prevPoint.x, prevPoint.y, anglePerSegment[i], Util.getRandom(parentEffect.segmentsLengthMin, parentEffect.segmentsLengthMax)).cpy();
			}
		}
		
		
		public void update()
		{
			if (System.currentTimeMillis() < delayUntil) return;

			if (currentSegment >= numOfSegments-1)
			{
				//start dissapearing
				if (alphaPerSegment[0] > 0.0f)
				{
					for (int i=0;i<numOfSegments;i++)
					{
						alphaPerSegment[i]-=dissolveFactor;
						if (alphaPerSegment[i] <=0.0f) alphaPerSegment[i] = 0.0f;
					}
				}
				else
				{
					nullMe = true;
				}				
			}
			else
			{
				if(alphaPerSegment[currentSegment] >= 1.0f)
				{
					//addNextSegment();
					currentSegment++;
				}
				else
				{
					alphaPerSegment[currentSegment]+=1f; //standard appearing alpha factor
					if (alphaPerSegment[currentSegment] > 1.0f) alphaPerSegment[currentSegment] = 1.0f;
				}
			}
		}
		
		public void render(SpriteBatch spriteBatch)
		{
			if (System.currentTimeMillis() < delayUntil) return;

			spriteBatch.setColor(1f, 1f, 1f, 1f);
			
			for (int i=0;i<numOfSegments;i++)
			{
//				long before = System.nanoTime();
				
				spriteBatch.setColor(getRanColorFromBase(color.r), getRanColorFromBase(color.g), getRanColorFromBase(color.b), alphaPerSegment[i]);
				
				Vector2 prevPoint = (i == 0) ? sourcePoint : segmentPoints[i-1];
				GFX.drawLine(spriteBatch, prevPoint.x, prevPoint.y, segmentPoints[i].x, segmentPoints[i].y, this.thickness, GFX.texElec);
				
//				System.out.println("Render PER segments: "+((System.nanoTime()-before)/1000L)+"us");
			}
			
			spriteBatch.setColor(1f, 1f, 1f, 1f);
		}

		private float getRanColorFromBase(float colorValue, float randomAmount)
		{
			return Util.getRandom(colorValue-randomAmount, colorValue+randomAmount);
		}
		
		private float getRanColorFromBase(float colorValue)
		{
			return getRanColorFromBase(colorValue, 0.07843f);
		}
	}
	
}
