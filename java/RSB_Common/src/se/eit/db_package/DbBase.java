/*
DbBase.java

The data base object (DbBase) is that base object for objects that can be stored in our data base.
If they need to be able to contain other data base objects then the extension DbContainer can be used.


Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-14
Created by Henrik Bjorkman (www.eit.se)

*/


package se.eit.db_package;

import java.io.PrintWriter;
import se.eit.web_package.*;


//import javax.media.opengl.GL2;

// DbBase is the base class for DbStorable.
//
// In DbBase we put methods that shall be implemented by extending (alias sub, specialized, derived) classes.
// Don't put variables in this class. Create a sub class for that.
// In this class put only:
// * methods that don't need class variables. 
// * abstract methods
// * methods returning a default value
// * methods giving an error message if not overridden by derived class.
// Most classes that extend this class shall also be listed in DbContainer.createObj so that they can be read/parsed from file.

public abstract class DbBase {

	//static final boolean DEBUG=false;
	   

	//int changedCounter=0;

	public static void static_error(String str)
	{
		WordWriter.safeError(className()+": "+str);
		System.exit(1);
	}

	public static void static_debug(String str)
	{
		WordWriter.safeDebug(className()+": "+str);
	}
	
	protected String debugPrefix()
	{
		String prefix=getType();
		final String name=this.getName();
		if (name!=null)
		{
			prefix+=" "+name;
		}
		prefix+=" "+this.getId();
		return prefix;
	}
	
	protected void debug(final String str)
	{
		WordWriter.safeDebug(debugPrefix()+": " + str);
	}
	
	
	protected void error(String str)
	{
		WordWriter.safeError(className()+" "+getNameAndPath("/")+" "+debugPrefix()+": "+ str);
		System.err.println("will do System.exit(1) now");
		System.exit(1);
	}
	
