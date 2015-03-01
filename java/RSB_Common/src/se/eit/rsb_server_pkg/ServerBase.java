package se.eit.rsb_server_pkg;

import se.eit.rsb_package.GlobalConfig;
import se.eit.rsb_package.Player;
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

	GlobalConfig config;
	ServerTcpConnection stc;
	Player player;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ServerBase.class.getSimpleName();	
	}	
	
    public void debug(String str)
	{
    	WordWriter.safeDebug(className()+"("+stc.getInfo()+"): "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}
	
	
	public ServerBase(GlobalConfig config, Player player, ServerTcpConnection stc)
	{
		this.config=config;
		this.stc=stc;
		this.player=player;
	}
	
    public static boolean isStringOkAsWorldName(String name)
    {
    	return WordWriter.isNameOk(name,1); // We shall perhaps require world names to be longer eventually.
    }
	
	abstract protected void join(DbBase bo);
	
	// This is called from PlayerConnectionThread.startNewGame when a new game shall be started.
	// The terrain etc for the new game needs to be filled in.
	public String createAndStore()
	{
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
	    		DbRoot wdb=stc.findWorldsDb();
	    		wdb.lockRead();
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
	    				// TODO: Actually we should reserve the name now. Otherwise it can happen that two users create a game with same name at the same time... It could be done by adding a place holder, a dummy object that is replaced with the real game later when it has been created? 
	    			}
	    		}
	    		finally
	    		{
	    			wdb.unlockRead();
	    		}
	    	}
		}		

		
    	if (worldName!=null)
    	{
    		createAndStore(worldName);
    	}
		return worldName;		
		
	}

	public void close()
	{
		if (stc.isOpen())
		{
			stc.close();
		}
	}
	
	abstract protected String createAndStore(String worldName);
}
