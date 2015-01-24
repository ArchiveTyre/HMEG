//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;

import se.eit.db_package.*;
import se.eit.web_package.*;




public class EmpireUnit extends ActiveObject {

	
	static final int NO_OWNER=-1;
	static final int CONTESTED_PROPERTY=-2;
	
	/*class typeProperties
	{
	}*/
	
	
	

    public int unitHealth;
	public int unitType;
	public int attackPoints;   // needed for unit to attack enemy units. 
    public int movePoints;     // needed for unit to move.
    
	public long unitTime=0;

    
    // temporary variable, not saved in file, it is only used during a tick
    //public int receivedHits;
    
    //typeProperties types[]=new typeProperties[10];
    
    
    

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireUnit.class.getSimpleName();	
	}


	public EmpireUnit(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);

		generateUnit();
		
		// will this work, will units have full parent hierarchy here?
		/*if (parent!=null)
		{
			final EmpireWorld ew=getEmpireWorld();
			//ew.setTickTimeCallback(this);
		}*/
	}

	public EmpireUnit()
	{	
		super();
	}

	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		unitHealth=wr.readInt();
		unitType=wr.readInt();
		attackPoints=wr.readInt();
		movePoints=wr.readInt();
		unitTime=wr.readLong();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(unitHealth);
		ww.writeInt(unitType);
		ww.writeInt(attackPoints);
		ww.writeInt(movePoints);
		ww.writeLong(unitTime);
	}	
	
	@Override
	public void listInfo(WordWriter ww, String prefix)
	{
		super.listInfo(ww, prefix);					
		ww.println(prefix+"unitType "+unitType);		
		ww.println(prefix+"attackPoints "+attackPoints);		
		ww.println(prefix+"movePoints "+movePoints);
		ww.println(prefix+"unitTime "+unitTime);		

	}

	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("unitType"))
		{
			unitType=wr.readInt();
			return 1;
		}
		else
		{
			return super.setInfo(wr, infoName);
		}
	}

	
	/*
	public EmpireTerrain getEmpireTerrain()
	{
		return (EmpireTerrain)getContainingObjOfClass(EmpireTerrain.class);
	}
	*/

	
	
	public void generateUnit()
	{
		//final EmpireWorld ew=getEmpireWorld();
		//final EmpireTerrain et=ew.getEmpireTerrain();

		
		owner=0;
		//pos=ew.generator.nextInt(et.sizeX*et.sizeY);
	}
	
	@Override
	public int getPosRecursive()
	{
		/*if (pos>=0)
		{
			return pos;
		}
		else
		{
			DbBase co=this.getContainingObj();
			if (co instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)co;
				return eu.getPosRecursive();
			}
		}*/
		
		DbBase co=this.getContainingObj();
		if (co instanceof EmpireUnit)
		{
			EmpireUnit eu=(EmpireUnit)co;
			return eu.getPosRecursive();
		}
		else if (co instanceof EmpireSector)
		{
			EmpireSector es=(EmpireSector)co;
			return es.getPosXY();
		}

		return -1;
	}

	// returns: 
	// XY coordinates if the unit is not aboard another unit
	// -1 if it is.
	public int getPosXY()
	{
		
		DbBase co=this.getContainingObj();
		if (co instanceof EmpireSector)
		{
			EmpireSector es=(EmpireSector)co;
			es.getPosXY();
		}

		return -1;
		
	}
	
	/*
	public EmpireOrder findOrder()
	{
		DbBase bo=iterateStoredObjects(null, EmpireOrder.class);
		return (EmpireOrder)bo;		
	}
	*/

	
	public int calculateStackMass()
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireUnitType eut=eul.getUnitType(this.unitType);
		
		int m=0;
		
		// Units own mass
		m+=eut.unitMass;
		
		// Add mass recursively
		// Mass of all child units (and their child units...)
		DbBase sol[]=getListOfSubObjects();
		for(int i=0; i<sol.length; i++)
		{
			DbBase so = sol[i];
			if (so instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)so;
				m+=eu.calculateStackMass();
			}
		}
		
		return m;
	}

	public int calculateOwnCrewContribution()
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireUnitType eut=eul.getUnitType(this.unitType);

		// It does not need crew if it is just cargo but it will contribute if it can
		if (isUnitCargo())
		{
			if (eut.CrewContribution<0)
			{
				return 0;
			}
		}
		
		return eut.CrewContribution;
	}
		
	public int calculateAvailableCrewMinusUsed()
	{

		// Units own crew contribution
		int m=calculateOwnCrewContribution();
		
		
		// Add crew contribution from sub units
		// all child units (and their child units...)
		DbBase sol[]=getListOfSubObjects();
		for(int i=0; i<sol.length; i++)
		{
			DbBase so = sol[i];
			if (so instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)so;
				m+=eu.calculateAvailableCrewMinusUsed();
			}
		}
		
		return m;
	}

	
	// This is used to know if the move can be made, 
	// It can tell if the move can be done now or ever, if not ever, its better to give up the move right away.
	public int calculateStackCarryCapacityNowOrEver(int fromTerrain, int toTerrain, boolean ever)
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireUnitType eut=eul.getUnitType(this.unitType);
				
		int c=0;
		
		// Units own carryCapacity regardless of current move points
		if ((eut.getMoveCost(fromTerrain)<EmpireUnitType.InfiniteMoveCost) && (eut.getMoveCost(toTerrain)<EmpireUnitType.InfiniteMoveCost))
		{
			if (ever || ((movePoints>=EmpireUnitType.minMovePointsToMove)))
			{
				c += eut.getCarryCapacity(fromTerrain, toTerrain);
			}
		}
		
		// Add carry capacity recursively
		for (DbBase so: this.listOfStoredObjects)
		{
			if (so instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)so;
				if (!eu.isUnitCargo())
				{
					c+=eu.calculateStackCarryCapacityNowOrEver(fromTerrain, toTerrain, ever);
				}
			}
		}

		return c;
	}
	
	
	
	public void payMoveCost(int fromTerrain, int toTerrain)
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireUnitType eut=eul.getUnitType(this.unitType);
		
		final int moveCostFromCurr=eut.getMoveCost(fromTerrain);
		final int moveNextToPos=eut.getMoveCost(toTerrain);
		
		// Units 
		final int moveCost=(moveCostFromCurr+moveNextToPos*3)/4;
				
	
		// Units own move cost
		// Pay units own move cost (only )
		if (movePoints>=EmpireUnitType.minMovePointsToMove)
		{
			// if it had enough move points to contribute carry capacity
			movePoints-=moveCost;
		}
		else
		{
			// Set to zero if it was cargo.
			movePoints = 0;
		}
		
		// pay move cost recursively
		DbBase sol[]=getListOfSubObjects();
		for(int i=0; i<sol.length; i++)
		{
			DbBase so = sol[i]; 
			if (so instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)so;
				eu.payMoveCost(fromTerrain, toTerrain);
			}
		}
	}
	
	
	
	// Returns null if OK, an error message if move can't be performed.
	public String moveUnit(int destSector)
	{
		final int curPosXY=getPosRecursive();
		if (curPosXY!=destSector)
		{
			final EmpireWorld ew=getEmpireWorld();
			final EmpireTerrain et=ew.getEmpireTerrain();
			
			//final int terrainDest=et.getTerrain(destSector);				
			final int terrainCurrMask=et.getTerrainMask(curPosXY);				

			final int d = et.getDirection(curPosXY, destSector);
			final int nextPos = et.getSectorIdByDirection(curPosXY, d);
			final int terrainNextPosMask=et.getTerrainMask(nextPos);
			
			final int stackMass=calculateStackMass();
			
			final int carryCapacityEver=calculateStackCarryCapacityNowOrEver(terrainCurrMask, terrainNextPosMask, true);
			
			final int availableCrewMinusUsed = calculateAvailableCrewMinusUsed();
			
			if (stackMass>carryCapacityEver)
			{
				// Unit is overloaded, it will never make the move.
				final String str="Giving up on move order, to heavy or can't move there.";
				debug(str);
				return str;
			}
			else if (availableCrewMinusUsed<0)
			{
				// insufficient crew
				final String str="Giving up on move order, insufficient crew";
				debug(str);
				return str;
			}
			else
			{
				final int carryCapacityCurrent=calculateStackCarryCapacityNowOrEver(terrainCurrMask, terrainNextPosMask, false);
				if (carryCapacityCurrent>=stackMass)
				{
					EmpireSector es=et.getSector(nextPos);
					
					// remove unit from its old position and add it on its new position.
					moveBetweenRooms(es);
					
					payMoveCost(terrainCurrMask, terrainNextPosMask);
	
					this.setUpdateCounter();			
				}
			}
			
		}
		return null;
	}
	
	
	public void takeHit(int attackStrength)
	{
		//final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		//final EmpireUnitType eut=eul.getUnitType(unitType);

		unitHealth-=attackStrength;

		if (unitHealth<=0)
		{
			final EmpireWorld ew=getEmpireWorld();
			ew.setTickCleanupCallback(this);
		}

		setUpdateCounter();
	}
	
