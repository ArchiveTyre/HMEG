/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.rsb_factory_pkg;

import se.eit.citypvp_package.*;
import se.eit.db_package.DbBase;
import se.eit.db_package.DbNoSaveRoot;
import se.eit.db_package.DbSubRoot;
import se.eit.rsb_package.*;

public class RsbFactory {
	
	public static void static_error(String str)
	{
		System.out.flush();
		System.err.println(className()+": static_error: "+str);
		Thread.dumpStack();
		System.exit(1);
	}
	
	public static void debug(String str)
	{
		System.out.println(className()+": static_error: "+str);
	}
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return RsbFactory.class.getSimpleName();	
	}
	
	
	
	// All database object classes that can be saved to disk or sent needs to be listed here. 
	// Thats all classes that inherit DbBase shall be listed here.
	public static DbBase createObj(String t) throws NumberFormatException
	{
		// Here all classes that do send something from their listChangedObjects methods needs to be handled.		
		final char ch=t.charAt(0);
		switch(ch)
		{
			case 'C':
			{
				if (t.equalsIgnoreCase("ChatRoomWorld"))
				{
					return new ChatRoomWorld();
				}
				else if (t.equalsIgnoreCase("CityPvpWorld"))
				{
					return new CityPvpWorld();
				}
				else if (t.equalsIgnoreCase("CityPvpRoom"))
				{
					return new CityPvpRoom();
				}
				else if (t.equalsIgnoreCase("CityPvpEntity"))
				{
					return new CityPvpEntity();
				}
				else if (t.equalsIgnoreCase("CityPvpAvatar"))
				{
					return new CityPvpAvatar();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			case 'H':
			{
				if (t.equalsIgnoreCase("HmegWorld"))
				{
					return new HmegWorld();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			case 'P':
			{
				if ((t.equalsIgnoreCase("Player")) || (t.equalsIgnoreCase("PlayerData")))
				{
					return new PlayerData();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			case 'R':
			{
				if (t.equalsIgnoreCase("RsbLong"))
				{
					return new RsbLong();
				}
				else if (t.equalsIgnoreCase("RsbBigBitMap"))
				{
					return new RsbBigBitMap();
				}
				else if (t.equalsIgnoreCase("RsbRoundBuffer"))
				{
					return new RsbRoundBuffer();
				}
				else if (t.equalsIgnoreCase("RsbString"))
				{
					return new RsbString();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			case 'A':
			{
				if (t.equalsIgnoreCase("ActivePlayerList"))
				{
					return new ActivePlayerList();
				}
				else if (t.equalsIgnoreCase("AvatarPlayerReferences"))
				{
					return new AvatarPlayerReferences();
				}
				break;				
			}
			case 'D':
			{
				if (t.equalsIgnoreCase("DbSubRoot"))
				{
					return new DbSubRoot();
				}
				else if (t.equalsIgnoreCase("DbNoSaveRoot"))
				{
					return new DbNoSaveRoot();
				}
				break;				
			}
			case '}':
			{
				if (t.equalsIgnoreCase("}"))
				{
					// This was an end marker, there are no more sub objects.
					return null;
				}
				break;				
			}
			default:
			{
				 // do nothing, error is reported further down.
			}
		}
		
		static_error("unknown object "+t);
		throw(new NumberFormatException("unknown object '"+t+"'"));			
	}

}
