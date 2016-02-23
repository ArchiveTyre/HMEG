/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.rsb_package;

public interface ActiveObjectInterface {

	// TODO No need to send gameTime as parameter, the value is available by calling getGameTime().
	public void activeObjectTick(long gameTime);
	public void activeObjectSlowTick(long gameTime);
	public void maintenanceObjectTick(long gameTime);
	public void luaCallSlowTick(long gameTime);
	public void luaCallMaintenanceTick(long gameTime);

	
}
