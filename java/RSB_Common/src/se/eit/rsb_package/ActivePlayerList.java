// ActivePlayerList.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_package;

import se.eit.db_package.DbStorable;
import se.eit.db_package.DbThreadSafe;
import se.eit.web_package.WordWriter;

public class ActivePlayerList extends DbThreadSafe {

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return ActivePlayerList.class.getSimpleName();	
	}
	

	public ActivePlayerList()
	{	
		super();
	}
	

	public int getNActivePlayers()
	{
		return listActivePlayers(null);
	}


	// Returns the number of active players
	public int listActivePlayers(WordWriter ww)
	{
		int n=0;
		if (listOfStoredObjects!=null)
		{
			for (DbStorable s : listOfStoredObjects)
			{
				AvatarPlayerReferences apr = (AvatarPlayerReferences)s;
				if (apr.isActive)
				{
					if (ww!=null)
					{
						ww.writeWord(s.getName());
					}
					n++;
				}
			}
		}
		return n;
	}

	// This is called to send an ascii message to this object.	
	@Override
	public void postMessageToThis(String msg)
	{
		// pass the message on to those in the room.
		if (this.listOfStoredObjects!=null)
		{
			for(DbStorable ds : this.listOfStoredObjects)
			{
				ds.postMessageToThis(msg);
			}
		}
	}
	
}
