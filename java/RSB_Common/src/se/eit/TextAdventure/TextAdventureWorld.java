//World.java
//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.TextAdventure;
import se.eit.db_package.*;

import se.eit.rsb_package.*;

//import se.eit.rsb_package.*;
import se.eit.web_package.*;




public class TextAdventureWorld extends WorldBase {
	
	
	protected int turn=0;

	//StringFifo stringFifo;	
	
	protected long tickTimeMs = 0;
	
	RoundBuffer roundBuffer=new RoundBuffer(32);
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return TextAdventureWorld.class.getSimpleName();	
	}


	public TextAdventureWorld(DbBase parent, String name, String createdBy) 
	{
		super(parent);
		setName(name);
		setCreatedBy(createdBy);
		
		
		// we don't generate here, the caller must write lock and call generateWorld in a separate call.
		//generateWorld();
	}

	public TextAdventureWorld()
	{	
		super();
	}

	
	public void generateWorld()
	{
		
		debug("creating new world " + getNameAndPath("/"));
 		
		
		//WorldRoot2d worldRoot = new WorldRoot2d();
		//worldRoot.generateWorld();
		//worldRoot.registerSelfInDbIdListAndAdd(this);
		
		
		// TODO: Here we need to create the rooms.
		
		TextAdventureRoom rootRoom = new TextAdventureRoom(this, "rootRoom");
		
		TextAdventureRoom spawnRoom = new TextAdventureRoom(rootRoom, "spawnRoom");
		TextAdventureRoom secondRoom = new TextAdventureRoom(rootRoom, "secondRoom");
		spawnRoom.connect(secondRoom, "north");
		secondRoom.connect(spawnRoom, "south");
		
	}
	
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		//debug("created unit " + name + " at "+pir);
		// It seems we should check that database is locked here. It is not always...
		turn = wr.readInt();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(turn);
	}
	
	/*
	public String getType()
	{
		return super.getType()+".World";
	}
	*/

	/*
	public void tick(int phase)
	{
		super.tick(phase);
		if (phase==GameObj.TICK_PHASE_LAST)
		{
			turn++;
		}		
	}
	*/
	
	// Non recursive search, will not find avatar if its not in the top of the world
	/*
	public Avatar findAvatar(String name)
	{
		DbBase bo=iterateStoredObjects(null);		
		while(bo!=null)
		{
			if (bo instanceof Avatar)
			{
				Avatar a=(Avatar)bo;
				if (a.getName().equals(name))
				{
					return a;
				}
			}
			bo=iterateStoredObjects(bo);
		}
		return null;
	}
	*/

	/*@Override
	public void tickSelfMs(int deltaMs)
	{
		//final long currentTime=System.currentTimeMillis();
		super.tickSelfMs(deltaMs); // This will do tick for all non DbIdObj. The objects that are subclasses of DbIdObj get their tick from DbIdList instead.	
	}
	*/

	
	// Find a room, optionally a room with a given name.
	// This method should probably been located in some other class.
	/*
	public Room findRoom(String name)
	{
		this.lockRead();
		try
		{
			DbBase bo=iterateStoredObjects(null);		
			while(bo!=null)
			{
				if (bo instanceof Room)
				{
					Room r=(Room)bo;
					if (name==null)
					{
						return r;
					}
					else
					{
						final String roomName=r.getName();
						if ((roomName!=null) && (roomName.equals(name)))
						{
							return r;
						}
					}
				}
				
				
				bo=iterateStoredObjects(bo);
			}
		}
		finally
		{
			this.unlockRead();
		}
		return null;
	}
	*/

    public void messageFromPlayer(Player player, String msg)
	{		
		//stringFifo.put(msg);
		debug("messageFromPlayer "+player.getName()+" "+msg);
		
		roundBuffer.put(player.getName()+" "+msg);
		
		/*
		for (int i=0;i<conncetedPlayers.length;i++)
		{
			if (conncetedPlayers[i]!=null)
			{
				conncetedPlayers[i].notify(conncetedPlayersRef[i], this.getId());
			}
		}
		*/
		notifySubscribers(this.getId());
		
	}

	public DbBase playerJoined(Player player)
	{
		// Har spelaren en avatar redan
		DbBase avatar = this.findDbNamedRecursive(player.getName(), 999);						

		if (avatar==null)
		{
			// Ingen hittad, skapa en ny,
			DbBase spawnRoom = this.findDbNamedRecursive("spawnRoom", 5);	
			
			if (spawnRoom==null)
			{
				error("did not find spawn room");
				
			}
			else
			{
			
			    avatar = new TextAdventureAvatar(spawnRoom , player.getName());
			}
		}
		
		return avatar;
	}


	public String getMsg(int updateCounter)
	{		
		return roundBuffer.get(updateCounter);
	}



	@Override
	public void tickMsCallback(long tickTimeMs)
	{
		/*
		final long deltaTimeMs = tickTimeMs - this.tickTimeMs;
		this.tickTimeMs += deltaTimeMs;
		
		// TODO: Here we do tick on all objects in this game regardless if they need it or not. We could use CPU more efficiently...
		for (DbIdObj dio : idList)
		{
			if (dio instanceof TextAdventureEntity)
			{
				TextAdventureEntity tae = (TextAdventureEntity)dio;
				tae.tickSelfMs(deltaTimeMs);
			}
		}
		*/		
	}
	
}