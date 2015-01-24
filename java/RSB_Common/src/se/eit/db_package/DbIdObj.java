package se.eit.db_package;

import se.eit.web_package.*;


public class DbIdObj  extends DbNamed 
{
    public static final boolean useId=true;
	
	private int id=-1;
	protected DbIdList idList=null;
	
	
	/*public void debug(String str)
	{
		super.debug(DbIdObj.class.getSimpleName() +": "+str);
	}*/
	
	public DbIdObj()
	{
		super();
	}
	
	
	@Override
	public int getId()
	{
		return id;
	}
	
	
	// to be called from DbIdList only.
	public void setId(int id, DbIdList idList)
	{
		// Just for debugging, this if can be removed later
		if ((this.id!=-1) && (this.id!=id) && (id!=-1))
		{
			error("already have id "+this.id+" "+id);
		}
		
		this.id=id;
		this.idList=idList;
	}
	

	@Override
	public void readSelf(WordReader wr)
	{
		super.readSelf(wr);
		
		id=wr.readInt();
		

		// If it has an ID we can try to register it. Perhaps this code is not needed. This will try to register also objects already registered which use CPU time. Registration for new objects can be done using registerSelfInDbIdListAndAdd.
		// Perhaps this code can be removed? Yes it shall be...
		/*
		if (id>=0)
		{
			DbRoot r=this.getDbRoot();
			// 
			if (r instanceof DbIdList)
			{
				this.idList=((DbIdList)r);
				this.idList.addIdObj(this);
			}
			else
			{
				// The object is not in the database so we can't register it  since we don't know which database at this point. A call to registerSelfInDbIdListAndAdd will be needed later.
				debug("readSelf: no known database to register in for id "+id);
			}
		}
		*/

		
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(id);	
	}
	
	public static DbIdList getDbIdList(DbBase parentObj)
	{	
		if (parentObj instanceof DbIdList)
		{
			return (DbIdList)parentObj;
		}
		
		if (parentObj instanceof DbIdObj)
		{
			final DbIdObj dio = (DbIdObj)parentObj;
			if (dio.idList!=null)
			{
				return dio.idList;
			}
		}
		
		return parentObj.getDbIdList();
	}
	
	// The object is linked into a hierarchy of objects. It is also assigned an ID.
	// Deprecated, use parent.addObject instead.
	@Override
	public void linkSelf(DbBase parentObj)
	{		
		// A problem here, base class DbChangedCounter needs this.idList (it uses getDbRoot) but is called (in super.linkSelf) before it is set here. One solution is to change the order of base classes so that this class i base for DbChangedCounter instead. Or we could move the line "this.idList=getDbIdList(parentObj);" to before "super.linkSelf(parentObj);"
		
		super.linkSelf(parentObj);
		// Find our DbIdList and register our selves in it.
		this.idList=getDbIdList(parentObj);
		if (this.idList != null)
		{
			id=this.idList.addIdObj(this);
		}
		else
		{
			debug("linkSelf no idList");
		}
	}

	/*
	@Override
	public int addObject(DbBase objToAdd)
	{
		final DbIdList dil=this.getDbIdList();
		if ((objToAdd instanceof DbIdObj) && (dil!=null))
		{
			DbIdObj dio = (DbIdObj)objToAdd;
			if (dio.idList == null)
			{
				dio.idList=dil;
				dio.id=dil.addIdObj(dio);
			}
		}
		return super.addObject(objToAdd);
	}
	*/
	
