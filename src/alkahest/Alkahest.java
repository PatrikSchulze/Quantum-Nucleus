package alkahest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.input.Mouse;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import game.Colors;
import game.Config;
import game.MapLayer;
import game.MapScript;
import game.SerializableGameMapData;
import game.SerializableMapLayerData;
import gfx.GFX;
import physics.QSlope;
import whitealchemy.FlippedTextureRegion;
import whitealchemy.GameMap;
import whitealchemy.TileSheet;
import whitealchemy.Util;
 
public class Alkahest implements ApplicationListener
{
	enum MODE { TILES, SPRITES, EFFECTS, LIGHTS, PLATFORM, NPCS, OBJECTS, SCRIPTS, SLOPES, AI } //animated tiles sprites maybe later
	MODE mode = MODE.TILES;
	
//	enum ACTION { PLACING, EDITING, DELETING }
//	ACTION action = ACTION.PLACING;
	
	enum INPUT_INTENTION { LAYER_ADD, LAYER_NAME, LAYER_META, MAP_SIZE, MAP_DNAME }
	
	private int topMenuHeight = 70;
	private float popupAlpha = 0f;
	private float targetZoom = 1f;
	private boolean drawObjects = true;
//	private boolean openPlatformType;
	private boolean drawHelp, drawBox2D; // editing for sprites only
	private String popupStr = "";
    private String placeSpriteName = null;
	private OrthographicCamera mapCamera, hud_camera, uiCam;
	private SpriteBatch spriteBatch;
	private SpriteCache spriteCache;
	private ShapeRenderer shapeRenderer;
	private Texture texBg, texAlkahest, texWhite;
	private FlippedTextureRegion texHelp;
	private Sprite markSprite = null;
	private Sprite placeSprite = null;
	private Sprite tempSprite = null;
    private HashMap<String, TileSheet> tileSheets;
    private HashMap<String, TextureAtlas> spriteAtlases;
    private BitmapFont font, bigFont;
    private GameMap gameMap;
    private Kryo kryo = new Kryo();
	private MapLayer currentLayer;
//	private QPlatform.MOVEMENT_DIRECTION platformMovement = QPlatform.MOVEMENT_DIRECTION.NONE;
	private Rectangle curScriptRect = null;
	private MapScript markScript = null;
    private Light markLight = null;
	private OrthographicCamera tempCam;
	private OrthographicCamera tempShapeCam;
	private Vector2 slopeVec = null;
	private QSlope markSlope = null;
	private GlyphLayout glyphL = new GlyphLayout();
	
	//UI
	private Skin skin;
//	private MuhStage muhStage;
	private Stage muhStage;
	private Drawable uiblack;
	private TextButton[] ui_menuLeftButtons  = new TextButton[2];
	private TextButton[] ui_menuRightButtons = new TextButton[10];
	private TextButton[] ui_editMenuButtons  = new TextButton[19];
	private SelectBox<String> ui_mapList;
	private SelectBox<String> ui_tilesheetList;
	private List<String> ui_spriteAtlasList;
	private List<String> ui_spritesList;
	private List<String> ui_layerlist;
	private Slider ui_sliderSpriteRed, ui_sliderSpriteGreen, ui_sliderSpriteBlue, ui_sliderSpriteAlpha;
	private Slider ui_sliderLightRed, ui_sliderLightGreen, ui_sliderLightBlue, ui_sliderLightAlpha;
	private Slider ui_sliderAmbientRed, ui_sliderAmbientGreen, ui_sliderAmbientBlue, ui_sliderAmbientAlpha;
	private TextButton ui_lightStatic, ui_lightXRay, ui_lightSoft;
	private Table ui_editMenuTable;
	private Table ui_layerListTable, ui_tilesTable, ui_spritesTable, ui_effectsTable, ui_scriptsTable, ui_aiTable, ui_slopesTable, ui_objectsTable, ui_npcsTable, ui_platformsTable, ui_lightsTable;
	private TextField ui_scriptTextPrefix;
	private SelectBox<String> ui_scriptTriggerBox;
	private TextButton ui_layerOrderUp, ui_layerOrderDown;
	private TextButton ui_parallaxProxy;
	private Table ui_checkTable;
	private Table ui_delTable;
	private CheckBox[] ui_layerVisChkBoxes;
	private TextButton ui_globalLightGamma, ui_globalLightDiffuse, ui_globalLightCulling, ui_globalLightBlurPlus, ui_globalLightBlurMinus, ui_globalLightShadows, ui_globalLightBlur;
	private Label ui_globalLightBlurNum;
	
	int blurNum = 3;
	//
	private Window ui_dialogWindow;
	private TextButton ui_dialogOk;
	private TextButton ui_dialogCancel;
	private Label ui_dialogLabel;
	private TextField ui_dialogField;
	private INPUT_INTENTION dialogI = null;
	
    /*
     * TODO
     * 
     * add layers
		remove layers
		layer size & parallax
     * 
     * Choose sprites 
     *  coloring ? NO for now
     *  
     *  save to map 
     *  
     *  parallax
     *  platforms
     *  enemies, later
     *  items, later
     */
    
    public static void main(String args[])
	{
    	System.out.println("Java Version: "+System.getProperty("java.version"));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 0;
		config.vSyncEnabled = true;	
		config.useGL30 = false;
		config.resizable = true;
//		config.width  = 1024;
//		config.height = 576;
		
//		config.width  = (int)(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()*0.97f);
//		config.height = (int)(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()*0.85f);
		
		config.width  = 1600;
		config.height = 900;
		
		config.fullscreen = false;
		config.addIcon("content/icon/icon128.png", FileType.Internal);
		config.addIcon("content/icon/icon64.png",  FileType.Internal);
		config.addIcon("content/icon/icon32.png",  FileType.Internal);
		config.addIcon("content/icon/icon16.png",  FileType.Internal);
		config.title = "Alkahest";
		
		//System.setProperty("org.lwjgl.input.Mouse.allowNegativeMouseCoords", "true");
		
//		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		
//		TexturePacker2.Settings settings = new TexturePacker2.Settings();
//        settings.maxWidth = 1024;
//        settings.maxHeight = 1024;
//        TexturePacker2.process(settings, "content/sprites_to_pack", "content/spritesets", "blah1");
        
		new LwjglApplication(new Alkahest(), config);
	}
    
	@Override
	public void create()
	{
		kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
		kryo.register(SerializableMapLayerData.class);
		kryo.register(SerializableGameMapData.class);
		
		Config.init();
//		drawLighting			= Config.getBoolean("alkahest_showLight");
//		drawSlopes				= Config.getBoolean("alkahest_drawSlopes");
		drawHelp				= Config.getBoolean("alkahest_drawHelp");
		drawBox2D				= Config.getBoolean("alkahest_drawBox2D");
		int defaultLayer		= Config.getInteger("alkahest_currentLayer");
		
		System.out.println("Alkahest\n");
		
		muhStage = new Stage();
		spriteBatch = new SpriteBatch();
		
		InputMultiplexer multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(muhStage);
		multiplexer.addProcessor(new MuhInputs());
		Gdx.input.setInputProcessor(multiplexer);
		
		hud_camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		hud_camera.setToOrtho(true);
		mapCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mapCamera.setToOrtho(true);
		uiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		uiCam.setToOrtho(false);
		
		font		= new BitmapFont(Gdx.files.internal("font/berlin23.fnt"), 	true);
		bigFont		= new BitmapFont(Gdx.files.internal("font/deja.fnt"), 		true);
		
		spriteAtlases = new HashMap<String, TextureAtlas>();
		FileHandle atlasFH = Gdx.files.internal("content/spritesheets");
		System.out.println("Loading Atlases");
		for (FileHandle cfh : atlasFH.list())
		{
			if (cfh.extension().equals("atlas"))
			{
				spriteAtlases.put(cfh.nameWithoutExtension(), new TextureAtlas(cfh, true));
				System.out.println("   "+cfh.nameWithoutExtension());
			}
		}
		System.out.println();
		
		//Load tileset textures
		HashMap<String, Texture> tilesetTextures;
		tilesetTextures = new HashMap<String, Texture>();
		String tilesetPath = "content/tilesheets";
		FileHandle fh = Gdx.files.internal(tilesetPath);
		System.out.println("loading Tileset textures");
		
		tileSheets = new HashMap<String, TileSheet>();
		
		for (FileHandle cfh : fh.list())
		{
			tilesetTextures.put(cfh.nameWithoutExtension(), new Texture(cfh, true));
			System.out.println("   "+cfh.nameWithoutExtension());
			tileSheets.put(cfh.nameWithoutExtension(), new TileSheet(tilesetTextures.get(cfh.nameWithoutExtension()), cfh.nameWithoutExtension()));
		}
		System.out.println();
				
		texBg			= new Texture(Gdx.files.internal("content/bg.png"));
		texAlkahest		= new Texture(Gdx.files.internal("content/alkahest.png"));
		texWhite		= new Texture(Gdx.files.internal("content/white.png"));
		texHelp			= new FlippedTextureRegion(Gdx.files.internal("content/keyboard.png"));
		
		shapeRenderer = new ShapeRenderer();
		spriteCache = new SpriteCache(500000, false);
		
		TileSelector.init(texWhite, spriteBatch, tileSheets.get("onebig"), spriteCache);
		TileSelector.cacheTileSheetGraphics();
		
//		gameMap = loadinOldGameMap("dae_corridor");
//		gameMap = GameMap.loadCreateMap(kryo, "exit_map", spriteAtlases, tileSheets, spriteCache, mapCamera);
		gameMap = GameMap.loadCreateMap(kryo, "autosave5", spriteAtlases, tileSheets, spriteCache, mapCamera);
		System.out.println("Slopes: "+gameMap.slopes.size());
		
		gameMap.cacheAll(spriteCache);
		currentLayer = gameMap.getLayer(defaultLayer);
		
		setCurrentSprite(spriteAtlases.get(gameMap.spriteAtlasFileName).getRegions().get(0).name);
		placeSprite.setPosition(getRelativeMouseX(mapCamera)-placeSprite.getWidth()*0.5f, getRelativeMouseY(mapCamera)-placeSprite.getHeight()*0.5f);
		
		new Thread(new Runnable()
		{
			@Override public void run()
			{
				int autosaveNum = 1;
				while(true)
				{
					Util.sleep(30000);
					//TODO removed for debug
//					gameMap.saveToFile(kryo, "autosave"+autosaveNum);
					autosaveNum++;
					if (autosaveNum > 7) autosaveNum = 1;
				}
			}
		}).start();
		
//		gameMap.addLight(new DirectionalLight(gameMap.rayHandler, 200, new Color(1f, 1f, 1f, 0.57f), 90));
		
		uim_Init();
		switchMode(MODE.TILES);
		ui_tilesheetList.setSelected((currentLayer).tileSheet.name); // setting tilsheet UI
		ui_layerlist.setSelected(currentLayer.infoName);
		ui_syncLightElements();
	}
	
	private void ui_syncLightElements()
	{
		if (markLight != null)
		{
			ui_lightStatic.setChecked(markLight.isStaticLight());
			ui_lightXRay.setChecked(markLight.isXray());
			ui_lightSoft.setChecked(markLight.isSoft());
			ui_sliderLightRed.setValue(markLight.getColor().r*255f);
			ui_sliderLightGreen.setValue(markLight.getColor().g*255f);
			ui_sliderLightBlue.setValue(markLight.getColor().b*255f);
			ui_sliderLightAlpha.setValue(markLight.getColor().a*255f);
		}
		
		ui_globalLightGamma.setChecked(RayHandler.getGammaCorrection());
		ui_globalLightDiffuse.setChecked(RayHandler.isDiffuse);
//		ui_globalLightCulling.setChecked(gameMap.rayHandler.setCulling(culling);.);
		ui_globalLightBlurNum.setText("Blur Num:    "+3);
	}
	
