// emp_base.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-01-01 by Henrik Bjorkman www.eit.se/hb



EmpWinList.prototype = Object.create(EmpWinBase.prototype);
EmpWinList.prototype.constructor = EmpWinList;



function EmpWinList(parentWin)
{	
	//console.log("EmpWinList:");

	EmpWinBase.call(this, parentWin); // call super constructor

	this.textOffsetY=(empWin.mapSectorHeight*3)/4;
	this.scrollOffsetY=0;
	this.myList=[];
	
	this.unitListElement=null;
	this.unitListContext=null;
	this.empObj=null;
}

EmpWinList.prototype.defineCentralAreaList=function(subWinSize)
{
	console.log("EmpWinList.defineCentralAreaList:");
	
	
	var newPage='';
	newPage+='<canvas id="unitsInSectorCanvas" width="'+subWinSize.x+'" height="'+subWinSize.y+'"></canvas>';
	return newPage;
}

EmpWinList.prototype.addEventListenersList=function()
{
	console.log("EmpWinList.addEventListenersList:");

	this.unitListElement=document.getElementById("unitsInSectorCanvas");
	this.unitListContext=this.unitListElement.getContext("2d");
	
	this.unitListElement.addEventListener('mousedown', function (evt) {
		var canvas = document.getElementById("unitsInSectorCanvas");
		empWin.mapMouseDownPos = mapGetMousePos(canvas, evt);
	}, false);

	this.unitListElement.addEventListener('mouseup', function (evt) {
		var canvas = document.getElementById("unitsInSectorCanvas");
		var mouseUpPos = mapGetMousePos(canvas, evt);

		var d=calcDist(mouseUpPos, empWin.mapMouseDownPos);

		if (d<=1.5)
		{
			// regular click
			console.log('EmpWinList.addEventListenersList: regular click: ' + mouseUpPos.x + "," + mouseUpPos.y+" "+d);
			empWin.mapGetStateHandler().clickList(mouseUpPos);
		}
		else
		{
			// drag
			// This is to scroll (move the visible part of) the map, not needed so this shall be removed eventually.
			/*
			console.log('EmpWinList.addEventListenersList: drag:');
			console.log(' mouse down position: ' + empWin.mapMouseDownPos.x + "," + empWin.mapMouseDownPos.y);
			console.log(' mouse up position: ' + mouseUpPos.x + "," + mouseUpPos.y);
			console.log(' dist: ' + d);
			empWin.mapGetStateHandler().dragList(empWin.mapMouseDownPos, mouseUpPos);*/
			
		}

	}, false);
}


EmpWinList.prototype.drawUnitList=function(empObj)
{
	//console.log("EmpWinList.drawUnitList:");

	this.empObj=empObj;

	if (this.unitListElement==null)
	{
		console.log("this.unitListElement==null");
		this.unitListElement=document.getElementById("unitsInSectorCanvas");
		this.unitListContext=this.unitListElement.getContext("2d");
	}


	var element=this.unitListElement;
	var context=this.unitListContext;
	
	this.myList=[];



	if (empObj!=null)
	{
		console.log("EmpWinList.drawUnitList: "+empObj.selfToString());
	
		var n=empObj.getNChildUnits();
		if (n>0)
		{
			var j=0;
			var len = empObj.children.length;
			
			element.height=n*empWin.mapSectorHeight;
			
			context.fillStyle="#E0E0E0";	
			context.fillRect(0, 0, element.width, element.height);
			
			//console.log("EmpWinList.drawUnitList, n="+n+", len="+len);
			for (var i=0; i<len; ++i) 
			{
				if (i in empObj.children) 
				{
					var c = empObj.children[i];
					var str=c.selfToString();
					//var str=c.index+" "+c.objName;
					
					var y=j*empWin.mapSectorHeight-this.scrollOffsetY;

					if (this.parentWin.isSelected(c.id))
					{
						this.showTickBoxXY(context, 0, y, empWin.mapSectorWidth, empWin.mapSectorHeight, "selected");
					}
					else
					{
						this.showTickBoxXY(context, 0, y, empWin.mapSectorWidth, empWin.mapSectorHeight, "box");					
					}

					if (c instanceof EmpUnit)
					{
						c.showSelfUnitContextXY(context, empWin.mapSectorWidth+4, y, empWin.mapSectorWidth, empWin.mapSectorHeight);			
					}
					
					
					context.font = '10pt Calibri';
					context.fillStyle = 'black';
					context.fillText(str, empWin.mapSectorWidth*2+8, y+this.textOffsetY);

					this.myList[j]=""+c.id;
					//console.log("EmpWinList.drawUnitList "+i+" "+j+" "+this.myList[j]);
					j++;
				}
			}

			this.myList.forEach(function(entry) {
			    console.log(" forEach "+entry);
			})

		}
		else
		{
			var str="empty";
			context.font = '10pt Calibri';
			context.fillStyle = 'black';
			context.fillText(str, 40, this.textOffsetY);
	
		}
	}
	else
	{
		var str="nothing selected";
		context.font = '10pt Calibri';
		context.fillStyle = 'black';
		context.fillText(str, 40, this.textOffsetY);

	}



	// show selected unit/sector etc
	this.parentWin.empWinMenu.mapUpdateUpperTextAreas();
}



