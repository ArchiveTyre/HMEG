// hmeg_map_win.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-28 by Henrik Bjorkman www.eit.se/hb




HmegWinInventory.prototype = Object.create(DivBase.prototype);
HmegWinInventory.prototype.constructor = HmegWinInventory;

function HmegWinInventory(parentWin)
{	
	DivBase.call(this, parentWin); // call super constructor

	this.ScreenSizeX = 10;

	this.scrollOffsetX=null; // in pixels
	this.scrollOffsetY=null;

	this.mapSizeX=null; // in pixels
	this.mapSizeY=null;
	
	this.scrollTopRemembered=null; // in pixels
	this.scrollLeftRemembered=null;
	
	this.parentWin = parentWin;
	this.currentRoom = null;
}


HmegWinInventory.prototype.defineDiv=function(divSize)
{
	this.initRoom();

	//this.setScrollToHome();

	var x=this.mapSizeX;
	if (x>divSize.x)
	{
		x=divSize.x;
		//console.log("HmegWinInventory.defineDiv: x="+x); 
	}
	

	var y=this.mapSizeY;
	if (y>divSize.y)
	{
		y=divSize.y;
		//console.log("HmegWinInventory.defineDiv: y="+y); 
	}

	var canvasXSize=Math.round(this.mapSizeX);

	// Set scroll to so that the canvas is centered. By default it would be in upper left.
	if ((this.mapSizeY!=null) && (this.scrollTopRemembered==null))
	{
		this.scrollTopRemembered=Math.round(this.mapSizeY/2-divSize.y/2);
		this.scrollLeftRemembered=Math.round(canvasXSize/2-divSize.x/2);

		//console.log("canvasXSize "+canvasXSize+", scrollLeftRemembered "+this.scrollLeftRemembered+ ", divSize.x " +divSize.x);
		//console.log("this.mapSizeY "+this.mapSizeY+", scrollTopRemembered "+this.scrollTopRemembered+ ", divSize.y " +divSize.y);

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
	newPage+='<div id="terrainDiv" style="width:'+divSize.x+'px; height:'+divSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';
	newPage+='<canvas id="mapCanvas" width="'+canvasXSize+'px" height="'+this.mapSizeY+'px"></canvas>';
	newPage+='</div>';

	return newPage;		
}

HmegWinInventory.prototype.addEventListenersDiv=function()
{
	console.log("addEventListenersDiv");
	
	if (this.scrollTopRemembered!=null)
	{
		$('#terrainDiv').scrollTop(this.scrollTopRemembered);
		$('#terrainDiv').scrollLeft(this.scrollLeftRemembered);
	}

	this.element=document.getElementById("mapCanvas");
	this.context=this.element.getContext("2d");

	DivBase.prototype.addEventListenersDiv.call(this, "mapCanvas");

}




HmegWinInventory.prototype.showWorldMapSectors=function(context, hmegRoom)
{
	//console.log("showWorldMapSectors "+ hmegRoom.getNChildObjects());




	// This is for entities
	
	var cl = hmegRoom.children;
	if (cl!=null)
	{
		var i;
		for(i in cl)
		{
			if (cl[i] instanceof HmegEntity)
			{
				var s=cl[i];		
				
				
				var xy = this.translateBlockToCanvas(i);
						
				//console.log("showWorldMapSectors "+i+" "+s.index+" "+s.selfToString()+ " "+xy.x+" "+xy.y);

				// Remove these tow lines later
				context.fillStyle="#d0c0d0";				
				context.fillRect(xy.x, xy.y, this.sectorWidth, this.sectorHeight);
				
				s.showSelfContextXY(context ,xy.x, xy.y, this.sectorWidth, this.sectorHeight);


				context.font = '8pt Calibri';
				context.fillStyle = 'black';
				context.fillText(""+s.stack ,xy.x , xy.y+16);

			}
		}
	}

}



HmegWinInventory.prototype.translateBlockToCanvas=function(i)
{
	var px = (i % this.ScreenSizeX) * (this.sectorWidth+4) ;
	var py = (Math.floor(i/this.ScreenSizeX)) * (this.sectorHeight+4);
	return {x: px, y: py};
}

HmegWinInventory.prototype.translateCanvasToBlock=function(xy)
{
	var x = Math.floor( xy.x / (this.sectorWidth+4)) ;
	var y = Math.floor( xy.y / (this.sectorHeight+4)) ;

	var i = x + this.ScreenSizeX*y ; 

	return i;
}




HmegWinInventory.prototype.initRoom=function()
{
	//console.log("init begin");

	var p = this.parentWin;
	var d = p.hmegDb;

	this.sectorWidth=p.sectorWidth;
	this.sectorHeight=p.sectorHeight;

	if (d!=null)
	{	
		var w = d.rootObj;
		if (w!=null)
		{
		
			//this.currentRoom = w.findSubObjectByName("spawnRoom");  // TODO: This shall be the room that is parent of avatar.
			//this.currentRoom = d.getById(p.avatarId).parent;
			this.currentRoom = d.getById(p.avatarId);
			if (this.currentRoom !=null)
			{
				this.mapSizeX=this.sectorWidth * this.currentRoom.xSectors;
				this.mapSizeY=this.sectorHeight * this.currentRoom.ySectors;
			}
			else
			{
				console.log("no current room");
			}		
		}
		else
		{
			console.log("no world");
		}		
	}
	else
	{
		console.log("no hmegDb");
	}		
	//console.log("init done");

}


HmegWinInventory.prototype.drawDiv=function()
{
	//console.log("drawDiv begin");
	
	this.initRoom();

	if (this.currentRoom!=null)
	{
		//console.log("drawDiv: current room " +this.currentRoom.objName + " "+ this.currentRoom.getNChildObjects());
	
	
		this.mapSizeX=this.sectorWidth * 16;
		this.mapSizeY=this.sectorHeight * 16;
		var canvasXSize=Math.round(this.mapSizeX);
		var canvasYSize=Math.round(this.mapSizeY);

		var element=this.element;
		element.width = canvasXSize;
		element.height = canvasYSize;
		this.context.fillStyle="#E0E0E0";				
		this.context.fillRect(0, 0, element.width, element.height);

		// show world map sectors
		this.showWorldMapSectors(this.context, this.currentRoom);
		
	}
	else
	{
		console.log("no current room");
	}		

	//console.log("drawDiv done");
}


// TODO: translating mouse position to canvas does not work on android-chrome. Perhaps this is a hint:  http://stackoverflow.com/questions/5189968/zoom-canvas-to-mouse-cursor
HmegWinInventory.prototype.click=function(mouseUpPos)
{
	var upSectorPos = this.translateCanvasToBlock(mouseUpPos);

	console.log("click " + upSectorPos);
	
		
    websocket.send('inventoryClick "'+upSectorPos+'"');

		
	
}


// We need a drag mouse function to scroll around if world is bigger than what we can show.
// http://www.w3schools.com/tags/canvas_clip.asp


HmegWinInventory.prototype.drag=function(mouseDownPos, mouseUpPos)
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
	
	this.drawDiv();
}
/*
HmegWinInventory.prototype.fromCanvasToMapXY=function(canvasPos)
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

HmegWinInventory.prototype.fromMapXYToCanvas=function(mapPos)
{


	var x = mapPos.x-this.scrollOffsetX;
	var y = mapPos.y-this.scrollOffsetY;

	//console.log("HmegWinInventory.fromMapXYToCanvas "+mapPos.x+" "+this.scrollOffsetX+" "+x+" "+this.mapSizeX);

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
*/

