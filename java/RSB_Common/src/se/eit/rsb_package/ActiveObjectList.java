// ActiveObjectList.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
//History:
//Created by Henrik 2014-03-08

package se.eit.rsb_package;


import se.eit.db_package.*;
import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;

// DbSubRoot objects register with DbTickList while objects within a database shall register with this.
// The reason is that ticks go to database first and then it will call the objects only if the game is running.

//TODO current implementation support only one (unspecified) interval. But some objects will perhaps need tick less than once per second or minute. 


public abstract class ActiveObjectList extends WorldBase {

	// Objects registered here will get a tick every game cycle (typically 50ms)
	DbList<ActiveObjectInterface> activeObjects = new  DbList<ActiveObjectInterface>();

	// One of the objects registered here will get a tick at a game cycle.
	// Perhaps we don't really need a separate list for this, we could just as well do maintenance tick to all ID objects.
	DbList<ActiveObjectInterface> slowTickObjects = new  DbList<ActiveObjectInterface>();
	int slowTickCount=0;
	
	int maintenanceCount=0;

	long tickTimeMs=0;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ActiveObjectList.class.getSimpleName();	
	}


	public ActiveObjectList()
	{
		super();
	}
	
	// deserialize from wr
	// sub classes with additional member variables that need 
	// to be saved to disk shall implement this method.
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		
		maintenanceCount = wr.readInt();
	}

	
	// serialize to ww
	// sub classes with additional member variables that need 
	// to be saved to disk shall implement this method.
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		
		ww.writeInt(maintenanceCount);
	}

	
	
	public int addActiveObject(ActiveObjectInterface ao)
	{
		int i = activeObjects.add(ao);
		return i;
	}

	// The activeObjectIndex to use is the one returned from addActiveObject
	// This shall not be called during a activeObjectTick callback.
	// It shall instead be removed during a maintenanceObjectTick or some other triggering event.
	public void removeActiveObject(int activeObjectIndex)
	{
		activeObjects.remove(activeObjectIndex);
	}

	// Returns an index for the maintenance object, this must be remembered by caller, to be used when calling removeMaintenanceObject.	
	// We don't really need a separate list for this, we could just as well do maintenance tick to all ID objects. If so this method is no longer needed.
	public int addSlowTickObject(ActiveObjectInterface ao)
	{
		int i = slowTickObjects.add(ao);
		return i;
	}
	
	// The activeObjectIndex to use is the one returned from addActiveObject
	// Unlike removeActiveObject, it is OK to call this during a maintenanceObjectTick callback.
	// We don't really need a separate list for this, we could just as well do maintenance tick to all ID objects. If so this method is no longer needed.
	public void removeSlowTickObject(int maintenanceObjectIndex)
	{
		slowTickObjects.remove(maintenanceObjectIndex);
	}
	
	// To be called from the tickMsCallback
	public void tickAllActiveObjects(long tickMs)
	{
		// It is possible to do this like this:
		/*
		for (ActiveObjectInterface s : activeObjects)
		{
			s.activeObjectTick(tickMs);
		}
		*/
		// But this is faster (unless the implementation of DbList is changed somehow)
		final int n=activeObjects.getCapacity();
		for (int i = 0; i<n; ++i)
		{
			ActiveObjectInterface s = activeObjects.get(i);
			if (s!=null)
			{
				// TODO the argument to activeObjectTick shall be delta time not accumulated time.				
				s.activeObjectTick(tickMs);
			}
		}

		
		int ns = slowTickObjects.getCapacity()+19/20;
		for (int i=0;i<ns;i++)
		{
			if (slowTickCount>=slowTickObjects.getCapacity())
			{
				slowTickCount=0;
				
				if (maintenanceCount>=dbListId.getCapacity())
				{
					maintenanceCount=0;
				}
				DbIdObj dio = dbListId.get(maintenanceCount);
				if ((dio!=null) && (dio instanceof ActiveObjectInterface))
				{
					ActiveObjectInterface moi=(ActiveObjectInterface)dio;
					moi.maintenanceObjectTick(tickMs);
				}
				++maintenanceCount;

			}
			else
			{
				ActiveObjectInterface s = slowTickObjects.get(slowTickCount);
				if (s!=null)
				{
					// TODO the argument to activeObjectSlowTick & luaCallSlowTick shall be delta time not accumulated time.								
					s.activeObjectSlowTick(tickMs);
					s.luaCallSlowTick(tickMs);
				}
				++slowTickCount;
			}
		}

		
	}
	
	// This is called by DbTickList.
	@Override
	public void tickMsCallback(long tickTimeMs)
	{
		// This is just to calculate how much time has passed since previous tick
		final long deltaTimeMs = tickTimeMs - this.tickTimeMs;
		this.tickTimeMs += deltaTimeMs;

		// Do this only if at least one client is connected.
		if (notificationDataList.size()!=0) 
		{
			this.lockWrite();
			try
			{
				final long newTime = addGameTime(deltaTimeMs);
			
				tickAllActiveObjects(newTime);
			}
			finally
			{
				this.unlockWrite();
			}
		}
	}

	@Override
	public void tickMsCallbackMaintenance(long tickTimeMs)
	{
		int n=notificationDataList.size();
		// Only save if no player is connected
		if (n==0) 
		{
			saveSelf();
		}
		else
		{
			debug("There are " + n + "  player(s) connected to world "+ this.getNameAndPath("."));
		}
	}

}