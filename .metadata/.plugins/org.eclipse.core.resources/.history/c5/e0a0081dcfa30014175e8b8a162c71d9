package se.eit.empire_package;

import se.eit.db_package.DbBase;

public abstract class ActiveObject extends EmpireUnitOrSector {

	/*
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireBase.class.getSimpleName();	
	}
	*/

	public int activeIndex=-1;
	

	public ActiveObject() 
	{
		super();
	}

	@Override
	public void unlinkSelf()
	{
		final EmpireWorld ew=getEmpireWorld();
		if (ew!=null)
		{
			ew.removeActiveObject(this);
		}
		super.unlinkSelf();
	}


	
	@Override
	public void linkSelf(DbBase parentObj)
	{
		super.linkSelf(parentObj);

		if (this.getDbRoot()!=null)
		{
			final EmpireWorld ew=getEmpireWorld();
			ew.addActiveObject(this);
		}
	}

	// During game tick update only internal variables. Don't move, add or remove units.
	public abstract void gameTick(long gameTime);

	
}
