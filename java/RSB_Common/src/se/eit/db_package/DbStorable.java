/*
DbStorable.java

The data base object (DbStorable) is that base object for objects that can be stored in our data base.
If they need to be able to contain other data base objects then the extension DbContainer can be used.


Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-14
Created by Henrik Bjorkman (www.eit.se)

*/


package se.eit.db_package;

import se.eit.rsb_package.GlobalConfig;
import se.eit.web_package.*;




// DbStorable is the base class for all objects that can be stored in a DbContainer.



public abstract class DbStorable extends DbBase {

	
    static final int NO_INDEX = -1; // must be -1, it is assumed so in several places
    
	// Objects are stored in other objects
	protected DbBase containingObj=null;  // This is a reference to the object that this object is stored in, alias parent object 
	private int containingIndex=NO_INDEX; // This tells in which slot in the parent object that this object is stored

	//int changedCounter=0;


	public DbStorable()
	{
		super();	
	}
	
	public DbStorable(final DbStorable org)
	{
		super(org);	
		
		// DbContainer copy constructor must set these.
		// don't really know at this point what to set them to though.
		this.containingIndex=NO_INDEX; // org.superObjIndex;
		this.containingObj=null; // org.superObj
	}
	
	
	
	// deserialise from wr
	// sub classes with additional member variables that need 
	// to be saved to disk shall implement this method.
	@Override
	public void readSelf(WordReader wr)	
	{
		// The super class of DbStorable does not have a readSelf method so here a call to super.readSelf is not needed. But all derived classes shall call "super.read".
		//super.read(wr);
		/*final int i=wr.readInt();
		if (containingIndex==NO_INDEX)
		{
			// We have a problem. readSelf is used for new object and for updating objects. If object just recently moved in server but client don't know that yet the containingIndex sent from server is not yet the one client shall use since here the object is not yet moved to the new room. This will need some cleaning up later. The correct way must be to let updateObj also contain information about wither object have changed room. 
			// as a workaround we set containingIndex only if we don't have one already 
			containingIndex = i;
		}
		
		// just for debugging
		if (containingObj!=null)
		{
			if (containingObj.getObjFromIndex(containingIndex)!=this)
			{
				debug("readSelf: something wrong with index");
				//containingObj.setContainingObjIndex(this, containingIndex);			
			}
		}*/

		containingIndex = wr.readInt();
	}

