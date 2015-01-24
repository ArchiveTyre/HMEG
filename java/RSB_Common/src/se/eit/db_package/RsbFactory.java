package se.eit.db_package;

import se.eit.TextAdventure.TextAdventureWorld;
import se.eit.citypvp_package.CityPvpAvatar;
import se.eit.citypvp_package.CityPvpEntity;
import se.eit.citypvp_package.CityPvpRoom;
import se.eit.citypvp_package.CityPvpWorld;
import se.eit.empire_package.EmpireStatesList;
import se.eit.empire_package.EmpireOrder;
import se.eit.empire_package.EmpireRoundBuffer;
import se.eit.empire_package.EmpireSector;
import se.eit.empire_package.EmpireState;
import se.eit.empire_package.EmpireTerrain;
import se.eit.empire_package.EmpireUnit;
import se.eit.empire_package.EmpireUnitType;
import se.eit.empire_package.EmpireUnitTypeList;
import se.eit.empire_package.EmpireWorld;
/*import se.eit.robogame_package.RoboGameAvatar;
import se.eit.robogame_package.RoboGameBlockRoom;
import se.eit.robogame_package.RoboGameMassObj;
import se.eit.robogame_package.RoboGameRoom;
import se.eit.robogame_package.RoboGameSphere;
import se.eit.robogame_package.RoboGameSphereWithMass;
import se.eit.robogame_package.RoboGameSubSphere;
import se.eit.robogame_package.RoboGameWorld;
import se.eit.robogame_package.RoboGameWorldRoot;*/
import se.eit.rsb_package.ChatRoomWorld;
import se.eit.rsb_package.Player;
import se.eit.rsb_package.WorldBase;

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
		DbBase bo = null;
		
		// Here all classes that do send something from their listChangedObjects methods needs to be handled.		
		final char ch=t.charAt(0);
		switch(ch)
		{
			case 'C':
			{
				if (t.equalsIgnoreCase("ChatRoomWorld"))
				{
					bo = new ChatRoomWorld();
				}
				else if (t.equalsIgnoreCase("CityPvpWorld"))
				{
					bo = new CityPvpWorld();
				}
				else if (t.equalsIgnoreCase("CityPvpRoom"))
				{
					bo = new CityPvpRoom();
				}
				else if (t.equalsIgnoreCase("CityPvpEntity"))
				{
					bo = new CityPvpEntity();
				}
				else if (t.equalsIgnoreCase("CityPvpAvatar"))
				{
					bo = new CityPvpAvatar();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			case 'E':
			{				
				if (t.equalsIgnoreCase("EmpireWorld"))
				{
					bo = new EmpireWorld();
				}
				else if (t.equalsIgnoreCase("EmpireTerrain"))
				{
					bo = new EmpireTerrain();
				}			
				else if ((t.equalsIgnoreCase("EmpireNation")) || (t.equalsIgnoreCase("EmpireState")))
				{
					bo = new EmpireState();
				}
				else if ((t.equalsIgnoreCase("EmpireNationsList")) || (t.equalsIgnoreCase("EmpireStatesList")))
				{
					bo = new EmpireStatesList();
				}
				else if (t.equalsIgnoreCase("EmpireUnit"))
				{
					bo = new EmpireUnit();
				}
				else if (t.equalsIgnoreCase("EmpireSector"))
				{
					bo = new EmpireSector();
				}
				else if (t.equalsIgnoreCase("EmpireOrder"))
				{
					bo = new EmpireOrder();
				}
				else if (t.equalsIgnoreCase("EmpireUnitTypeList"))
				{
					bo = new EmpireUnitTypeList();
				}
				else if (t.equalsIgnoreCase("EmpireUnitType"))
				{
					bo = new EmpireUnitType();
				}
				else if (t.equalsIgnoreCase("EmpireRoundBuffer"))
				{
					bo = new EmpireRoundBuffer();
				}				
				else
				{
					static_error("unknown object '"+t+"'");
				}
				break;	
			}
			case 'P':
			{
				if (t.equalsIgnoreCase("Player"))
				{
					bo = new Player();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			/*case 'R':
			{
				if (t.equalsIgnoreCase("RoboGameRoom"))
				{
					bo = new RoboGameRoom();
				}
				else if (t.equalsIgnoreCase("RoboGameBlockRoom"))
				{
					bo = new RoboGameBlockRoom();
				}
				else if (t.equalsIgnoreCase("RoboGameSubSphere"))
				{
					bo = new RoboGameSubSphere();
				}
				else if (t.equalsIgnoreCase("RoboGameSphere"))
				{
					bo = new RoboGameSphere();
				}
				else if (t.equalsIgnoreCase("RoboGameMassObj"))
				{
					bo = new RoboGameMassObj();
				}
				else if (t.equalsIgnoreCase("RoboGameSphereWithMass"))
				{
					bo = new RoboGameSphereWithMass();
				}
				else if (t.equalsIgnoreCase("RoboGameAvatar"))
				{
					bo = new RoboGameAvatar();
				}
				else if (t.equalsIgnoreCase("RoboGameWorldRoot"))
				{
					bo = new RoboGameWorldRoot();
				}
				else if (t.equalsIgnoreCase("RoboGameWorld"))
				{
					bo = new RoboGameWorld();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}*/
			case 'T':
			{
				if (t.equalsIgnoreCase("TextAdventureWorld"))
				{
					bo = new TextAdventureWorld();
				}
				break;	
			}
			
			case 'W':
			{				
				if (t.equalsIgnoreCase("WorldBase"))
				{
					bo = new WorldBase();
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			case '}':
			{
				if (t.equalsIgnoreCase("}"))
				{
					// This was an end marker, there are no more sub objects.
					bo = null;
				}
				else
				{
					static_error("unknown object "+t);
				}
				break;				
			}
			default:
			{
				debug("unknown object "+t);
				throw(new NumberFormatException("unknown object "+t));
			}
		}

		return bo;
	}

}
