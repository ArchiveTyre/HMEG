package se.eit.rsb_server_pkg;

import java.io.IOException;

import se.eit.rsb_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class ServerTcpConnection {

	public DbRoot db;
	public WebConnection cc;
	
	public boolean dontUseRef;
	public int nref;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ServerTcpConnection.class.getSimpleName();	
	}	
	
    public static void debug(String str)
	{
    	WordWriter.safeDebug(className()+": "+str);
	}

    // To be called if something goes wrong
	public void error(String str)
	{
    	WordWriter.safeError(className()+": "+str);

	    close();
	}
	
    
	public ServerTcpConnection(DbRoot db, WebConnection cc)
	{
		this.db=db;
		this.cc=cc;
	}
	
	public boolean isOpen()
	{
		return (cc!=null) && (cc.isOpen());
	}
	
	
	public void close()
	{
		if (cc!=null)
		{
			cc.close();
			cc=null;
		}
	}

	public void finalize()
	{
	    debug("finalize");
	    close();    	
	}
	
	public void writeLine(String str) throws IOException
	{
		if (cc!=null)
		{
			cc.writeLine(str);
		}
		else
		{
			debug("writeLine: cc is null, failed to write "+str);
			throw(new IOException("cc is null"));
		}
	}
	
	public String readLine(long timeout_ms) throws InterruptedException, IOException
	{
		return cc.readLine(timeout_ms);
	}
	
	public WebConnection getTcpConnection()
	{
		return cc;
	}
		
	public DbRoot findWorldsDb()
	{
		return db.findDb(WorldBase.nameOfWorldsDb);
	}

	public DbRoot findPlayersDb()
	{
		return db.findDb(Player.nameOfPlayersDb);
	}
	
	public DbRoot findOrCreatePlayersDb()
	{
		DbRoot playersDatabase = findPlayersDb();

		if (playersDatabase==null)
		{
            debug("player database not found, creating it");
            
        	playersDatabase = new DbRoot(db, Player.nameOfPlayersDb);	    	
        	//db.addGameObj(playersDatabase);
   		}
		
		return playersDatabase;
	}
	
	public WorldBase findWorld(String name)
	{		
    	if (name==null)
    	{
            debug("findWorld: name was null");
    		return null;
    	}
    	
    	DbRoot worldsDatabase = findWorldsDb();

		if (worldsDatabase==null)
		{
            error("world database not found");
			return null;
		}
    	
		DbRoot ro = worldsDatabase.findDb(name);
		

		if (ro==null)
		{
		   	debug("world named '"+ name+"' was not found");
		   	return null;
		}
			
		
   		if (!(ro instanceof WorldBase))
   		{
		   	error("" + name+" was not a world");
			return null;
		}
		


		return (WorldBase)ro;				
	}

    // To resume playing in a world the player has been playing in before.
    protected String selectWorld()
    {
    	String worldName=null;
    	debug("startNew");
    	
    	DbRoot worldsDatabase = findWorldsDb();
    	Object[] worldNames;
    	
    	worldsDatabase.lockRead();
    	try
    	{
    		worldNames = worldsDatabase.getAllNamesNonRecursive();
    	}
    	finally
    	{
    		worldsDatabase.unlockRead();
    	}
	
    	if ((worldNames!=null) && (worldNames.length<=16))
    	{   		
    		worldName = promptList("list_enter_world_name", "enter name of world to continue", worldNames);    		
    	}
    	else
    	{    	
    		worldName = promptString("enter_world_name", "enter name of world to continue");
    	}
    	
    	return worldName;
    }

	   /*
    Send a question to other end and wait for one of two possible answers.
    Returns the string with answer if reply was ok, null if not ok or cancel.
    */
	public String askAndWait(String question, String replyOk, String replyCancel)
	{		
		//debug("askAndWait \""+question+"\" \""+replyOk+"\" \""+replyCancel+"\"");
		String str=null;
			
		while ((isOpen()) && (str==null))
		{
			try 
			{
				cc.writeLine(question);

				String lineRead = cc.readLine(15*60*1000);
				
				if (lineRead==null)
				{
					debug("this should never happen");
					cc.close();
					break;
				}
				
				String line=WordReader.getLineWithoutLf(lineRead);
				String firstWord=WordReader.getWord(line);
				
				if (line==null)
				{
					debug("got null");
					break;
				}
				else if (firstWord.equals(replyOk))
				{
					str = WordReader.getRemainingLine(line);
					//debug("got ok <"+str+">");
					break;
				}
				else if (firstWord.equals(replyCancel))
				{
					debug("got cancel");
					str = null;
					break;
				}
				else
				{
					debug("not correct reply "+firstWord);
				}
			} catch (IOException e) {
				e.printStackTrace();
				close();
			} catch (InterruptedException e) {
				debug("timeout");
				close();
			} catch (Exception e) {
				e.printStackTrace();
				close();
			}
		}
		return str;
	}
	
	
		

	// A reference number is added to the prompt message being sent to client.
	// The reference number is added so that we can check that the reply was a reply to the question sent and not a reply to some other question.
	// This must match how its done in method prompt in ClientThread.java
	private String promptAndWaitRef(String question)
	{	
		String str=null;
		while(str==null)
		{
			str=askAndWait("prompt "+nref+" "+question, "replyOk", "replyCancel");

			if (str==null)
			{
				break;
			}

			String ref = WordReader.getWord(str);
			if (ref.equals(""+nref))
			{		
				str = WordReader.getRemainingLine(str);
			}
			else
			{
				debug("nref did not match, got "+ref+" expected "+nref);
			}
			nref++;
			
		}		
		return str;
	}
	
	// Query
	private String directPrompt(String question)
	{
		try 
		{
			cc.writeLine("query " + question);
			return cc.readLine(15*60*1000);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();				
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	

	// Compose a question in two parts, a specialised part and a default part.
	// The client program shall use the tag if it has dedicated support for that particular question.
	// If not the client shall show the defaultText to the client user and defaultTypeOfQuestion
	private String promptTagOrDefault(String tag, String defaultText, String defaultTypeOfQuestion)
	{			
		if (dontUseRef)
		{
			return directPrompt(tag+" \""+defaultText+"\" "+defaultTypeOfQuestion);
		}
		else
		{
			return promptAndWaitRef(tag+" \""+defaultText+"\" "+defaultTypeOfQuestion);
		}
	}
	
	// This will prompt client/user for a text to be entered.
	// Used to ask client a question.
	// Parameters:
	// * tag is identifier string for the question, it must be one word only (that is no spaces). The client shall avoid changing the tags since that will require clients to also change.
	// * defaultText is an additional text that client program shall use only if it does not recognise the tag. The server is free to change this text at any time.
	// The client is expected to answer with either:
	// * reply and some text  
	// * cancel
	// Possible return values:
	// * a string, the text from client reply message 
	// * null, if it client answered with cancel.
	public String promptString(String tag, String defaultText)
	{	
		int n=0;
		while(++n<100)
		{
			String str = promptTagOrDefault(tag, defaultText, "promptString");
			
			if (str==null)
			{
				break;
			}
						
			debug("promptString \""+tag+"\" \""+defaultText+"\" \""+str+"\"");

			WordReader wr=new WordReader(str);
			
			if (wr.isNextString())
			{
				return wr.readString();
			}
			else
			{
				String reply=wr.readWord();
				if (reply.equals("cancel"))
				{
					return null;					
				}
				debug("Answer was not a string, it did not begin with '\"' "+str);
			}
		}
		debug("to many tries");
		return null;
	}
	
	// This will prompt client/user for a number to be entered.
	// Used to ask client for a number
	// Parameters:
	// * tag see promptBox.
	// * defaultText see promptBox.
	// The client is expected to answer with either:
	// * reply and some text  
	// * cancel
	// Preferably ask only for numbers >=0 since this returns -1 if cancel.
	public int promptInt(String tag, String defaultText, int min, int max)
	{	
		for(;;)
		{
			String str = promptTagOrDefault(tag, defaultText, "promptInt");
			
			if (str==null)
			{
				break;
			}
			
			debug("promptInt \""+tag+"\" \""+defaultText+"\" \""+str+"\"");

			WordReader wr=new WordReader(str);
			
			if (wr.isNextInt())
			{
				int n=wr.readInt();
				
				if ((n>=min) && (n<=max))
				{
					return n;
				}
				else
				{
					alertBox("out_of_range", "out of range");
				}				
			}
			else
			{
				debug("Answer was not a number "+ str);
			}
		}
		return -1;
	}
	
	// This will prompt client/user for a button to be chosen.
	// multiple choice
	// Will ask client a question to be answered with one of the given alternatives (or cancel)
	// Parameters:
	// * tag: see promptBox.
	// * defaultText: see promptBox.
	// * buttonNames: A list of alternatives to choose from
	// Returns
    //  the number of the chosen alternative, zero counting
	//  -1 for timeout, cancel or error
	public int promptButtons(String tag, String defaultText, Object buttonNames[])
	{	
		String names="";
		int n=0;
		
		for(int i=0; i<buttonNames.length; i++)
		{
			names+=" \""+buttonNames[i]+"\"";
		}		
		
		while(cc!=null)
		{
			String str = promptTagOrDefault(tag, defaultText, "buttonPrompt "+ buttonNames.length + names);
			
			if ((str==null) || (n++>100))
			{
				break;
			}
						
			debug("promptButtons \""+tag+"\" \""+defaultText+"\" \""+str+"\"");

			WordReader wr=new WordReader(str);
			
			if (wr.isNextInt())
			{
				return wr.readInt();
			}
			else
			{
				debug("Answer was not a button "+str);
			}
		}
		return -1;
	}

	// Similar as promptButtons but intended to be used when there can be a lot to choose from.
    // We should change so that the client answers with the number of the choice or a string. 
	protected String promptList(String tag, String defaultText, Object possibilities[])
	{
		boolean found=false;
		String name=null;
		
		while(found==false)
		{
			cc.writeLine("listClear");
			for(int i=0; i<possibilities.length; i++)
			{
				cc.writeLine("listAdd " + possibilities[i]);
			}
		
			name = promptTagOrDefault(tag, defaultText, "listPrompt "+ possibilities.length);
			
			if (name==null)
			{
				debug("user pressed cancel, probably");
				break;
			}

			if (name.equals("cancel"))
			{
				debug("user pressed cancel");
				name=null;
				break;
			}
			
			if (WordReader.isInt(name))
			{
				int i = Integer.parseInt(name);
				if ((i>=0) && (i<possibilities.length))
				{
					//if (possibilities[i] instanceof String)
					name=possibilities[i].toString();
					break;
				}
			}
			
			debug("promptList \""+tag+"\" \""+defaultText+"\" \""+name+"\"");			
			
			for(int i=0; i<possibilities.length; i++)
			{
				if (name.equals(possibilities[i]))
				{
					debug("answer matches alternative "+i);
					found=true;
				}
			}

			if (!found)
			{
				debug("answer is not one of the expected");
			}
			
			cc.writeLine("listClear");
		}
		

		return name;
	}
	
	
	
    // Send a notification to client.
	// An answer is not expected. 
	// Parameters:
	// * tag see promptBox.
	// * defaultText see promptBox.
    //
	public void alertBox(String tag, String defaultText)
	{
		if (dontUseRef)
		{
			//cc.writeLine("alertBox"+" "+tag+" \""+defaultText+"\"");
			//cc.writeLine("confirmBox"+" "+tag+" \""+defaultText+"\"");

			// alert and confirm did not work with current web client, will use a generic button dialog instead. 
			Object[] options = {"OK"};
			
        	int r = promptButtons(tag, defaultText, options);

        	switch(r)
        	{
        		case 0: debug("alertBox: ok");
    			default: debug("alertBox: not ok");
        	}
		}
		else
		{
			cc.writeLine("alertBox"+" "+tag+" \""+defaultText+"\"");
		}
		
		
		
	}
	
	
	// Prompt user for a world name. Check that name is ok and not already used.
    public String enterNameForNewGame()
    {
    	String worldName=null;
    	debug("enterNameForNewGame");
    	while (worldName==null)
    	{
    		worldName = promptString("enter_new_world_name", "enter new world name");
    		
    		if (worldName==null)
    		{
    			// User canceled or disconnected.
    			break;
    		}
    		else 
    		{
	    		if (ServerBase.isStringOkAsWorldName(worldName))
	    		{ 
	    			debug("name was ok '"+worldName+"'");
	    	    	// Check that name is not taken
	    			DbNamed go = findWorld(worldName);
	    	    	if (go==null)
	    	    	{
	    	    		// The name is free	    	    		
	    		    	
	    		    	return worldName;
	    	    	}
	    	    	else
	    	    	{
	    	    		alertBox("name_already_taken", "name already taken");
	    	    		worldName=null;
	    	    	}
	    		}
	    		else
	    		{
	    			// The given name contains characters that are not allowed.
	    			alertBox("name_not_accepted", "name not accepted, name must be characters and digits only");
	    			worldName=null;
	    		}
    		}
    	}
    	
    	return worldName;
    }
	
	
}
