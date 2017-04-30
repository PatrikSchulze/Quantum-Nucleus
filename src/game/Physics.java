package game;

import java.util.ArrayList;

import physics.QSlope;
import whitealchemy.GameMap;
import whitealchemy.PhysicsEvents;
import whitealchemy.QBox;
import whitealchemy.QEntity;
import whitealchemy.QOpenPlatform;
import whitealchemy.QPlatform;

import com.badlogic.gdx.math.Rectangle;

public class Physics
{
	//Lets avoid 'new's in the gameloop for RAM/GC reasons
	static QSlope calcSlope = null;
	static QBox calcSprite = null;
	static QPlatform calcPlatform = null;
	static Rectangle calcRect = new Rectangle(0,0,0,0);
	
	//Data Structures / Arrays
	static ArrayList<QEntity> entities;
	static ArrayList<QBox> structure;
	static ArrayList<QPlatform> platforms;
	static ArrayList<QOpenPlatform> openPlatforms;
	static GameMap gameMap;
	
	static PhysicsEvents eventConsumer;
	
	public static void init(PhysicsEvents _consumer, GameMap gmap)
	{
		gameMap = gmap;
		
		structure = new ArrayList<QBox>();
		platforms = new ArrayList<QPlatform>();
		openPlatforms = new ArrayList<QOpenPlatform>();
		entities = new ArrayList<QEntity>();
		
		eventConsumer = _consumer;
		
		buildBoxes(gameMap.getStructLayer());
	}
	
	public static void buildBoxes(MapLayer layer)
	{
		for (int y=0;y<layer.height;y++)
		{
			for (int x=0;x<layer.width;x++)
			{
				if (!layer.isTileEmpty(x, y))
				{
					Physics.structure.add(new QBox(x*MapLayer.TILESIZE, y*MapLayer.TILESIZE, MapLayer.TILESIZE, MapLayer.TILESIZE));
				}
			}
		}
	}
	
	public static QSlope isSlopeBeneath(Rectangle rect)
	{
		for (int i=0;i<gameMap.slopes.size();i++)
		{
			calcRect.set(rect);
			calcRect.y+=0;
			if (gameMap.slopes.get(i).overlaps(calcRect))		return gameMap.slopes.get(i);

			calcRect.y+=6;
			if (gameMap.slopes.get(i).overlaps(calcRect))		return gameMap.slopes.get(i);
		}

		return null;
	}

	public static QBox isStructureBeneath(Rectangle rect)
	{
		calcRect.set(rect);
		calcRect.y+=1;

		for (int i=0;i<structure.size();i++)
		{
			if (calcRect.overlaps(structure.get(i)))
				return structure.get(i);
		}
		return null;
	}

	public static QPlatform isPlatformBeneath(Rectangle rect)
	{
		calcRect.set(rect);
		calcRect.y+=1;
		for (int i=0;i<platforms.size();i++)
			if (calcRect.overlaps(platforms.get(i)))  return platforms.get(i);
		return null;
	}

	public static QOpenPlatform isOnTopOfOpenPlatform(Rectangle rect)
	{
		calcRect.set(rect);
		for (int i=0;i<openPlatforms.size();i++)
		{
			if (calcRect.overlaps(openPlatforms.get(i)))  return null; //Check OpenPlatform for expl
		}

		calcRect.y+=1;
		for (int i=0;i<openPlatforms.size();i++)
			if (calcRect.overlaps(openPlatforms.get(i)))  return openPlatforms.get(i);

		return null;
	}

	public static Rectangle calcCollideStructure(QBox sprite, float _x, float _y)
	{
		if (isSlopeBeneath(sprite) != null)
			_y-=6;

		calcRect.set(sprite);
		calcRect.x+=_x;
		calcRect.y+=_y;

		for (int i=0;i<structure.size();i++)
		{
			calcSprite = structure.get(i);
			if (calcRect.overlaps(calcSprite))	return calcSprite;
		}
		return null;
	}
	
	private static void resetJump(QEntity entity)
	{
		entity.acceleration.y = 0.0f;
		entity.speed.y = 0.0f;
		entity.timeJumped = -1;
	}
	
