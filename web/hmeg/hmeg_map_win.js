// hmeg_map_win.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-28 by Henrik Bjorkman www.eit.se/hb




HmegMapWin.prototype = Object.create(DivBase.prototype);
HmegMapWin.prototype.constructor = HmegMapWin;

function HmegMapWin(parentWin)
{	
	DivBase.call(this, parentWin); // call super constructor

	this.scrollOffsetX=null; // in pixels
	this.scrollOffsetY=null;

	this.mapSizeX=null; // in pixels
	this.mapSizeY=null;
	
	this.scrollTopRemembered=null; // in pixels
	this.scrollLeftRemembered=null;
	
	this.parentWin = parentWin;
	this.currentRoom = null;
}


// This is called when this div shall be added to the page.
// NOTE this only sets up the page. When it is time to draw the div drawDiv is called.
HmegMapWin.prototype.defineDiv=function(divSize)
{
	this.initRoom();

	//this.setScrollToHome();

	var x=this.mapSizeX;
	if (x>divSize.x)
	{
		x=divSize.x;
		//console.log("HmegMapWin.defineDiv: x="+x); 
	}
	

	var y=this.mapSizeY;
	if (y>divSize.y)
	{
		y=divSize.y;
		//console.log("HmegMapWin.defineDiv: y="+y); 
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

// After all "div" have been setup (after defineDiv but before drawDiv) this is called so that each div can register and remember its elements etc.
HmegMapWin.prototype.addEventListenersDiv=function()
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




HmegMapWin.prototype.showWorldMapSectors=function(context, hmegRoom)
{
	//console.log("showWorldMapSectors "+hmegRoom.xSectors+ " "+hmegRoom.ySectors+ "  "+this.sectorWidth+" "+this.sectorHeight);



	// This is for regular blocks of the current room
	// Loop x & y coordinates
	for (var x=0; x<hmegRoom.xSectors; x++)
	{
		
		for (var y=0; y<hmegRoom.ySectors; y++)
		{
			var  r= hmegRoom.map[x][y]; 
			if (r!=0)
			{			
				
				var xy=this.translateBlockToCanvas(x,y);	
			
				// This is for debugging, remove later
				//context.fillStyle="#0080F0";				
				//context.fillRect(xy.x, xy.y, this.sectorWidth, this.sectorHeight);


				var id = r & 0xFF;
				r >>= 8;
				var damage = r & 0xFFFF;
				r >>= 16;
				var rotate = r & 0x3;
				r >>= 2;
				var argu = 0;
				console.log(id+" Derp"+ rotate);
				var block = hmegRoom.img[id+"_"+rotate+"_"+argu];
				if (block === undefined)
				{
					context.fillStyle="#4040A0";				
					context.fillRect(xy.x, xy.y, this.sectorWidth, this.sectorHeight);
					
				}
				else
				{
					block.showSelfContextXY(context ,xy.x, xy.y, this.sectorWidth, this.sectorHeight);
				}
				//TODO Add damage over_layer.
				
			}
			
			
		}
		
	}


	// This is for entities residing in the current room
	// if any loop all children
	var cl = hmegRoom.children;
	if (cl!=null)
	{
		var i;
		for(i in cl)
		{
			if (cl[i] instanceof HmegEntity)
			{
				var s=cl[i];
				//console.log("HmegMapWin.prototype.drawDiv "+s.index+" "+s.selfToString());
				
				//var px=s.x*this.sectorWidth; // TODO use a translate function.
				//var py=s.y*this.sectorHeight;		
				
				var xy=this.translateBlockToCanvas(s.x, s.y);	


				// is it a room?
				if (s instanceof HmegRoom)
				{
					// Rooms will have one image that is to be drawn to represent the room (even if the room contains other objects) 
					context.fillStyle="#c0c0c0";				
					context.fillRect(xy.x, xy.y, this.sectorWidth*s.outerX, this.sectorHeight*s.outerY);				

					var canvas2 = document.createElement('canvas');
					var context2 = canvas2.getContext('2d');

					canvas2.width = this.sectorWidth*s.xSectors;
					canvas2.height = this.sectorHeight*s.ySectors;

					this.showWorldMapSectors(context2, s);
					
					context.drawImage(canvas2, xy.x, xy.y, this.sectorWidth*s.outerX, this.sectorHeight*s.outerY);

				}
				
				// This is for debugging, remove later				
				//context.fillStyle="#80F000";				
				//context.fillRect(xy.x, xy.y, this.sectorWidth, this.sectorHeight);
				
				s.showSelfContextXY(context ,xy.x, xy.y, this.sectorWidth, this.sectorHeight);



			}
		}
	}
	
}

HmegMapWin.prototype.initRoom=function()
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
			this.playerAvatar = d.getById(p.avatarId);
			this.currentRoom = this.playerAvatar.parent;
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


// This is called when this div shall be drawn (or redrawn)
HmegMapWin.prototype.drawDiv=function()
{
	//console.log("drawDiv begin");
	
	this.initRoom();

	if (this.currentRoom!=null)
	{
		this.mapSizeX=this.sectorWidth * this.currentRoom.xSectors;
		this.mapSizeY=this.sectorHeight * this.currentRoom.ySectors;
		var canvasXSize=Math.round(this.mapSizeX);
		var canvasYSize=Math.round(this.mapSizeY);

		var element=this.element;
		element.width = canvasXSize;
		element.height = canvasYSize;
		this.context.fillStyle="#e0e0e0";				
		this.context.fillRect(0, 0, element.width, element.height);

		// show world map sectors
			
		var showRoom=this.currentRoom;
		
		var a=this.playerAvatar;
		if (a!=null)
		{
			console.log("a "+ a.x + " "+ a.y);

			var p = showRoom.getParent(); 
			// if in control room then 
			if (p instanceof HmegRoom)
			{
				if (showRoom.isControlPanel(a.x,a.y))
				{
					console.log("control panel");
					a=showRoom;
					showRoom=p;
				}
			}
		}
		
		this.showWorldMapSectors(this.context, showRoom);
		
	}
	else
	{
		console.log("no current room");
	}		

	//console.log("drawDiv done");
}


// TODO: translating mouse position to canvas does not work on android-chrome. Perhaps this is a hint:  http://stackoverflow.com/questions/5189968/zoom-canvas-to-mouse-cursor
HmegMapWin.prototype.click=function(mouseUpPos)
{
	mapPos = this.translateCanvasToBlock(mouseUpPos);


	console.log("click " + mapPos.x +" "+mapPos.y);
	
		
    websocket.send('mapClick '+mapPos.x+' '+mapPos.y);
	
}


// We need a drag mouse function to scroll around if world is bigger than what we can show.
// http://www.w3schools.com/tags/canvas_clip.asp


HmegMapWin.prototype.drag=function(mouseDownPos, mouseUpPos)
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


HmegMapWin.prototype.translateBlockToCanvas=function(x,y)
{
	var px=x*this.sectorWidth;
	var py=y*this.sectorHeight;
	return {x: px, y: py};
}

HmegMapWin.prototype.translateCanvasToBlock=function(xy)
{
	var bx = Math.floor( xy.x / (this.sectorWidth)) ;
	var by = Math.floor( xy.y / (this.sectorHeight)) ;

	//var i = bx + this.ScreenSizeX*by ; 
	//return i;
	return {x: bx, y: by};
}


/*
HmegMapWin.prototype.fromCanvasToMapXY=function(canvasPos)
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

HmegMapWin.prototype.fromMapXYToCanvas=function(mapPos)
{


	var x = mapPos.x-this.scrollOffsetX;
	var y = mapPos.y-this.scrollOffsetY;

	//console.log("HmegMapWin.fromMapXYToCanvas "+mapPos.x+" "+this.scrollOffsetX+" "+x+" "+this.mapSizeX);

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


HmegMapWin.prototype.center=function()
{
	this.scrollOffsetX=null;
	this.scrollOffsetY=null;
	this.setScrollToHome();
	this.drawDiv();	
}


HmegMapWin.prototype.setScrollToHome=function()
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

