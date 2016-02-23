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


//TODO This is probably outdated and not used any more


public class MibWorld extends WorldBase 
{
	

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return MibWorld.class.getSimpleName();	
	}


	public MibWorld(DbContainer parent, String name, String createdBy) 
	{
		super(parent);
		this.regName(name);
		this.setCreatedBy(createdBy);
	}

	public MibWorld()
	{	
		super();
	}

	
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
	}


	@Override
	public void generateWorld() {
		// This type of world is initially empty
		
	}	
		
	// This returns the name of the server object that clients shall use to play this world.
	@Override
	public String serverForThisWorld()
	{
		return "MibServer";
	}
	
}