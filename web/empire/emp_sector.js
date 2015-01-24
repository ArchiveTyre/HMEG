// emp_sector.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-28 by Henrik Bjorkman www.eit.se/hb




// subclass extends superclass
EmpSector.prototype = Object.create(EmpBase.prototype);
EmpSector.prototype.constructor = EmpSector;

function EmpSector(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	this.unitOwner=parseInt(arg[3]);
	this.sectorTerrain=parseInt(arg[4]);
	
}

EmpSector.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.unitOwner=parseInt(arg[3]);
	this.sectorTerrain=parseInt(arg[4]);

	//this.info=arg.slice(3);
}


EmpSector.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", id="+this.id;
	str+=", owner="+this.unitOwner;

    str+=", sectorTerrain="+this.sectorTerrain;

	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}

EmpSector.prototype.getSectorTypeName=function(sectorTerrain)
{
	var typeName;
	
	if ((sectorTerrain&4)!=0)
	{
		typeName='city';
	}
	else if ((sectorTerrain&16)!=0)
	{
		typeName='mineralDeposit';
	}
	else if ((sectorTerrain&8)!=0)
	{
		typeName='field';
	}
	else if ((sectorTerrain&3)==3)
	{
		typeName='beach';
	}
	else if ((sectorTerrain&2)!=0)
	{
		typeName='land';
	}
	else if ((sectorTerrain&1)!=0)
	{
		typeName='sea';
	}
	else
	{
		typeName='unknown';		
	}

	return typeName;
}

EmpSector.prototype.showSectorSubUnitContext=function(context, x, y, width, height)
{
	var n=this.getNChildUnits();
	if (n==1)
	{
		for(i in this.children)
		{
			this.children[i].showSelfUnitContextXY(context, x, y, width, height);
		}
	}
	else if (n>0)
	{
		context.font = '12pt Calibri';
		context.fillStyle = 'black';
		context.fillText(""+n, x+width/4, y+(height*2)/3);
	}
	else
	{
	}

}

EmpSector.prototype.showSelfSectorContextBkg=function(context, x, y, width, height)
{
	var typeName='unknown';
	
	/*
	if (this.sectorTerrain>1)
	{
	    // land
	    
		// Here we can either look at units in the sector or who owns the sector itself. Both should work but result is a little different.
		var o = this.getSectorOwner();
		// var o = this.unitOwner	
	
		if (o == empWin.mapNation)
		{			
			context.fillStyle="#8080F0";
		}
		else if (o >= 0)
		{
			context.fillStyle="#F08080";
		}
		else if (o == -2)
		{
			context.fillStyle="#D080D0";
		}
		else
		{
			context.fillStyle="#B0F0B0"; // for dark green use: "#00FF00"	
		}
	}
	else
	{
	    // sea
		context.fillStyle="#CFCFCF";  // for dark blue use: "#4060FF"	
	}
	context.fillRect(x+4,y+4,width-8,height-8);
	*/
	
	    
	var s = this.getSectorOwnerState(empWin.mapNation);

	//  0  : If nobody own the sector
	//  1  : State 'state' own this sector (alone) 
	//  2  : State 'state' have no units in the sector but other states do.
	//  3  : State 'state' have units in the sector and other states too.
	switch(s)
	{
		case 1:		
			context.fillStyle="#8080F0"; // This sector belong to this player
			context.fillRect(x,y,width,height);
			break;
		case 2:
			context.fillStyle="#F08080"; // Some other player own this sector
			context.fillRect(x,y,width,height);
			break;
		case 3:
			context.fillStyle="#D080D0"; // More than one player in this sector
			context.fillRect(x,y,width,height);
			break;
		default:				
			break;
	}

	if (this.sectorTerrain>1)
	{
		// land
		context.fillStyle="#B0F0B0"; // for dark green use: "#00FF00"	
	}
	else
	{
	    // sea
		context.fillStyle="#CFCFCF";  // for dark blue use: "#4060FF"	
	}
	context.fillRect(x+1,y+1,width-2,height-2);
}

/*
EmpSector.prototype.showSelfSectorContextImg=function(context, x, y, width, height)
{

	typeName=this.getSectorTypeName(this.sectorTerrain);

	var imageObj = new Image();
	imageObj.onload = function() {
		context.drawImage(imageObj, x, y-Math.round((width-height)/2), width, width); // Images are 64x64 but map on the rows are 56px high, so using width as height. Images shall have some extra transparency in upper and lower instead.
	};
	imageObj.src = typeName+".png";

	//context.stroke();
}
*/

EmpSector.prototype.showSelfSectorContextImg=function(context, x, y, width, height)
{
	var t = empWin.sectorTypesList.getSectorType(this.sectorTerrain);
	
	t.showSelfContextXY(context, x, y, width, height);
}


EmpSector.prototype.showSelfSectorContext=function(context, x, y, width, height)
{
	this.showSelfSectorContextBkg(context, x, y, width, height);
	this.showSelfSectorContextImg(context, x, y, width, height);
}


EmpSector.prototype.showSelfAndSubUnitContext=function(context, x, y, width, height)
{
	this.showSelfSectorContextBkg(context, x, y, width, height);
	this.showSelfSectorContextImg(context, x, y, width, height);
	this.showSectorSubUnitContext(context, x, y, width, height);
	
}