	// Serialise to ww
	// Write all available variables within this object.
	// Sub classes with additional member variables that need to be saved to disk must implement this method.
	// Those overriding methods shall also call "super.write".
	@Override
	public void writeSelf(WordWriter ww)
	{
		// The super class of DbStorable does not have a writeSelf method so here a call to super.writeSelf is not needed. But all derived classes shall call "super.write".
		//super.write(ww);
		
		ww.writeInt(containingIndex);
	}

	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		//super.listInfo(pw, prefix);					
		pw.println(prefix+"containingIndex "+containingIndex);		
	}

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
	
	
	/*go.containingIndex=i;
	go.superObj=this;*/
	// This shall be called from DbContainer only.
	protected void setContainingObjIndex(DbBase bo, int i)
	{
		containingIndex = i;
		containingObj = bo;
	}
	
	public String getDbRootNameAndPath(String separator)
	{	
		DbRoot ro = this.getDbRoot();
		if (ro!=null)
		{
			return ro.getNameAndPath(separator);
		}
		return "unknown";
	}

	public String getNameAndPath(String separator)
	{	
        if (containingObj!=null)
		{	
			return containingObj.getNameAndPath(separator)+separator+containingIndex;
		}
		
		return separator+ separator +containingIndex;
	}
	
	public String getNameAndPathToRoot(String separator)
	{
        if ((containingObj!=null) && (!(containingObj instanceof DbRoot)))
		{	
			return containingObj.getNameAndPath(separator)+separator+containingIndex;
		}
		
		return separator + containingIndex;
	}
	
  	
	public String getIndexPathWithinDbRoot()
	{
		return getIndexPathWithinDbRoot(".");
	}
	
	// If separator is given as " " then this is the inverse of getObjFromPathWithinDbRoot
	// This is similar to getIndexPath but gives the path within a game data base, not all the way to program wide root.
	public String getIndexPathWithinDbRoot(String separator)
	{
        if (containingObj!=null)
		{	
        	// If there was a getIndexPathWithinDbRoot in class DbRoot we would perhaps not need this instanceof?
        	if (containingObj instanceof DbRoot)
        	{
        		return ""+containingIndex;
        	}
			return containingObj.getIndexPathWithinDbRoot(separator)+separator+containingIndex;
		}
		return ""+containingIndex;
	}
  	
	// If separator is given as " " then this is the inverse of getObjFromPath
	// This is same as getIndexPathWithinDbRoot but gives the path all the way up to program wide root
	public String getIndexPath(String separator)
	{
        if (containingObj!=null)
		{	
        	/*if (containingIndex<0)
        	{
        		return "";
        	}*/
			return containingObj.getIndexPath(separator)+separator+containingIndex;
		}
		return "";
	}
	
	

	
	@Override
	public int getIndex()
	{
		return containingIndex;
	}
	
	public void setIndex(int index)
	{
		containingIndex = index;
	}

	
	// This will return the root object for the database in which this object resides. 
	// Is this thread save? perhaps not 100% but hopefully it will work anyway.
	// NOTE if this object is a DbRoot itself it does not return itself.
	// HOWEVER there is an overriding method in DbRoot that will do just that.
	// so be aware...
	public DbRoot getDbRoot()
	{
		if (containingObj!=null)
		{
			// There is a getDbRoot in class DbRoot so why do we need this instanceof?
			if (containingObj instanceof DbRoot)
			{
			    return (DbRoot)containingObj;
			}
			else
			{
				return containingObj.getDbRoot();
			}
		}
		else
		{
			//debug("did not find root object");
		}	
		return null;
	}

	// Get an DbIdList above this object.
	// NOTE if this object is an DbIdList itself it does not return itself.
	// There is an overriding method in DbIdObj that can return a cashed value
	public DbIdList getDbIdList()
	{
		if (containingObj!=null)
		{
			// There is a getDbRoot in class DbRoot so why do we need this instanceof?
			if (containingObj instanceof DbIdList)
			{
			    return (DbIdList)containingObj;
			}
			else
			{
				return containingObj.getDbIdList();
			}
		}
		else
		{
			//debug("did not find root object");
		}	
		return null;
	}

	

    // alternative, but not tested version of debugReadLock
    public void debugReadLock()
    {
    	// These code lines can be commented out when the code has been tested.
    	if (GlobalConfig.DEBUG_READ_LOCKS)
    	{
    		if (containingObj!=null)
    		{
    			containingObj.debugReadLock();
    		}
    	}
    }
    
    

	// Call this from methods for which a write lock should have been made before calling.
    /*
    public void debugWriteLock()
    {
    	// This code can be commented out when the code has been tested.    	
    	DbRoot ro = getDbRoot();
    	if (ro!=null)
    	{
    		ro.debugWriteLock();
    	}
    }
    */

    
    // alternative, but not tested version of debugReadLock
	@Override
    public void debugWriteLock()
    {
    	// This code can be commented out when the code has been tested.
		if (containingObj!=null)
		{
			containingObj.debugWriteLock();
		}
    }

    
    
    /*
    public void incDbChangedCounter()
    {
		if (containingObj!=null)
		{
			containingObj.incDbChangedCounter();
		}
    	changedCounter++;
    }
	*/
   
    
    
	@Override
	public boolean isChanged(int rootCounter)
	{
		return false;
	}
   
	@Override
	public DbContainer getContainingObj()
	{		
		return (DbContainer)containingObj;
	}
	
	public int getParentId() 
	{
		if (containingObj instanceof DbIdObj)
		{
			return ((DbIdObj)containingObj).getId();
		}
		return -1;
	}
		
	
	// Perhaps not correct place for this method? But it will do for now.
	public long getGameTime()
	{
		if (containingObj!=null)
		{
			return containingObj.getGameTime();
		}

		return -1;
	}

	// Get a containing object that is of given class.
	/*
	public DbBase getContainingObjOfClass(Class<?> cls)
	{
		if (this.getClass() == cls)
		{
			return this;
		}
		else if (containingObj!=null)
		{
			containingObj.getContainingObjOfClass(cls);
		}
		return null;	
	}
	*/
	
	// Get a containing object that is of given class.
	// Unfortunately this can only find objects that are exactly of the given class 
	// That is if an object is of an extended class then it is not found when looking for one of its base classes.
	/*public DbBase getContainingObjOfClass(Class<?> cls)
	{
	    DbBase bo = this.getContainingObj();
	    
	    if (bo==null)
	    {
	    	return null;
	    }
	    else if (bo.getClass() == cls)
	    {
			return bo;
	    }
	    return bo.getContainingObjOfClass(cls);
	}*/

	public void linkSelf(DbBase parentObj)
	{
		if (parentObj instanceof DbContainer)
		{
			DbContainer co=(DbContainer)parentObj;
			containingIndex=co.addDbStorableToContainer(this);
			containingObj=parentObj;
		}
		else
		{
			error("could not store object");
		}
	}
	
	
	// Remove this object from the super objects list
	public void unlinkSelf()
	{
		if (containingObj instanceof DbContainer)
		{
			containingObj.removeDbStorable(this);
		}
	}
	
	public DbSuperRoot getDbSuperRoot()
	{
		if (containingObj instanceof DbSuperRoot)
		{
			return (DbSuperRoot)containingObj;
		}
		
		if (containingObj!=null)
		{
			return containingObj.getDbSuperRoot();
		}
		
		return null;
	}


}