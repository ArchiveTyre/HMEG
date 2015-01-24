// emp_terrain.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb



// subclass extends superclass
EmpTerrain.prototype = Object.create(EmpBase.prototype);
EmpTerrain.prototype.constructor = EmpTerrain;

function EmpTerrain(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	
	parent.eTerrain=this;
}

EmpTerrain.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.world.terrain=this;
	this.sizeX=parseInt(arg[3]);
	this.sizeY=parseInt(arg[4]);

	//this.info=arg.slice(3);
}

// deprecated, to be removed when no longer used.
/*
EmpTerrain.prototype.showSelfContext=function(context)
{
	this.showSelfTerrainContext(context);
}
*/

// deprecated, to be removed when no longer used.
EmpTerrain.prototype.showSelfTerrainContext=function(context)
{
	var len = this.children.length
	for (var i=0; i<len; ++i)
	{
		if (i in this.children)
		{
			var c=this.translateSectorIndexToColumn(i);
			var r=this.translateSectorIndexToRow(i);

			var offset=empWin.mapCalcOffSet(r);
			this.children[i].showSelfAndSubUnitContext(context, offset+c*empWin.mapSectorWidth, r*empWin.mapSectorHeight, empWin.mapSectorWidth, empWin.mapSectorHeight);
		}
	}
}


