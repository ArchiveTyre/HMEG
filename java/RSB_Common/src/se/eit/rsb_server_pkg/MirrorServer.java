package se.eit.rsb_server_pkg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
//import java.util.Queue;

import se.eit.db_package.DbBase;
import se.eit.db_package.DbContainer;
import se.eit.db_package.DbIdObj;
import se.eit.db_package.NotificationReceiver;
//import se.eit.empire_package.EmpireState;
import se.eit.rsb_package.GameBase;
import se.eit.rsb_srv_main_pkg.GlobalConfig;
import se.eit.rsb_package.Player;
import se.eit.rsb_package.WorldBase;
import se.eit.web_package.MyBlockingQueue;
import se.eit.web_package.WordWriter;

public abstract class MirrorServer extends ServerBase implements NotificationReceiver {

	public MirrorServer(GlobalConfig config, Player player,
			ServerTcpConnection stc) {
		super(config, player, stc);
	}

	/*class IntegerInteger
	{
		IntegerInteger(){};
		public int a=0;
		public int b=0;
	}*/
	
	// latestVersionOfIdObjectSent keeps a list of the objects from worldBase that have been sent to the client so that 
	// we can know if updates are needed.
	// http://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
	// http://docs.oracle.com/javase/7/docs/api/java/util/HashMap.html
	HashMap<Integer, Integer> latestVersionOfIdObjectSent = new HashMap<Integer, Integer>();
	//Queue<IntegerInteger> toBeAddedToListOfLatestVersions = new Queue<IntegerInteger>();
	// idObjectsToUpdate keeps a list of objects that needs to be sent to client
	MyBlockingQueue<Integer> idObjectsToUpdate = new MyBlockingQueue<Integer>(100);

	protected WorldBase worldBase = null;
	
	protected GameBase playerAvatar; // TODO: This shall be of some avatar class.

	private int updateSequenceNumberToSend=0;
	protected int updateSequenceNumberReceived=0;
	private int networkLag=0;
	
	protected void dumpWorld(WorldBase worldBase, ServerTcpConnection stc) throws IOException
	{
		// Send world data to client, TODO: Should probably write to an internal string buffer first and then send it to client (to avoid locking database more than needed)
		worldBase.lockRead();
		try
		{
			WordWriter ww = new WordWriter(stc.getTcpConnection());
			
			//ww.setFlushAfterLf();
			ww.writeLine("mirrorWorld");
			ww.writeBegin();
			ww.writeWord(worldBase.getType());
			worldBase.writeRecursive(ww);
			ww.writeEnd();
			ww.flush();
		}
		finally
		{
			worldBase.unlockRead();
		}	
		// Tell client that the entire database is now down loaded.
		stc.writeLine("empWorldShow"); // TODO: empWorldShow command should be a mirror command.
	}
	
	
	// This sends data to client and remember which data (version) it has sent
	protected void sendUpdateAndRemember(DbIdObj io) throws IOException
	{
		final int id=io.getId();
		
		if (id<0)
		{
			// Don't send updates of objects without an ID. This can happen since we recurse upwards and eventually come to objects above this world. It can also happen if an object has been deleted.
			if (io instanceof GameBase)
			{
				debug("no ID for "+io.getName());
			}
			return;
		}
		

		
		// If client gets an object before that objects parent it will have trouble. So make sure we have sent it.
		final DbContainer parentObject=io.getParent();
		if ((parentObject!=null) && (parentObject instanceof DbIdObj))
		{
			// Parent exist, have we sent it to client?
			final int parentId=parentObject.getId();
			final Integer parentCounter=latestVersionOfIdObjectSent.get(parentId);
			if ((parentCounter==null) && (parentId>=0))
			{
				// Parent is never sent to client. Send it first (before sending the object to be updated).
				sendUpdateAndRemember((DbIdObj)parentObject);
			}
		}

		final int cc=io.getDbChangedCounter();
		final Integer sentCounter=latestVersionOfIdObjectSent.get(id); // will be null if not yet sent.
		
		WordWriter ww = new WordWriter();
		ww.writeWord((sentCounter==null) ? "mirrorAdd" : "mirrorUpdate");
		ww.writeInt(id); // id of this object
		ww.writeInt(io.getParentId()); // id of parent object
		ww.writeWord(io.getType()); // is this needed?
		io.writeSelf(ww);

		
		final String str=ww.getString();
		
		/*if (sentCounter==null)
		{
			debug("toClient " +str);
		}*/
		
		stc.writeLine(str); // TODO: worldBase should not be read locked when calling stc.writeLine since that call may block. Instead this function shall return a string, the string is sent after worldBase.unlockRead(); 

		// remember what has been sent to client
		latestVersionOfIdObjectSent.put(id, cc); // remember version of all units sent. 
	}
		
	
	protected int sendUpdateAndRememberIfVisible(DbIdObj io) throws IOException
	{
		if (io.isVisibleTo(playerAvatar))
		{
			//debug("visible "+io.toShortFormatedString());
			sendUpdateAndRemember(io);
			return 1;
		}
		else
		{
			//debug("not visible "+io.toShortFormatedString());
		}
		return 0;
	}
	
