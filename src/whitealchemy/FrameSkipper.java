package whitealchemy;


/**
 * FrameSkipper.
 * Initialize with FPS value as parameter.
 * call FrameSkipper.sync() instead of Thread.yield()
 * in situation of no Vsync, ergo window mode
 *
 * if this class proves to work fine. use this all the time
 * because Vsync does not mean 60 FPS by default.
 * For example, A 100 Hz CRT withh sync at 100FPS.
 *
 * @author Patrik Schulze, Artificial Zero Media
 */
public class FrameSkipper
{
    private int target_fps;
    private long timeThen;
    private boolean forceSleep = false;

//    public FrameSkipper(int targetFrameRate)
//    {
//        target_fps = targetFrameRate;
//        timeThen = (Sys.getTime() * 1000000000L) / Sys.getTimerResolution();
//        if (Runtime.getRuntime().availableProcessors() < 2)
//        {
//        	forceSleep = true;
//        	System.out.println("Single Core CPU: Switching to Thread.sleep()");
//        }
//    }
//
//    public void sync()
//    {
//        long gapTo = 1000000000L / target_fps + timeThen;
//        long timeNow = (Sys.getTime() * 1000000000L) / Sys.getTimerResolution();
//
//        while (gapTo > timeNow)
//        {
//        	if (!forceSleep)	Thread.yield();
//        	else				try {Thread.sleep(0);}catch(InterruptedException e){}
//            timeNow = (Sys.getTime() * 1000000000L) / Sys.getTimerResolution();
//        }
//
//        timeThen = timeNow;
//                
//    }
    
    public int getTarget_fps()
    {
    	return target_fps;
    }
    
    public FrameSkipper(int targetFrameRate)
    {
        target_fps = targetFrameRate;
        timeThen = System.nanoTime();
//        if (Runtime.getRuntime().availableProcessors() < 2)
//        {
//        	forceSleep = true;
//        	System.out.println("Single Core CPU: Switching to Thread.sleep()");
//        }
    }

    public void sync()
    {
        long gapTo = 1000000000L / target_fps + timeThen;
        long timeNow = System.nanoTime();

        while (gapTo > timeNow)
        {
//        	if (!forceSleep)	Thread.yield();
//        	else				try {Thread.sleep(0);}catch(InterruptedException e){}
        	try {Thread.sleep(0);}catch(InterruptedException e){}
            timeNow = System.nanoTime();
        }

        timeThen = timeNow;
                
    }

    /*
    public static void main(String args[])
    {
            System.out.println("Simple Timer Test: " +
                    "set to 2 (should actually be 60 for most games)");

            FrameSkipper frameSkip = new BasicTimer(2);

            while (true)
            {
                    frameSkip.sync();
                    System.out.println("TICK");
            }
    }*/
    
}
