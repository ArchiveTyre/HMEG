package se.eit.rsb_server_pkg;

import java.io.IOException;

import se.eit.db_package.DbBase;
//import se.eit.empire_package.EmpireState;
//import se.eit.empire_package.EmpireWorld;
import se.eit.rsb_srv_main_pkg.GlobalConfig;
import se.eit.rsb_package.Player;
import se.eit.rsb_package.WorldBase;
import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;

public abstract class OpServer extends MirrorServer {

	protected DbBase defaultObj;

	
	public OpServer(GlobalConfig config, Player player, ServerTcpConnection stc) {
		super(config, player, stc);
	}

	/*
	@Override
	protected void join(DbBase bo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String createAndStore(String worldName) {
		// TODO Auto-generated method stub
		return null;
	}
*/

	
	public void writeLine(String str) throws IOException
	{
		// The message can be sent directly to a console.
		// Or indirectly by posting a message to the state that the player has.

		// To console:
		stc.writeLine("empConsoleAppend \""+str+"\"");
		
		// To state messages:
		//postMessage(str);
	}

	protected void unknownCommand(String cmd) throws IOException
	{	
		writeLine("unknown command '"+cmd+"', ");						
		writeLine("try help");
	}
	
	protected boolean isAdmin()
	{
		WorldBase wb=(WorldBase)worldBase;
		if (wb.isOp(player.getName()))
		{
			return true;
		}
		return player.getName().equals(wb.getCreatedBy());
	}
	
	protected DbBase findObjectByNameOrIndex(DbBase db, WordReader wr)
	{
		if (db==null) return null;
		return db.findObjectByNameIndexOrId(wr);
	}
	
	protected DbBase findObjectByNameOrIndex(String path)
	{
		DbBase db=null;
		if ((path.length()>0 && (path.charAt(0)=='/')))
		{
			db=worldBase;
			path=path.substring(1);
		}
		else
		{
			db=defaultObj;
		}
		WordReader wr= new WordReader(path);
		return findObjectByNameOrIndex(db, wr);
	}

	
	protected void typicalHelpPath(String cmd, String msg) throws IOException
	{
		writeLine(msg);							
		writeLine("Usage: "+cmd+" <pathOrName>");
		writeLine("  Where <pathOrName> is an object name, index number, object id or combinations of those.");							
		writeLine("  Object IDs are a number prefixed by ~");		
		writeLine("  A path can look like: /0/city");
	}
	
	// Returns true if the command was found and help given.
	protected boolean helpCommand(String cmd) throws IOException
	{
		if (cmd==null)
		{
			if (isAdmin())
			{
				writeLine("Additional commands for op:");
				writeLine("  cat, cd, deop, go, id, ls, mute, op, pause, pwd, set, setPw, stat, ts");
			}
		}
		else if (cmd.equals("cd"))
		{
			typicalHelpPath(cmd, "Changes the default object");
		}
		else if (cmd.equals("cat"))
		{
			typicalHelpPath(cmd, "Gives detailed information about an object");
		}
		else if (cmd.equals("dump"))
		{
			typicalHelpPath(cmd, "Gives detailed information about an object in a short form");
		}
		else if (cmd.equals("deop"))
		{
			writeLine("Remove players from the list of game administrators.");
		}
		else if (cmd.equals("ls"))
		{
			typicalHelpPath(cmd, "List all objects stored inside/under the given object");
		}
		else if (cmd.equals("op"))
		{
			typicalHelpPath(cmd, "Add a player to the list of game ops/administrators. Only ops can issue this command. To see the op list try: cat /");
		}
		else if (cmd.equals("setPw"))
		{
			writeLine("Make the game private. Players must enter the password before they can get a place in the game.");					
		}
		else if (cmd.equals("pwd"))
		{
			writeLine("Shows which object is the current default object.");					
			writeLine("Many other commands can use the current object by using a '.'");	
			writeLine("Example: cat .");	
		}
		else if (cmd.equals("set"))
		{
			writeLine("Change the value of some property of an object.");							
			writeLine("Usage: set <nameOrPath> <nameOfProperty> <value>");							
			writeLine("Use the cat command to see which poroperties an object has and the current values.");							
			writeLine("Only op can use this command.");	
		}
		else
		{
			return false;
		}
		return true;
	}
	
