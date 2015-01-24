// emp_unit.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

/*
// subclass extends superclass
EmpMisc.prototype = Object.create(EmpBase.prototype);
EmpMisc.prototype.constructor = EmpMisc;

function EmpMisc(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
}


EmpMisc.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", empType="+this.empType;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}
*/




function EmpBaseFactory(world, parent, cType, arg)
{
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
	

	return new EmpMisc(world, parent, cType, arg);
}


