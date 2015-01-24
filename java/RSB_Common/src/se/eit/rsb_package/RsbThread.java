package se.eit.rsb_package;

public class RsbThread extends Thread {

	
    private float spinDelta = 1f;	
    private float angle = 0f;
	

	public synchronized void setSpeed(float spin)
	{		
		this.spinDelta = spin;
	}
	
	public synchronized float getSpeed()
	{		
		return this.spinDelta;
	}
	
	public synchronized float getAngle()
	{		
		return this.angle;
	}

	public void run() 
	{
		for(;;)
		{
	        angle = angle + spinDelta;
	        if (angle >= 360f) angle = angle - 360;
			
			myWait();
		}
		
		
	}
	
	public synchronized void myWait()
	{
		try {
			this.wait(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		  		  
	}
	
}
