/*
DbContainer.java

The data base object (DbBase) is that base object for objects that can be stored in our data base.
If they need to be able to contain other data base objects then this extension DbContainer can be used.
Perhaps this should be called Parent instead of Container?

Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

*/


package se.eit.db_package;

// Wish I could found a way to avoid this circular dependency...
import se.eit.web_package.*;



public abstract class DbContainer extends DbStorable {

	
	public DbList<DbStorable> listOfStoredObjects=null;
	

	
	public static void static_error(String str)
	{
		System.out.flush();
		System.err.println(className()+": static_error: "+str);
		Thread.dumpStack();
		System.exit(1);
	}
	
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return DbContainer.class.getSimpleName();	
	}

	
	public DbContainer()
	{
		super();	
	}

	// copy constructor, remember to create new objects of everything (dont't just simply copy references)
	public DbContainer(final DbContainer org)
	{	
		super(org);
	
		error("Not implemented yet, all sub objects are ignored");		
		/*
		listOfStoredObjects=new DbBase[org.listOfStoredObjects.length];
		
		for(int i=0;i<org.listOfStoredObjects.length;i++)
		{
			listOfStoredObjects[i]=org.listOfStoredObjects[i].deepCopy();
			listOfStoredObjects[i].superObj=this;
			listOfStoredObjects[i].superObjIndex=i;
		}
		*/
	}
	
	public void readChildrenRecursive(WordReader wr) throws NumberFormatException
	{
		clearStored();
		
		String m = wr.readWord();

		
		// Temporary workaround, this should not be needed, remove later...
		if (m.equals(""))
		{
			debug("expected begin marker '"+WordWriter.BEGIN_MARK+"' but found empty string");
			m = wr.readWord();
		}
		
		
		if (m.equals(WordWriter.BEGIN_MARK))
		{
			while (!wr.isNextEnd())
			{
				//int index = wr.readInt();				

				String t = wr.readWord(); // get type of object to parse

				// create an object of the desired type
				DbBase bo=RsbFactory.createObj(t);
				
				if (bo instanceof DbContainer)
				{
					((DbContainer)bo).readSelfRecursiveAndLink(this,wr);
				}
				
			}
			/*String e =*/ wr.readWord();
		
		}
		else
		{
			debug("expected begin marker '"+WordWriter.BEGIN_MARK+"' but found '"+m+"'");			
		}
	}

	
	
	// read self and all child objects
	public void readSelfRecursiveAndLink(DbBase parent, WordReader wr) throws NumberFormatException
	{
		clearStored();

		readSelf(wr);
		
		linkSelf(parent); // TODO: linkSelf is deprecated, use: parent.addChild(this);
				
		readChildrenRecursive(wr);
	}


	public void readRecursive(WordReader wr) throws NumberFormatException
	{
		clearStored();
	
		readSelf(wr);
		
		//linkSelf(parent);
				
		readChildrenRecursive(wr);	
	}
	
	
	public static DbBase staticParse(WordReader wr) throws NumberFormatException
	{
		DbBase bo = null;
		String t = wr.readWord(); // get type of object to parse
		bo=RsbFactory.createObj(t);
		bo.readRecursive(wr);
		return bo;
	}

	
		
	
	
	// serialize to ww
	// write all objects stored in this object
	@Override
	public void writeRecursive(WordWriter ww)
	{		
		//super.write(ww);
		//writeSelf(ww);

		debugReadLock();

		writeSelf(ww);

		ww.writeBegin();
		
		if (listOfStoredObjects!=null)
		{
			// get the size of the list of stored objects
			final int n = listOfStoredObjects.getCapacity();
			
			for(int i=0;i<n;i++)
			{
				DbBase d = listOfStoredObjects.get(i);
				if (d != null)
				{
					//DbContainer c=(DbContainer)d;

					//ww.writeInt(i);

					ww.writeWord(d.getType());
				
					d.writeRecursive(ww);
				}
			}
		}
		ww.writeEnd();
	}
		
	
	// Gives number of objects stored in this object,
	// not recursive
	public final int getNSubObjects()
	{
		debugReadLock();
		if (listOfStoredObjects!=null)
		{
			return listOfStoredObjects.size();
		}
		return 0;
	}
	
	
	// For most cases its better to use go.getIndex() directly than this.
	public int getIndexOfObj(DbBase go)
	{		
		// data base shall be read locked when this method is called. But this is called very often so the debug check can be commented out here for performance reasons.
		debugReadLock();

		// Most of these code lines are just for debugging (but not all). We need to know if we have inconsistencies in the database but none of these error should happen of course.
		if (go!=null)
		{
			final int index = go.getIndex();
			
			if ((index!=NO_INDEX))
			{
				if ((index>=0) && (index<listOfStoredObjects.getCapacity()))
				{
					if (listOfStoredObjects.get(index)!=go)
					{
						// This should NOT happen if code is bug free. Perhaps if there is a multi thread problem it can still happen?
						error("index not correct "+index);						
					}
				}
				else
				{
					// This should NOT happen if code is bug free. Perhaps if there is a multi thread problem it can still happen?
					error("index not in range "+index);
				}
				
				return index;
			}
			else
			{
				// This should NOT happen if code is bug free. Perhaps if there is a multi thread problem it can still happen?
				error("index is not set");
			}
		}
		else
		{
			//debug("getIndexOfObj null"); // it is allowed to ask for index of null. It will return NO_INDEX which must be -1.
		}		
		return NO_INDEX;
	}

	
	
	
	
	
	// Remove and delete all sub objects, recursively
	public void clearStored()
	{	
		debugWriteLock();
		
		//super.clear(); // if super class need clear remember to call it here.
		
		if (listOfStoredObjects!=null)
		{
			for (DbStorable s : listOfStoredObjects)
			{
				s.unlinkSelf();
			}
			
			listOfStoredObjects=null;
		}

	}
	
	public void unlinkSelf()
	{
		//debugWriteLock(); // check not needed as long as clearStored will check

		clearStored();
		super.unlinkSelf();
	}
	
	

	// Returns index if ok, 
	// <0 if not ok
	// The caller must set the index in objToAdd to the returned value.
	// Caller must also set containingObj
	protected int addDbStorableToContainer(DbStorable objToAdd)
	{
		debugWriteLock(); // not thread safe, lock database for write before calling

		if (listOfStoredObjects==null)
		{
			listOfStoredObjects = new DbList<DbStorable>();
		}

		
		if (objToAdd!=null)
		{
			// Check if the object has an index already.
			int index=objToAdd.getIndex();
			if (index!=NO_INDEX)
			{				
				// It does try to add it at that slot.
				listOfStoredObjects.add(index, objToAdd);
			}
			else
			{
				// It did not, store it with a new index.
				index = listOfStoredObjects.add(objToAdd);
			}
			return index;
		}
		else
		{
			error("addGameObj null");
			return -1;			
		}
	}
	
	
	// Same as addDbStorableToContainer but will also set containingObj and containingIndex
	protected int addDbStorable(DbStorable objToAdd)
	{
		final int i = addDbStorableToContainer(objToAdd);
		objToAdd.setContainingObjIndex(this, i);
		return i;
	}

	// Same as addObject but object does not need to be registered in IdList already (that is taken care of in derived classes).
	// Is this duplicate of registerSelfInDbIdListAndAdd? Can one of those be removed?
	/*
	public int addAndRegObjAtIndex(DbStorable bo, final int index)
	{
		return addObject(bo, index);
	}
	*/
	
	// deprecated, the name is misleading since it sounds like the opposite of addObject which this is not. Use removeDbStorable instead.
	protected int removeObject(DbBase obj_to_remove) 
	{
		return this.removeDbStorable(obj_to_remove);
	}
	
	// Non recursive remove of an object.
	// This is not "deleteDbBase". It is not specialized in other classes that keeps track of objects Most importantly the DbIdList.
	// It shall only be used when moving objects between containers (AKA rooms, AKA parent objects).
	// Returns <0 if not ok >=0 if ok
	protected int removeDbStorable(DbBase obj_to_remove) 
	{
		debugWriteLock(); // not thread safe, lock database for write before calling
		
		final int i=obj_to_remove.getIndex();
		
		if ((i >= 0) && (i<listOfStoredObjects.getCapacity()))
		{
			if (listOfStoredObjects.get(i)!=obj_to_remove)
			{
				error("wrong object in list " + i + " "+obj_to_remove.getType());				
			}
			else
			{
				listOfStoredObjects.remove(i);
			}
			//notifySubObjects(obj_to_remove.name + " left the room", obj_to_remove);
		}
		else if (i == NO_INDEX)
		{
			error("no index " + obj_to_remove.getType());
		}
		else
		{
			error("object to remove outside list: " + i+ " "+obj_to_remove.getType());
		}

		obj_to_remove.setContainingObjIndex(null, NO_INDEX);

		
		return 0;		
	}


	// Move this object from one DbContainer to another.
	// Returns zero
	// Special care needs to be taken regarding when this method is called or else things like iterating the list of objects during a tick can get messed up.
	// Derived classes may need to override this method in order to translate things like positions and velocity vectors.
	// deprecated use: 'moveBetweenRooms(DbContainer to)' instead.
	public int moveBetweenRooms(DbContainer from, DbContainer to)
	{
		if (from!=getContainingObj())
		{
			error("can we move from a room we are not in?");
		}
		else
		{
			moveBetweenRooms(to);
		}
		return 0;
	}
	
	
	// Same as moveBetweenRooms but to be used if the object need to be placed at a specified slot in the destination container. Is this used?
	/*
	public int moveBetweenRooms(DbContainer from, DbContainer to, final int newIndex)
	{
		debugWriteLock();

		from.removeObject(this);
		this.setIndex(newIndex);
		to.addDbStorableFull(this);
		return 0;
	}
	*/
	
	// Returns true if this object is in the list of recursive parents of object o
	boolean isRecursiveParentOf(DbContainer o)
	{
		DbContainer dc=o.getParent();
		if (dc==null)
		{
			return false;
		}				
		if (dc==this)
		{
			return true;
		}
		return isRecursiveParentOf(dc);
	}
	
	// Move this object from its current container to another container.
	// Returns zero if OK
	public int moveBetweenRooms(DbContainer to)
	{
		debugWriteLock();

		if (to == null)
		{
			debug("Can not move into nothing.");
			return -1;
		}

		
		if (to == this)
		{
			debug("Can not move into itself.");
			return -1;
		}
		
		// Make sure an object is not put into something inside itself.
		if (this.isRecursiveParentOf(to))
		{
			debug("Can not move into a child (or child of a child).");		
			return -1;
		}
		
		DbBase from=getContainingObj();
		from.removeObject(this);
		to.addDbStorable(this);
		return 0;
	}
	
	
	
	// This method can be used to iterate the sub objects
	// Give parameter d as null to get the first object.
	// Deprecated.
	/*
	@Override
	public DbBase iterateStoredObjects(DbBase d)
	{
		debugReadLock();

		if (listOfStoredObjects==null) return null;
		
		int i=getIndexOfObj(d);
		while(++i<listOfStoredObjects.getCapacity())
		{
			if (listOfStoredObjects.get(i)!=null)
			{
				return listOfStoredObjects.get(i);
			}
		}
		return null;
	}
	*/
	
	// This method can be used to iterate the sub objects
	// Give parameter d as null to get the first object.
	// This will give only objects of a certain type. It will not give objects that extend cls.
	// Deprecated.
	/*
	public DbBase iterateStoredObjects(DbBase d, Class<?> cls)
	{
		debugReadLock();

		if (listOfStoredObjects==null) return null;
		int i=getIndexOfObj(d);
		
		while(++i<listOfStoredObjects.getCapacity())
		{
			if ((listOfStoredObjects.get(i)!=null) && (listOfStoredObjects.get(i).getClass() == cls))
			{
				return listOfStoredObjects.get(i);
			}
		}
		return null;
	}
	*/
	
	
	// There might be a problem here if objects move from one room to another during a tick, then some may get tick twice and some not at all.
	// One solution is to override this method in DbIdList that will do tick on all sub objects by id instead.
	// Another is to not do the move during tick but to ask for a callback after tick.
	// Simply, it is up to application to solve that possible issue if it even is a problem.
	/*
	@Override
	public void tickRecursiveMs(int deltaMs)
	{		
		debugWriteLock();
		tickSelfMs(deltaMs);
		if (listOfStoredObjects != null)
		{
			for (DbStorable d : listOfStoredObjects)
			{
				d.tickRecursiveMs(deltaMs);
			}
		}
	}
	*/

	// This is probably just a duplicate of getObjFromIndexPath
	// This is the reverse of getIndexPathWithinDbRoot(" ")
	// Finds an object from its path within a data base
	// The path shall be a series of indexes separated with space, looking like this: "1 3 2 4"
	// Or separated by '.' like this: "1.2.3.4"
	// Todo: Instead of period '.' we should have allowed use of slash '/' here as in "1/2/3/4". 	
	public DbBase getObjFromIndexPathWithinDbRoot(String path)
	{
		WordReader wr = new WordReader(path);
		return getObjFromIndexWithinDbRootWr(wr);
	}

	// This is the reverse of getIndexPath(".")
	// Finds an object from its path within a data base
	// The path shall be a series of indexes separated with space, looking like this: "1 3 2 4"
	// Or separated by '.' like this: "1.2.3.4"
	// TODO: Instead of period '.' we should have allowed use of slash '/' here as in "1/2/3/4". 
	public DbBase getObjFromIndexPath(String path)
	{
		WordReader wr = new WordReader(path);
		return getObjFromIndexPathWr(wr);
	}
	
	
	// help function for getObjFromIndexWithinDbRoot, TODO: this is perhaps identical to getObjFromIndexPathWr so one of those can be removed
	// Path must be a space separated list of indexes
	private DbBase getObjFromIndexWithinDbRootWr(WordReader wr)
	{		
		int index=wr.readInt();
		DbBase bo = getObjFromIndex(index);

		if (!wr.isOpenAndNotEnd())
		{
			return bo;
		}
		else
		{
			if (bo instanceof DbContainer)
			{
				DbContainer c=(DbContainer)bo;
				return c.getObjFromIndexWithinDbRootWr(wr);
			}
		}
		
		error("object not found at path");
		return null;
	}	

	
	// help function for getObjFromIndexWithinDbRoot
	// Path must be a space or '/' separated list of indexes
	// TODO: Would like to allow # for ID, similar to findObjectByNameIndexOrId.
	// This differs from findObjectByNameIndexOrId in that names can not be used and it can not go upwards in hierarchy (does not understand '..').
	public DbBase getObjFromIndexPathWr(WordReader wr)
	{		
		int index=wr.readInt();
		DbBase bo = getObjFromIndex(index);

		if (!wr.isOpenAndNotEnd())
		{
			return bo;
		}
		else
		{
			if (bo instanceof DbContainer)
			{
				DbContainer c=(DbContainer)bo;
				return c.getObjFromIndexWithinDbRootWr(wr);
			}
		}
		
		error("object not found at path");
		return null;
	}	
	
	// Get a sub object for a given index.
	public DbBase getObjFromIndex(int index)
	{
		
		// This check should not be needed, remove later when things work more smoothly
		if (listOfStoredObjects==null)
		{
			debug("getObjFromIndex: listOfStoredObjects==null "+index);
			return null;
		}
		else if (index>=listOfStoredObjects.getCapacity())
		{
			//debug("getObjFromIndex: index>=listOfStoredObjects.length "+index+ " "+listOfStoredObjects.length);
			return null;			
		}
		else if (index < 0)
		{
			debug("getObjFromIndex: index<0 "+index);
			return null;						
		}
		
		
		return listOfStoredObjects.get(index);
	}
	
	// deprecated, this is inefficient
	public static DbBase findRelativeFromIndex(String nameOrIndex, DbBase sid[])
	{
		int index=Integer.parseInt(nameOrIndex);
		
		for(int i=0;i<sid.length; i++)
		{
			if (sid[i].getIndex()==index)
			{
				return sid[i];
			}
		}
		return null;
	}

	public static DbBase findRelativeFromName(String nameOrIndex, DbBase sid[])
	{
		for(int i=0;i<sid.length; i++)
		{
			if (sid[i].getName().equals(nameOrIndex))
			{
				return sid[i];
			}
		}
		return null;
	}

	
	// deprecated, this is inefficient, use getChildFromIndexOrName instead.
	public static DbBase findRelativeFromNameOrIndex(String nameOrIndex, DbBase sid[])
	{		
		if (WordReader.isInt(nameOrIndex))
		{
			return findRelativeFromIndex(nameOrIndex, sid);
		}
		else
		{
			return findRelativeFromName(nameOrIndex, sid);		
		}
		
	}

	// Similar to getObjFromIndexPathWr but it allows .. for parent object and '#' or '~' for ID and it allows names or index to be used.
	// Most important difference is that this stays inside a game data base (not program wide root). 
	// If path begins with '/' it starts from the game root not program wide root (or at least that was intended but does it work?).
	public DbBase findObjectByNameIndexOrId(WordReader wr)
	{				
		if (!wr.isOpenAndNotEnd())
		{
			return this;		
		}
		
		String nameOrIndex = wr.readWord("/");
		
		if (nameOrIndex == null)
		{
			return this;			
		}
		
		if (nameOrIndex.equals(""))
		{
			return this;
		}
		else if ((nameOrIndex.charAt(0)=='#') || (nameOrIndex.charAt(0)=='~'))
		{
			final String idStr=nameOrIndex.substring(1);
			final int id=Integer.parseInt(idStr);
			DbRoot dr=this.getDbRoot();
			DbBase defaultObj=dr.getDbIdObj(id);
			if (defaultObj==null) return null;
			return defaultObj.findObjectByNameIndexOrId(wr);			
		}
		else if (nameOrIndex.equals(".."))
		{
			DbBase defaultObj = this.getContainingObj(); 
			if (defaultObj==null) return null;
			return defaultObj.findObjectByNameIndexOrId(wr);
		}
		else if (nameOrIndex.equals("."))
		{
			return this.findObjectByNameIndexOrId(wr);
		}
		else
		{		
			return this.getChildFromIndexOrName(nameOrIndex);
		}
	
	}
	
	// TODO: Rename this to findObjectByNamePathIndexPathOrId
	public DbBase findObjectByNameIndexOrId(String path)
	{
		WordReader wr = new WordReader(path);
		return findObjectByNameIndexOrId(wr);
	}

	
	public DbBase getChildFromIndexOrName(String nameOrIndex)
	{
		if (WordReader.isInt(nameOrIndex))
		{
			int index=Integer.parseInt(nameOrIndex);
			DbBase db=this.getObjFromIndex(index);
			return db;
		}
		else
		{
			if (this.listOfStoredObjects!=null)
			{
				for (DbBase db : this.listOfStoredObjects)
				{
					if (db.getName().equals(nameOrIndex))
					{
						return db;
					}
				}
			}
			
		}
		return null;
	}
	
	
	// Searches recursively for an object with the given type
	// First found is returned even if there are more than one.
	// Databases should be read lock before calling this.
	// http://stackoverflow.com/questions/3397160/how-can-i-pass-a-class-as-parameter-and-return-a-generic-collection-in-java

	public DbBase findRecursive(int recursionDepth, Class<?> cls)
	{
		debugReadLock();
		
		if (listOfStoredObjects!=null)
		{
			for (DbStorable g : listOfStoredObjects)
			{
				if (g.getClass() == cls)
				{
					return g;
				}
	
				// Perhaps recursively in a sub object?
				// if ((recursionDepth>0) && (g instanceof DbContainer))
				if (recursionDepth>0)
				{
					DbContainer c=(DbContainer)g;
					DbBase s = c.findRecursive(recursionDepth-1, cls);
					if (s!=null)
					{
						return s;
					}
				}
			}		
		}
		return null;
	}
	
	// classes that have their own update counters shall implement this method (that method shall call super as usual)
	// previousUpdateCounter shall be the value root counter had when object update was sent previous time	
	// If a class adds its own listChangedObjects then a parse for the class name needs to be added in ClientThread where it interprets the "updatePartly" message.
	@Override
	public void listChangedObjects(int previousUpdateCounter, WordWriter ww)
	{
		debugReadLock();

		//super.listChangedObjects(previousUpdateCounter, ww);

		// Check sub objects
		if (listOfStoredObjects!=null)
		{
			for (DbStorable d : listOfStoredObjects)
			{
				d.listChangedObjects(previousUpdateCounter, ww);
			}
		}
	}
	

	// getSubObj
	// gives a list of all sub objects, alias child objects
	// If there are much to be done with the sub objects (such as sending data to a client) 
	// this can be better than iterating since this read lock only while making the list.	
	// Returns an array with references (not copies) to all sub objects.
	// Not thread safe, if thread safe is needed use getListOfSubObjectsThreadSafe.
	public DbBase[] getListOfSubObjects()
	{	
		debugReadLock();
		
		final int n=this.getNSubObjects();
		DbBase[] bol=new DbBase[n];

		if (n>0)
		{
			int i=0;
			for (DbStorable d : listOfStoredObjects)
			{
				bol[i]=d;
				i++;
			}
		}
		return bol;		
	}
	
	// Gives the current capacity of the listOfStoredObjects (capacity can increase)
	public int getListCapacity()
	{
		if (listOfStoredObjects!=null)
		{
			return listOfStoredObjects.getCapacity();
		}
		return 0;
	}

	
	// Deprecated, Not a good name on this method since it gives capacity, max index would actually be cpacity-1 
	public int getMaxIndex()
	{
		return getListCapacity();
	}
	
	/*
	@Override
	public void registerSelfAndSubObjectsInDbIdList(DbIdList idList)
	{	
		if (listOfStoredObjects!=null)
		{
			for (DbStorable bo : listOfStoredObjects)
			{
				bo.registerSelfAndSubObjectsInDbIdList(idList);
			}
		}
	}
	*/

}