	private void ui_syncSpriteSliders()
	{
		if (markSprite == null) return;
		ui_sliderSpriteRed.setValue(markSprite.getColor().r*255f);
		ui_sliderSpriteGreen.setValue(markSprite.getColor().g*255f);
		ui_sliderSpriteBlue.setValue(markSprite.getColor().b*255f);
		ui_sliderSpriteAlpha.setValue(markSprite.getColor().a*255f);
	}
	
	private void setCurrentSprite(String in)
	{
		placeSpriteName = in;
		placeSprite = spriteAtlases.get(gameMap.spriteAtlasFileName).createSprite(placeSpriteName);
		if (ui_sliderSpriteRed != null)		ui_syncSpriteSliders();
	}
	
	private void setAtlas(String in)
	{
		gameMap.setSpriteAtlas(spriteAtlases.get(in), in);
		
		ui_spritesList.clear();
		Array<String> list = new Array<String>();
		list = new Array<String>();
		for  (AtlasRegion a : spriteAtlases.get(gameMap.spriteAtlasFileName).getRegions())
		{
			list.add(a.name);
		}
		ui_spritesList.setItems(list);
		
		setCurrentSprite(spriteAtlases.get(gameMap.spriteAtlasFileName).getRegions().get(0).name);
	}
	
	private void uim_Init()
	{
		muhStage.getViewport().setCamera(uiCam);
		
		skin = new Skin(Gdx.files.internal("content/uiskin.json"));
		
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.BLACK);
		pixmap.fill();
		uiblack = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
		
		skin.get(ListStyle.class).selection.setBottomHeight(3);
		skin.get(ListStyle.class).selection.setTopHeight(3);
		skin.get(ListStyle.class).selection.setLeftWidth(8);
		skin.get(ListStyle.class).selection.setRightWidth(8);
		skin.get(ListStyle.class).background = uiblack;
		
		uim_setupTopMenu();
		uim_setupLayerList();
		
		uim_setupSlopesSubMenu();
		uim_setupTilesSubMenu();
		uim_setupSpritesSubMenu();
		uim_setupEffectsSubMenu();
		uim_setupScriptSubMenu();
		uim_setupAiSubMenu();
		uim_setupSlopesSubMenu();
		uim_setupObjectSubMenu();
		uim_setupNpcsSubMenu();
		uim_setupPlatformsSubMenu();
		uim_setupLightsSubMenu();
		
		uim_setupEditMenu();
		uim_setupDialog();
	}
	
	private void uim_setupDialog()
	{
		ui_dialogWindow =new Window("YO Dialog", skin);

		ui_dialogLabel = new Label("message text", skin);
		ui_dialogWindow.add(ui_dialogLabel).center().top().padBottom(25).padTop(25);
		ui_dialogWindow.row().colspan(2).padBottom(25);
		ui_dialogField = new TextField("", skin);
		ui_dialogWindow.add(ui_dialogField).width(260).center().padBottom(25);
		ui_dialogWindow.row();
		ui_dialogOk = new TextButton("OK", skin);
		ui_dialogOk.addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) {
			ui_dialogWindow.setVisible(false);
			ui_dialogWindow.setModal(false);
			dialogUsed(ui_dialogField.getText(), dialogI);
		}});
		ui_dialogWindow.add(ui_dialogOk).width(110).center().bottom().left();
		ui_dialogCancel = new TextButton("Cancel", skin);
		ui_dialogCancel.addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) {
			ui_dialogWindow.setVisible(false);
			ui_dialogWindow.setModal(false);
		}});
		ui_dialogWindow.add(ui_dialogCancel).width(110).center().bottom().right();
		ui_dialogWindow.pad(20);
		ui_dialogWindow.pack();
		
		uim_setButtonToNotCheck(ui_dialogOk);
		uim_setButtonToNotCheck(ui_dialogCancel);
		
		ui_dialogWindow.setVisible(true);
		ui_dialogWindow.moveBy((Gdx.graphics.getWidth()*0.5f)-(ui_dialogWindow.getWidth()*0.5f), 
								(Gdx.graphics.getHeight()*0.5f)-(ui_dialogWindow.getHeight()*0.5f));
		ui_dialogWindow.setVisible(false);
		muhStage.addActor(ui_dialogWindow);
	}
	
	public void switchMode(MODE inmode)
	{
		for (TextButton b : ui_menuRightButtons) b.setChecked(false);
		
		mode = inmode;
		uim_hideAllSubMenus();
		int checkID = -1;
		if (inmode == MODE.TILES || inmode == MODE.SPRITES || inmode == MODE.EFFECTS || inmode == MODE.LIGHTS)
		{
			ui_layerListTable.setVisible(true);
			if (inmode == MODE.TILES)			{ui_tilesTable.setVisible(true);	checkID = 0;}
			else if (inmode == MODE.SPRITES)	{ ui_spritesTable.setVisible(true); checkID = 1; }
			else if (inmode == MODE.EFFECTS)	{ui_effectsTable.setVisible(true); checkID = 2;}
			else if (inmode == MODE.LIGHTS)		{ui_lightsTable.setVisible(true); checkID = 7;}
		}
		else if (inmode == MODE.PLATFORM)		{ui_platformsTable.setVisible(true); checkID = 3;}
		else if (inmode == MODE.NPCS)			{ui_npcsTable.setVisible(true); checkID = 4;}
		else if (inmode == MODE.OBJECTS)		{ui_objectsTable.setVisible(true); checkID = 5;}
		else if (inmode == MODE.SCRIPTS)		{ui_scriptsTable.setVisible(true); checkID = 6;}
		else if (inmode == MODE.SLOPES)			{ui_slopesTable.setVisible(true); checkID = 8;}
		else if (inmode == MODE.AI)				{ui_aiTable.setVisible(true); checkID = 9;}
		
		if (inmode != MODE.TILES)	{ TileSelector.visible = false; }
		
		if (checkID > -1)	ui_menuRightButtons[checkID].setChecked(true);
	}
	
	private void uim_hideAllSubMenus()
	{
		ui_tilesTable.setVisible(false);
		ui_spritesTable.setVisible(false);
		ui_effectsTable.setVisible(false);
		ui_aiTable.setVisible(false);
		ui_slopesTable.setVisible(false);
		ui_objectsTable.setVisible(false);
		ui_npcsTable.setVisible(false);
		ui_platformsTable.setVisible(false);
		ui_lightsTable.setVisible(false);
		ui_layerListTable.setVisible(false);
		ui_scriptsTable.setVisible(false);
	}
	
	public void uim_setupTopMenu()
	{
		Table toplefttable = new Table();
		toplefttable.align(Align.topLeft);
		toplefttable.setFillParent(true);
		
		Table toprighttable = new Table();
		toprighttable.align(Align.topRight);
		toprighttable.setFillParent(true);
		
		ui_menuLeftButtons[0] = new TextButton("Save", skin);
		ui_menuLeftButtons[1] = new TextButton("Menu", skin);
		ui_menuLeftButtons[1].padRight(15);
		ui_menuLeftButtons[1].padLeft(3);
		
		ui_menuLeftButtons[0].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				saveMap();
				ui_menuLeftButtons[0].setChecked(false);
			}
		});
		
		ui_menuLeftButtons[1].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				ui_editMenuTable.setVisible(!ui_editMenuTable.isVisible());
				ui_menuLeftButtons[1].setChecked(false);
			}
		});
		
		ui_mapList = new SelectBox<String>(skin);
		Array<String> mapNames = new Array<String>();
		File folder = new File("maps/");
		for (File f : folder.listFiles())
		{
			if (f.getName().endsWith(".qmap"))
			{
				mapNames.add(f.getName());
			}
		}
		ui_mapList.setItems(mapNames);
		ui_mapList.setSelected("exit_map.qmap");
		ui_mapList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				new Dialog("Some Dialog", skin, "dialog") {
					protected void result (Object object) {
						if (Boolean.parseBoolean(""+object) == true) {
							loadMap(ui_mapList.getSelected());
						}
					}
				}.text("Load "+ui_mapList.getSelected()+" ?").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(muhStage);
			}
		});
		
		// RIGHT SIDE
		ui_menuRightButtons[0] = new TextButton("Tiles", skin);
		ui_menuRightButtons[0].getStyle().checked = ui_menuRightButtons[0].getStyle().down;
		ui_menuRightButtons[1] = new TextButton("Sprites", skin);
		ui_menuRightButtons[2] = new TextButton("Effects", skin);
		ui_menuRightButtons[3] = new TextButton("Platforms", skin);
		ui_menuRightButtons[4] = new TextButton("NPCs", skin);
		ui_menuRightButtons[5] = new TextButton("Objects", skin);
		ui_menuRightButtons[6] = new TextButton("Scripts", skin);
		ui_menuRightButtons[7] = new TextButton("Lights", skin);
		ui_menuRightButtons[8] = new TextButton("Slopes", skin);
		ui_menuRightButtons[9] = new TextButton("AI", skin);
		ui_menuRightButtons[0].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.TILES); } });
		ui_menuRightButtons[1].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.SPRITES); } });
		ui_menuRightButtons[2].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.EFFECTS); } });
		ui_menuRightButtons[3].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.PLATFORM); } });
		ui_menuRightButtons[4].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.NPCS); } });
		ui_menuRightButtons[5].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.OBJECTS); } });
		ui_menuRightButtons[6].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.SCRIPTS); } });
		ui_menuRightButtons[7].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.LIGHTS); } });
		ui_menuRightButtons[8].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.SLOPES); } });
		ui_menuRightButtons[9].addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) { switchMode(MODE.AI); } });
		
		for (TextButton b : ui_menuLeftButtons)
		{
			b.pad(7);
			toplefttable.add(b).width(90).padLeft(1).padRight(1);
		}
		toplefttable.add(ui_mapList).minWidth(300).padLeft(18);
		
		for (TextButton b : ui_menuRightButtons)
		{
			b.pad(7);
			toprighttable.add(b).width(90).padLeft(1).padRight(1);
		}
		
		muhStage.addActor(toplefttable);
		muhStage.addActor(toprighttable);
	}
	
	public void uim_setupAiSubMenu()
	{
		ui_aiTable = new Table(skin);
		ui_aiTable.align(Align.top);
		ui_aiTable.setFillParent(true);
//		ui_aiTable.setHeight(300);
		ui_aiTable.padTop(100);
		TextButton proxy = new TextButton("AI SUB MENU", skin);
		ui_aiTable.add(proxy).top();
		
		muhStage.addActor(ui_aiTable);
		
		ui_aiTable.setVisible(false);
	}
	
	public void uim_setupSlopesSubMenu()
	{
		ui_slopesTable = new Table(skin);
		ui_slopesTable.align(Align.top);
		ui_slopesTable.setFillParent(true);
//		ui_slopesTable.setHeight(300);
		ui_slopesTable.padTop(100);
		TextButton proxy = new TextButton("SLOPES SUB MENU", skin);
		ui_slopesTable.add(proxy).top();
		
		muhStage.addActor(ui_slopesTable);
		
		ui_slopesTable.setVisible(false);
	}
	
	public void uim_setupTilesSubMenu()
	{
		ui_tilesTable = new Table(skin);
		ui_tilesTable.align(Align.topLeft);
		ui_tilesTable.setFillParent(true);
		ui_tilesTable.padLeft(10);
		ui_tilesTable.padTop(44);
		
		Label tilesetLabel = new Label("Tilesets   ", skin);
		ui_tilesTable.add(tilesetLabel).left().top().row();
		
		Array<String> tilesets = new Array<String>();
		Iterator<String> iterator = tileSheets.keySet().iterator();
		while (iterator.hasNext())
		{
			tilesets.add(iterator.next());
		}
		
		ui_tilesheetList = new SelectBox<String>(skin);
		ui_tilesheetList.setItems(tilesets);
		
		ui_tilesheetList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				switchTilesheet(ui_tilesheetList.getSelected());
			}
		});
		
		ui_tilesTable.add(ui_tilesheetList).minWidth(320);
		muhStage.addActor(ui_tilesTable);
		ui_tilesTable.setVisible(false);
	}
	
	public void uim_setupSpritesSubMenu()
	{
		ui_spritesTable = new Table(skin);
		ui_spritesTable.align(Align.topLeft);
		ui_spritesTable.setFillParent(true);
//		ui_spritesTable.setHeight(300);
		ui_spritesTable.padTop(60);
		ui_spritesTable.padLeft(20);
		
		//##################### SLIDERS  ####################################################
		ui_sliderSpriteRed   = new Slider(1, 255, 1, false, skin);
		ui_sliderSpriteGreen = new Slider(1, 255, 1, false, skin);
		ui_sliderSpriteBlue  = new Slider(1, 255, 1, false, skin);
		ui_sliderSpriteAlpha  = new Slider(1, 255, 1, false, skin);
		
		ChangeListener chg = new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				if (markSprite != null) markSprite.setColor(ui_sliderSpriteRed.getValue()/255.0f, ui_sliderSpriteGreen.getValue()/255.0f, ui_sliderSpriteBlue.getValue()/255.0f, ui_sliderSpriteAlpha.getValue()/255.0f);
			}
		};
		ui_sliderSpriteRed.addListener(chg);
		ui_sliderSpriteGreen.addListener(chg);
		ui_sliderSpriteBlue.addListener(chg);
		ui_sliderSpriteAlpha.addListener(chg);
		
		ui_syncSpriteSliders();
		
		TextButton bxflip = new TextButton("X Flip", skin);
		uim_setButtonToNotCheck(bxflip);
		bxflip.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (markSprite != null) markSprite.flip(true, false);
			}
		});
		TextButton byflip = new TextButton("Y Flip", skin);
		uim_setButtonToNotCheck(byflip);
		byflip.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (markSprite != null) markSprite.flip(false, true);
			}
		});
		
		Table sliderTable = new Table();
		sliderTable.setFillParent(false);
		sliderTable.align(Align.right);
		
		sliderTable.add(new Label("Red:  ", skin));
		sliderTable.add(ui_sliderSpriteRed).padLeft(20).width(200).row();
		sliderTable.add(new Label("Green:  ", skin));
		sliderTable.add(ui_sliderSpriteGreen).padLeft(20).width(200).row();
		sliderTable.add(new Label("Blue:  ", skin));
		sliderTable.add(ui_sliderSpriteBlue).padLeft(20).width(200).row();
		sliderTable.add(new Label("Alpha:  ", skin));
		sliderTable.add(ui_sliderSpriteAlpha).padLeft(20).width(200).row();
		sliderTable.add(bxflip).width(200).colspan(2).row();
		sliderTable.add(byflip).width(200).colspan(2).row();
		
		//##################################################################################
		
