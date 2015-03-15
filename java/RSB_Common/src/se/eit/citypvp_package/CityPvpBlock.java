package se.eit.citypvp_package;

public  class CityPvpBlock {

	
static int air = 0;
static int dirt = 1;
static int ladder = 2;
static int grass = 3;
static int doorIn = 6;// also more known as room; #ENTITY#
static int doorOut = 7;
static int wood = 8;
static int woodstairsleft = 9;
static int woodstairsright = 10;
static int controlPanel = 11;
static int log = 12;
static int avatarFigure = 13;// avatar figure. #ENTITY#
static int ballon = 14;
static public boolean recistance(int id, int force)
{
	if (force >= 3 && id == 3)
	{
		return true;
	}

	return false;
}	
	
	static public boolean isWalkable(int id)
	{
		
		if (id == ladder || id == air || id == doorOut || id == woodstairsleft || id == woodstairsright || id == controlPanel)
		{
			return true;
		}
		return false;
	}

	
		public static int inBlockGravity(int id) {
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
	static public int loot_wood (int id) { return 1;}
	static public int loot_stone (int id){return 1; }
	static public int loot_mineral (int id){return 1;}
	
	static public int cost_wood (int id) { return 1;}
	static public int cost_stone (int id){return 1; }
	static public int cost_mineral (int id){return 1;}
	
	
	
	
	
}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

