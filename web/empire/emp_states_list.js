// emp_states_list.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb



// subclass extends superclass
EmpStatesList.prototype = Object.create(EmpBase.prototype);
EmpStatesList.prototype.constructor = EmpStatesList;

function EmpStatesList(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	
	// Todo: keep only one of these
	this.world.nationList=this;	
	parent.eStatesList=this;
	
}


EmpStatesList.prototype.selfToString=function()
{
	var str="name="+this.objName;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}



