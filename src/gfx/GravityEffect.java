package gfx;

import game.Colors;
import game.Content;
import game.QNGame;
import whitealchemy.Trigonometrics;
import whitealchemy.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GravityEffect implements QNEffect
{
	float gss = 0.1f;
	float gsr = 1f;
	float dis, smf;
	float gravCX, gravCY;
	float dx, dy;
	float angle, orbit;
	float postAlpha = 1f;
	Sound sound;
	Sprite gravitySprite;
	ParticleEffect[] pe;
	Vector2 pull = new Vector2(0,0);
	Vector2 pos = new Vector2(0,0);
	Vector2 pp = new Vector2(0,0);
	
	float power = 1f;
	int time = 0;
	
	public boolean killMe = false;
	
	public GravityEffect(float x, float y, int totalArms, float _power)
	{
		pe = new ParticleEffect[totalArms];
		for (int i = 0; i< pe.length ;i++)
		{
			pe[i] = new ParticleEffect();
			pe[i].load(Gdx.files.internal("content/particle/gra_blue3.p"), Gdx.files.internal("content/particle"));
		}
		
		
		gravitySprite = new Sprite(Content.texGravity);
		gravitySprite.setPosition(x  - (gravitySprite.getWidth()*0.5f), y - (gravitySprite.getHeight()*0.5f));
		
		power = _power;
		
		sound = Content.sndBlackHole2;
		
		//TODO
		//play sound ?
		sound.play();
	}
	
	@Override
	public void setPosition(float _x , float _y)
	{
		gravitySprite.setPosition(_x,  _y);
	}
	
	@Override
	public void update(QNGame game) //TODO list of all entities, everything that can be pulled
	{
		if (gss < 1.75f) gss+=0.002f;
		else if (gss > 1.75f) gss=1.75f;
		gsr+=3f*(1.0f*(1.75f/gss));
		if (gsr >= 360f) gsr-=360f;
		
		if (gss >= 1.75f) time++;
		
		int r = Util.getRandom(0, 27);
		if (r <= 3)
		{
			gravitySprite.setColor(Colors.gravityBlue2);
		}
		else gravitySprite.setColor(Colors.gravityBlue1);
		
		if (time <= 0) gravitySprite.setScale(gss);
		if (time <= 0) gravitySprite.setAlpha(0.94f*Util.getSmallFloatPercentageValue(1.75f, gss));
		gravitySprite.setRotation(gsr);
		
		gravCX = (gravitySprite.getX()+(gravitySprite.getWidth()/2f));
		gravCY = (gravitySprite.getY()+(gravitySprite.getHeight()/2f));
		
		for (int i = 0;i<pe.length;i++)
		{
			for (ParticleEmitter em : pe[i].getEmitters())
			{
				if (time > 2*60)
				{
					postAlpha-=0.001f;
					em.getTransparency().setLow(postAlpha);
					em.getTransparency().setHigh(postAlpha);
					gravitySprite.setAlpha(postAlpha);
					gravitySprite.setScale(1.75f*postAlpha);
				}
				
				if (postAlpha <= 0f)
				{
					postAlpha = 0f;
					killMe = true;
				}
				
				dx = em.getX();
		    	dy = em.getY();
		    	dx = dx - gravCX;
				dy = dy - gravCY;
				angle = MathUtils.radiansToDegrees*MathUtils.atan2(dy, dx);
				angle = angle-180;
				
				em.getAngle().setHigh(angle);
				
				orbit = 120*postAlpha;
//				orbit = 200*postAlpha;
				pp.set(Trigonometrics.getOrbitLocationDeg(gravCX, gravCY, gsr+((360f/pe.length)*i), orbit));
				pe[i].setPosition(pp.x, pp.y);
				pe[i].update(0.01666f);
			}
		}
		
		dis = Trigonometrics.getDistanceFast(gravCX, gravCY, game.myNova.centerX(), game.myNova.centerY());
		if (dis < 400)
		{
			smf = Util.getPercentage(dis, 400)/1000.0f;
			smf-=0.1f;
			if (smf > 1f) smf = 1f;
			if (smf < 0f) smf = 0f;
			pull.set(gravCX - game.myNova.centerX(), gravCY - game.myNova.centerY());
			pull.scl(smf*0.12f);
			pos = game.myNova.getPosition();
			pos.add(pull);
			game.myNova.setPosition(pos);
		}
	}
	
	@Override
	public void drawBackPart(SpriteBatch batch)
	{
		for (int i = 0;i<pe.length;i++)
			pe[i].draw(batch);	
	}
	
	@Override
	public void drawFrontPart(SpriteBatch batch)
	{
		gravitySprite.draw(batch);	
	}

	@Override
	public boolean isKillMe()
	{
		return killMe;
	}

	@Override
	public float getX() {
		return gravitySprite.getX();
	}

	@Override
	public float getY() {
		return gravitySprite.getY();
	}
	
	
	
}
