// MirrorServer.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_server_pkg;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.eit.db_package.DbBase;
import se.eit.db_package.DbContainer;
import se.eit.db_package.DbIdObj;
import se.eit.db_package.DbStorable;
import se.eit.db_package.DbThreadSafe;
import se.eit.db_package.NotificationReceiver;
import se.eit.web_package.MyBlockingQueue;
import se.eit.web_package.WordWriter;

public abstract class MirrorServer extends ServerBase implements NotificationReceiver {

	public MirrorServer() {
		super();
	}

	
	
	
	/*class IntegerInteger
	{
		IntegerInteger(){};
		public int a=0;
		public int b=0;
	}*/
	
	// latestVersionOfIdObjectSent keeps a list of the objects from worldBase that have been sent to the client so that we can know if updates are needed.
	// http://docs.oracle.com/javase/7/docs/api/java/util/Hashtable.html
	// http://docs.oracle.com/javase/7/docs/api/java/util/HashMap.html
	// TODO From what I know HashMap will internally create objects for every key-value pair stored (See https://en.wikipedia.org/wiki/Criticism_of_Java). That will make for a lot of memory usage overhead. Perhaps implement an own HashMap internally using long to store the pair of ints?
	protected HashMap<Integer, Integer> latestVersionOfIdObjectSent = new HashMap<Integer, Integer>(); // key is object ID and value is a changed counter.
	//Queue<IntegerInteger> toBeAddedToListOfLatestVersions = new Queue<IntegerInteger>();

	// idObjectsToUpdate keeps a list of objects that needs to be sent to client. This queue will be added to by another thread than reading it so using a thread safe queue.
	// TODO Here every client-server thread has its own list. We could make a much more CPU optimized list if we instead had one ring buffer with objects that has been changed. Each client-server thread would only need an index so it knows how far in that ring buffer it has come. A ring buffer would be OK since we already have a feature to send everything from scratch if the queue gets to big.  
	protected MyBlockingQueue<Integer> idObjectsToUpdate = new MyBlockingQueue<Integer>(0x10000);

	//protected WorldBase worldBase = null;
	
	protected DbThreadSafe playerAvatar; // TODO: This shall be of some avatar base class or use AvatarPlayerReferences
	
	
	// Update sequence numbers are used so server can tell if it is OK to send more messages. If client does send the sequence number back server may not send more messages to client until it does.
	private int serverSequenceNumberToSend=0;
	protected int serverSequenceNumberReceived=0;
	
	// Client sequence numbers are used to tell client that it is OK to send more messages. If client does not get its sequence number back it may not send more messages until itr does.
	protected int clientSquenceNumberReceived=0; // TODO: This and clientSquenceNumberReceivedAndAcked should not be needed.
	protected int clientSquenceNumberReceivedAndAcked=0; // Remember sequence number sent to client, need this to know if we need to send a mirrorUpdated, we must send a mirrorUpdated if clientSquenceNumberReceived has changed.

	// This is used for debug logging, can be removed later
	private static int debugLogCounter=0; 
	int cleanupCount=0;

