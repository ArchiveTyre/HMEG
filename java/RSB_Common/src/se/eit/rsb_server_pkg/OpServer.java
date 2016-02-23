// OpServer.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_server_pkg;

import java.io.IOException;

import se.eit.db_package.DbBase;
import se.eit.rsb_package.WorldBase;
import se.eit.rsb_server_pkg.MirrorServer;
import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;

public abstract class OpServer extends MirrorServer {

	protected DbBase defaultObj;

	
	public OpServer() {
		super();
	}


	
	public void writeLine(String str) throws IOException
	{
		// The message can be sent directly to a console.
		// Or indirectly by posting a message to the state that the player has.

		if (str.length()>0)
		{
			debug(str);
		}
		
		WordWriter ww=new WordWriter();
		
		ww.writeWord("consoleMessage");
		ww.writeString(str);
		
		// To console:
		String m=ww.getString();
		stc.writeLine(m); 
		

		// To state messages:
		//postMessage(str);
	}

	
	protected void unknownCommand(String cmd, WordWriter ww) throws IOException
	{	
		ww.writeLine("unknown command '"+cmd+"', ");						
		ww.writeLine("try help");
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

	
	protected void typicalHelpPath(String cmd, String msg, WordWriter ww) throws IOException
	{
		ww.writeLine(msg);							
		ww.writeLine("Usage:");
		ww.writeLine("  "+cmd+" <pathOrName>");
		ww.writeLine("");
		ww.writeLine("  Where:");
		ww.writeLine("    <pathOrName> is an object name, index number, ");
		ww.writeLine("                 object id or combinations of those.");							
		ww.writeLine("                 Object IDs are a number prefixed by ~");		
		ww.writeLine("                 A path can look like: /0/city");
	}
	
	
	protected boolean helpCommand(String cmd, WordReader wr, WordWriter ww, String hist) throws IOException
	{
		if ((cmd==null) || (cmd.length()==0))
		{
			if (isAdmin())
			{
				ww.writeLine("Op/admin commands:");
				ww.incIndentation();
				ww.writeLine("cat : Gives detailed information about an object");
				ww.writeLine("cd : Change the default object");
				ww.writeLine("cmd : Send a command to an object");
				ww.writeLine("deop : Remove player from ops list");
				ww.writeLine("get : Get some information about an object");
				ww.writeLine("go : Start the game or change the game speed");
				ww.writeLine("ls : List child objects");
				ww.writeLine("mute : mute a player");
				ww.writeLine("op : Add player to ops list");
				ww.writeLine("pause : pause the game (use go to start again)");
				ww.writeLine("pwd : Print working default object");
				ww.writeLine("set : Change the value of some property of an object");
				ww.writeLine("setSrvPw: Set password needed for joining ");
				ww.writeLine("stat : Show some general information about an object");
				ww.writeLine("");
				if (hist!=null)
				{
					ww.writeLine("For more help about a command try:");
					ww.writeLine("  "+hist+" <command>");
					ww.writeLine("Example:");
					ww.writeLine("  "+hist+" op");
				}
				ww.decIndentation();
			}
			else
			{
				//ww.writeLine("you need to be admin (alias op) to use sc (alias op) commands");
			}
		}
		else if (cmd.equals("cd"))
		{
			typicalHelpPath("cd", "Changes the default object", ww);
		}
		else if (cmd.equals("cat"))
		{
			typicalHelpPath("cat", "Gives detailed information about an object", ww);
		}
		else if (cmd.equals("cmd"))
		{
			ww.writeLine("Sends a command to an object");					
			ww.writeLine("For example: cmd ~81 say hello");
			ww.writeLine("Will make object ~81 say hello");
			
		}
		else if (cmd.equals("dump"))
		{
			typicalHelpPath("dump", "Gives detailed information about an object in a short form", ww);
		}
		else if (cmd.equals("deop"))
		{
			ww.writeLine("Remove players from the list of game administrators.");
		}
		else if (cmd.equals("get"))
		{
			ww.writeLine("Get the value of some property of an object.");							
			ww.writeLine("Usage: get <nameOrPath> <nameOfProperty>");							
			ww.writeLine("");
			ww.writeLine("Use the cat command to see which properties");
			ww.writeLine("an object has and the current values.");							
			ww.writeLine("See also set command.");	
			ww.writeLine("");
			ww.writeLine("Only op can use this command.");	
		}
		else if (cmd.equals("ls"))
		{
			typicalHelpPath("ls", "List all objects stored inside/under the given object", ww);
		}
		else if (cmd.equals("op"))
		{
			ww.writeLine("Add a player to the list of game ops/administrators.");					
			ww.writeLine("Only ops can issue this command.");
			ww.writeLine("To see the op list try:");
			ww.writeLine("  cat /");
			ww.writeLine("or:");
			ww.writeLine("  get / ops");
			ww.writeLine("");
			typicalHelpPath("op", "", ww);
		}
		else if (cmd.equals("setSrvPw"))
		{
			ww.writeLine("Make the game private. Players must enter the ");
			ww.writeLine("password before they can get a place in the game.");					
		}
		else if (cmd.equals("pwd"))
		{
			ww.writeLine("Shows which object is the current default object.");					
			ww.writeLine("Many other commands can use the current object by using a '.'");	
			ww.writeLine("Example: cat .");	
		}
		else if (cmd.equals("set"))
		{
			ww.writeLine("Change the value of some property of an object.");							
			ww.writeLine("Usage: set <nameOrPath> <nameOfProperty> <value>");							
			ww.writeLine("");
			ww.writeLine("Use the cat command to see which properties");
			ww.writeLine("an object has and the current values.");							
			ww.writeLine("See also get command.");	
			ww.writeLine("");
			ww.writeLine("Only op can use this command.");	
		}
		else
		{
			ww.writeLine("No help for command "+cmd);
			return false;
		}
		return true;
	}
	

	
	// Returns true if the command was found and performed.
	protected boolean doCommand(String cmd, WordReader wr, WordWriter ww) throws IOException
	{	
		final char ch=cmd.charAt(0);
		boolean cmdFound=false;
		
		switch(ch)
		{
			/*case 'a':
				if (cmd.equals("addPlayerPref"))
				{		
					String prefName = wr.readWord();
					String prefValue = wr.readString();
					player.addPlayerPrefThreadSafe(prefName, prefValue);						
					cmdFound=true;					
				}
				break;*/

			case 'c':
			{
				if (cmd.equals("cd"))
				{
					String n=wr.readWord(); // Name Or Index
					if (n.length()>0)
					{
	
						DbBase newDefaultObj=findObjectByNameOrIndex(n);
									
						if (newDefaultObj != null)
						{
							debug("newDefaultObj "+newDefaultObj.getNameOrIndex());
							defaultObj=newDefaultObj;
						}
						else
						{
							ww.writeLine("not found");
						}
					}
					else
					{
						defaultObj=worldBase;
					}
					cmdFound = true;
				}
				else if (cmd.equals("cat"))
				{
					String n=wr.readWord(); // Name Or Index
					DbBase d= findObjectByNameOrIndex(n);
					d.listInfo(ww);
					cmdFound = true;
				}
				else if (cmd.equals("cmd"))
				{
					if (isAdmin())
					{
						String n=wr.readWord(); // Name Or Index
						DbBase d= findObjectByNameOrIndex(n);
						String str=wr.readWord();
						d.interpretCommand(str, wr, ww);
					}
					else
					{
						ww.writeLine("Permission denied");
					}
					cmdFound = true;
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
							ww.writeLine("Removed all but "+player.getName()+" from op list");
						}
						else
						{
							WorldBase ew=(WorldBase)worldBase;
							final int r = ew.rmOp(n);
							if (r>0)
							{
								ww.writeLine("Removed "+n+" from op list");							
							}
							else
							{
								ww.writeLine("Did not find "+n+" in op list");								
							}
						}
					}
					else
					{
						ww.writeLine("Permission denied");
					}
					cmdFound = true;
				}	
				else if (cmd.equals("dump"))
				{
					String n=wr.readWord(); // Name Or Index
					DbBase d= findObjectByNameOrIndex(n);
					ww.writeLine(d.toString());
					cmdFound = true;
				}
				break;
			}
			case 'g':
			{
				if (cmd.equals("get"))
				{
					String n=wr.readWord(); // Name Or Index

					if (isAdmin())
					{
						DbBase d= findObjectByNameOrIndex(n);
						if (d!=null)
						{
							String valueTag=wr.readWord();
							if (d.getInfo(ww, valueTag, wr)<0)
							{
								ww.writeLine("did not find tag '"+valueTag+"'");
							}
							else
							{
								ww.writeLine("");
							}
						}
						else
						{
							ww.writeLine("did not find object '"+n+"'");
						}
					}	
					else
					{
						ww.writeLine("Permission denied");
					}
					cmdFound = true;
				}
				break;
			}

			case 'h':
			{
				if (cmd.equals("help"))
				{
					String subCmd=wr.readWord(); // Name Or Index

					return helpCommand(subCmd, wr, ww, cmd);
				}
				break;
			}
			case 'l':
			{
				if (cmd.equals("ls"))
				{				
					String n=wr.readWord(); // Name Or Index
					final DbBase d= findObjectByNameOrIndex(n);
		
					final DbBase sid[]=d.getListOfSubObjectsThreadSafe();
			
					for(int i=0;i<sid.length; i++)
					{
						ww.writeLine(sid[i].toShortFormatedString());
					}
					cmdFound = true;
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
						if (helpCommand(n, wr, ww, "man")==false)
						{
							ww.writeLine("Sorry, no more help on '"+cmd+"'");
						}
					}
					else
					{
						helpCommand("man", wr, ww, null);
					}
					cmdFound = true;
				}
				break;
			}
			case 'o':
				if (cmd.equals("op"))
				{
					String  n=wr.readName();
					if (isAdmin())
					{
						WorldBase ew=(WorldBase)worldBase;
						final int r = ew.addOp(n);
						if (r>0)
						{
							ww.writeLine("Added "+n+" to op list");							
						}
						else
						{
							ww.writeLine("Did not add "+n+" to op list");								
						}

					}
					else
					{
						ww.writeLine("Permission denied");
					}
					cmdFound = true;
				}	
				break;
			case 'p':
			{
				if (cmd.equals("pwd"))
				{
					if (wr.isOpenAndNotEnd())
					{
						helpCommand(cmd, wr, ww, null);
					}
					else
					{
						ww.writeLine(defaultObj.getNameAndPathInternal("/"));
					}
					cmdFound = true;
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
						typicalHelpPath(cmd, "Show some general information about an object.", ww);
					}
					else
					{
						DbBase d= findObjectByNameOrIndex(n);
						ww.writeLine(d.getObjInfoPathNameEtc());
					}
					cmdFound = true;
				}
				else if (cmd.equals("set"))
				{
					String n=wr.readWord(); // Name Or Index

					if (isAdmin())
					{
						DbBase d= findObjectByNameOrIndex(n);
						if (d!=null)
						{
							String valueTag=wr.readWord();
							if (d.setInfo(wr, valueTag)<0)
							{
								ww.writeLine("did not find tag '"+valueTag+"'");
							}
						}
						else
						{
							ww.writeLine("did not find object '"+n+"'");
						}
					}	
					else
					{
						ww.writeLine("Permission denied");
					}
					cmdFound = true;
				}
				else if (cmd.equals("setSrvPw"))
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
							ww.writeLine("That PW is to long, to short or contains characters not allowed");							
						}
					}
					else
					{
						ww.writeLine("Permission denied");
					}
					cmdFound = true;
				}
				break;
			}
			case 'x':
				if (cmd.equals("xyzzy"))
				{
					ww.writeLine("nothing happens");
					cmdFound = true;
				}	
				break;
			default:
			{
				// Nothing
			}
		}
		return cmdFound;
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
