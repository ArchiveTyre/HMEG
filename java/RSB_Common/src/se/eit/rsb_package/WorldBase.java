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

	
	protected String createdBy;
	protected long creationTime;
	protected long gameTime;
	String ops;
	
	

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
	}	

	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"createdBy "+createdBy);		
		pw.println(prefix+"creationTime "+creationTime);		
		pw.println(prefix+"gameTime "+gameTime);		
		pw.println(prefix+"ops '"+ops+"'");		
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

	public int addOp(String name)
	{
		if (WordWriter.isNameOk(name, 1))
		{
			if (ops.length()>0)
			{
				ops+=" ";
			}
			ops+=name;
		}
		else
		{
			return -1;
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

	public int rmOp(String str)
	{
		int n=0;
		WordReader wr=new WordReader(ops);
		ops="";
		while(wr.isOpenAndNotEnd())
		{
			String a=wr.readWord();
			if (!a.equals(str))
			{
				addOp(a);
			}
			else
			{
				n++;
			}
		}
		return n;
	}
	
	public boolean isOp(String name)
	{
		String ops=getOps();
		if (ops!=null)
		{
			WordReader wr=new WordReader(ops);
			while(wr.isOpenAndNotEnd())
			{
				String n=wr.readName();
				if (name.equals(n))
				{
					return true;
				}
			}
		}
		return false;
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
}
