//World.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.rsb_package;

import se.eit.db_package.*;
import se.eit.web_package.*;


// TODO This is probably outdated and not used any more

public class ChatRoomWorld extends WorldBase {
	
	
	int turn=0;

	//StringFifo stringFifo;	
	
	
	RoundBuffer roundBuffer=new RoundBuffer(32);
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ChatRoomWorld.class.getSimpleName();	
	}


	public ChatRoomWorld(DbContainer parent, String name, String createdBy) 
	{
		super(parent);
		this.regName(name);
		this.setCreatedBy(createdBy);
		
		// we don't generate here, the caller must write lock and call generateSelf in a separate call.
		//generateSelf();
	}

	public ChatRoomWorld()
	{	
		super();
	}

	
	public void generateSelf()
	{
		
		debug("creating new world " + getNameAndPath("/"));
 		
		
		//WorldRoot2d worldRoot = new WorldRoot2d();
		//worldRoot.generateSelf();
		//worldRoot.registerSelfInDbIdListAndAdd(this);

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

	/*
	@Override
	public void tickMs(int deltaMs)
	{
		//final long currentTime=System.currentTimeMillis();
		super.tickMs(deltaMs); // This will do tick for all non DbIdObj. The objects that are subclasses of DbIdObj get their tick from DbIdList instead.	
	}*/

	
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

	public void messageFromPlayer(PlayerData player, String msg)
	{		
		//stringFifo.put(msg);
		debug("messageFromPlayer "+player.getName()+" "+msg);
		
		roundBuffer.put(player.getName()+": "+msg);
		
		/*for (int i=0;i<conncetedPlayers.length;i++)
		{
			if (conncetedPlayers[i]!=null)
			{
				conncetedPlayers[i].notify(conncetedPlayersRef[i], this.getId());
			}
			
		}*/
        notifySubscribers(this.getId());

	}

	

	
	public String getMsg(int updateCounter)
	{		
		return roundBuffer.get(updateCounter);
	}
	

	// This returns the name of the server object that clients shall use to play this world.
	@Override
	public String serverForThisWorld()
	{
		return "ChatRoomServer";
	}

}