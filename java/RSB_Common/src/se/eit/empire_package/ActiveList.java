//
//Copyright (C) 2014 Henrik Björkman www.eit.se
//
//History:
//Created by Henrik 2014-03-08

package se.eit.empire_package;


import se.eit.rsb_package.*;
import se.eit.db_package.*;


public class ActiveList extends WorldBase {

	 //ArrayList<ActiveObject> activeObjects = new  ArrayList<ActiveObject>();
	DbList<ActiveObject> activeObjects = new  DbList<ActiveObject>();

	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireBase.class.getSimpleName();	
	}


	public ActiveList()
	{
		super();
	}
		
	public void addActiveObject(ActiveObject ao)
	{
		ao.activeIndex = activeObjects.add(ao);
	}

	public void removeActiveObject(ActiveObject ao)
	{
		activeObjects.remove(ao.activeIndex);
	}

	public void tickActiveObjects(long tickMs)
	{
		for (ActiveObject s : activeObjects)
		{
			s.gameTick(tickMs);
		}
	}
}