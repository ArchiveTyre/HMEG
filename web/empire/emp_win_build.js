// emp_win_build.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

EmpWinBuild.prototype = Object.create(EmpWinBase.prototype);
EmpWinBuild.prototype.constructor = EmpWinBuild;





function EmpWinBuild(parentWin)
{
	EmpWinBase.call(this, parentWin); // call super constructor

	// constants
	this.offsetY=0;

	this.sectorSizeX=empWin.mapSectorWidth;
	this.sectorSizeY=empWin.mapSectorHeight;
	this.textOffsetY=(this.sectorSizeY*3)/4;
	
	this.parentWin=parentWin;

	this.orderList=[];
}

EmpWinBuild.prototype.getPossibleOrders=function ()
{
	var i=empWin.mapSelection.unitType;
	if (i in empWin.empDb.unitTypesList.children)
	{
		var t=empWin.empDb.unitTypesList.children[i];
		var o=t.possibleBuilds;
		var arg=hlibSplitString(o);
		return arg;
	}
	return [];
}


EmpWinBuild.prototype.showOrder=function(context, i, orderStr)
{
	var y = this.offsetY + i*this.sectorSizeY;
	context.font = '8pt Calibri';
	context.fillStyle = 'black';
	context.fillText(orderStr, this.sectorSizeX+8, y+this.textOffsetY);

	var l = empWin.empDb.unitTypesList;

    console.log("orderStr "+orderStr+" l "+l.selfToString());

/*
	for (var t in l.children)
	{
		if (typeof t.objName !== 'undefined')
		{
			console.log("t.objName "+t.objName);
			if (t.objName == orderStr)
			{
				if (t instanceof EmpUnitType)
				{
					t.showSelfContextXY(context, this.sectorSizeX+4, y, this.sectorSizeX, this.sectorSizeY);			
				}
			
			}
		}
	}
	*/

	if (l.children!=null)
	{
		var len = l.children.length
		for (var i=0; i<len; ++i)
		{
			if (i in l.children)
			{
				c=l.children[i];
				console.log("c.objName "+c.objName);
				if (c.objName == orderStr)
				{
					if (c instanceof EmpUnitType)
					{
						c.showSelfContextXY(context, 2, y, this.sectorSizeX, this.sectorSizeY);			
					}
				
				}
			}
		}
	}
}

EmpWinBuild.prototype.defineCentralArea=function(subWinSize)
{
	this.sectorSizeX=empWin.mapSectorWidth;
	this.sectorSizeY=empWin.mapSectorHeight;

	var newPage='';

	// The central area of the page	
	newPage+='<div id="buildDiv" style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';
	
	newPage+='What shall be built';
	newPage+='<canvas id="myBuildCanvas" width="'+subWinSize.x+'" height="'+subWinSize.y+'"></canvas>';

	newPage+='</div>';


	
	return newPage;
}

EmpWinBuild.prototype.addEventListeners=function()
{
	this.mapAddEventListenerForMyCanvas("myBuildCanvas");
}

EmpWinBuild.prototype.drawSubWin=function()
{
	var element=document.getElementById("myBuildCanvas");
	var context=element.getContext("2d");


	//console.log("EmpWinBuild: drawSubWin "+parentWin.element.width+" "+parentWin.element.height);


	context.font = '8pt Calibri';
	context.fillStyle = 'black';
	context.fillText("what shall be built", 2, 12);

	
	var arg=this.getPossibleOrders();
	
	element.height=this.offsetY+(arg.length*this.sectorSizeY)+8;
	
	context.fillStyle="#E0E0E0";	
	context.fillRect(0, 0, element.width, element.height);
	
	var c=0;
	for (var i=0; i<arg.length;i++)
	{
		var a=arg[i];
		console.log("showOrder "+i+" "+c+" "+a);
		this.orderList[c]=a;
		this.showOrder(context, c, a);
		c++;
	}

}



EmpWinBuild.prototype.getType=function(y)
{
	var n = Math.floor((y-this.offsetY) / this.sectorSizeY);


	var len = this.orderList.length;
	if ((n>=0) && (n<len))
	{
		if (n in this.orderList) 
		{
			return this.orderList[n];
		}
	}
	return null;
}

EmpWinBuild.prototype.click=function(mouseUpPos)
{
	var x=mouseUpPos.x;
	var y=mouseUpPos.y;
	var t=this.getType(y);
	if (t!=null)
	{
		console.log("build " +t);
		empWin.mapSendOrderOne("build " +t);
		empWin.mapSetShowState(0);
	}
	else
	{
		console.log("EmpWinBuild: try again "+x+" "+y);
	}
}