	public static  void xLogic(QEntity entity)
	{
		calcSlope = isSlopeBeneath(entity);
		if (calcSlope != null)
		{
			if (calcSlope.type == QSlope.TYPE.LEFT_UP)
			{
				if (entity.speed.x > 0.0f) //going right
				{
					entity.y+=Math.abs(entity.speed.x); //push down
					resetJump(entity);
				}
				else if (entity.speed.x < 0.0f) //going left
				{
					if (calcSlope.overlapsSensorLeft(entity))
					{
						entity.y-=Math.abs(entity.speed.x); //push up
						resetJump(entity);
					}
				}
			}
			else if (calcSlope.type == QSlope.TYPE.RIGHT_UP)
			{
				if (entity.speed.x > 0.0f) //going right
				{
					if (calcSlope.overlapsSensorRight(entity))
					{
						entity.y-=Math.abs(entity.speed.x); // push up
						resetJump(entity);
					}
				}
				else if (entity.speed.x < 0.0f) //going left
				{
					entity.y+=Math.abs(entity.speed.x); //push down
					resetJump(entity);
				}
			}
		}

		//platforms
		calcPlatform = isOnTopOfOpenPlatform(entity);
		if (calcPlatform != null) //first check openplatforms
		{
			if (calcPlatform.speed.x != 0.0f && calcCollideStructure(entity, calcPlatform.speed.x, 0) == null)
				entity.x+=calcPlatform.speed.x;
		}
		else	//then normal platforms; though there is no special priority here, the order is arbitrary
		{
			calcPlatform = isPlatformBeneath(entity);
			if (calcPlatform != null && calcPlatform.speed.x != 0.0f && calcCollideStructure(entity, calcPlatform.speed.x, 0) == null)
				entity.x+=calcPlatform.speed.x;
		}

		//walking
		if (entity.speed.x != 0.0f)
		{
			Rectangle r = calcCollideStructure(entity, entity.speed.x, 0f);
			if (r == null) // lets check for platforms
			{
				calcRect.set(entity);
				calcRect.x+=entity.speed.x;
				for (int i=0;i<platforms.size();i++)
				{
					calcPlatform = platforms.get(i);
					if (calcRect.overlaps(calcPlatform))
					{
						if (calcPlatform.speed.y < 0.0f && Math.abs(entity.bottom()-calcPlatform.y) < 9) // 9 arbitrary number, this may fail if the platform is really fast
						{
							//we dont consider it a full collision to avoid a bug which freezes entity x motion in certain situations
						}
						else
						{
							r = calcPlatform;
							break;
						}
					}
				}
			}

			//we would collide with the wall, lets stop
			if (r != null)
			{
				if (entity.speed.x == QEntity.MAX_WALK_SPEED || entity.speed.x == -QEntity.MAX_WALK_SPEED)
				{
					if (entity.speed.x > 0.0f)
						eventConsumer.eventRunningIntoWallToRight(entity);
					else
						eventConsumer.eventRunningIntoWallToLeft(entity);
				}
				calcRect.set(entity);
				entity.speed.x = 0.0f;
			}
		}
	}
	
	public static  void dragEntityDownFromPlatformMovement(QEntity entity)
	{
		for (int i=0;i<platforms.size();i++)
		{
			calcPlatform = platforms.get(i);
			if (calcPlatform.speed.y > 0.0f)
			{
				float dp = calcPlatform.y - entity.bottom();
				if (dp >= -5 && dp <= 40) // weird value based. generalize
				{
					entity.y+=calcPlatform.speed.y;
				}
			}
		}

		for (int i=0;i<openPlatforms.size();i++)
		{
			calcPlatform = openPlatforms.get(i);
			if (calcPlatform.speed.y > 0.0f)
			{
				float dp = calcPlatform.y - entity.bottom();
				if (dp >= -5 && dp <= 40) // weird value based. generalize
				{
					entity.y+=calcPlatform.speed.y;
				}
			}
		}
	}
	
	public static  boolean isAnyGroundBeneath(QEntity entity)
	{
		return (isStructureBeneath(entity) != null || isOnTopOfOpenPlatform(entity) != null || isPlatformBeneath(entity) != null || isSlopeBeneath(entity) != null);
	}