	protected void println(String str)
	{
		System.out.println(className()+": " + str);
	}

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return DbBase.class.getSimpleName();	
	}

	public String getType()
	{	
		return this.getClass().getSimpleName(); // This gives the name of the extended class, that is this does not always give string "DbBase".
	}

	public abstract String getDbRootNameAndPath(String separator);
	/*
	public String getDbRootNameAndPath(String separator)
	{	
		DbRoot ro = this.getDbRoot();
		if (ro!=null)
		{
			return ro.getNameAndPath(separator);
		}
		return "unknown";
	}
    */
	
	// Gives full path to the absolute root of all data bases loaded
	public String getNameAndPath(String separator)
	{
		error("getNameAndPath");
		return null;
	}
	
	// Gives full path within a data base.
	public String getNameAndPathInternal(String separator)
	{
		error("getNameAndPathToRoot");
		return null;
	}
  	
	public String getIndexPathWithinDbRoot()
	{
		return getIndexPathWithinDbRoot(".");
	}
	
	// If separator is given as " " then this is the inverse of getObjFromPathWithinDbRoot
	public abstract String getIndexPathWithinDbRoot(String separator);
  	
	public abstract String getIndexPath(String separator);

	/*
	public static String getTypeStatic()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return DbBase.class.getSimpleName();
	}
	*/
		
		
	/*
	public String getTypePath()
	{
		return getType();
	}
	*/


	/*public final String getName()
	{
		return name;
	}*/
	

	public DbBase()
	{
	}
	
	// Copy constructor, is this used at all?
	public DbBase(final DbBase org)
	{
		error("is this used?");
	}
	
	
	
	// deserialise from wr
	// sub classes with additional member variables that need 
	// to be saved to disk shall implement this method.
	public abstract void readSelf(WordReader wr);	

	// Serialise to ww
	// Write all available variables within this object.
	// Sub classes with additional member variables that need to be saved to disk must implement this method.
	// Those overriding methods shall also call "super.write".
	public abstract void writeSelf(WordWriter ww);

	
	public abstract void writeRecursive(WordWriter ww);
	
	public abstract void readRecursive(WordReader wr);
	public abstract void readSelfRecursiveAndLink(DbBase parent, WordReader wr);

	// This is a clever way to make a string from a stream, will keep it for reference.
	// http://c2.com/cgi/wiki?StringBuffer
	/*
	public String toString()
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter (stringWriter);			
		WordWriter ww=new WordWriter(out);
		writeSelf(ww);
		ww.close();
		return stringWriter.toString();
	}
	*/
	
	public String toString()
	{
		WordWriter ww=new WordWriter();
		writeSelf(ww);
		return ww.getString();
	}
	
	// Gives the index (alias slot) at which this object is stored in the containing obj.
	public abstract int getIndex();
	
	
	// This will return the nearest root object for the database in which this object resides. 
	// If this is a DbRootect there is an override in DbRoot class that returns itself.
	// Is this thread save? perhaps not 100% but hopefully it will work anyway.
	public abstract DbRoot getDbRoot();
	
	public abstract DbSuperRoot getDbSuperRoot();

	// Same as getDbRoot but to get an DbIdList (DbRoot are usually also DbIdList) so one of these methods can be removed later...
	public abstract DbIdList getDbIdList();

	public abstract void debugReadLock();
   
    public abstract void debugWriteLock();
    
	// This must be overridden in an extended class.
    public void incDbChangedCounter()
    {
		error("incDbChangedCounter");    	
    }
    
	public abstract boolean isChanged(int rootCounter);
	
	// Gives the object in which this object is stored.
	public abstract DbContainer getContainingObj();
	
	public DbContainer getParent()
	{
		return getContainingObj();
	}
	
	protected abstract void setContainingObjIndex(DbBase bo, int i);
	
	public abstract void listInfo(WordWriter pw, String prefix);
	
	// returns 1 if info to set was found, -1 if not found.
	public int setInfo(WordReader wr, String infoName)
	{
		return -1;
	}
	
	
	public abstract String getName();
	
	public int getId()
	{
		return -1;
	}
	
	// This must be overridden in an extended class.
	public void writeIdOrPath(WordWriter ww)
	{
		ww.writeString(getIndexPathWithinDbRoot(" "));
		error("incDbChangedCounter");    	
	}
	
	// This must be overridden in an extended class.
	public DbIdObj getDbIdObj(int i)
	{
		error("getDbIdObj");    	
		return null;
	}
	
	// This must be overridden in an extended class.
	/*public D3ObjWithPos getContainingD3ObjWithPos()
	{
		error("getContainingD3ObjWithPos");    	
		return null;		
	}*/
	
	//public abstract void tickSelfMs(int deltaMs);
	//public abstract void tickRecursiveMs(int deltaMs);
	
	/*
	public void tickMs(long tickMs)
	{
		error("tickMs");    	
	}
	*/
	
	// Remove and delete all sub objects, recursively
	public abstract void clearStored();
	
	//protected abstract DbBase getObjFromIndexWithinDbRootWr(String path, WordReader wr);
	
	/*public int moveBetweenRooms(final D3ObjWithPos from, final D3ObjWithPos to)
	{
		error("moveBetweenRooms");    	
		return -1;		
	}*/
	
	// Find a sub object of given class.
	// A Call to this can look like this: EmpireTerrain et=(EmpireTerrain) bo.findRecursive(0, EmpireTerrain.class);
	public abstract DbBase findRecursive(int recursionDepth, Class<?> cls);
	
	// Unfortunately this can only find objects that are exactly of the given class 
	// That is if an object is of an extended class then it is not found when looking for one of its base classes.
	//public abstract DbBase getContainingObjOfClass(Class<?> cls);
	
	public abstract void listChangedObjects(int previousUpdateCounter, WordWriter ww);

	
	// This method is supposed to be overridden in an extended class.
	/*
	public void registerInDbIdList()
	{
		error("registerInDbIdList");
	}
	*/
	
	
	// Removes an object stored in this object. But this does not remove it from all lists such as DbIdList.
	// If the stored object is to be deleted and never used again use deleteObject instead.
	// deprecated, the name is misleading since it sounds like the opposite of addObject which this is not. Use removeDbStorable instead.
	protected abstract int removeObject(DbBase obj_to_remove);
	
	// Removes an object stored in this object. But this does not remove it from all lists such as DbIdList.
	// If the stored object is to be deleted and never used again use deleteObject instead.
	protected abstract int removeDbStorable(DbBase obj_to_remove);
	
	
	// Removes an object from all container and IdList
	protected void deleteObject(DbBase obj_to_remove)
	{
		if (obj_to_remove.getParent()!=this)
		{
			debug("not my child "+obj_to_remove.getName());
		}
		obj_to_remove.unlinkSelf();
	}
	
	
	// link this object with all DbContainer and DbIdList etc that it shall be registered in.
    // objects stored this way shall do unlinkSelf if they are to be removed.
	// Classes like DbIdObj and DbStorable shall have a specialization of this.
	public abstract void linkSelf(DbBase parentObj);	
	
	// unlink this object from all list it is part of for example DbContainer and DbIdList
	// All objects that have been stored with a call to storeSelf shall use this method to unlink themselves when they are to be removed.
	// Classes like DbIdObj and DbStorable shall have a specialization of this.
	public abstract void unlinkSelf();	
	
	
	public int addObject(DbBase gameObjToAdd)
	{
		gameObjToAdd.linkSelf(this);
		return 0;
	}

	public abstract String getNameOrIndex();
	
	public abstract DbBase findDbNamedRecursive(String name);
	
	public abstract DbBase findDbNamedRecursive(String name, int recursionDepth);

	public abstract DbBase findDbNamedRecursive(String name, int recursionDepth, Class<?> cls);	

	public abstract void listNameAndPath(PrintWriter pw, int recursionDepth, String prefix);

	public abstract DbBase getObjFromIndex(int index);
	
	public abstract DbBase findGameObjNotRecursive(String name);
	
	// Deprecated, use DbIterator instead.
	//public abstract DbBase iterateStoredObjects(DbBase d);
	
	public abstract DbBase findRelativeFromNameOrIndex(String nameOrIndex);	
	
	public abstract String getObjInfoPathNameEtc();
	
	public abstract void listSubObjects(PrintWriter pw, String prefix);
	
	public abstract int moveBetweenRooms(DbContainer from, DbContainer to);

	public int moveToRoom(DbContainer to)
	{
		return this.moveBetweenRooms(this.getParent(), to);
	}

	
	public abstract boolean changeInfo(String tag, String value);
	
	//public abstract void init(final GL2 gl);
	
	//public abstract void dispose(final GL2 gl)
	
	/*
	public void registerSelfInDbIdListAndAdd(DbBase container)
	{
		error("registerSelfInDbIdListAndAdd");		
	}
	*/
	
	// This must be overridden in an extended class.
	public void registerSelfInDbIdListAndAddUsingLockWrite(DbBase container)
	{
		error("registerSelfInDbIdListAndAddUsingLockWrite");		
	}

	
	public void setUpdateCounter()
	{
		error("setUpdateCounter");
	}
	
	public int getDbChangedCounter()
	{
		error("getDbChangedCounter");
		return -1;
	}
	
	
	public abstract DbBase getObjFromIndexPathWithinDbRoot(String path);
	public abstract DbBase getObjFromIndexPath(String path);

	public abstract DbBase findObjectByNameIndexOrId(WordReader wr);
	public abstract DbBase findObjectByNameIndexOrId(String path);

	//public abstract DbBase getObjFromIndexWithinDbRootWr(WordReader wr);
	
	// Deprecated, use linkSelf instead.
	/*public void registerSelfAndSubObjectsInDbIdList(DbIdList idList)
	{
		error("registerSelfAndSubObjectInDbIdList");
	}*/
	
	// This must be overridden in an extended class.
	/*public int regDbIdObj(DbIdObj idObj)
	{
		error("regDbIdObj");	
		return -1;
	}*/
	
	// getSubObj
	// gives a list of all sub objects, alias child objects
	// If there are much to be done with the sub objects (such as sending data to a client) 
	// this can be better than iterating since this read lock only while making the list.	
	// Returns an array with references (not copies) to all sub objects.
	public abstract DbBase[] getListOfSubObjects();

	public DbBase[] getListOfSubObjectsThreadSafe()
	{
		error("getListOfSubObjectsThreadSafe");	
		return null;		
	}
	
	public abstract long getGameTime();
	
	public abstract String toShortFormatedString();

}

