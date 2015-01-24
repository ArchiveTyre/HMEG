// emp_world.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-03-29 by Henrik Bjorkman www.eit.se/hb



// subclass extends superclass
EmpWorld.prototype = Object.create(EmpBase.prototype);
EmpWorld.prototype.constructor = EmpWorld;

function EmpWorld(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	console.log("EmpWorld: '"+this.arg+"'");	

	this.eUnitTypeList;
	this.eStatesList=null;
	this.eTerrain=null;
}

EmpWorld.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.gameSpeed=arg[10];
	this.gameTime=arg[11]; // ???
	
	this.world.empireWorld=this;

	this.info=arg.slice(3);
	
}

EmpWorld.prototype.selfToString=function()
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



EmpWorld.prototype.getEmpUnitTypeList=function()
{
	// Following code probably is not needed.
	/*if (this.eUnitTypeList==null)
	{
		if (this.children!=null)
		{
			var i;
			for(i in this.children)
			{
				if (this.children[i] instanceof EmpUnitTypeList)
				{
					this.eUnitTypeList=this.children[i];
				}
			}
		}
	}*/	

	return this.eUnitTypeList;
}


EmpWorld.prototype.getEmpireStatesList=function()
{
	// Following code probably is not needed.
	/*if (this.eStatesList==null)
	{
		if (this.children!=null)
		{
			var i;
			for(i in this.children)
			{
				if (this.children[i] instanceof EmpStatesList)
				{
					this.eStatesList=this.children[i];
				}
			}
		}
	}*/	

	return this.eStatesList;
}


EmpWorld.prototype.getEmpireTerrain=function()
{
	// Following code probably is not needed.
	if (this.eTerrain==null)
	{
		if (this.children!=null)
		{
			var i;
			for(i in this.children)
			{
				if (this.children[i] instanceof EmpTerrain)
				{
					this.eTerrain=this.children[i];
				}
			}
		}
	}	

	return this.eTerrain;
}

EmpWorld.prototype.getState=function()
{
	return this.getEmpireStatesList().children[empWin.mapNation];
}