EmpWinList.prototype.getClickedUnitIdList=function(y)
{
	console.log("EmpWinList.getClickedUnitIdList:");

	var n = Math.floor((y+this.scrollOffsetY) / empWin.mapSectorHeight);
	var len = this.myList.length;
	
	console.log("EmpWinList.getClickedUnitIdList "+y+" "+empWin.mapSectorHeight+" "+n+" "+len+" ");

	if (this.empObj!=null)
	{
		console.log("EmpWinList.getClickedUnitIdList: id=#"+this.empObj.id);
	}

	/*
	this.myList.forEach(function(entry) {
	    console.log(" forEach "+entry);
	})
	*/

	if ((n>=0) && (n<len))
	{
		if (n in this.myList) 
		{
			return this.myList[n];
		}
	}
	return null;
}

EmpWinList.prototype.clickList=function(mouseUpPos)
{
	console.log("EmpWinList.clickList:");

	var x=mouseUpPos.x;
	var y=mouseUpPos.y;
	var id=this.getClickedUnitIdList(y);
	if (id!=null)
	{
		// Was it a click in select box or on unit itself
		if (x<=empWin.mapSectorWidth+2)
		{
		    // it was select box
			console.log("selectedUnit ~" +id);
			this.parentWin.toggleSelection(id);
			this.drawSubWin();
		}
		else
		{
		    // it was on the unit itself		
			console.log("clickedUnit ~" +id);
			var c=this.parentWin.empDb.getById(id);
	
			this.parentWin.mapSelection=c;
			this.parentWin.empWinMenu.mapUpdateUpperTextAreas();
			if(c!=null)
			{
				console.log("c "+c.selfToString());;
			}
			
			
			empWin.mapSetShowState(2);
			this.myList=[];
	
			//	empWin.mapUpdateUpperTextArea();
			//	this.drawSubWin();
		}
	}
	else
	{
		console.log("EmpWinSectors.click: try again "+x+" "+y);
		this.parentWin.mapSelection=this.parentWin.mapSelection.parent;
		empWin.mapSetShowState(2);
	}
}





EmpWinList.prototype.dragList=function(mouseDownPos, mouseUpPos)
{
	console.log("EmpWinList.dragList:");

    var mouseDrag=this.mouseDiff(mouseDownPos, mouseUpPos);
	
	this.scrollOffsetY-=mouseDrag.y;
	
	if (this.scrollOffsetY<0)
	{
		this.scrollOffsetY=0;
	}

	console.log("drag "+mouseDrag.x+" "+mouseDrag.y+" "+this.scrollOffsetY);
	
	this.drawUnitList(this.empObj);
}


EmpWinList.prototype.showTickBoxXY=function(context, x, y, width, height, imageName)
{
	//var imageName=name+".png";


	var imageObj = new Image();
	imageObj.onload = function() {
		context.drawImage(imageObj,x, y-Math.round((width-height)/2), width, width); // Images are 64x64 but map on the rows are 56px high, so using width as height. Images shall have some extra transparency in upper and lower instead.
	};
	imageObj.onerror = function() {
		context.font = '8pt Calibri';
		context.fillStyle = 'black';
		context.fillText(imageName, x, y+8);
	};

	imageObj.src = imageName+".png";
}

