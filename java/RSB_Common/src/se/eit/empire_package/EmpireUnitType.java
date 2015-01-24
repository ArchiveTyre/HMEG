//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;

import se.eit.db_package.*;
import se.eit.web_package.*;



public class EmpireUnitType extends EmpireBase {

    final static public int InfiniteBuildCostEconomical=9999;
    final static public int InfiniteBuildCostCapacity=9999;
    final static public int InfiniteMoveCost=9999;
    final static public int InfiniteMass=9999;

    final static public int HomeSectorBonus=50; // Extra bonus money to each state that is still owning its home sector.

	public String typeName;        // Name of this unit type
	public int income;             // If positive value: how much money the unit gives, if negative: how much it will cost to have this unit.
	public int buildMoneyCost;     // How much money it will cost to start building this unit
	public int buildCapacityCost;  // How move points (not money) it will buildMoneyCost to start building this unit
	public int landMoveCost;       // How many move points the unit needs to move on land (speed is the inverse of this value)
	public int seaMoveCost;        // How many move points the unit needs to move at sea
	public int unitMass;           // How heavy the unit is
	public int landCarryCapacity;  // How much mass the unit can carry on land (it must also carry itself)
	public int seaCarryCapacity;   // How much mass the unit can carry at see
	public int maxHealth;          // How many hits this unit can receive without being destroyed.
	public int attackProbability;  // Probability of hitting other units. The higher value the more likely to score a hit.
	public int attackStrength;     // How many hits (health loss) this unit will inflict if it scores a hit
	public int landDefenseMoving;  // Probability of avoiding hits by other units at land, the higher value the more likely to avoid a hit.
	public int landDefenseStill;   // Probability of avoiding hits by other units at land, the higher value the more likely to avoid a hit.
	public int seaDefence;         // Probability of avoiding hits by other units at sea
	public int CrewContribution;         // If <0 How much crew it needs, if >0 How much crew it produces
	public String canTransformInto;        // What the unit turns into if transformed
	public String transformationMaterial; // Material needed to transform unit into something else.
	public String disassemblesInto;        // What the unit turns into if disassembled         
	public String possibleOrders;  // What orders the unit can do
	public String possibleBuilds;  // What the unit can build (if it can build see possibleOrders)
	public String buildMaterialNeeded;  // Material needed to build this type of object
	public String requiredTerrain;  // In which type of sectors the object can reside

