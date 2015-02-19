// emp_win_base.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-31 by Henrik Bjorkman www.eit.se/hb


// http://www.html5canvastutorials.com/advanced/html5-canvas-mouse-coordinates/
// This solution work in most cases. But in android-chrome it does not work if user has zoomed in.
function mapGetMousePos(canvas, evt) {
	var rect = canvas.getBoundingClientRect();
	return {
		x: Math.round((evt.clientX-rect.left)/(rect.right-rect.left)*canvas.width),
		y: Math.round((evt.clientY-rect.top)/(rect.bottom-rect.top)*canvas.height)
	};
}




function DivBase(parentWin)
{	
	this.parentWin=parentWin;
}

// Returns the html code needed to show the div.
DivBase.prototype.defineDiv=function(divSize)
{
	console.log('defineDiv not implemented '+divSize.x+" "+divSize.y);
}

DivBase.prototype.addEventListenersDiv=function()
{
	console.log('addEventListenersDiv not implemented');
}

DivBase.prototype.drawDiv=function()
{
	console.log('drawDiv not implemented');
}

DivBase.prototype.click=function(mouseUpPos)
{
	console.log('click not implemented');
}


DivBase.prototype.drag=function(mouseDownPos, mouseUpPos)
{
	console.log('drag not implemented');
}


// This registers the code that will handle various events
DivBase.prototype.addEventListenersDiv=function(canvasName)
{
	var canvas = document.getElementById(canvasName);
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);

	canvas.addEventListener('mousemove', function(evt) {
		// We get here if there was an event (such as mouse click) on the canvas
		var mousePos = mapGetMousePos(canvas, evt);

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


DivBase.prototype.mouseDiff=function(m1, m2)
{
	var d={x: (m2.x-m1.x), y: (m2.y-m1.y)};
	return d;
}




