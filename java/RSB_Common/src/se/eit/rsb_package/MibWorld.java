//World.java
//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.rsb_package;
import se.eit.db_package.*;


//import se.eit.rsb_package.*;
import se.eit.web_package.*;



public class MibWorld extends WorldBase 
{
	

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return MibWorld.class.getSimpleName();	
	}


	public MibWorld(DbBase parent, String name, String createdBy) 
	{
		super(parent);
		this.setName(name);
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
		
}