//		placeSpriteName = spriteAtlases.get(gameMap.spriteAtlasFileName).getRegions().get(0).name;
		
		Array<String> list = new Array<String>();
		Iterator<String> iterator = spriteAtlases.keySet().iterator();
		while (iterator.hasNext())
		{
			list.add(iterator.next()); //iterating atlasses
		}
		ui_spriteAtlasList = new List<String>(skin);
		ui_spriteAtlasList.setItems(list);
		
		list.clear();
		list = new Array<String>();
		for  (AtlasRegion a : spriteAtlases.get(gameMap.spriteAtlasFileName).getRegions())
		{
			list.add(a.name);
		}
		ui_spritesList = new List<String>(skin);
		ui_spritesList.setItems(list);
		
		ui_spriteAtlasList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				System.out.println("ATLAS CHANGE "+ui_spriteAtlasList.getSelected());
				setAtlas(ui_spriteAtlasList.getSelected());
			}
		});
		
		ui_spritesList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				setCurrentSprite(ui_spritesList.getSelected());
				System.out.println("SPRITES CHANGE "+ui_spritesList.getSelected());
			}
		});
		
//		ui_spritesList.
		Label lAtlas = new Label("Atlas", skin);
		Label lSprites = new Label("Sprites", skin);
		
		ScrollPane scrollPaneAtlas = new ScrollPane(ui_spriteAtlasList, skin);
		ScrollPane scrollPaneSprites = new ScrollPane(ui_spritesList, skin);
		
		ui_spritesTable.add(lAtlas).left().top().row();
		ui_spritesTable.add(scrollPaneAtlas).minWidth(160).maxHeight(100).left();
		
		ui_spritesTable.add(sliderTable).right().top().padLeft(30).padTop(-30).row();
		
		ui_spritesTable.add(lSprites).left().padBottom(14).padTop(40).row();
		ui_spritesTable.add(scrollPaneSprites).minWidth(160).maxHeight(130).left().row();
		
		muhStage.addActor(ui_spritesTable);
		
		ui_spritesTable.setVisible(false);
	}

	public void uim_setupEffectsSubMenu()
	{
		ui_effectsTable = new Table(skin);
		ui_effectsTable.align(Align.top);
		ui_effectsTable.setFillParent(true);
//		ui_effectsTable.setHeight(300);
		ui_effectsTable.padTop(100);
		TextButton proxy = new TextButton("EFFECTS SUB MENU", skin);
		ui_effectsTable.add(proxy).top();
		
		muhStage.addActor(ui_effectsTable);
		
		ui_effectsTable.setVisible(false);
	}
	
	public void uim_setupScriptSubMenu()
	{
		ui_scriptsTable = new Table();
		ui_scriptsTable.align(Align.topLeft);
		ui_scriptsTable.setFillParent(true);
		ui_scriptsTable.padTop(100);
		ui_scriptsTable.padLeft(20);
		
//		List<String> scriptList = new List<String>(skin);
//		Array<String> scriptIDs = new Array<String>();
//		for (MapScript s : gameMap.mapScripts)
//		{
//			scriptIDs.add(s.idprefix);
//		}
//		scriptList.setItems(scriptIDs);
		
		ui_scriptTextPrefix = new TextField("", skin);
		ui_scriptTriggerBox = new SelectBox<String>(skin);
		ui_scriptTriggerBox.setItems("MAP_LAUNCH", "COLLISION", "ACTION");
		
		TextButton saveb = new TextButton("Save", skin);
		
		uim_setButtonToNotCheck(saveb);
		
		saveb.addListener(new ClickListener() { public void clicked(InputEvent event, float x, float y) {
			if (markScript != null) {
				markScript.idprefix = ui_scriptTextPrefix.getText();
				markScript.trigger  = MapScript.TRIGGER.valueOf(ui_scriptTriggerBox.getSelected());
			}
			muhStage.unfocusAll();
		}});
		
		Table table = new Table();
		table.setFillParent(false);
		table.add(new Label("Prefix:  ", skin));
		table.add(ui_scriptTextPrefix).row();
		table.add(ui_scriptTriggerBox).colspan(2).fill().row();
		table.add(saveb).colspan(2).fill();
		
		ui_scriptsTable.add(table);
		muhStage.addActor(ui_scriptsTable);
		
		ui_scriptsTable.setVisible(false);
	}
	
	public void uim_setupObjectSubMenu()
	{
		ui_objectsTable = new Table(skin);
		ui_objectsTable.align(Align.top);
		ui_objectsTable.setFillParent(true);
		ui_objectsTable.padTop(100);
		TextButton proxy = new TextButton("OBJECTS SUB MENU", skin);
		ui_objectsTable.add(proxy).top();
		
		muhStage.addActor(ui_objectsTable);
		
		ui_objectsTable.setVisible(false);
	}
	
	public void uim_setupNpcsSubMenu()
	{
		ui_npcsTable = new Table(skin);
		ui_npcsTable.align(Align.top);
		ui_npcsTable.setFillParent(true);
		ui_npcsTable.padTop(100);
		TextButton proxy = new TextButton("NPCS SUB MENU", skin);
		ui_npcsTable.add(proxy).top();
		
		muhStage.addActor(ui_npcsTable);
		
		ui_npcsTable.setVisible(false);
	}
	
	public void uim_setupPlatformsSubMenu()
	{
		ui_platformsTable = new Table(skin);
		ui_platformsTable.align(Align.top);
		ui_platformsTable.setFillParent(true);
		ui_platformsTable.padTop(100);
		TextButton proxy = new TextButton("PLATFORMS SUB MENU", skin);
		ui_platformsTable.add(proxy).top();
		
		muhStage.addActor(ui_platformsTable);
		
		ui_platformsTable.setVisible(false);
	}
	
	public void uim_setupLightsSubMenu()
	{
		ui_lightsTable = new Table(skin);
		ui_lightsTable.align(Align.topLeft);
		ui_lightsTable.setFillParent(true);
		ui_lightsTable.padTop(65);
		ui_lightsTable.padLeft(20);
		
		ui_globalLightGamma			= new TextButton("Gamma Correction", skin);
		ui_globalLightDiffuse		= new TextButton("Diffuse", skin);
		ui_globalLightCulling		= new TextButton("Culling", skin);
		ui_globalLightShadows		= new TextButton("Shadows", skin);
		ui_globalLightBlur			= new TextButton("Blur", skin);
		ui_globalLightBlurNum		= new Label("Blur Num: ", skin);
		ui_globalLightBlurPlus		= new TextButton("+", skin);
		ui_globalLightBlurMinus		= new TextButton("-", skin);
		uim_setButtonToNotCheck(ui_globalLightBlurPlus);
		uim_setButtonToNotCheck(ui_globalLightBlurMinus);
		ui_globalLightGamma.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				RayHandler.setGammaCorrection(ui_globalLightGamma.isChecked()); 
			}
		});
		ui_globalLightDiffuse.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				RayHandler.useDiffuseLight(ui_globalLightDiffuse.isChecked()); 
			}
		});
		ui_globalLightCulling.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				gameMap.rayHandler.setCulling(ui_globalLightCulling.isChecked()); 
			}
		});
		ui_globalLightShadows.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				gameMap.rayHandler.setShadows(ui_globalLightShadows.isChecked()); 
			}
		});
		ui_globalLightBlur.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				gameMap.rayHandler.setBlur(ui_globalLightBlur.isChecked()); 
			}
		});
		ui_globalLightBlurPlus.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				blurNum++;
				gameMap.rayHandler.setBlurNum(blurNum);
				ui_globalLightBlurNum.setText("Blur Num: "+blurNum);
			}
		});
		ui_globalLightBlurMinus.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				blurNum--;
				gameMap.rayHandler.setBlurNum(blurNum);
				ui_globalLightBlurNum.setText("Blur Num: "+blurNum);
			}
		});
		
		ui_sliderAmbientRed    = new Slider(1, 255, 1, false, skin);
		ui_sliderAmbientGreen  = new Slider(1, 255, 1, false, skin);
		ui_sliderAmbientBlue   = new Slider(1, 255, 1, false, skin);
		ui_sliderAmbientAlpha  = new Slider(1, 255, 1, false, skin);
		
		ChangeListener chgam = new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				gameMap.rayHandler.setAmbientLight(ui_sliderAmbientRed.getValue()/255.0f, ui_sliderAmbientGreen.getValue()/255.0f, ui_sliderAmbientBlue.getValue()/255.0f, ui_sliderAmbientAlpha.getValue()/255.0f);
			}
		};
		ui_sliderAmbientRed.addListener(chgam);
		ui_sliderAmbientGreen.addListener(chgam);
		ui_sliderAmbientBlue.addListener(chgam);
		ui_sliderAmbientAlpha.addListener(chgam);
		
		Table globalLightTable = new Table();
		globalLightTable.setFillParent(false);
		globalLightTable.add(new Label("Global:", skin)).width(200).colspan(3).row();
		globalLightTable.add(ui_globalLightGamma).width(200).colspan(3).row();
		globalLightTable.add(ui_globalLightDiffuse).width(200).colspan(3).row();
		globalLightTable.add(ui_globalLightCulling).width(200).colspan(3).row();
		globalLightTable.add(ui_globalLightShadows).width(200).colspan(3).row();
		globalLightTable.add(ui_globalLightBlur).width(200).colspan(3).row();
		globalLightTable.add(ui_globalLightBlurNum);
		globalLightTable.add(ui_globalLightBlurMinus).width(40);
		globalLightTable.add(ui_globalLightBlurPlus).width(40).row();
		globalLightTable.add(new Label("Red:", skin));
		globalLightTable.add(ui_sliderAmbientRed).width(160).colspan(2).row();
		globalLightTable.add(new Label("Green:", skin));
		globalLightTable.add(ui_sliderAmbientGreen).width(160).colspan(2).row();
		globalLightTable.add(new Label("Blue:", skin));
		globalLightTable.add(ui_sliderAmbientBlue).width(160).colspan(2).row();
		globalLightTable.add(new Label("Alpha:", skin));
		globalLightTable.add(ui_sliderAmbientAlpha).width(160).colspan(2).row();
		
		// ############# ONE LIGHT #####################################
		ui_lightStatic 	= new TextButton("Static", skin);
		ui_lightXRay 	= new TextButton("X Ray", skin);
		ui_lightSoft 	= new TextButton("Soft", skin);
		ui_lightStatic.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (markLight != null) markLight.setStaticLight(ui_lightStatic.isChecked());
			}
		});
		ui_lightXRay.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (markLight != null) markLight.setXray(ui_lightXRay.isChecked());
			}
		});
		ui_lightSoft.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				if (markLight != null) markLight.setSoft(ui_lightSoft.isChecked());
			}
		});
		
		ui_sliderLightRed    = new Slider(1, 255, 1, false, skin);
		ui_sliderLightGreen  = new Slider(1, 255, 1, false, skin);
		ui_sliderLightBlue   = new Slider(1, 255, 1, false, skin);
		ui_sliderLightAlpha  = new Slider(1, 255, 1, false, skin);
		
		ChangeListener chg = new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				if (markLight != null) markLight.setColor(ui_sliderLightRed.getValue()/255.0f, ui_sliderLightGreen.getValue()/255.0f, ui_sliderLightBlue.getValue()/255.0f, ui_sliderLightAlpha.getValue()/255.0f);
			}
		};
		ui_sliderLightRed.addListener(chg);
		ui_sliderLightGreen.addListener(chg);
		ui_sliderLightBlue.addListener(chg);
		ui_sliderLightAlpha.addListener(chg);
		
		ui_syncLightElements();
		
		Table sliderTable = new Table();
		sliderTable.setFillParent(false);
		sliderTable.align(Align.right);
		
		sliderTable.add(new Label("Red:  ", skin));
		sliderTable.add(ui_sliderLightRed).padLeft(20).width(200).row();
		sliderTable.add(new Label("Green:  ", skin));
		sliderTable.add(ui_sliderLightGreen).padLeft(20).width(200).row();
		sliderTable.add(new Label("Blue:  ", skin));
		sliderTable.add(ui_sliderLightBlue).padLeft(20).width(200).row();
		sliderTable.add(new Label("Alpha:  ", skin));
		sliderTable.add(ui_sliderLightAlpha).padLeft(20).width(200).row();
		//##################################################################################
		
		ui_lightsTable.add(globalLightTable).row();
		ui_lightsTable.add(new Label("This Light:", skin)).padTop(100).minWidth(240).row();
		ui_lightsTable.add(ui_lightStatic).minWidth(240).row();
		ui_lightsTable.add(ui_lightXRay).minWidth(240).row();
		ui_lightsTable.add(ui_lightSoft).minWidth(240).row();
		ui_lightsTable.add(sliderTable);
		
		muhStage.addActor(ui_lightsTable);
		
		ui_lightsTable.setVisible(false);
	}
	
	public void uim_setupLayerList()
	{
		ui_layerListTable = new Table();
		ui_layerListTable.align(Align.topRight);
		ui_layerListTable.setFillParent(true);
		ui_layerListTable.padRight(12);
		
		ui_checkTable = new Table();
		ui_delTable = new Table();
		
		Table layerButtonsTable = new Table();
		layerButtonsTable.align(Align.right);
		ui_parallaxProxy = new TextButton("Parallax Settings", skin);
		ui_layerOrderUp = new TextButton("UP", skin);
		ui_layerOrderDown = new TextButton("DOWN", skin);
		ui_layerOrderUp.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				int curIndex = gameMap.layers.indexOf(currentLayer);
				gameMap.layers.remove(currentLayer);
				if (curIndex == 0)
					gameMap.layers.add(gameMap.layers.size(), currentLayer);
				else
					gameMap.layers.add(curIndex-1, currentLayer);
				
				uim_reserLayerList();
			}
		});
		ui_layerOrderDown.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				int curIndex = gameMap.layers.indexOf(currentLayer);
				gameMap.layers.remove(currentLayer);
				if (curIndex == gameMap.layers.size())
					gameMap.layers.add(0, currentLayer);
				else
					gameMap.layers.add(curIndex+1, currentLayer);
				
				uim_reserLayerList();
			}
		});
		
		uim_setButtonToNotCheck(ui_parallaxProxy);
		uim_setButtonToNotCheck(ui_layerOrderUp);
		uim_setButtonToNotCheck(ui_layerOrderDown);
		layerButtonsTable.add(ui_parallaxProxy).right().top().padRight(10).padBottom(60).row();
		layerButtonsTable.add(ui_layerOrderUp).right().bottom().padRight(10).width(90).row();
		layerButtonsTable.add(ui_layerOrderDown).right().bottom().padRight(10).width(90).row();
		
		ui_layerlist = new List<String>(skin);
		uim_reserLayerList();
		
		ui_layerlist.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				ArrayList<MapLayer> list = gameMap.layers;
				for (int i=0;i<list.size();i++)
				{
					if (list.get(i).infoName.equals(ui_layerlist.getSelected()))
					{
						currentLayer = list.get(i);
						break;
					}
				}
				ui_tilesheetList.setSelected((currentLayer).tileSheet.name);
			}
		});
		
		ui_layerListTable.add(layerButtonsTable).padRight(10);
		ui_layerListTable.add(ui_layerlist);
		ui_layerListTable.add(ui_checkTable);
		ui_layerListTable.add(ui_delTable);
		
		
		ui_layerListTable.padTop(60);
		
		muhStage.addActor(ui_layerListTable);
	}
	
	private void uim_reserLayerList()
	{
		ui_checkTable.clear();
		ui_checkTable.padLeft(6);
		ui_checkTable.padRight(6);
		ui_checkTable.background(uiblack);
		ui_layerVisChkBoxes = new CheckBox[gameMap.layers.size()];
		for (int i=0; i<ui_layerVisChkBoxes.length; i++)
		{
			ui_layerVisChkBoxes[i] = new CheckBox("", skin);
			ui_layerVisChkBoxes[i].setChecked(true);
			ui_checkTable.add(ui_layerVisChkBoxes[i]).height(25).padTop(3).padBottom(3).row();
		}
		
		ui_delTable.clear();
		ui_delTable.padLeft(6);
		ui_delTable.padRight(6);
		ui_delTable.background(uiblack);
		TextButton[] delButton = new TextButton[gameMap.layers.size()];
		for (int i=0; i<delButton.length; i++)
		{
			delButton[i] = new TextButton("DEL", skin);
			uim_setButtonToNotCheck(delButton[i]);
			
			final int listindex = i;
			delButton[i].addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					
					MapLayer toRemove = null;
					for (int j=0;j<gameMap.layers.size();j++)
					{
						if (gameMap.layers.get(j).infoName.equals(ui_layerlist.getItems().get(listindex)))
						{
							toRemove = gameMap.layers.get(j);
							break;
						}
					}
					
					final MapLayer rem = toRemove;
					new Dialog("Delete Layer", skin, "dialog") {
						protected void result (Object object) {
//							System.out.println("Chosen: " + object);
							if (Boolean.parseBoolean(""+object) == true)
							{
								gameMap.layers.remove(rem);
								currentLayer = gameMap.layers.get(0);
								uim_reserLayerList();
							}
						}
					}.text("Do you want to delete layer "+rem.infoName+" ?").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(muhStage);
					
				}
			});
			
			ui_delTable.add(delButton[i]).height(25).padTop(3).padBottom(3).row();
		}
		
		Array<String> layerStrings = new Array<String>();
		for (MapLayer layer : gameMap.layers)
		{
			layerStrings.add(layer.infoName);
		}
		ui_layerlist.setItems(layerStrings);
		
		ui_layerlist.setSelected(currentLayer.infoName);
	}
	
	public void uim_setupEditMenu()
	{
		ui_editMenuTable = new Table();
		ui_editMenuTable.defaults().width(260);
		ui_editMenuTable.setFillParent(false);
		ui_editMenuTable.setPosition(160+82, Gdx.graphics.getHeight()-280);
//		fileTable.padTop(menuLeftButtons[0].getHeight());
		
		ui_editMenuButtons[0] = new TextButton("Change Layer Size", skin);
		ui_editMenuButtons[1] = new TextButton("Change Layer Meta", skin);
		ui_editMenuButtons[2] = new TextButton("Change Layer Name", skin);
		ui_editMenuButtons[3] = new TextButton("Change Map Size", skin);
		ui_editMenuButtons[4] = new TextButton("Change Map Display Name", skin);
		ui_editMenuButtons[5] = new TextButton("Toggle Show Objects", skin);
		ui_editMenuButtons[9] = new TextButton("Add Layer", skin);
		ui_editMenuButtons[10] = new TextButton("Fill Layer", skin);
		ui_editMenuButtons[11] = new TextButton("Wipe Layer (Tiles)", skin);
		ui_editMenuButtons[12] = new TextButton("Empty Unseen Tiles", skin);
		ui_editMenuButtons[13] = new TextButton("Fill Tile Line", skin);
		ui_editMenuButtons[14] = new TextButton("Reset Camera", skin);
		ui_editMenuButtons[15] = new TextButton("Toggle Show Box2D", skin);
		
		ui_editMenuButtons[0].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
//				uim_callDialog("Input Layer Size", field, inputIntention);
			}
		});
		
		ui_editMenuButtons[1].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				uim_callDialog("Input Layer Meta", ""+currentLayer.meta, INPUT_INTENTION.LAYER_META);
			}
		});
		
		ui_editMenuButtons[2].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				uim_callDialog("Input Layer Name", ""+currentLayer.infoName, INPUT_INTENTION.LAYER_NAME);
			}
		});
		
		ui_editMenuButtons[3].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				uim_callDialog("Input Map Size", gameMap.getBaseSizeWidthInTiles()+"x"+gameMap.getBaseSizeHeightInTiles(), INPUT_INTENTION.MAP_SIZE);
			}
		});

		ui_editMenuButtons[4].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				uim_callDialog("Input new Map Name", gameMap.name, INPUT_INTENTION.MAP_DNAME);
			}
		});

		ui_editMenuButtons[5].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				drawObjects = !drawObjects;
			}
		});
		
		ui_editMenuButtons[11].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				currentLayer.wipeTiles(spriteCache);
				popup("Wiped Layer from Tiles");
			}
		});
		
		ui_editMenuButtons[14].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				resetCamera();
			}
		});
		
		ui_editMenuButtons[15].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				drawBox2D = !drawBox2D;
			}
		});
		
		
		for (final TextButton b : ui_editMenuButtons)
		{
			if (b == null) continue;
			b.addListener(new ClickListener() {
				public void clicked(InputEvent event, float x, float y) {
					b.setChecked(false);
					ui_editMenuTable.setVisible(false);
				}
			});
			
			ui_editMenuTable.add(b).row();
		}
		
		muhStage.addActor(ui_editMenuTable);
		ui_editMenuTable.setVisible(false);
	}
	
	private void uim_callDialog(String message, String field, INPUT_INTENTION inputIntention)
	{
		dialogI = inputIntention;
		ui_dialogWindow.setVisible(true);
		ui_dialogWindow.setModal(true);
		ui_dialogLabel.setText(message);
		ui_dialogField.setText(field);
		ui_dialogWindow.setKeepWithinStage(true); 
	}
	
	private void resetCamera()
	{
		targetZoom = mapCamera.zoom = 1f;
		popup("Camera Reset");
	}

	private void uim_setButtonToNotCheck(final Button b)
	{
		b.addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				b.setChecked(false);
			}
		});
	}
	
	@Override	public void resume() { }
	@Override	public void resize(int width, int height) {	
    	mapCamera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//		 Gdx.gl.glViewport(0, 0, width, height);
    	hud_camera.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    	uiCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    	muhStage.getViewport().setCamera(uiCam);
    	
//    	float zoom = 1280.0f/width;
    	targetZoom = mapCamera.zoom = (1280.0f/width);
    	
    	for (MapLayer layer : gameMap.layers)
    	{
    		if (layer.hasParallaxCamera())
    		{
    			layer.initParallaxCamera(mapCamera);
    		}
    	}
		
//    	System.out.println("Resized to: "+width+" x "+height);
//    	System.out.println("GDX : "+Gdx.graphics.getWidth()+" x "+Gdx.graphics.getHeight());
//    	System.out.println("mapCamera viewport : "+mapCamera.viewportWidth+" x "+mapCamera.viewportHeight);
//    	System.out.println("hud_camera viewport : "+hud_camera.viewportWidth+" x "+hud_camera.viewportHeight);
	}
	@Override	public void pause() { }
	@Override	public void dispose()
	{
		//TODO removed for debug
//		gameMap.saveToFile(kryo, "exit_map");
		
		saveToConfig();
		
		spriteBatch.dispose();
		spriteCache.dispose();
		TileSelector.shapeRenderer.dispose();
		texBg.dispose();
	    texAlkahest.dispose();
	    texWhite.dispose();
	    font.dispose();
	    
	    Iterator iterator = tileSheets.values().iterator();
	    while(iterator.hasNext())
	    {
	    	((TileSheet)(iterator.next())).dispose();
	    }
	    iterator = spriteAtlases.values().iterator();
	    while(iterator.hasNext())
	    {
	    	((TextureAtlas)(iterator.next())).dispose();
	    }
	}
	
	private void saveToConfig()
	{
		int layer = 0;
		for (int i=0; i<gameMap.layers.size(); i++)
		{
			if (currentLayer == gameMap.layers.get(i))
			{
				layer = i;
				break;
			}
		}
		
//		Config.putBoolean("alkahest_showLight", 			drawLighting);
//		Config.putBoolean("alkahest_drawSlopes", 			drawSlopes);
		Config.putBoolean("alkahest_drawHelp", 				drawHelp);
		Config.putBoolean("alkahest_drawBox2D", 			drawBox2D); 
		Config.putInteger("alkahest_currentLayer",			layer);
		Config.prefs.flush();
	}
	
	private boolean isDelKeyDown()
	{
		return Gdx.input.isKeyPressed(Input.Keys.FORWARD_DEL);
	}
	
	private boolean isEditKeyDown()
	{
		return Gdx.input.isKeyPressed(Input.Keys.TAB);
	}
	
	public void compute()
	{
		placeSprite.setPosition(getRelativeMouseX(mapCamera)-placeSprite.getWidth()*0.5f, getRelativeMouseY(mapCamera)-placeSprite.getHeight()*0.5f);
		
		if (Math.abs(targetZoom-mapCamera.zoom) > 0.01f)
		{
			mapCamera.zoom+=(targetZoom-mapCamera.zoom)/10.0f;
			keepMapCameraWithinBounds();
		}
		else
		{
			mapCamera.zoom = targetZoom;
			mapCamera.zoom = ((float)Math.round(mapCamera.zoom*100f)/100f);
			keepMapCameraWithinBounds();
		}
		
		mapCamera.position.x = (float)Math.round(mapCamera.position.x);
		mapCamera.position.y = (float)Math.round(mapCamera.position.y);
		
		hud_camera.update();
		mapCamera.update();
	}
	
	private void popup(String _str)
	{
		popupAlpha 	= 1f;
		popupStr 	= _str;
	}
	
	@Override
	public void render()
	{
		compute();
		TileSelector.compute();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		spriteBatch.setColor(Color.WHITE);

		//BACKGROUND
		spriteBatch.setProjectionMatrix(hud_camera.combined);
		spriteBatch.begin();
			spriteBatch.setColor(Color.BLACK);
			spriteBatch.draw(GFX.texWhite, 0, 0, Gdx.graphics.getWidth(), topMenuHeight);
			spriteBatch.setColor(Color.WHITE);
			spriteBatch.draw(texBg, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			spriteBatch.draw(texAlkahest, (Gdx.graphics.getWidth()*0.5f)-(texAlkahest.getWidth()*0.5f), (Gdx.graphics.getHeight()*0.5f)+(texAlkahest.getHeight()*0.5f), texAlkahest.getWidth(), -texAlkahest.getHeight());
		spriteBatch.end();
		
		for (int i=0;i<gameMap.layers.size();i++)
		{
			if (ui_layerVisChkBoxes[i].isChecked()) gameMap.getLayer(i).draw(spriteCache, mapCamera);
		}
		
		if (mode == MODE.LIGHTS) gameMap.renderLight(mapCamera);
		
		TileSelector.draw();
		
		int tileIndexX = getMapTileMouseX();
		int tileIndexY = getMapTileMouseY();
		
		spriteBatch.setProjectionMatrix(hud_camera.combined);
		spriteBatch.setColor(Color.WHITE);
		spriteBatch.begin();
			font.setColor(Color.WHITE);
			font.draw(spriteBatch, "pos: "+mapCamera.position.x + " , "+mapCamera.position.y + " @ " + mapCamera.zoom + "x  -  "+"Mouse: "+getRelativeMouseX(mapCamera)+" , "+getRelativeMouseY(mapCamera),  Gdx.graphics.getWidth()*0.4f, Gdx.graphics.getHeight()-font.getLineHeight()*1.15f);
			font.draw(spriteBatch, "FPS: "+Gdx.graphics.getFramesPerSecond()+"      tile index: "+tileIndexX + " , "+tileIndexY, Gdx.graphics.getWidth()*0.4f, Gdx.graphics.getHeight()-font.getLineHeight()*2.2f);
//			if(mode == MODE.SLOPES) font.draw(spriteBatch, ""+slopeType, Gdx.graphics.getWidth()-font.getBounds(""+slopeType).width-10, font.getLineHeight()*2.15f);
			font.draw(spriteBatch, gameMap.displayName+" ("+gameMap.name+")"+ "   Base Size: "+gameMap.getBaseSizeWidthInTiles()+" x "+gameMap.getBaseSizeHeightInTiles(), 10, Gdx.graphics.getHeight()-font.getLineHeight()*3.2f);
			font.draw(spriteBatch, "Layer: "+currentLayer.infoName+"  ("+currentLayer.meta+")  size: "+currentLayer.width+" x "+currentLayer.height, 10, Gdx.graphics.getHeight()-font.getLineHeight()*2.2f);
			if (currentLayer instanceof MapLayer) font.draw(spriteBatch, "Tilesheet: "+(currentLayer).tileSheet.name, 10, Gdx.graphics.getHeight()-font.getLineHeight()*1.2f);
			font.draw(spriteBatch, "MarkSprite: "+markSprite, Gdx.graphics.getWidth()*0.35f, 70);
			
			if (mode == MODE.SPRITES)
			{
				if (!isEditKeyDown() && !isDelKeyDown())
				{
					if (placeSprite != null)
					{
						spriteBatch.setProjectionMatrix(mapCamera.combined);
						placeSprite.draw(spriteBatch, 0.5f);
					}
				}
				else
				{
					if (isEditKeyDown())
					{
						spriteBatch.setProjectionMatrix(mapCamera.combined);
						tempSprite = gameMap.getSpriteOnPosition(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera), currentLayer);
						
						if (tempSprite != null)
						{
							spriteBatch.setColor(Colors.yellow50);			
							spriteBatch.draw(tempSprite, tempSprite.getX(), tempSprite.getY(), 
									tempSprite.getOriginX(), tempSprite.getOriginY(), tempSprite.getWidth(), tempSprite.getHeight(), 
									tempSprite.getScaleX(), tempSprite.getScaleY(), tempSprite.getRotation()); 
						}
					}
					
					if (markSprite != null)
					{
						spriteBatch.setColor(0f, 0f, 0f, 0.5f);
						spriteBatch.setProjectionMatrix(mapCamera.combined);
//							spriteBatch.draw(markSprite, markSprite.getX(), markSprite.getY());
							spriteBatch.draw(markSprite, markSprite.getX(), markSprite.getY(), 
									markSprite.getOriginX(), markSprite.getOriginY(), markSprite.getWidth(), markSprite.getHeight(), 
									markSprite.getScaleX(), markSprite.getScaleY(), markSprite.getRotation());
						spriteBatch.setColor(Color.WHITE);
					}
				}
			}
			else if (mode == MODE.TILES)
			{
				if (!isEditKeyDown() && !isDelKeyDown() && tileIndexX > -1 && tileIndexY > -1)
				{
					drawTilePreview(tileIndexX, tileIndexY);
				}
			}
			
		spriteBatch.end();
		
		if (mode == MODE.SLOPES)
		{
			if (!isEditKeyDown() && !isDelKeyDown() && tileIndexX > -1 && tileIndexY > -1)	drawSlopePreview(tileIndexX, tileIndexY);
		}
		
		if (drawBox2D)		gameMap.drawBox2D(mapCamera);
		
		if (tileIndexX > -1 && tileIndexY > -1)
		{
			if (mode == MODE.TILES)
			{
				if (!TileSelector.visible)		drawGrid(tileIndexX, tileIndexY, TileSelector.getSelectionWidth(), TileSelector.getSelectionHeight());
			}
		}
		
		if (mode == MODE.LIGHTS) // Light sources indicators
		{
			spriteBatch.setProjectionMatrix(mapCamera.combined);
			spriteBatch.begin();
				for (Light light : gameMap.lights)
			    {
			    	spriteBatch.setColor(Colors.white5);
			    	if (markLight == light)	spriteBatch.setColor(Colors.yellow50);
			    	spriteBatch.draw(texWhite, light.getX()-15, light.getY()-15, 30, 30);
			    }
			spriteBatch.end();
		}
		else if  (mode == MODE.SCRIPTS)		drawScripts();
		else if  (mode == MODE.SLOPES)		drawSlopes();
		
		if (popupAlpha > 0f)
		{
			spriteBatch.setProjectionMatrix(hud_camera.combined);
			spriteBatch.begin();
				bigFont.setColor(1f, 1f, 0f, popupAlpha);
				glyphL.setText(bigFont, popupStr);
				bigFont.draw(spriteBatch, popupStr, (Gdx.graphics.getWidth()/2f)-(glyphL.width/2f), (Gdx.graphics.getHeight()/2f)-(glyphL.height/2f));
				popupAlpha-=0.01f;
				if (popupAlpha < 0f) popupAlpha = 0f;
				bigFont.setColor(Color.WHITE);
			spriteBatch.end();
		}
		
		if (drawHelp)	drawHelp();
		
		muhStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		muhStage.draw();
	}
	
	private void drawScripts()
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		spriteBatch.setProjectionMatrix(mapCamera.combined);
		spriteBatch.begin();
			
			for (MapScript script : gameMap.mapScripts)
			{
				spriteBatch.setColor(Colors.blue50);
				if (markScript != null && markScript == script) spriteBatch.setColor(Colors.yellow50);
				spriteBatch.draw(texWhite, script.rect.x, script.rect.y, script.rect.width, script.rect.height);
				font.setColor(Colors.white80);
				font.draw(spriteBatch, "Trigger: "+script.trigger, script.rect.x+15, script.rect.y+15);
				font.draw(spriteBatch, "ID: "+script.idprefix, script.rect.x+15, script.rect.y+(font.getLineHeight()*1.2f)+15);
			}
			
			spriteBatch.setColor(Colors.white5);
			if (curScriptRect != null) spriteBatch.draw(texWhite, curScriptRect.x, curScriptRect.y, curScriptRect.width, curScriptRect.height);
			
			font.setColor(Color.WHITE);
			spriteBatch.setColor(Color.WHITE);
		spriteBatch.end();
	}
	
	private void drawHelp()
	{
		spriteBatch.setProjectionMatrix(hud_camera.combined);
		spriteBatch.begin();
			float ox = (Gdx.graphics.getWidth()  * 0.5f) - (texHelp.getWidth()  * 0.5f);
			float oy = (Gdx.graphics.getHeight() * 0.5f) - (texHelp.getHeight() * 0.5f);
			spriteBatch.draw(texHelp, ox, oy);
		
			spriteBatch.setColor(Color.WHITE);

		spriteBatch.end();
	}

	private void drawTilePreview(int tileIndexX, int tileIndexY)
	{
		if (!TileSelector.visible)
		{
			if (currentLayer.hasParallaxCamera()) tempCam = currentLayer.getParallaxCamera(mapCamera);
			else								  tempCam = mapCamera;
			
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			spriteBatch.setColor(Colors.white80);
			float x1 = (tempCam.viewportWidth/2f)  - (tempCam.position.x/tempCam.zoom) + (tileIndexX*MapLayer.TILESIZE/tempCam.zoom);
			float y1 = (tempCam.viewportHeight/2f) - (tempCam.position.y/tempCam.zoom) + (tileIndexY*MapLayer.TILESIZE/tempCam.zoom);
//			y1*=-1;
				for (int iy = 0; iy < ((int)TileSelector.selectedTileAreaEnd.y-(int)TileSelector.selectedTileAreaStart.y)+1; iy++)
				{
					for (int ix = 0; ix < ((int)TileSelector.selectedTileAreaEnd.x-(int)TileSelector.selectedTileAreaStart.x)+1; ix++)
					{
						spriteBatch.draw(TileSelector.tileSheet.getTile((int)TileSelector.selectedTileAreaStart.x+ix, (int)TileSelector.selectedTileAreaStart.y+iy), 
								x1+(ix*MapLayer.TILESIZE/tempCam.zoom), y1+(iy*MapLayer.TILESIZE/tempCam.zoom), MapLayer.TILESIZE/tempCam.zoom, MapLayer.TILESIZE/tempCam.zoom);
					}
				}
			spriteBatch.setColor(Color.WHITE);
		}
	}
	
	private void drawSlopePreview(int tileIndexX, int tileIndexY)
	{
		shapeRenderer.setProjectionMatrix(mapCamera.combined);
		shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.CYAN);
			if (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
			{
				if (slopeVec != null) shapeRenderer.line(slopeVec.x, slopeVec.y, getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera));
				shapeRenderer.circle(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera), 4);
			}
			else
			{
				if (slopeVec != null) shapeRenderer.line(slopeVec.x, slopeVec.y, tileIndexX*MapLayer.TILESIZE, tileIndexY*MapLayer.TILESIZE);
				shapeRenderer.circle(tileIndexX*MapLayer.TILESIZE, tileIndexY*MapLayer.TILESIZE, 4);
			}
		shapeRenderer.end();
	}
	
	private void drawSlopes()
	{
		shapeRenderer.setProjectionMatrix(mapCamera.combined);
		shapeRenderer.begin(ShapeType.Line);
			for (int i=0;i<gameMap.slopes.size();i++)
			{
				shapeRenderer.setColor(Color.WHITE);
				if (markSlope != null && markSlope == gameMap.slopes.get(i))	shapeRenderer.setColor(Color.YELLOW);
				shapeRenderer.line(gameMap.slopes.get(i).getP1().x, gameMap.slopes.get(i).getP1().y, gameMap.slopes.get(i).getP2().x, gameMap.slopes.get(i).getP2().y);
			}
		shapeRenderer.end();
	}
	
	private void drawGrid(int tileIndexX, int tileIndexY, int regionWidth, int regionHeight)
	{
		Color color1 = Colors.blue0;
		Color color2 = Colors.blue100;
		
		if (currentLayer.hasParallaxCamera()) tempCam = currentLayer.getParallaxCamera(mapCamera);
		else								  tempCam = mapCamera;
		
		if (isDelKeyDown())
		{
			color1 = Colors.red0;
			color2 = Colors.red100;
		}
		
		tempShapeCam = new OrthographicCamera(tempCam.viewportWidth, tempCam.viewportHeight);
		tempShapeCam.setToOrtho(false);
		tempShapeCam.position.set(tempCam.position);
		tempShapeCam.zoom = tempCam.zoom;
		
			
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		float sx1 = (tempCam.viewportWidth/2f)-(tempCam.position.x/tempCam.zoom)+(tileIndexX*MapLayer.TILESIZE/tempCam.zoom);
		float sy1 = (tempCam.viewportHeight/2f)+(tempCam.position.y/tempCam.zoom)-((tileIndexY+1)*MapLayer.TILESIZE/tempCam.zoom);
		float sx2 = (tempCam.viewportWidth/2f)-(tempCam.position.x/tempCam.zoom)+(tileIndexX*MapLayer.TILESIZE/tempCam.zoom);
		float sy2 = (tempCam.viewportHeight/2f)+(tempCam.position.y/tempCam.zoom)-((tileIndexY+1)*MapLayer.TILESIZE/tempCam.zoom);
		
		//tilefactor
		float tfx = MapLayer.TILESIZE/tempCam.zoom;
		float tfy = MapLayer.TILESIZE/tempCam.zoom;
		tfx*=regionWidth;
		tfy*=regionHeight;
		
		sy1-=tfy-(MapLayer.TILESIZE/tempCam.zoom);
		sy2-=tfy-(MapLayer.TILESIZE/tempCam.zoom);
		
		shapeRenderer.setProjectionMatrix(tempShapeCam.combined);
		
			if (isDelKeyDown())
			{
				shapeRenderer.begin(ShapeType.Filled);
					shapeRenderer.setColor(Colors.red50);
					shapeRenderer.rect(sx1, sy1, regionWidth*(MapLayer.TILESIZE/tempCam.zoom), regionHeight*(MapLayer.TILESIZE/tempCam.zoom));
					shapeRenderer.setColor(Color.WHITE);
				shapeRenderer.end();
			}
		
		
		shapeRenderer.begin(ShapeType.Line);
			
		//horizontal
			shapeRenderer.line(sx1-(tfy*2), 	sy1, sx2+(tfy*0.5f), sy2, 			color1, color2); // bottom left
			shapeRenderer.line(sx1-(tfy*2), 	sy1+tfy, sx2+(tfy*0.5f), sy2+tfy,  	color1, color2); // upper left
			shapeRenderer.line(sx2+(tfy*0.5f), 	sy1, sx2+(tfy*3f), sy2, 			color2, color1); // bottom right
			shapeRenderer.line(sx2+(tfy*0.5f), 	sy1+tfy, sx2+(tfy*3f), sy2+tfy,  	color2, color1); // upper right
			
		//vertical
			shapeRenderer.line(sx1, sy1-(tfx*2), 		sx2, sy2+(tfx*0.5f), 		color1, color2);
			shapeRenderer.line(sx1+tfx, sy1-(tfx*2), 	sx2+tfx, sy2+(tfx*0.5f),  	color1, color2);
			shapeRenderer.line(sx1, sy2+(tfx*0.5f), 	sx2, sy2+(tfx*3f), 			color2, color1);
			shapeRenderer.line(sx1+tfx, sy2+(tfx*0.5f), sx2+tfx, sy2+(tfx*3f),  	color2, color1);
			
		shapeRenderer.end();
	}
	
