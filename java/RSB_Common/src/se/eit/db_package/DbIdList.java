package se.eit.db_package;


import se.eit.web_package.*;


// Perhaps we could improve this code by using HashMap? 
// http://docs.oracle.com/javase/7/docs/api/java/util/HashMap.html

public class DbIdList extends DbRoot {

	
	protected DbList<DbIdObj> idList=new DbList<DbIdObj>();
	
	//Queue<Integer> unusedIds = new LinkedList<Integer>();  // A queue of unused IDs to replace latestIdGiven. The idea is to avoid reusing an ID too soon.
	
	
	public DbIdList()
	{
		super();
		
		// The IdList object will be object zero itself. First other object will be 1.
		// Not sure if it is a good idea that the IdList is in the list itself.
		// If an ID list is in another ID list it should have an ID assigned from there, but internally its ID shall be zero. How will we know which ID (of two possible) is wanted?
		idList.add(0, this);
		setId(0, this);  
	}





	// Get a reference to an object from its ID.
	@Override
	public DbIdObj getDbIdObj(int id)
	{
		if (id<0)
		{
			debug("no id "+id);
			return null;
		}
		return idList.get(id);
	}

	// Register ID object in list (unless it is already registered)
	// Returns id if ok, 
	// <0 if not ok
	// The caller must set the id in idObj to the returned value.
	public int addIdObj(DbIdObj idObj)
	{
		debugWriteLock(); // not thread safe, lock database for write before calling

		int id=idObj.getId();
		
		if (id>=0)
		{
		  idList.add(id, idObj);
		}
		else
		{
		  id=idList.add(idObj);
		}
		
		return id;
	}
	

	
	
	
	// This will do tick for all DbIdObj. The objects that are not subclasses of DbIdObj get their tick from DbRoot instead.
	// Since we now allow objects to move between rooms and that happens during tick we need to call tick using DbIdList instead of sub objects. Otherwise iterating the world got messed up when objects where moved as they may do during a tick.
    // TODO: Is this still used? Yes but need to look into this because this will take a lot of CPU time for perhaps little use.
    /*
	@Override
	public void tickRecursiveMs(int ms)
	{		
		lockWrite();
		try
		{
			//tickSelfMs(ms); // don't do self here since ID=0 is also self and called in the loop just below. May need to change this if we remove self from idList.

			for (DbIdObj s : idList)
			{
				s.tickSelfMs(ms);
			}			
		}
		finally
		{
			unlockWrite();
		}		
	}
    */

	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);
		pw.println(prefix+"idList.capacity "+idList.getCapacity());
	}

	// This will recursively check that all sub objects are registered in the id list. If they are not registered they are registered.
	/*public void registerSubObjectsInDbIdList()
	{
		if (listOfStoredObjects!=null)
		{
			for (DbStorable bo : listOfStoredObjects)
			{
				bo.registerSelfAndSubObjectsInDbIdList(this);
			}
		}
	}*/
	
	public int getDbIdListLength()
	{
		return idList.getCapacity();		
	}
	
	// Remove an id from the list
	// TODO: we should keep a queue of unused IDs so that we reuse only the one that has been unused the longest time.
	public void unregDbIdObj(DbIdObj idObj)
	{
		final int id=idObj.getId();

		// just for debugging, can be commented out later
		if ((idObj.getDbIdList()!=this) || (idList.get(id)!=idObj)) 
		{
			error("inconsistent db "+" "+id);
		}

		idList.remove(id);
		idObj.setId(-1, null);
	}

	
}
