//WorldBase.java
//
//The top object for all games shall inherit this class
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
//History:
//Created by Henrik 2014-03-01


package se.eit.rsb_package;

import java.util.Random;

import se.eit.db_package.*;
import se.eit.web_package.*;



public abstract class WorldBase extends NotificationSender
{
	
	public final static String nameOfWorldsDb = "worldsDatabase"; // worldsDatabase
    public final static String typeOfWorldsDb = "DbNoSaveRoot"; 
	
	
	protected String createdBy;  // Who created this game world
	protected long creationTime; // When game was created in unix/posix time milliseconds.
	//protected long gameTime; // How long the game has been running in its own time units counting (e.g. this is not incremented when game is paused). 
	protected RsbLong gameTime=null;
	String ops=""; // A lost of admins/ops that can pause and start the game.
	public String gamePassword="";
	String bannedPlayers="";

	public String customWorldScript=null; // Custom LUA script to be used when generating world
	//public String customAvatarScript_no_longer_used=null; // Custom LUA script to be used when adding an Avatar. TODO let there be an override function in customWorldScript (WorldMain.lua) instead. Or a function "createAvatar" in spawn room.
	public String customScriptPath=null; // Path to custom LUA scripts to be used for this world
	
	//protected DbNamed playerList; // A list of currently connected players
	
