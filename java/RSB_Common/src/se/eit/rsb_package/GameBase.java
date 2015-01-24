//
//Copyright (C) 2014 Henrik Björkman www.eit.se
//
//History:
//Created by Henrik 2014-04-08


package se.eit.rsb_package;

import se.eit.db_package.*;


public abstract class GameBase extends DbThreadSafe {
	
	// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
	public static String className()
	{	
		return GameBase.class.getSimpleName();	
	}


	public GameBase()
	{
		super();
	}

	
	//abstract public void generate();

	/*
	@Override	
	public void tickRecursiveMs(int deltaMs)
	{		
		// doing nothing, objects within a game will need some other more efficient method than this.
	}
	*/

}