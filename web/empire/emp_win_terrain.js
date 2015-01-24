// emp_win_terrain.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-28 by Henrik Bjorkman www.eit.se/hb




EmpWinTerrain.prototype = Object.create(EmpWinBase.prototype);
EmpWinTerrain.prototype.constructor = EmpWinTerrain;

function EmpWinTerrain(parentWin)
{	
	EmpWinBase.call(this, parentWin); // call super constructor

	this.scrollOffsetX=null;
	this.scrollOffsetY=null;

	this.mapSizeX=null;
	this.mapSizeY=null;
	
	this.scrollTopRemembered=null;
	this.scrollLeftRemembered=null;
}


EmpWinTerrain.prototype.defineCentralArea=function(subWinSize)
{
    // http://stackoverflow.com/questions/9798331/zoom-a-browser-window-view-with-javascript
	/*$(document).ready(function(){
	  $('body').css('zoom','100%'); // Webkit browsers
	  $('body').css('zoom','1.0'); // Other non-webkit browsers
	  $('body').css('-moz-transform',scale(1.0, 1.0)); // Moz-browsers
	});*/

	this.setScrollToHome();

	var x=this.mapSizeX;
	if (x>subWinSize.x)
	{
		x=subWinSize.x;
		//console.log("EmpWinTerrain.defineCentralArea: x="+x); 
	}
	

	var y=this.mapSizeY;
	if (y>subWinSize.y)
	{
		y=subWinSize.y;
		//console.log("EmpWinTerrain.defineCentralArea: y="+y); 
	}

	var canvasXSize=Math.round(this.mapSizeX+empWin.mapSectorWidth/2);

	// Set scroll to so that the canvas is centered. By default it would be in upper left.
	if ((this.mapSizeY!=null) && (this.scrollTopRemembered==null))
	{
		this.scrollTopRemembered=Math.round(this.mapSizeY/2-subWinSize.y/2);
		this.scrollLeftRemembered=Math.round(canvasXSize/2-subWinSize.x/2);

		//console.log("canvasXSize "+canvasXSize+", scrollLeftRemembered "+this.scrollLeftRemembered+ ", subWinSize.x " +subWinSize.x);
		//console.log("this.mapSizeY "+this.mapSizeY+", scrollTopRemembered "+this.scrollTopRemembered+ ", subWinSize.y " +subWinSize.y);

		if (this.scrollTopRemembered<0)
		{
			this.scrollTopRemembered=0;
		}
		if (this.scrollLeftRemembered<0)
		{
			this.scrollLeftRemembered=0;
		}
	}


	// The central area of the page	
	var newPage='';
	newPage+='<div id="terrainDiv" style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';
	newPage+='<canvas id="myCanvas" width="'+canvasXSize+'px" height="'+this.mapSizeY+'px"></canvas>';
	newPage+='</div>';

	return newPage;		
}

EmpWinTerrain.prototype.addEventListeners=function()
{
	if (this.scrollTopRemembered!=null)
	{
		$('#terrainDiv').scrollTop(this.scrollTopRemembered);
		$('#terrainDiv').scrollLeft(this.scrollLeftRemembered);
	}

    this.element=document.getElementById("myCanvas");
	this.context=this.element.getContext("2d");


	this.mapAddEventListenerForMyCanvas("myCanvas");
}

EmpWinTerrain.prototype.showWorldMapSectors=function(context, t, step)
{
		var cl = t.children;
		if (cl!=null)
		{
			var i;
			for(i in cl)
			{
				if (cl[i] instanceof EmpSector)
				{
					var s=cl[i];
					//console.log("EmpWinTerrain.prototype.drawSubWin "+s.index+" "+s.selfToString());
					
					var c=t.translateSectorIndexToColumn(i);
					var r=t.translateSectorIndexToRow(i);			
					var offset=empWin.mapCalcOffSet(r);
					var canvasXY=this.fromMapXYToCanvas({x: offset+c*empWin.mapSectorWidth, y: r*empWin.mapSectorHeight}); 
		
		
					s.showSelfSectorContextBkg(context, canvasXY.x, canvasXY.y, empWin.mapSectorWidth, empWin.mapSectorHeight);
					s.showSelfSectorContextImg(context, canvasXY.x, canvasXY.y, empWin.mapSectorWidth, empWin.mapSectorHeight);
					s.showSectorSubUnitContext(context, canvasXY.x, canvasXY.y, empWin.mapSectorWidth, empWin.mapSectorHeight);
				}
			}
		}
}