HmegWinInventory.prototype.center=function()
{
	this.scrollOffsetX=null;
	this.scrollOffsetY=null;
	this.setScrollToHome();
	this.drawDiv();	
}


HmegWinInventory.prototype.setScrollToHome=function()
{
	if (rootDiv!=null)
	{
		var db=rootDiv.empDb;
		if (db!=null)
		{
			var w = db.getEmpireWorld();
			
			if (w!=null)
			{
				var t=w.getEmpireTerrain();

				if (t!=null)
				{		
					this.mapSizeX=rootDiv.mapSectorWidth * t.sizeX;
					this.mapSizeY=rootDiv.mapSectorHeight * t.sizeY;
	
					// Figure out how to set the offset so that the players home town gets in the middle of the canvas.
					if (this.scrollOffsetX==null)
					{
						var es = w.getState();
						if (es!=null)
						{
							var homeSectorId = es.homeSectorId;
							var homeSector = db.getById(homeSectorId);
							var homeSectorPos = homeSector.index;
							var c=t.translateSectorIndexToColumn(homeSectorPos);
							var r=t.translateSectorIndexToRow(homeSectorPos);
							var playerCenterX=Math.round(c*rootDiv.mapSectorWidth);							
							var playerCenterY=Math.round(r*rootDiv.mapSectorHeight);							
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
	}	
}

