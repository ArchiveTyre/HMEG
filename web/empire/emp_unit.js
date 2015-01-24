// emp_unit.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-03-29 by Henrik Bjorkman www.eit.se/hb


		



// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/create


// subclass extends superclass
EmpUnit.prototype = Object.create(EmpBase.prototype);
EmpUnit.prototype.constructor = EmpUnit;



function EmpUnit(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
}

EmpUnit.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.unitOwner=parseInt(arg[3]);
	this.unitHealth=parseInt(arg[4]);
	this.unitType=parseInt(arg[5]);
	//this.attackPoints=parseInt(arg[6]);
	//this.movePoints=parseInt(arg[7]);
	//this.unitTime=parseInt(arg[8]);

	//this.info=arg.slice(3);
}


EmpUnit.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", id="+this.id;
	str+=", type="+this.world.unitTypesList.children[this.unitType].objName;
	str+=", unitHealth="+this.unitHealth;
	str+=", owner="+this.unitOwner;

	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}


EmpUnit.prototype.showSelfUnitContextXY=function(context, x, y, width, height)
{
	var t=this.getUnitType();
	if (t!=null)
	{
		t.showSelfContextXY(context, x, y, width, height);
	}
	else
	{
		context.font = '8pt Calibri';
		context.fillStyle = 'black';
		context.fillText(this.unitType, x, y+8);
	}
}













