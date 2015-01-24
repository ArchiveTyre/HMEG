/*
TmpObj.java

This is an object that can reside in our data structures but it should not be saved to disk

Perhaps this is not used any more?


Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

*/





package se.eit.rsb_package;
import se.eit.db_package.*;


public class TmpObj extends DbNamed {

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return TmpObj.class.getSimpleName();	
	}
	
	public TmpObj(String name)
	{
		super(name);
	}
	
	// not thread safe, non recursive count of sub objects
	public final int countSubDbObj()
	{
		debugReadLock();

		return listOfStoredObjects.size();
	}


	
}