	/*
	protected void dumpWorld(WorldBase worldBase, ServerTcpConnection stc) throws IOException
	{
		// Send world data to client, TODO: Should probably write to an internal string buffer first and then send it to client (to avoid locking database more than needed)
		worldBase.lockRead();
		try
		{
			WordWriter ww = new WordWriterWebConnection(stc.getTcpConnection());
			
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
		stc.writeLine("showWorld"); // TODO: empWorldShow command should be a mirror command.
	}
	*/
	
	
	// This sends data to client and remember which data (version) it has sent
	protected void sendUpdateAndRemember(DbStorable io) throws IOException
	{
		final int id=io.getId();
		
		if (id<0)
		{
			// Don't send updates of objects without an ID. 
			// Usually this is an unlinked object, it has been added to the list of objects to be sent to a client but before it was sent it was unlinked.
			// So shall we send a "mirrorRemove" to client? No we can't since we do not have the ID any more.
			// TODO do we need a better way for this so that we can inform the client at this point? One way would be to first move objects to a waste bin. Then we clear the waste bin at maintenance tick. That would make it possible to avoid new objects getting the just used ID before info about its deletion is sent to client.

			debug("sendUpdateAndRemember: no ID for "+io.getName()+" of type "+io.getType());
			return;
		}

		
		// If client gets an object before that objects parent it will have trouble. So make sure we have sent it.
		final DbContainer parentObject=io.getParent();
		if (parentObject!=null)
		{
			// Parent exist, 
			final int parentId=parentObject.getId();
			// Is it an object that has an ID?
			if (parentId>=0)
			{
				// It has an ID, we can send it.
				// Have we sent it to client?
				final Integer parentCounter=latestVersionOfIdObjectSent.get(parentId);
				if (parentCounter==null)
				{
					// Parent is not yet sent to client. 
					// Send it first (before sending the object to be updated).
					sendUpdateAndRemember((DbIdObj)parentObject);
				}
				else
				{
					// Do nothing, since we have a counter for it we know that the parent is already sent.
				}
			}
			else
			{
				// Do nothing, the parent is not an object we can send.
			}
		}
		else
		{
			// Do nothing, there is no parent,
		}

		final int changeCounter=io.getDbChangedCounter();
		final Integer sentCounter=latestVersionOfIdObjectSent.get(id); // will be null if not yet sent.
		
		// Sending mirrorAdd/mirrorUpdate
		// Format:
		// mirrorAdd/mirrorUpdate objectId parentId objectType ...
		
		if ((sentCounter!=null) && (changeCounter==sentCounter))
		{
			debug("do we need to send this?"); // This don't seem to happen anyway.
		}
		
		
		WordWriter ww = new WordWriter();
		//ww.writeWord((sentCounter==null) ? "mirrorAdd" : "mirrorUpdate");
		ww.writeWord((sentCounter==null) ? "ma" : "mu");
		ww.writeInt(id); // id of this object
		ww.writeInt(io.getParentId()); // id of parent object
		ww.writeWord(io.getType());
		io.writeSelf(ww);

		
		final String str=ww.getString();
		
		/*if (sentCounter==null)
		{
			debug("toClient " +str);
		}*/
		
		stc.writeLine(str); // TODO: worldBase should not be read locked when calling stc.writeLine since that call may block. Instead this function shall return a string, the string is sent after worldBase.unlockRead(); 

		// remember what has been sent to client
		latestVersionOfIdObjectSent.put(id, changeCounter); // remember version of all units sent. 
	}
		
	
	protected int sendUpdateAndRememberIfVisible(DbStorable io) throws IOException
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
		//ww.writeWord("mirrorRemove");					
		ww.writeWord("mr");
		ww.writeInt(id);
		String str=ww.getString();
		//debug(str);
		stc.writeLine(str);
	}


	// -------------------------------------------------------------------------------------------------------------------------------
	// TODO this is inefficient we need to be able to get removals via notification and not scan this list of everything we have sent!
	// Iterating this list we could do little by little so that we can reduce its size.
	// -------------------------------------------------------------------------------------------------------------------------------
	// 
	// Returns the number of objects removed
	public int findRemovalsAndRemember() throws IOException, InterruptedException
	{
		//debug("findRemovalsAndRemember");
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
		//
		Iterator<Map.Entry<Integer, Integer>> iterator = latestVersionOfIdObjectSent.entrySet().iterator();
		while(iterator.hasNext())
		{
			final Map.Entry<Integer, Integer> entry = iterator.next();  // ConcurrentModificationException on this line, but why?
		    final int key=entry.getKey(); // key is an object ID.

			
			final DbIdObj io=worldBase.getDbIdObj(key);
			if ((io==null) || (!io.isVisibleTo(playerAvatar)))
			{
				// An object has been removed, tell client about it.
				debug("findRemovalsAndRemember ~"+key);
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
	
	
	// This is deprecated since we must not send to much to client at once, it would cause severe lag.
	public int findAndSendDbUpdatesToClientAll() throws IOException, InterruptedException
	{
		debug("findAndSendDbUpdatesToClientAll");
		
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
			for (final DbIdObj io : worldBase.idList.dbListId)
			{
				updateObjToClient(io);			
			}
		}
		finally
		{
			worldBase.unlockRead();
		}
		
		idObjectsToUpdate.clear();
		
		//stc.writeLine("mirrorUpdated "+serverSequenceNumberToSend++);
		return n;
	}

	// Deprecated, use "updateObjToClient(final DbStorable io)" instead.
	/*
	public int updateObjToClient(int id) throws IOException
	{
		final DbIdObj io=worldBase.getDbIdObj(id);
		return updateObjToClient(io);
	}
	*/
	
	// Returns:
	// 0 if nothing needed to be sent
	// 1 if object was sent
	public int updateObjToClient(final DbStorable io) throws IOException
	{
		{
			final int id = io.getId();
			final Integer prevChangeCounter=latestVersionOfIdObjectSent.get(id);
			if (prevChangeCounter==null)
			{
				// This object is new, not previously sent to client
				sendUpdateAndRememberIfVisible(io);
				return 1;
			}
			else
			{
				// an object previously sent, sending again is only needed if it has been changed since then
				if (io.isChanged(prevChangeCounter))
				{
					// It has been changed, send to client.
					sendUpdateAndRememberIfVisible(io);
					return 1;
				}
			}
			
		}
		
		return 0;
	}
	
	// Send an objects data to client. Send also all child objects recursively.
	public int updateObjToClientRecursive(final DbIdObj io) throws IOException
	{
		debug("updateObjToClientRecursive "+io.getNameAndId());
		int n=updateObjToClient(io);

		// Send also all objects stored in "io".
		if (io.listOfStoredObjects!=null)
		{
			for(DbStorable db: io.listOfStoredObjects)
			{
				n+=updateObjToClientRecursive((DbIdObj)db);
			}
		}
		
		
		return n;
	}

	// This method will iterate the list of objects that has been changed and send updates to the client if needed.  
	public int iterateUpdatesToClient() throws IOException, InterruptedException
	{
		//debug("iterateUpdatesToClient "+idObjectsToUpdate.size());
		int n=0;
		if (idObjectsToUpdate.size()>0)
		{
			// not empty
			worldBase.lockRead();
		    try 
		    {
			    try 
			    {
			    	for(;;) // loop until InterruptedException
			    	{
						int id=idObjectsToUpdate.take(0);
						final DbIdObj io=worldBase.getDbIdObj(id);
						
						if (io==null)
						{
							// An object previously sent to client no longer exist, tell client to remove it.
							debug("sendRemoved ~"+id);
							sendRemoved(id);
							latestVersionOfIdObjectSent.remove(id);
						}
						else if (!io.isVisibleTo(playerAvatar))
						{
							// An object previously sent to client no longer visible, tell client to remove it.
							debug("notVisible ~"+id);
							sendRemoved(id);
							latestVersionOfIdObjectSent.remove(id);
						}
						else
						{
							n+=updateObjToClient(io);
						}
			    	}
			    } catch (InterruptedException e) {
					// do nothing, this is normal when idObjectsToUpdate is empty
			    }

				// Probably this code is not needed if we do "sendRemoved(id)" and "latestVersionOfIdObjectSent.remove(id);" above. But will run it once in a while anyway.
			    if (cleanupCount==0)
			    {
			    	n+=findRemovalsAndRemember();
			    	cleanupCount=1000;
			    }
			    else
			    {
			    	--cleanupCount;
			    }
				
			}
			finally
			{
				worldBase.unlockRead();
			}
		    
	    }
		return n;
	}
	
	// This method will check if it is OK to send more updates to client and send updates.
	// It will look at some acknowledge counters to be sure client is still connected before sending updates. 
	public void findAndSendDbUpdatesToClient() throws IOException, InterruptedException
	{
		//debug("findAndSendDbUpdatesToClient");
		int n=0;

		// Do not send to many updates. Client or network might not keep up.
		final int seqNrDiff = serverSequenceNumberToSend - serverSequenceNumberReceived;
		if ( seqNrDiff > 16)
		{
			if (debugLogCounter==10)
			{
				debug("network lag? "+serverSequenceNumberToSend+" "+serverSequenceNumberReceived);
			}
			debugLogCounter++;
		}
		else
		{		
			if (idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity())
			{
				debug("findAndSendDbUpdatesToClient, idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity(), "+idObjectsToUpdate.size());
				// queue has gotten full. Ignore the queue, instead scan database from scratch.
				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
				// TODO We can not have this when the world is big since we don't want to send everything to client in update. We need to activate findDbUpdatesToClientRange functionality in YukigassenServer instead. Or we must allow the queue to grow as much as needed.
				// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
				n+=findAndSendDbUpdatesToClientAll();
				// idObjectsToUpdate.clear(); Not needed here, it is done in findAndSendDbUpdatesToClientAll.
			}
			else
			{
				// the notification queue was not full, send updates using it, if is not empty.
				n+=iterateUpdatesToClient();
			}
			debugLogCounter=0;
		}

	    if ((n>0) || (clientSquenceNumberReceived != clientSquenceNumberReceivedAndAcked))
	    {
	    	clientSquenceNumberReceivedAndAcked = clientSquenceNumberReceived;
	    	stc.writeLine("mirrorUpdated "+ serverSequenceNumberToSend + " "+clientSquenceNumberReceivedAndAcked);
	    	serverSequenceNumberToSend++;
	    	// The client is expected to reply to this with mirrorAck <seq nr>
	    }
	}
		
		

	
	// This is called by other threads when something in the "world" database has been updated.
	// Notify can be called from various threads so do only thread safe stuff here.
	// subscribersRef is not used since changes in the world database is our only subscription.
	// sendersRef is expected to be an object ID.
	// TODO This solution is inefficient. It is a costly operation to add to linked queues, it is done for all connected clients and its done with database write locked. A better way is to use a simple round buffer within the database. Only one round buffer is needed. Clients can get changes from it while database is only read locked.
	public void notifyRef(int subscribersRef, int sendersRef)
	{
		if (idObjectsToUpdate.size() == idObjectsToUpdate.getCapacity())
		{
			// debug("idObjectsToUpdate is full");
			// queue is full
			// ignore further notifications.
			// we will instead scan the entire database for changes. See findAndSendDbUpdatesToClient.
		}
		else
		{				
			final DbBase db = worldBase.getDbIdObj(sendersRef);
			if (db!=null)
			{

				// The following few lines are only debugging, can be removed later. Oops this debug code does not work, not thread safe.
				/*int v=latestVersionOfIdObjectSent.get(sendersRef);
				if (db.getDbChangedCounter() == v)
				{
					error("no change?");
				}*/
					
					
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
				error("notify "+sendersRef+" null");
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
