// emp_win_sector.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-03-14 by Henrik Bjorkman www.eit.se/hb


EmpWinSectors.prototype = Object.create(EmpWinList.prototype);
EmpWinSectors.prototype.constructor = EmpWinSectors;



function EmpWinSectors(parentWin)
{	
	EmpWinList.call(this, parentWin); // call super constructor

	
}



EmpWinSectors.prototype.defineCentralArea=function(subWinSize)
{
	this.sectorSizeX=empWin.mapSectorWidth;
	this.sectorSizeY=empWin.mapSectorHeight;
	var curHeight=this.sectorSizeY;

	var newPage='';

	// The central area of the page	
	newPage+='<div style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';


	newPage+='<div>';
	newPage+='<canvas id="currentSectorCanvas" width="'+(subWinSize.x-32)+'" height="'+curHeight+'"></canvas>';
	newPage+='</div>';

	newPage+='<div style="text-align:left">';
	newPage+='<input class=empbutton id="upButton" type="button" value="up" onclick="empWin.mapSelection=empWin.mapSelection.parent; empWin.mapSetShowState(0)">';
	newPage+='<input class=empbutton id="moveButton" type="button" value="move" onclick="empWin.mapMoveOrder()">';
	newPage+='<input class=empbutton id="boardButton" type="button" value="go aboard   " onclick="empWin.mapGoAboard()">';
	newPage+='<input class=empbutton id="unselectButton" type="button" value="unselect" onclick="empWin.clearSelectionList(); empWin.mapSetShowState(2)"></br>';
	newPage+='<p>';
	newPage+='</div>';


	newPage+='<div>';	
	newPage+=this.defineCentralAreaList({x:subWinSize.x-32, y:subWinSize.y-curHeight-10-32});
	newPage+='</div>';
	
	newPage+='</div>';
	
	return newPage;
}

EmpWinSectors.prototype.addEventListeners=function()
{
	this.currentSectorElement=document.getElementById("currentSectorCanvas");
	this.currentSectorContext=this.currentSectorElement.getContext("2d");

	this.mapAddEventListenerForMyCanvas("currentSectorCanvas");

	this.addEventListenersList();

	this.scrollOffsetY=0;	
}



EmpWinSectors.prototype.drawSubWin=function()
{
	this.drawCurrentSector();
	this.drawUnitList(this.parentWin.mapSelection);
}


EmpWinSectors.prototype.drawCurrentSector=function()
{
	var element=this.currentSectorElement;
	var context=this.currentSectorContext;

	//context.clearRect(0, 0, element.width, element.height);
	context.fillStyle="#E0E0E0";	
	context.fillRect(0, 0, element.width, element.height);

	if (this.parentWin.mapSelection!=null)
	{
		var str=this.parentWin.mapSelection.selfToString();

		this.parentWin.mapSelection.showSelfSectorContext(context, 0, 0, empWin.mapSectorWidth, empWin.mapSectorHeight);			
						
		context.font = '10pt Calibri';
		context.fillStyle = 'black';
		context.fillText(str, this.sectorSizeX+8, this.textOffsetY);
	}
}




EmpWinSectors.prototype.click=function(mouseUpPos)
{
	console.log("nothing happens when clicking here, for now");	

	/*
	console.log("For now will use this as an 'up' button since one is missing.");
	empWin.mapSelection=empWin.mapSelection.parent;
	empWin.mapSetShowState(0);
	*/	
}

