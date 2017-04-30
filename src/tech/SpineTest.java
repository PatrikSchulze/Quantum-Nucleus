package tech;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;

public class SpineTest extends ApplicationAdapter {
	SpriteBatch batch;
	
	SkeletonRenderer renderer;
	SkeletonRendererDebug debugRenderer;

	SkeletonData skeletonData;
	Skeleton skeleton;
	Animation walkAnimation;
	Animation jumpAnimation;
	OrthographicCamera cam;
	AnimationState state;
	
	private class MyInputs extends InputAdapter
	{
		@Override public boolean keyDown(int keycode)
		{
			if (keycode == Keys.LEFT)
			{
			}
			else if (keycode == Keys.UP)
			{
				state.setAnimation(0, "jump", false);
				state.addAnimation(0, "run", true, 0);
			}
			else if (keycode == Keys.RIGHT)
			{
				state.setAnimation(0, "run", true);
			}
			else if (keycode == Keys.DOWN)
			{
				
			}
			
			return super.keyDown(keycode);
		}
	}

	public void create () {
		Gdx.graphics.setDisplayMode(1280,  720,  false);
		batch = new SpriteBatch();
		renderer = new SkeletonRenderer();
		renderer.setPremultipliedAlpha(true);
		debugRenderer = new SkeletonRendererDebug();
		Gdx.input.setInputProcessor(new MyInputs());
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.setToOrtho(true);

		final String name = "content/spine/spineboy";

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(name + ".atlas"), false);

		SkeletonBinary binary = new SkeletonBinary(atlas);
		binary.setScale(0.6f);
		skeletonData = binary.readSkeletonData(Gdx.files.internal(name + ".skel"));
		
//		SkeletonJson json = new SkeletonJson(atlas);
//		json.setScale(0.6f);
//		skeletonData = json.readSkeletonData(Gdx.files.internal(name + ".json"));
		
		walkAnimation = skeletonData.findAnimation("walk");
		jumpAnimation = skeletonData.findAnimation("jump");

		skeleton = new Skeleton(skeletonData);
		skeleton.setFlipY(true);
		skeleton.updateWorldTransform();
		skeleton.setPosition(300, 500);
		
		AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.
		stateData.setMix("run", "jump", 0.2f);
		stateData.setMix("jump", "run", 0.2f);

		state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
		state.setTimeScale(0.5f); // Slow all animations down to 50% speed.
//		state.setAnimation(0, "run", true);
//		state.addAnimation(0, "jump", false, 2); // Jump after 2 seconds.
//		state.addAnimation(0, "run", true, 0); // Run after the jump.
	}

	public void render ()
	{
		state.update(0.016666f); // Update the animation time.
		state.apply(skeleton);
		skeleton.updateWorldTransform();
		
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		cam.update();

		batch.setProjectionMatrix(cam.combined);
		batch.begin();
			renderer.draw(batch, skeleton);
		batch.end();

//		debugRenderer.draw(skeleton);
	}

//	public void resize (int width, int height) {
//		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
//		debugRenderer.getShapeRenderer().setProjectionMatrix(batch.getProjectionMatrix());
//	}

	public static void main (String[] args) throws Exception {
		new LwjglApplication(new SpineTest());
	}
}