EmpWinTerrain.prototype.drawSubWin=function()
{
	if (empWin.empDb!=null)
	{	
		var w = empWin.empDb.getEmpireWorld();
		
		if (w!=null)
		{
			// get the size of our image/map
			var t=w.getEmpireTerrain();
			
			this.mapSizeX=empWin.mapSectorWidth * t.sizeX;
			this.mapSizeY=empWin.mapSectorHeight * t.sizeY;
			var canvasXSize=Math.round(this.mapSizeX+empWin.mapSectorWidth/2);

			// flickering fix, see: http://stackoverflow.com/questions/2795269/does-html5-canvas-support-double-buffering
			var canvas = document.createElement('canvas');
			canvas.width = canvasXSize;
			canvas.height = this.mapSizeY;
			var context = canvas.getContext('2d');

			// show world map sectors
			this.showWorldMapSectors(context, t, 0);
			
			var element=this.element;
			this.context.fillStyle="#E0E0E0";				
			this.context.fillRect(0, 0, element.width, element.height);
			this.context.drawImage(canvas, 0, 0);
		}		
	}
}


// TODO: translating mouse position to canvas does not work on android-chrome. Perhaps this is a hint:  http://stackoverflow.com/questions/5189968/zoom-canvas-to-mouse-cursor
EmpWinTerrain.prototype.click=function(mouseUpPos)
{
	mapPos = this.fromCanvasToMapXY(mouseUpPos);

	var upSectorPos = empWin.empDb.terrain.fromXYToPos(mapPos);
	
	// a click in another sector
	if ((empWin.mapOrder!=null) && (empWin.nSelected()>0))
	{
		// The user have started to enter a "moveTo" order
		
		// all selected units are to be ordered to move to the sector that was clicked
		for(i in empWin.selectionList)
		{						
			var str='unitOrder '+ i + ' "'+ empWin.mapOrder+ ' ' + upSectorPos+'"';
			console.log(str);
			doSend(str);
		}
		
		empWin.clearSelectionList();
		empWin.mapOrder=null;
	}
	else
	{
		// No order is being entered, show the user the content of the sector clicked in.
		
		this.scrollTopRemembered=$('#terrainDiv').scrollTop();
		this.scrollLeftRemembered=$('#terrainDiv').scrollLeft();
		
		empWin.mapSelection=empWin.empDb.getSector(upSectorPos);
		empWin.mapSetShowState(2);
	}
	
		
	// show selected unit/sector etc
	this.parentWin.empWinMenu.mapUpdateUpperTextAreas();
	
}


// We need a drag mouse function to scroll around if world is bigger than what we can show.
// http://www.w3schools.com/tags/canvas_clip.asp


EmpWinTerrain.prototype.drag=function(mouseDownPos, mouseUpPos)
{
    var mouseDrag=this.mouseDiff(mouseDownPos, mouseUpPos);
	
	this.scrollOffsetX-=mouseDrag.x;
	this.scrollOffsetY-=mouseDrag.y;
	
	/*
	if (this.scrollOffsetX<0)
	{
		this.scrollOffsetX+=this.mapSizeX;
	}
	
	if (this.scrollOffsetY<0)
	{
		this.scrollOffsetY+=this.mapSizeY;
	}

	if (this.scrollOffsetX>=this.mapSizeX)
	{
		this.scrollOffsetX-=this.mapSizeX;
	}

	if (this.scrollOffsetY>=this.mapSizeY)
	{
		this.scrollOffsetY-=this.mapSizeY;
	}
*/


	console.log("drag "+mouseDrag.x+" "+mouseDrag.y+"  "+this.scrollOffsetX+" "+this.scrollOffsetY);
	
	this.drawSubWin();
}

