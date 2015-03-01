// MirrorFactory.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb, all rights reserved.
// Created 2015-01-30 by Henrik Bjorkman


function MirrorFactory(world, parent, cType, arg)
{
	/*if (parent==null)
	{
		console.log("parent is null, cType " + cType+" "+arg);
	}
	else
	{
		console.log("parent.id "+parent.id +", cType " + cType+" "+arg);
	}*/

	if (cType == 'EmpireUnit')
	{
		return new EmpUnit(world, parent, cType, arg);
	}
	else if (cType == 'EmpireSector')
	{
		return new EmpSector(world, parent, cType, arg);
	}
	else if (cType == 'EmpireOrder')
	{
		return new EmpOrder(world, parent, cType, arg);
	}
	else if (cType == 'EmpireTerrain')
	{
		return new EmpTerrain(world, parent, cType, arg);
	}
	else if ((cType == 'EmpireState') || (cType == 'EmpireNation'))
	{
		return new EmpState(world, parent, cType, arg);
	}
	else if (cType == 'EmpireRoundBuffer')
	{
		return new EmpRoundBuffer(world, parent, cType, arg);
	}
	else if ((cType == 'EmpireStatesList') || (cType == 'EmpireNationsList'))
	{
		return new EmpStatesList(world, parent, cType, arg);
	}
	else if (cType == 'EmpireUnitType')
	{
		return new EmpUnitType(world, parent, cType, arg);
	}
	else if (cType == 'EmpireUnitTypeList')
	{
		return new EmpUnitTypeList(world, parent, cType, arg);
	}
	else if (cType == 'EmpireWorld')
	{
		return new EmpWorld(world, parent, cType, arg);
	}
	else if (cType == 'RsbLong')
	{
		return new MirrorLong(world, parent, arg);
	}
	else if (cType == 'RsbString')
	{
		return new MirrorString(world, parent, arg);
	}
	else if (cType == 'HmegWorld')
	{
		return new HmegWorld(world, parent, arg);
	}
	else if (cType == 'CityPvpRoom')
	{
		return new HmegRoom(world, parent, arg);
	}
	else if (cType == 'CityPvpEntity')
	{
		return new HmegEntity(world, parent, arg);
	}
	else if (cType == 'CityPvpAvatar')
	{
		return new HmegAvatar(world, parent, arg);
	}
	/*else if (cType == 'RsbBigBitMap')
	{
		return new MirrorBigBitMap(world, parent, arg);
	}*/
	else if (cType == 'RsbRoundBuffer')
	{
		return new MirrorRoundBuffer(world, parent, arg);
	}
	
	
	console.log("unknown cType " + cType+" "+arg);


	return null;
}

