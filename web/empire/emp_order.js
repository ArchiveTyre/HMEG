// emp_order.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb


		



// subclass extends superclass
EmpOrder.prototype = Object.create(EmpBase.prototype);
EmpOrder.prototype.constructor = EmpOrder;

function EmpOrder(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	//console.log("empWorldInfo: EmpireOrder '"+this.order+"'");	
	
}

EmpOrder.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.order=hlibRemoveQuotes(arg[3]);

	this.info=arg.slice(3);
}


EmpOrder.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", order="+this.order;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}


