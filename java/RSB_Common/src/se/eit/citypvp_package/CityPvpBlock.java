package se.eit.citypvp_package;

public  class CityPvpBlock {

	
static int air = 0;
static int dirt = 1;
static int ladder = 2;
static int grass = 3;
// id 6 = door in
static int doorOut = 7;
static int wood = 8;
static int woodstairsleft = 9;
static int woodstairsright = 10;
static int controlPanel = 11;

	
	
	static public String getBlockTexture(int id)
	{
		if (id == air)
		{
			return "air";
		}
		if (id == dirt)
		{
			return "dirt";
		}
		if (id ==ladder)
		{
			return "ladder";
		}
		if (id == grass)
		{
			return "grass";
		}
		if (id == doorOut)
		{
			return "door";
		}
		if (id == wood)	
		{
			return "wood";
		}
		if (id == woodstairsright)	
		{
			return "woodstairright";
		}
		if (id == woodstairsleft)	
		{
			return "woodstairleft";		
		}
		if (id == controlPanel)	
		{
			return "controlpanel";
			
		}
		if (id == controlPanel)	
		{
			return "controlpanel";	
		}		
		return "unknown";

		
	}
	static public boolean isWalkable(int id)
	{
		
		if (id == ladder || id == air || id == doorOut || id == woodstairsleft || id == woodstairsright || id == controlPanel)
		{
			return true;
		}
		return false;
	}
	static public int inBlockGravity(int id)
	{
		if (id == ladder)
		{
			return 0;
		}
		if (id == woodstairsleft || id == woodstairsright)
		{
			return -1;
		}
		return 1;
	}
	
	
	
	
	
	
	
}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

