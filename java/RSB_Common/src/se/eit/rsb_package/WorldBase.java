//WorldBase.java
//
//The top object for all games shall inherit this class
//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Created by Henrik 2014-03-01


package se.eit.rsb_package;

import se.eit.db_package.*;
//import java.util.Random;
import se.eit.web_package.*;



public class WorldBase extends DbTickReceiver
{
	
	public final static String nameOfWorldsDb = "worldsDatabase"; 

	
	protected String createdBy;  // Who created this game world
	protected long creationTime; // When game was created in unix/posix time milliseconds.
	protected long gameTime; // How long the game has been running in its own time units counting (e.g. this is not incremented when game is paused).
	String ops=""; // A lost of admins/ops that can pause and start the game.
	public String gamePassword="";
	String bannedPlayers="";

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return WorldBase.class.getSimpleName();	
	}


	public WorldBase(DbBase parent) 
	{
		super();

		DbRoot ro = parent.getDbRoot();
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
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		//debug("created unit " + name + " at "+pir);
		// It seems we should check that database is locked here. It is not always...
		createdBy = wr.readWord();
		creationTime= wr.readLong();
		gameTime=wr.readLong();
		ops=wr.readString();
		gamePassword=wr.readString();
		bannedPlayers=wr.readString();

	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeWord(createdBy);
		ww.writeLong(creationTime);
		ww.writeLong(gameTime);
		ww.writeString(ops);
		ww.writeString(gamePassword);
		ww.writeString(bannedPlayers);
	}	

	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"createdBy "+createdBy);		
		pw.println(prefix+"creationTime "+creationTime);		
		pw.println(prefix+"gameTime "+gameTime);		
		pw.println(prefix+"ops '"+ops+"'");		
		pw.println(prefix+"gamePassword '"+gamePassword+"'");		
		pw.println(prefix+"bannedPlayers '"+bannedPlayers+"'");		
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
		else
		{
			return super.setInfo(wr, infoName);
		}
	}

	public long getGameTime()
	{
		return gameTime;
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
		return isWordInListOfWords(name, ops);
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

	public void setCreatedBy(String createdBy)
	{
		this.createdBy=createdBy;
	}
	
	public String getGamePassword()
	{
		return gamePassword;
	}
}
