//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;




//import se.eit.rsb_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;


//Only objects of type "EmpireSector" shall be stored in objects of this class.



public class EmpireTerrain extends EmpireBase {
	

	//StringFifo stringFifo;	
	
	public int sizeX;
	public int sizeY;
	
	//public int terrain[];


	
	final static int INFINITE_MOVE_COST=0x7FFFFFFF;
	final static int N_SECTOR_NEIGHBORS_1=6;
	final static int N_SECTOR_NEIGHBORS_2=18;
	final static int N_SECTOR_NEIGHBORS_3=36;
	final static int SAME_SECTOR=-1;
	final static int maxNations = 64;
	final static int ETERNITY = 1000000;
		 
	final static int N_CITYS = 8;
	final static int N_ISLANDS = 6;
	final static int N_BEACH_FACTOR = 5;
	final static int N_FIELDS_FACTOR = 10;
	final static int N_MINERAL_FACTOR = 30;
	
	// TODO: Perhaps we can move these to EmpireSector.java
	// Bitmasks for the property of a sector (max 32 bits, since int is used to store this)
	final static int SEA_TERRAIN_MASK = 1; // If true ships can move here
	final static int LAND_TERRAIN_MASK = 2;    // If true land units can move here
	final static int CITY_TERRAIN_MASK = 4; // If true a city can be here  
	final static int FIELD_TERRAIN_MASK = 8; // If true a farm can be placed here   
	final static int MINERAL_TERRAIN_MASK = 0x10; // If true a metal production can be placed here
 
 
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireTerrain.class.getSimpleName();	
	}


	public EmpireTerrain(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);
		generateTerrain();
	}

	public EmpireTerrain()
	{	
		super();
	}

	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		//debug("created unit " + name + " at "+id);
		// It seems we should check that database is locked here. It is not always...
		sizeX=wr.readInt();
		sizeY=wr.readInt();		
		debug("readSelf "+sizeX+" "+sizeY);
		
		/*terrain=new int[sizeX*sizeY];
		for(int i=0;i<sizeX*sizeY;i++)
		{
			terrain[i]=wr.readInt();
		}*/
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(sizeX);
		ww.writeInt(sizeY);
		
		/*for(int i=0;i<sizeX*sizeY;i++)
		{
			ww.writeInt(terrain[i]);
		}*/
	}	
	
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"sizeX "+sizeX);		
		pw.println(prefix+"sizeY "+sizeY);		
	}

	
	public int getNSectorNeighbors(int distance)
	{
		switch(distance)
		{
		case 0: return -1;
		case 1: return N_SECTOR_NEIGHBORS_1;
		case 2: return N_SECTOR_NEIGHBORS_2;		
		case 3: return N_SECTOR_NEIGHBORS_3;
		default: return -1;
		}
	}
	
	public boolean isNextToTerrain(int pos, int terrainMask, int distance)
	{
		final int n=getNSectorNeighbors(distance);
		
		for(int i=0;i<n;i++)
		{
			int sid=getSectorIdByDirection(pos, i);
			if ((getTerrainMask(sid) & terrainMask)!=0)
			{
				return true;
			}
		}
		return false;
	}
	
	
	
	public void makeCanalToSea(int pos)
	{
		final EmpireWorld ew=getEmpireWorld();
		
		for(int i=0;i<100;i++)
		{			
			int direction=ew.generator.nextInt(N_SECTOR_NEIGHBORS_1); // try randomly

			int sid=getSectorIdByDirection(pos, direction);
			if (isNextToTerrain(sid, SEA_TERRAIN_MASK, 1))
			{
				setTerrainMask(sid,SEA_TERRAIN_MASK);
				return;
			}
		}
	}

	
	public void generateTerrain()
	{
		
		final EmpireWorld ew=getEmpireWorld();

		ew.lockWrite();

		try
		{
		
			sizeX=32;
			sizeY=32;		
	
			// Generate the terrain
			//terrain = new int[sizeX*sizeY];
	
			/*for(int x=0;x<sizeX;x++)
			{
				for(int y=0;y<sizeY;y++)
				{
					new EmpireSector(this, "x"+x+"y"+y);
				}
			}*/
			
			int landSectors=0;
			
			// create some islands/continents
			for(int n=0;n<N_ISLANDS;n++)
			{
				//int x=sizeX/4+ew.generator.nextInt(sizeX/2);
				//int y=sizeY/4+ew.generator.nextInt(sizeY/2);
				//int sid0=getSectorId(x,y);
				int sid0=ew.generator.nextInt(sizeX*sizeY);

				
				for(int i=-1;i<N_SECTOR_NEIGHBORS_3;i++)
				{
					int sid=getSectorIdByDirection(sid0, i);
					if ((getTerrainMask(sid) & LAND_TERRAIN_MASK) == 0)  // not land here
					{
						// this was sea, now it is land
						clrTerrainMask(sid,SEA_TERRAIN_MASK);
						setTerrainMask(sid,LAND_TERRAIN_MASK);
						landSectors++;
					}
				}
			}
	
			
			// create more land if needed, shall be at least 25%
			while ((landSectors*4)<(sizeX*sizeY))
			{
				// Find a sea sector that is next to some land
				int j=1000;
				while (--j>=0)
				{
					int sid=ew.generator.nextInt(sizeX*sizeY);
					if ((getTerrainMask(sid) & LAND_TERRAIN_MASK) == 0)  // not land here
					{						
						// It must be next to at least one land sector
						if (isNextToTerrain(sid, LAND_TERRAIN_MASK, 1))
						{
							// this was sea, now it is land
							clrTerrainMask(sid,SEA_TERRAIN_MASK);
							setTerrainMask(sid, LAND_TERRAIN_MASK);
							landSectors++;
							break;
						}
					}
				}
			}

			
			// Count how many possible beaches we have.
			int nCoastal=0;
			for(int pos=0; pos<sizeX*sizeY; pos++)
			{
				// Is it land
				if ((getTerrainMask(pos) & LAND_TERRAIN_MASK) > 0)
				{
					// this is land
					
					// is it next to at least one sea sector
					if (isNextToTerrain(pos, SEA_TERRAIN_MASK, 1))
					{
						// it is	
						nCoastal++;
					}
				}
				
			}
			
			// Add some beaches
			for(int i=0; i<nCoastal/N_BEACH_FACTOR; i++)
			{
				// Find a land sector
				int j=100;
				while (--j>=0)
				{
					int pos=ew.generator.nextInt(sizeX*sizeY);
					if ((getTerrainMask(pos) & LAND_TERRAIN_MASK) > 0)
					{
						// this is land
						
						// It must be next to at least one sea sector
						if (isNextToTerrain(pos, SEA_TERRAIN_MASK, 1))
						{
							// this is land, now with a river or canal	
							setTerrainMask(pos, SEA_TERRAIN_MASK);
							break;
						}
					}
				}
			}

			
			// Add some fields
			for(int i=0; i<landSectors/N_FIELDS_FACTOR; i++)
			{
				// Find a land sector
				int j=100;
				while (--j>=0)
				{
					int pos=ew.generator.nextInt(sizeX*sizeY);
					if ((getTerrainMask(pos)&LAND_TERRAIN_MASK) != 0)
					{
						// this is land, now with a field	
						setTerrainMask(pos, FIELD_TERRAIN_MASK);
						break;
					}
				}
			}
			
		
			// Add some mineral deposits
			for(int i=0; i<landSectors/N_MINERAL_FACTOR; i++)
			{
				// Find a land sector
				int j=100;
				while (--j>=0)
				{
					int pos=ew.generator.nextInt(sizeX*sizeY);
					if ((getTerrainMask(pos)&LAND_TERRAIN_MASK) != 0)
					{
						// this is land, now with a mineral deposit	
						setTerrainMask(pos, MINERAL_TERRAIN_MASK);
						break;
					}
				}
			}
			

			
			// Add some terrain where cities can be built
			for(int i=0; i<N_CITYS*2; i++)
			{
				// Find a land sector
				int j=1000; // used to prevent eternal loop, if something went wrong
				while (--j>=0)
				{
					int pos=ew.generator.nextInt(sizeX*sizeY);
					if ((getTerrainMask(pos) & LAND_TERRAIN_MASK) != 0)
					{
						// this was land
						
						// don't want to be too close to other cities, if possible
						if ((j<100) || (!isNextToTerrain(pos, CITY_TERRAIN_MASK, 1)))
						{				
							setTerrainMask(pos, SEA_TERRAIN_MASK);
							setTerrainMask(pos, CITY_TERRAIN_MASK);
	
							// If city is not next to sea check make a canal/river if it is not too far from sea.
							if ((!isNextToTerrain(pos, SEA_TERRAIN_MASK, 1)) && (isNextToTerrain(pos, SEA_TERRAIN_MASK, 2)))
							{
								makeCanalToSea(pos);						
							}
														
							break;
							
						}			
					}
				}
			}

						
			
			
			// Add some castles, one for each state.
			for(int i=0; i<N_CITYS; i++)
			{
				final EmpireStatesList enl = ew.getEmpireNationsList();
				final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
				final EmpireState es=enl.getEmpireNation(i);
				// Find a city sector
				int j=1000000;
				while (--j>0)
				{
					int pos=ew.generator.nextInt(sizeX*sizeY);
					if (  ((getTerrainMask(pos)&CITY_TERRAIN_MASK)!=0)   ||   ((j<1000) && ((getTerrainMask(pos)&LAND_TERRAIN_MASK)!=0))  )
					{
						// this was land possible to put a city on
							
						EmpireSector s = (EmpireSector)getObjFromIndex(pos);

						// Is it free
						if ((s.getNSubObjects()==0) || (j<1000))
						{							
							setTerrainMask(pos, CITY_TERRAIN_MASK);
							s.setOwner(i);
							es.homeSectorId=s.getId();
							
							// There must be some initial units in the city
							
							EmpireUnit eu2=new EmpireUnit();
							eu2.linkSelf(s);
							eu2.setName("t"+eu2.getId());
							eu2.unitType=EmpireUnitTypeList.TowerType;
							eu2.owner=i;
							eu2.unitHealth=eul.getUnitType(EmpireUnitTypeList.TowerType).maxHealth;
							

							EmpireUnit eu4=new EmpireUnit();
							eu4.linkSelf(eu2);
							eu4.setName("c"+eu4.getId());
							eu4.unitType=EmpireUnitTypeList.CrewType;
							eu4.owner=i;

							/*EmpireUnit eu3=new EmpireUnit(eu, "i"+i);
							eu3.unitType=EmpireUnitTypeList.InfType;
							eu3.owner=i;
							
							EmpireUnit eu4=new EmpireUnit(eu, "i"+i);
							eu4.unitType=EmpireUnitTypeList.InfType;
							eu4.owner=i;*/
							
							break;
							
						}
							
					}
				}
			}
			
			
		}
		finally
		{
			ew.unlockWrite();
		}
	}

	
	public EmpireSector getSector(int sid)
	{
		EmpireSector s = (EmpireSector)getObjFromIndex(sid);
		
		if (s==null)
		{
			s = new EmpireSector();
			s.setName("x"+getSectorX(sid)+"y"+getSectorY(sid));
			s.setIndex(sid); // We want this object to be at the given index
			s.setTerrain(SEA_TERRAIN_MASK); // Initially all sectors are sea
			this.addObject(s);
			s.setUpdateCounter();
		}
		
		return s;
	}
	

	public int getTerrainMask(int sid)
	{
		EmpireSector s = (EmpireSector)getObjFromIndex(sid);

		if (s==null)
		{
			return SEA_TERRAIN_MASK; // All undefined sectors are sea
		}
		
		return s.getTerrain();
	}
	
	/*
	public void setTerrain(int sid, int terrain)
	{
		//return terrain[sid];
		EmpireSector s = getSector(sid);
		s.setTerrain(terrain);
	}
	*/

	public void setTerrainMask(int sid, int terrainMask)
	{
		//return terrain[sid];
		EmpireSector s = getSector(sid);
		s.setTerrain(s.getTerrain() | terrainMask);
	}
	
	// this is not tested yet
	public void clrTerrainMask(int sid, int terrainMask)
	{
		//return terrain[sid];
		EmpireSector s = getSector(sid);
		s.setTerrain(s.getTerrain() & ~terrainMask);
	}

	public int getTerrainMask(int x, int y)
	{
		return getTerrainMask(getSectorId(x,y));
	}
	
	public int getSectorId(int x, int y)
	{
		return x+sizeX*y;
	}
	
	public int getSectorX(int id)
	{
		return id%sizeX;
	}
	
	public int getSectorY(int id)
	{
		return id/sizeX;
	}
	
	// Returns -1 if no direction (from is same as to).
	// 0 = right
	// 1 = up-right
	// 2 = up-left
	// 3 = left
	// 4 = down-left
	// 5 = down-right
	public int getDirection(int from, int to)
	{
		final int fromX=getSectorX(from);
		final int fromY=getSectorY(from);
		final int toX=getSectorX(to);
		final int toY=getSectorY(to);
		int dX=toX - fromX;
		int dY=toY - fromY;
		
		// Wrap around X
		if (dX>sizeX/2)
		{
			dX-=sizeX;
		}
		else if (dX<-sizeX/2)
		{
			dX+=sizeX;
		}
		
		// Wrap around Y
		if (dY>sizeY/2)
		{
			dY-=sizeY;
		}
		else if (dY<-sizeY/2)
		{
			dY+=sizeY;
		}

		
		
		if (dY>0)
		{
			// 04  or 05
			if (dX>0)
			{
				return 5;
			}
			else if (dX<0)
			{
				return 4;
			}
			else
			{
				if ((fromY%2)==0)
				{
					return 5;					
				}
				else
				{
					return 4;					
				}				
			}
		}
		else if (dY<0)
		{
			// 02  or 01
			if (dX>0)
			{
				return 1;
			}
			else if (dX<0)
			{
				return 2;
			}
			else
			{
				if ((fromY%2)==0)
				{
					return 1;					
				}
				else
				{
					return 2;					
				}
			}
		}
		else
		{
			//  03, -1 or 00
			if (dX>0)
			{
				return 0;
			}
			else if (dX<0)
			{
				return 3;
			}
			else
			{
				return -1;
			}	
		}
	}

