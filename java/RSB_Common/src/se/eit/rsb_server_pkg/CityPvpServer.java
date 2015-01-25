package se.eit.rsb_server_pkg;


import java.io.IOException;

import se.eit.rsb_package.*;
import se.eit.citypvp_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class CityPvpServer extends ServerBase implements NotificationReceiver {

	// Number of blocks shown on screen
	final int ScreenSizeX=16; 
    final int ScreenSizeY=16;
	
    // size of a standard block
	final int xSize=32; 
	final int ySize=32;
	
	int state = 0;
	CityPvpWorld w;
	int updateCounter=0;
	int currentbuilding = 0;
	int oldAvatarPosX = 0;
	int oldAvatarPosY = 0;
	
	
	//public int[][] map = new int[ScreenSizeX][ScreenSizeY];

	CityPvpAvatar avatar;
	//CityPvpRoom currentRoom=null;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return CityPvpServer.class.getSimpleName();	
	}	
	
    public static void debug(String str)
	{
    	WordWriter.safeDebug(className()+": "+str);
	}

    public static void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);
	}
	
	
	
	public CityPvpServer(GlobalConfig config, Player player, ServerTcpConnection cc)
	{
		super(config, player, cc);	
	}
	
    public CityPvpWorld createAndStoreNewGame(String worldName)
    {
		DbRoot wdb=stc.findWorldsDb();
    	
    	// Create the new world
		CityPvpWorld newWorld = new CityPvpWorld(wdb, worldName, player.getName());
		
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
	
    public String createAndStore(String worldName)
    {
   		createAndStoreNewGame(worldName);
		return worldName;
    }
	
	public CityPvpWorld findChatRoom(String name)
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
			
   		if (!(ro instanceof CityPvpWorld))
   		{
		   	stc.error("" + name+" was not a ChatWorld");
			return null;
		}


		return (CityPvpWorld)ro;				
	}

	CityPvpRoom getCurrentRoom()
	{
		if (avatar != null)
		{
			DbBase p = avatar.getParent();
			if (p instanceof CityPvpRoom)
		    return (CityPvpRoom)(p);
		}
		return null;
	}

	
	protected void join(DbBase bo)
	{
		if (bo instanceof CityPvpWorld)
		{
			join((CityPvpWorld)bo);
		}
		else
		{
			error("not a chat room");
		}
	}

	protected void join(CityPvpWorld bo)
	{
		try 
		{
			w=bo;
			
			String nap= w.getNameAndPath("/");
	
			stc.alertBox("joining_world", "joining city pvp "+ nap);
	
	        // Här registrerar serverns client process sig hos spelvärldsklassen att man vill veta när något hänt. 
			// om något har hänt då anropar spelvärldsklassen notify i denna klass
			w.addNotificationReceiver(this, 0);		
	
			
			avatar = (CityPvpAvatar)w.playerJoined(player);
			//currentRoom = (CityPvpRoom)avatar.getContainingObj();
					
			stc.writeLine("openCityPvp");  // This is received in on.js method onMessage.
			/*
			for(int x=0;x<CityPvpRoom.xSectors;x++)
			{	
				for(int y=0;y<CityPvpRoom.ySectors;y++)
				{
					stc.writeLine("AddImg "+CityPvpBlock.getBlockTexture(getCurrentRoom().map[x][y])+" "+x*xSize+" "+y*ySize+" "+xSize+" "+ ySize);
					//if (getCurrentRoom().map[x][y] == 1)
					//{
					//	stc.writeLine("AddImg ship "+x*xSize+" "+y*ySize+" "+xSize+" "+ ySize);
					//}
					//else if (getCurrentRoom().map[x][y]==2)
					//{
					//	stc.writeLine("AddImg ladder "+x*xSize+" "+y*ySize+" "+xSize+" "+ ySize);
					//}	
					map[x][y]=getCurrentRoom().map[x][y];
				}
			}	
			*/
			notify(-1,-2);
			
			w.messageFromPlayer(player, "joined");
			stc.writeLine("TextBoxAppend \""+"Hello "+player.getName()+"!\"");
	
			

		
		
	  		for(;;)
	  		{
	  			// Här väntar man på att nått ska komma från klienten
				String r = stc.readLine(15*60*1000);

				//debug("reply from client ("+player.getName()+") "+r);

				WordReader wr=new WordReader(r);
				
				String cmd=wr.readWord();
				
				
				
				// så här kollar vi vad klienten ville
				if (cmd.equals("cancel"))
				{
					// Klienten vill sluta
					break;
				}
				else if (cmd.equals("textMsg")) // klienten ville säga nått
				{
					String m=wr.readString();
					debug("textMsg: "+m);
					if (messageFromPlayer(m) != 0)
					{
						break;
					}					
				}
				else if (cmd.equals("mouseClick"))
				{
					// Klienten har klickat på något
					String m=wr.readString();
					//debug("mouseClick: "+m);
					
	     	        WordReader mr=new WordReader(m);
	     	        int mouseX=mr.readInt();
	     	        int mouseY=mr.readInt();


	     	        if (state == 2)
	     	        {
	     	        	// Om vi är i detta state så har klienten klickat i sitt inventory
		     	        int xf = mouseX/(xSize+4);     	        		
		     	        int yf = mouseY/(ySize+4);
		     	        
	     	        	
	     	        	
	     	        	CityPvpEntity obj = (CityPvpEntity) avatar.getObjFromIndex(xf+ScreenSizeX*yf);
	     	        	if (obj !=null)
	     	        	{
		     	        	currentbuilding = obj.itemtype;
			     	        notify(-1,-2);
			     	        debug("currentbuilding set:"+currentbuilding);
	     	        	}
	     	        
	     	        }
	     	        else
	     	        {	
	     	        	// Detta var nog ett klick på kartan
	     	        	int screenX=mouseX/xSize;
	     	        	int screenY=mouseY/xSize;
	     	        	int worldX=translateScreenToWorldX(avatar, screenX);
	     	        	int worldY=translateScreenToWorldY(avatar, screenY);
	     	        	System.out.println("BUILDHERE");
	     	        	if (currentbuilding == 0)
	     	        	{
	     	        		// Klienten vill ta bort något
	     	        		System.out.println("BUILDREMOVE ");
	     	        		int t = getCurrentRoom().getTile(worldX, worldY);
	     	        		if (t != -1)
	     	        		{
	     	        			getCurrentRoom().changeTile(worldX, worldY, 0);	
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
	     	        			getCurrentRoom().changeTile(worldX, worldY, currentbuilding);
	     	        			avatar.fill_mineral -= CityPvpBlock.cost_mineral(currentbuilding);
	     	        			avatar.fill_wood    -= CityPvpBlock.cost_wood(currentbuilding);
	     	        			avatar.fill_stone   -= CityPvpBlock.cost_stone(currentbuilding) ;
	     	        		}
	     	        		else
	     	        		{
	     	        			debug("Client does not have enough resources");
	     	        		}
	     	        	}
     	        		
	     	        		
	     	        	
	     	        	//getCurrentRoom().changeTile(worldX, worldY, currentbuilding);
	     	        
	     	        }
	     	        
				}
				else if (cmd.equals("mouseDrag"))
				{
					String m=wr.readString();
					debug("mouseDrag: "+m);
					if (messageFromPlayer(m) != 0)
					{
						break;
					}
				}
				else if (cmd.equals("keypress"))
				{
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
	     	        	a.move(-1,0);
						break;
	     	        case 'd':
	     	        	a.move(1,0);
						break;
	     	        case 'w':
	     	        	a.move(0,-1);
						break;
	     	        case 's':
	     	        	a.move(0,1);
						break;
	     	       case 'g':
	     	    	    // This is the cheat button, when user press it give more resources
	     	    	    avatar.giveItem(0, 1);
	     	        	avatar.giveItem(1, 2);
	     	        	avatar.giveItem(2, 3);
	     	        	avatar.giveItem(3, 4);
	     	        	avatar.giveItem(7, 5);
	     	        	avatar.giveItem(8, 6);
	     	        	avatar.giveItem(9, 7);
	     	        	avatar.giveItem(10, 8);
	     	        	avatar.giveItem(11, 9);
	     	        	avatar.fill_mineral+=1;
	     	        	avatar.fill_stone+=1;
	     	        	avatar.fill_wood+=1;
						break;	
	     	       case 'e':
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
						break;	
		     	      case 'q':
		     	        	try
		     	        	{
		     	        		w.lockWrite();
		     	        		avatar.moveBetweenRooms(a);
			     	        	a = avatar;
			     	        	avatar.move(-1, 0);
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
		}
		catch (IOException e) {
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

    
	public int messageFromPlayer(String msg)
	{
		w.messageFromPlayer(player, msg);
		
		return 0;
	}

	
	public void close()
	{
		w.removeNotificationReceiver(this);
		w.messageFromPlayer(player, "left");
		//super.close();
	}
	
	
	
	public int translateWorldToScreenX(CityPvpEntity a, int x) 
	{
		x = x - a.x +8;	
		return x;	
	}

	public int translateScreenToWorldX(CityPvpEntity a, int x) 
	{
		x = x + a.x - 8;	
		return x;	
	}
	public int translateWorldToScreenY(CityPvpEntity a, int y) 
	{
		y = y - a.y +8;	
		return y;	
	}

	public int translateScreenToWorldY(CityPvpEntity a, int y) 
	{
		y = y + a.y - 8;	
		return y;	
	}

	public int translateScreenToPixelX(int x) 
	{
    	return x*xSize;
	}
			
	public int translateScreenToPixelY(int y) 
	{
    	return y*ySize;
	}

	public String getEntityTypeName(int itemtype, int motion)
	{
		switch(itemtype)
		{
  		case 0: return "avatar_"+motion;
		case 6: return "city";
		default: return "unknown";
		}
	}
	
	
	
	
	
	//	return;
	
	// metoden här blir anropad av "spelvärldsklassen" när nått nytt har hänt.
	public void notify(int subscribersRef, int sendersRef)
	{
		try {

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
			
			
			
			// adda scrolling
			
			// Börja med att rensa hela skärmen (=canvas hos clienten)
			//if (sendersRef==-2)
			//map = new int[cr.xSectors][cr.ySectors];
			stc.writeLine("ClearTile "+0+" "+0+" "+ScreenSizeX*xSize+" "+ ScreenSizeY*ySize);
	
			/*if (state == 0)
			{
				stc.writeLine("ClearTile "+0+" "+0+" "+xSize+" "+ ySize);
			}*/
			
			// Detta är för text meddelanden
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
						WordWriter ww = new WordWriter(stc.getTcpConnection());
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
		
			//stc.writeLine("clear");
			
			// Rita en ram runt rummet, hela rummet kanske inte syns på kartan 
			/*for(int x=0;x<cr.xSectors;x++)
			{	
				
				
			}*/
	
		
			// kolla vad som ändrats i w.map jämfört med egna kopian av map, eller gör man det?
			// Rummets egna "rutor"
			for(int x=0;x<cr.xSectors;x++)
			{	
				for(int y=0;y<cr.ySectors;y++)
				{
					int sx=translateWorldToScreenX(a, x);
					int sy=translateWorldToScreenY(a, y);
					if ((sx>=0) && (sx<ScreenSizeX) && (sy>=0) && (sy<ScreenSizeY))
					{				
						//if (map[sx][sy]!=cr.map[x][y])
						{
							//map[sx][sy]=cr.map[x][y];
							if (cr.map[x][y]==0)
							{
								stc.writeLine("EmptyTile "+sx*xSize+" "+sy*ySize+" "+xSize+" "+ ySize);
							}
							else
							{
								stc.writeLine("AddImg "+CityPvpBlock.getBlockTexture(cr.map[x][y])+" "+sx*xSize+" "+sy*ySize+" "+xSize+" "+ ySize);
							}
						}
					}
					else
					{
						// Denna del av kartan är utanför skärmen/canvas hos klienten och kan inte ses
						//stc.writeLine("TileOutsideMap "+sx*xSize+" "+sy*ySize+" "+xSize+" "+ ySize);
					}
				}
			}	

			
			
			
			// Här skriver man ut entitys
			DbBase[] list = cr.getListOfSubObjectsThreadSafe();
			
			for(int i=0;i<list.length;i++)
			{
				if (list[i] instanceof CityPvpEntity)
				{
					CityPvpEntity e = (CityPvpEntity)list[i];
					
					int sx=translateWorldToScreenX(a, e.x);
					int sy=translateWorldToScreenY(a, e.y);
					
					int px=translateScreenToPixelX(sx);
					int py=translateScreenToPixelY(sy);
	
					if ((sx>=0) && (sx<ScreenSizeX) && (sy>=0) && (sy<ScreenSizeY))
					{
	
						int eXSize=1;
						int eYSize=1;
						if(e instanceof CityPvpRoom)
						{
							CityPvpRoom cpr=(CityPvpRoom)e;
							eXSize=cpr.outerX;
							eYSize=cpr.outerY;
							/*
							int scalesizeFactorX=xSize*eXSize/cpr.xSectors;
							int scalesizeFactorY=ySize*eYSize/cpr.ySectors;
							
							// Test code
							//cpr.getImage();
							
							int x= 0;
							while(cpr.xSectors > x)
							{
								int y = 0;
								while(y < cpr.ySectors)
								{								
									stc.writeLine("AddImg "+CityPvpBlock.getBlockTexture(cpr.map[x][y])+" "+(px+scalesizeFactorX*x)+" "+(py+scalesizeFactorY*y)+" "+scalesizeFactorX+" "+ scalesizeFactorY);
									//stc.writeLine("AddImg "+getEntityTypeName(e.itemtype) + " "+translateScreenToPixelX(sx)+" "+translateScreenToPixelY(sy)+" "+xSize*eXSize+" "+ ySize*eYSize);
									//cpr.map
									y++;
								}
									
								x++;
							}
							*/
							{
							final String imageName=getEntityTypeName(e.itemtype, e.state);
							stc.writeLine("AddImg "+imageName + " "+px+" "+py+" "+xSize*eXSize+" "+ ySize*eYSize);
							}
							
							{
							final String imageName=cpr.prepareImageAndGetName(config);
							stc.writeLine("AddImg "+imageName + " "+px+" "+py+" "+xSize*eXSize+" "+ ySize*eYSize);
							}
						}
						
						else
						{	
							final String imageName=getEntityTypeName(e.itemtype, e.state);
							stc.writeLine("AddImg "+imageName + " "+px+" "+py+" "+xSize*eXSize+" "+ ySize*eYSize);
						}
	
						// TODO  Display data of cpm:
						
						
						
					
						
						
						
						
	 				    // mark this position on screen as needing update.
						//map[sx][sy]=-1;
					}
					else
					{
						//debug("outside screen "+e.getId());
					}
				}			
				
			}
	
			if (state==2)	
			{
				// Draw also the inventory list
				drawState2();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.close();
		}

		
	}
	
	
	public void drawState2() throws IOException
	{
		DbBase[] list = avatar.getListOfSubObjectsThreadSafe();
		
		
		for(int i=0;i<list.length;i++)
		{
			if (list[i] instanceof CityPvpEntity)
			{
				CityPvpEntity e = (CityPvpEntity)list[i];

				
				
				int x = (i%10)*(xSize+4) ;
				int y = (i/10)*(ySize+4);
				
				
					
				stc.writeLine("AddImg frame "+(x)+" "+(y)+" "+(xSize+4)+" "+ (ySize+4));
				//	stc.writeLine("ClearTile "+e.Oldx*xSize+" "+e.Oldy*ySize+" "+xSize+" "+ ySize);

				//	stc.writeLine("AddImg ship "+e.Oldx*xSize+" "+e.Oldy*ySize+" "+xSize+" "+ ySize);
				
				String n = CityPvpBlock.getBlockTexture(e.itemtype);
				stc.writeLine("AddImg "+n+" "+(x+2)+" "+(y+2)+" "+xSize+" "+ ySize+" "+e.stack);
				
				//stc.writeLine("FillText "+e.stack+" "+(x+2)+" "+(y+2));
		
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

}
