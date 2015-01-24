// emp_state.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb





// subclass extends superclass
EmpState.prototype = Object.create(EmpBase.prototype);
EmpState.prototype.constructor = EmpState;

function EmpState(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	
	this.eRoundBuffer=null;
}

EmpState.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.sectorOwner=parseInt(arg[3]);
	this.savedMoney = parseInt(arg[4]);
	this.moneyChange = parseInt(arg[5]);
	this.headOfState = parseInt(arg[6]);
	this.coRulers = parseInt(arg[7]);
	this.homeSectorId = parseInt(arg[8]);
	//this.info=arg.slice(3);
}

EmpState.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", money="+this.money;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}






