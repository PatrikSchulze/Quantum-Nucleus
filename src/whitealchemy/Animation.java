package whitealchemy;

import com.badlogic.gdx.graphics.g2d.TextureRegion;



/**
 * 
 * @author Patrik Schulze
 *
 */
public class Animation
{	
	public static final boolean LOOP = true;
	public static final boolean ONCE = false;
	
    private int delaycounter; // delaycounter ++ bis zum speed, dann next frame
    private int speed; //  1-20, 1 = fast
    private int curFrameIndex; // temporary frame
    private TextureRegion frames[];
    private boolean loop;
    private boolean finish; // if loop = false & animation runned once, = true

    public Animation(TextureRegion[] regions, int in_speed, boolean loopin)
    {
        curFrameIndex 		 = 0;
        delaycounter     = 0;
        speed		 = in_speed;
        frames 		 = regions;
        loop 		 = loopin;
        finish 		 = false;
    }

    public int getWidth() { return frames[curFrameIndex].getRegionWidth(); }
    public int getHeight() {return frames[curFrameIndex].getRegionHeight();}
    public boolean isFinish() { return finish; }
    public int getFrameIndex() {     return curFrameIndex;  }

    public void resetToBeginning()
    {
            curFrameIndex = 0;
            finish = false;
    }

    public void skipToTheEnd()
    {
        if (!loop)
        {
            while (!finish)
            {
                update();
            }
        }
    }
	
    public void update()
    {
        delaycounter++;
        if (delaycounter == speed)
        {
            delaycounter = 0;
            if ((curFrameIndex+1) < frames.length)
            {
                curFrameIndex++;
            }
            else
            {
                if (loop)
                {
                    curFrameIndex = 0;
                }
                else
                {
                    if (finish == false) { finish = true;}
                }
            }
        }
    }
	
    public TextureRegion getFrame()
    {
    	return frames[curFrameIndex];
    }
    
    public TextureRegion getFrameManually(int num)
    {
    	return frames[num];
    }
    public boolean isLoop() { return loop; }
    public int getSpeed() { return speed; }
    public int getFrameCount() { return frames.length; }

}