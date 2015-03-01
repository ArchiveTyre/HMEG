// PlayerConnectionThread.java
//
// Copyright (C) 2012 Henrik Björkman www.eit.se
//
// This is the thread that handles the connection from a client.
// One instance of this class per client.
//
//
// Written using this instruction:
// http://courseweb.xu.edu.ph/courses/ics10/tutorials/Java_Unleashed_Second_Edition/ch25.htm#UsingUDPIP
//
// History:
// Created by Henrik Björkman 1997-05-03
// Moved to own file. Henrik 1999-09-08
// Changed to use swing. Henrik 2012-10-14 
// Changed to udp. Henrik 2012-10-16
// Adapted for use with RSB. Henrik 2013-05-04


package se.eit.rsb_server_pkg;

import java.io.IOException;
import java.util.Random;

import se.eit.rsb_package.*;
import se.eit.rsb_srv_main_pkg.ChatRoomServer;
import se.eit.rsb_srv_main_pkg.CityPvpServer;
import se.eit.rsb_srv_main_pkg.GlobalConfig;
import se.eit.rsb_srv_main_pkg.HmegServer;
import se.eit.rsb_srv_main_pkg.MibServer;
import se.eit.rsb_srv_main_pkg.TextAdventureServer;

// Should investigate if these imports can be avoided.
import se.eit.citypvp_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;
import se.eit.TextAdventure.*;



//This class is the thread that handles all communication with a client
public class PlayerConnectionThread extends Thread implements WebSocketConnection
{
	static final long desired_frame_rate = 0; // actually milliseconds per frame, if zero run as fast as possible bit a small sleep/wait only.

	//static final boolean send_everything=false; // true here was used for testing, not used now, this flag can be removed eventually
	
    protected GlobalConfig config;
	
	protected ServerTcpConnection stc;
	Player player;
    String worldName=null;
    
    static Random generator = new Random();

    
    public MyBlockingQueue<String> msgQueue=new MyBlockingQueue<String>(32);

	

	public void error(String s)
	{
		stc.error("PlayerConnectionThread: "+s);
	}

	  
    public static void debug(String str)
	{
    	WordWriter.safeDebug("PlayerConnectionThread: "+str);
	}

	public static void println(String s)
	{
	    System.out.println("PlayerConnectionThread: "+s);
	}

    
	// Remember to start the thread after creating the instance.
  	public PlayerConnectionThread(GlobalConfig config, WebConnection tc, DbRoot db)   
  	{
  		this.config = config;
  		this.stc = new ServerTcpConnection(db, tc);
	    debug("PlayerConnectionThread: "+tc.getInfo());	    
  	}
  
  	/*
  static int my_abs(int a)
  {
	  if (a<0)
	  {
		  return -a;
	  }
	  	  
	  return a;
  }
  */

  
  