	// The object is linked into a hierarchy of objects. It is also assigned an ID.
	/*public void linkSelf(DbBase parentObj, int index)
	{		
		// A problem here, base class DbChangedCounter needs this.idList (it uses getDbRoot) but is called (in super.linkSelf) before it is set here. One solution is to change the order of base classes so that this class i base for DbChangedCounter instead. Or we could move the line "this.idList=getDbIdList(parentObj);" to before "super.linkSelf(parentObj);"
		
		super.linkSelf(parentObj, index);
		// Find our DbIdList and register our selves in it.
		this.idList=getDbIdList(parentObj);
		if (this.idList != null)
		{
			id=this.idList.addIdObj(this);
		}
	}*/

	
	// Similar to link self but object and parameter is swapped, 
	// The object bo is added under this object.
	/*
	public int addAndRegObjAtIndex(DbStorable bo, final int index)
	{
		int r = super.addAndRegObjAtIndex(bo, index);
		
        // If it is an IdObj it shall be registered in the IdList.
		if (bo instanceof DbIdObj)
		{
			DbIdObj io=(DbIdObj)bo;
			if (io.idList==null)
			{
				io.idList=this.getDbIdList();
				if (io.idList != null)
				{
					io.id=io.idList.addIdObj(io);
				}
				else
				{
					error("did not find idList");
				}
			}
			else
			{
				error("object was already registered in IdList");
			}
		}
		
		return r;
	}
	*/
	
	// Add this object to the database and assign an ID
	// Any sub objects are also assigned IDs
	
	/*
	@Override
	public void registerSelfInDbIdListAndAdd(DbBase container)
	{
		// Add this object in it super room (alias parent alias containing object)
		container.addObject(this);
		
		// assign a unique ID for this object (and its sub objects)
		registerInDbIdList();	
	}
	*/

	// Add this object to the database and assign an ID
	// Any sub objects are also assigned IDs
	//@Override
	/*
	public void registerSelfInDbIdListAndAddUsingLockWrite(DbBase container)
	{
		DbRoot ro=container.getDbRoot();
		container.addObject(this);
		ro.lockWrite();
		try
		{
			// Add this object in it super room (alias parent alias containing object)
			container.addObject(this);
			
			// assign a unique ID for this object (and its sub objects)
			ro.addIdObj(this);			
		}
		finally
		{
			ro.unlockWrite();
		}	
	}
	*/
	
	/*
	// This will register this object and all sub objects in the id list.
	@Override
	public void registerSelfAndSubObjectsInDbIdList(DbIdList idList)
	{	
		if (this.idList!=null)
		{
			if (this.idList!=idList)
			{
				error("already registered");
			}
			else
			{
				debug("already registered");
			}
		}
		
		this.idList=idList;
		id=idList.addIdObj(this);

		super.registerSelfAndSubObjectsInDbIdList(idList);
	}
*/
	
	/*
	public void fixId()
	{
		DbRoot ro=super.getDbRoot();
		if (ro instanceof DbIdList)
		{
			this.idList=(DbIdList)ro;
			id=this.idList.addIdObj(this);			
		}
		
	}
    */	
	
	@Override
	public void writeIdOrPath(WordWriter ww)
	{
		if (useId)
		{
			ww.writeInt(getId());
		}
		else
		{
			ww.writeString(getIndexPathWithinDbRoot(" "));
		}
	}
	
	public String getIdAndName()
	{
		final String name=getName();
		if (name!=null)
		{
			return getId()+":"+getName();
		}
		return ""+getId();
	}
	
	
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"id "+id);		
	}
	
	public DbIdList getDbIdList()
	{
		return idList;		
	}
	
	// unlink alias delete or unregister
	// probably we should also do the same with all objects stored in this object.
	// and this method and clear should be the same.
	@Override
	public void unlinkSelf()
	{
		// Small problem here, if we do idList.unregDbIdObj before super.unlinkSelf then notify changes will not work since that is done in unlinkSelf but needs to know which ID list it is in. Will try doing unlinkSelf before unregDbIdObj...  
		super.unlinkSelf();
		if (idList==null)
		{
			// This happens, do not know why yet. But it happens when gameTickCleanup deletes a NothingType. 
			debug("idList was null, "+this.toString());
			//throw(new NullPointerException("idList was null, "+this.toString()));
		}
		else
		{
			idList.unregDbIdObj(this);
			idList=null;
		}
	}

	public DbRoot getDbRoot()
	{
		return (DbRoot)idList;
	}
	
}