//	private void trace(Object obj)
//	{
//		System.out.println("["+Thread.currentThread().getStackTrace()[3].getLineNumber()+"] STUB: "+obj);
//	}
	
	private void saveMap()
	{
		//save
		System.out.print("\nSaving "+gameMap.name+" ...   ");
		
		gameMap.saveToFile(kryo);
		
		System.out.println("Done");
		popup("Saved "+gameMap.name);
	}
	
	private void loadMap(String mapName)
	{
		System.out.print("\nLoading "+mapName+" ...   ");
		
		currentLayer = null;
		
		try{
			gameMap = GameMap.loadCreateMap(kryo, mapName, spriteAtlases, tileSheets, spriteCache, mapCamera);
			reCache();
			currentLayer = gameMap.layers.get(0);
			ui_syncLightElements();
			System.out.println("Done");
			popup("Loaded "+mapName);
		}
		catch(Exception e){
			e.printStackTrace();
			popup("FAILED to load "+mapName);
		}
	}

	public GameMap loadinOldGameMap(String inName)
    {
    	GameMap map = null;
    	String dName = Util.readFromFile("content/old_maps/"+inName+"/mapinfo.dat");
    	String[] size = Util.readFromFile("content/old_maps/"+inName+"/mapsize.info").split(",");
    	
    	map = new GameMap(Integer.parseInt(size[0]), Integer.parseInt(size[1]), inName, dName, spriteAtlases.get("blah1"), "blah1");
    	
    	for (int i = 1;i<99;i++)
    	{
    		FileHandle fh = Gdx.files.internal("content/old_maps/"+inName+"/layer"+i+".info");
    		if (fh.exists())
    		{
    			MapLayer tLayer = loadinOldMapLayer(inName, i);
    			map.addLayer(tLayer);
    		}
    		else
    		{
    			break;
    		}
    	}

        return map;
    }
	
	public MapLayer loadinOldMapLayer(String inName, int layerIndex)
    {
    	String parts[] = null;
    	String whole   = null;
    	
    	whole = Util.readFromFile("content/old_maps/"+inName+"/layer"+layerIndex+".info");
        parts = whole.split(",");

        int w = Integer.parseInt(parts[0]);
        int h = Integer.parseInt(parts[1]);
//        String tileset = parts[2]; // unused
        
        String wholeBaseSize = Util.readFromFile("content/old_maps/"+inName+"/mapsize.info");
        int baseW = Integer.parseInt(wholeBaseSize.split(",")[0]);
        int baseH = Integer.parseInt(wholeBaseSize.split(",")[1]);
        
        MapLayer tLayer = new MapLayer(w, h, tileSheets.get("onebig"), parts[4], parts[3], baseW , baseH, mapCamera);

    	whole = Util.readFromFile("content/old_maps/"+inName+"/layer"+layerIndex+".map");
        parts = whole.split("\r\n|\r|\n");
        
        int i = 0;
        for (int x=0; x<w;x++)
        {
            for (int y=0; y<h;y++)
            {
            	if (!parts[i].equals("0"))
            	{
            		int xIndex = Integer.parseInt(parts[i].split(",")[0]);
            		int yIndex = Integer.parseInt(parts[i].split(",")[1]);
            		tLayer.setTile(x, y, xIndex, yIndex);
            	}
                i++; // go to next line in file
            }
        }
        
        return tLayer;
    }
	
	public int getMapTileMouseX()
	{
		int rx = -1;
		
		if (currentLayer.hasParallaxCamera())
			rx = getRelativeMouseX(currentLayer.getParallaxCamera(mapCamera));
		else
			rx = getRelativeMouseX(mapCamera);
		
		if (rx >= 0 && rx < currentLayer.width*MapLayer.TILESIZE)
		{
			return (rx/MapLayer.TILESIZE);
		}
		return -1;
	}

	public int getMapTileMouseY()
	{
		int ry = -1;
		
		if (currentLayer.hasParallaxCamera())
			ry = getRelativeMouseY(currentLayer.getParallaxCamera(mapCamera));
		else
			ry = getRelativeMouseY(mapCamera);
		
		if (ry >= 0 && ry < currentLayer.height*MapLayer.TILESIZE)
		{
			return (ry/MapLayer.TILESIZE);
		}
		return -1;
	}
	
	private int getRelativeMouseX(OrthographicCamera cam)
	{
		return (int)(Gdx.input.getX()*cam.zoom)+(int)(cam.position.x)-(int)((cam.viewportWidth*cam.zoom)/2.0f);
	}
	
	private int getRelativeMouseY(OrthographicCamera cam)
	{
		return (int)(Gdx.input.getY()*cam.zoom)+(int)(cam.position.y)-(int)((cam.viewportHeight*cam.zoom)/2.0f);
	}
	
	private void eraseTileNORECACHE(MapLayer tileLayer, int xi, int yi)
	{
		tileLayer.eraseTile(xi, yi);
	}
	
	private void eraseTile(MapLayer tileLayer, int xi, int yi)
	{
		tileLayer.eraseTile(xi, yi);
		reCache();
	}
	
	private void setTile(MapLayer tileLayer, int xi, int yi, int sheetX, int sheetY)
	{
		tileLayer.setTile(xi, yi, sheetX, sheetY);
		reCache();
	}
	
	private void setTileNORECACHE(MapLayer tileLayer, int xi, int yi, int sheetX, int sheetY)
	{
		tileLayer.setTile(xi, yi, sheetX, sheetY);
	}
	
	private void reCache()
	{
		spriteCache.clear();
		gameMap.cacheAll(spriteCache);
		TileSelector.cacheTileSheetGraphics();
	}
	
	private void keepMapCameraWithinBounds()
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
	
	private void switchTilesheet(String in)
	{
		spriteCache.clear();
		currentLayer.setTileSheet(tileSheets.get(in), spriteCache);
		TileSelector.setTileSheet(currentLayer.tileSheet);
		gameMap.cacheAll(spriteCache);
		
		ui_tilesheetList.setSelected((currentLayer).tileSheet.name);
	}
	
	private void dialogUsed(String in, INPUT_INTENTION inputIntention)
	{
		if (inputIntention == INPUT_INTENTION.LAYER_META)
		{
			currentLayer.meta = in;
			popup("Changed Meta");
		}
		else if (inputIntention == INPUT_INTENTION.LAYER_NAME)
		{
			currentLayer.infoName = in;
			popup("Changed layer name");
		}
		else if (inputIntention == INPUT_INTENTION.LAYER_ADD)
		{
			if (in != null && in.length() > 0)
			{
				try
				{
					String[] splits = in.split(",");
					
					MapLayer layer = new MapLayer(Integer.parseInt(splits[2].split("x")[0]), Integer.parseInt(splits[2].split("x")[1]), 
							currentLayer.tileSheet, 
							splits[0], splits[1], 
							gameMap.getBaseSizeWidthInTiles(), gameMap.getBaseSizeHeightInTiles(),
							mapCamera);
					int index = gameMap.layers.indexOf(currentLayer)+1;
					layer.cacheIt(spriteCache);
					gameMap.addLayer(layer, index);
					
					currentLayer = layer;
					popup("New layer");
				}
				catch(Exception e)
				{
					e.printStackTrace();
					popup("Adding layer FAILED");
				}
			}
		}
		else if (inputIntention == INPUT_INTENTION.MAP_DNAME)
		{
			gameMap.displayName = in;
			System.out.println("Set GameMap Display name to "+in);
		}
		else if (inputIntention == INPUT_INTENTION.MAP_SIZE)
		{
			gameMap.setBaseSize(Integer.parseInt(in.split("x")[0]), Integer.parseInt(in.split("x")[1]));
		}
	}
	
	private class MuhInputs extends InputAdapter
	{
		@Override public boolean keyDown(int keycode)
		{
			if (keycode == Input.Keys.S)
			{
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
				{
					saveMap();
				}
			}
			
			if (keycode == Input.Keys.L)
			{
				if (mode == MODE.LIGHTS && (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)))
				{
					gameMap.removeAllLights();
					popup("REMOVED ALL LIGHTS");
				}
			}
			
			if (keycode == Input.Keys.T)
			{
				if (mode == MODE.TILES)
				{
					String currentSheet = currentLayer.tileSheet.name;
					String gotoSheet = currentSheet;
					Iterator<String> iterator = tileSheets.keySet().iterator();
					String firstSheet = null;
					while (iterator.hasNext())
					{
						String str = iterator.next();
						if (firstSheet == null) firstSheet = str;
						boolean match = str.equals(currentSheet);
						if (match) 
						{
							if (iterator.hasNext())
								
								gotoSheet = iterator.next();
							else
								gotoSheet = firstSheet;
							
							break;
						}
					}
					switchTilesheet(gotoSheet);
				}
			}
			else if (keycode == Input.Keys.B)
			{
				currentLayer.spritesAbove = !currentLayer.spritesAbove;
				
				reCache();
			}
			
			if (keycode == Input.Keys.FORWARD_DEL)
			{
				if (mode == MODE.SCRIPTS)
				{
					if (markScript != null)
					{
						gameMap.mapScripts.remove(markScript);
						markScript = null;
						ui_scriptTextPrefix.setText("");
					}
				}
				else if (mode == MODE.LIGHTS)
				{
					if (markLight != null)
					{
						gameMap.removeLight(markLight);
						markLight = null;
					}
				}
				else if (mode == MODE.SPRITES)
				{
					if (markSprite != null)
					{
						currentLayer.eraseSprite(markSprite);
						markSprite = null;
						
						spriteCache.clear();
						gameMap.cacheAll(spriteCache);
						TileSelector.cacheTileSheetGraphics();
					}
				}
			}
			else if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN)
			{
				if (isEditKeyDown())
				{
					if (markSprite != null)
					{
						markSprite.flip(false, true);
						reCache();
					}
				}
			}
			else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT)
			{
				if (mode == MODE.SPRITES)
				{
					if (isEditKeyDown())
					{
						if (markSprite != null)
						{
							markSprite.flip(true, false);
							reCache();
						}
					}
				}
			}
			else if (keycode == Input.Keys.SPACE)
			{
				if (mode == MODE.SPRITES)
				{

				}
				else if (mode == MODE.SLOPES)
				{
//					if 		(slopeType == QSlope.TYPE.LU_FULL) slopeType = QSlope.TYPE.RU_FULL;
//					else if (slopeType == QSlope.TYPE.RU_FULL) slopeType = QSlope.TYPE.LU_HALF;
//					else if (slopeType == QSlope.TYPE.LU_HALF) slopeType = QSlope.TYPE.RU_HALF;
//					else if (slopeType == QSlope.TYPE.RU_HALF) slopeType = QSlope.TYPE.LU_FULL;
				}
				else if (mode == MODE.LIGHTS)
				{
//					RayHandler, rays, color, distance, x, y, directionDegree, coneDegree
					
//					ConeLight light = new ConeLight(gameMap.rayHandler, 80, ColorLib.defaultConeLight, 900, getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera), 90, 90);
					PointLight light = new PointLight(gameMap.rayHandler, 80, Colors.defaultConeLight, 900, getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera));
					if (Gdx.input.isKeyPressed(Keys.X)) light.setXray(true);
					if (Gdx.input.isKeyPressed(Keys.S)) light.setStaticLight(false);
					else								light.setStaticLight(true);
					
					gameMap.addLight(light);
					markLight = light;
					ui_syncLightElements();
				}
			}
			else if (keycode == Input.Keys.PAGE_DOWN)
			{
				if (!TileSelector.visible)
				{
					if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
					{
						ArrayList<MapLayer> list = gameMap.layers;
						for (int i=0;i<list.size();i++)
						{
							if (currentLayer == list.get(i))
							{
								if (i == list.size()-1)		currentLayer = list.get(0);
								else						currentLayer = list.get(i+1);
								
								break;		
							}
						}
						ui_layerlist.setSelected(currentLayer.infoName);
						ui_tilesheetList.setSelected((currentLayer).tileSheet.name);
					}
					else
					{
						int curIndex = gameMap.layers.indexOf(currentLayer);
						gameMap.layers.remove(currentLayer);
						if (curIndex == gameMap.layers.size())
							gameMap.layers.add(0, currentLayer);
						else
							gameMap.layers.add(curIndex+1, currentLayer);
						
						uim_reserLayerList();
					}
				}
			}
			else if (keycode == Input.Keys.PAGE_UP)
			{
				if (!TileSelector.visible)
				{
					if (!Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
					{
						ArrayList<MapLayer> list = gameMap.layers;
						for (int i=0;i<list.size();i++)
						{
							if (currentLayer == list.get(i))
							{
								if (i == 0)		currentLayer = list.get(list.size()-1);
								else			currentLayer = list.get(i-1);
								break;
							}
						}
						ui_layerlist.setSelected(currentLayer.infoName);
						ui_tilesheetList.setSelected((currentLayer).tileSheet.name);
					}
					else
					{
						int curIndex = gameMap.layers.indexOf(currentLayer);
						gameMap.layers.remove(currentLayer);
						if (curIndex == 0)
							gameMap.layers.add(gameMap.layers.size(), currentLayer);
						else
							gameMap.layers.add(curIndex-1, currentLayer);
						
						uim_reserLayerList();
					}
				}
				
			}
			
			if (keycode == Input.Keys.F1)
			{
				drawHelp = !drawHelp;
			}
			if (keycode == Input.Keys.F2)
			{
				resetCamera();
			}
			if (keycode == Keys.ESCAPE)			Gdx.app.exit();

			if (keycode == Input.Keys.F7)
			{
				gameMap.createLightWall();
				popup("Creating Light wall");
			}
			
			if (keycode == Input.Keys.F3)
			{
				dialogUsed(gameMap.displayName, INPUT_INTENTION.MAP_DNAME);
			}
			else if (keycode == Input.Keys.F4)
			{
				dialogUsed(gameMap.getBaseSizeWidthInTiles()+"x"+gameMap.getBaseSizeHeightInTiles(), INPUT_INTENTION.MAP_SIZE);
			}
			else if (keycode == Input.Keys.F9)
			{
				dialogUsed(currentLayer.infoName, INPUT_INTENTION.LAYER_NAME);
			}
			else if (keycode == Input.Keys.F10)
			{
				dialogUsed(currentLayer.meta, INPUT_INTENTION.LAYER_META);
			}
			else if (keycode == Input.Keys.F)
			{
				if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT))
				{
					if (mode == MODE.TILES && !isEditKeyDown() && !isDelKeyDown() && currentLayer.isEmpty())
					{
						for (int my = 0; my < currentLayer.height; my+=((int)TileSelector.selectedTileAreaEnd.y-(int)TileSelector.selectedTileAreaStart.y)+1)
						{
							for (int mx = 0; mx < currentLayer.width; mx+=((int)TileSelector.selectedTileAreaEnd.x-(int)TileSelector.selectedTileAreaStart.x)+1)
							{
								for (int iy = 0; iy < ((int)TileSelector.selectedTileAreaEnd.y-(int)TileSelector.selectedTileAreaStart.y)+1; iy++)
								{
									for (int ix = 0; ix < ((int)TileSelector.selectedTileAreaEnd.x-(int)TileSelector.selectedTileAreaStart.x)+1; ix++)
									{
										if (mx+ix >= currentLayer.width || my+iy >= currentLayer.height)	break;
										
										setTileNORECACHE(currentLayer, 
												mx+ix, my+iy, 
												(int)TileSelector.selectedTileAreaStart.x+ix, (int)TileSelector.selectedTileAreaStart.y+iy);
									}
								}
							}
						}
						currentLayer.cacheIt(spriteCache);
					}
					else
					{
						popup("! Failed: Layer has to be empty !");
					}
				}
			}
		
			return super.keyDown(keycode);
		}
		
		public boolean touchUp(int screenX, int screenY, int pointer, int button)
		{
			if (button == Input.Buttons.LEFT)
			{
				if (mode == MODE.TILES)
				{
					
				}
				else if (mode == MODE.SCRIPTS)
				{
					if (!isEditKeyDown() && !isDelKeyDown())
					{
						if (curScriptRect != null)
						{
							int rx = getRelativeMouseX(mapCamera);
							if (rx >= curScriptRect.x)	curScriptRect.width = rx-curScriptRect.x;
							
							int ry = getRelativeMouseY(mapCamera);
							if (ry >= curScriptRect.y)	curScriptRect.height = ry-curScriptRect.y;
							
							if (curScriptRect.width > 5 && curScriptRect.height > 5)
							{
								MapScript script = new MapScript(curScriptRect, MapScript.TRIGGER.COLLISION, "ID_NULL");
								gameMap.mapScripts.add(script);
								curScriptRect = null;
							}
						}
					}
				}
			}
			
			return super.touchUp(screenX, screenY, pointer, button);
		}
		
		@Override
        public boolean touchDown (int x, int y, int pointer, int button)
		{
			if (button == Input.Buttons.LEFT)
			{
				if (mode == MODE.TILES)
				{
					if (TileSelector.visible)
					{
						int tix = TileSelector.getTileSheetMouseX();
						int tiy = TileSelector.getTileSheetMouseY();
						if (tix > -1 && tiy > -1)
						{
							TileSelector.selectedTileAreaStart.set(tix, tiy);
							TileSelector.selectedTileAreaEnd.set(tix, tiy);
						}
					}
					else
					{
						if (!TileSelector.visible && getMapTileMouseX() > -1 && getMapTileMouseY() > -1)
						{
							for (int iy = 0; iy < ((int)TileSelector.selectedTileAreaEnd.y-(int)TileSelector.selectedTileAreaStart.y)+1; iy++)
							{
								for (int ix = 0; ix < ((int)TileSelector.selectedTileAreaEnd.x-(int)TileSelector.selectedTileAreaStart.x)+1; ix++)
								{
									if (!isEditKeyDown() && !isDelKeyDown())
									{
										setTileNORECACHE(currentLayer, 
												getMapTileMouseX()+ix, getMapTileMouseY()+iy, 
												(int)TileSelector.selectedTileAreaStart.x+ix, (int)TileSelector.selectedTileAreaStart.y+iy);
									}
									else if (isDelKeyDown())
									{
										eraseTileNORECACHE(currentLayer, getMapTileMouseX()+ix, getMapTileMouseY()+iy);
									}
								}
							}
							reCache();
//							currentLayer.cacheIt(spriteCache);
						}
					}
				}
				else if (mode == MODE.SPRITES)
				{
					if (!isEditKeyDown() && !isDelKeyDown())
					{
						if (placeSprite != null)
						{
							Sprite sp = spriteAtlases.get(gameMap.spriteAtlasFileName).createSprite(placeSpriteName);
							sp.setPosition(placeSprite.getX(), placeSprite.getY());
							
							//scale rotation and all that shit
							
							currentLayer.addSprite(sp, placeSpriteName);
							
							spriteCache.clear();
							gameMap.cacheAll(spriteCache);
							TileSelector.cacheTileSheetGraphics();
						}
					}
					else if (isEditKeyDown())
					{
						markSprite = gameMap.getSpriteOnPosition(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera), currentLayer);
					}
				}
				else if (mode == MODE.SLOPES)
				{
					if (!isEditKeyDown() && !isDelKeyDown())
					{
						float x1 = getMapTileMouseX()*MapLayer.TILESIZE;
						float y1 = getMapTileMouseY()*MapLayer.TILESIZE;
						
						if (slopeVec == null)
						{
							if (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
								slopeVec = new Vector2(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera));
							else
								slopeVec = new Vector2(x1, y1);
						}
						else
						{
							QSlope.TYPE type;
							Vector2 mouseVec = new Vector2(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera));
							
							if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
								mouseVec = new Vector2(getMapTileMouseX()*MapLayer.TILESIZE, getMapTileMouseY()*MapLayer.TILESIZE);

							Vector2 leftVec, rightVec;
							
							if (slopeVec.x < mouseVec.x)	leftVec = slopeVec;
							else							leftVec = mouseVec;
							
							if (leftVec == slopeVec)		rightVec = mouseVec;
							else							rightVec = slopeVec;
							
							if (leftVec.y < rightVec.y)
								type = QSlope.TYPE.LEFT_UP;
							else
								type = QSlope.TYPE.RIGHT_UP;
							
							gameMap.addSlope(type, slopeVec.x, slopeVec.y, mouseVec.x, mouseVec.y);
							slopeVec = null;
						}
					}
					else if (isEditKeyDown())
					{
						
					}
					else if (isDelKeyDown())
					{
						if (markSlope != null)
						{
							for (int i=0;i<gameMap.slopes.size();i++)
							{
								if (markSlope == gameMap.slopes.get(i)) { gameMap.slopes.remove(i); break; }
							}
						}
					}
				}
				else if (mode == MODE.LIGHTS)
				{
					if (!isEditKeyDown() && !isDelKeyDown())
					{
						markLight = gameMap.getLightOnPosition(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera));
						ui_syncLightElements();
					}
				}
				else if (mode == MODE.SCRIPTS)
				{
					if (!isEditKeyDown() && !isDelKeyDown())
					{
						if (gameMap.getScriptOnPosition(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera)) != null)
						{
							markScript = gameMap.getScriptOnPosition(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera));
							ui_scriptTextPrefix.setText(""+markScript.idprefix);
							ui_scriptTriggerBox.setSelected(markScript.trigger.toString());
						}
						else
						{
							curScriptRect = new Rectangle(getRelativeMouseX(mapCamera), getRelativeMouseY(mapCamera), 0, 0);
						}
					}
				}
			}
			else if (button == Input.Buttons.MIDDLE)
			{
				if (mode == MODE.TILES)	TileSelector.toggleVisible();
			}
			else if (button == Input.Buttons.RIGHT)
			{
				//due to scrolling, I do nothing
			}
			
			return super.touchDown(x, y, pointer, button);
		}
		
		@Override public boolean touchDragged(int screenX, int screenY, int pointer)
		{
			if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
			{
				if (TileSelector.visible)
				{
					TileSelector.tileSheetCamera.position.x-=Mouse.getDX()*TileSelector.tileSheetCamera.zoom;
					TileSelector.tileSheetCamera.position.y+=Mouse.getDY()*TileSelector.tileSheetCamera.zoom;
				}
				else
				{
					mapCamera.position.x-=Mouse.getDX()*mapCamera.zoom;
					mapCamera.position.y+=Mouse.getDY()*mapCamera.zoom;
					keepMapCameraWithinBounds();
				}
			}
			else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
			{
				if (mode == MODE.TILES)
				{
					if (!TileSelector.visible)
					{
						if (getMapTileMouseX() > -1 && getMapTileMouseY() > -1)
						{
							if (TileSelector.getSelectionWidth() == 1 && TileSelector.getSelectionHeight() == 1 )
							{
								if (!isEditKeyDown() && !isDelKeyDown())
								{
									setTile(currentLayer, 
											getMapTileMouseX(), getMapTileMouseY(), 
											(int)TileSelector.selectedTileAreaStart.x, (int)TileSelector.selectedTileAreaStart.y);
								}
								else if (isDelKeyDown())
								{
									eraseTile(currentLayer, getMapTileMouseX(), getMapTileMouseY());
								}
							}
							else if (TileSelector.getSelectionWidth() > 1 || TileSelector.getSelectionHeight() > 1 )
							{					//BIG SELECTION
								if (!isEditKeyDown() && !isDelKeyDown())
								{
									// lets only place a big set of tiles if EVERY tile is empty before
									boolean passThrough = true;
									for (int iy = 0; iy < TileSelector.getSelectionHeight(); iy++)
									{
										for (int ix = 0; ix < TileSelector.getSelectionWidth(); ix++)
										{
											if (!(currentLayer).isTileEmpty(getMapTileMouseX()+ix,  getMapTileMouseY()+iy))
											{
												passThrough = false; // if ANY tile is not empty, its not going to happen
//												break;
											}
										}
									}
									
									if (passThrough)
									{
										for (int iy = 0; iy < TileSelector.getSelectionHeight(); iy++)
										{
											for (int ix = 0; ix < TileSelector.getSelectionWidth(); ix++)
											{
												setTile(currentLayer, getMapTileMouseX()+ix, getMapTileMouseY()+iy, 
														(int)TileSelector.selectedTileAreaStart.x+ix, (int)TileSelector.selectedTileAreaStart.y+iy);
											}
										}
									}
								}
								else if (isDelKeyDown())
								{
									for (int iy = 0; iy < TileSelector.getSelectionHeight(); iy++)
									{
										for (int ix = 0; ix < TileSelector.getSelectionWidth(); ix++)
										{
											eraseTile(currentLayer, getMapTileMouseX()+ix, getMapTileMouseY()+iy);
										}
									}
								}
							}
						}
					}
					else
					{
						int tix = TileSelector.getTileSheetMouseX();
						int tiy = TileSelector.getTileSheetMouseY();
						if (tix > -1 && tiy > -1)
							TileSelector.selectedTileAreaEnd.set(tix, tiy);
					}
				}
				else if (mode == MODE.SPRITES)
				{
					if (isEditKeyDown())
					{
						if (markSprite != null)
						{
							if (Gdx.input.isKeyPressed(Keys.S))			markSprite.scale((Mouse.getDX()*mapCamera.zoom)/100f);
							else if (Gdx.input.isKeyPressed(Keys.R))	markSprite.rotate((Mouse.getDX()*mapCamera.zoom)%360f);
							else										markSprite.translate(Mouse.getDX()*mapCamera.zoom, -Mouse.getDY()*mapCamera.zoom);
							
							reCache();
						}
					}
				}
				else if (mode == MODE.SCRIPTS)
				{
					if (!isEditKeyDown() && !isDelKeyDown())
					{
						if (curScriptRect != null)
						{
							int rx = getRelativeMouseX(mapCamera);
							if (rx >= curScriptRect.x)	curScriptRect.width = rx-curScriptRect.x;
							
							int ry = getRelativeMouseY(mapCamera);
							if (ry >= curScriptRect.y)	curScriptRect.height = ry-curScriptRect.y;
						}
					}
					else if (isEditKeyDown())
					{
						if (markScript != null)
						{
							markScript.rect.x+=Mouse.getDX()*mapCamera.zoom;
							markScript.rect.y+=(-Mouse.getDY()*mapCamera.zoom);
						}
					}
				}
				else if (mode == MODE.LIGHTS)
				{
					if (markLight != null) markLight.setPosition(markLight.getX()+(Mouse.getDX()*mapCamera.zoom), markLight.getY()-(Mouse.getDY()*mapCamera.zoom) );
				}
			}
			
			
			return super.touchDragged(screenX, screenY, pointer);
		}
		
		@Override public boolean scrolled(int amount)
		{
			if (TileSelector.visible)
			{
				if (amount > 0 && TileSelector.targetZoom < 2.3f) // zoom out
					TileSelector.targetZoom+=0.2f;
				else if (amount < 0 && TileSelector.targetZoom > 0.3f)
					TileSelector.targetZoom-=0.2f;
			}
			else
			{
				if (Gdx.input.isKeyPressed(Input.Keys.G))
				{
					
				}
				else
				{
					if (amount > 0 && targetZoom < 2.3f) // zoom out
						targetZoom+=0.2f;
					else if (amount < 0 && targetZoom > 0.3f)
						targetZoom-=0.2f;
				}
			}
			
			return super.scrolled(amount);
		}
	}
}