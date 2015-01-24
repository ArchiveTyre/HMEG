package se.eit.rsb_server_pkg;


import java.io.IOException;

import se.eit.rsb_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class MibServer extends ServerBase implements NotificationReceiver {

	//int updateCounter=0;
	
	//DbRoot defaultMibEntry=null;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return MibServer.class.getSimpleName();	
	}	
	
    public static void debug(String str)
	{
    	WordWriter.safeDebug(className()+": "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}
	
	
	
	public MibServer(GlobalConfig config, Player player, ServerTcpConnection cc)
	{
		super(config, player, cc);
	
	}
	
    public MibWorld createAndStoreNewGame(String worldName)
    {
		DbRoot wdb=stc.findWorldsDb();

		MibWorld newMib = new MibWorld(wdb, worldName, player.getName());
		
    	
    	// Save the database with the new world	    		    	
		newMib.saveRecursive(config);
    
		return newMib;
    }
	
    
    public String createAndStore(String worldName)
    {
    	if (worldName!=null)
    	{
    		createAndStoreNewGame(worldName);
    	}
		
		return worldName;
    }

    
	public MibWorld findMib(String name)
	{		
    	if (name==null)
    	{
            debug("findChatRoom: name was null");
    		return null;
    	}
    	
    	DbRoot worldsDatabase = stc.findWorldsDb();

		if (worldsDatabase==null)
		{
            stc.error("world database not found");
			return null;
		}
    	
		DbRoot ro = worldsDatabase.findDb(name);
		

		if (ro==null)
		{
		   	debug("world "+ name+" was not found");
		   	return null;
		}
			
   		if (!(ro instanceof MibWorld))
   		{
		   	stc.error("" + name+" was not a MibWorld");
			return null;
		}


		return (MibWorld)ro;				
	}
	
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

	public static void append(WordWriter ww, String msg)
	{
		ww.writeName("TextBoxAppend");
		ww.writeString(msg);
	}

	public static void prompt(ServerTcpConnection cc)
	{
		WordWriter ww = new WordWriter(cc.getTcpConnection());
		ww.writeName("TextBoxPrompt");
		ww.endLine();
	}

	
	protected void join(DbBase bo)
	{
		//w.addNotificationReceiver(this, 0);		
		//defaultMibEntry = ro;
		
		try 
		{
			
			stc.writeLine("openTextBox");
			
	  		for(;;)
	  		{
	  			prompt(stc);
	  			
				String r = stc.readLine(15*60*1000);
				
				WordReader wr=new WordReader(r);
				
				String cmd=wr.readWord();					

				
				
				debug("reply from client ("+player.getName()+") "+cmd);
				
				if (cmd.equals("cancel"))
				{
					break;
				}
				/*else if (cmd.equals("ls_restart"))
				{
					//String m=wr.readString();
					
					final DbRoot db=w.getDbRoot();
					db.lockRead();
					try
					{
						DbBase bo=w.iterate(null);
						while(bo!=null)
						{
							System.out.println("  "+bo.getNameOrIndex());  //+"  "+no.getType()
							WordWriter ww = new WordWriter(cc.getTcpConnection());
							ww.writeLine("> "+bo.getName());
							bo=w.iterate(bo);
						}
					}
					finally
					{
						db.unlockRead();
					}
					
					
		    		PrintWriter pw=new PrintWriter(System.out);
		    		w.listSubObjects(pw, "  ");
		    		pw.endLine();
				
				}
				else if (cmd.equals("ls_next"))
				{

				}*/
				else if (cmd.equals("textMsg"))
				{					
					String line=WordReader.removeQuotes(wr.readString());
					WordReader wr2=new WordReader(line);					
					String subCmd=wr2.readWord();					

					debug("textMsg subCmd: "+subCmd);
					if (subCmd.equals("ls"))
					{			
						DbBase sid[]=bo.getListOfSubObjects();

						for(int i=0;i<sid.length; i++)
						{
							WordWriter ww = new WordWriter(stc.getTcpConnection());
							final String name=sid[i].getNameOrIndex();
							append(ww, name);
							ww.endLine();
						}
						
					}
					else if (subCmd.equals("cd"))
					{
						DbBase sid[]=bo.getListOfSubObjects();
		    			String noi=wr2.readWord();
		    			DbBase newDefaultObj=null;
		    			
		    			if (noi.equals(".."))
		    			{
		    				newDefaultObj=bo.getContainingObj();
		    			}
		    			else if (noi.equals("."))
		    			{
		    				newDefaultObj=bo;
		    			}
		    			else
		    			{		    			
		    				newDefaultObj=findRelativeFromNameOrIndex(noi, sid);
		    			}

			    		if (newDefaultObj != null)
			    		{
			    			debug("newDefaultObj "+newDefaultObj.getNameOrIndex());
			    			bo=newDefaultObj;
			    		}
			    		else
			    		{
			    			debug("newDefaultObj not found");
			    		}
						
					}
					else if (subCmd.equals("cat"))
					{
						WordWriter ww = new WordWriter(stc.getTcpConnection());
	    		
						StringBuffer sb=new StringBuffer();
						WordWriter sbww = new WordWriter(sb);
						bo.listInfo(sbww, "  ");				
						append(ww, sb.toString());

						ww.endLine();
					}
					else if (subCmd.equals("dump"))
					{
						WordWriter ww = new WordWriter(stc.getTcpConnection());
						append(ww, bo.toString());
						ww.endLine();
					}
					else if (subCmd.equals("type"))
					{
						WordWriter ww = new WordWriter(stc.getTcpConnection());
						append(ww, bo.getType());
						ww.endLine();
					}
					else if (subCmd.equals("show"))
					{
						WordWriter ww = new WordWriter(stc.getTcpConnection());
						append(ww, bo.getObjInfoPathNameEtc());
						ww.endLine();
					}
			    	else if (cmd.equals("pwd"))
			    	{
						WordWriter ww = new WordWriter(stc.getTcpConnection());
						ww.writeName("TextBoxAppend");
						//System.out.println("  "+defaultObj.getNameAndPath("/")+getIdIfAny(defaultObj));
						ww.writeString(bo.getNameAndPath("/"));		
						ww.endLine();
			    	}
					else
					{
						WordWriter ww = new WordWriter(stc.getTcpConnection());
						ww.writeName("TextBoxAppend");
						ww.writeString("unknown command, available commands: ");						
						append(ww, "cat");
						append(ww, "cd");
						append(ww, "ls");
						append(ww, "pwd");
						ww.endLine();
					}									
				}
				else
				{
					stc.error("unknown command " + cmd);
					break;
				}						
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
		    close();
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
		super.close();
	}
	

	public void notify(int subscribersRef, int sendersRef)
	{
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
