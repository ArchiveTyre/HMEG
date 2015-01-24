// EmpireWorld.java
//
// Copyright (C) 2013 Henrik Björkman www.eit.se
//
// History:
// Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;



//import se.eit.rsb_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;


public class EmpireWorld extends ActiveList {
	
    final static int INFINITE_MOVE_COST=0x7FFFFFFF;
    final static int N_SECTOR_NEIGHBORS_1=6;
    final static int N_SECTOR_NEIGHBORS_2=18;
    final static int N_SECTOR_NEIGHBORS_3=36;
    final static int SAME_SECTOR=-1;
    final static int maxNations = 64;
    final static int ETERNITY = 0x7FFFFFFF;
    
    public Random generator = new Random();
	long gameTimeMsCounter;
	long tickTimeMs=0;

	int tickState;
	public int gameSpeed=0; // Game starts paused, the player who created the game must issue a "go" command.
	
	//public int gameTime=0; // This is part of WorldBase
	
	//Queue<EmpireBase> timeQueue = new LinkedList<EmpireBase>();
	Queue<EmpireBase> performOrdersQueue = new LinkedList<EmpireBase>();
	Queue<EmpireBase> interactQueue = new LinkedList<EmpireBase>();
	Queue<EmpireBase> cleanupQueue = new LinkedList<EmpireBase>();

