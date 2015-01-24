package se.eit.TextAdventure;
import se.eit.db_package.*;
import se.eit.rsb_package.*;

public class TextAdventureRoom extends GameBase {
	
	
	public TextAdventureRoom doors[] = new TextAdventureRoom[6];
	public String door_name[] = new String[6];
	
	
	public TextAdventureRoom(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);

	    // TODO: vad mer behövs här?	
		
		
	}
	
	/*
	public int list(PlayerCommandInterpreter pci)
	{
	
	    // list doors
	    listDoors();

	    // list objects in this room
	    listObjects();
	}
	*/
	
	/*
	public int listDoors(PlayerCommandInterpreter pci)
	{
	
	    // list doors
		int n=0;
		pci.println("From this room you can go:");
		for(int i=0;i<6;i++)
		{		
			if (doors[i]!=null)
			{
				pci.println("  "+ door_name[i] + " to " + doors[i].name);
				n++;
			}
		}	
	    if (n==0) 
	    {
	    	pci.println("nowhere");
	    }
	}

	public int listObjects(PlayerCommandInterpreter pci)
	{
	
	    // list doors
	    n=0;
	    pci.println("In this room there is:");
		for(int i=0;i<64;i++)
		{
			if (listOfsubGameObj[i]!=null)
			{
				pci.println("  "+ listOfsubGameObj[i].name);
				n++;
			}
		}
	    if (n==0) 
	    {
	    	pci.println("nothing");
	    }
		return n;
	}
	*/
	
	
	public int connect(TextAdventureRoom other_room, String door_name)
	{
		for(int i=0;i<6;i++)
		{
			if (doors[i]==null)
			{
				doors[i]= other_room;
				this.door_name[i] = door_name;
				return 0;
			}
		}
		System.out.println("Room is full.");
		return -1;		
	}
	
	
	public final TextAdventureRoom findDoor(String name)
	{
		for(int i=0;i<6;i++)
		{
			if (doors[i]!=null)
			{
				if (this.door_name[i].equals(name))
				{
				    return doors[i];
				}
			}
		}
		return null;
	}
	
	
	

}