EmpWinTerrain.prototype.fromCanvasToMapXY=function(canvasPos)
{
	var x = canvasPos.x+this.scrollOffsetX;
	var y = canvasPos.y+this.scrollOffsetY;

	while (x<0)
	{
		x+=this.mapSizeX;
	}
	
	while (y<0)
	{
		y+=this.mapSizeY;
	}

/*
	canvasXSize=Math.round(this.mapSizeX+empWin.mapSectorWidth/2);
	while (x>=canvasXSize)
	{
		x-=this.mapSizeX;
	}
*/

	while (x>=this.mapSizeX)
	{
		x-=this.mapSizeX;
	}


	while (y>=this.mapSizeY)
	{
		y-=this.mapSizeY;
	}

	return {x: x, y: y};
}

EmpWinTerrain.prototype.fromMapXYToCanvas=function(mapPos)
{


	var x = mapPos.x-this.scrollOffsetX;
	var y = mapPos.y-this.scrollOffsetY;

	//console.log("EmpWinTerrain.fromMapXYToCanvas "+mapPos.x+" "+this.scrollOffsetX+" "+x+" "+this.mapSizeX);

	while (x<0)
	{
		x+=this.mapSizeX;
	}
	
	while (y<0)
	{
		y+=this.mapSizeY;
	}

	while (x>=this.mapSizeX)
	{
		x-=this.mapSizeX;
	}

	while (y>=this.mapSizeY)
	{
		y-=this.mapSizeY;
	}

	return {x: x, y: y};
}

EmpWinTerrain.prototype.center=function()
{
	this.scrollOffsetX=null;
	this.scrollOffsetY=null;
	this.setScrollToHome();
	this.drawSubWin();	
}


EmpWinTerrain.prototype.setScrollToHome=function()
{
	if (empWin!=null)
	{
		var db=empWin.empDb;
		if (db!=null)
		{
			var w = db.getEmpireWorld();
			
			if (w!=null)
			{
				var t=w.getEmpireTerrain();
		
				this.mapSizeX=empWin.mapSectorWidth * t.sizeX;
				this.mapSizeY=empWin.mapSectorHeight * t.sizeY;

				// Figure out how to set the offset so that the players home town gets in the middle of the canvas.
				if (this.scrollOffsetX==null)
				{
					var es = w.getState();
					var homeSectorId = es.homeSectorId;
					var homeSector = db.getById(homeSectorId);
					var homeSectorPos = homeSector.index;
					var c=t.translateSectorIndexToColumn(homeSectorPos);
					var r=t.translateSectorIndexToRow(homeSectorPos);
					var playerCenterX=Math.round(c*empWin.mapSectorWidth);							
					var playerCenterY=Math.round(r*empWin.mapSectorHeight);							
					var mapCenterX=Math.round(this.mapSizeX/2);							
					var mapCenterY=Math.round(this.mapSizeY/2);							
					this.scrollOffsetX=-(mapCenterX-playerCenterX);
					this.scrollOffsetY=-(mapCenterY-playerCenterY);
					console.log("homeSectorId "+homeSectorId+", homeSectorPos " + homeSectorPos+ " c " + c + " r " + r);
					console.log("playerCenterX "+playerCenterX+", playerCenterY " + playerCenterY+ " mapCenterX " + mapCenterX + " mapCenterY " + mapCenterY);
					console.log("scrollOffsetX "+this.scrollOffsetX+ " scrollOffsetY " +this.scrollOffsetY);
				}
			}
		}		
	}	
				
}

