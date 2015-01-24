// emp_win_unit.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

EmpWinUnit.prototype = Object.create(EmpWinList.prototype);
EmpWinUnit.prototype.constructor = EmpWinUnit;





function EmpWinUnit(parentWin)
{
	//console.log("EmpWinUnit:");

	EmpWinList.call(this, parentWin); // call super constructor

	this.offsetX=2;
	this.sectorSizeX=72;
	this.unitOffsetY=55;
	this.stepY=10;
	this.font='8pt Calibri';
	this.sizeX=64;
	this.sizeY=64;

	this.orderList=[];
}


EmpWinUnit.prototype.showPossibleOrder=function(context, i, order)
{
	console.log("EmpWinUnit.showPossibleOrder: "+i+" "+order);

	var imageObjMove = new Image();
	var x=this.offsetX+i*this.orderSizeX;
	var y=0;
	var w=this.orderSizeX;
	var h=this.orderSizeY;
	var fileName=order+".png"
	imageObjMove.onload = function() {
		context.drawImage(imageObjMove, x, y, w, h);

	};
	imageObjMove.onerror = function() {
		context.font = '8pt Calibri';
		context.fillStyle = 'black';
		context.fillText(order, x, y+32/2);
	};
	imageObjMove.src = fileName;
}

EmpWinUnit.prototype.getPossibleOrders=function()
{
	console.log("EmpWinUnit.getPossibleOrders:");

	var i=empWin.mapSelection.unitType;
	if (i in empWin.empDb.unitTypesList.children)
	{
		var t=empWin.empDb.unitTypesList.children[i];
		var o=t.possibleOrders;
		var arg=hlibSplitString(o);
		return arg;
	}
	return [];
}

EmpWinUnit.prototype.getOrder=function(x)
{
	console.log("EmpWinUnit.getOrder:");

	var m = (x-this.offsetX) / this.orderSizeX;
	var n = Math.floor(m);
	console.log("getOrder "+n+" "+m);
	var order = null;
	if ((n>=0) && (n<this.orderList.length))
	{
		order=this.orderList[n];
	}
	return order;
}


EmpWinUnit.prototype.defineCentralArea=function(subWinSize)
{
	console.log("EmpWinUnit.defineCentralArea:");
	
	this.sectorSizeX=empWin.mapSectorWidth;
	this.sectorSizeY=empWin.mapSectorHeight;
	

	this.orderSizeX=empWin.mapSectorWidth*2;
	this.orderSizeY=empWin.mapSectorWidth*2;
	
	var arg=this.getPossibleOrders();
	var orderTotX = this.orderSizeX* arg.length;
	if (orderTotX > subWinSize.x)
	{
		// All orders do not fit on page. Scale down with some factor.
		var f=subWinSize.x/orderTotX;
		this.orderSizeX*=f;
		this.orderSizeY*=f;
	}


	var curHeight=this.sectorSizeY+this.orderSizeY;

	var newPage='';
	newPage+='<div style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';

	newPage+='<div>';
	newPage+='<canvas id="myUnitCanvas" width="'+(subWinSize.x-32)+'" height="'+this.sectorSizeY+'"></canvas>';
	newPage+='<canvas id="myOrderCanvas" width="'+(subWinSize.x-32)+'" height="'+this.orderSizeY+'"></canvas>';
	newPage+='</div>';

	newPage+='<div style="text-align:left">';
	newPage+='<input class=empbutton id="upButton" type="button" value="up" onclick="empWin.mapSelection=empWin.mapSelection.parent; empWin.mapSetShowState(2)">';
	newPage+='<input class=empbutton id="moveButton" type="button" value="move" onclick="empWin.mapMoveOrder()">';
	newPage+='<input class=empbutton id="boardButton" type="button" value="go aboard   " onclick="empWin.mapGoAboard()">';
	newPage+='<input class=empbutton id="unselectButton" type="button" value="unselect" onclick="empWin.clearSelectionList(); empWin.mapSetShowState(2)"></br>';
	newPage+='<p>';
	newPage+='</div>';

	newPage+='<div>';
	newPage+=this.defineCentralAreaList({x:subWinSize.x-32, y:subWinSize.y-curHeight-20-32});
	newPage+='</div>';

	newPage+='</div>';

	return newPage;
}

