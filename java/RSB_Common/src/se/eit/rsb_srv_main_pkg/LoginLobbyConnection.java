// LoginServerConnection.java
//
// Copyright (C) 2015 Henrik Björkman www.eit.se/hb
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_srv_main_pkg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import se.eit.db_package.DbList;
import se.eit.rsb_factory_pkg.GlobalConfig;
import se.eit.rsb_package.PlayerData;
import se.eit.rsb_package.Version;
import se.eit.web_package.MyBlockingQueue;
import se.eit.web_package.WordReader;

public class LoginLobbyConnection implements Runnable {

	GlobalConfig globalConfig;
	
	private Socket socket = null;
	private PrintStream out = null;
	private BufferedReader in = null;

	//private int avatarId=-1;
	
	//public DbList<NotificationData> notificationDataList=new DbList<NotificationData>();
	
	// queueList can be used from other threads. So synchronized must be used when handling common data.
	public DbList<MyBlockingQueue<String>> queueList=new DbList<MyBlockingQueue<String>>();
	
	//private ReentrantReadWriteLock rwl=null;
	
	
	public String getType()
	{	
		return this.getClass().getSimpleName(); // This gives the name of the extended class, that is this does not always give this class.
	}
	
	
	// Just for debugging.
	void debug(String s)
	{
	    System.out.println(getType() + ": " + s);   
	}
	
	void error(String s)
	{
		System.out.flush();
	    System.err.println(getType() + ": " + s);
		Thread.dumpStack();
	    System.exit(1);
	}

	
	public LoginLobbyConnection(GlobalConfig globalConfig)
	{
		this.globalConfig=globalConfig;
		//rwl=new ReentrantReadWriteLock();
	}
	
	// Returns true if there was a problem
	public boolean query(WordReader wr)
	{
		String ref=wr.readWord();
		
		if (ref.equals("login_or_reg"))
		{
			out.println("Login");
		}
		else if (ref.equals("enter_player_name"))
		{
			if (globalConfig.loginServerUsername!=null)
			{
				out.println("\""+globalConfig.loginServerUsername+"\"");
			}
			else
			{
				out.println("\"rsbgameserver\"");
			}
		}
		else if (ref.equals("player_not_found"))
		{
			return true;		
		}
		else if (ref.equals("enter_player_pw"))
		{
			if (globalConfig.loginServerUserPw!=null)
			{
				out.println("\""+globalConfig.loginServerUserPw+"\"");
			}
			else
			{
				out.println("\"ohnowhatisthis\""); // TODO same as user name above
			}
		}
		else if (ref.equals("login_ok"))
		{
			out.println("OK");
		}
		else if (ref.equals("2d_or_3d_support")) // This is deprecated
		{
			out.println("2d");
		}
		else if (ref.equals("game_support"))
		{
			// Tell server which service we want.
			out.println("loginServer");
		}
		else if (ref.equals("join_or_create"))
		{
			out.println("\"Continue game\"");
		}
		else if (ref.equals("list_enter_game_type_name"))
		{
			out.println("\"loginServer\"");
		}
		else if (ref.equals("no_worlds_available"))
		{
			//out.println("\"OK\"");
			return true;
		}
		else if (ref.equals("list_enter_world_name"))
		{
			//out.println("LoginServerWorld");
			out.println("yukigassenLobby");  // lobby with name after the name of the game
		}
		else if (ref.equals("list_can_not_find"))
		{
			return true;
		}
		else if (ref.equals("joining_world"))
		{
			out.println("OK");
		}
		else if (ref.equals("openLoginWorld"))
		{
			// Ignore
		}
		else if (ref.equals("selectType"))
		{
			out.println("server");
		}
		/*else if (ref.equals("serverUrl"))
		{
			if (globalConfig.myHostname!=null)
			{
				out.println("\"/"+globalConfig.myHostname+":"+globalConfig.myPort+"\"");
			}
			else
			{
				out.println("Cancel");
			}
		}*/
		/*else if (ref.equals("serverIp"))
		{
			if (globalConfig.myHostname!=null)
			{
				out.println("\""+globalConfig.myHostname+"\"");
			}
			else
			{
				out.println("Cancel");
			}
		}*/
		else if (ref.equals("serverPort"))
		{
			if (globalConfig.port!=-1)
			{
				out.println("\""+globalConfig.port+"\"");
			}
			else
			{
				out.println("Cancel");
			}
		}
		else
		{
			debug("unknown query");
		}
			
		return false;
	}

	public void clientIsOk(WordReader wr)
	{
		int questionIndex = wr.readInt();				

		String questionAnswer = wr.readString();				

		/*NotificationData notificationData = notificationDataList.get(questionIndex); 
		
		notificationData.doNotify(0);
		
		notificationDataList.remove(questionIndex);*/										
		
		MyBlockingQueue<String> mbq = null;
		
		synchronized(this)
		{
			// queueList can be used from other threads. So synchronized must be used when handling common data.
			mbq = queueList.get(questionIndex);
		}
		
		mbq.put(questionAnswer);
		
	}
	
