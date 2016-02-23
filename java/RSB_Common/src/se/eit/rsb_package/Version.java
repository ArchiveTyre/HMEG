/* 
Version.java


Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license


Before doing changes
	cd ~/eclipse/RoboticsSandBox/workspace; git pull ~/Desktop/twotb/git/RoboticsSandBox 
	
After doing changes. 
* Check in to local repository.
* backup by doing pull from server repository.
	cd ~/Desktop/twotb/git/RoboticsSandBox; git pull ~/eclipse/RoboticsSandBox/workspace

History
Created feb 2013 by Henrik Bjorkman
based on First Person Java Proto Game 0.1.12


*/

package se.eit.rsb_package;

public class Version {
	
	// If version is changed here consider also web/js/main.js and cpp/src/version.h
	
	// ServerVersion and ClientVersion
	
	public static String serverVersion()
	{
		return "www.eit.se/rsb/0.9/server";
	}
	
	public static String clientVersion()
	{
		return "www.eit.se/rsb/0.9/client";
	}
	
	// This is used when program resent itself on console
	/*public static String getProgramNameAndVersion()
	{
		return "www.eit.se/rsb/0.9";
	}*/

	// This is used when reading and saving files
	public static String fileFormatVersion()
	{
		return "www.eit.se/rsb/0.9";
	}
}