	public static  void applyGravity(QEntity entity)
	{
		//not standing on any ground 
		if (!isAnyGroundBeneath(entity))
		{	
			if (entity.speed.y < QEntity.TERMINAL_VELOCITY)
				entity.acceleration.y+=0.015f;
			else
				entity.acceleration.y = 0.0f;
		}
	}

	public static  void holdAboveSlopes(QEntity entity)
	{
		calcSlope = isSlopeBeneath(entity); 
		if (calcSlope != null)
		{
			if (entity.speed.y < 0.0f || entity.speed.x != 0.0f) // going up
			{
				for (int i=0;i<gameMap.slopes.size();i++)
				{
					boolean jumpOut = gameMap.slopes.get(i).overlaps(entity);

					while (gameMap.slopes.get(i).overlaps(entity))
						entity.y--;

					if(jumpOut) break;
				}
			}
			else if (entity.speed.y >= 0.0f) // going down
			{
				resetJump(entity);
			}
		}
	}

	public static void holdAboveStructure(QEntity entity)
	{
		for (int i=0;i<structure.size();i++)
		{
			calcSprite = structure.get(i);
			if (entity.overlaps(calcSprite))
			{
				//ok so overlap
				if (entity.speed.y < 0.0f) // going up
				{
					entity.acceleration.y = 0.0f;
					entity.speed.y+= 0.5f;

					//push down
					entity.y = calcSprite.y + calcSprite.height;
					System.out.println("push up structure");
					for (int j=0;j<structure.size();j++)
					{
						while (structure.get(j).overlaps(entity))
						{
							entity.y--;
						}
					}
				}
				else //going down
				{
					resetJump(entity);

					//push up
					entity.y = calcSprite.y - entity.height;
				}
			}
		}
	}

	public static void freezeEntityYandPushItUp(QEntity entity, QPlatform platform)
	{
		resetJump(entity);

		entity.y = platform.y - entity.height;
	}

	public static void holdAbovePlatforms(QEntity entity)
	{
		for (int i=0;i<platforms.size();i++)
		{
			calcPlatform = platforms.get(i);

			if (entity.overlaps(calcPlatform))
			{
				if (entity.speed.y > 0.0f) //going down
				{
					if (entity.bottom() >= calcPlatform.y && entity.y < calcPlatform.y)
					{
						freezeEntityYandPushItUp(entity, calcPlatform);
					}
					else
					{
						while(entity.overlaps(calcPlatform))
						{
							entity.y++;
						}
					}
				}
				else
				{
					entity.acceleration.y = 0.0f;
					entity.speed.y+= 0.5f;

					//push down
					entity.y = calcPlatform.y + calcPlatform.height;
				}
			}
		}
	}

	public static void holdAboveOpenPlatforms(QEntity entity)
	{
		for (int i=0;i<openPlatforms.size();i++)
		{
			calcPlatform = openPlatforms.get(i);
			if (entity.overlaps(calcPlatform) && entity.speed.y >= 0.0f) // overlapping a platform while entity goes down or stands
			{
				float distance = Math.abs( entity.bottom()-calcPlatform.y );
				if (distance <= QEntity.TERMINAL_VELOCITY)
				{
					freezeEntityYandPushItUp(entity, calcPlatform);
				}
			}
		}
	}

	public static void dropDown(QEntity entity)
	{
		calcPlatform = isOnTopOfOpenPlatform(entity);
		if (calcPlatform != null)
		{	
			entity.y+=QEntity.TERMINAL_VELOCITY+25;
			entity.speed.y = Math.abs(calcPlatform.speed.y)+0.5f;

			eventConsumer.eventDroppingDown(entity);
		}
	}
	