	public static final int MaxMovePoints=10;           // How many move points a unit can have
	public static final int minMovePointsToMove=0;      // How many move points a unit must have to make a move
	public static final int minMovePointsToBuild=0;     // How many move points a unit must have to build something
	public static final int movePointsUntilUnlinked=10; // If a unit was destroyed, keep it a few game ticks before unlinking it.
	public static final int MaxAttackPoints=10;         // How many attack points a unit can have
	public static final int attackCost=10;              // How many attack points it costs to attack
	public static final int minAttackPointsToAttack=10; // How many attack points that are needed to perform an attack 	
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireUnitType.class.getSimpleName();	
	}


	public EmpireUnitType(DbBase parent, String typeName, int buildMoneyCost, int buildCapacityCost, int income, int landMoveCost, int seaMoveCost, int unitMass, int landCarryCapacity, int seaCarryCapacity, int maxHealth, int attackProbability, int attackStrength, int landDefenseMoving, int landDefenseStill, int seaDefence, int CrewContribution, String canTransformInto, String transformationMaterial, String disassemblesInto, String possibleOrders, String possibleBuilds, String buildMaterialNeeded, String requiredTerrain) 
	{
		super();
		parent.addObject(this);
		this.setName(typeName);

		this.typeName=typeName;
		this.buildMoneyCost=buildMoneyCost;		
		this.buildCapacityCost=buildCapacityCost;
		this.income=income;
		this.landMoveCost=landMoveCost;
		this.seaMoveCost=seaMoveCost;
		this.unitMass=unitMass;
		this.landCarryCapacity=landCarryCapacity;
		this.seaCarryCapacity=seaCarryCapacity;
		this.maxHealth=maxHealth;
		this.attackProbability=attackProbability;
		this.attackStrength=attackStrength;
		this.landDefenseMoving=landDefenseMoving;
		this.landDefenseStill=landDefenseStill;
		this.seaDefence=seaDefence;
		this.CrewContribution=CrewContribution;
		this.canTransformInto=canTransformInto;
		this.transformationMaterial=transformationMaterial;
		this.disassemblesInto=disassemblesInto;
		this.possibleOrders=possibleOrders;
		this.possibleBuilds=possibleBuilds;
		this.buildMaterialNeeded=buildMaterialNeeded;
		this.requiredTerrain=requiredTerrain;
	}

	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"typeName "+typeName);		
		pw.println(prefix+"buildMoneyCost "+buildMoneyCost);		
		pw.println(prefix+"buildCapacityCost "+buildCapacityCost);		
		pw.println(prefix+"income "+income);		
		pw.println(prefix+"landMoveCost "+landMoveCost);		
		pw.println(prefix+"seaMoveCost "+seaMoveCost);		
		pw.println(prefix+"unitMass "+unitMass);		
		pw.println(prefix+"landCarryCapacity "+landCarryCapacity);		
		pw.println(prefix+"seaCarryCapacity "+seaCarryCapacity);		
		pw.println(prefix+"maxHealth "+maxHealth);				
		pw.println(prefix+"attackProbability "+attackProbability);		
		pw.println(prefix+"attackStrength "+attackStrength);		
		pw.println(prefix+"landDefenseMoving "+landDefenseMoving);		
		pw.println(prefix+"landDefenseStill "+landDefenseStill);		
		pw.println(prefix+"seaDefence "+seaDefence);	
		pw.println(prefix+"CrewContribution "+CrewContribution);	
		pw.println(prefix+"canTransformInto "+canTransformInto);
		pw.println(prefix+"transformationMaterial "+transformationMaterial);
		pw.println(prefix+"disassemblesInto "+disassemblesInto);	
		pw.println(prefix+"possibleOrders "+possibleOrders);		
		pw.println(prefix+"possibleBuilds "+possibleBuilds);
		pw.println(prefix+"buildMaterialNeeded "+buildMaterialNeeded);
		pw.println(prefix+"requiredTerrain "+requiredTerrain);
		
	}

	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("typeName"))
		{
			typeName=wr.readString();
			return 1;
		}
		else if (infoName.equals("buildMoneyCost"))
		{
			buildMoneyCost=wr.readInt();
			return 1;
		}
		else if (infoName.equals("buildCapacityCost"))
		{
			buildCapacityCost=wr.readInt();
			return 1;
		}
		else if (infoName.equals("income"))
		{
			income=wr.readInt();
			return 1;
		}
		else if (infoName.equals("landMoveCost"))
		{
			landMoveCost=wr.readInt();
			return 1;
		}
		else if (infoName.equals("seaMoveCost"))
		{
			seaMoveCost=wr.readInt();
			return 1;
		}
		else if (infoName.equals("unitMass"))
		{
			unitMass=wr.readInt();
			return 1;
		}
		else if (infoName.equals("landCarryCapacity"))
		{
			landCarryCapacity=wr.readInt();
			return 1;
		}
		else if (infoName.equals("seaCarryCapacity"))
		{
			seaCarryCapacity=wr.readInt();
			return 1;
		}
		else if (infoName.equals("maxHealth"))
		{
			maxHealth=wr.readInt();
			return 1;
		}
		else if (infoName.equals("attackProbability"))
		{
			attackProbability=wr.readInt();
			return 1;
		}
		else if (infoName.equals("attackStrength"))
		{
			attackStrength=wr.readInt();
			return 1;
		}
		else if (infoName.equals("landDefenseMoving"))
		{
			landDefenseMoving=wr.readInt();
			return 1;
		}
		else if (infoName.equals("landDefenseStill"))
		{
			landDefenseStill=wr.readInt();
			return 1;
		}
		else if (infoName.equals("CrewContribution"))
		{
			CrewContribution=wr.readInt();
			return 1;
		}
		else if (infoName.equals("canTransformInto"))
		{
			canTransformInto=wr.readString();
			return 1;
		}
		else if (infoName.equals("transformationMaterial"))
		{
			transformationMaterial=wr.readString();
			return 1;
		}
		else if (infoName.equals("disassemblesInto"))
		{
			disassemblesInto=wr.readString();
			return 1;
		}
		else if (infoName.equals("possibleOrders"))
		{
			possibleOrders=wr.readString();
			return 1;
		}
		else if (infoName.equals("possibleBuilds"))
		{
			possibleBuilds=wr.readString();
			return 1;
		}
		else if (infoName.equals("buildMaterialNeeded"))
		{
			buildMaterialNeeded=wr.readString();
			return 1;
		}
		else if (infoName.equals("requiredTerrain"))
		{
			requiredTerrain=wr.readString();
			return 1;
		}
		
		
		return super.setInfo(wr, infoName);
	}

	static public boolean isMixedLandAndSea(int terrainMask)
	{
		final int m=EmpireTerrain.SEA_TERRAIN_MASK | EmpireTerrain.LAND_TERRAIN_MASK;
		return (terrainMask & m)==m;
	}
	
	static public boolean isOpenSea(int terrainMask)
	{
		return (terrainMask & EmpireTerrain.SEA_TERRAIN_MASK)!=0;
	}
	
	static public boolean isDryLand(int terrainMask)
	{
		return (terrainMask & EmpireTerrain.LAND_TERRAIN_MASK)!=0;
	}

	public int getMoveCost(int terrainMask)
	{
		if (isMixedLandAndSea(terrainMask))
		{
			// Both land and sea units can move in this sector (river, lake or canal)
			return Math.min(landMoveCost,seaMoveCost);			
		}
		else if (isOpenSea(terrainMask))
		{
			// Only sea units can move in this sector (open sea, no bridge)
			return seaMoveCost;
		}
		else if (isDryLand(terrainMask))
		{
			// Only land units can move in this sector (land without rivers or canals)
			return landMoveCost;
		}
		else
		{
			return Math.max(landMoveCost,seaMoveCost);
		}
	}
	
	public int getCarryCapacity(int terrainMask)
	{
		if (isMixedLandAndSea(terrainMask))
		{
			// Both land and sea units can move in this sector (river, lake or canal)
			return Math.max(landCarryCapacity,seaCarryCapacity);			
		}
		else if (isOpenSea(terrainMask))
		{
			// Only sea units can move in this sector (open sea, no bridge)
			return seaCarryCapacity;
		}
		else if (isDryLand(terrainMask))
		{
			// Only land units can move in this sector (land without rivers or canals)
			return landCarryCapacity;
		}
		else
		{
			return Math.min(landCarryCapacity,seaCarryCapacity);
		}
	}
	
	public int getMoveCost(int fromTerrain, int toTerrain)
	{
		final int moveCostFromCurr=this.getMoveCost(fromTerrain);
		final int moveNextToPos=this.getMoveCost(toTerrain);
		
		final int moveCost=(moveCostFromCurr+moveNextToPos)/2;
		return moveCost;
	}
	

	public int getCarryCapacity(int fromTerrain, int toTerrain)
	{
		final int carryCapacityFromCurr=this.getCarryCapacity(fromTerrain);
		final int carryCapacityToPos=this.getCarryCapacity(toTerrain);
		
		final int carryCapacity=Math.min(carryCapacityFromCurr,carryCapacityToPos);
		return carryCapacity;
	}
	
	
	public int getDefenceMoving(int terrainMask)
	{
		if (isMixedLandAndSea(terrainMask))
		{
			// Both land and sea units can move in this sector (river, lake or canal)
			return Math.max(landDefenseMoving,seaDefence);			
		}
		else if (isOpenSea(terrainMask))
		{
			// Only sea units can move in this sector (open sea, no bridge)
			return seaDefence;
		}
		else if (isDryLand(terrainMask))
		{
			// Only land units can move in this sector (land without rivers or canals)
			return landDefenseMoving;
		}
		else
		{
			return Math.min(landDefenseMoving,seaDefence);
		}
	}

	public int getDefenceStill(int terrainMask)
	{
		if (isMixedLandAndSea(terrainMask))
		{
			// Both land and sea units can move in this sector (river, lake or canal)
			return Math.max(landDefenseMoving,seaDefence);			
		}
		else if (isOpenSea(terrainMask))
		{
			// Only sea units can move in this sector (open sea, no bridge)
			return seaDefence;
		}
		else if (isDryLand(terrainMask))
		{
			// Only land units can move in this sector (land without rivers or canals)
			return landDefenseStill;
		}
		else
		{
			return Math.min(landDefenseStill,seaDefence);
		}
	}
	
	public EmpireUnitType()
	{	
		super();
	}

	
	@Override
	public void readSelf(WordReader wr)	throws NumberFormatException
	{
		super.readSelf(wr);

		typeName=wr.readWord();        
		buildMoneyCost=wr.readInt();             
		buildCapacityCost=wr.readInt();    
		income=wr.readInt();           
		landMoveCost=wr.readInt();     
		seaMoveCost=wr.readInt();      
		unitMass=wr.readInt();         
		landCarryCapacity=wr.readInt();
		seaCarryCapacity=wr.readInt(); 
		maxHealth=wr.readInt();		   
		attackProbability=wr.readInt();
		attackStrength=wr.readInt();   
		landDefenseMoving=wr.readInt();      
		landDefenseStill=wr.readInt();      
		seaDefence=wr.readInt();
		CrewContribution=wr.readInt();
		canTransformInto=wr.readString();
		transformationMaterial=wr.readString();
		disassemblesInto=wr.readString();
		possibleOrders=wr.readString();
		possibleBuilds=wr.readString();
		buildMaterialNeeded=wr.readString();
		requiredTerrain=wr.readString();	
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		
		ww.writeWord(typeName);
		ww.writeInt(buildMoneyCost);
		ww.writeInt(buildCapacityCost);
		ww.writeInt(income);
		ww.writeInt(landMoveCost);
		ww.writeInt(seaMoveCost);
		ww.writeInt(unitMass);
		ww.writeInt(landCarryCapacity);
		ww.writeInt(seaCarryCapacity);
		ww.writeInt(maxHealth);
		ww.writeInt(attackProbability);
		ww.writeInt(attackStrength);
		ww.writeInt(landDefenseMoving);
		ww.writeInt(landDefenseStill);
		ww.writeInt(seaDefence);
		ww.writeInt(CrewContribution);
		ww.writeString(canTransformInto);
		ww.writeString(transformationMaterial);
		ww.writeString(disassemblesInto);
		ww.writeString(possibleOrders);
		ww.writeString(possibleBuilds);
		ww.writeString(buildMaterialNeeded);
		ww.writeString(requiredTerrain);
		
	}	
	
	
	
	
}