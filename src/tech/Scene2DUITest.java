package tech;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import gfx.GFX;

public class Scene2DUITest extends ApplicationAdapter
{
	enum MODE { TILES, SPRITES, EFFECTS, LIGHTS, PLATFORM, NPCS, OBJECTS, SCRIPTS, SLOPES, AI } //animated tiles sprites maybe later
	MODE mode = MODE.TILES;
	SpriteBatch batch;
	
	Skin skin;
	Stage stage;
	Drawable uiblack;
	TextButton[] ui_menuLeftButtons  = new TextButton[2];
	TextButton[] ui_menuRightButtons = new TextButton[10];
	TextButton[] ui_editMenuButtons  = new TextButton[16];
	SelectBox<String> ui_mapList;
	List<String> ui_layerlist;
	Table ui_editMenuTable;
	Table ui_layerListTable, ui_tilesTable, ui_spritesTable, ui_effectsTable, ui_scriptsTable, ui_aiTable, ui_slopesTable, ui_objectsTable, ui_npcsTable, ui_platformsTable, ui_lightsTable;
	TextArea ui_scriptTextArea;
	TextButton ui_layerOrderUp, ui_layerOrderDown;
	TextButton ui_parallaxProxy;
	
	public static void main(String args[])
	{
    	System.out.println("Java Version: "+System.getProperty("java.version"));
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 0;
		config.vSyncEnabled = true;	
		config.resizable = true;
		config.width  = 1600;
		config.height = 900;
		
		new LwjglApplication(new Scene2DUITest(), config);
	}

	@Override
	public void create ()
	{
		batch = new SpriteBatch();
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
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

//		final Slider slider = new Slider(1, 100, 1, false, skin);
		
		setupTopMenu();
		setupLayerList();
		setupEditMenu();
		
		setupSlopesSubMenu();
		setupTilesSubMenu();
		setupSpritesSubMenu();
		setupEffectsSubMenu();
		setupScriptSubMenu();
		setupAiSubMenu();
		setupSlopesSubMenu();
		setupObjectSubMenu();
		setupNpcsSubMenu();
		setupPlatformsSubMenu();
		setupLightsSubMenu();
	}
	
	public void switchMode(MODE inmode)
	{
		mode = inmode;
		hideAllSubMenus();
		if (inmode == MODE.TILES || inmode == MODE.SPRITES || inmode == MODE.EFFECTS || inmode == MODE.LIGHTS)
		{
			ui_layerListTable.setVisible(true);
			if (inmode == MODE.TILES)			ui_tilesTable.setVisible(true);
			else if (inmode == MODE.SPRITES)		ui_spritesTable.setVisible(true);
			else if (inmode == MODE.EFFECTS)		ui_effectsTable.setVisible(true);
			else if (inmode == MODE.LIGHTS)			ui_lightsTable.setVisible(true);
		}
		else if (inmode == MODE.AI)				ui_aiTable.setVisible(true);
		else if (inmode == MODE.SCRIPTS)		ui_scriptsTable.setVisible(true);
		else if (inmode == MODE.PLATFORM)		ui_platformsTable.setVisible(true);
		else if (inmode == MODE.NPCS)			ui_npcsTable.setVisible(true);
		else if (inmode == MODE.OBJECTS)		ui_objectsTable.setVisible(true);
		else if (inmode == MODE.SCRIPTS)		ui_scriptsTable.setVisible(true);
		else if (inmode == MODE.SLOPES)			ui_slopesTable.setVisible(true);
		
		//visible what we need
//		TILES, SPRITES, EFFECTS, LIGHTS, PLATFORM, ENEMIES, OBJECTS, SCRIPTS, SLOPES, AI
	}
	
	private void hideAllSubMenus()
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
	