EmpWinUnit.prototype.addEventListeners=function()
{
	console.log("EmpWinUnit.addEventListeners:");

	this.mapAddEventListenerForMyCanvas("myOrderCanvas");
	this.addEventListenersList();
}


EmpWinUnit.prototype.drawSubWin=function()
{
	console.log("EmpWinUnit.drawSubWin:");


	this.drawUnitList(empWin.mapSelection);

	{
		var element=document.getElementById("myUnitCanvas");
		var context=element.getContext("2d");
	
		context.fillStyle="#E0E0E0";	
		context.fillRect(0, 0, element.width, element.height);
	
		this.orderList=[];
	
		var u=empWin.mapSelection;
	
		if (u!=null)
		{
	
			var str=u.selfToString();
	
	
			var str=u.selfToString();
			u.showSelfUnitContextXY(context, 0, 0, empWin.mapSectorWidth, empWin.mapSectorHeight);			
							
			context.font = '10pt Calibri';
			context.fillStyle = 'black';
			context.fillText(str, this.sectorSizeX+8, this.textOffsetY);
		}
	}
	
	if (u!=null)
	{
		// possible orders for the unit
		var element=document.getElementById("myOrderCanvas");
		var context=element.getContext("2d");

		context.fillStyle="#E0E0E0";	
		context.fillRect(0, 0, element.width, element.height);

		var arg=this.getPossibleOrders();
		var c=0;
		var n=arg.length;
		//element.width=n*this.sectorSizeX+8;
		
		
		for (var i=0; i<n;i++)
		{
			var a=arg[i];
			//console.log("showPossibleOrder "+i+" "+a);
			if (empWin.mapSelection.orderIsRelevant(a))
			{
				this.orderList[c]=a;
				this.showPossibleOrder(context, c, a);
				c++;
			}
		}



	}
	else
	{
		var str="no unit selected";
		context.font = this.font;
		context.fillStyle = 'black';
		context.fillText(str, this.offsetX, this.stepY);

	}



	// show selected unit/sector etc
	this.parentWin.empWinMenu.mapUpdateUpperTextAreas();
}



EmpWinUnit.prototype.click=function(mouseUpPos)
{
	var x=mouseUpPos.x;
	var y=mouseUpPos.y;

	console.log("EmpWinUnit.click: "+x+" "+y);

	if (empWin.mapSelection!=null)
	{


		// This was a click in the order area

		var order= this.getOrder(x);
		console.log("order "+order);

		if (order == null)
		{
			console.log("did not find an order at "+x+" "+y);		
			// For now will use this as an "up" button since one is missing.
			empWin.mapSelection=empWin.mapSelection.parent;
			empWin.mapSetShowState(2);				
		}
		else if (order == "build")
		{
			empWin.mapSetShowState(4);
		}
		else if (order == "moveTo")
		{
			empWin.clearSelectionList();
			empWin.addSelection(empWin.mapSelection.id);
			empWin.mapMoveOrder();
			empWin.mapSetShowState(0);
		}
		else if (order == "cancelOrder")
		{
			empWin.mapCancelOrder();
			empWin.mapSetShowState(2);
		}
		else if (order == "goTo")
		{
			empWin.clearSelectionList();
			empWin.addSelection(empWin.mapSelection.id);
			empWin.mapGoAboard();
			empWin.mapSetShowState(0);
		}
		else
		{
			console.log("sending order "+order);
			empWin.mapSendOrderOne(order);
			empWin.mapSetShowState(0);
		}
	}
	else
	{
		console.log("no unit selected");
	}
}