/*
Sector numbering if size is 32x32 
00  01  02  04 ...
  32  33  34  35 ...  
64  65  66  67 ...
  96  97  98  99 ...
.
.
.


Directions
        24  23  22  21
      25  10  09  08  20
    26  11  02  01  07  19
  27  12  03  -1  00  06  18
    28  13  04  05  17  35
      29  14  15  16  34
        30  31  32  33


*/
	
	
	public int getSectorToRight(int id)
	{
		int x=id%sizeX;
		int y=id/sizeX;
		if (x>=sizeX-1)
		{
			x=0;
		}
		else
		{
			x++;
		}
		return x+y*sizeX;
	}

	public int getSectorToLeft(int id)
	{
		int x=id%sizeX;
		int y=id/sizeX;
		if (x<=0)
		{
			x=sizeX-1;
		}
		else
		{
			x--;
		}
		return x+y*sizeX;
	}

	private int getSectorToUpp(final int id)
	{
		int x=id%sizeX;
		int y=id/sizeX;
		if (y==0)
		{
			y=sizeY-1;
		}
		else
		{
			y--;
		}
		return x+y*sizeX;
	}
	
	private int getSectorToDown(final int id)
	{
		int x=id%sizeX;
		int y=id/sizeX;

		if (y==sizeY-1)
		{
			y=0;
		}
		else
		{
			y++;
		}	
		return x+y*sizeX;
	}
	
	
	public int getSectorToUppRight(final int id)
	{
		//int x=id%sizeX;
		int y=id/sizeX;

		// If we are at even row then move just up.
		if ((y%2)==0)
		{
			return getSectorToUpp(id);
		}
		
		// Otherwise move left and up		
		return getSectorToRight(getSectorToUpp(id));
	}

	public int getSectorToUppLeft(final int id)
	{
		//int x=id%sizeX;
		int y=id/sizeX;

		// If we are at uneven row then move just up.
		if ((y%2)!=0)
		{
			return getSectorToUpp(id);
		}
		
		// Otherwise move left and up		
		return getSectorToLeft(getSectorToUpp(id));
	}

	public int getSectorToLowLeft(final int id)
	{
		//int x=id%sizeX;
		int y=id/sizeX;

		// If we are at uneven row then move just down.
		if ((y%2)!=0)
		{
			return getSectorToDown(id);
		}
		
		// Otherwise move left and up		
		return getSectorToLeft(getSectorToDown(id));
	}
	
	public int getSectorToLowRight(final int id)
	{
		//int x=id%sizeX;
		int y=id/sizeX;

		// If we are at uneven row then move just down.
		if ((y%2)==0)
		{
			return getSectorToDown(id);
		}
		
		// Otherwise move left and up		
		return getSectorToRight(getSectorToDown(id));
	}

	
	public int getSectorIdByDirection(final int id, final int a)
	{
		switch (a)
		{
			case -1: return id; 
			case 0: return getSectorToRight(id);
			case 1: return getSectorToUppRight(id);
			case 2: return getSectorToUppLeft(id);
			case 3: return getSectorToLeft(id);
			case 4: return getSectorToLowLeft(id);
			case 5: return getSectorToLowRight(id);

			case 6: return getSectorToRight(getSectorToRight(id));
			case 7: return getSectorToRight(getSectorToUppRight(id));
			case 8: return getSectorToUppRight(getSectorToUppRight(id));
			case 9: return getSectorToUppLeft(getSectorToUppRight(id));
			case 10: return getSectorToUppLeft(getSectorToUppLeft(id));
			case 11: return getSectorToLeft(getSectorToUppLeft(id));
			case 12: return getSectorToLeft(getSectorToLeft(id));
			case 13: return getSectorToLeft(getSectorToLowLeft(id));
			case 14: return getSectorToLowLeft(getSectorToLowLeft(id));
			case 15: return getSectorToLowLeft(getSectorToLowRight(id));
			case 16: return getSectorToLowRight(getSectorToLowRight(id));
			case 17: return getSectorToRight(getSectorToLowRight(id));					

			case 18: return getSectorToRight(getSectorToRight(getSectorToRight(id)));
			case 19: return getSectorToRight(getSectorToRight(getSectorToUppRight(id)));
			case 20: return getSectorToRight(getSectorToUppRight(getSectorToUppRight(id)));
			case 21: return getSectorToUppRight(getSectorToUppRight(getSectorToUppRight(id)));
			case 22: return getSectorToUppRight(getSectorToUppRight(getSectorToUppLeft(id)));
			case 23: return getSectorToUppRight(getSectorToUppLeft(getSectorToUppLeft(id)));
			case 24: return getSectorToUppLeft(getSectorToUppLeft(getSectorToUppLeft(id)));
			case 25: return getSectorToLeft(getSectorToUppLeft(getSectorToUppLeft(id)));
			case 26: return getSectorToLeft(getSectorToLeft(getSectorToUppLeft(id)));
			case 27: return getSectorToLeft(getSectorToLeft(getSectorToLeft(id)));
			case 28: return getSectorToLeft(getSectorToLeft(getSectorToLowLeft(id)));
			case 29: return getSectorToLeft(getSectorToLowLeft(getSectorToLowLeft(id))); 								
			case 30: return getSectorToLowLeft(getSectorToLowLeft(getSectorToLowLeft(id))); 					
			case 31: return getSectorToLowLeft(getSectorToLowLeft(getSectorToLowRight(id))); 					
			case 32: return getSectorToLowLeft(getSectorToLowRight(getSectorToLowRight(id))); 					
			case 33: return getSectorToLowRight(getSectorToLowRight(getSectorToLowRight(id))); 					
			case 34: return getSectorToRight(getSectorToLowRight(getSectorToLowRight(id))); 					
			case 35: return getSectorToRight(getSectorToRight(getSectorToLowRight(id)));			
			
			default: break;
		}

		return -1;
	}

	

	
	
	public EmpireUnit findUnitAt(int x, int y)
	{
		int pos=getSectorId(x,y);
		DbBase sol[]=getListOfSubObjects();
		for(int i=0; i<sol.length; i++)
		{
			DbBase so = sol[i];
			if (so instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)so;
				if (eu.getPosRecursive()==pos)
				{
					return eu;
				}
			}
		}
		return null;
	}
	
	
	/*
	public EmpireWorld getEmpireWorld()
	{
	    DbBase bo = this.getContainingObj();
	    
	    if (bo instanceof EmpireWorld)
	    {
			return (EmpireWorld)bo;
	    }
	    else  if (bo instanceof EmpireTerrain)
	    {
	    	EmpireTerrain et=(EmpireTerrain)bo;
			return et.getEmpireWorld();
	    }
	    return null;
	}
*/

	
	
	// Calculate defensive strength in a given sector 
	public int calcDefensiveStrengthAtSector(int sid, int attacker)
	{
		int defStrength=0;
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();

		final int st=getTerrainMask(sid);
		
		// look for enemies
		
		EmpireSector es = getSector(sid);
		if (es.getNSubObjects()>0)
		{
		  for (DbStorable bo : es.listOfStoredObjects)
		  {
			if (bo instanceof EmpireUnit)
			{
				final EmpireUnit eu=(EmpireUnit)bo;
				
				// Cargo does not contribute to defense.
				if (!eu.isUnitCargo())
				{
					// Is it an enemy
					if (eu.isEnemy(attacker))
					{
						final int t=eu.unitType;
						final EmpireUnitType eut=eul.getUnitType(t);
						
						if (eu.movePoints < EmpireUnitType.MaxMovePoints)
						{
							defStrength+=eut.getDefenceMoving(st);
						}
						else
						{
							defStrength+=eut.getDefenceStill(st);							
						}
					}
				}
			}
		  }				
		}

		return defStrength;
	}
	
	
	public void hitSector(int sid, int attacker, int attackStrength)
	{
		//final EmpireWorld ew=getEmpireWorld();
		//final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();

		// look for enemies of attacker

		EmpireSector es = getSector(sid);
		if (es.getNSubObjects()>0)
		{
			for (DbStorable bo : es.listOfStoredObjects)
			{
				if (bo instanceof EmpireUnit)
				{
					final EmpireUnit eu=(EmpireUnit)bo;
					
					// Cargo does not get hit directly.
					if (!eu.isUnitCargo())
					{
						// Only enemy units are hit
						if (eu.isEnemy(attacker))
						{
							eu.takeHit(attackStrength);
						}
					}
				}
			}		
		}
	}


	// Returns >=0 if only one nation can claim the sector.
	// -1 if no one can
	// -2 if more than one can
	public int getSectorOwner(int sid)
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		int owner=-1;



		EmpireSector es = getSector(sid);
		
		if (listOfStoredObjects!=null)
		{
			for (DbStorable bo : es.listOfStoredObjects)
			{
				if (bo instanceof EmpireUnit)
				{
					final EmpireUnit eu=(EmpireUnit)bo;
					//if (eu.getPosXY()==sid)
					{
						final EmpireUnitType eut=eul.getUnitType(eu.unitType);
						
						if (eut.attackProbability>0)
						{
							if (owner==-1)
							{
								// at least one possible owner. 
								owner=eu.owner;
							}
							else if (owner!=eu.owner)
							{
								// units from more than one country in sector -> no owner 
								return -2;
							}
						}		
					}
				}
			}
		}		
		return owner;
	}
	
	
	// one and only one word may be in the name when calling this
	public static int getTerrainTypeMaskFromTerrainTypeName(String name)
	{
		if (WordReader.isInt(name))
		{
			return Integer.parseInt(name);
		}
		else if (name.equals("sea"))
		{
			return SEA_TERRAIN_MASK;
		}
		else if (name.equals("land"))
		{
			return LAND_TERRAIN_MASK;			
		}
		else if (name.equals("city"))
		{
			return CITY_TERRAIN_MASK;			
		}
		else if (name.equals("field"))
		{
			return FIELD_TERRAIN_MASK;			
		}
		else if (name.equals("mineralDeposit"))
		{
			return MINERAL_TERRAIN_MASK;			
		}
		else
		{
			return -1;
		}
	}
	
	public static int getTerrainTypeMaskFromTerrainTypeNames(String names)
	{
		String a[]=WordReader.split(names);
		int m=0;
						
		for(int i=0;i<a.length;i++)
		{
			m|=EmpireTerrain.getTerrainTypeMaskFromTerrainTypeName(a[i]);
		}
		return m;
	}

	// one and only one bit may be set in the mask when calling this
	public static String getTerrainTypeNameFromTerrainTypeMask(int mask)
	{
		switch(mask)
		{
			case SEA_TERRAIN_MASK: return "sea";
			case LAND_TERRAIN_MASK: return "land";
			case CITY_TERRAIN_MASK: return "city";
			case FIELD_TERRAIN_MASK: return "field";
			case MINERAL_TERRAIN_MASK: return "mineralDeposit";
			default: return "";
		}
	}
	
	public static String getTerrainTypeNamesFromTerrainTypeMask(int mask)
	{
		String name="";
		for(int i=0;i<32;i++)
		{
			if (((1<<i) & mask) != 0)
			{
				if (name.length()!=0)
				{
					name+=" ";
				}
				name+=getTerrainTypeNameFromTerrainTypeMask(1<<i);
			}
		}
		return name;
	}
}