	public void setupTopMenu()
	{
		Table toplefttable = new Table();
		toplefttable.align(Align.topLeft);
		toplefttable.setFillParent(true);
		
		Table toprighttable = new Table();
		toprighttable.align(Align.topRight);
		toprighttable.setFillParent(true);
		
		ui_menuLeftButtons[0] = new TextButton("Save", skin);
		ui_menuLeftButtons[1] = new TextButton("Edit", skin);
		ui_menuLeftButtons[1].padRight(15);
		ui_menuLeftButtons[1].padLeft(3);
		
		ui_menuLeftButtons[0].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				//save
			}
		});
		
		ui_menuLeftButtons[1].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				ui_editMenuTable.setVisible(!ui_editMenuTable.isVisible());
			}
		});
		
		ui_mapList = new SelectBox<String>(skin);
		ui_mapList.setItems("dae_corridor", "dae_bleblablo", "someotherlongname");
		ui_mapList.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				new Dialog("Some Dialog", skin, "dialog") {
					protected void result (Object object) {
						if (Boolean.parseBoolean(""+object) == true)	 System.out.println("Loading: "+ui_mapList.getSelected());
					}
				}.text("Load "+ui_mapList.getSelected()+" ?").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(stage);
			}
		});
		
		
		// RIGHT SIDE
		ui_menuRightButtons[0] = new TextButton("Tiles", skin);
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
		
		stage.addActor(toplefttable);
		stage.addActor(toprighttable);
	}
	
	public void setupAiSubMenu()
	{
		ui_aiTable = new Table(skin);
		ui_aiTable.align(Align.top);
		ui_aiTable.setFillParent(true);
//		ui_aiTable.setHeight(300);
		ui_aiTable.padTop(100);
		TextButton proxy = new TextButton("AI SUB MENU", skin);
		ui_aiTable.add(proxy).top();
		
		stage.addActor(ui_aiTable);
		
		ui_aiTable.setVisible(false);
	}
	
	public void setupSlopesSubMenu()
	{
		ui_slopesTable = new Table(skin);
		ui_slopesTable.align(Align.top);
		ui_slopesTable.setFillParent(true);
//		ui_slopesTable.setHeight(300);
		ui_slopesTable.padTop(100);
		TextButton proxy = new TextButton("SLOPES SUB MENU", skin);
		ui_slopesTable.add(proxy).top();
		
		stage.addActor(ui_slopesTable);
		
		ui_slopesTable.setVisible(false);
	}
	
	public void setupTilesSubMenu()
	{
		ui_tilesTable = new Table(skin);
		ui_tilesTable.align(Align.top);
		ui_tilesTable.setFillParent(true);
//		ui_tilesTable.setHeight(300);
		ui_tilesTable.padTop(100);
		TextButton proxy = new TextButton("TILES SUB MENU", skin);
		ui_tilesTable.add(proxy).top();
		
		stage.addActor(ui_tilesTable);
		
		ui_tilesTable.setVisible(false);
	}
	
	public void setupSpritesSubMenu()
	{
		ui_spritesTable = new Table(skin);
		ui_spritesTable.align(Align.top);
		ui_spritesTable.setFillParent(true);
//		ui_spritesTable.setHeight(300);
		ui_spritesTable.padTop(100);
		TextButton proxy = new TextButton("SPRITES SUB MENU", skin);
		ui_spritesTable.add(proxy).top();
		
		stage.addActor(ui_spritesTable);
		
		ui_spritesTable.setVisible(false);
	}

	public void setupEffectsSubMenu()
	{
		ui_effectsTable = new Table(skin);
		ui_effectsTable.align(Align.top);
		ui_effectsTable.setFillParent(true);
//		ui_effectsTable.setHeight(300);
		ui_effectsTable.padTop(100);
		TextButton proxy = new TextButton("EFFECTS SUB MENU", skin);
		ui_effectsTable.add(proxy).top();
		
		stage.addActor(ui_effectsTable);
		
		ui_effectsTable.setVisible(false);
	}
	
	public void setupScriptSubMenu()
	{
		ui_scriptsTable = new Table();
		ui_scriptsTable.align(Align.top);
		ui_scriptsTable.setFillParent(true);
		ui_scriptsTable.padTop(100);
		
		List<String> scriptList = new List<String>(skin);
		scriptList.setItems("portal_left_top", "portal_left_bottom", "auto_alarm1", "auto_alarm2", "some_other_script", "enemy_spawn_bla");
		
		ui_scriptTextArea = new TextArea("Script stuff~", skin);
		ui_scriptsTable.add(ui_scriptTextArea).top().width(300).height(200);
		
		stage.addActor(ui_scriptsTable);
		
		ui_scriptsTable.setVisible(false);
	}
	
	public void setupObjectSubMenu()
	{
		ui_objectsTable = new Table(skin);
		ui_objectsTable.align(Align.top);
		ui_objectsTable.setFillParent(true);
		ui_objectsTable.padTop(100);
		TextButton proxy = new TextButton("OBJECTS SUB MENU", skin);
		ui_objectsTable.add(proxy).top();
		
		stage.addActor(ui_objectsTable);
		
		ui_objectsTable.setVisible(false);
	}
	
	public void setupNpcsSubMenu()
	{
		ui_npcsTable = new Table(skin);
		ui_npcsTable.align(Align.top);
		ui_npcsTable.setFillParent(true);
		ui_npcsTable.padTop(100);
		TextButton proxy = new TextButton("NPCS SUB MENU", skin);
		ui_npcsTable.add(proxy).top();
		
		stage.addActor(ui_npcsTable);
		
		ui_npcsTable.setVisible(false);
	}
	
	public void setupPlatformsSubMenu()
	{
		ui_platformsTable = new Table(skin);
		ui_platformsTable.align(Align.top);
		ui_platformsTable.setFillParent(true);
		ui_platformsTable.padTop(100);
		TextButton proxy = new TextButton("PLATFORMS SUB MENU", skin);
		ui_platformsTable.add(proxy).top();
		
		stage.addActor(ui_platformsTable);
		
		ui_platformsTable.setVisible(false);
	}
	
	public void setupLightsSubMenu()
	{
		ui_lightsTable = new Table(skin);
		ui_lightsTable.align(Align.top);
		ui_lightsTable.setFillParent(true);
		ui_lightsTable.padTop(100);
		TextButton proxy = new TextButton("LIGHTS SUB MENU", skin);
		ui_lightsTable.add(proxy).top();
		
		stage.addActor(ui_lightsTable);
		
		ui_lightsTable.setVisible(false);
	}
	
	public void setupLayerList()
	{
		ui_layerListTable = new Table();
		ui_layerListTable.align(Align.topRight);
		ui_layerListTable.setFillParent(true);
		ui_layerListTable.padRight(12);
		
		CheckBox[] chkBoxes = new CheckBox[5];
	
		Table checkTable = new Table();
		checkTable.padLeft(6);
		checkTable.padRight(6);
		checkTable.background(uiblack);
		
		Table layerButtonsTable = new Table();
		layerButtonsTable.align(Align.right);
		ui_parallaxProxy = new TextButton("Parallax Settings", skin);
		ui_layerOrderUp = new TextButton("UP", skin);
		ui_layerOrderDown = new TextButton("DOWN", skin);
		layerButtonsTable.add(ui_parallaxProxy).right().top().padRight(10).padBottom(60).row();
		layerButtonsTable.add(ui_layerOrderUp).right().bottom().padRight(10).width(90).row();
		layerButtonsTable.add(ui_layerOrderDown).right().bottom().padRight(10).width(90).row();
		
		ui_layerlist = new List<String>(skin);
		ui_layerlist.setItems("Parallax BG", "Direct BG", "Structure", "Idonteven", "Overlays");
		
		for (CheckBox box : chkBoxes)
		{
			box = new CheckBox("", skin);
			checkTable.add(box).padTop(3).padBottom(3).row();
		}
		
		ui_layerListTable.add(layerButtonsTable).padRight(10);
		ui_layerListTable.add(ui_layerlist);
		ui_layerListTable.add(checkTable);
		
		ui_layerListTable.padTop(60);
		
		stage.addActor(ui_layerListTable);
	}
	
	public void setupEditMenu()
	{
		ui_editMenuTable = new Table();
		ui_editMenuTable.defaults().width(320);
		ui_editMenuTable.setFillParent(false);
		ui_editMenuTable.setPosition(160+82, Gdx.graphics.getHeight()-280);
//		fileTable.padTop(menuLeftButtons[0].getHeight());
		
		ui_editMenuButtons[0] = new TextButton("Change Layer Size", skin);
		ui_editMenuButtons[1] = new TextButton("Change Layer Type", skin);
		ui_editMenuButtons[2] = new TextButton("Change Layer Name", skin);
		ui_editMenuButtons[3] = new TextButton("Change Map Size", skin);
		ui_editMenuButtons[4] = new TextButton("Change Map Display Name", skin);
		ui_editMenuButtons[5] = new TextButton("Toggle Scripts", skin);
		ui_editMenuButtons[6] = new TextButton("Toggle Objects", skin);
		ui_editMenuButtons[7] = new TextButton("Toggle Grid", skin);
		ui_editMenuButtons[8] = new TextButton("------------------------------", skin);
		ui_editMenuButtons[9] = new TextButton("Add Layer", skin);
		ui_editMenuButtons[10] = new TextButton("Remove Layer", skin);
		ui_editMenuButtons[11] = new TextButton("Fill Layer", skin);
		ui_editMenuButtons[12] = new TextButton("Empty Layer", skin);
		ui_editMenuButtons[13] = new TextButton("Empty Unseen Tiles", skin);
		ui_editMenuButtons[14] = new TextButton("Fill Tile Line", skin);
		ui_editMenuButtons[15] = new TextButton("Fill Tile Line", skin);
		
		ui_editMenuButtons[0].addListener(new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				ui_editMenuTable.setVisible(false);
				new Dialog("Some Dialog", skin, "dialog") {
					protected void result (Object object) {
//						System.out.println("Chosen: " + object);
					}
				}.text("New Map").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(stage);
			}
		});
		
		for (TextButton b : ui_editMenuButtons)
		{
			ui_editMenuTable.add(b).row();
		}
		
		stage.addActor(ui_editMenuTable);
		ui_editMenuTable.setVisible(false);
	}

	
	
	@Override
	public void render ()
	{
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.setColor(Color.BLACK);
		batch.begin();
		batch.draw(GFX.texWhite, 0, 0, Gdx.graphics.getWidth(), 70);
		batch.end();
		batch.setColor(Color.WHITE);
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}

	@Override
	public void resize (int width, int height)
	{
		
	}

	@Override
	public void dispose ()
	{
		stage.dispose();
		skin.dispose();
	}
	
}
