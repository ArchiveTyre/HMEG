/*
Main.java

To test this use:
websocket-client.html
websocket-client.js




Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license



History:
2013-05-08
Created by Henrik Bjorkman (www.eit.se/hb)

*/



package se.eit.rsb_srv_main_pkg;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStream;

import se.eit.rsb_factory_pkg.GlobalConfig;
import se.eit.rsb_factory_pkg.RsbFactory;
import se.eit.rsb_package.*;
import se.eit.rsb_srv_main_pkg.FileServer;
import se.eit.rsb_srv_main_pkg.SocketServer;
import se.eit.rsb_server_pkg.TickThread;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class Main {

	GlobalConfig globalConfig=new GlobalConfig();
	DbTickList dbRoot = null;
	DbBase defaultObj=dbRoot; 
	WebServer webServer = null;
	TickThread tickThread = null;
	WordReader wr = null;
	
	public static void debug(String str)
	{
		WordWriter.safeDebug("Main: " + str);
	}

	protected static void error(String str)
	{
		WordWriter.safeError("Main: " + str);	
		System.exit(1);
	}

	
	public static String getIdIfAny(DbBase no)
	{
		final int id=no.getId();
		
		if (id>=0)
		{
			return " (id="+id+")";
		}
		return "";
	}
	   
	
	protected void commandParser(WordReader cmdReader)
	{
		String cmd=cmdReader.readWord();

		if (cmd.equals("help"))
		{
			System.out.println("  help : Show this text");
			System.out.println("  cd <name> : Set default object, to move up do 'cd ..'");
			System.out.println("  cdi <id> : Set default object");
			System.out.println("  ls : List sub objects");
			System.out.println("  rm : delete default object (be carefull, there will be no warning)");
			System.out.println("  mv <id> : move default object to the room with id <id>");
			System.out.println("  sa : Save all databases in server");
			System.out.println("  save : Save default database (the one where default object is residing)");
			//System.out.println("show <index path> : index base path shall be a string sourounded with \"\" like \"3 2 5\"");
			System.out.println("  ta : set time acceleration (or 0 for pause)");
			System.out.println("  cat : list properties of default object");
			System.out.println("  ts : show all data in default object as string (ts=ToString)");
			System.out.println("  set <tag> <value> : change a property of default object");
			System.out.println("  dr : dump recursively");
			System.out.println("  ds : dump self");
			System.out.println("  rls <depth>: recursively list sub objects");
			System.out.println("  quit : Shut down server");
			System.out.println("  note! Sometimes the argument is a name sometimes and id, sometimes a name is a number but that name/number is not same as id");
		}
		else if (cmd.equals("sa"))
		{
			debug("saving all");
			//dbRoot.setGlobalConfig(globalConfig);
			dbRoot.saveRecursive();
		}
		else if (cmd.equals("save"))
		{
			DbSubRoot db=defaultObj.getDbSubRoot();
			debug("saving "+db.getName());
			//db.setGlobalConfig(globalConfig);
			db.saveRecursive();
			
		}
		/*else if (cmd.equals("cdi"))
		{
			String n=cmdReader.readWord();
			if (WordReader.isInt(n))
			{
				int id=Integer.parseInt(n);
				DbSubRoot db=defaultObj.getDbSubRoot();
				if (db instanceof DbIdList)
				{
					DbIdList il=(DbIdList)db;
					defaultObj = il.getDbIdObj(id);
				}
				else
				{
					System.out.println("default object is not in an DbIdList");
				}
			}
			else
			{
				System.out.println("argument did not look like a number");
			}
		}*/
		else if (cmd.equals("pwd"))
		{
			System.out.println("  "+defaultObj.getNameAndPath("/")+getIdIfAny(defaultObj));
		}
		else if (cmd.equals("cd"))
		{
			String n=cmdReader.readWord();
			
			// Check that input was given.
			if (!n.equals(""))
			{
				if (n.charAt(0) == '~')
				{
					n=n.substring(1);
					int id=Integer.parseInt(n);
					DbSubRoot db=defaultObj.getDbSubRoot();
					if (db instanceof DbIdList)
					{
						DbIdList il=(DbIdList)db;
						defaultObj = il.getDbIdObj(id);
					}
					else
					{
						System.out.println("default object is not in an DbIdList");
					}				
				}
				else
				{
					DbBase newDefaultObj=defaultObj.findRelativeFromNameOrIndex(n);
					if (newDefaultObj!=null)
					{
						defaultObj=newDefaultObj;
					}
				}
			}
			else
			{
				// Set the dir to default
				defaultObj = dbRoot;
			}
			//debug("sd "+defaultObj.getNameAndPath());		
		}
		else if (cmd.equals("show"))
		{
			System.out.println(defaultObj.getObjInfoPathNameEtc());
		}
		else if (cmd.equals("rm"))
		{
			String n=cmdReader.readWord();
			DbBase objToDelete=null;
			if (n.equals(".."))
			{
				// Can not remove super object
			}
			else if ((n.equals("")) || (n.equals(".")))
			{
				objToDelete=defaultObj;
				defaultObj=defaultObj.getContainingObj();
				// Can not remove selected object	
			}
			else if (WordReader.isInt(n))
			{
				int i=Integer.parseInt(n);
				objToDelete=(DbNamed)defaultObj.getObjFromIndex(i);
			}
			else
			{
				objToDelete=(DbNamed)defaultObj.findGameObjNotRecursive(n);
			}

			if (objToDelete!=null)
			{
			
				DbSubRoot db=defaultObj.getDbSubRoot();
				db.lockWrite();
				try
				{
					objToDelete.unlinkSelf();
				}
				finally
				{
					db.unlockWrite();
				}
			}
			else
			{
				System.out.println("did not find object to delete "+n);
			}
				
		}
		else if (cmd.equals("rls"))
		{
			//debug("list " + dbRoot.getName());
			if (cmdReader.isNextIntOrFloat())
			{
				final int recursionDepth=cmdReader.readInt();
				defaultObj.listNameAndPath(new PrintWriter(System.out), recursionDepth, "  ");
			}
			else
			{
				//String path=WordReader.getWord(line);
				//DbBase bo=dbRoot.getObjFromIndexPathWithinDbSubRoot(path);
				defaultObj.listNameAndPath(new PrintWriter(System.out), 0, "  ");
			}
		}
		else if (cmd.equals("ls"))
		{
			PrintWriter pw=new PrintWriter(System.out);
			defaultObj.listSubObjects(pw, "  ");
			pw.flush();
		}
		else if (cmd.equals("cat")) 
		{
			PrintWriter pw = new PrintWriter (System.out);			
			WordWriter ww=new WordWriterPrintWriter(pw);
			defaultObj.listInfo(ww);
			ww.flush();
			pw.flush();
		}
		else if (cmd.equals("ts")) 
		{
			PrintWriter pw = new PrintWriter (System.out);
			WordWriter ww=new WordWriterPrintWriter(pw);
			ww.println(defaultObj.toString());
			ww.flush();
			pw.flush();
		}
		else if (cmd.equals("dr"))
		{
			PrintWriter pw = new PrintWriter (System.out);
			WordWriter ww=new WordWriterPrintWriter(pw);
			defaultObj.writeRecursive(ww);
			ww.flush();
			pw.flush();
		}
		else if (cmd.equals("ds"))
		{
			PrintWriter pw = new PrintWriter (System.out);
			WordWriter ww=new WordWriterPrintWriter(pw);
			defaultObj.writeSelf(ww);
			ww.flush();
			pw.flush();
		}
		else if (cmd.equals("mv"))
		{
			String n=cmdReader.readWord();
			
			DbNamed from=(DbNamed)defaultObj.getContainingObj();
   			
			if (from!=null)
			{
				if (WordReader.isInt(n))
				{
					int id=Integer.parseInt(n);
					DbSubRoot db=defaultObj.getDbSubRoot();
					if (db instanceof DbIdList)
					{
						DbIdList il=(DbIdList)db;
						DbNamed to = il.getDbIdObj(id);
						
						db.lockWrite();
						try
						{
							//defaultObj.moveBetweenRooms(from, to);
							defaultObj.moveToRoom(to);
						}
						finally
						{
							db.unlockWrite();
						}					
					}
					else
					{
						System.out.println("default object is not in an DbIdList");
					}
				}
				else
				{
					System.out.println("argument did not look like a number");
				}
			}
			else
			{
				System.out.println("can't move root object");
			}
		}
		else if (cmd.equals("find"))
		{
			String name=cmdReader.readWord();
							

			
			DbBase newDefaultObj;
			DbSubRoot r=defaultObj.getDbSubRoot();
			r.lockRead();
			try
			{
				newDefaultObj=defaultObj.findDbNamedRecursive(name);
			}
			finally
			{
				r.unlockRead();
			}



			if (newDefaultObj!=null)
			{
				defaultObj=newDefaultObj;
			}
			else
			{
				System.out.println("did not find "+name);
			}
			
		}
		else if (cmd.equals("set"))
		{
			DbSubRoot db=defaultObj.getDbSubRoot();
			db.lockWrite();
			try
			{
				final String tag=cmdReader.readWord();

				final int result=defaultObj.setInfo(cmdReader, tag);
				if (result==0)
				{
					System.out.println("did not find "+tag+ " or it is a read only property");											
				}
				else
				{
					defaultObj.setUpdateCounter();
				}
			}
			finally
			{
				db.unlockWrite();
			}
		}
		
		else if (cmd.equals("quit"))
		{
			debug("close tcp ip server");
			webServer.close();
			webServer=null;
			
			debug("stop tick");
			tickThread.setDone();
			tickThread=null;
			
			// Save all databases
			// Or not, we don't want to save all, only those changed.
			//debug("save all databases");
			//dbRoot.save();
			//dbRoot.clear();
			
			wr.close();
			wr=null;
			
			System.out.println("exit");
			System.exit(0);
		}
		else if (cmd.equals("ta"))
		{
			tickThread.timeAcceleration=cmdReader.readInt();
			System.out.println("timeAcceleration set to: "+tickThread.timeAcceleration);
		}
		else
		{
			System.out.println("Unknown command " + cmd + " " + cmdReader.readLine() + ", try: help");
		}
		
	}
	
	   
	public void go()
	{
		System.out.println("Welcome to Drekkar games");
	
		globalConfig.logConfig(System.out);

   	
		
		debug("load existing worlds");
		loadExistingWorldsAndPlayers();

		debug("Create and start tick, the thread that updates the world");
		tickThread = new TickThread(globalConfig, dbRoot);
		Thread ttt = new Thread(tickThread);
	 	ttt.start();

	 	
	 	LoginLobbyConnection loginServerConnection=null;
	 	if (globalConfig.loginServerHostname!=null)
	 	{
	 		// Connect to login server
	 		loginServerConnection=new LoginLobbyConnection(globalConfig);
			Thread loginServerConnectionThread = new Thread(loginServerConnection);
			loginServerConnectionThread.start(); 		
	 	}
	 	
	 	FileServer fileServer=new FileServer(globalConfig, dbRoot);
	 	WebSocketServer webSocketServer= new SocketServer(globalConfig, dbRoot, loginServerConnection);

	 	
		debug("Create and start the thread that accepts new connections");
		webServer = new WebServer(globalConfig.port, globalConfig.httpRootDir, webSocketServer, fileServer);   	
		Thread stt = new Thread(webServer);
		stt.start();
		
		if (globalConfig.startWebBrowser==true)
		{
			debug("start web browser");
			if(Desktop.isDesktopSupported())
			{
			  try 
			  {
				Desktop.getDesktop().browse(new URI("http://localhost:"+globalConfig.port));
			  } catch (IOException e) {
					error("IOException "+e);
					e.printStackTrace();
			  } catch (URISyntaxException e) {
					error("IOException "+e);
					e.printStackTrace();
			  }
			}
			else
			{
				debug("desktop is not supported");
			}
		}
		
		//debug("check input from standard input");
		defaultObj=dbRoot;
		wr=new WordReaderInputStream(System.in);
		while (wr.isOpenAndNotEnd())
		{
			//debug("default object: "+defaultObj.getNameAndPath("/")+getIdIfAny(defaultObj));
			String cmdLine=wr.readLine();
			WordReader cmdReader=new WordReader(cmdLine);
			
			
			commandParser(cmdReader);
			
		}
		debug("closed console");

	}
	


	
	
	
	// The files we parse here is written from DbSubRoot.saveSelf so if something is changed there it may need changing here also.	
	private static void parse_file(File f, WordReader fwr, DbSubRoot rootObj) throws FileNotFoundException
	{
		
		
		
		final String name = fwr.readWord('.'); // get the first part of the filename					
		
		
		if (fwr.isOpenAndNotEnd())
		{
			DbSubRoot dbRoot = (DbSubRoot)rootObj.findOrCreateChildObject(name, "DbNoSaveRoot");
		
			parse_file(f, fwr, dbRoot);
		}
		else
		{
			// This is the final part of the full file name
			debug("parsing '" + f.getAbsolutePath()+"', '" + name + "', to be stored in '"+ rootObj.getNameAndPath("/")+"'");

			// Open the file and create a WordReader to parse it.
			BufferedReader is = new BufferedReader(new FileReader(f));
			WordReader wr = new WordReaderBufferedReader(is);
			
			// Make sure the saved file format is the expected format and version.
			final String expectedProgramNameAndVersion=Version.fileFormatVersion();
			
			if (wr.isNextString())
			{
				// This is probably our own proprietary file format
				String pnav=wr.readString();
				if (pnav.equals(expectedProgramNameAndVersion))
				{
					debug("version "+pnav);
					try
					{
						// TODO we need to create the DbSubRoot object here, then let it linkSelf into rootObj and then read recursively the rest. This because things like GlobalConfig will not be known during recursive read unless it is part of the full tree already. 
						
						//DbBase go=DbContainer.staticParse(wr);
	
						String t = wr.readWord(); // get type of object to parse
						DbBase go=RsbFactory.createObj(t);			
						
						if (go instanceof DbNamed)
						{
							//DbNamed gameObj = (DbNamed)go;
							//if (name.equals(gameObj.getName()))
							//{
								//debug("name ok '"+name+"'");
								
								rootObj.lockWrite();
								try
								{
									go.linkSelf(rootObj);
	
								}
								finally
								{
									rootObj.unlockWrite();
								}
	
								
								go.readRecursive(wr);
	
								
								// It was not possible to register interference while loading since all objects must be loaded before setting up interference lists. So doing it here.
								// TODO: Is there a better way so this can be done more automatically? Probably if using dedicated objects for interference and not sub rooms, then it can be done during linkSelf and the call below shall be removed.
								//go.registerInterferenceRecursive(); 
							//}
							//else
							//{
							//	error("name not same as file name "+gameObj.getName()+" "+name+ " ignored object");
							//}
						}
						else
						{
							error("not DbNamed");
						}
					}
					catch (NumberFormatException e)
					{
						WordWriter.safeError("readChildrenRecursive: NumberFormatException: '" + e.getMessage() + "'");
					}
				}
				else
				{
					// This was not the expected file save format, ignore this file.
					System.out.println("ignored '" + f.getAbsolutePath() + "', old version: '"+pnav+"'");
				}
			}
			else if (wr.isNextBegin())
			{
				// The file began with a '{' (alias begin) so this is perhaps JSON
				wr.readWord();

				if (wr.isNextString())
				{
					String v=wr.readString();
					
					if (v.equals("versionInfo"))
					{
						String pnav=wr.readString();
						if (pnav.equals(expectedProgramNameAndVersion))
						{
							String t=wr.readString();
					
							DbBase o=RsbFactory.createObj(t);			
		
							try
							{
								
								
								rootObj.lockWrite();
								try
								{
									o.linkSelf(rootObj);
		
								}
								finally
								{
									rootObj.unlockWrite();
								}
		
								
								o.readRecursive(wr);
		
								
							}
							catch (NumberFormatException e)
							{
								WordWriter.safeError("readRecursive: NumberFormatException: '" + e.getMessage() + "'");
							}
						}
						else
						{
							// Currently we can only read files generated by same version of the program (we do not always update the version string though, in fact usually we do not, so this is not expected to happen often).
							System.out.println("old file version: '"+pnav+"', ignored: '" + f.getAbsolutePath() + "'");
						}
					}
					else
					{	
						// The first object in our save files is expected to be the version info. 
						System.out.println("unknown version, ignored: '" + f.getAbsolutePath() + "'");							
					}
				}
				else
				{
					// This was not the expected file save format, ignore this file.
					System.out.println("not the expected file format, ignored: '" + f.getAbsolutePath()+"'");
				}
			}
			else
			{
				// This was not the expected file save format, ignore this file.
				System.out.println("unknown file format, ignored: '" + f.getAbsolutePath() + "'");				
			}
		}
	}
	
	private static void parse_file_name(File f, String fileName, DbSubRoot rootObj) throws FileNotFoundException
	{
		
		// check if it starts with magic word wap and ends with extension txt.
		WordReader fwr=new WordReader(fileName);
		
		final String prefix = fwr.readWord('.'); // get the first part of the filename					
		
		if ((prefix.equals(rootObj.getName())))
		{
			// The first part of name matches our root object

			parse_file(f, fwr, rootObj);
		}
		else
		{
			debug("ignored file '" +f.getAbsolutePath() + "' since it did not begin with '" + rootObj.getName()+"'");
		}
	}

	// Saving sub roots is done in DbSubRoot.saveSelf. Then naming here and there need to match.	
	void loadFolder(File folder, String path) throws IOException
	{
		// Folder exist, now get the list of all files in it.
		File[] listOfFiles = folder.listFiles();
		
		//File f = new File("ugl_save_world.txt");
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{
			// For each file in the folder, check if it is a file, ignore directories.
			File f = listOfFiles[i];										
			if (f.isFile()) 
			{
				// This is a file, get its name
				final String fileName = f.getName();

				if (fileName.endsWith(".txt"))
				{
					final String n = path + fileName.substring(0, fileName.length()-4); // remove .txt part of file name
					parse_file_name(f, n, dbRoot);
				}							
				else if (fileName.endsWith(".json"))
				{
					final String n = path + fileName.substring(0, fileName.length()-5); // remove .json part of file name
					parse_file_name(f, n, dbRoot);
				}							
				else
				{
					debug("ignored file '"+ f.getName()+ "' (not a .txt or .json file)");
				}
			}
			else if (f.isDirectory())
			{
				String n=path;
				/*if (path.length()>0)
				{
					n+=".";
				}*/
				n+=f.getName()+".";
				debug("dir " + n);
				loadFolder(f, n);
			}
			else
			{
				debug("ignored "+ f.getName());
			}

		}
	}
	
	// Saving sub roots is done in DbSubRoot.saveSelf. Then naming here and there need to match.	
	public void loadExistingWorldsAndPlayers()
	{
		dbRoot = new DbTickList("wap", globalConfig); 
		
		dbRoot.lockWrite();
		try {
					

			//DbSubRoot players = new DbSubRoot(newRoot, PlayerData.nameOfPlayersDb);	
			//DbSubRoot worlds = new DbSubRoot(newRoot, WorldBase.nameOfWorldsDb);

			

			
			//players.lockWrite();
			//worlds.lockWrite();
			
			try {
				// Will examine all files in current directory (alias folder)
				File folder = new File(globalConfig.savesRootDir);
				System.out.println("loading worlds from " + folder.getAbsolutePath());
				if (folder.exists())
				{			
					loadFolder(folder, "");
				}
				else
				{
					System.out.println("Folder \".\" not found");
				}
				
				
				// Some databases are not stored on disc, recreate new ones for these.	
				/*DbSubRoot players = (DbSubRoot)*/
				dbRoot.findOrCreateChildObject(PlayerData.nameOfPlayersDb, PlayerData.typeOfPlayersDb);
				
				/*DbSubRoot worlds = (DbSubRoot)*/dbRoot.findOrCreateChildObject(WorldBase.nameOfWorldsDb, WorldBase.typeOfWorldsDb);

				
			} catch (IOException e) {
					e.printStackTrace();
			}
			finally
			{
				//players.unlockWrite();
				//worlds.unlockWrite();
			}
			/*
			World w = newRoot.findWorld("world");
			if (w==null) 
			{
				System.out.println("creating new world");
	
				World newMapAndNations = new World("world", "worldmap and nations"); 
				newRoot.addGameObj(newMapAndNations);
			}
			*/
	
			/*
			GameObj pdb = newRoot.findGameObj("playersdb") ;
			if (pdb==null)
			{
				pdb = new DbSubRoot("playersdb", ""); 			
				newRoot.addGameObj(pdb);
			}
			*/
		}
		finally
		{
			dbRoot.unlockWrite();
		}
	}

	
	public static void help()
	{
		System.out.println("Usage: ./server.jar <options> [options]");
		System.out.println("");
		System.out.println("Where [options] are:");
		System.out.println("-p <port>                    : tcpip port number"); // Specify the IP port for the server. Clients shall connect to this port. Typically -p 8080.
		System.out.println("-d <path>                    : javascript directory");
		System.out.println("-s <path>                    : game save directory");
		System.out.println("-l <path>                    : script/lua directory");
		System.out.println("-r <path>                    : use a general root directory for the 3 above"); // Often the folders web, lua and saved_games are located in a common folder, so instead of using -d -s- l this can be used. 
		System.out.println("-w                           : if set launch a web browser"); // Useful when testing, a pure servers should not use this.
		System.out.println("-g <host> <port> <user> <pw> : game server only (no user data)"); // This server shall connect to the main server, players will be sent over from there.
		System.out.println("-e                           : allow external servers"); // Set this switch if this is the main login server. External servers use -g to connect to this server.
		System.out.println("-n <pw>                      : set a password for creating new games"); // If this is a login server this needs to be set to some good password.
		System.out.println("");
		System.out.println("Examples:");
		System.out.println("-p 8080 -d ../../web -s ../../saved_games -w");
		System.out.println("./server.jar -p 8080 -d /home/henrik/git/RoboticsSandBox/web -s /home/henrik/saved_games -w");  	
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
		/*
		{
			System.out.println(""+Long.toString(-2, 16));
			System.out.println(""+Long.toString(255, 16));
			
			final long i=Long.parseLong("fe000000fc000000", 16);
			System.out.println(""+i);
		}
		*/
		
		/*
		BigBitMap bbm=new BigBitMap("10 8 9 80");
		for(int i=0;i<256;i++)
		{
			if (bbm.getBit(i)>0)
			{
				System.out.println(""+i);
			}
		}
		*/

		Main m = new Main();

		
		//WebHttpServer w = new WebHttpServer();

		if (args.length<1)
		{
			help();
		}
		else
		{
			for(int i=0; i<args.length; i++)
			{
				if (args[i].equals("-p"))
				{
					i++;
					m.globalConfig.port=Integer.parseInt(args[i]);
					debug("using port "+ m.globalConfig.port);
				}
				else if (args[i].equals("-d"))
				{
					i++;
					m.globalConfig.httpRootDir=args[i];
					debug("using httpRootDir "+ m.globalConfig.httpRootDir);
				}
				else if (args[i].equals("-s"))
				{
					i++;
					m.globalConfig.savesRootDir=args[i];
					debug("using savesRootDir "+ m.globalConfig.savesRootDir);
				}
				else if (args[i].equals("-w"))
				{
					m.globalConfig.startWebBrowser=true;
					debug("start web browser");
				}
				else if (args[i].equals("-g"))
				{
					i++;
					m.globalConfig.loginServerHostname=args[i];
					i++;
					m.globalConfig.loginServerPort=Integer.parseInt(args[i]);
					i++;
					m.globalConfig.loginServerUsername=args[i];
					i++;
					m.globalConfig.loginServerUserPw=args[i];
					debug("login server "+m.globalConfig.loginServerHostname+" "+m.globalConfig.loginServerPort);
				}
				else if (args[i].equals("-n"))
				{
					i++;					
					m.globalConfig.startGamePw=args[i];
					debug("password is required to start a new game");					
				}
				else if (args[i].equals("-l"))
				{
					i++;
					m.globalConfig.luaScriptDir=args[i];
					debug("using luaScriptDir "+ m.globalConfig.luaScriptDir);
				}
				else if (args[i].equals("-r"))
				{
					i++;
					String path=args[i];
					m.globalConfig.luaScriptDir=path+"/lua";
					m.globalConfig.httpRootDir=path+"/web";
					m.globalConfig.savesRootDir=path+"/saved_games";
					debug("using luaScriptDir "+ m.globalConfig.luaScriptDir);
					debug("using httpRootDir "+ m.globalConfig.httpRootDir);
					debug("using savesRootDir "+ m.globalConfig.savesRootDir);
				}
				else if (args[i].equals("-e"))
				{
					m.globalConfig.allowExternal=true;
					debug("start web browser");
				}
				else
				{
					help();
					//System.exit(1);
					return;
				}
				
			}

		 	// Check that the http root directory exist
		 	File theDir = new File(m.globalConfig.httpRootDir);
		 	if (!theDir.exists()) {
		 		System.out.println("\nThe http root directory was not found at: "+m.globalConfig.httpRootDir+"\n");
		 		System.out.println("Use switch -? for usage help");
				//System.exit(1);		 		  
				return;
		 	}
			
			
			// Start a web html server
			//Thread thread = new Thread(w);
			//thread.start();
			
			
			m.go();
		}
	}

}
