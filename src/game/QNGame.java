package game;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import gfx.GFX;
import gfx.GravityEffect;
import gfx.QNEffect;
import whitealchemy.Animation;
import whitealchemy.GameMap;
import whitealchemy.PhysicsEvents;
import whitealchemy.PlusCam;
import whitealchemy.QEntity;
import whitealchemy.QOpenPlatform;
import whitealchemy.QPlatform;
import whitealchemy.TileSheet;


/**
 *
 * @author Patrik Schulze, ENMA Games
 */
public class QNGame implements ApplicationListener, PhysicsEvents
{
	public PlusCam camera, hudCamera;
	public SpriteBatch spriteBatch;
	public SpriteCache spriteCache;
//	public Viewport viewport;
	public BitmapFont font;
	public ShapeRenderer shapeRender;
	public QEntity myNova;
	public Kryo kryo;
	public GameMap gameMap;
	FrameBuffer fbo;
	float fa = 0.001f;
	boolean experiment1 = false;
	boolean experiment2 = false;
	
	ArrayList<QNEffect> qnEffects;
	
	int aniC = 0;
	Animation aniRun;
	
	Texture texGrid;
	
	//Assets
	public TextureRegion   texPlatform;
	public TextureRegion   texOpenPlatform;
	public boolean initDone = false;
	public boolean loadScreenDone = false;
	public Exception runTimeException	= null;
	
