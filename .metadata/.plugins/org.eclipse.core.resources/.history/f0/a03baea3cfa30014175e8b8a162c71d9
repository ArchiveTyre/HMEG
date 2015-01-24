package se.eit.rsb_server_pkg;



import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.eit.rsb_package.*;
import se.eit.empire_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class EmpireServer extends ServerBase implements NotificationReceiver {

	//int updateCounter=0;
	
	//DbRoot defaultMibEntry=null;
	
	EmpireWorld ew;
	DbBase defaultObj;
	EmpireTerrain et;
	EmpireUnitTypeList eul;
	EmpireState empireNation;
	
	int updateCounter=0;
	
	// latestVersionOfIdObjectSent keeps a list of the objects from ew that have been sent to the client so that 
	// we can know if updates are needed.
	// http://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
	// http://docs.oracle.com/javase/7/docs/api/java/util/HashMap.html
	HashMap<Integer, Integer> latestVersionOfIdObjectSent = new HashMap<Integer, Integer>();
	
	// idObjectsToUpdate keeps a list of objects that needs to be sent to client
	MyBlockingQueue<Integer> idObjectsToUpdate = new MyBlockingQueue<Integer>(100);

	
	//static final private boolean sendEmpWorld=true;

	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireServer.class.getSimpleName();	
	}	
	
    public static void debug(String str)
	{
    	WordWriter.safeDebug(className()+": "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}

	
	public EmpireServer(GlobalConfig config, Player player, ServerTcpConnection cc)
	{
		super(config, player, cc);	
	}
	
    public String createAndStore(String worldName)
    {
   		createAndStoreNewGame(worldName);	
		return worldName;
    }
	
	
    public EmpireWorld createAndStoreNewGame(String worldName)
    {
    	EmpireWorld ew;
		DbRoot wdb=stc.findWorldsDb();
		try
		{
			wdb.lockWrite();

			ew = new EmpireWorld(wdb, worldName, player.getName());
		}
		finally
		{
			wdb.unlockWrite();
		}
		
    	// todo: ask user about size etc of the world to create here...
    	
    	
    	// Save the database with the new world	    		    	
		ew.saveRecursive(config);
    
		return ew;
    }
	
    /*
	public static DbBase findRelativeFromIndex(String nameOrIndex, DbBase sid[])
	{
		int index=Integer.parseInt(nameOrIndex);
		for(int i=0;i<sid.length; i++)
		{
			if (sid[i].getIndex()==index)
			{
				return sid[i];
			}
		}
		return null;
	}

	public static DbBase findRelativeFromName(String nameOrIndex, DbBase sid[])
	{
		for(int i=0;i<sid.length; i++)
		{
			if (sid[i].getName().equals(nameOrIndex))
			{
				return sid[i];
			}
		}
		return null;
	}

	
	public static DbBase findRelativeFromNameOrIndex(String nameOrIndex, DbBase sid[])
	{		
		if (WordReader.isInt(nameOrIndex))
		{
			return findRelativeFromIndex(nameOrIndex, sid);
		}
		else
		{
			return findRelativeFromName(nameOrIndex, sid);		
		}
		
	}
    */
    
	public static void append(WordWriter ww, String msg)
	{
		ww.writeName("TextBoxAppend");
		ww.writeString(msg);
		ww.endLine();
	}
/*
	public static void prompt(ServerTcpConnection stc)
	{
		WordWriter ww = new WordWriter(stc.getTcpConnection());
		ww.writeName("empPing"); // is this still needed?
		ww.endLine();
	}
*/
	// This is called when a user have sent an order for a unit
	public void unitOrder(WordReader wr) throws IOException
	{
		int unitId=wr.readInt();
		String order=wr.readString();
		if (empireNation==null)
		{
			debug("ignored orders from player with no state/nation");
			WordWriter ww = new WordWriter();
			ww.writeName("TextBoxAppend");
			ww.writeString("You have no state/nation");
			writeLine(ww.getString());
		}
		else
		{
			if (order==null)
			{
				debug("unitOrder missing for unit: "+unitId);
				WordWriter ww = new WordWriter();
				ww.writeName("TextBoxAppend");
				ww.writeString("Did not get the full order");
				writeLine(ww.getString());
			}
			else
			{
				debug("unitOrder: " + unitId + " '" + order + "'");
				DbBase bo = ew.getDbIdObj(unitId);
				
				if (bo instanceof EmpireBase)
				{
					EmpireBase eb=(EmpireBase)bo;
					
					// Only unit owner can give orders to it
					if (eb.getOwner() == empireNation.getIndex())
					{
						EmpireOrder eo = new EmpireOrder();
						eo.setName("o"+eb.getOwner());
						eo.setOrder(order);
						eb.addObjectThreadSafe(eo); // TODO: It would be better to add new orders last, so that orders are performed in the order they are given
						debug("order id "+eo.getId());
					}
					else
					{
						writeLine("Unit "+unitId+" is not your unit");
					}
				}
			}
		}
	}

	
	public void postMessage(EmpireState empireState, String str) throws IOException
	{
		ew.lockWrite();
		try
		{
			if (empireState!=null)
			{
				//empireNation.postMessage("console as message: "+str);
				empireState.postMessage(str);				
			}
			else
			{
				EmpireStatesList enl=ew.getEmpireNationsList();
				for(DbBase db: enl.listOfStoredObjects)
				{
					EmpireState es = (EmpireState)db;
					es.postMessage(str);
				}
			}
		}
		finally
		{
			ew.unlockWrite();
		}
	}

	
	public void writeLine(String str) throws IOException
	{
		// The message can be sent directly to a console.
		// Or indirectly by posting a message to the state that the player has.

		// To console:
		stc.writeLine("empConsoleAppend \""+str+"\"");
		
		// To state messages:
		//postMessage(str);
	}
	
	
	
	public void cancelOrder(WordReader wr)
	{
		int unitId=wr.readInt();
		debug("cancelOrder: "+unitId);
		DbBase u = ew.getDbIdObj(unitId);
		if ((u!=null) && (u instanceof DbContainer))
		{
			DbContainer co = (DbContainer)u;
			ew.lockWrite();
			try
			{
				// iterate
				/*
				DbBase bo=u.iterateStoredObjects(null);
				while(bo!=null)
				{
					if (bo instanceof EmpireOrder)
					{
						bo.clearStored();
						bo.unlinkSelf();
						break;
					}
					bo=u.iterateStoredObjects(bo);
				}
				*/
				int n=co.getMaxIndex();
				for(int i=0;i<n;i++)
				{
					DbBase bo=u.getObjFromIndex(i);
					if (bo instanceof EmpireOrder)
					{
						debug("canceling order "+((EmpireOrder)bo).getOrder());
						bo.clearStored();
						bo.unlinkSelf();						
					}
				}
			}
			finally
			{
				ew.unlockWrite();
			}
		}
		else
		{
			debug("did not find unit "+unitId);
		}
	}

	protected DbBase findObjectByNameOrIndex(DbBase defaultObj, WordReader wr)
	{
		if (defaultObj==null) return null;
		return defaultObj.findObjectByNameIndexOrId(wr);
	}
	
	protected DbBase findObjectByNameOrIndex(String path)
	{
		if ((path.length()>0 && (path.charAt(0)=='/')))
		{
			defaultObj=ew;
			path=path.substring(1);
		}
		WordReader wr= new WordReader(path);
		return findObjectByNameOrIndex(defaultObj, wr);
	}


	protected void unknownCommand(String cmd) throws IOException
	{	
		writeLine("unknown command '"+cmd+"', ");						
		writeLine("try help");
	}

	protected void helpCommand(String cmd) throws IOException
	{
		if (cmd==null)
		{
			writeLine("Player commands:");
			writeLine("  help, list, say");
			writeLine("Additional commands for op:");
			writeLine("  cat, cd, deop, go, id, ls, op, pause, pwd, set, stat, ts");
			writeLine("For more help about one command try the command followed by a '?'");
			writeLine("Example:");
			writeLine("ls ?");
		}
		else
		{
			writeLine("Sorry, no more help on '"+cmd+"'");			
		}
	}
	
	protected boolean isAdmin()
	{
		if (ew.isOp(player.getName()))
		{
			return true;
		}
		return player.getName().equals(ew.getCreatedBy());
	}

	
	protected void typicalHelpPath(String cmd, String msg) throws IOException
	{
		writeLine(msg);							
		writeLine("Usage: "+cmd+" <pathOrName>");
		writeLine("  Where <pathOrName> is an object name, index number, object id or combinations of those.");							
		writeLine("  Object IDs are a number prefixed by ~");		
		writeLine("  A path can look like: /0/city");
	}

	protected EmpireState getEmpireStateFromName(String n)
	{
		EmpireStatesList enl=ew.getEmpireNationsList();
		EmpireState en=null;

		{
			if (WordReader.isInt(n))
			{
				final int idx = Integer.parseInt(n);
				en=(EmpireState)enl.getObjFromIndex(idx);
			}

			if (en==null)
			{
				//en=(EmpireState)enl.findGameObjNotRecursive(n);
				//DbBase db=findObjectByNameOrIndex(n);
				DbBase db=enl.findObjectByNameIndexOrId(n);
				if (db instanceof EmpireState)
				{
					en = (EmpireState)db;
				}
			}

			if (en==null)
			{
				en=(EmpireState)enl.getNationByOwner(n);
			}

			if (en==null)
			{
				DbBase d= findObjectByNameOrIndex(n);
				if (d instanceof EmpireState)
				{
					en=(EmpireState)d;
				}
				else if (d instanceof EmpireUnit)
				{
					EmpireUnit eu=(EmpireUnit)d;
					final int o=eu.getOwner();
					en=(EmpireState)enl.getObjFromIndex(o);									
				}
				else if (d instanceof EmpireSector)
				{
					EmpireSector es=(EmpireSector)d;
					final int o=es.getOwner();
					en=(EmpireState)enl.getObjFromIndex(o);									
				}
			}
		}
		return en;
	}
	
	
    // this interprets a message from a client	
	protected void textMsg(WordReader wr) throws IOException
	{	
		String cmd=wr.readWord();
		
		debug("textMsg cmd: '"+cmd+"'");
		
		if (cmd.equals(""))
		{
			writeLine("");
			return;
		}
		
		final char ch=cmd.charAt(0);
		
		switch(ch)
		{
		case 'a':
		{
			if (cmd.equals("addAlly"))
			{
				try
				{
					ew.lockWrite();

					if (wr.isOpenAndNotEnd())
					{
						String n=wr.readWord(); // Name Or Index of state to say something to.
						
						if (n.equals("?"))
						{
							writeLine("Add a player as an ally, use state name, player name or state number.");							
							writeLine("Usage: addAlly <receiver>");							
							writeLine("  Where <receiver> is a name or number");							
						}
						else
						{
							EmpireState en=getEmpireStateFromName(n);
							if (en!=null)
							{
								empireNation.addAlly("~"+en.getId());
								writeLine("State "+en.getName()+" added as ally");		
								en.postMessage("State " + empireNation.getName()+" offer alliance");					
							}
							else
							{
								writeLine("state "+ n +" was not found");
								writeLine("Usage: addAlly <receiver>");
								writeLine("  Where <receiver> is a name or number");							
							}
						}
					}
					else
					{
						writeLine("Usage: say <name> <message>");											
					}
				}
				finally
				{
					ew.unlockWrite();
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		}			
		case 'c':
			if (cmd.equals("cd"))
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Changes the default object");
				}
				else if (n.length()>0)
				{

					DbBase newDefaultObj=findObjectByNameOrIndex(n);
								
					if (newDefaultObj != null)
					{
						debug("newDefaultObj "+newDefaultObj.getNameOrIndex());
						defaultObj=newDefaultObj;
					}
					else
					{
						writeLine("not found");
					}
				}
				else
				{
					defaultObj=ew;
				}
			}
			else if (cmd.equals("cat"))
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Gives detailed information about an object");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
		
					WordWriter ww = new WordWriter();
					d.listInfo(ww, "  ");
					String str=ww.getString();
					WordReader wr2 = new WordReader(str);
					while(wr2.isOpenAndNotEnd())
					{
						String line2=wr2.readLine();
						writeLine(line2);
					}
				}
			}
			else if (cmd.equals("cancelOrder"))
			{					
				cancelOrder(wr);
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'd':
			if (cmd.equals("dump"))
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Gives detailed information about an object in a short form");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
					writeLine(d.toString());
				}
			}
			else if (cmd.equals("deop"))
			{
				String n=wr.readWord(); // Name Or Index
				if ((n==null) || (n.length()==0))
				{
					ew.setOps(player.getName());
					writeLine("Removed all but "+player.getName()+" from op list");
				}
				else if (n.equals("?"))
				{
					writeLine("Remove all other players from the list of game administrators.");
				}
				else
				{
					if (isAdmin())
					{
						final int r = ew.rmOp(n);
						if (r>0)
						{
							writeLine("Removed "+n+" from op list");							
						}
						else
						{
							writeLine("Did not find "+n+" in op list");								
						}
					}
					else
					{
						writeLine("Permission denied");
					}
				}
			}	
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'g':
			if (cmd.equals("go"))
			{
				if ((wr.isOpenAndNotEnd()) && (wr.isNextInt()))
				{
					int d=wr.readInt(); // delay, lower value -> faster game
					if (isAdmin())
					{
						ew.setGameSpeed(d);
						postMessage(null,"game on");
						writeLine("game is now on, to pause use 'pause' command.");
					}
					else
					{
						writeLine("Permission denied");
					}						
				}
				else
				{
					writeLine("Start the game or change the game speed.");											
					writeLine("Usage: go <tick time divider>");											
					writeLine("  Where <tick time divider>  is number of milliseconds per game tick");	
					writeLine("  A value between 1000 an 10000 ms/tick is recomended.");	
					writeLine("To stop the game again use the pause command.");
				}
			}	
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'h':
			if (cmd.equals("help"))
			{
				if (wr.isOpenAndNotEnd())
				{
					String n=wr.readWord(); // Name Or Index
					helpCommand(n);
				}
				else
				{
					helpCommand(null);
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'i':
			if (cmd.equals("id"))
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Gives the unique ID number of the given object");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
					writeLine(""+d.getId());
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'l':
			if (cmd.equals("ls"))
			{				
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "List all objects stored inside/under the given object");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
		
					DbBase sid[]=d.getListOfSubObjectsThreadSafe();
			
					for(int i=0;i<sid.length; i++)
					{
						writeLine(sid[i].toShortFormatedString());
					}
				}
			}
			else if (cmd.equals("list"))
			{				
				EmpireStatesList enl=ew.getEmpireNationsList();
				for(DbBase db: enl.listOfStoredObjects)
				{
					writeLine(db.toString());
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'o':
			if (cmd.equals("op"))
			{
				String  n=wr.readName();
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Add a player to the list of game administrators. Only ops can issue this command. To see the op list try: cat /");
				}
				else
				{
					if (isAdmin())
					{
						ew.addOp(n);
					}
					else
					{
						writeLine("Permission denied");
					}
				}
			}	
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'p':
			if (cmd.equals("pwd"))
			{
				if (wr.isOpenAndNotEnd())
				{
					writeLine("Shows which object is the current default object.");					
					writeLine("Many other commands can use the current object by using a '.'");	
					writeLine("Example: cat .");	
				}
				else
				{
					writeLine(defaultObj.getNameAndPathInternal("/"));
				}
			}
			else if (cmd.equals("pause"))
			{
				if (wr.isOpenAndNotEnd())
				{
					writeLine("Pauses the game. Only an op can use this command");
					writeLine("To start the game again use the go command.");
				}
				else
				{
					if (isAdmin())
					{
						ew.setGameSpeed(0);
						postMessage(null, "game paused");
						writeLine("Game is paused, to start again use command 'go'");
					}
					else
					{
						writeLine("Permission denied");
					}
				}
			}	
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 'r':
		{
			if (cmd.equals("rmAlly"))
			{
				try
				{
					ew.lockWrite();

					if (wr.isOpenAndNotEnd())
					{
						String n=wr.readWord(); // Name Or Index of state to say something to.
						
						if (n.equals("?"))
						{
							writeLine("Remove a player as an ally, use state name, player name or state number.");							
							writeLine("Usage: rmAlly <receiver>");							
							writeLine("  Where <receiver> is a name or number");							
						}
						else
						{
							EmpireState en=getEmpireStateFromName(n);
							if (en!=null)
							{
								final int r=empireNation.rmAlly("~"+en.getId());
								if (r>0)
								{
									writeLine("State "+en.getName()+" removed as ally");		
									en.postMessage("State " + empireNation.getName()+" no longer offer alliance");
								}
								else
								{
									writeLine("state "+ n +" was not an ally");		
								}
							}
							else
							{
								writeLine("state "+ n +" was not found");
								writeLine("Usage: rmAlly <receiver>");
								writeLine("  Where <receiver> is a name or number");							
							}
						}
					}
					else
					{
						final int r=empireNation.rmAlly(null);
						writeLine("Removed all from list of allies. There was "+r+".");
					}
				}
				finally
				{
					ew.unlockWrite();
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		}			
		case 's':		
			if (cmd.equals("stat"))
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Show some general information about an object.");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
					writeLine(d.getObjInfoPathNameEtc());
				}
			}
			else if (cmd.equals("say"))
			{
				try
				{
					ew.lockWrite();

					if (wr.isOpenAndNotEnd())
					{
						String n=wr.readWord(); // Name Or Index of state to say something to.
						String m=wr.readLine();
						
						//EmpireStatesList enl=(EmpireStatesList) ew.findRecursive(0, EmpireStatesList.class);
						EmpireStatesList enl=ew.getEmpireNationsList();
						EmpireState en=null;
						
						if (n.equals("?"))
						{
							writeLine("Say something to another player. Receiver can be player name or state number.");							
							writeLine("Usage: say <receiver> <message>");							
							writeLine("  Where <receiver> is a name or number");							
						}
						else
						{
							en=getEmpireStateFromName(n);
							
							if (en!=null)
							{
								writeLine("to "+en.getName()+": '"+m+"'");		
								en.postMessage("from " + empireNation.getName()+": '"+m+"'");					
							}
							else
							{
								writeLine("receiver "+ n +" was not found");					
								writeLine("Usage: say <receiver> <message>");
							}
						}
					}
					else
					{
						writeLine("Usage: say <name> <message>");											
					}
				}
				finally
				{
					ew.unlockWrite();
				}
			}
			else if (cmd.equals("set"))
			{
				String n=wr.readWord(); // Name Or Index

				if (n.equals("?"))
				{
					writeLine("Change the value of some property of an object.");							
					writeLine("Usage: set <nameOrPath> <nameOfProperty> <value>");							
					writeLine("Use the cat command to see which poroperties an object has and the current values.");							
					writeLine("Only op can use this command.");	
				}
				else
				{
					if (isAdmin())
					{
						DbBase d= findObjectByNameOrIndex(n);
						if (d!=null)
						{
							String valueTag=wr.readWord();
							if (d.setInfo(wr, valueTag)<0)
							{
								writeLine("did not find tag '"+valueTag+"'");
							}
						}
						else
						{
							writeLine("did not find object '"+n+"'");
						}
					}	
					else
					{
						writeLine("Permission denied");
					}
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		case 't':
			if (cmd.equals("type"))
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Gives the type of an object.");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
					writeLine(d.getType());
				}
			}
			else if (cmd.equals("ts")) // ToString
			{
				String n=wr.readWord(); // Name Or Index
				if (n.equals("?"))
				{
					typicalHelpPath(cmd, "Show detailed information about an object in short form (ToString). See also the cat command.");
				}
				else
				{
					DbBase d= findObjectByNameOrIndex(n);
					writeLine(d.toString());
				}
			}
			else
			{
				unknownCommand(cmd);
			}
			break;
		
		case 'u':
			if (cmd.equals("unitOrder"))
			{					
				unitOrder(wr);
			}
			else
			{
				unknownCommand(cmd);
			}
			break;			
		case 'x':
			if (cmd.equals("xyzzy"))
			{
				writeLine("nothing happens");
			}	
			else
			{
				unknownCommand(cmd);
			}
			break;
			
			
		default:
			{
				unknownCommand(cmd);
				break;
			}									
		}
		
		// send prompt
		writeLine("ok");
	}

	
	// This sends data to client and remember which data (version) it has sent
	// TODO: Perhaps we must make sure client have the parent object also.
	protected void sendUpdateAndRemember(DbIdObj io) throws IOException
	{
		final int id=io.getId();
		final int cc=io.getDbChangedCounter();

		WordWriter ww = new WordWriter();
		ww.writeWord("empWorldUpdate");
		ww.writeInt(id); // id of this object
		ww.writeInt(io.getParentId()); // id of parent object
		ww.writeWord(io.getType());
		io.writeSelf(ww);

		
		final String str=ww.getString();
		
		// EmpireState is updated all the time so don't log those updates.
		/*if (!io.getType().equals("EmpireState"))
		{
			debug(str);		
		}*/
		
		stc.writeLine(str);

		// remember what has been sent to client
		latestVersionOfIdObjectSent.put(id, cc); // remember version of all units sent.

	}
	
	
	protected void sendRemoved(int id) throws IOException
	{	
		WordWriter ww = new WordWriter();
		ww.writeWord("empRemoveUnit");					
		ww.writeInt(id);
		String str=ww.getString();
		debug(str);
		stc.writeLine(str);
	}
	
	
	protected void join(EmpireWorld ew)
	{
		int notifyIdx=-1;
		try 
		{
			this.ew=ew;
			defaultObj=ew;
	
			// Find the nation for this player
			
			// Get terrain
			EmpireStatesList enl;
			ew.lockRead();
			try
			{		
				//enl=(EmpireStatesList) ew.findRecursive(0, EmpireStatesList.class);
				enl=ew.getEmpireNationsList();
				empireNation=(EmpireState) enl.getNationByOwner(player.getName());
			}
			finally
			{
				ew.unlockRead();
			}
			
			if (empireNation==null)
			{		
				empireNation=(EmpireState) enl.takeNation(player.getName());			
			}
			
			if (empireNation!=null)
			{
				stc.alertBox("in_charge", "you are in charge of state/nation "+empireNation.getName());
			}
			else
			{
				stc.alertBox("in_charge", "all states/nations are taken, you can only spectate");			
			}
		
			notifyIdx=ew.addNotificationReceiver(this, 0);
			
			
			// Tell client SW to draw the empire main window
			stc.writeLine("openEmpire "+ew.getNameOrIndex());

			//counter=ew.isChanged(previousUpdateCounter);

			if (empireNation!=null)
			{
				stc.writeLine("joinEmpire "+empireNation.getIndex());
			}
			
			// Send world data to client, TODO: Should probably write to an internal string buffer first and then send it to client (to avoid locking database more than needed)
			ew.lockRead();
    		try
    		{
    			WordWriter ww = new WordWriter(stc.getTcpConnection());
    			//ww.setFlushAfterLf();
				ww.writeLine("empWorld");
				ww.writeBegin();
				//ww.writeInt(-1);
				ww.writeWord(ew.getType());
				ew.writeRecursive(ww);
				ww.writeEnd();
				ww.flush();
    		}
    		finally
    		{
    			ew.unlockRead();
    		}						
    		stc.writeLine("empWorldShow");
    		//stc.close();stc=null;return;
			
    		// If user log in while game is paused.
			if (ew.gameSpeed==0)
			{
	    		if (isAdmin())
	    		{
    				postMessage(empireNation, "game is paused");
    				postMessage(empireNation, "you are admin of this game, to start the game issue command: go 10000");
    			}
	    		else
	    		{
    				postMessage(empireNation, "game is paused (waiting for op/admin to start the game)");
	    			
	    		}
    		}

			
			
			// loop waiting for input from client
	  		while(stc.isOpen())
	  		{
	  			int timeoutCounter=0; // If we get many timeouts in a row we will disconnect

	  			//prompt(stc); // this is perhaps not needed any more

	  			try {
	  				String r = stc.readLine(100);

	  				// This was not timeout so reset that counter
	  				timeoutCounter=0;
				
					WordReader wr=new WordReader(r);
					
					String cmd=wr.readWord();					
	
					
					debug("reply from client '"+player.getName()+"' "+cmd);
					if (cmd.equals("cancel"))
					{
						break;
					}
					/*else if (cmd.equals("empPong"))
					{
						// When server send ping the client shall reply with pong.
						// Perhaps this feature is not needed?
						debug("empPong reply from client '"+player.getName()+"' "+cmd);
					}*/
					else if (cmd.equals("tick"))
					{
						debug("tick");
					}
					else if (cmd.equals("mouseDown"))
					{
						WordWriter ww = new WordWriter();
						int x=wr.readInt();
						int y=wr.readInt();					
						debug("mouseDown: "+x+" "+y);
						ww.writeWord("upperTextAreaAppend");
						ww.writeString("mouse down at "+x+" "+y);
						stc.writeLine(ww.getString());
					}
					else if (cmd.equals("mouseUp"))
					{					
						WordWriter ww = new WordWriter();
						int x=wr.readInt();
						int y=wr.readInt();					
						debug("mouseUp: "+x+" "+y);										
						ww.writeWord("upperTextAreaAppend");
						EmpireUnit eu=et.findUnitAt(x,y);
						if (eu!=null)
						{
							ww.writeString(eu.toString());
						}
						else
						{
							ww.writeString("no unit at "+x+" "+y);
						}
						stc.writeLine(ww.getString());
					}
					else if (cmd.equals("textMsg"))
					{
						final String txtMsg=wr.readString();
						String line=WordReader.removeQuotes(txtMsg);
						WordReader wr2=new WordReader(line);					
						textMsg(wr2);
					}
					else if (cmd.equals("unitOrder"))
					{					
						unitOrder(wr);
					}
					else if (cmd.equals("cancelOrder"))
					{					
						cancelOrder(wr);
					}
					else
					{
						stc.error("unknown command " + cmd);
						break;
					}			

	  			} catch (InterruptedException e) {
	  				// This was just a timeout. But if we have many in a row we disconnect the client.
	  				if (timeoutCounter>15*60*10)
	  				{
	  					throw new InterruptedException();
	  				}
	  				else
	  				{
		  				timeoutCounter++;	  					
	  				}
	  			}
	
				// Find changes in database.
				
				
				if (idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity())
				{
					// queue has gotten full. Ignore the queue, instead scan database from scratch.
				
				
					// http://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
					
					// Iterate the list of objects we have previously sent to client.
					// If the object no longer exist send a "removeUnit" message.
					// Check for removed objects or updated objects.
					// http://java67.blogspot.se/2013/08/best-way-to-iterate-over-each-entry-in.html
					Iterator<Map.Entry<Integer, Integer>> iterator = latestVersionOfIdObjectSent.entrySet().iterator();
					while(iterator.hasNext())
					{
					    Map.Entry<Integer, Integer> entry = iterator.next();
					    int key=entry.getKey();
						DbIdObj io=ew.getDbIdObj(key);
						if (io==null)
						{
							sendRemoved(key);
	 					    iterator.remove(); // right way to remove entries from Map, 
						}
						else
						{
							final Integer value=entry.getValue();
							final int sentDbChangedCounter=value;
							final int currentDbChangedCounter=io.getDbChangedCounter();
							if (sentDbChangedCounter!=currentDbChangedCounter)
							{
								sendUpdateAndRemember(io);							
							}
						}
					}
	
	
									
					// Here we should iterate all units in the part of the world this user can see.
					// But for now we will iterate everything. This might become terribly inefficient when world grows large.
					final int idListLength=ew.getDbIdListLength();
					for (int i=0; i<idListLength; i++)
					{
						DbIdObj io=ew.getDbIdObj(i);
						if (io!=null)
						{
							final Integer prevChangeCounter=latestVersionOfIdObjectSent.get(i);
							if (prevChangeCounter==null)
							{
								sendUpdateAndRemember(io);
							}
							else
							{
								final int sentDbChangedCounter=prevChangeCounter;
								if (io.isChanged(sentDbChangedCounter))
								{
									sendUpdateAndRemember(io);
								}
							}
						}			
					}
					idObjectsToUpdate.clear();
					
		    		stc.writeLine("empWorldUpdated");	

				}
				else
				{
					// the notification queue was not full, send updates using it.
					int n=idObjectsToUpdate.size();
					if (n>0)
					{
						while(n>0)
						{
							int id=idObjectsToUpdate.take(0);
							DbIdObj io=ew.getDbIdObj(id);
							if (io==null)
							{
								sendRemoved(id);
							}
							else
							{
								
								final Integer prevChangeCounter=latestVersionOfIdObjectSent.get(id);
								if (prevChangeCounter==null)
								{
									// a previously not sent object
									sendUpdateAndRemember(io);
								}
								else
								{
									// an object previously sent, sending again is only needed if it has been changed since then
									final int sentDbChangedCounter=prevChangeCounter;
									if (io.isChanged(sentDbChangedCounter))
									{
										sendUpdateAndRemember(io);
									}
								}
								
							}
							n--;
						}
			    		stc.writeLine("empWorldUpdated");	
					}
				}
					
					
					
					

				// Check for changes that needs to be sent to client
				// not tested yet
				/*
				final int rootUpdateCounter=ew.getDbRootUpdateCounter();
				if (updateCounter!=rootUpdateCounter)
				{
					WordWriter ww = new WordWriter();
		    		//ww.writeWord("worldChanges");
					ew.listChangedObjects(updateCounter, ww);
		    		//ww.println("\n"); // sending this newline is probably not needed any longer, remove some day
				
		    		stc.writeLine(ww.getString());
					
					updateCounter=rootUpdateCounter;
				}
				*/
				
				// Check for messages to be sent to client
				/*
				String msg;
				try {
					msg = this.msgQueue.take(0);
						// send messages to client
						ww.writeLine("say "+msg);
				} catch (InterruptedException e) {
					// timeout is normal
				}
				*/
	
	
  			}
				
		} catch (InterruptedException e) {
			// This was probably just timeout.
			//e.printStackTrace();
			debug("run: InterruptedException "+e);
		} catch (IOException e) {
			// Probably just a disconnect
			//e.printStackTrace();
			debug("run: IOException "+e);
		} 
		finally
		{
			if (notifyIdx!=-1)
			{
				ew.removeNotificationReceiver(notifyIdx);
				notifyIdx=-1;
			}
		    close();
		}
		
		
		
	}
			
    

	protected void join(DbBase bo)
	{
		//bo.addNotificationReceiver(this, 0);		
		//defaultMibEntry = ro;
		
		if (bo instanceof EmpireWorld)
		{
			join((EmpireWorld)bo);			
		}
		else
		{
			error("not an EmpireWorld");
		}
	}	
    
	public int messageFromPlayer(String msg)
	{
		//w.messageFromPlayer(player, msg);
		
		return 0;
	}

	
	public void close()
	{
		//w.removeNotificationReceiver(this);
	}
	

	// Notify can be called from various threads so do only thread safe stuff here.
	public void notify(int subscribersRef, int sendersRef)
	{
		if (idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity())
		{
			// queue is full
			// ignore further notifications.
			// we will need to scan the entire database for changes instead.
		}
		else
		{
			idObjectsToUpdate.put(sendersRef);
		}
	}
	
	public void unlinkNotify(int subscribersRef)
	{
		try {
			stc.writeLine("empWorldClose");
			stc.close();
		} catch (IOException e) {
			debug("notify: IOException "+e);
			e.printStackTrace();
		}			
	}
}