	public static void computePhysicsForEntity(QEntity entity)
	{
		xLogic(entity);

		//drag entity when standing on platforms Y direction
		dragEntityDownFromPlatformMovement(entity);

		calcPlatform = isPlatformBeneath(entity); 
		if (calcPlatform != null && calcPlatform.speed.y < 0.0f)
			entity.y+=calcPlatform.speed.y;

		calcPlatform = isOnTopOfOpenPlatform(entity); 
		if (calcPlatform != null && calcPlatform.speed.y < 0.0f)
			entity.y+=calcPlatform.speed.y;

		boolean slopeBeneathBefore = (isSlopeBeneath(entity) != null);
		boolean onGroundBeforeCompute = isAnyGroundBeneath(entity);
		float entYBefore = entity.y;
		float speedYBeforeCompute = entity.speed.y;
		entity.compute();
		float entYAfter = entity.y;
		boolean onGroundAfterCompute = isAnyGroundBeneath(entity);
		float speedYAfterCompute = entity.speed.y;

		if (!onGroundBeforeCompute && onGroundAfterCompute && !slopeBeneathBefore) //EVENT FIRE
		{
			if (Math.abs(entYAfter-entYBefore) > 2.0f)
			{
				eventConsumer.eventLandingOnGround(entity);
				System.out.println("Landing Delta: "+(entYAfter-entYBefore)+"\tBefore: "+entYBefore+"\tAfter: "+entYAfter);
			}
			else
				System.out.println("Ignoring Landing Delta: "+(entYAfter-entYBefore)+"\tBefore: "+entYBefore+"\tAfter: "+entYAfter);
		}

		if (speedYBeforeCompute < 0.0f && speedYAfterCompute > 0.0f) //EVENT FIRE
			eventConsumer.eventApex(entity);

		//grind down X movement
		//this causes the entity to slowly lose momentum and not suddenly, to slide just a bit
		if (entity.speed.x < 0.0f) //going left
		{
			if (!entity.goLeft)
			{
				entity.speed.x+=0.25f;
				if (entity.speed.x >= 0.0f)
				{
					entity.speed.x = 0.0f;
					eventConsumer.eventStoppedRunning(entity);
				}
			}
		}
		else if (entity.speed.x > 0.0f) // going right
		{
			if (!entity.goRight)
			{
				entity.speed.x-=0.25f;
				if (entity.speed.x <= 0.0f)
				{
					entity.speed.x = 0.0f;
					eventConsumer.eventStoppedRunning(entity);
				}
			}
		}

		applyGravity(entity);

		holdAboveStructure(entity);
		holdAboveSlopes(entity);
		holdAbovePlatforms(entity);

		float speedYBeforeOpenPlatform = entity.speed.y;
		holdAboveOpenPlatforms(entity);
		if (speedYBeforeOpenPlatform > 1f && isOnTopOfOpenPlatform(entity) != null) //EVENT FIRE
			eventConsumer.eventLandingOnGround(entity);
	}
	
	public static void reactToInputsForEntity(QEntity entity)
	{
		if (entity.goLeft)
		{
			if (entity.speed.x == 0f) eventConsumer.eventStartRunningToLeft(entity);
			
			if (calcCollideStructure(entity, -QEntity.MAX_WALK_SPEED, 0f) != null)
			{
				if (Math.abs(entity.speed.x) < 0.3f) eventConsumer.eventPushingWallToLeft(entity); //EVENT FIRE
			}
			else
			{
				if (entity.speed.x > -QEntity.MAX_WALK_SPEED)
					entity.speed.x-=0.25f;
				else if (entity.speed.x < -QEntity.MAX_WALK_SPEED)
					entity.speed.x = -QEntity.MAX_WALK_SPEED;
			}
		}

		if (entity.goRight)
		{
			if (entity.speed.x == 0f) eventConsumer.eventStartRunningToRight(entity);
			
			if (calcCollideStructure(entity, QEntity.MAX_WALK_SPEED, 0f) != null)
			{
				if (Math.abs(entity.speed.x) < 0.3f) eventConsumer.eventPushingWallToRight(entity); //EVENT FIRE
			}
			else 
			{
				if (entity.speed.x < QEntity.MAX_WALK_SPEED)
					entity.speed.x+=0.25f;
				else if (entity.speed.x > QEntity.MAX_WALK_SPEED)
					entity.speed.x = QEntity.MAX_WALK_SPEED;
			}
		}
	}
	
}
