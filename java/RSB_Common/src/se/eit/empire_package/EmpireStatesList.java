//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;


//import java.util.Random;


//import se.eit.rsb_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;


public class EmpireStatesList extends EmpireBase {
	
    final int nNations=8;

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireStatesList.class.getSimpleName();	
	}

	public EmpireStatesList(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);

		generateList();
	}

	public EmpireStatesList()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
	}	
	
	public void generateList()
	{
	    for(int i=0;i<nNations;i++)
	    {
			new EmpireState(this, "State"+i);

	    	
	    	/*
	    	EmpireState en = new EmpireState(this, "State"+i);
	    	en.postMessage("wellcome to EIT Empire");
	    	
			final EmpireWorld ew=getEmpireWorld();
			if (ew.gameSpeed==0)
			{
				en.postMessage("Game is paused. (Game admin can use command: 'go' to start.)");
			}
	    	*/
	    }
	}
	
    // Post message to all states
	public void postMessage(String str)
	{
		debugWriteLock();

		DbBase sol[]=getListOfSubObjects();
		for(int i=0; i<sol.length; i++)
		{
			DbBase so = sol[i]; 
			if (so instanceof EmpireState)
			{
				EmpireState en = (EmpireState)so;
				en.postMessage(str);
			}
		}

		
	}
	
	public EmpireState getNationByOwner(String PlayerName)
	{
		final DbRoot db=getDbRoot();
		db.lockRead();
		try
		{
			if (listOfStoredObjects!=null)
			{
	
				for (DbStorable d : listOfStoredObjects)
				{
					if (d instanceof EmpireState)
					{
						EmpireState es=(EmpireState)d;
						
						if (es.headOfState.equals(PlayerName))
						{
							return es;
						}

						/*DbBase o = en.findGameObjNotRecursive(PlayerName);
						
						if (o != null)
						{
							return en;
						}*/
					}
				}
			}
		}
		finally
		{
			db.unlockRead();
		}
		return null;
	}
	

	public EmpireState takeNation(String PlayerName)
	{
		final DbRoot db=getDbRoot();
		db.lockWrite();
		try
		{
			if (listOfStoredObjects!=null)
			{
				for (DbStorable d : listOfStoredObjects)
				{
					if (d instanceof EmpireState)
					{
						EmpireState es=(EmpireState)d;
						if (es.headOfState.length()==0)
						{
							es.headOfState = PlayerName;
							/*EmpireOrder eo = new EmpireOrder();
							eo.setOrder("");
							eo.setName(PlayerName);
							eo.linkSelf(d);*/
							return es;
						}
					}
				}
			}	
		}
		finally
		{
			db.unlockWrite();
		}
		return null;
	}

	
	public EmpireState getEmpireNation(int n)
	{
		if (n<0)
		{
			return null;
		}
		
		DbBase db = getObjFromIndex(n);
		if (db instanceof EmpireState)
		{
			return (EmpireState)db;
		}
		else
		{
			error("getEmpireNation: internal error, wrong object type "+n);
			return null;
		}
	}
	
}