	protected void sendRemoved(int id) throws IOException
	{	
		WordWriter ww = new WordWriter();
		ww.writeWord("mirrorRemove");					
		ww.writeInt(id);
		String str=ww.getString();
		//debug(str);
		stc.writeLine(str);
	}

	// Returns the number of objects to be removed
	public int findRemovalsAndRemember() throws IOException, InterruptedException
	{
		int n=0;
		// Iterate the list of objects we have previously sent to client.
		// If the object no longer exist send a "removeUnit" message.
		// Check for removed objects or updated objects.
		// http://java67.blogspot.se/2013/08/best-way-to-iterate-over-each-entry-in.html
		//
		// There is a problem with this code
		// Exception in thread "Thread-9" java.util.ConcurrentModificationException
		// But without this code information about removed objects are not sent to client. 
		// Solution is to only use this for removing objects, don't do sendUpdateAndRemember from here.
		Iterator<Map.Entry<Integer, Integer>> iterator = latestVersionOfIdObjectSent.entrySet().iterator();
		while(iterator.hasNext())
		{
			final Map.Entry<Integer, Integer> entry = iterator.next();  // ConcurrentModificationException on this line, but why?
		    final int key=entry.getKey(); // key is an object ID.

			
			final DbIdObj io=worldBase.getDbIdObj(key);
			if ((io==null) || (!io.isVisibleTo(playerAvatar)))
			{
				// An object has been removed, tell client about it.
				sendRemoved(key);
				iterator.remove(); // right way to remove entries from Map, this does not cause ConcurrentModificationException.
				n++;
			}
			else
			{
				// An object has been changed, tell client about it.
				// No, this can result in ConcurrentModificationException if a parent is missing. So commented out this.
				/*
				final Integer value=entry.getValue();
				final int sentDbChangedCounter=value;
				final int currentDbChangedCounter=io.getDbChangedCounter();
				if (sentDbChangedCounter!=currentDbChangedCounter)
				{
					sendUpdateAndRemember(io); // this would cause ConcurrentModificationException.
				}
				*/
				// do nothing, changes are sent in next step, the loop "for (int i=0; i<idListLength; i++)" below.
			}
			
		}		
		return n;
	}
	
	public int findAndSendDbUpdatesToClientAll() throws IOException, InterruptedException
	{
		// http://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
		int n=0;
		
		try
		{
			worldBase.lockRead();
			findRemovalsAndRemember();


						
			// Here we should iterate all units in the part of the world this user can see.
			// But for now we will iterate everything. This might become terribly inefficient when world grows large. Hopefully the usual case is that idObjectsToUpdate is used and not this. 
			//final int idListLength=worldBase.getDbIdListLength();
			//for (int i=0; i<idListLength; i++)
			for (final DbIdObj io : worldBase.idList)
			{
				final int i=io.getId();
	
					final Integer prevChangeCounter=latestVersionOfIdObjectSent.get(i);
					if (prevChangeCounter==null)
					{
						// This object is new, has not yet been sent to the client. Tell client about it.
						n+=sendUpdateAndRememberIfVisible(io);
					}
					else
					{
						final int sentDbChangedCounter=prevChangeCounter;
						if (io.isChanged(sentDbChangedCounter))
						{
							// This object is changed. Send to client.
							n+=sendUpdateAndRememberIfVisible(io);
						}
						else
						{
							// This object has not changed, do nothing.
						}
					}
				}
			}
			finally
			{
				worldBase.unlockRead();
			}
			
			idObjectsToUpdate.clear();
			
			//stc.writeLine("mirrorUpdated "+updateSequenceNumberToSend++);
			return n;
	}
	
