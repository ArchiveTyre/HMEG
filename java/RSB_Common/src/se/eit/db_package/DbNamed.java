/*
DbNamed.java

This is an object that can reside in the database, it can contain other objects in its turn and
it has a name.

Perhaps DbNamed would have been a better name for this class?


Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

*/


package se.eit.db_package;

import java.io.PrintWriter;
import se.eit.web_package.*;


public class DbNamed extends DbContainer {
	
	public static final int TICK_PHASE_LAST=0;
	
	
	private String name;


	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return DbNamed.class.getSimpleName();	
	}
	
	public DbNamed()
	{	
		super();
	}
	
	
	public DbNamed(String name)
	{
		super();
		
		this.name=name;
	}

	public DbNamed(final DbNamed namedObj)
	{
		super(namedObj);
		
		this.name=namedObj.name;
	}

	// don't think this is used.
	/*
	public DbNamed deepCopy() {
		return new DbNamed(this);
	}
	*/
	
	
	@Override
	public void readSelf(WordReader wr)
	{
		super.readSelf(wr);
		
		name=wr.readWord();
		
		if (name.equals("null"))
		{
			name=null;
		}
	}

	
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);

		ww.writeWord(name);		
	}
	

	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("name"))
		{
			name=wr.readString();
			return 1;
		}
		return super.setInfo(wr, infoName);
	}

	// Get the name of an object, or its index if it does not have a name.
	@Override
	public String getNameOrIndex()
	{
		if (name==null)
		{
			return ""+getIndex();
		}
		else if (name.equals("null"))
		{
			debug("name null should not be used");
			return ""+getIndex();
		}			
		return name;
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name=name;
		setUpdateCounter();
	}
	

	@Override
	public String getNameAndPath(String separator)
	{
		// Is there an object above this one.
		if (containingObj!=null)
		{
			final String p=getContainingObj().getNameAndPath(separator);
			final String n=getNameOrIndex();
			return p + separator +n;				
		}
		return /*separator+ separator+*/ getName();
	}


	@Override
	public String getNameAndPathInternal(String separator)
	{
		// Is there an object above this one.
		if ((containingObj!=null) && (!(containingObj instanceof DbRoot)))
		{
			final String p=containingObj.getNameAndPathInternal(separator);
			final String n=getNameOrIndex();
			return p + separator +n;				
		}
		return separator + getName();
	}

	// Searches all sub objects for a game object with the given name.
	// First found is returned even if there are more than one.
	// Databases should be read lock before calling this.
	@Override
	public DbBase findDbNamedRecursive(String name, int recursionDepth)
	{
		//debugReadLock();
		final DbRoot r=getDbRoot();
		r.lockRead(); // TODO: read locking in a recursive call is very inefficient. Should do the read lock in calling method instead.
		try
		{	
			if (listOfStoredObjects!=null)
			{			
				for (DbStorable g : listOfStoredObjects)
				{
					if (name.equals(g.getName()))
					{
						return g;
					}
		
					// Perhaps recursively in a sub object?
					if (recursionDepth>0)
					{
						DbBase s = g.findDbNamedRecursive(name, recursionDepth-1);
						if (s!=null)
						{
							return s;
						}
					}				
				}
			}
		}
		finally
		{
			r.unlockRead();
		}

		return null;
	}


	// Searches all sub objects for a game object with the given name and type
	// First found is returned even if there are more than one.
	// Databases should be read lock before calling this.
	// http://stackoverflow.com/questions/3397160/how-can-i-pass-a-class-as-parameter-and-return-a-generic-collection-in-java
    // TODO: Can we deprecate this?
	@Override
	public DbBase findDbNamedRecursive(String name, int recursionDepth, Class<?> cls)
	{
		//debugReadLock();
		final DbRoot r=getDbRoot();
		r.lockRead(); // TODO: read locking in a recursive call is very inefficient. Should do the read lock in calling method instead.
		try
		{		
			if (listOfStoredObjects!=null)
			{			
				for (DbStorable g : listOfStoredObjects)
				{
					if ((g.getClass() == cls) && (name.equals(g.getName())))
					{
						return g;
					}
		
					// Perhaps recursively in a sub object?
					if (recursionDepth>0)
					{
						DbBase s = g.findDbNamedRecursive(name, recursionDepth-1,cls);
						if (s!=null)
						{
							return s;
						}
					}
				}		
			}
		}
		finally
		{
			r.unlockRead();
		}
		return null;
	}

	
	@Override
	public DbBase findDbNamedRecursive(String name)
	{
		return findDbNamedRecursive(name, Integer.MAX_VALUE);
	}


	@Override
	public DbBase findGameObjNotRecursive(String name)
	{
		return findDbNamedRecursive(name, 0);
	}
	
	// Search from the top
	/*
	public DbNamed findGameObjRecursiveFromTop(String name)
	{
		DbNamed go = findDbNamedRecursive(name, 0);
		if (go!=null) 
		{
			return go;
		}
		go = findDbNamedRecursive(name, 1);
		if (go!=null) 
		{
			return go;
		}
		go = findDbNamedRecursive(name, 2);
		if (go!=null) 
		{
			return go;
		}		
		return findDbNamedRecursive(name, Integer.MAX_VALUE);
	}
	*/
	
	// deprecated, use 	addDbObj instead
	public int addGameObj(DbBase gameObjToAdd)
	{
		return addObject(gameObjToAdd);
	}
	
	
	// not thread safe, non recursive count of sub objects
	public final int countSubGamObjNonRecursive()
	{
		debugReadLock();
		return this.getNSubObjects();
	}
	
	// get a non recursive list of names of all sub objects
	public String[] getAllNamesNonRecursive()
	{
		//final DbRoot r=getDbRoot();
		//r.lockRead();
		//try
		{		
			debugReadLock();
			
			int c=0;
			int n=countSubGamObjNonRecursive();
			String[] sa = new String[n];
	
			if (listOfStoredObjects!=null)
			{
				for (DbStorable d : listOfStoredObjects)
				{
					sa[c++]=d.getName();
				}
			}			
			return sa;
		}
		//finally
		//{
		//	r.unlockRead();
		//}
	}
	

	public String getObjInfoPathNameEtc()
	{
		String info="";
		
		int id=getId();
		if (id >= 0)
		{			
			info+="id=~"+id;		
		}

		
		info+=", name="+getNameOrIndex();

		info+=", index="+this.getIndex();

		/*if (name!=null)
		{
			info+=" name="+name;
		}
		else
		{
			info+=" "+getIndex();
		}*/
		
		info+=", type="+getType();
		
		final String nap=getNameAndPath("/");			
		info+=", path=/"+nap;
		
		final DbRoot ro=getDbRoot();
		if (ro!=null)
		{
			final String db=ro.getNameAndPath("/");
			final String idx=getIndexPathWithinDbRoot("/");
			info+=", db=/"+db;
			info+=", indexPath=/"+idx;
		}
				
		return info;
	}

	
	@Override
	public void listNameAndPath(PrintWriter pw, int recursionDepth, String prefix)
	{
		if (listOfStoredObjects!=null)
		{
			for (DbStorable bo : listOfStoredObjects)
			{
				String npe=bo.getNameAndPath("/");
				pw.println(prefix+npe);
				pw.flush();
				if (recursionDepth>0)
				{
					bo.listNameAndPath(pw, recursionDepth-1, prefix+"  ");
				}
			}
		}
	}


	// List available variables within this object.
	// This method needs to be implemented by sub classes if they contain variables that shall be listed in the server console by "list" command. Those overriding methods shall also call "super.listInfo".
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);
		
		pw.println(prefix+"type "+getType());
		pw.println(prefix+"name "+getName());
		
	}
	
	// Returns true if tag was found and changed.
	// This method needs to be implemented by sub classes if they contain variables that shall be possible to change in the server console by "set" command. Those overriding methods shall also call "super.changeInfo".
	@Override
	public boolean changeInfo(String tag, String value)
	{	
		this.debugWriteLock();
		
		if (tag.equals("name"))
		{
			setName(value);
			return true;
		}
		
		// There is no listInfo in the super class of DbNamed but all classes that override this method shall do the call in the line below.
		// return super.changeInfo(pw, prefix);
		
		return false;
	}
	
	
	public String toShortFormatedString()
	{
		return String.format("  %-8s ~%-10s %-20s %-32s", getIndex(), getId(), getType(), getNameOrIndex()); 		
	}
	
	
	@Override
	public void listSubObjects(PrintWriter pw, String prefix)
	{
		final DbRoot db=this.getDbRoot();
		db.lockRead();
		try
		{
			if (listOfStoredObjects!=null)
			{
				for (DbStorable no : listOfStoredObjects)
				{
					System.out.println(no.toShortFormatedString());
				}
			}
		}
		finally
		{
			db.unlockRead();
		}
	}
	
	
	// To find an object relative to this object using a name or index.	
	@Override
	public DbBase findRelativeFromNameOrIndex(String nameOrIndex)
	{
		if (nameOrIndex.equals(".."))
		{
			return (DbNamed)getContainingObj();				
		}
		else if (nameOrIndex.equals("."))
		{
			// Do nothing
			return this;
		}
		else if (WordReader.isInt(nameOrIndex))
		{
			int i=Integer.parseInt(nameOrIndex);
			return (DbNamed)getObjFromIndex(i);
		}
		else
		{
			return findGameObjNotRecursive(nameOrIndex);
		}			
	}
	
	/*
	@Override
	public void tickSelfMs(final int deltaMs)
	{
	
	}
	*/
}
