package se.eit.citypvp_package;

import se.eit.db_package.DbContainer;

public class HmegWorld extends CityPvpWorld {

	public HmegWorld(DbContainer parent, String name, String createdBy) 
	{
		super(parent,name,createdBy);
	}
	
	public HmegWorld()
	{
		super();
	}

	@Override
	public void generateWorld()
	{
		generateSelf();
	}

	// This returns the name of the server object that clients shall use to play this world.
	@Override
	public String serverForThisWorld()
	{
		return "HmegServer";
	}

	@Override
	public void linkSelf(DbContainer parentObj)
	{
		super.linkSelf(parentObj);
		requestTick(100);
	}
	
}