	public int iterateUpdatesToClient() throws IOException, InterruptedException
	{
		int n=0;
		if (idObjectsToUpdate.size()>0)
		{
			// not empty
			worldBase.lockRead();
		    try 
		    {
				n+=findRemovalsAndRemember();

		    	for(;;) // loop until InterruptedException
		    	{
					int id=idObjectsToUpdate.take(0);
					
					final DbIdObj io=worldBase.getDbIdObj(id); // TODO: Do we need read lock here?
					if (io==null)
					{
						// An object previously sent to client no longer exist, tell client to remove it.
						//sendRemoved(id);
					}
					else if (!io.isVisibleTo(playerAvatar))
					{
						// An object previously sent to client no longer visible, tell client to remove it.
						//sendRemoved(id);
					}
					else
					{
						
						final Integer prevChangeCounter=latestVersionOfIdObjectSent.get(id);
						if (prevChangeCounter==null)
						{
							// This object is new, not previously sent to client
							sendUpdateAndRemember(io);
							n++;
						}
						else
						{
							// an object previously sent, sending again is only needed if it has been changed since then
							final int sentDbChangedCounter=prevChangeCounter;
							if (io.isChanged(sentDbChangedCounter))
							{
								// It has been changed, send to client.
								sendUpdateAndRemember(io);
								n++;
							}
						}
						
					}
		    	}
			} catch (InterruptedException e) {
				// do nothing, this is normal when idObjectsToUpdate is empty
			}
			finally
			{
				worldBase.unlockRead();
			}
		    
	    }
		return n;
	}
	
	
	public void findAndSendDbUpdatesToClient() throws IOException, InterruptedException
	{
		// Do not send to many updates. Client or network might not keep up.
		if ((updateSequenceNumberToSend - updateSequenceNumberReceived) > 2)
		{
			if (networkLag<5)
			{
				debug("network lag? "+updateSequenceNumberToSend+" "+updateSequenceNumberReceived);
			}
			networkLag++;
			return;
		}
	
		int n=0;
		
		if (idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity())
		{
			// queue has gotten full. Ignore the queue, instead scan database from scratch.
			n+=findAndSendDbUpdatesToClientAll();
		}
		else
		{
			// the notification queue was not full, send updates using it, if is not empty.
			n+=iterateUpdatesToClient();
		}

	    if (n>0)
	    {
	    	stc.writeLine("mirrorUpdated "+ updateSequenceNumberToSend++);
	    }

	}
		
		

	
	
	// Notify can be called from various threads so do only thread safe stuff here.
	// sendersRef is expected to be an object ID.
	public void notify(int subscribersRef, int sendersRef)
	{
		if (idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity())
		{
			// queue is full
			// ignore further notifications.
			// we will instead scan the entire database for changes.
		}
		else
		{
			final DbBase db = worldBase.getDbIdObj(sendersRef);
			if (db!=null)
			{
				if (db.isVisibleTo(playerAvatar))  
				{
					// If it is visible add/update shall be sent to client
					idObjectsToUpdate.put(sendersRef);
				}
				else if (latestVersionOfIdObjectSent.get(sendersRef)!=null) 					
				{
					// If it is not visible but it has been then remove shall be sent to client 
					idObjectsToUpdate.put(sendersRef);
				}
			}
			else
			{
				debug("notify "+sendersRef+" null");
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