/*
	@Override	
	public void gameTickEconomy()
	{

		
		
	}
	*/
	
	static public boolean isWordInString(String w, String s)
	{
		WordReader wr= new WordReader(s);
		while(wr.isOpenAndNotEnd())
		{
			String o=wr.readWord();
			if (o.equals(w))
			{
				return true;
			}
		}
		return false;
	}
	

	static int inc(int current, long delta, int max)
	{
		if (current==max)
		{
			return current;
		}		
		
		current+=delta;
		if (current>max)
		{
			return max;
		}
		
		return current;
	}

	
	// During game tick update only local properties of this object. Don't move, add or remove units. Don't touch/change other units. That is to be done during interact.
	@Override
	public void gameTick(long gameTime)
	{		
		final EmpireWorld ew=getEmpireWorld();
		final EmpireStatesList enl = ew.getEmpireNationsList();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireState en=enl.getEmpireNation(owner);
		final EmpireUnitType eut=eul.getUnitType(unitType);
		//final EmpireTerrain et=ew.getEmpireTerrain();
		//final EmpireSector pes=(this.getContainingObj() instanceof EmpireSector)?(EmpireSector)this.getContainingObj() : null;

		final long deltaGameTime=gameTime-unitTime;
		unitTime=gameTime;
		
		if(deltaGameTime>10)
		{
			debug("deltaGameTime "+deltaGameTime);
		}
			
		if (en!=null)
		{
			en.moneyChange+=deltaGameTime*eut.income;
			//en.setUpdateCounter();  // Perhaps we can find another place to do this update, it will happen quite frequently. In EmpireState.java perhaps?
		}
		
		
		if (attackPoints<EmpireUnitType.MaxAttackPoints)
		{
			attackPoints=inc(attackPoints, deltaGameTime, EmpireUnitType.MaxAttackPoints);
			//setUpdateCounter();
		}

		
		//debug("gameTime "+gameTime);
		if (movePoints<EmpireUnitType.MaxMovePoints)
		{
			// Unit has moved (or is new), recover move points.
			movePoints = inc(movePoints, deltaGameTime, EmpireUnitType.MaxMovePoints);
			setUpdateCounter();
		}
		else
		{
			if ((attackPoints==EmpireUnitType.MaxAttackPoints))
			{
				// Units recovers health only if it is not moving and not fighting (have full move points and full attack points).
				if (unitHealth<eut.maxHealth)
				{
					unitHealth = inc(unitHealth, deltaGameTime, eut.maxHealth);
					setUpdateCounter();
				}
			}

			/*
			// We need to cleanup destroyed units after some time, this is one way.
			if (this.unitType==EmpireUnitTypeList.NothingType)
			{
				this.unlinkSelf();
				return; // return here is needed to prevent this unit from registering tick callback.
			}
			else
			{
				ew.setTickTimeCallback(this); // This will make sure existing units keep getting tick.
			}
			*/
		}
		

        // This unit might have orders to do
		if (this.getNSubObjects()>0)
		{
			ew.setTickPerformOrdersQueueCallback(this);
		}
			
		// This unit may want to do some attack also.
		if (attackPoints>=EmpireUnitType.minAttackPointsToAttack)
		{
			ew.setTickInteractCallback(this);
		}

		
		// This unit needs to do cleanup. Or we could fix the owner issue here instead of in cleanup.
		if ((this.owner<0) || (this.unitType==EmpireUnitTypeList.NothingType))
		{
			ew.setTickCleanupCallback(this);
		}

	}
	
	public int getUnitTypeNumberFromUnitTypeName(String name)
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();

		DbBase db=eul.getChildFromIndexOrName(name);

		if (db!=null)
		{
			return db.getIndex();
		}
		
		debug("Did not find unit type "+name);
		
		return -1;
	}
	
	
    // Returns null if all was found
	// A message if not all was found
	public String checkOrUseBuildMaterial(String materialNames, boolean justCheck)
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final int n=eul.getListCapacity();
		String materialName="";
		
		// Get the list of materials as an array of strings
		String materialNeededNames[]=WordReader.split(materialNames);
				
		// arrays for all possible materials
		int materialAvailableOrUsed[]=new int[n];
		int buildMaterialNeeded[]=new int[n];
		
		// Now check what we need
		for(int i=0;i<materialNeededNames.length;++i)
		{
			int j=getUnitTypeNumberFromUnitTypeName(materialNeededNames[i]);
			if (j>=0)
			{
				buildMaterialNeeded[j]++;
			}
			else
			{
				return "unknown material needed "+materialNeededNames[i];
			}
		}
				
		// Now check what we have
		for(DbBase db: this.listOfStoredObjects)
		{
			if (db instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)db;
				int ut=eu.unitType;
				if (ut>=0)
				{
					if (!justCheck)
					{
						if (buildMaterialNeeded[ut]>materialAvailableOrUsed[ut])
						{
							eu.unitType=EmpireUnitTypeList.NothingType;
							eu.setUpdateCounter();
							materialAvailableOrUsed[ut]++;
						}
					}
					else
					{
						materialAvailableOrUsed[ut]++;
					}
				}
			}
		}
		
		// Check also for material in parent
		for(DbBase db: this.getParent().listOfStoredObjects)
		{
			if (db instanceof EmpireUnit)
			{
				EmpireUnit eu=(EmpireUnit)db;
				int ut=eu.unitType;
				if (ut>=0)
				{
					if (!justCheck)
					{
						if (buildMaterialNeeded[ut]>materialAvailableOrUsed[ut])
						{
							eu.unitType=EmpireUnitTypeList.NothingType;
							eu.setUpdateCounter();
							materialAvailableOrUsed[ut]++;
						}
					}
					else
					{
						materialAvailableOrUsed[ut]++;
					}
				}
			}
		}

		
		// Check that everything was found
		for(int i=0;i<n;++i)
		{
			if (buildMaterialNeeded[i]>materialAvailableOrUsed[i])
			{
				// This was not found, or not enough of it.
				materialName+=eul.getObjFromIndex(i).getName()+" ";
			}
		}
		
		// Everything needed was found
		if (materialName.length()!=0)
		{
			return materialName;
		}
		return null;
	}

	
	public boolean sectorOwnerOrElseClaimSector(final EmpireUnitType euttb)
	{
		if (euttb.unitMass<EmpireUnitType.InfiniteMass)
		{
			// The unit to build is not infinite mass, don't need to own sector
			return true;
		}

		EmpireSector es=getEmpireSectorRecursivelyForUnit();

		if (es.owner==this.owner)
		{
			return true;
		}
		
		
		return false;

	}

	
	
	
	
	public boolean isCorrectTerrain(final EmpireUnitType euttb)
	{
		String rt=euttb.requiredTerrain;
		
		if (rt.length()==0)
		{
			// This type of object can be built in any terrain
			return true;
		}
		
		int m=EmpireTerrain.getTerrainTypeMaskFromTerrainTypeNames(rt);
		
		EmpireSector es=getEmpireSectorRecursivelyForUnit();
		
		if ((m&es.terrain)!=m)
		{		
			// some required terrain is missing
			return false;
		}
		
		return true;
	}
	
	public boolean isThereRoomInSectorForBuilding(final EmpireUnitType euttb)
	{	
		if (euttb.unitMass<EmpireUnitType.InfiniteMass)
		{
			// The unit to build is not infinite mass
			return true;
		}

		// The unit to build is infinite mass, it can only be built in a sector with no other such units (such as cities)
		
		EmpireSector es=getEmpireSectorRecursivelyForUnit();
		
		if (es.calculateMassInSector()<EmpireUnitType.InfiniteMass)
		{
			return true;
		}
		
		return false;
	}
	
	// TODO: is this duplicate of getTerrainMaskRecursive?
	public EmpireSector getEmpireSectorRecursivelyForUnit()
	{
		if (this.getContainingObj() instanceof EmpireSector)
		{
			return (EmpireSector)this.getContainingObj();
		}
		else if (this.getContainingObj() instanceof EmpireUnit)
		{
			EmpireUnit eu=(EmpireUnit)this.getContainingObj();
			return eu.getEmpireSectorRecursivelyForUnit();
		}
		else
		{
			return null;
		}
	}
	
	
	@Override	
	public void gameTickPerformOrders()
	{

		final EmpireWorld ew=getEmpireWorld();
		final EmpireStatesList enl = ew.getEmpireNationsList();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireState en=enl.getEmpireNation(owner);
		final EmpireUnitType eut=eul.getUnitType(unitType);
		final EmpireTerrain et=ew.getEmpireTerrain();
		final EmpireSector pes=(this.getContainingObj() instanceof EmpireSector)?(EmpireSector)this.getContainingObj() : null;

	
		
		// check if there is an order to perform for this unit
		for (DbStorable bo : this.listOfStoredObjects)
		{

  		    if (bo instanceof EmpireOrder)
		    {
  		    	try
  		    	{
  		    	
					EmpireOrder eo = (EmpireOrder)bo;
					WordReader wr=new WordReader(eo.getOrder());
					String cmd=wr.readWord();
					
					
					if (!isWordInString(cmd, eut.possibleOrders))
					{
						tellOwner("Can not do that order "+cmd);
						bo.unlinkSelf();					
					}
					else if (cmd.equals("build"))
					{
						if ((en!=null) && (en.savedMoney>0))
						{
							String whatToBuild=wr.readWord();
							
							if (!isWordInString(whatToBuild, eut.possibleBuilds))
							{
								tellOwner("This unit can not build "+whatToBuild);
								bo.unlinkSelf();			
							}
							else
							{
								//debug("order " + bo.getId() + " build "+ whatToBuild);
								int i=getUnitTypeNumberFromUnitTypeName(whatToBuild);
	
								if (i>=0)
								{
									
									// This is just for debugging, can be removed later
									if (!eul.getUnitTypeName(i).equals(whatToBuild))
									{
										error("eul.getUnitTypeName(i) != whatToBuild "+ i+ " "+eul.getUnitTypeName(i)+" "+whatToBuild);
									}
									
									
									final EmpireUnitType euttb = eul.getUnitType(i); // Empire Unit Type To Build
									if (euttb.buildCapacityCost>=EmpireUnitType.InfiniteBuildCostCapacity)
									{
										tellOwner("can't build "+whatToBuild);
										bo.unlinkSelf();													
									}
									else if (en.savedMoney<0)
									{
										tellOwner("can't build, no money");
										bo.unlinkSelf();													
									}
									else
									{
										if (isCorrectTerrain(euttb))
										{
											if (isThereRoomInSectorForBuilding(euttb))
											{
												if (sectorOwnerOrElseClaimSector(euttb))
												{
													if (en.savedMoney>euttb.buildMoneyCost)
													{
														// this state has enough money
													
														if (movePoints>=EmpireUnitType.minMovePointsToBuild)
														{
															// the city has enough build points
															String materialMissing=checkOrUseBuildMaterial(euttb.buildMaterialNeeded, true);
															if (materialMissing==null)
															{
																// it has enough materials
																
																// now it will build something
																EmpireUnit eu = new EmpireUnit();
																eu.linkSelf(this.getParent());
																//eu.linkSelf(this); // wanted to dut the new object in "this" instead of parent but then all new objects get destroyed, very strange. 
																eu.setName(whatToBuild.substring(0, 1)+eu.getId()); // +"_from_"+getName()
																eu.unitType=i;
																eu.owner=owner;
																//eu.pos=pos;
																eu.attackPoints=0;
																eu.movePoints=0;
																movePoints-=euttb.buildCapacityCost;
																checkOrUseBuildMaterial(euttb.buildMaterialNeeded, false);
																en.moneyChange-=euttb.buildMoneyCost;
																eu.unitTime=unitTime;
																bo.unlinkSelf();
																//useMaterialToBuild(euttb.buildMaterialNeeded);
																//tellOwner("building unit "+whatToBuild); // Don't tell owner for now, it might be to much messages.
																this.setUpdateCounter();
																en.setUpdateCounter();
															}
															else
															{
																debug("order " + bo.getId() + " materialMissing="+materialMissing);
																tellOwner("to build "+whatToBuild + " you need more "+materialMissing);
																bo.unlinkSelf();					
															}
														}
														else
														{
															//debug("order " + bo.getId() + " not enough move points to build yet "+ whatToBuild+", have="+movePoints+", need="+EmpireUnitType.minMovePointsToBuild);
														}
													}
													else
													{
														//debug("order " + bo.getId() + " not enough money to build yet "+ whatToBuild+ " state " + en.getName() +" have " + en.savedMoney+ " need "+euttb.buildMoneyCost);
													}
												}
												else
												{
													//tellOwner("could not build "+whatToBuild + " here, it belongs to someone else, will try to claim it.");
													//bo.unlinkSelf();												
													
													// Will try to take sector
													EmpireSector es=getEmpireSectorRecursivelyForUnit();
													es.setNewOwner(this.owner);
												}
											}
											else
											{
												tellOwner("can't build "+whatToBuild + " here, there is already something here");
												bo.unlinkSelf();																										
											}
										}
										else
										{
											tellOwner("can't build "+whatToBuild + " here, it needs to be in: "+euttb.requiredTerrain);
											bo.unlinkSelf();															
										}
									}
								}
								else
								{
									tellOwner("Unknown unit to build "+whatToBuild);
									bo.unlinkSelf();					
								}
							}
						}
					}
					else if (cmd.equals("moveTo")) // TODO: move is deprecated, it is possible to use 'goTo' to move to a sector, the difference is that goTo expects an ID as parameter while 'move' expects a position on the map. 
					{
						if ((en!=null) && (en.savedMoney>0))
						{				
							int dest=wr.readInt();
			
							final String r = moveUnit(dest);			
			
							final int pos=getPosRecursive();
							if ((pos==dest) || (r!=null))
							{
								// destination reached or move is not possible, remove this order and tell owner
								bo.unlinkSelf();
								if (r!=null)
								{
									// something went wrong, move was not possible
									tellOwner(r);
								}
							}					
						}
						else
						{
							bo.unlinkSelf();
							tellOwner("can't move, no money");				
						}
					}
					else if (cmd.equals("scrap") || cmd.equals("transform") || cmd.equals("disassemble"))
					{
						debug("order " + bo.getId() + " scrap unit "+ getId());
	
						//int dest=wr.readInt();
	
						bo.unlinkSelf();
						
						WordReader mwr;
						
						if (cmd.equals("disassemble"))
						{
							mwr=new WordReader(eut.disassemblesInto);
							tellOwner("was disassembled as ordered");
						}
						else if (cmd.equals("transform"))
						{
							String materialMissing=checkOrUseBuildMaterial(eut.transformationMaterial, true);
							if (materialMissing==null)
							{
								mwr=new WordReader(eut.canTransformInto);
								checkOrUseBuildMaterial(eut.transformationMaterial, false);
								tellOwner("was transformed as ordered");
							}
							else
							{
								tellOwner("need "+materialMissing+" to transform");
								break;
							}
						}
						else
						{
							mwr=new WordReader("");
							tellOwner("was scrapped as ordered");
						}
	
						if (mwr.isOpenAndNotEnd())
						{
							int m2=getUnitTypeNumberFromUnitTypeName(mwr.readName());
	
							if (m2>=0)
							{
								this.unitType=m2;
							
								while(mwr.isOpenAndNotEnd())
								{
									m2=getUnitTypeNumberFromUnitTypeName(mwr.readName());
		
									if (m2>=0)
									{
										EmpireUnit eu = new EmpireUnit(this.getParent(), this.getName());
										eu.unitType=m2;
										eu.owner=owner;
										eu.attackPoints=0;
										eu.movePoints=this.movePoints;
									}
								}
							}
						}
						else
						{				
							// If we do unlink here we may get a null pointer exception later, so for now set it to nothing. 
							//this.unlinkSelf();
							this.unitType=EmpireUnitTypeList.NothingType;
						}
						this.setUpdateCounter();
					}
					else if ((cmd.equals("goTo")) || (cmd.equals("board"))) // board is deprecated, use goTo
					{
						DbIdList il=this.getDbIdList();					
						
						int idOfDestObj=wr.readInt();
		
						debug("order id " + bo.getId() + ", unit "+ getId()+ ", goTo id "+idOfDestObj);
		
						DbBase destObj=il.getDbIdObj(idOfDestObj);
						
						/*if (destObj instanceof EmpireUnit)
						{
							final int curPosXY=getPosRecursive();
							EmpireUnit deu=(EmpireUnit)destObj;
		
							if (curPosXY==deu.getPosRecursive())
							{
								// Both units are at some location
								//this.pos=-1;
								int r= this.moveBetweenRooms(deu);
								if (r!=0)
								{
									tellOwner("tried to board unit "+idOfDestObj+" but could not");								
								}
								bo.unlinkSelf();
								deu.setUpdateCounter();
								this.setUpdateCounter();
								//tellOwner("boarded unit "+idOfDestObj);
							}
							else
							{
								// Not at same location, so move in that direction
								int dest=deu.getPosRecursive();
								if (dest>=0)
								{
									moveUnit(dest);
								}
								else
								{
									// perhaps destination unit is destroyed? Cancel order.
									tellOwner("tried to board unit "+idOfDestObj+" but could not find it");
									bo.unlinkSelf();
								}
							}
						}
						else*/ if (destObj instanceof EmpireUnitOrSector)
						{
							final int curPosXY=getPosRecursive();
							EmpireUnitOrSector deu=(EmpireUnitOrSector)destObj;
		
							if (curPosXY==deu.getPosRecursive())
							{
								// Both units are at some location
								//this.pos=-1;
								int r= this.moveBetweenRooms(deu);
								if (r!=0)
								{
									tellOwner("tried to board unit "+idOfDestObj+" but could not");								
								}
								bo.unlinkSelf();
								deu.setUpdateCounter();
								this.setUpdateCounter();
								//tellOwner("boarded unit "+idOfDestObj);
							}
							else
							{
								// Not at same location, so move in that direction
								int dest=deu.getPosRecursive();
								if (dest>=0)
								{
									moveUnit(dest);
								}
								else
								{
									// perhaps destination unit is destroyed? Cancel order.
									tellOwner("tried to board unit "+idOfDestObj+" but could not find it");
									bo.unlinkSelf();
								}
							}
						}
						else
						{
							tellOwner("this unit can not go to ~"+idOfDestObj);
							bo.unlinkSelf();							
						}
					}
					else if (cmd.equals("unload"))
					{
						// unload all stored EmpireUnit units
						debug("order " + bo.getId() + " unload "+ getId());
						
						// Get sector that this unit is in, unloaded units are to be placed in the sector
						EmpireSector es=et.getSector(getPosRecursive());
						
						// Get a list of all stored objects
						DbBase[] list=getListOfSubObjects();
						
						for(int i=0;i<list.length;i++)
						{
							if (list[i] instanceof EmpireUnit)
							{
								EmpireUnit eus=(EmpireUnit)list[i];
								eus.moveBetweenRooms(es);
								eus.setUpdateCounter();
								//break;
							}
						}
		
						// order hopefully performed so remove it.
						bo.unlinkSelf();
					}
					else
					{
						tellOwner("unknown command:"+cmd);
						bo.unlinkSelf();
					}
  		    	}
  		    	catch (NumberFormatException e)
  		    	{
					tellOwner("failed command:"+e);
					bo.unlinkSelf();
  		    	}
			}
		}
		
		// Check if this unit shall claim a sector (change sector owner)
		if (pes !=null)
		{
			//if (eut.attackStrength>0)
			if (eut.income>0)
			{
					pes.setNewOwner(this.owner);
			}
		}
		
	}
	

	public int getTerrainMaskRecursive()
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireTerrain et=ew.getEmpireTerrain();
		final int pos=getPosRecursive();
		return et.getTerrainMask(pos);		
	}

	
	// Calculate defenders strength
	public int calculateEnemyDefence()
	{
		final EmpireWorld ew=getEmpireWorld();
		final EmpireTerrain et=ew.getEmpireTerrain();
		final int pos=getPosRecursive();
		
		int defStrength=0;
		
		// loop, look at nearby sectors, count number of defenders.
		for(int d=EmpireTerrain.SAME_SECTOR; d<EmpireTerrain.N_SECTOR_NEIGHBORS_1;d++)
		{
			final int sid=et.getSectorIdByDirection(pos, d);
			
			defStrength+=et.calcDefensiveStrengthAtSector(sid, owner);
		}
		return defStrength;
	}
		
	public boolean isUnitCargo()
	{	
		final DbContainer parent = this.getParent();

		if (parent instanceof EmpireSector)
		{
			// It is not cargo if it is in a sector (then it is not aboard another unit)
			return false;
		}
		
		if (parent instanceof EmpireUnit)
		{
			// This unit is aboard another unit but if that is a unit of same type its not cargo
			
			EmpireUnit eu = (EmpireUnit)parent;
			
			if (eu.unitType != this.unitType)
			{
				// It is aboard a unit of another type so it is cargo.
				return true;
			}			
			else
			{
				// Same type of unit, check if that unit is cargo
				return eu.isUnitCargo();
			}
		}
		
		// This is not in a sector and not in a unit. What is it in.
		debug("unexpected cargo");
		return true;
	}
	
	@Override	
	public void gameTickInteract()
	{		
		final EmpireWorld ew=getEmpireWorld();
		final EmpireTerrain et=ew.getEmpireTerrain();
		final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
		final EmpireUnitType eut=eul.getUnitType(unitType);

		// has it reloaded? If not it will fight later
		if (attackPoints>=EmpireUnitType.minAttackPointsToAttack)
		{
			// yes, this unit have enough attack points to fight
				
			// Check if this unit can attack other units
			final int as=eut.attackProbability;
			if (as>0)
			{			
				// Units does not fight if they are loaded onto other units.
				if (!isUnitCargo())
				{		
					// This unit can attack if it is on appropriate terrain
					// Is it on suitable ground or water for the unit to fight from.
					final int terrainMask=getTerrainMaskRecursive();
					final int carryCapacity=eut.getCarryCapacity(terrainMask);
					if (carryCapacity>eut.unitMass) // TODO: should perhaps use "required terrain" instead?
					{
						// This unit is in a sector from which it can fight/attack

						// Are there enemies nearby to attack?
						final int defStrength=calculateEnemyDefence();	
				
						// was there units to attack?
						if (defStrength>0)
						{
							// There are enemies within range
							
							// Does it have enough crew			
							final int availableCrewMinusUsed = calculateAvailableCrewMinusUsed();
							if (availableCrewMinusUsed>=0)
							{			
								// It does have crew
								
								attackPoints-=EmpireUnitType.attackCost;
								
								final int ap=ew.generator.nextInt(as*100);
								final int dp=ew.generator.nextInt(defStrength*100);
								
								final int pos=getPosRecursive();
								
								if (ap >= dp)
								{
									// hit successfully
			
									final int damage=ew.generator.nextInt(eut.attackStrength);
									
									tellOwner("at sector "+pos+ " scored a hit, damage="+damage+", probability="+as+":"+defStrength);


									// loop nearby sectors and increment hit count
									for(int d=EmpireTerrain.SAME_SECTOR; d<EmpireTerrain.N_SECTOR_NEIGHBORS_1;d++)
									{
										final int sid=et.getSectorIdByDirection(pos, d);
										
										
										et.hitSector(sid, owner, damage);					
									}
								}
								else
								{
									// miss
									debug("#"+this.getId()+" "+this.getName() + " at sector "+pos+ " failed its attack "+as+" "+defStrength);
			
									tellOwner("at sector "+pos+ " missed, "+as+":"+defStrength);
								}
							}
							else
							{
								//tellOwner("has insufficient crew to fight");
							}
						}
						else
						{
							// no units to attack
						}
					}
					else
					{
						//debug("the unit can not fight from this terrain");
					}
				}
				else
				{
					// This unit is loaded onto other units, so it can not fight now.
				}
			}
			else
			{
				// this unit can not attack other units
			}
		}
	}
	

	
	@Override	
	public void gameTickCleanup()
	{

		if (this.unitType==EmpireUnitTypeList.NothingType)
		{
			// This unit is destroyed let it stay a few ticks so that players can see where their unit is.
			if (movePoints>=EmpireUnitType.movePointsUntilUnlinked)
			{
				this.unlinkSelf();
			}
		}
		else
		{
			final EmpireWorld ew=getEmpireWorld();
			final EmpireUnitTypeList eul=ew.getEmpireUnitTypeList();
			final EmpireUnitType eut=eul.getUnitType(unitType);
			final EmpireTerrain et=ew.getEmpireTerrain();

			
			// Does this unit have an owner? 
			if (owner<0)
			{
				// No, is this a city (or other unit that earns money)?
				if (eut.income>0)
				{
					// city has no owner, city can be captured.
					// Get owner of the sector the city is in
					int newOwner=et.getSectorOwner(getPosRecursive());
					if (newOwner>=0)
					{
						setOwner(newOwner);				
						tellOwner("was captured");
						setUpdateCounter();
					}
				}
			}
			
			// Was this unit shot at
			if (unitHealth<=0)
			{
				// is it a city
				// Either cities should require a crew or they shall need to be rebuilt if captured. Have not decided yet. Cities requiring a crew is not implemented yet anyway.
				/*if (eut.income>0)
				{
					// Yes, cities are not destroyed, only change owner
					if (owner!=-1)
					{
		                tellOwner("was lost");
						owner=-1;
					}
					unitHealth=0;
					setUpdateCounter();
				}
				else*/
				{
					// no, destroy this unit (unless it is already destroyed)
					if (this.unitType!=EmpireUnitTypeList.NothingType)
					{
						tellOwner("was destroyed");
						this.unitType=EmpireUnitTypeList.NothingType;
						//unitHealth=0;
						movePoints=0;
					}
				}
			}

		}
		 
	}
	
	// It is enemy if it is not own, not nobody's and not ally
	public boolean isEnemy(int attacker)
	{
		if ((owner!=attacker) && (owner!=-1))
		{
			// Is it a non ally
			final EmpireWorld ew=getEmpireWorld();
			final EmpireStatesList enl = ew.getEmpireNationsList();
			final EmpireState a=enl.getEmpireNation(attacker);
			final EmpireState o=enl.getEmpireNation(owner);
			final String idStrA="~"+a.getId();
			final String idStrO="~"+o.getId();
			if (!a.isAlly(idStrO))
			{
				return true;
			}
			if (!o.isAlly(idStrA))
			{
				return true;
			}
		}
		return false;
	}


}