    public Random random = new Random();

	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return WorldBase.class.getSimpleName();	
	}


	public WorldBase(DbContainer parent) 
	{
		super();

		DbSubRoot ro = parent.getDbSubRoot();
		ro.lockWrite();
		try
		{
			this.linkSelf(parent);
		}
		finally
		{
			ro.unlockWrite();
		}

		creationTime=System.currentTimeMillis();	
	}


	public WorldBase()
	{	
		super();
		creationTime=System.currentTimeMillis();	
	}

	@Override
	public void linkSelf(DbContainer parentObj)
	{
		super.linkSelf(parentObj);
		requestTick(1000);
	}

	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		//debug("created unit " + name + " at "+pir);
		// It seems we should check that database is locked here. It is not always...
		createdBy = wr.readString();
		creationTime= wr.readLong();
		/*gameTime=*/wr.readLong();
		ops=wr.readString();
		gamePassword=wr.readString();
		bannedPlayers=wr.readString();
		customWorldScript=wr.readString();
		/*customAvatarScript_no_longer_used=*/wr.readString();
		customScriptPath=wr.readString();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeString(createdBy);
		ww.writeLong(creationTime);
		ww.writeLong(0/*gameTime*/);
		ww.writeString(ops);
		ww.writeString(gamePassword);
		ww.writeString(bannedPlayers);
		ww.writeString(customWorldScript);
		ww.writeString(""/*customAvatarScript_no_longer_used*/);
		ww.writeString(customScriptPath);
		
	}	

	@Override
	public void listInfo(WordWriter pw)
	{
		super.listInfo(pw);					
		pw.println("createdBy"+" "+createdBy);		
		pw.println("creationTime"+" "+creationTime);		
		//pw.println(prefix+"gameTime "+gameTime);		
		pw.println("ops"+" '"+ops+"'");		
		pw.println("gamePassword"+" '"+gamePassword+"'");		
		pw.println("bannedPlayers"+" '"+bannedPlayers+"'");		
		pw.println("customWorldScript"+" '"+customWorldScript+"'");		
		//pw.println(prefix+"customAvatarScript '"+customAvatarScript_no_longer_used+"'");		
		pw.println("customScriptPath"+" '"+customScriptPath+"'");		
		
	}

	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("gamePassword"))
		{
			gamePassword=wr.readString();
			return 1;
		}
		else if (infoName.equals("ops"))
		{
			ops=wr.readString();
			return 1;
		}
		else if (infoName.equals("bannedPlayers"))
		{
			bannedPlayers=wr.readString();
			return 1;
		}
		else if (infoName.equals("customWorldScript"))
		{
			customWorldScript=wr.readString();
			return 1;
		}
		/*else if (infoName.equals("customAvatarScript"))
		{
			customAvatarScript_no_longer_used=wr.readString();
			return 1;
		}*/
		else
		{
			return super.setInfo(wr, infoName);
		}
	}

	@Override
	public int getInfo(WordWriter ww, String parameterName, WordReader wr)
	{
		if (parameterName.equals("ops"))
		{
			ww.writeString(ops);
			return 1;
		}
		if (parameterName.equals("gamePassword"))
		{
			ww.writeString(gamePassword);
			return 1;
		}
		
		return super.getInfo(ww, parameterName, wr);
	}

	
	
	public String getCreatedBy()
	{
		return createdBy;
	}
	
	public String getOps()
	{
		return ops;
	}

	public String getBannedPlayers()
	{
		return bannedPlayers;
	}

	// Return >0 if OK, <=0 if not OK.
	public int addOp(String name)
	{
		if (WordWriter.isNameOk(name, 1))
		{
			ops = Misc.addWordToList(name, ops);
			return 1;
		}
		return 0;
	}

	public int setOps(String name)
	{
		if (WordWriter.isNameOk(name, 1))
		{
			ops=name;
		}
		else
		{
			return -1;
		}
		return 0;
	}

	// Return >0 if OK, <=0 if not OK.
	public int rmOp(String str)
	{
		int n =  Misc.getNWordInList(str , ops);
		if (n>0)
		{
			ops = Misc.removeWordFromList(str, ops);
		}
		return n;
	}
	
	static public boolean isWordInListOfWords(String name, String list)
	{
		return Misc.getNWordInList(name, list)>0?true:false;
	}

	public boolean isOp(String name)
	{
		return this.createdBy.equals(name) || isWordInListOfWords(name, ops);
	}

	public boolean isPlayerBanned(String name)
	{
		return isWordInListOfWords(name, bannedPlayers);
	}
	
	@Override
	public void tickMsCallback(long tickTimeMs)
	{
		// Extending classes shall override this method.
	}
	
	/*
	@Override
	public void tickGameSlow(GlobalConfig config)
	{
		// Extending classes shall override this method.
	}
	*/

	public void setCreatedBy(String createdBy)
	{
		this.createdBy=createdBy;
	}
	
	public String getGamePassword()
	{
		return gamePassword;
	}

	
	
	
	// Find or create the time object
	/*
	public RsbLong findGameTime()
	{
		if (gameTime!=null)
		{
			return gameTime;
		}
		
		gameTime = (RsbLong)findOrCreateChildObject("_gameTime", "RsbLong");
	    
		return gameTime;
	}
	*/

	// How much time the game has progressed.
	// For FPS games the unit here shall be milliseconds (ms) but for strategy games it is just unspecified ticks.
	public long getGameTime()
	{
		//return findGameTime().getValue();
		return gameTime.getValue();
	}
	
	public void setGameTime(long newTime)
	{
		//findGameTime().setValue(newTime);
		gameTime.setValue(newTime);
	}

	// Returns the new time
	public long addGameTime(long deltaTimeMs)
	{
		final long newTime=getGameTime()+deltaTimeMs;
		setGameTime(newTime);
		return newTime;
	}

	// Create the sub objects we must have
	/*
	@Override
	public void generateSelf()
	{
		RsbLong tmp = new RsbLong();
		tmp.linkSelf(this);
		tmp.setName("_gameTime");
	}
	*/

	@Override
	public void regName(String name)
	{
		this.lockWrite();
		try
		{
			super.regName(name);
		}
		finally
		{
			this.unlockWrite();
		}
	}

	// Register the objects created by createNamedObjects
	@Override
	public void regNamedObject(DbNamed addedObject)
	{
		final String n=addedObject.getName();
		if ((addedObject instanceof RsbLong) && (n.equals("_gameTime")))
		{
			
			// the following few lines are for debugging, can be removed later
			if ((gameTime!=null) && (addedObject!=gameTime))
			{
				error("not allowed to change this child object");
			}

			gameTime=(RsbLong)addedObject;
		}
		else
		{
			super.regNamedObject(addedObject);
		}
	}

	// Create child objects that this object must have.
	@Override
	public void createNamedObjects()
	{
		super.createNamedObjects();
		
		// The following few lines are for debugging, can be removed or commented out later
		if (gameTime!=null)
		{
			error("gameTime!=null");
		}

		// The gameTime is stored in a separate object because it is changed so frequently.
		RsbLong tmp = new RsbLong();
		tmp.linkSelf(this);
		tmp.regName("_gameTime");
		
	}
	
	// All game worlds must override this.
	// TODO this should be abstract.
	public void generateWorld()
	{
		error("generateWorld is not implemented");
	}


	// This returns the name of the server object that clients shall use to play this world.
	public abstract String serverForThisWorld();

}
