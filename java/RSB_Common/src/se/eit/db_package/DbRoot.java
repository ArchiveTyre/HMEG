/*
DbRoot.java

This is an object that can reside in the database, it can contain other objects in its turn,
it has a name and it can be saved to file. 

This object is called rootObj but since there can be more than one that is not a perfect name.
Perhaps something like "object that when saving to file is saved as its own file" but that is too long.
This is the root of objects that are saved to one file so in that way the name is ok.

Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

*/


package se.eit.db_package;
import se.eit.rsb_package.*;




import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import se.eit.web_package.*;


// This is intended to be the root object for the database.
// It contains a ReentrantReadWriteLock to be used when reading or writing in the database.

public class DbRoot extends DbThreadSafe {

	private ReentrantReadWriteLock rwl=null;
		
	//boolean needSave=true; // save to disk should be needed only if this is true. This variable is probably not needed.

	// This counter is to be incremented every time a data base is write locked. 
	// That way we can know if it has changed by only looking at this counter. 
	// Further all objects that are changed shall have their changed counters 
	// set to the same value as this. That way we can also know which objects 
	// have been updated since last update and which have not.  
	int rootUpdateCounter; 
	
	//StringFifo updateFifos[]=new StringFifo[8]; 	


	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return DbRoot.class.getSimpleName();	
	}

	public DbRoot()
	{	
		super();
		rwl=new ReentrantReadWriteLock();
	}

	
	public DbRoot(DbBase parent, String name)
	{
		super();
		parent.addObject(this);
		this.setName(name);

		rwl=new ReentrantReadWriteLock();
	}
	
	
	
	@Override	
	public void readSelf(WordReader wr)	
	{
		lockWrite();		// TODO: probably enough with a debugWriteLock() check here.
		try 
		{
			super.readSelf(wr);
			rootUpdateCounter=wr.readInt();
		}
		finally
		{
			unlockWrite();
		}
	}
	
	
	
	@Override	
	public void writeSelf(WordWriter ww)
	{		
		lockRead();		
		try 
		{
			super.writeSelf(ww);
			ww.writeInt(rootUpdateCounter);
		}
		finally
		{
			unlockRead();
		}
	}
	
	
	
	@Override	
	public void readSelfRecursiveAndLink(DbBase parent, WordReader wr)
	{
		lockWrite();		
		try 
		{
			super.readSelfRecursiveAndLink(parent, wr);
		}
		finally
		{
			unlockWrite();
		}
	}
	
	
	public void readChildrenRecursive(WordReader wr) throws NumberFormatException
	{
		lockWrite();		
		try 
		{
			super.readChildrenRecursive(wr);
		}
		finally
		{
			unlockWrite();
		}
	}

	public void readRecursive(WordReader wr) throws NumberFormatException
	{
		lockWrite();		
		try 
		{
			super.readRecursive(wr);
		}
		finally
		{
			unlockWrite();
		}
	}

	
	@Override	
	public void writeRecursive(WordWriter ww)
	{		
		lockRead();		
		try 
		{
			super.writeRecursive(ww);
		}
		finally
		{
			unlockRead();
		}
	}
	
	/*
	public DbNamed findRelativeFromNameOrIndex(String nameOrIndex)
	{
		DbNamed no;
		lockRead();		
		try 
		{
			no=super.findRelativeFromNameOrIndex(nameOrIndex);
		}
		finally
		{
			unlockRead();
		}
		return no;
	}
	*/

	@Override	
	public DbRoot getDbRoot()
	{
		return this;
	}	

	// It is the responsibility on everyone calling this method to also call the unlockRead
	public void lockRead()
	{
		//debug("lockRead");
		rwl.readLock().lock();
	}

	public void unlockRead()
	{
		//debug("unlockRead");
		rwl.readLock().unlock();
	}

	// It is the responsibility on everyone calling this method to also call the unlockWrite
	public void lockWrite()
	{
		//debug("lockWrite");
		rwl.writeLock().lock();
		incDbRootUpdateCounter();
	}

	public void unlockWrite()
	{
		//debug("unlockWrite");
		rwl.writeLock().unlock();
	}
	
	/*
	@Override	
	public void tickRecursiveMs(int deltaMs)
	{		
		//debug("tickMs");
		lockWrite();
		try 
		{
			//incDbRootUpdateCounter(); // not needed here, its called at every lockWrite.
			super.tickRecursiveMs(deltaMs);
		}
		finally
		{
			unlockWrite();
		}
	}
    */
	
	
	// Iterate only root objects
	/*
	public DbRoot iterateDbRoot(DbBase ro)
	{
		for (DbStorable d : listOfStoredObjects)
		{
			if (d instanceof DbRoot)
			{
				return (DbRoot)d;
			}
		}
		return null;
	}
	*/
	
	public int getNOfDbRoot()
	{
		int n=0;
		if (listOfStoredObjects!=null)
		{
			for (DbStorable d : listOfStoredObjects)
			{
				if (d instanceof DbRoot)
				{
					++n;
				}
			}
		}
		return n;
		
	}
	
	// save all sub root objects
	// TODO: Would like file format to be "json.org"
	public void saveRecursive(GlobalConfig config)
	{
		lockRead();
		try {			
			
			if (getNOfDbRoot()>0)
			{	
				// There are other DbRoot so let those save themselves instead
				for (DbStorable go : listOfStoredObjects)
				{
					if (go instanceof DbRoot)
					{
						DbRoot ro = (DbRoot)go;
						ro.saveRecursive(config);
					}				
					else
					{
						error("mixed DbRoot and non DbRoot in "+ getName());
					}
				}
			}
			else
			{
				// There are no other DbRoot so save this
				saveSelf(config);
			}
		}
		finally
		{
			unlockRead();
		}			
	}
	
	
	public void saveSelf(GlobalConfig config)
	{
		lockRead();
		try {			
			
			// http://docs.oracle.com/javase/6/docs/api/java/io/File.html
			String fileName=config.savesRootDir+"/"+getNameAndPath(".")+".txt";
			System.out.println("saving to " + fileName);
			
			File f1 = new File(fileName+"_");
			FileWriter fw = new FileWriter(f1);
			WordWriter fww = new WordWriter(new PrintWriter(fw));
			
			// Write name and version
	  		final String nameAndVersion=Version.getNameAndVersion();
	  		fww.writeString(nameAndVersion);

			
        	//fww.println("");
			
			// Write type of object
        	fww.writeWord(getType());
        	
        	// Dump objects internal data
        	writeRecursive(fww);
        	
        	// Close the writer and stream
        	fww.close();
        	fw.close();
        	
        	
			File f2 = new File(fileName+"~");
			if (f2.exists())
			{
				f2.delete();
			}								
			File f3 = new File(fileName);
			File f4 = new File(fileName+"~");
			f3.renameTo(f4);
			f1.renameTo(f3);
        	
        	
        	System.out.println("path " + f3.getAbsolutePath());
        	
        	//needSave=false;

		} catch (IOException e) {
			System.err.println("\nSave to file failed");
			e.printStackTrace();       	
		}
		finally
		{
			unlockRead();
		}			
	}


	// deprecated, use findGameObjNotRecursive instead
	/*
	public GameObj findGameObj(String name)
	{
		debug("findGameObj is deprecated");
		return findGameObjNotRecursive(name);		
	}
	*/
	

	
	// Searches all sub objects for a game object with the given name.
	// First found is returned even if there are more than one.
	// Databases should be read lock before calling this.
	public DbRoot findDbRootRecursive(String name, int recursionDepth)
	{
		debugReadLock();
		
		if (listOfStoredObjects!=null)
		{
			for (DbStorable g : listOfStoredObjects)
			{
			  if (g instanceof DbRoot)
			  {
				DbRoot r = (DbRoot)g;
				if (name.equals(g.getName()))
				{
					return r;
				}
	
				// Perhaps recursively in a sub object?
				if (recursionDepth>0)
				{
					DbRoot s = r.findDbRootRecursive(name, recursionDepth-1);
					if (s!=null)
					{
						return s;
					}
				}
			  }
			}		
		}
		return null;
	}

	/*
	private DbRoot findDbRootRecursive(String name)
	{
		return findDbRootRecursive(name, Integer.MAX_VALUE);
	}
	*/


	public DbRoot findDbRootNotRecursive(String name)
	{
		return findDbRootRecursive(name, 0);
	}
	
	public DbRoot findDbRootNotRecursiveReadLock(String name)
	{
		lockRead();
		try
		{
			return findDbRootRecursive(name, 0);		
		}
		finally
		{
			unlockRead();			
		}
	}

	
	// To find a database (alias root object).
	public DbRoot findDb(String name)	
	{
		DbRoot db = null;

		lockRead();
		try
		{
			db = findDbRootNotRecursive(name);
		}
		finally
		{
			unlockRead();
		}
		
		if (db==null)
		{
			debug("did not find DbRoot "+name);
		}
		
		return db;		
	}
	

	public int addObjectAndSave(DbBase gameObjToAdd, GlobalConfig config)
	{
		int r=0;
		lockWrite();
		try {
			r=super.addObject(gameObjToAdd);
			saveRecursive(config);
		}
		finally
		{
			unlockWrite();
		}
	
		return r;		
	}
	
	/*
	public DbNamed findDbNamedRecursive(String name, int recursionDepth)
	{
		lockRead();
		try {
			return super.findDbNamedRecursive(name, recursionDepth);
		}
		finally
		{
			unlockRead();
		}			
	}
	*/
	
	@Override	
	public void listChangedObjects(int previousUpdateCounter, WordWriter ww)
	{
		lockRead();
		try {
			super.listChangedObjects(previousUpdateCounter, ww);
		}
		finally
		{
			unlockRead();
		}			
	}
	
	@Override	
	public void listSubObjects(PrintWriter pw, String prefix)
	{
		lockRead();
		try {
			super.listSubObjects(pw, prefix);
		}
		finally
		{
			unlockRead();
		}			
	}

	
	/*
	public int addGameObj(DbBase gameObjToAdd)
	{
		int r=0;
		try {
			lockWrite();
			r=super.addGameObj(gameObjToAdd);
		}
		finally
		{
			unlockWrite();
		}
	
		return r;		
	}
	*/
	// Call this from methods for which a read lock should have been made before calling.
	@Override	
    public void debugReadLock()
    {
    	// These code lines can be commented out when the code has been tested.
    	if (GlobalConfig.DEBUG_READ_LOCKS)
    	{
	    	if ((rwl!=null) && (!rwl.isWriteLockedByCurrentThread()) && (rwl.getReadLockCount()==0))
	    	{
	    		//debug("it should be read locked but was not "+getName());
	    		System.out.flush();
	    		System.err.println(className()+" it should be read locked but was not '"+getName()+"'");
	    		Thread.dumpStack();	    		
	    	}
    	}
    }
	
	// Call this from methods for which a write lock should have been made before calling.
	@Override	
    public void debugWriteLock()
    {
    	// This code can be commented out when the code has been tested.
    	if ((rwl!=null) && (!rwl.isWriteLockedByCurrentThread()))
    	{
    		error("it should be write locked but was not '"+getName()+"'");
    	}
    }
    
    
    private void incDbRootUpdateCounter()
    {
    	rootUpdateCounter++;
    }
    
	public int getDbRootUpdateCounter()
    {
    	return rootUpdateCounter;
    }
    
    
	@Override	
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);
		pw.println(prefix+"rootUpdateCounter "+rootUpdateCounter);
	}
    
	//public abstract void notifySubscribers(int id);

	@Override
	public String getNameAndPathInternal(String separator)
	{
		return separator;
	}

	/*
	@Override
	public void tickMs(long tickTimeMs)
	{
		if (listOfStoredObjects!=null)
		{
			for (DbStorable s : listOfStoredObjects)
			{
				if (s instanceof DbRoot)
				{
					s.tickMs(tickTimeMs);
				}
			}
		}

		//super.tickMs(tickTimeMs);
		
	}
	*/

}