	//To get the first word in a string.
	public static String getFirstWord(String str) 
	{
	    int i=0;
	    while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {i++;}
	    return(str.substring(0,i));
	}
	  
	  
	//To get a string without the first n words in string str.
	public static String skipWords(String str, int n)
	{
	    int i=0;
	    
	    while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {i++;}
	    
	    while (n>0)
	    {
	      while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {i++;}
	      while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {i++;}
	      n--;
	    }
	    
	    return(str.substring(i));
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

	
	public boolean isOpen()
	{
		return (stc!=null) && (stc.isOpen());
	}
	
 
 	

	
	
	public Player findPlayer(String name)
	{
    	if (name==null)
    	{
            error("name was null");
    		return null;
    	}
    	
    	DbRoot playersDatabase = stc.findOrCreatePlayersDb();
    	
		DbRoot ro = playersDatabase.findDb(name);
		

		if (ro==null)
		{
		   	debug("player "+ name+" was not found");
			return null;
		}
			
		if (ro instanceof Player)
		{
			return (Player)ro;				
		}
		
		error("\"" + name+"\" was not a player");
		return null;
	}
	

	
    
    public void playerLogin()
    {
  		String name=null;
  		int n=0;
    	debug("player login");
    	
        while(++n<100) 
		{
        	name = stc.promptString("enter_player_name", "enter your name");
        	
        	if (name==null)
        	{
        		break;
        	}
        	
        	Player p=findPlayer(name);
        	  			
    		if (p!=null)
    		{
    			
    			if (p.password!=null)
    			{
    				String pw = stc.promptString("enter_player_pw","give password");
    		    		    			
    				if (pw==null)
    				{
    					break;
    				}
    				else if (p.password.equals(pw))
	    			{
	    				this.player=p;
	    				stc.alertBox("login_ok", "login ok");
	    				return;
	    			}
	    			else
	    			{
	    				stc.alertBox("wrong_password", "wrong pw");
	    				return;
	    			}
    			}
    			else
    			{
    				this.player=p;
    				return;
    			}
    		}
    		else
    		{
    			stc.alertBox("player_not_found", "player " + name + " not found");
				return;
    		}
    		
		}
    	debug("player login failed, "+n+" tries");

    }
    
    // Register a new player
    public void regNewPlayer()
    {
  		String name=null;
  		String info=stc.getTcpConnection().getInfo();
    	debug("reg new player "+info);
    	
        for(;;) 
		{        	
        	// Ask player to give a desired user name
        	name = stc.promptString("enter_player_name", "give a new name");
        	if (name==null)
        	{
        		break;
        	}
        	else if (isStringOkAsPlayerName(name))
    		{
	    		Player player = findPlayer(name) ;
	    		
	    		if (player==null)
	    		{
	    			// Player not found, we can continue registering the new player profile
	    			
	    			player = new Player(null, name, info, "null");

	    			while((stc!=null) && (stc.isOpen()) && (player.password==null))
	    	        {    			
	    				String pw = stc.promptString("enter_new_password", "give new password");
		    			   					    			
		    			if (isStringOkAsPw(pw))
		    			{
			    			/*String pw2 = promptBox("confirm_new_password", "confirm new password");
			    			
			    			if ((isEmailAddressOk(pw2, 3)) && (pw2.equals(pw)))*/
			    			{
			    				// password is ok
			    				
			    				for(;;)
			    				{
					            	String emailAddress = stc.promptString("enter_email", "give an email address to be used if you forget your password");
					            	if (emailAddress==null)
					            	{
					            		return;
					            	}
					            	else if (isEmailAddressOk(emailAddress))
					    			{					    				
					    				player.password=pw;
					    				player.emailAddress=emailAddress;
					    				this.player=player;
				    					DbRoot playersDb = stc.findOrCreatePlayersDb();
					    				playersDb.addObjectAndSave(player, config);

					    				stc.alertBox("player_reg_accepted", "player reg accepted");
					    				
						    			/*int r=SendEmail.sendEmailTo(player.emailAddress, "mpe", "Hello " + player.getName()+ "\nWelcome to MultiPlayerEmpire\nwww.eit.se/hb/mpe\nActivation code: "+player.emailVerificationCode +"\n\n");

						    			if (r!=0)
						    			{
						    				stc.alertBox("email_failed", "sending email did not seem to work"); 
						    			}*/
					    				
					    				return;
					    			}
					            	else
					            	{
					            		stc.alertBox("email_not_ok", "the entered email address was not ok");
					            	}
			    				}
			    			}
			    			/*else
			    			{
			    				stc.alertBox("passwords_did_not_match", "passwords did not match");
			    			}*/
		    			}
		    			else
		    			{
		    				stc.alertBox("password_to_long_or_short", "to long, to short or contains characters not allowed");
		    			}
	    	        }
	    		}
	    		else
	    		{
	    			stc.alertBox("player_already_exist", "player "+name+" already exist");
	    		}
    		
    		}
    		else
    		{
				stc.alertBox("name_to_long_or_short", "to long, to short or containing characters not allowed");
    		}
		}
    }

    
    public void recoverPw()
    {
  		String name=null;
    	debug("recover password from "+stc.getTcpConnection().getInfo());
    	
        for(;;) 
		{
        	name = stc.promptString("enter_player_name", "give a new name");
        	if (name==null)
        	{
        		break;
        	}
        	else if (isStringOkAsPlayerName(name))
    		{
    		
	
	    		Player player = findPlayer(name) ;
	    		
	    		if (player!=null)
	    		{
	    			stc.alertBox("player_found", "username " + player.getName() + " found, will send email (it can take a few minutes, if still not found check spam filters)");
	    			
	    			/*int r=SendEmail.sendEmailTo(player.emailAddress, "your pw", player.password);

	    			if (r!=0)
	    			{
	    				stc.alertBox("email_failed", "sending email did not seem to work"); 
	    			}*/
	    			
	        		break;
	    		}
	    		else
	    		{
	    			stc.alertBox("user_not_found", "the given username don't seem to exist");
	        		break;
	    		}
    		}
    		else
    		{
				stc.alertBox("name_to_long_or_short", "to short or to long");
    		}
		}		
    	
    	
    }
    
    public void setNewPw()
    {
    	for(;;)
    	{			
			
			Object[] options = {"Change password",
                    "Change email",
                    "Email activation",
                    "Cancel"};
			
        	int r = stc.promptButtons("change_pw", "Choose setting to change", options);
        	switch(r)
        	{
        		case 0:
        		{
    				String pw = stc.promptString("enter_new_password", "give new password");
    		    	
    				if (isStringOkAsPw(pw))
    				{
    	    			String pw2 = stc.promptString("confirm_new_password", "confirm new password");
    	    			
    	    			if ((isStringOkAsPw(pw2)) && (pw2.equals(pw)))
    	    			{
    	    				player.password=pw;
    	    				player.saveRecursive(config);
    	    				
    		    			stc.alertBox("password_changed", "password changed");
    	    				return;
    	    			}
    	    			else
    	    			{
    	    				stc.alertBox("passwords_did_not_match", "passwords did not match");
    	    			}
    				}
    				else
    				{
    					stc.alertBox("password_to_long_or_short", "to long or short");
    				}
        			break;
        		}
        		case 1: 
        		{
    				String ea = stc.promptString("enter_new_email", "give new email address");
    		    	
    				if (isEmailAddressOk(ea))
    				{
    					player.emailAddress=ea;
    					player.saveRecursive(config);

    	    			/*int r2=SendEmail.sendEmailTo(player.emailAddress, "mpe", "Hello " + player.getName()+ "\nWelcome to MultiPlayerEmpire\nwww.eit.se/hb/mpe\nActivation code: "+player.emailVerificationCode +"\n\n");
    	    			if (r2!=0)
    	    			{
    	    				stc.alertBox("email_failed", "sending email did not seem to work"); 
    	    			}*/
        				return;
    				}
    				else
    				{
    					stc.alertBox("email_to_long_or_short", "to long or short, did not change it");
    				}
        			
        			break;
        		}
        		case 2: 
        		{
    				if (player.emailVerificationCode==0)
    				{
    					
    					stc.alertBox("already_activated", "your email adress is already verified");
    				}
    				else
    				{
    				
    					int i = stc.promptInt("enter_activation_code", "enter activation code", 1, 999999);
    			    	
    					if (i<0)
    					{
    						stc.alertBox("number_not_in_range", "number not in range");
    						return;
    					}
    					else if (i==player.emailVerificationCode)
    					{
    						stc.alertBox("email_verified", "email verified");
    						player.emailVerificationCode=0;
    						player.saveRecursive(config);
    						return;
    					}
    				}
        			
        			break;        		
        		}
        		default: return;
        	}
			
			
    	}
    }

    /*
    public World2d createAndStoreWorld2d(final long startTime)
    {
		World2d newMapAndNations = new World2d(worldName, player.getName(), startTime);
		
		try {
			newMapAndNations.lockWrite();
    		newMapAndNations.generateWorld();
		}
		finally
		{
			newMapAndNations.unlockWrite();
		}
		
		
		DbRoot wdb=cc.findWorldsDb();
		wdb.lockWrite();
		try
		{
			wdb.addGameObj(newMapAndNations);
		}
		finally
		{
			wdb.unlockWrite();
		}
    	
    	// Save the database with the new world	    		    	
		newMapAndNations.saveRecursive();
    
		return newMapAndNations;
    }
    
*/
    
    

    
    // Start a new game, ask user for which type of game to start
    // Returns the name of the started game
    public String startNewGame()
    {
    	//String typeOfGame=null;
    	debug("startNewGame");
		Object typeNames[]=new String[1];  
		// List all supported games here. If adding one here, add it in playWorld also.
		typeNames[0]="Hmeg";
		//typeNames[0]="CityPvp";
		//typeNames[0]="Empire";
		//typeNames[3]="RoboGame";
		//typeNames[3]="ChatRoom";
		//typeNames[4]="TextAdventure";
		//typeNames[5]="MibWorld";

		
		
		int typeOfGame = stc.promptButtons("list_enter_game_type_name", "what kind of game to start", typeNames);    		
		
		
		if (typeOfGame<0)
		{
			// user pressed cancel or disconnected.
			worldName=null;
		}
		else 
		{
			switch(typeOfGame)
			{
    			/*case 0:
    			{
    				EmpireServer s=new EmpireServer(config, player, stc);
    			    worldName=s.createAndStore();    				
    			    break;
    			}
    			case 1:
    			{
    				CityPvpServer s=new CityPvpServer(config, player, stc);
    			    worldName=s.createAndStore();    				
    			    break;
    			}*/
    			case 0:
    			{
    				HmegServer s=new HmegServer(config, player, stc);
    			    worldName=s.createAndStore();
    			    break;
    			}
    			/*case 3:
    			{
    				RoboGameServer s=new RoboGameServer(config, player, stc);
    				worldName=s.createAndStore();
    			    break;
    			}*/
    			/*case 3:
    			{
    				ChatRoomServer s=new ChatRoomServer(config, player, stc);
    			    worldName=s.createAndStore();
    			    break;
    			}
    			case 4:
    			{
    				MibServer s=new MibServer(config, player, stc);
    			    worldName=s.createAndStore();    				
    			    break;
    			}*/
			}
    	}
    	
    	return worldName;
    }
    
    
    // To join a world the player has not been playing in before.
    /*protected String joinExisting()
    {
    	String worldName=null;
    	debug("startNew");
  		worldName = cc.promptString("enter_world_name", "enter name of world to join");
    	
    	return worldName;
    }*/
    
    
    
    public void listGames() throws IOException
    {
    	DbRoot worldsDatabase = stc.findWorldsDb();
    	
    	worldsDatabase.lockRead();
	    try
	    {
			if (worldsDatabase.listOfStoredObjects!=null)
			{	    	
				for (DbStorable bo : worldsDatabase.listOfStoredObjects)
				{
			    	debug(bo.getName());		    	
			    	
			    	stc.writeLine("say "+bo.getName());
		    	}
			}
	    }
	    finally
	    {
	    	worldsDatabase.unlockRead();
	    }
    }
			
    
    // We get here when client wants to play in one world
    /*protected void chatRoom(String worldName)
    {
	    ChatRoomConnection crc=new ChatRoomConnection(player, cc);
		crc.joinChatRoom(worldName);
    }*/
    
    

    // We get here when client wants to play in one world
    /*protected void playRoboWorld(String worldName)
    {
	    RoboGameConnection rgc=new RoboGameConnection(player, cc, msgQueue, hwc);
		rgc.playWorld(worldName);
    }*/
    
    // We get here when client wants to play in one world
    protected void playWorld(String worldName)
    {
        debug("worldName \"" + worldName+"\" playerName \"" + player.getName() +"\"");
        
        if (worldName==null)
        {
        	return;
        }
        
    	//DbRoot w = cc.db.findDbRootNotRecursive(worldName);
        DbRoot wdb = stc.findWorldsDb();
    	DbRoot w = wdb.findDbRootNotRecursiveReadLock(worldName);

  		if (w!=null)
        {
    		stc.alertBox("joining_world", "joining world "+ w.getNameAndPath("/"));
    		
    		// One "if" for each supported game here. If adding one here, add it in startNewGame also.
    		if (w instanceof MibWorld)
    		{
    			MibServer mib=new MibServer(config, player, stc);
    			mib.join(w);	    			
    		}	    		
    		else if (w instanceof ChatRoomWorld)
    		{
    			ChatRoomServer cs=new ChatRoomServer(config, player, stc);
    			cs.join(w);
    		}
    		else if (w instanceof TextAdventureWorld)
    		{
    			TextAdventureServer cs=new TextAdventureServer(config, player, stc);
    			cs.join(w);
    		}
    		/*else if (w instanceof EmpireWorld)
    		{
    			EmpireServer es=new EmpireServer(config, player, stc);
    			es.join(w);	    			
    		}*/	    	
    		/*else if ((w instanceof RoboGameWorld) && (stc.dontUseRef==false))
    		{
    			RoboGameServer rgc=new RoboGameServer(config, player, stc);
    			rgc.join(w);
    		}*/
    		else  if ((w instanceof MibWorld) && (stc.dontUseRef==false))
    		{
    			MibServer mib=new MibServer(config, player, stc);
    			mib.join(w);	    			
    		}
    		else if (w instanceof HmegWorld)
    		{
    			HmegServer cps=new HmegServer(config, player, stc);
    			cps.join(w);	    			
    		}
    		else if (w instanceof CityPvpWorld)
    		{
    			CityPvpServer cps=new CityPvpServer(config, player, stc);
    			cps.join(w);	    			
    		}
    		else
    		{
            	stc.alertBox("game_not_found", "that game is not supported "+ worldName);
    		}
        }
        else
        {
        	stc.alertBox("world_not_found", "world not found "+ worldName);
        }
    }   

    
    protected String selectAndPlayWorld()
    {
    	String worldName=stc.selectWorld();

    	if (worldName!=null)
        {
    		playWorld(worldName);
        }
    	
    	return worldName;
    }
    	
    	    /*
    protected String startNewRoboGame()
    {
	    RoboGameConnection rgc=new RoboGameConnection(player, cc, msgQueue, hwc);
	    worldName=rgc.startNew();
		rgc.playWorld(worldName);
		return worldName;
    }*/
    
    //Provide the service.
  	public void run() 
  	{    
  		System.out.println("A client connected from \""+stc.getTcpConnection().getInfo()+"\"");

  		// This must match what is done by ClientThread  		
  		
        try {

        	// The first is to wait for client to say what protocol version it wish to use
			String protocol = stc.readLine(60*1000); // 60*1000 = 1 min timeout
 		
			//debug("testing !\"#¤%&/()=!");
			
	  		if (protocol.equals("rsb_game") || protocol.equals("rsb_web_game"))
	  		{
	  			debug("client accepted for protocol '" + protocol+"'");

				stc.dontUseRef=protocol.equals("rsb_web_game");

	  			
				//stc.writeLine("xehpuk.com/mpe/"+ Version.ver);
		
				// Verify that the client is of same version.
				// This is done by sending a string to client with the servers version.
				// The client is expected to reply with its version.
		  		String serverNameAndVersion=WordReader.replaceCharacters(Version.getNameAndVersion()+"/server", ' ', '_');
		  		String clientNameAndVersion=WordReader.replaceCharacters(Version.getNameAndVersion()+"/client", ' ', '_');		  		
				String clientVersion=stc.askAndWait(serverNameAndVersion,clientNameAndVersion, "cancel");
				if (clientVersion!=null)
				{
				  debug("clientVersion ok, "+clientVersion);
				}
				else
				{
				  debug("clientVersion not ok, expected "+clientNameAndVersion);
				}
				
				
				// Now its time to as client to login or register a new user account.
		        while(player==null) 
				{		        	
		        	// These are the expected replies on our prompting.
					Object[] options = {"Login",
		                    "Reg new account",
		                    "Recover pw",
		                    "Cancel"};
					
		        	// Send request to client to prompt user for what he or she want to do, login or register...
		        	int r = stc.promptButtons("login_or_reg", "Welcome to RSB", options);
		        	
		        	// We have a reply. Call next method to proceed accordingly.
		        	switch(r)
		        	{
		        		case 0: playerLogin(); break;
		        		case 1: regNewPlayer(); break;
		        		case 2: recoverPw(); break;
		        		default: throw new IOException("cancel");
		        	}
		    		
				}		

		        // Here the user have logged in to an account. Now ask client/user what to do next.

		        if (protocol.equals("rsb_web_game"))
		        {
			        while ((player!=null) && (stc!=null) && (stc.isOpen()))
					{
			        	// These are the alternatives to be given to the user at client end.
						Object[] options = {"Continue game",		                    
			                    "Start new game",
			                    "List existing games",
			                    "Account settings",
			                    /*"Join an existing game",*/
			                    "Join chat room",
			                    "Cancel"};
						
			        	int r = stc.promptButtons("join_or_create", "Do you want to join an existing game or start a new?", options);
			        	switch(r)
			        	{
			        		case 0: worldName=selectAndPlayWorld(); break;
			        		case 1: worldName=startNewGame(); playWorld(worldName); break;
			        		case 2: worldName=null; listGames(); break;
			        		case 3: worldName=null; setNewPw(); break;
			        		case 4: worldName=null; playWorld("DefaultChatRoom"); break;
			        		//case 4: worldName=joinExisting(); playRoboWorld(worldName); break;
			        		default: player=null; break;
			        	}
			
			   		}
		        	
		        }
		        else
		        {
			        while ((player!=null) && (stc!=null) && (stc.isOpen()))
					{
			        	// These are the alternatives to be given to the user at client end.
						Object[] options = {"Continue game",		                    
			                    "Start new game",
			                    "List existing games",
			                    "Account settings",
			                    "Cancel"};
						
			        	int r = stc.promptButtons("join_or_create", "Do you want to join an existing game or start a new?", options);
			        	switch(r)
			        	{
			        		case 0: worldName=selectAndPlayWorld(); break;
			        		case 1: worldName=startNewGame(); playWorld(worldName); break;
			        		case 2: worldName=null; listGames(); break;
			        		case 3: worldName=null; setNewPw(); break;
			        		default: player=null; break;
			        	}
			
			   		}
		        }
	  		}
	  		else
	  		{
	  			debug("client not accepted, unknown protocol "+protocol);
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
        
        close();
        debug("run end");
	}

  	
  	public void close()
  	{
  		if (stc != null)
  		{
  			if (stc.isOpen())
  			{
  				try {
					stc.writeLine("close");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
  			}
  			stc.close();
  		} 
  	}
  	
  	
	public void finalize()
	{
	    debug("finalize");    
	    
	    close();
	}

	
	public void say(String msg)
	{
		this.msgQueue.put(msg);
	}


	@Override
	public WebFileData takeSocketData(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