	// Returns true if the command was found and performed.
	protected boolean doCommand(String cmd, WordReader wr) throws IOException
	{	
		final char ch=cmd.charAt(0);
		
		switch(ch)
		{
			case 'c':
			{
				if (cmd.equals("cd"))
				{
					String n=wr.readWord(); // Name Or Index
					if (n.equals("?"))
					{
						helpCommand(cmd);
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
						defaultObj=worldBase;
					}
				}
				else if (cmd.equals("cat"))
				{
					String n=wr.readWord(); // Name Or Index
					if (n.equals("?"))
					{
						helpCommand(cmd);
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
				else
				{
					return false;
				}
				break;
			}
			case 'd':
			{
				if (cmd.equals("deop"))
				{
					if (isAdmin())
					{
						String n=wr.readWord(); // Name Or Index
						if ((n==null) || (n.length()==0))
						{
							WorldBase ew=(WorldBase)worldBase;
							ew.setOps(player.getName());
							writeLine("Removed all but "+player.getName()+" from op list");
						}
						else if (n.equals("?"))
						{
							helpCommand(cmd);
						}
						else
						{
							WorldBase ew=(WorldBase)worldBase;
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
					}
					else
					{
						writeLine("Permission denied");
					}
				}	
				else if (cmd.equals("dump"))
				{
					String n=wr.readWord(); // Name Or Index
					if (n.equals("?"))
					{
						helpCommand(cmd);
					}
					else
					{
						DbBase d= findObjectByNameOrIndex(n);
						writeLine(d.toString());
					}
				}
				else
				{
					return false;
				}
				break;
			}
			case 'h':
			{
				if (cmd.equals("help"))
				{
					if (wr.isOpenAndNotEnd())
					{
						String n=wr.readWord(); // Name Or Index
						if (helpCommand(n)==false)
						{
							writeLine("Sorry, no more help on '"+cmd+"'");
						}
					}
					else
					{
						helpCommand(null);
					}
				}
				else
				{
					return false;
				}
				break;
			}
			case 'm':
			{
				if (cmd.equals("man"))
				{
					if (wr.isOpenAndNotEnd())
					{
						String n=wr.readWord(); // Name Or Index
						if (helpCommand(n)==false)
						{
							writeLine("Sorry, no more help on '"+cmd+"'");
						}
					}
					else
					{
						helpCommand(null);
					}
				}
				else
				{
					return false;
				}
				break;
			}
			case 'o':
				if (cmd.equals("op"))
				{
					String  n=wr.readName();
					if (n.equals("?"))
					{
						helpCommand(cmd);
					}
					else
					{
						if (isAdmin())
						{
							WorldBase ew=(WorldBase)worldBase;
							final int r = ew.addOp(n);
							if (r>0)
							{
								writeLine("Added "+n+" to op list");							
							}
							else
							{
								writeLine("Did not add "+n+" to op list");								
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
					return false;
				}
				break;
			case 'p':
			{
				if (cmd.equals("pwd"))
				{
					if (wr.isOpenAndNotEnd())
					{
						helpCommand(cmd);
					}
					else
					{
						writeLine(defaultObj.getNameAndPathInternal("/"));
					}
				}
				else
				{
					return false;
				}
				break;
			}
			case 's':
			{
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
				else if (cmd.equals("set"))
				{
					String n=wr.readWord(); // Name Or Index

					if (n.equals("?"))
					{
						helpCommand(cmd);
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
				else if (cmd.equals("setPw"))
				{
					String n=wr.readWord(); // Name Or Index
					if (isAdmin())
					{
						if (OpServer.isStringOkAsPw(n))
						{
							WorldBase ew=(WorldBase)worldBase;
							ew.gamePassword=n;
						}
						else
						{
							writeLine("That PW is to long, to short or contains characters not allowed");							
						}
					}
					else
					{
						writeLine("Permission denied");
					}
				}
				else
				{
					return false;
				}
				break;
			}
			case 'x':
				if (cmd.equals("xyzzy"))
				{
					writeLine("nothing happens");
				}	
				else
				{
					return false;
				}
				break;
			default:
			{
				return false;
			}
		}
		return true;
	}

	
    public static boolean isStringOkAsPlayerName(String name)
    {
    	return WordWriter.isNameOk(name,1); // We shall perhaps require player names to be longer eventually.
    }
    
	public static boolean isEmailAddressOk(String name)
	{
	    return WordWriter.isStringOk(name,"_.@-#$+~", 3); // Actually a lot more characters are allowed in an email address,  but for our own convenience we will allow only these.
	}
  
	public static boolean isStringOkAsPw(String name)
	{
	    return WordWriter.isStringOk(name,"_.@-#$+~", 1); // We shall definitely require passwords to be longer later.
	}

	
}
