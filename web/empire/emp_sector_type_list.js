// emp_unit_type.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-01-15 by Henrik Bjorkman www.eit.se/hb




function EmpSectorTypeList()
{

	this.children=[];
	this.children['city'] = new EmpSectorType('city');
	this.children['mineralDeposit'] = new EmpSectorType('mineralDeposit');
	this.children['field'] = new EmpSectorType('field');
	this.children['beach'] = new EmpSectorType('beach');
	this.children['land'] = new EmpSectorType('land');
	this.children['sea'] = new EmpSectorType('sea');
	this.children['unknown'] = new EmpSectorType('unknown');
	
}



EmpSectorTypeList.prototype.getSectorType=function(sectorTerrain)
{
	if ((sectorTerrain&4)!=0)
	{
		return this.children['city'];
	}
	else if ((sectorTerrain&16)!=0)
	{
		return this.children['mineralDeposit'];
	}
	else if ((sectorTerrain&8)!=0)
	{
		return this.children['field'];
	}
	else if ((sectorTerrain&3)==3)
	{
		return this.children['beach'];
	}
	else if ((sectorTerrain&2)!=0)
	{
		return this.children['land'];
	}
	else if ((sectorTerrain&1)!=0)
	{
		return this.children['sea'];
	}
	else
	{
		return this.children['unknown'];
	}
}

