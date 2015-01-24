package se.eit.db_package;

public class DbThreadSafe extends DbChangedCounter {

	public DbThreadSafe() {
		super();	
	}


	public DbBase[] getListOfSubObjectsThreadSafe()
	{	
		final DbRoot db=getDbRoot();
		db.lockRead();
		try
		{
			return getListOfSubObjects();
		}
		finally
		{
			db.unlockRead();
		}
	}

	
	public int moveToRoomThreadSafe(DbContainer to)
	{
		
		final DbRoot db=getDbRoot();
		db.lockWrite();
		try
		{
			return moveToRoom(to);
		}
		finally
		{
			db.unlockWrite();
		}
		
	}


	public void addObjectThreadSafe(DbStorable obj)
	{
		final DbRoot db=getDbRoot();
		db.lockWrite();
		try
		{
			//obj.linkSelf(this);
			this.addObject(obj);
			obj.setUpdateCounter();  // This should be superfluous but there is some problem somewhere, without this the JS client will not get order updates so this is still needed but it should not be. Hopefully not needed now.
		}
		finally
		{
			db.unlockWrite();
		}
	}
	
	// Same as above but used if object is to be stored on a specific position.
	// Deprecated. Do setIndex on the object to add and then use "public void addObjectThreadSafe(DbStorable obj)". It will use the index the object wants.
	/*
	public void addObjectThreadSafe(DbStorable obj, int index)
	{
		final DbRoot db=getDbRoot();
		db.lockWrite();
		try
		{
			//obj.linkSelf(this, index);
			this.addAndRegObjAtIndex(obj, index);
		}
		finally
		{
			db.unlockWrite();
		}
	}
	*/


	public void delObjectThreadSafe(DbBase obj)
	{
		final DbRoot db=getDbRoot();
		db.lockWrite();
		try
		{
			obj.unlinkSelf();
		}
		finally
		{
			db.unlockWrite();
		}
	}

	
	public final int getNSubObjectsThreadSafe()
	{	
		final DbRoot db=getDbRoot();
		db.lockRead();
		try
		{
			return getNSubObjects();
		}
		finally
		{
			db.unlockRead();
		}
	}

}
