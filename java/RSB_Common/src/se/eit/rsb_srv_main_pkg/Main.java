/*
Main.java

To test this use:
websocket-client.html
websocket-client.js



Copyright 2013 Henrik Björkman (www.eit.se/hb)


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

import se.eit.rsb_package.*;
import se.eit.rsb_server_pkg.FileServer;
import se.eit.rsb_server_pkg.SocketServer;
import se.eit.rsb_server_pkg.TickThread;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class Main {

	GlobalConfig config=new GlobalConfig();
	DbSuperRoot dbRoot = null;
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
			dbRoot.saveRecursive(config);
		}
		else if (cmd.equals("save"))
		{
			DbRoot db=defaultObj.getDbRoot();
			debug("saving "+db.getName());
			db.saveRecursive(config);
			
		}
		else if (cmd.equals("cdi"))
		{
			String n=cmdReader.readWord();
			if (WordReader.isInt(n))
			{
				int id=Integer.parseInt(n);
				DbRoot db=defaultObj.getDbRoot();
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
		}
		else if (cmd.equals("pwd"))
		{
			System.out.println("  "+defaultObj.getNameAndPath("/")+getIdIfAny(defaultObj));
		}
		else if (cmd.equals("cd"))
		{
			String n=cmdReader.readWord();
			DbBase newDefaultObj=defaultObj.findRelativeFromNameOrIndex(n);

			if (newDefaultObj!=null)
			{
				defaultObj=newDefaultObj;
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
			
				DbRoot db=defaultObj.getDbRoot();
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
			if (cmdReader.isNextInt())
			{
				final int recursionDepth=cmdReader.readInt();
				defaultObj.listNameAndPath(new PrintWriter(System.out), recursionDepth, "  ");
			}
			else
			{
				//String path=WordReader.getWord(line);
				//DbBase bo=dbRoot.getObjFromIndexPathWithinDbRoot(path);
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
			WordWriter pw=new WordWriter(System.out);
			defaultObj.listInfo(pw, "  ");
			pw.flush();
		}
		else if (cmd.equals("ts")) 
		{
			WordWriter pw=new WordWriter(System.out);
			pw.println(defaultObj.toString());
			pw.flush();
		}
		else if (cmd.equals("dr"))
		{
			WordWriter ww=new WordWriter(System.out);
			defaultObj.writeRecursive(ww);
			ww.flush();
		}
		else if (cmd.equals("ds"))
		{
			WordWriter ww=new WordWriter(System.out);
			defaultObj.writeSelf(ww);
			ww.flush();
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
					DbRoot db=defaultObj.getDbRoot();
					if (db instanceof DbIdList)
					{
						DbIdList il=(DbIdList)db;
						DbNamed to = il.getDbIdObj(id);
						
						db.lockWrite();
						try
						{
							defaultObj.moveBetweenRooms(from, to);
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
			DbRoot r=defaultObj.getDbRoot();
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
			DbRoot db=defaultObj.getDbRoot();
			db.lockWrite();
			try
			{
				final String tag=cmdReader.readWord();
				final String value=WordReader.getLineWithoutLf(cmdReader.readLine());
				final boolean result=defaultObj.changeInfo(tag, value);
				if (result==false)
				{
					System.out.println("did not find "+tag+ " or it is a read only property");											
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
		System.out.println("Welcome to RSB");
	
		config.logConfig(System.out);

   	
		
		debug("load existing worlds");
		dbRoot = loadExistingWorldsAndPlayers();

		debug("Create and start tick, the thread that updates the world");
		tickThread = new TickThread(config, dbRoot);
		Thread ttt = new Thread(tickThread);
	 	ttt.start();

	 	FileServer fileServer=new FileServer(config, dbRoot);
	 	WebSocketServer webSocketServer= new SocketServer(config, dbRoot);

	 	
		debug("Create and start the thread that accepts new connections");
		webServer = new WebServer(config.port, config.httpRootDir, webSocketServer, fileServer);   	
		Thread stt = new Thread(webServer);
		stt.start();
		
		if (config.startWebBrowser==true)
		{
			debug("start web browser");
			if(Desktop.isDesktopSupported())
			{
			  try 
			  {
				Desktop.getDesktop().browse(new URI("http://localhost:"+config.port));
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
		
		debug("check input from standard input");
		defaultObj=dbRoot;
		wr=new WordReader(System.in);
		while (wr.isOpenAndNotEnd())
		{
			System.out.println("default object: "+defaultObj.getNameAndPath("/")+getIdIfAny(defaultObj));
			String cmdLine=wr.readLine();
			WordReader cmdReader=new WordReader(cmdLine);
			
			
			commandParser(cmdReader);
			
		}
		debug("closed console");

	}
	


	
	
	
	
	private static void parse_file(File f, WordReader fwr, DbRoot rootObj) throws FileNotFoundException
	{
		
		
		
		final String name = fwr.readWord('.'); // get the first part of the filename					
		
		
		if (fwr.isOpenAndNotEnd())
		{
		
			DbRoot dbRoot=rootObj.findDbRootNotRecursive(name);
			
			if (dbRoot!=null)			
			{
				parse_file(f, fwr, dbRoot);
			}
			else
			{
				error("did not find '"+name+ "' in '" +rootObj.getNameAndPath("/")+"'");
			}
		}
		else
		{
			System.out.println("parsing '" + f.getAbsolutePath()+"', '" + name + "', to be stored in '"+ rootObj.getNameAndPath("/")+"'");

			
			BufferedReader is = new BufferedReader(new FileReader(f));
			WordReader wr = new WordReader(is);
			
			final String nameAndVersion=Version.getNameAndVersion();
			String nav=wr.readString();
			if (nav.equals(nameAndVersion))
			{
				debug("version "+nav);
				try
				{
					DbBase go=DbContainer.staticParse(wr);
				
					if (go instanceof DbNamed)
					{
						DbNamed gameObj = (DbNamed)go;
						if (name.equals(gameObj.getName()))
						{
							debug("name ok '"+name+"'");
							rootObj.addObject(go);
						}
						else
						{
							error("name not same as file name "+gameObj.getName()+" "+name+ " ignored object");
						}
					}
				}
				catch (NumberFormatException e)
				{
					WordWriter.safeError("readChildrenRecursive: NumberFormatException: '" + e.getMessage() + "'");
				}
			}
			else
			{
				System.out.println("ignored " + f.getAbsolutePath() + " old version");
			}
		}
	}
	
	private static void parse_file_name(File f, String fileName, DbRoot rootObj) throws FileNotFoundException
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
	
	
	public DbSuperRoot loadExistingWorldsAndPlayers()
	{
		DbSuperRoot newRoot = new DbSuperRoot("wap"); 
		
		newRoot.lockWrite();
		try {
		
			//GameObj lobby = new GameObj("players");
			//newRoot.addGameObj(lobby);
											
			//GameObj audioLobby = new TmpObj("audioChannels", "place for audio channels of players");
			//newRoot.addGameObj(audioLobby);
			

			DbRoot players = new DbRoot(newRoot, Player.nameOfPlayersDb);
			//newRoot.addGameObj(players);
			
			DbRoot worlds = new DbRoot(newRoot, WorldBase.nameOfWorldsDb);
			//newRoot.addGameObj(worlds);

			players.lockWrite();
			worlds.lockWrite();
			
			try {
				// Will examine all files in current directory (alias folder)
				File folder = new File(config.savesRootDir);
				System.out.println("loading worlds from " + folder.getAbsolutePath());
				if (folder.exists())
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
								parse_file_name(f, fileName.substring(0, fileName.length()-4), newRoot);
							}							
							else
							{
								debug("ignored file '"+ f.getName()+ "' (not a .txt file)");
							}
						}
						else if (f.isDirectory())
						{
							debug("ignored dir "+ f.getName());
						}
						else
						{
							debug("ignored "+ f.getName());
						}
					}
				}
				else
				{
					System.out.println("Folder \".\" not found");
				}
			} catch (IOException e) {
					e.printStackTrace();
			}
			finally
			{
				players.unlockWrite();
				worlds.unlockWrite();
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
				pdb = new DbRoot("playersdb", ""); 			
				newRoot.addGameObj(pdb);
			}
			*/
		}
		finally
		{
			newRoot.unlockWrite();
		}
		return newRoot;
	}

	
	public static void help()
	{
		System.out.println("Usage: ./server.jar <options> [options]");
		System.out.println("");
		System.out.println("Where [options] are:");
		System.out.println("-p <port>   : tcpip port number");
		System.out.println("-d <path>   : javascript directory");
		System.out.println("-s <path>   : game save directory");
		System.out.println("-w          : if set launch a web browser");
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
					m.config.port=Integer.parseInt(args[i]);
					debug("using port "+ m.config.port);
				}
				else if (args[i].equals("-d"))
				{
					i++;
					m.config.httpRootDir=args[i];
					debug("using httpRootDir "+ m.config.httpRootDir);
				}
				else if (args[i].equals("-s"))
				{
					i++;
					m.config.savesRootDir=args[i];
					debug("using savesRootDir "+ m.config.savesRootDir);
				}
				else if (args[i].equals("-w"))
				{
					m.config.startWebBrowser=true;
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
		 	File theDir = new File(m.config.httpRootDir);
		 	if (!theDir.exists()) {
		 		System.out.println("\nThe http root directory was not found at: "+m.config.httpRootDir+"\n");
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