	// Returns true when it is time to close the connection
	public boolean commandInterpreter(String line)
	{
		WordReader wr = new WordReader(line);

		String cmd=wr.readWord();
		
		char ch=cmd.charAt(0);
		
		
		
		switch(ch)
		{
			case 'a': 
				if (cmd.equals("avatarId"))
				{
					//avatarId = wr.readInt();
					return false;
				}	
			break;
			case 'c':
				if (cmd.equals("clientIsOk"))
				{
					// This is a reply to one of our "isClientOk" questions sent to server
					clientIsOk(wr);
				}
				else if (cmd.equals("close"))
				{
					
					return true;
				}
			
			case 'l': 
				if (cmd.equals("listClear"))
				{
					debug("ignored listClear");
					return false;
				}
				break;
			case 'q': 
				if (cmd.equals("query"))
				{
					return query(wr);
				}
				break;
			case 'm': 
				if ((cmd.equals("ma")) || (cmd.equals("mirrorAdd")))
				{
					// Ignore
					return false;
				}		
				else if ((cmd.equals("mu")) || (cmd.equals("mirrorUpdate")))
				{
					// Ignore
					return false;
				}		
				else if ((cmd.equals("mr")) || (cmd.equals("mirrorRemove")))
				{
					// Ignore
					return false;
				}	
				break;
			default: 
				break;
		}
		
		debug("unknown command: "+line);
		return false;
	}

	@Override
	public void run() {
		// TODO: Connect with the login server
		
		connect(globalConfig.loginServerHostname, globalConfig.loginServerPort);
		
		try {
			
			// Send an empty line to server
			// This will tell server that we are not a web socket client.
			out.println("rsb_web_game");
			
			// wait for first message from server		
			String line=in.readLine();
				
			// It should say server version.

			// ServerVersion and ClientVersion
	  		//String serverNameAndVersion=WordReader.replaceCharacters(Version.getProgramNameAndVersion()+"/server", ' ', '_');
	  		//String clientNameAndVersion=WordReader.replaceCharacters(Version.getProgramNameAndVersion()+"/client", ' ', '_');
	  		String serverNameAndVersion=Version.serverVersion();
	  		String clientNameAndVersion=Version.clientVersion();
	  		
			if (line==null)
			{
				debug("got null");
				throw new IOException("got null");
			}
			if (line.length()==0)
			{
				debug("got empty string");
				throw new IOException("got empty string");
			}
			else if (line.equals(serverNameAndVersion))
	    	{
				//cc.writeLine("xehpuk.com/rsb/client/"+ Version.getVersion());
				out.println(clientNameAndVersion);							
	    	}
			else
			{
				error("expected server version \"" + serverNameAndVersion +"\" but got \""+line+"\"");
				throw new IOException("wrong version, expected \"" + serverNameAndVersion +"\" but got \""+line+"\"");
			}

			
				
			for(;;) 
			{
				line=in.readLine();
				
				if (line==null)
				{
					debug("received null");
					break;
				}
				
				if (line.length()==0)
				{
					debug("empty string");
					break;
				}

				debug("line: "+line);
				
				boolean r = commandInterpreter(line);
				
				if (r)
				{
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		close();
	}


	// This can be called from other threads. So synchronized must be used when handling common data.
	// Returns null if not OK
	public PlayerData isClientOk(/*NotificationData notificationData,*/ int playerIndex, int playerCode) 
	{		
		MyBlockingQueue<String> mbq = new MyBlockingQueue<String>(8);
		
		int questionIndex=-1;
		
		synchronized(this)
		{
			questionIndex = queueList.add(mbq);
		}
		
		//int questionIndex = notificationDataList.add(notificationData);
		
		out.println("isClientOk "+ playerIndex + " "+playerCode+" "+questionIndex);
		
		// Need to wait for a PlayerData
		
		PlayerData playerData=null;
		
		try {
			String str=mbq.take(60000);
			
			if (str.length()>1)
			{
				WordReader wr=new WordReader(str);
				playerData=new PlayerData();
				playerData.lockWrite();
				try
				{
					playerData.readSelf(wr);
				}
				finally
				{
					playerData.unlockWrite();					
				}
			}
			
		} catch (InterruptedException e) {
			debug("No reply from login server");
			e.printStackTrace();
		}
		
		synchronized(this)
		{
			queueList.remove(questionIndex);
		}	
		
		return playerData;
	}
	
	
	public void connect(String hostname, int port)
	{
	    if ((hostname!=null) && (port>0) && (socket==null))
	    {
	       debug("trying to connect to "+hostname+":"+port);
	       try 
	       {
	         // Create a socket to communicate to the specified host and port
	         socket = new Socket(hostname, port);
	    
	         // Create streams for reading and writing lines of text
	         // from and to this socket.
	         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));         
	         out = new PrintStream(socket.getOutputStream());
	
	         debug("Connected to " + socket.getInetAddress() + ":"+ socket.getPort());        
	       }      
	       catch (IOException e) 
	       {
	         error("connect failed "+e);
	       }
	       finally 
	       {
	         // Always be sure to close the socket if any
	         //try { if (socket != null) socket.close(); } catch (IOException e2) { ; }
	       }
	    }
	    else
	    {
	    	error("could not connect");
	    }
	}

	public synchronized void close()
	{
		try 
		{
			if (in!=null)
			{
				in.close();
				in=null;
			}
			if (out!=null)
			{
				out.close();
				out=null;
			}
			if (socket!=null)
			{
				socket.close();
				socket=null;
			}
		}
		catch( IOException e ) 
		{
			error("reconnect " + e );
		}
	}
	
	
	public void finalize()
	{
		debug("finalize");
	    close();
	}
	
}
