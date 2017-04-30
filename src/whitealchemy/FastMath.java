package whitealchemy;

/**
 * 
 * @author "Riven" & numerous people from java-gaming.org
 *
 */
public class FastMath
{
   private static final int    BIG_ENOUGH_INT   = 16 * 1024;
   private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT + 0.0000;
   private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5000;
   private static final double BIG_ENOUGH_CEIL  = BIG_ENOUGH_INT + 0.9999;
   @SuppressWarnings("unused")
   private static final float   DEG,RAD;
   private static final int     SIN_BITS,SIN_MASK,SIN_COUNT;
   private static final float   radFull,radToIndex;
   private static final float   degFull,degToIndex;
   private static final float[] sin, cos;
   
   private static final int     ATAN2_BITS        = 7;

   private static final int     ATAN2_BITS2       = ATAN2_BITS << 1;
   private static final int     ATAN2_MASK        = ~(-1 << ATAN2_BITS2);
   private static final int     ATAN2_COUNT       = ATAN2_MASK + 1;
   private static final int     ATAN2_DIM         = (int) Math.sqrt(ATAN2_COUNT);

   private static final float   ATAN2_DIM_MINUS_1 = (ATAN2_DIM - 1);

   private static final float[] atan2             = new float[ATAN2_COUNT];
   
   static
   {
      RAD = (float) Math.PI / 180.0f;
      DEG = 180.0f / (float) Math.PI;

      SIN_BITS  = 12;
      SIN_MASK  = ~(-1 << SIN_BITS);
      SIN_COUNT = SIN_MASK + 1;

      radFull    = (float) (Math.PI * 2.0);
      degFull    = (float) (360.0);
      radToIndex = SIN_COUNT / radFull;
      degToIndex = SIN_COUNT / degFull;

      sin = new float[SIN_COUNT];
      cos = new float[SIN_COUNT];

      for (int i = 0; i < SIN_COUNT; i++)
      {
         sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
         cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
      }
   }
   
   static
   {
      for (int i = 0; i < ATAN2_DIM; i++)
      {
         for (int j = 0; j < ATAN2_DIM; j++)
         {
            float x0 = (float) i / ATAN2_DIM;
            float y0 = (float) j / ATAN2_DIM;

            atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
         }
      }
   }

   public static int fastFloor(float x) {
      return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
   }

   public static int fastRound(float x) {
      return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
   }

   public static int fastCeil(float x) {
      return (int) (x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
   }
   
   public static final float sin(float rad)
   {
      return sin[(int) (rad * radToIndex) & SIN_MASK];
   }

   public static final float cos(float rad)
   {
      return cos[(int) (rad * radToIndex) & SIN_MASK];
   }

   public static final float sinDeg(float deg)
   {
      return sin[(int) (deg * degToIndex) & SIN_MASK];
   }

   public static final float cosDeg(float deg)
   {
      return cos[(int) (deg * degToIndex) & SIN_MASK];
   }

   public static final float atan2Deg(float y, float x)
   {
      return FastMath.atan2(y, x) * DEG;
   }

   public static final float atan2DegStrict(float y, float x)
   {
      return (float) Math.atan2(y, x) * DEG;
   }

   public static final float atan2(float y, float x)
   {
      float add, mul;

      if (x < 0.0f)
      {
         if (y < 0.0f)
         {
            x = -x;
            y = -y;

            mul = 1.0f;
         }
         else
         {
            x = -x;
            mul = -1.0f;
         }

         add = -3.141592653f;
      }
      else
      {
         if (y < 0.0f)
         {
            y = -y;
            mul = -1.0f;
         }
         else
         {
            mul = 1.0f;
         }

         add = 0.0f;
      }

      float invDiv = ATAN2_DIM_MINUS_1 / ((x < y) ? y : x);

      int xi = (int) (x * invDiv);
      int yi = (int) (y * invDiv);

      return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
   }
   
   public static final float atan2_SPASI(float y, float x)
   {
	   float add, mul;

	      if (x < 0.0f)
	      {
	         if (y < 0.0f)
	         {
	            x = -x;
	            y = -y;

	            mul = 1.0f;
	         }
	         else
	         {
	            x = -x;
	            mul = -1.0f;
	         }

	         add = -3.141592653f;
	      }
	      else
	      {
	         if (y < 0.0f)
	         {
	            y = -y;
	            mul = -1.0f;
	         }
	         else
	         {
	            mul = 1.0f;
	         }

	         add = 0.0f;
	      }
	   
	   int i; 
	   if ( x < y ) 
	       i = (int)ATAN2_DIM_MINUS_1 * ATAN2_DIM + (int)(x * (ATAN2_DIM_MINUS_1 / y)); 
	   else
	       i = (int)(y * (ATAN2_DIM_MINUS_1 / x)) * ATAN2_DIM + (int)ATAN2_DIM_MINUS_1; 
	     
	   return (atan2[i] + add) * mul;
   }
   
}