	/*
	 * BUGS: October 13, 2013 
	 * - jumping up against a wall will trigger the Landing on Ground Event repeatedly
	 * -  TODO: slopes
	 * - landing on slopes is not smooth
	 */
	@Override
	public void create()
	{
		System.out.println(Display.getTitle()+"\n");
		Config.init();
		
//		Gdx.graphics.setDisplayMode(Config.width, Config.height, Config.fullscreen);
		
		kryo = new Kryo();
		kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
		kryo.register(SerializableMapLayerData.class);
		kryo.register(SerializableGameMapData.class);
		
		Content.minimalInit();
		font = Content.font;
		
		camera = new PlusCam(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.setToOrtho(true);
//		viewport = new FitViewport(1280,720, camera);
//		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		hudCamera = new PlusCam(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		hudCamera.setToOrtho(true);
		spriteBatch 	= new SpriteBatch();
		
	}

	public static void main(String[] args)
	{
		System.out.println("Java Version: "+System.getProperty("java.version"));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("content/icon/icon128.png", FileType.Internal);
		config.addIcon("content/icon/icon64.png",  FileType.Internal);
		config.addIcon("content/icon/icon32.png",  FileType.Internal);
		config.addIcon("content/icon/icon16.png",  FileType.Internal);
		config.samples = 0;
		config.vSyncEnabled = false;
		config.foregroundFPS = 0;
		config.backgroundFPS = 0;
		config.resizable = true;
		config.title = "QN";
		
		String reso = Config.getResolutionString();
		if (reso != null)
		{
			config.width  = Integer.parseInt(reso.split(",")[0]);
			config.height = Integer.parseInt(reso.split(",")[1]);
			config.fullscreen = Boolean.parseBoolean(reso.split(",")[2]);
			System.out.println("Config loaded.");
		}
		else
		{
			config.width  = 1280;
			config.height = 720;
			config.fullscreen = false;
			System.out.println("No config found.");
		}
		
		/*
		 * Borderless fullscreen
		 * System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
			cfg.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
			cfg.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
			cfg.fullscreen = false
		 */
		
		new LwjglApplication(new QNGame(), config);
		
	}
	
	private void loadMap(String mapName, int player_x, int player_y) // do include .map ending
	{
		System.out.print("\nLoading "+mapName+" ...   ");
		
		try{
			spriteCache.clear();
			gameMap = GameMap.loadCreateMap(kryo, mapName, Content.spriteAtlases, Content.tileSheets, spriteCache, camera);
			gameMap.cacheAll(spriteCache);
			qnEffects = new ArrayList<QNEffect>();
			Physics.init(this, gameMap);
			myNova = new QEntity(Content.samusAtlas.findRegion("idle"), player_x, player_y, gameMap.getWorld());
			System.out.println("Done");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void initGame()
	{
		initDone = true;
		
		Content.init();
		Content.assetLoader.finishLoading();
		Content.setupReferences();
		
		for (String str : Content.assetLoader.getAssetNames())
		{
			System.out.println("Asset: "+str);
		}
		
		//Percentage
		System.out.println("Content loading progress:\t"+Content.assetLoader.getProgress());
		
		//update one frame
		//Content.assetLoader.update();
		
		TextureRegion[] ta = new TextureRegion[10];
		for (int i = 0;i<10;i++)
		{
			ta[i] = Content.samusAtlas.findRegion("run_right"+i);
		}
		aniRun = new Animation(ta, 4 , Animation.LOOP);
		
		spriteCache = new SpriteCache(500000, false);
		
		ShaderProgram.pedantic = false;
		
		spriteCache.setShader(Shaders.defaultShader);

		if (experiment1 || experiment2) fbo = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
		shapeRender = new ShapeRenderer();
		shapeRender.setColor(Color.WHITE);
		
		System.out.println();
		System.out.println("Regions in Physics:");
		for (AtlasRegion reg : Content.physicsAtlas.getRegions())
		{
			System.out.println("\t-> "+reg.name);
		}
		
		texGrid = new Texture(Gdx.files.internal("content/grid.png"));

		texPlatform  	= Content.physicsAtlas.findRegion("phys-platform");
		texOpenPlatform = Content.physicsAtlas.findRegion("phys-openplatform");

		Gdx.input.setInputProcessor(new MaInputListener());

//		loadMap("dae_corridor");
//		loadMap("exit_map", 1000, 1000);
		loadMap("dae_corridor", 1000, 1000);
//		loadMap("dae_testsite", 1000, 1000);
		
		if (experiment1 || experiment2) gameMap.rayHandler.setLightMapRendering(true);
	}

	private void checkInputs()
	{
		if (Gdx.input.isKeyPressed(Keys.SPACE))
		{
			long timeInMs = System.currentTimeMillis() - myNova.timeJumped;
			if (timeInMs >= QEntity.MAX_TIME_JUMP) myNova.timeJumped = -1;
			else
			{
				if (myNova.speed.y == 0.0f)
					myNova.speed.y = -1.0f;
				if (myNova.speed.y < 0.0f && myNova.speed.y > -7.0f)
					myNova.speed.y+= -1.0f;
				else
					myNova.speed.y = -7f;

				if (myNova.speed.y < -7f) myNova.speed.y = -7f;
			}
		}

		myNova.goLeft  = Gdx.input.isKeyPressed(Keys.LEFT);
		myNova.goRight = Gdx.input.isKeyPressed(Keys.RIGHT);
	}

	private void adjustCamera()
	{
//
//		if (Math.abs(targetZoom-camera.zoom) > 0.0001f)
//			camera.zoom+=(targetZoom-camera.zoom)/20.0f;
//		else
//			camera.zoom = targetZoom;
		
		camera.zoom = 1.0f;
		
		float dx = myNova.x+(myNova.width/2f)  - camera.position.x;
		float dy = myNova.y+(myNova.height/2f) - camera.position.y;
		
		if (myNova.facingRight)
			dx+=(myNova.width/2f)+(Gdx.graphics.getWidth()*0.2f);
		else
			dx-=(myNova.width/2f)+(Gdx.graphics.getWidth()*0.2f);
		
		double h = Math.sqrt(dx * dx + dy * dy);
		float dn = (float)(h / Math.sqrt(2));

		Vector2 newPos = new Vector2((dx/dn)*(dn*0.05f), (dy/dn)*(dn*0.05f));

		camera.position.x += newPos.x;
		camera.position.y += newPos.y;
		
		keepMapCameraWithinBounds(camera);
		
		camera.position.x = (float)Math.round(camera.position.x);
		camera.position.y = (float)Math.round(camera.position.y);
	}
	
	private void keepMapCameraWithinBounds(OrthographicCamera mapCamera)
	{
		float minX = (Gdx.graphics.getWidth()*0.5f)*mapCamera.zoom;
		float minY = (Gdx.graphics.getHeight()*0.5f)*mapCamera.zoom;
		float maxX = gameMap.getBaseSizeWidthInPixels()  - ((Gdx.graphics.getWidth()*0.5f)*mapCamera.zoom);
		float maxY = gameMap.getBaseSizeHeightInPixels() - ((Gdx.graphics.getHeight()*0.5f)*mapCamera.zoom);
		
		if (mapCamera.position.x < minX) mapCamera.position.x = minX;
		if (mapCamera.position.y < minY) mapCamera.position.y = minY;
		if (mapCamera.position.x > maxX) mapCamera.position.x = maxX;
		if (mapCamera.position.y > maxY) mapCamera.position.y = maxY;

	}

	private void logic()
	{
		checkInputs();
		gameMap.world.step(0.01666f, 0, 0);
		
		for (int i = qnEffects.size() - 1; i >= 0; i--)
		{
			QNEffect e = qnEffects.get(i);
			e.update(this);
			if (e.isKillMe()) {qnEffects.remove(e); e = null;}
		}
		
		if (myNova.speed.x != 0f)
		{
			myNova.treg = aniRun.getFrame();
			aniRun.update();
		}

		for (int i=0;i<Physics.platforms.size();i++)		Physics.platforms.get(i).compute();
		for (int i=0;i<Physics.openPlatforms.size();i++)	Physics.openPlatforms.get(i).compute();

		Physics.reactToInputsForEntity(myNova);
		Physics.computePhysicsForEntity(myNova);

		for (int i=Physics.entities.size()-1; i >= 0; i--) // the reverse traverse, for easy removal
		{
			Physics.computePhysicsForEntity(Physics.entities.get(i));
		}

		adjustCamera();
		camera.update();
	}

	public void preFBO()
	{
		fbo.begin();
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	public void updateShaders()
	{
		fa+=0.005f;
		
		if (experiment1)
		{
			Shaders.swirl.begin();
				Shaders.swirl.setUniformf("amount", fa);
			Shaders.swirl.end();
		}
//		else if (experiment2)
//		{
//			Shaders.pinch.begin();
//				Shaders.pinch.setUniformf("time", fa*1f);
//			Shaders.pinch.end();
//		}
	}
	
	@Override
	public void render()
	{
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		try
		{
			if (runTimeException != null)
			{
				spriteBatch.setProjectionMatrix(camera.combined);
				spriteBatch.begin();
				
					float lih = font.getLineHeight();
		        	int offs = 240;
		        	font.setColor(Color.RED);
			    	GFX.drawStringCentered(spriteBatch, font, "RUNTIME EXCEPTION", 0, offs);
			    	font.setColor(Color.WHITE);
			    	GFX.drawStringCentered(spriteBatch, font, "Report  problem  to  ENMA Games at  someemail@someaddress.com", 0, offs+lih);
			    	GFX.drawStringCentered(spriteBatch, font, "A screenshot has been saved to THERE", 0, offs+lih+lih);
			    	GFX.drawStringCentered(spriteBatch, font, "Press   SPACE   to   send this problem to ENMA Games.", 0, offs+(lih*3));
			    	GFX.drawStringCentered(spriteBatch, font, "Press   ESC   to   terminate.", 0, offs+(lih*4));
			    	font.setColor(Color.RED);
			    	if (runTimeException.getMessage().indexOf("class incompatible") == -1)
			    		GFX.drawStringCentered(spriteBatch, font, ""+runTimeException.toString(), 0, offs+(lih*6));
			    	else
			    		GFX.drawStringCentered(spriteBatch, font, "Save  Game  Incompability.", 0, offs+(lih*6));
			    	GFX.drawStackTraceCentered(spriteBatch, font, runTimeException.getStackTrace(), 0, offs+(lih*6)+10);
			    	
			    spriteBatch.end();
			}
			else
			{
				if (initDone)
				{
					renderMainGame();
				}
				else
				{
					if (loadScreenDone) initGame();
					
					renderInitLoadingScreen();
					
					loadScreenDone = true;
				}
			}
		}
		catch(Exception e)
        {
        	if (runTimeException == null || runTimeException.getMessage().equalsIgnoreCase("null") ) runTimeException = e;
            e.printStackTrace();
        }
	}
	
	public void renderInitLoadingScreen()
	{
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
			font.setColor(Color.WHITE);
        	GFX.drawStringCentered(spriteBatch, font, "Loading", 0, Gdx.graphics.getHeight()/2);
		spriteBatch.end();
	}
	
	public void renderMainGame()
	{
		logic();
		
		
		spriteBatch.setShader(Shaders.defaultShader);
		
		
		if (experiment1 || experiment2)	preFBO();
		
		gameMap.renderAllLayers(spriteCache, camera);
		
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
			for (int i=0;i<Physics.platforms.size();i++)
				Physics.platforms.get(i).render(spriteBatch);
	
			for (int i=0;i<Physics.openPlatforms.size();i++)
				Physics.openPlatforms.get(i).render(spriteBatch);
	
			for (int i=0;i<Physics.entities.size();i++)
				Physics.entities.get(i).render(spriteBatch);
			
			for (QNEffect e : qnEffects)	e.drawBackPart(spriteBatch);
			
			myNova.render(spriteBatch);
			
			for (QNEffect e : qnEffects)	e.drawFrontPart(spriteBatch);
			
			if (experiment1  || experiment2)
			{
				gameMap.rayHandler.setCombinedMatrix(camera.combined, camera.position.x, camera.position.y, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
				gameMap.rayHandler.update();
				spriteBatch.draw(gameMap.rayHandler.getLightMapTexture(), 0, 0);
			}
			
		spriteBatch.end();
		
//		spriteBatch.setProjectionMatrix(hudCamera.combined);
//		spriteBatch.begin();
//			spriteBatch.draw(texGrid, 0, 0);
//		spriteBatch.end();
		
		if (experiment1 || experiment2)
		{
			fbo.end();
			updateShaders();
			
			if 		(experiment1) spriteBatch.setShader(Shaders.swirl);
			else if (experiment2) spriteBatch.setShader(Shaders.pinch);
			
			spriteBatch.setProjectionMatrix(hudCamera.combined);
			spriteBatch.begin();
				spriteBatch.draw(fbo.getColorBufferTexture(), 0, 0);
			spriteBatch.end();
			
			spriteBatch.setShader(Shaders.defaultShader);
		}
		
//		before = System.nanoTime();
		
		if (Config.drawLighting && !experiment1 && !experiment2) gameMap.renderLight(camera);
		
//		System.out.println("renderLight: "+((System.nanoTime()-before)/1000L)+"us");
		
//		before = System.nanoTime();
//		if (Config.drawLighting) gameMap.drawBox2D(camera);
//		System.out.println("drawBox2D: "+((System.nanoTime()-before)/1000L)+"us");
		
//		gameMap.drawBox2D(camera);

		spriteBatch.setProjectionMatrix(hudCamera.combined);
		spriteBatch.begin();
			drawDebugInfo();
		spriteBatch.end();

		
		drawDebugSlopes();
		
		Display.sync(60);
	}
	
	public void drawDebugSlopes()
	{
		shapeRender.setProjectionMatrix(camera.combined);
		shapeRender.begin(ShapeType.Line);
		shapeRender.setColor(Color.WHITE);
			for (int i=0;i<gameMap.slopes.size();i++)
				shapeRender.line(gameMap.slopes.get(i).getP1().x, gameMap.slopes.get(i).getP1().y, gameMap.slopes.get(i).getP2().x, gameMap.slopes.get(i).getP2().y);
		shapeRender.end();
	}
	
	public void drawDebugInfo()
	{
		font.setColor(Color.WHITE);
		font.draw(spriteBatch, "FPS: "+Gdx.graphics.getFramesPerSecond(), 12,  10);
		font.draw(spriteBatch, "Rel Mouse: "+getRelativeMouseX(camera)+" , "+getRelativeMouseY(camera), 12,  30);
		font.draw(spriteBatch, "Structure beneath: "+(Physics.isStructureBeneath(myNova) != null), 12,  50);
		font.draw(spriteBatch, "Any Ground beneath: "+Physics.isAnyGroundBeneath(myNova), 12,  70);
		Physics.calcSlope = Physics.isSlopeBeneath(myNova);
		if (Physics.calcSlope != null)
			font.draw(spriteBatch, "Slope: "+Physics.calcSlope.type, 230,  70);

		font.draw(spriteBatch, "Entites: "+Physics.entities.size(), 12,  90);
		font.draw(spriteBatch, "Platforms: "+(Physics.platforms.size()+Physics.openPlatforms.size()), 12,  110);
		font.draw(spriteBatch, "Structure: "+Physics.structure.size(), 12,  130);

		font.draw(spriteBatch, "Cam X: "+camera.position.x, Gdx.graphics.getWidth()-120,  Gdx.graphics.getHeight()-60);
		font.draw(spriteBatch, "Cam Y: "+camera.position.y, Gdx.graphics.getWidth()-120,  Gdx.graphics.getHeight()-40);
		font.draw(spriteBatch, "Cam Zoom: "+camera.zoom, Gdx.graphics.getWidth()-120,  Gdx.graphics.getHeight()-20);
		font.draw(spriteBatch, "jump: "+myNova.timeJumped, 20,  Gdx.graphics.getHeight()-80);
		font.draw(spriteBatch, "Speed: "+myNova.speed, 20,  Gdx.graphics.getHeight()-60);
		font.draw(spriteBatch, "Accel: "+myNova.acceleration, 20,  Gdx.graphics.getHeight()-40);
		font.draw(spriteBatch, "Position: "+myNova.getPosition(), 20,  Gdx.graphics.getHeight()-20);
	}

	// running against a wall at full speed, may used for appropriate animation
	public void eventRunningIntoWallToLeft(QEntity entity)
	{
		
	}
	public void eventRunningIntoWallToRight(QEntity entity)
	{
		
	}
	// dropping to ground event
	public void eventLandingOnGround(QEntity entity)
	{
		System.out.println("Event: Landing on Ground");
		myNova.treg = Content.samusAtlas.findRegion("idle");
	}

	// the apex of the jump has been reached
	public void eventApex(QEntity entity)
	{
		System.out.println("Event: Apex");
	}

	// starting to jump
	public void eventStartJumping(QEntity entity)
	{
		System.out.println("Event: Start Jumping");
		myNova.treg = Content.samusAtlas.findRegion("jump");
	}

	// dropping to ground event
	public void eventDroppingDown(QEntity entity)
	{
		System.out.println("Event: Dropping Down from Open Platform");
	}

	// pushing against a wall constantly, LEFT
	public void eventPushingWallToLeft(QEntity entity)
	{
		//NOTE: there might be an "off" animation still in progress, you may wanna wait for it to finish before switching
		System.out.println("Event: pushing against a wall, to the left");
	}

	// pushing against a wall constantly, RIGHT
	public void eventPushingWallToRight(QEntity entity)
	{
		//NOTE: there might be an "off" animation still in progress, you may wanna wait for it to finish before switching
		System.out.println("Event: pushing against a wall, to the right");
	}

	@Override
	public void eventStartRunningToLeft(QEntity entity)
	{
//			System.out.println("run Left");
	}

	@Override
	public void eventStartRunningToRight(QEntity entity)
	{
//			System.out.println("run Right");
	}
	
	@Override
	public void eventStoppedRunning(QEntity entity)
	{
		myNova.treg = Content.samusAtlas.findRegion("idle");
	}
	
	public void addGravityEffect(float _x, float _y)
	{
		qnEffects.add(new GravityEffect(_x, _y, 8, 1f));
	}
	
	@Override public void resize(int width, int height)
	{
//		Gdx.gl.glViewport(0, 0, width, height);
		
//		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		
		Vector3 prePos = camera.position.cpy();
		
		camera.setToOrtho(true, width, height);
    	hudCamera.setToOrtho(true, width, height);
    	
    	camera.position.set(prePos);
    	
    	float zoom = 1280.0f/width;
    	camera.zoom = zoom;
    	
    	if (gameMap != null)
    	{
    		for (MapLayer layer : gameMap.layers)
        	{
        		if (layer.hasParallaxCamera())
        		{
        			layer.initParallaxCamera(camera);
        		}
        	}
    	}
	}
	@Override public void pause()	{ } 
	@Override public void dispose()
	{
		spriteBatch.dispose();
		if (spriteCache!=null)spriteCache.dispose();
		
		if (gameMap != null)
		{
			gameMap.world.destroyBody(myNova.body);
			gameMap.dispose();
		}
		
		Config.save(this);
		Content.dispose();
	}
	@Override public void resume()	{	}

	public static float getRelativeMouseX(OrthographicCamera camera)
	{
		return (Gdx.input.getX()*camera.zoom)+(camera.position.x)-((camera.viewportWidth*camera.zoom)/2.0f);
	}

	public static float getRelativeMouseY(OrthographicCamera camera)
	{
		return (Gdx.input.getY()*camera.zoom)+(camera.position.y)-((camera.viewportHeight*camera.zoom)/2.0f);
	}

	class MaInputListener extends InputAdapter
	{
		public boolean touchDown(int x, int y, int pointer, int button)
		{
			if (button == Buttons.RIGHT)
			{
				myNova.setPosition(getRelativeMouseX(camera)-(myNova.width/2f), getRelativeMouseY(camera)-(myNova.height/2f));
			}
			
			if (button == Buttons.LEFT)
			{
				addGravityEffect(getRelativeMouseX(camera), getRelativeMouseY(camera));
			}
			
			if (button == Buttons.MIDDLE)
			{
				qnEffects.get(0).setPosition(getRelativeMouseX(camera), getRelativeMouseY(camera));
			}
			
			System.out.println("button: "+button);

			return true;
		}

		public boolean keyDown(int keycode)
		{
			if (keycode == Keys.SPACE)
			{
				if (Physics.isAnyGroundBeneath(myNova))
				{
					if (!Gdx.input.isKeyPressed(Keys.DOWN))
					{
						if (myNova.timeJumped == -1)
						{
							myNova.timeJumped = System.currentTimeMillis();
							eventStartJumping(myNova);
						}
					}
					else
						Physics.dropDown(myNova);
				}
			}

			if (keycode == Keys.ESCAPE)	Gdx.app.exit();
			
			if ((keycode == Keys.F1) && (Gdx.input.isKeyPressed(Keys.ALT_LEFT)))
			{
				Gdx.graphics.setDisplayMode(1280, 720, false);
			}
			
			if ((keycode == Keys.F2) && (Gdx.input.isKeyPressed(Keys.ALT_LEFT)))
			{
				Gdx.graphics.setDisplayMode(1600, 900, false);
			}
			
			if ((keycode == Keys.F3) && (Gdx.input.isKeyPressed(Keys.ALT_LEFT)))
			{
				Gdx.graphics.setDisplayMode(Display.getDesktopDisplayMode().getWidth(), Display.getDesktopDisplayMode().getHeight(), true);
			}
			
			
			
			if (keycode == Keys.R)
			{
				myNova.createDefaultRedFlash();
			}


			if (keycode == Keys.P)
			{
				QPlatform platform = new QPlatform(texPlatform, getRelativeMouseX(camera)-(texPlatform.getRegionWidth()/2f), getRelativeMouseY(camera)-(texPlatform.getRegionHeight()/2f), gameMap.getWorld());

				Physics.platforms.add(platform);
			}

			if (keycode == Keys.O)
			{
				QOpenPlatform oPlatform = new QOpenPlatform(texOpenPlatform, getRelativeMouseX(camera)-(texOpenPlatform.getRegionWidth()/2f), getRelativeMouseY(camera)-(texOpenPlatform.getRegionHeight()/2f), gameMap.getWorld());
				Physics.openPlatforms.add(oPlatform);
			}


			if (keycode == Keys.I)
			{
				QPlatform platform = new QPlatform(texPlatform, getRelativeMouseX(camera)-(texPlatform.getRegionWidth()/2f), getRelativeMouseY(camera)-(texPlatform.getRegionHeight()/2f), gameMap.getWorld());
				platform.setMovement(QPlatform.MOVEMENT_DIRECTION.VERTICAL, 500);

				Physics.platforms.add(platform);
			}

			if (keycode == Keys.U)
			{
				QPlatform platform = new QPlatform(texPlatform, getRelativeMouseX(camera)-(texPlatform.getRegionWidth()/2f), getRelativeMouseY(camera)-(texPlatform.getRegionHeight()/2f), gameMap.getWorld());
				platform.setMovement(QPlatform.MOVEMENT_DIRECTION.HORIZONTAL, 500);

				Physics.platforms.add(platform);
			}



			if (keycode == Keys.T)
			{
				QOpenPlatform oPlatform = new QOpenPlatform(texOpenPlatform, getRelativeMouseX(camera)-(texOpenPlatform.getRegionWidth()/2f), getRelativeMouseY(camera)-(texOpenPlatform.getRegionHeight()/2f), gameMap.getWorld());
				oPlatform.setMovement(QPlatform.MOVEMENT_DIRECTION.VERTICAL, 500);

				Physics.openPlatforms.add(oPlatform);
			}

			if (keycode == Keys.Z)
			{
				QOpenPlatform oPlatform = new QOpenPlatform(texOpenPlatform, getRelativeMouseX(camera)-(texOpenPlatform.getRegionWidth()/2f), getRelativeMouseY(camera)-(texOpenPlatform.getRegionHeight()/2f), gameMap.getWorld());
				oPlatform.setMovement(QPlatform.MOVEMENT_DIRECTION.HORIZONTAL, 500);

				Physics.openPlatforms.add(oPlatform);
			}



			
			if (keycode == Keys.NUM_9)
			{
				camera.startQuake(3000, 0.2f);
			}

			
			


			return true;
		}

		public boolean keyUp(int keycode)
		{
			if (keycode == Keys.SPACE)		myNova.timeJumped = -1;
			if (keycode == Keys.ESCAPE)		Gdx.app.exit();

			return true;
		}
	}

	
}