	EmpireStatesList empireStatesList;
	EmpireTerrain empireTerrain;
	EmpireUnitTypeList empireUnitTypeList;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireWorld.class.getSimpleName();	
	}


	public EmpireWorld(DbBase parent, String name, String createdBy) 
	{
		super();
		parent.addObject(this);
		this.setName(name);
		this.createdBy=createdBy;

		generateWorld();
	}

	public EmpireWorld()
	{	
		super();
	}

	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		gameTimeMsCounter=wr.readLong();
		tickState=wr.readInt();
		gameSpeed=wr.readInt();
		//gameTime=wr.readInt();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeLong(gameTimeMsCounter);
		ww.writeInt(tickState);
		ww.writeInt(gameSpeed);
		//ww.writeInt(gameTime);
	}
	
	@Override
	public void listInfo(WordWriter ww, String prefix)
	{
		super.listInfo(ww, prefix);					
		ww.println(prefix+"gameTimeMsCounter "+gameTimeMsCounter);		
		ww.println(prefix+"tickState "+tickState);
		ww.println(prefix+"gameSpeed "+gameSpeed);
		//ww.println(prefix+"gameTime "+gameTime);
	}
	
	// returns 1 if info to set was found, -1 if not found.
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("gameSpeed"))
		{
			gameSpeed=wr.readInt();
			return 1;
		}
		return super.setInfo(wr, infoName);
	}
	
	public void generateWorld()
	{
		// TODO: Should keep a reference to all objects created here so that getEmpireUnitTypeList, getEmpireTerrain and getEmpireNationsList dont need to search for these.
        try
        {
        	this.lockWrite();
        	empireUnitTypeList = new EmpireUnitTypeList(this, "TypesList");

        	empireStatesList = new EmpireStatesList(this, "StatesList");
	
		    empireTerrain = new EmpireTerrain(this, "Terrain");
			
        }
        finally
        {
        	this.unlockWrite();
        }
	}
	
	public EmpireUnitTypeList getEmpireUnitTypeList()
	{
		if (empireUnitTypeList==null)
		{	
			if (listOfStoredObjects!=null)
			{		
				for (DbStorable s : this.listOfStoredObjects)
				{
					if (s instanceof EmpireUnitTypeList)
					{
						empireUnitTypeList=(EmpireUnitTypeList)s;
					}
				}
			}
		}
		
		return empireUnitTypeList;
	}

	
	public EmpireTerrain getEmpireTerrain()
	{
		if (empireTerrain==null)
		{	
			if (listOfStoredObjects!=null)
			{		
				for (DbStorable s : this.listOfStoredObjects)
				{
					if (s instanceof EmpireTerrain)
					{
						empireTerrain=(EmpireTerrain)s;
					}
				}
			}
		}
		
		return empireTerrain;
	}
	
	
	public EmpireStatesList getEmpireNationsList()
	{
		if (empireStatesList==null)
		{	
			if (listOfStoredObjects!=null)
			{		
				for (DbStorable s : this.listOfStoredObjects)
				{
					if (s instanceof EmpireStatesList)
					{
						empireStatesList=(EmpireStatesList)s;
					}
				}
			}
		}
		
		return empireStatesList;
	}

	/*
	protected void doEconomyAndMovement()
	{
		if (timeQueue.isEmpty())
		{
			// time queue is empty, perhaps units have not yet registered for tick, this gives them a chance to to so.
			final int n=this.getDbIdListLength();
			// Loop over all ID objects in the database for this game
			for (int i=0; i<n; i++)
			{
				final DbIdObj io=this.getDbIdObj(i);
				if (io instanceof EmpireBase)
				{
					EmpireBase eb = (EmpireBase)io;
					eb.gameTick(gameTime);
				}
			}			
		}
		else
		{
			// Hopefully we only need to do tick on the units in our queue, this will save CPU time.
			Queue<EmpireBase> tmpTimeQueue = timeQueue;
			timeQueue=new LinkedList<EmpireBase>(); // new queue since units will re register for next tick during this tick (don't want eternal loop here)

	        while (!tmpTimeQueue.isEmpty()) {
	        	EmpireBase eb = tmpTimeQueue.remove();
	        	eb.gameTick(gameTime);
	        }
		}
			
	}
	*/
	
	protected void doOrders()
	{
        while (!performOrdersQueue.isEmpty()) {
        	EmpireBase eb = performOrdersQueue.remove();
			eb.gameTickPerformOrders();
        }
	}
	
	protected void doInteract()
	{
		/*
		final int n=this.getDbIdListLength();
		
		for (int i=0; i<n; i++)
		{
			final DbIdObj io=this.getDbIdObj(i);
			if (io instanceof EmpireBase)
			{
				EmpireBase eb = (EmpireBase)io;
				eb.gameTickInteract();
			}
		}	
		*/
        while (!interactQueue.isEmpty()) {
        	EmpireBase eb = interactQueue.remove();
			eb.gameTickInteract();
        }
	}
	
	protected void doCleanup()
	{
		/*
		final int n=this.getDbIdListLength();
		
		for (int i=0; i<n; i++)
		{
			final DbIdObj io=this.getDbIdObj(i);
			if (io instanceof EmpireBase)
			{
				EmpireBase eb = (EmpireBase)io;
				eb.gameTickCleanup();
			}
		}	
		*/
        while (!cleanupQueue.isEmpty()) {
        	EmpireBase eb = cleanupQueue.remove();
			eb.gameTickCleanup();
        }
	}

	public void tickGameMs(int deltaMs)
	{
		//super.tickMs(deltaMs);
		if (gameSpeed!=0)
		{
			gameTimeMsCounter+=deltaMs;
			if (gameTimeMsCounter>=gameSpeed)
			{
				gameTime++;
				gameTimeMsCounter-=gameSpeed;
						
				// The following comment is perhaps out dated: Since we now allow objects to move between rooms and that happens during tick we need to call tick using DbIdList instead of sub objects. Otherwise iterating the world got messed up when objects where moved as they may do during a tick.
				
				
				lockWrite(); // TODO: Db must be locked during tick. But perhaps we should make sure all tick go via DbThreadSafe class by having a tickMsThreadSafe method there.
				try
				{
					//final int n=this.getDbIdListLength();
	
					switch(tickState)
					{
						case 0:
						{
							tickActiveObjects(gameTime);
							//doEconomyAndMovement();
							doOrders();
							doInteract();
							doCleanup();
							tickState++;
							break;
						}
						default:
						{
							tickActiveObjects(gameTime);
							//doEconomyAndMovement();
							doOrders();
							doInteract();
							doCleanup();
							tickState=0;
							break;
						}
					}
				}
				finally
				{
					unlockWrite();
				}		
			}
		}
	}

	
	public void setGameSpeed(int gameSpeed)
	{
		this.gameSpeed=gameSpeed;
		this.setUpdateCounter(); // TODO: This is in the root object for the game, it might not work with update counter since its unclear what ID to use. Shall it use ID it would have in the database it is placed or shall it have a fixed value of say 0? Or shall we avoid putting anything that changes in the root object?

		/*final EmpireNationsList enl = this.getEmpireNationsList();

		enl.postMessage("game speed "+gameSpeed);*/

	}
	
	
	// deprecated (it does nothing)
	/*
	public void setTickTimeCallback(EmpireBase eb)
	{
		//timeQueue.add(eb);
	}
	*/
	
	public void setTickPerformOrdersQueueCallback(EmpireBase eb)
	{
		performOrdersQueue.add(eb);
	}
	
	
	public void setTickInteractCallback(EmpireBase eb)
	{
		interactQueue.add(eb);
	}
	
	public void setTickCleanupCallback(EmpireBase eb)
	{
		cleanupQueue.add(eb);
	}

		
	@Override
	public void tickMsCallback(long tickTimeMs)
	{
		final long deltaTimeMs = tickTimeMs - this.tickTimeMs;
		this.tickTimeMs += deltaTimeMs;
		tickGameMs((int)deltaTimeMs);		
	}

}