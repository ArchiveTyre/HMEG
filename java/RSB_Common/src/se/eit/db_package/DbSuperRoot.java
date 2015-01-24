package se.eit.db_package;



//This class works in pair with DbTickReceiver

public class DbSuperRoot extends DbRoot {

	public DbList<DbTickReceiver> tickList=new DbList<DbTickReceiver>();;
	
	
	public DbSuperRoot()
	{	
		super();
	}

	
	public DbSuperRoot(String name)
	{
		super();
		this.setName(name);
	}
	
	// Returns a reference to be used to cancel periodic tick
	public int requestPeriodicTick(DbTickReceiver tickReceiver)
	{
		return tickList.add(tickReceiver);
	}
	
	public void removePeriodicTick(int i)
	{
		if (i>=0)
		{
			tickList.remove(i);
		}
	}
	
	
	public void tickMsSuper(long tickTimeMs)
	{
		for (DbTickReceiver s : tickList)
		{
			s.tickMsCallback(tickTimeMs);
		}		
	}
	
	/*
	public void tickMsSuper(long tickTimeMs)
	{
		Iterator<DbTickReceiver> i=tickList.iterator();
		
		while (i.hasNext())
		{
			DbTickReceiver dtr = i.next();
		
			dtr.tickMsCallback(tickTimeMs);
		}
	}
	*/
}
