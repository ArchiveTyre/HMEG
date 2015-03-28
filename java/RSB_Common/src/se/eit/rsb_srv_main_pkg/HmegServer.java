/*
HmegServer.java

Copyright (c) 2015 Henrik Björkman (www.eit.se/hb)

*/

package se.eit.rsb_srv_main_pkg;

import java.io.IOException;

import se.eit.rsb_package.*;
import se.eit.rsb_server_pkg.OpServer;
import se.eit.rsb_server_pkg.ServerTcpConnection;
import se.eit.citypvp_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;





public class HmegServer extends OpServer 
{

	int currentbuilding = 0;

	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return HmegServer.class.getSimpleName();	
	}	
	
    public void debug(String str)
	{
    	WordWriter.safeDebug(className()+"("+stc.getInfo()+"): "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}

    
    
	public HmegServer(GlobalConfig config, Player player,
			ServerTcpConnection stc) {
		super(config, player, stc);
		// TODO Auto-generated constructor stub
	}


	CityPvpRoom getCurrentRoom()
	{
		if (playerAvatar != null)
		{
			DbBase p = playerAvatar.getParent();
			if (p instanceof CityPvpRoom)
		    return (CityPvpRoom)(p);
		}
		return null;
	}
    public HmegWorld createAndStoreNewGame(String worldName)
    {
		DbRoot wdb=stc.findWorldsDb();
    	
    	// Create the new world
		HmegWorld newWorld = new HmegWorld(wdb, worldName, player.getName());
		
    	// Let it generate its contents
    	try {
			newWorld.lockWrite();
    		newWorld.generateWorld();
		}
		finally
		{
			newWorld.unlockWrite();
		}
		
    	
    	// Save the database with the new world	    		    	
		newWorld.saveRecursive(config);
    
		return newWorld;
    }
	
	@Override
    public String createAndStore(String worldName)
    {
   		createAndStoreNewGame(worldName);
		return worldName;
    }

	@Override
	public void join(DbBase bo)
	{
		if (bo instanceof HmegWorld)
		{
			joinHmeg((HmegWorld)bo);
		}
		else
		{
			error("not a HMEG game");
		}
	}
	

	protected void unknownCommand(String cmd)
	{
		debug("unknown command from client '"+player.getName()+"' "+cmd);
		stc.error("unknown command " + cmd);
	}

	
	protected void joinHmeg(HmegWorld w)
	{
		int notifyIdx=-1;
		try 
		{
			worldBase=w;
			defaultObj=w;


			// Find the nation for this player			

			playerAvatar = w.playerJoined(player);
			CityPvpAvatar avatar=(CityPvpAvatar)playerAvatar;


			
			if (playerAvatar==null)
			{
				error("no avatar");
			}
		
			notifyIdx=w.addNotificationReceiver(this, 0); // TODO: Since MirrorServer is handling the notifications it should add and remove subscription.
			
			
			// Tell client SW to draw the empire main window
			stc.writeLine("openHmeg "+w.getNameOrIndex());




			findAndSendDbUpdatesToClientAll();

			
			stc.writeLine("avatarId " + playerAvatar.getId()); 
			
			
			stc.writeLine("hmegShow");
			stc.writeLine("hmegShow");

			

  			int timeoutCounter=0; // If we get many timeouts in a row we will disconnect
			
			
			// loop waiting for input from client
	  		while(stc.isOpen())
	  		{

	  			try {
	  				final String r = stc.readLine(100);

	  				// This was not timeout so reset that counter
	  				timeoutCounter=0;
				
					WordReader wr=new WordReader(r);
					
					final String cmd=wr.readWord();					
	
					final char ch=cmd.charAt(0);
					
					switch(ch)
					{
					case 'k':
						if (cmd.equals("keypress"))
						{
							try
							{
								w.lockWrite();
								
								String m=wr.readString();
								//debug("keypress: "+m);
				     	        WordReader mr=new WordReader(m);
				     	        int k=mr.readInt();
				     			CityPvpRoom cr=getCurrentRoom();
				     			CityPvpEntity a = avatar;
				     			if (cr==null)
				     			{
				     				return;
				     			}

				     			
				     			// if in control room then 
				     			if (cr.getParent() instanceof CityPvpRoom)
				     			{
				     				CityPvpRoom cpr=(CityPvpRoom)cr.getParent();
				     				if (cr.isControlPanel(avatar.x,avatar.y))
				     				{
				     					a=cr;
				     					cr=cpr;
				     				}
				     			}
				     	        switch(k)
								{
					 	        case 'a':
					 	        	a.move(-1,0, 0);
									break;
					 	        case 'd':
					 	        	a.move(1,0, 0);
									break;
					 	        case 'w':
					 	        	a.move(0,-1, 0);
									break;
					 	        case 's':
					 	        	a.move(0,1, 0);
									break;
					 	       case 'g':
					 	    	    // This is the cheat button, when user press it give more resources
					 	    	    debug("giveItem");
					 	    	    avatar.giveItem(0, 1);
					 	        	avatar.giveItem(1, 2);
					 	        	avatar.giveItem(2, 3);
					 	        	avatar.giveItem(3, 4);
					 	        	avatar.giveItem(7, 5);
					 	        	avatar.giveItem(8, 6);
					 	        	avatar.giveItem(9, 7);
					 	        	avatar.giveItem(9, -8);
					 	        	avatar.giveItem(10, 8);
					 	        	avatar.giveItem(11, 9);
					 	        	avatar.giveItem(11, -8);
					 	        	avatar.giveItem(14, -9);
					 	        	
					 	        	avatar.fill_mineral+=1;
					 	        	avatar.fill_stone+=1;
					 	        	avatar.fill_wood+=1;
					 	        	avatar.postMessage("resources given");
					 	        	avatar.setUpdateCounter();
									break;	
					 	       /*case 'e':
					 	    	    // This is the open or close inventory button
					 	    	    debug("keypress e");
					 	    	    if (state==0)
					 	    	    {
					 	    	    	state = 2;
					 	    	    }
					 	    	    else
					 	    	    {
					 	    	    	state = 0;
					 	    	    }
					 	        	notify(-1,-2);
									break;	*/
						 	      case 'q':
						 	        	try
						 	        	{
						 	        		w.lockWrite();
						 	        		avatar.moveBetweenRooms(a);
							 	        	a = avatar;
							 	        	avatar.move(-1, 0, 0);
						 	        	}
						 	        	finally
						 	        	{
						 	        		w.unlockWrite();
						 	        	}		     	        	
										break;
								default: 
									break;
								}

							}
							finally
							{
								w.unlockWrite();
							}
							
						}
						else
						{
							unknownCommand(cmd);
						}
						break;
					
					case 'i':
						if (cmd.equals("inventoryClick"))
						{
							final String s=wr.readString();
							final int i=Integer.parseInt(s);
							
		     	        	CityPvpEntity obj = (CityPvpEntity) avatar.getObjFromIndex(i);
		     	        	if (obj !=null)
		     	        	{
			     	        	currentbuilding = obj.itemtype;
				     	        notify(-1,-2);
				     	        debug("currentbuilding set:"+currentbuilding);
		     	        	}
						}
						else
						{
							unknownCommand(cmd);
						}
						break;
					case 'm':
						if (cmd.equals("mirrorAck")) // TODO: This shall be in class MirrorServer
						{
							updateSequenceNumberReceived=wr.readInt();
						}
						else if (cmd.equals("mapClick"))
						{
		     	        	// Detta var ett klick på kartan

		     	        	/*int screenX=mouseX/xSize;
		     	        	int screenY=mouseY/xSize;
		     	        	int worldX=translateScreenToWorldX(avatar, screenX);
		     	        	int worldY=translateScreenToWorldY(avatar, screenY);*/
							
							final int worldX=wr.readInt();
							final int worldY=wr.readInt();
							
			     	        debug("mapClick:"+worldX+" "+worldY);

							try
							{
								w.lockWrite();
			     	        
			     	        
			     	        	System.out.println("BUILDHERE");
			     	        	if (currentbuilding == 0)
			     	        	{
			     	        		// Klienten vill ta bort något
			     	        		System.out.println("BUILDREMOVE ");
			     	        		int t = getCurrentRoom().getTile(worldX, worldY, 0);
			     	        		if (t != -1)
			     	        		{
			     	        			getCurrentRoom().changeTile(worldX, worldY,0 ,0);	
			     	        			avatar.fill_mineral +=  CityPvpBlock.loot_mineral(t);
			     	        			avatar.fill_stone +=  CityPvpBlock.loot_stone(t);
			     	        			avatar.fill_wood +=  CityPvpBlock.loot_wood(t);
			     	        		}
			     	        	}
				     	        else
			     	        	{
				     	        	// Klienten vill lägga till något
			     	        		System.out.println("BUILD");
			     	        		if (
			     	        				CityPvpBlock.cost_mineral(currentbuilding) <= avatar.fill_mineral 			 &&
			     	        				CityPvpBlock.cost_wood(currentbuilding) <= avatar.fill_wood 					 && 
			     	        				CityPvpBlock.cost_stone(currentbuilding) <= avatar.fill_stone					 )
			     	        				  
			     	        		{
			     	        			System.out.println("BUILDITTT");
			     	        			getCurrentRoom().changeTile(worldX, worldY,0 , currentbuilding);
			     	        			avatar.fill_mineral -= CityPvpBlock.cost_mineral(currentbuilding);
			     	        			avatar.fill_wood    -= CityPvpBlock.cost_wood(currentbuilding);
			     	        			avatar.fill_stone   -= CityPvpBlock.cost_stone(currentbuilding) ;
					     	        	avatar.setUpdateCounter();
			     	        		}
			     	        		else
			     	        		{
			     	        			debug("Client does not have enough resources");
					     	        	avatar.postMessage("not enough resources");
			     	        		}
			     	        	}
		     	        		
							}
							finally
							{
								w.unlockWrite();
							}
		     	        	
		     	        	//getCurrentRoom().changeTile(worldX, worldY, currentbuilding);
							
							
						}
						else
						{
							unknownCommand(cmd);
						}
						break;
					case 't':						
						if (cmd.equals("textMsg"))
						{
							debug("reply from client '"+player.getName()+"' "+cmd);
							final String txtMsg=wr.readString();
							String line=WordReader.removeQuotes(txtMsg);
							WordReader wr2=new WordReader(line);
							String cmd2=wr2.readWord();
							if (doCommand(cmd2, wr2)==false)
							{
								unknownCommand(cmd2);
							}
						}
						else
						{
							unknownCommand(cmd);
						}
						break;
						
					case 'c':
						if (cmd.equals("cancel"))
						{
							debug("reply from client '"+player.getName()+"' "+cmd);
							stc.close(); // TODO: should keep connection and instead return to menu.
							//throw new InterruptedException("cancel from client");							
						}
						else
						{
							unknownCommand(cmd);
						}
						break;

					default:
						unknownCommand(cmd);
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
				findAndSendDbUpdatesToClient();

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
			// TODO: Since MirrorServer is handling the notifications it should add and remove subscription. This cleanup should be moved to class MirrorServer somehow. 
			if (notifyIdx!=-1)
			{
				w.removeNotificationReceiver(notifyIdx);
				notifyIdx=-1;
			}
		    //close();
		}
		
		
		
	}
	
	
}