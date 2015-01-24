/* 
Version.java

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
	static final String n="www.eit.se/rsb"; 
	static final String v="0.9";

	/*
	public static String getName()
	{
		return n;
	}
	public static String getVersion()
	{
		return v;
	}
	*/
	
	public static String getNameAndVersion()
	{
		return n+"/"+v;
	}
}

