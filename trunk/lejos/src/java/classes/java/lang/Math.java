package java.lang;

/**
 * Mathematical functions.
 *
 * @author <a href="mailto:bbagnall@escape.ca">Brian Bagnall</a>
 */
public final class Math {

	final static double [] DIGIT = {45.0, 26.56505118, 14.03624347, 7.125016349, 3.576334375, 1.789910608, 0.89517371, 0.447614171, 0.2238105, 0.111905677, 0.055952892, 0.027976453, 0.013988227, 0.006994114, 0.003497057};
	
	// Math constants
	public static final double E = 2.718281828459045;
	public static final double PI = 3.141592653589793;

	// These constants are used for method trig()
	private static final int SIN = 0;
	private static final int COS = 1;
	private static final int TAN = 2;

	/**
	* Cosine function.
	*/
	public static double cos(double a) {
		return trig(a, COS);
	}

	/**
	* Power function.
	*/
	// will redo pow() properly later to handle doubles for b.
	public static double pow(double a, int b) {
		double c = 1.0;
		
		if(b > 0) {
			for(int i=0;i<b;i++) {
				c = c * a;
			}
		}
		else if(b < 0) {
			for(int i=0;i>b;i--) {
				c = c / a;
			}	
		}	
		
		return c;
	}

        /**
	 * Sine function.
	 */
	public static double sin(double a) {
		return trig(a, SIN);
	}

	/**
	 * Square root function.
	 */
	public static double sqrt(double a) {
		double b = a;
		double delta = a;
		double accuracy = 0.0000001; // Can't be smaller than this
		
		// Special situation if a < 0
		if(a<0)
			return 0; // ** Should return NaN
		
		// Special situation if a < 1
		if(a<1) {
			b=1;
			delta = 1;
		}
		
		// TBD: b*b should be assigned to a local.
		while(((b*b) > (a + accuracy))||((b*b)<(a-accuracy))) {
			delta = delta/2;
			if((b*b) > a)
				b = b - delta;
			else
				b = b + delta;
				
		        //josx.platform.rcx.LCD.showNumber ((int) (b * 100));
			//josx.platform.rcx.Button.VIEW.waitForPressAndRelease();
			//System.out.println("b = " + b);
		}

		return b;
	}

        /**
         * Tangent function.
	 */
	public static double tan(double a) {
		return trig(a, TAN);
	}
	
	private static double trig(double a, int returnType) {
	 	
   	// This method uses radians input, just like the official java.lang.Math
   	a = toDegrees (a);
   	
   	// ** When a=0, 90, 180, 270 should return even number probably
   	
   	// With positive numbers, subtracting 360 until angle is between 0-360
		while(a >= 360) {
			a = a - 360;
		}
		
		// With negative numbers, add 360 until between 0-360
   	while(a < 0) {
   		a = a + 360;	
   	}
   	   	
   	// Cos is negative in quadrants 2 and 3 (angle 90-270)
   	int cosMult = 1;
   	if((a<270)&&(a>90))
   		cosMult = -1;  		
   	
   	// Sin is negative in quadrants 3 and 4 (angle 180-360)
   	int sinMult = 1;
   	if((a<360)&&(a>180))
   		sinMult = -1;
   	
   	// Transform the starting angle to between 0-90
   	// Since the cordic method is only accurate for angles 0-90, must do
   	// this to handle angles 90-360
   	if(a>=180)
   		a = a - 180;
   		
   	if(a>90)
   		a = 180 - a;
   	
   	// ** The core trig calculations to produce Cos & Sin **
		int N = DIGIT.length - 1;
		double x = 0.607252935;  // Absolute best accuracy available
		double y = 0.0;
		
		for(int i = 0;i <= N;i++) {
			// ** Temp code:
			//System.out.println(i + "  " + "x = " + x + "  y = " + y + "  A = " + a + "  digit = " + digit[i]); 
			double dx = x / Math.pow(2, i);
    	double dy = y / Math.pow(2, i);
    	double da = DIGIT[i];
    	
    	if(a >= 0) {
    		x = x - dy;
    		a = a - da;
    		y = y + dx;
    	}
    	else {
    		x = x + dy;
    		a = a + da;
    		y = y - dx;
    	}
		}  // ** End of core trig calculations **
		
		// Now use multipliers (set at start of routine) to convert sin
		// and cos to +/- (depends on the quadrant):
		y = y * sinMult;
		x = x * cosMult;
		
		if(returnType == SIN)
			return y;
		else if (returnType == COS)
			return x;
		else
			return y/x;
	}
	
	/**
	 * Arc tangent function
	 */
	public static double atan(double a) {
  	
   	int N = DIGIT.length - 1;
		double x = 1.0;
		double y = a;
		double t = 0;
		
		for(int i = 0;i <= N;i++) {
			
			double dx = x / Math.pow(2, i);
    	double dy = y / Math.pow(2, i);
    	double da = DIGIT[i];
    	
    	if(y < 0) {
    		x = x - dy;
    		t = t - da;
    		y = y + dx;
    	}
    	else {
    		x = x + dy;
    		t = t + da;
    		y = y - dx;
    	}
		}
		
		return toRadians(t);
	}

        /**
         * Converts radians to degrees.
	 */
	public static double toDegrees(double angrad) {
		return (angrad * 360)/(2 * PI);
	}
	
	/**
	 * Converts degrees to radians.
	 */
	public static double toRadians(double angdeg) {
		return (2*PI*angdeg)/360;
	}
}
