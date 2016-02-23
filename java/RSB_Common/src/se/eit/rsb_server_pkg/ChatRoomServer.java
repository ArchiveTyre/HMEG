/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.rsb_server_pkg;


import java.io.IOException;

import se.eit.rsb_package.*;
import se.eit.rsb_server_pkg.ServerBase;
import se.eit.db_package.*;
import se.eit.web_package.*;


public class ChatRoomServer extends ServerBase implements NotificationReceiver {

	ChatRoomWorld w;
	int updateCounter=0;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ChatRoomServer.class.getSimpleName();	
	}	
	
    public void debug(String str)
	{
    	WordWriter.safeDebug(className()+"("+stc.getTcpConnection().getTcpInfo()+"): "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}
	
	
	
	public ChatRoomServer()
	{
		super();	
	}
	
	/*
    public ChatRoomWorld createAndStoreNewGame(String worldName)
    {
		DbSubRoot wdb=stc.findOrCreateGameDb();
    	
    	// Create the new chat room
    	ChatRoomWorld newMapAndNations = new ChatRoomWorld(wdb, worldName, player.getName());
		
    	// Let it generate its contents
    	try {
			newMapAndNations.lockWrite();
    		newMapAndNations.generateSelf();
		}
		finally
		{
			newMapAndNations.unlockWrite();
		}
		
    	
    	// Save the database with the new world	    		    	
		newMapAndNations.saveRecursive(config);
    
		return newMapAndNations;
    }
	
    public String createAndStore(String worldName)
    {
   		createAndStoreNewGame(worldName);
		return worldName;
    }
    */
	
	public ChatRoomWorld findChatRoom(String name)
	{		
    	if (name==null)
    	{
            debug("findChatRoom: name was null");
    		return null;
    	}
    	
    	DbSubRoot worldsDatabase = stc.findOrCreateGameDb();

		if (worldsDatabase==null)
		{
            stc.error("world database not found");
			return null;
		}
    	
		DbSubRoot ro = worldsDatabase.findDb(name);
		

		if (ro==null)
		{
		   	debug("world "+ name+" was not found");
		   	return null;
		}
			
   		if (!(ro instanceof ChatRoomWorld))
   		{
		   	stc.error("" + name+" was not a ChatRoomWorld");
			return null;
		}


		return (ChatRoomWorld)ro;				
	}
	
	public void join(DbBase bo)
	{
		if (bo instanceof ChatRoomWorld)
		{
			join((ChatRoomWorld)bo);
		}
		else
		{
			debug("not a chat room");
		}
	}

	protected void join(ChatRoomWorld bo)
	{
		try 
		{
			w=bo;
			
			String nap= w.getNameAndPath("/");
	
			stc.alertBox("joining_world", "joining chat room "+ nap);
	
			stc.writeLine("openChatRoom");  // This command is handled in on.js method onMessage
	
			stc.writeLine("TextBoxAppend \""+"Hello "+player.getName()+"!\"");
	
	
			w.addNotificationReceiver(this, 0);		
			w.messageFromPlayer(player, "joined");

	  		for(;;)
	  		{
				String r = stc.readLine(15*60*1000);
				
				WordReader wr=new WordReader(r);
				
				String cmd=wr.readWord();					
				
				
				debug("reply from client ("+player.getName()+") "+cmd);
				
				if (cmd.equals("cancel"))
				{
					break;
				}
				else if (cmd.equals("textMsg"))
				{
					String m=wr.readString();
					debug("textMsg: "+m);
					if (messageFromPlayer(m) != 0)
					{
						break;
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
	

    // We get here when client wants to enter chat room
	/*
    protected void join(String chatRoomName)
    {
        debug("chatRoom: worldName \"" + chatRoomName+"\" playerName \"" + player.getName() +"\"");
        
        if (player!=null)
        {
	  		w = findChatRoom(chatRoomName);


	  		if (w==null)
	  		{
	  			createAndStoreNewGame(chatRoomName);
		  		w = findChatRoom(chatRoomName);  	
	  		}

	  		if (w!=null)
	        {	  		
	  			join(w);
	        }
	        else
	        {
	        	stc.alertBox("world_not_found", "chat room found "+ chatRoomName);
	        }   
        }
        else
        {
    		stc.alertBox("login_first", "You have not logged in yet.");       	
        }
	        
        debug("chatRoom end");
    }
    */

    
	public int messageFromPlayer(String msg)
	{
		w.messageFromPlayer(player, msg);
		
		return 0;
	}

	
	public void close()
	{
		w.removeNotificationReceiver(this);
		w.messageFromPlayer(player, "left");
	}
	
	public void notifyRef(int subscribersRef, int sendersRef)
	{
		for (;;)
		{
			String msg=w.getMsg(updateCounter);
			
			if (msg!=null)
			{
				updateCounter++;
				if (msg.equals(""))
				{
					// ignore empty
				}
				else
				{
					//cc.writeLine("TextBoxAppend \""+msg+"!\"");
					WordWriter ww = new WordWriterWebConnection(stc.getTcpConnection());
					ww.writeName("TextBoxAppend");
					ww.writeString(msg);
					ww.flush();
				}
			}
			else
			{
				break;
			}

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

	@Override
	public WorldBase createWorld() {
		return new ChatRoomWorld();
	}

	@Override
	public void joinWorld() {	
	}

	/*
	@Override
	public boolean need2dSupport()
	{
		return true;
	}

	@Override
	public boolean need3dSupport()
	{
		return false;
	}*/
	
}
