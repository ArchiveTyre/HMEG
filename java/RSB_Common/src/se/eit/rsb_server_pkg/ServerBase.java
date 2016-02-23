// ServerBase.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2015 


package se.eit.rsb_server_pkg;

import se.eit.rsb_factory_pkg.GlobalConfig;
import se.eit.rsb_package.PlayerData;
import se.eit.rsb_package.WorldBase;
import se.eit.db_package.*;
import se.eit.web_package.*;


/*
 * All game server threads inherit this class.
 * To create a new type of game:
 * Create a new class extending this class.
 * Add it in PlayerConnectionThread.startNewGame and in PlayerConnectionThread.playWorld
 * Create a new game base object extending WorldBase. 
 * Add that new class in DbContainer.createObj
 */


public abstract class ServerBase {

	protected WorldBase worldBase;
	protected GlobalConfig config;
	protected ServerTcpConnection stc;
	protected PlayerData player;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ServerBase.class.getSimpleName();	
	}	
	
    public void debug(String str)
	{
    	WordWriter.safeDebug(className()+"("+stc.getTcpInfo()+"): "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}
	
	
	public ServerBase()
	{
	}
	
    public static boolean isStringOkAsWorldName(String name)
    {
    	return WordWriter.isNameOk(name,1); // We shall perhaps require world names to be longer eventually.
    }
	
	public void joinWorld(WorldBase worldBase, GlobalConfig config, PlayerData player, ServerTcpConnection stc)
	{
		this.worldBase=worldBase;
		this.config=config;
		this.player=player;
		this.stc=stc;
		joinWorld();
	}

	
	// This is called from PlayerConnectionThread.startNewGame when a new game shall be started.
	// The terrain etc for the new game needs to be filled in.
	public WorldBase createAndStore(GlobalConfig config, PlayerData player, ServerTcpConnection stc)
	{
		this.config=config;
		this.stc=stc;
		this.player=player;
		
		if (worldBase!=null)
		{
			error("worldBase is not null");
		}
		
		String worldName=null;
		while((stc.isOpen() && (worldName==null)))
		{
	    	worldName=stc.enterNameForNewGame();

	    	if (worldName==null)
	    	{
	    		// user did cancel or disconnect.
	    		break;	    		
	    	}
	    	
	    	if (!isStringOkAsWorldName(worldName))
    		{    			
    			stc.alertBox("name_not_accepted", "name not accepted, try another name with only letters and digits");
    			worldName=null;
    		}
	
	    	if (worldName!=null)
	    	{
	    		DbSubRoot wdb=stc.findOrCreateGameDb();
	    		wdb.lockWrite();
	    		try
	    		{
	    			if (wdb.findGameObjNotRecursive(worldName)!=null)
	    			{
	    				debug("Name already exist '"+worldName+"'");
	    	    		stc.alertBox("name_already_taken", "name already taken");
	    				worldName=null;
	    			}	    
	    			else
	    			{
	    				debug("Name '"+worldName+"' is not yet used");
	    				
    		    		worldBase = createWorld();
    		    		worldBase.linkSelf(wdb);
    		    		worldBase.regName(worldName);
    		    		worldBase.setCreatedBy(player.getName());
	    		    	
	    				
	    			}
	    		}
	    		finally
	    		{
	    			wdb.unlockWrite();
	    		}
	    	}
		}		

		
    	if (worldBase!=null)
    	{
    		worldBase.lockWrite();
	    	try {
	    		configureGame();
	    		//worldBase.setGlobalConfig(config);
				worldBase.saveRecursive();
	    	}
			finally
			{
				worldBase.unlockWrite();
			}
    	}
    	

    	
		return worldBase;		
		
	}

	public void close()
	{
		if (stc.isOpen())
		{
			stc.close();
		}
	}
	
	//abstract protected String createAndStore(String worldName);
	
	abstract public WorldBase createWorld();

	
	public void configureGame()
	{		
		worldBase.generateWorld();
	}

	abstract public void joinWorld();
	
	/*
	// Tell here what type of client is needed for the game
	abstract public boolean need2dSupport();

	// Tell here what type of client is needed for the game
	abstract public boolean need3dSupport();
	*/


	public boolean onlyOnMainServer()
	{
		return false;
	}

}
