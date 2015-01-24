// emp_unit_type.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-28 by Henrik Bjorkman www.eit.se/hb



// subclass extends superclass
EmpUnitTypeList.prototype = Object.create(EmpBase.prototype);
EmpUnitTypeList.prototype.constructor = EmpUnitTypeList;

function EmpUnitTypeList(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	
	// Todo: keep only one of these
	this.world.unitTypesList=this;
	parent.eUnitTypeList=this;
}



