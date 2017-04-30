package gfx;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import game.QNGame;

public interface QNEffect
{
	public void update(QNGame game);
	public void drawBackPart(SpriteBatch batch);
	public void drawFrontPart(SpriteBatch batch);
	public boolean isKillMe();
	public void setPosition(float _x, float _y);
	public float getX();
	public float getY();
}
