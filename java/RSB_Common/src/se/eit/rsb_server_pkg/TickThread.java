package se.eit.rsb_server_pkg;

import se.eit.db_package.*;
import se.eit.web_package.*;



public class TickThread implements Runnable
{
	static final int desired_frame_rate = 100; // in ms

	public int timeAcceleration=1; // 0 will pause all games
	
	long sim_frame_time;

	DbSuperRoot db=null;

	boolean done=false;

	long sim_tick_time=0; 
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return DbBase.class.getSimpleName();	
	}
	
	public TickThread(DbSuperRoot db) 
	{
		this.db=db;
	}
	
	public void debug(String str)
	{
		WordWriter.safeDebug(className()+": "+str);
	}

	public void error(String str)
	{
		WordWriter.safeError(className()+": "+str);
		System.exit(1);
	}
	
	
	public void setDone()
	{
		done=true;
	}
	
	public void run() 
	{
		while(!done) 
		{
			final long cur_time=System.currentTimeMillis();
			final long time_to_wait=sim_frame_time - cur_time;
			
			if (time_to_wait>desired_frame_rate*2)			
			{
				error("frame rate in future");
				myWait((int)desired_frame_rate);
				sim_frame_time = cur_time + desired_frame_rate;
			}
			else if (time_to_wait>0)
			{
				// The normal case
				myWait((int)time_to_wait);				   
				sim_frame_time += desired_frame_rate;
			}
			else if (time_to_wait>-desired_frame_rate)
			{
				// slightly behind, skip wait but update sim_frame_time normally
				//cc.myWait(1);
				sim_frame_time += desired_frame_rate;			
			}
			else
			{
				// Far behind, skip sim_frame_time forward
				debug("frame rate behind");
				//cc.myWait(1);
				sim_frame_time = cur_time + desired_frame_rate/2;
			}

			//debug("tick "+cur_time+" "+sim_frame_time+" "+System.currentTimeMillis());
			//db.tickRecursiveMs(desired_frame_rate*timeAcceleration);
			sim_tick_time += desired_frame_rate*timeAcceleration;
			db.tickMsSuper(sim_tick_time);
			
		}
	}
	
	public synchronized void myWait(int time_ms)
	{
		try 
		{
			this.wait(time_ms);
		}  
		catch (InterruptedException e) {;}
	}
	
	
	
}