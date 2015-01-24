package se.eit.rsb_package;

public class Misc {

	//public static final float G = 6.67384F*Misc.exp(-11); // N(m/Kg)2
    // http://www.nyteknik.se/popular_teknik/teknikrevyn/article3766124.ece
	public static final float G = 6.67545E-11F; // N(m/Kg)2  Big G
	
	
	public static float square(float f)
	{
		return f*f;
	}

	public static float square(int i)
	{
		return i*i;
	}

	public static float cube(float f)
	{
		return f*f*f;
	}

	public static float cube(int i)
	{
		return i*i*i;
	}

	public static float exp(int i) {
		if (i>0)
		{
			return 10*exp(i-1);
		}
		else if (i<0)
		{
			return exp(i+1)/10;
		}
		return 1;
	}
	
}
