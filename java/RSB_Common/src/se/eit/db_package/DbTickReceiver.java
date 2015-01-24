package se.eit.db_package;


import se.eit.db_package.DbBase;
import se.eit.db_package.DbSuperRoot;
import se.eit.db_package.NotificationSender;

// This class works in pair with DbSuperRoot

public abstract class DbTickReceiver extends NotificationSender {
	int tickRef=-1;

	
	@Override
	public void linkSelf(DbBase parentObj)
	{
		super.linkSelf(parentObj);
		requestTick();
	}

	@Override
	public void unlinkSelf()
	{
		cancelTick();
		super.unlinkSelf();
	}

	public abstract void tickMsCallback(long tickTimeMs);
	{
		// Extending classes shall override this method.
	}

	public void requestTick()
	{
		if (tickRef==-1)
		{
			DbSuperRoot dsr=this.getDbSuperRoot();
			if (dsr!=null)
			{
				tickRef = dsr.requestPeriodicTick(this);
			}
		}		
	}
	
	public void cancelTick()
	{
		if (tickRef!=-1)
		{
			DbSuperRoot dsr=this.getDbSuperRoot();
			dsr.removePeriodicTick(tickRef);
			tickRef=-1;
		}		
	}
	
}
