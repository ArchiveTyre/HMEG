// emp_win_base.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-31 by Henrik Bjorkman www.eit.se/hb

function sqr(a)
{
	return a*a;
}

function calcDist(p1, p2)
{
	return Math.sqrt(sqr(p1.x-p2.x)+sqr(p1.y-p2.y));
}

// http://www.html5canvastutorials.com/advanced/html5-canvas-mouse-coordinates/
// This first simple solution work in most cases. But in android-chrome it does not work if user has zoomed in.
/*
function mapGetMousePos(canvas, evt) {
	var rect = canvas.getBoundingClientRect();
	return {
		x: evt.clientX - rect.left,
		y: evt.clientY - rect.top
	};
}
*/
// Trying this more advanced one:
// But same as above, in android-chrome it does not work if user has zoomed in.
function mapGetMousePos(canvas, evt) {
	var rect = canvas.getBoundingClientRect();
	return {
		x: Math.round((evt.clientX-rect.left)/(rect.right-rect.left)*canvas.width),
		y: Math.round((evt.clientY-rect.top)/(rect.bottom-rect.top)*canvas.height)
	};
}




function EmpWinBase(parentWin)
{	
	this.parentWin=parentWin;
}

EmpWinBase.prototype.defineCentralArea=function(subWinSize)
{
	console.log('defineCentralArea not implemented '+subWinSize.x+" "+subWinSize.y);
}

EmpWinBase.prototype.addEventListeners=function()
{
	console.log('addEventListeners not implemented');
}

EmpWinBase.prototype.drawSubWin=function()
{
	console.log('drawSubWin not implemented');
}

EmpWinBase.prototype.click=function(mouseUpPos)
{
	console.log('click not implemented');
}


EmpWinBase.prototype.drag=function(mouseDownPos, mouseUpPos)
{
	console.log('drag not implemented');
}


// This registers the code that will handle various events
EmpWinBase.prototype.mapAddEventListenerForMyCanvas=function(canvasName)
{
	// HTML5 Canvas Mouse Coordinates Tutorial
	// http://www.html5canvastutorials.com/advanced/html5-canvas-mouse-coordinates/
	var canvas = document.getElementById(canvasName);
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);

	canvas.addEventListener('mousemove', function(evt) {
		// We get here if there was an event (such as mouse click) on the canvas
		var mousePos = mapGetMousePos(canvas, evt);
 		//var mMessage = 'Mouse position: ' + mousePos.x + ',' + mousePos.y;
		//mapWriteMessage(canvas, mMessage);
		/*var sectorPos = mapTranslateMousePosToSector(mousePos);
		if (terrainMap.isOnMap(sectorPos.x,sectorPos.y)!=0)
		{
			var sMessage = 'Sector position: ' + sectorPos.x + ',' + sectorPos.y+' ('+terrainMap.getUnitInfo(sectorPos.x,sectorPos.y)+')';  ;
			empWin.mapWriteMessage(canvas, sMessage);
		}
		else
		{
			empWin.mapWriteMessage(canvas, '');
		}

		if (typeof empWin.mapMouseDownPos !== 'undefined')
		{
			//context.moveTo(empWin.mapMouseDownPos.x,empWin.mapMouseDownPos.y);
			//context.lineTo(mousePos.x,mousePos.y);
			//context.stroke();
		}*/

	}, false);

	canvas.addEventListener('mousedown', function (evt) {
		empWin.mapMouseDownPos = mapGetMousePos(canvas, evt);
	}, false);


	canvas.addEventListener('mouseup', function (evt) {
		var mouseUpPos = mapGetMousePos(canvas, evt);

		var d=calcDist(mouseUpPos, empWin.mapMouseDownPos);

		if (d<=1.5)
		{
			// regular click
			console.log('Sector click position: ' + mouseUpPos.x + "," + mouseUpPos.y+" "+d);
			empWin.mapGetStateHandler().click(mouseUpPos);
		}
		else
		{
			// drag
			// This is to scroll (move the visible part of) the map, not implemented yet
			console.log('mouse down position: ' + empWin.mapMouseDownPos.x + "," + empWin.mapMouseDownPos.y);
			console.log('mouse up position: ' + mouseUpPos.x + "," + mouseUpPos.y);
			console.log('dist: ' + d);
			empWin.mapGetStateHandler().drag(empWin.mapMouseDownPos, mouseUpPos);
			
		}

		// TODO: This call should be done somewhere else, this does not add an event listener.
		//empWin.mapUpdateUpperTextAreas();

	}, false);
}


EmpWinBase.prototype.mouseDiff=function(m1, m2)
{
	var d={x: (m2.x-m1.x), y: (m2.y-m1.y)};
	return d;
}




