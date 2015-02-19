// DivButton.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-31 by Henrik Bjorkman www.eit.se/hb



// http://www.html5canvastutorials.com/advanced/html5-canvas-mouse-coordinates/
// This solution work in most cases. But in android-chrome it does not work if user has zoomed in.
function divBaseTranslateMousePos(canvas, evt) {
	var rect = canvas.getBoundingClientRect();
	return {
		x: Math.round((evt.clientX-rect.left)/(rect.right-rect.left)*canvas.width),
		y: Math.round((evt.clientY-rect.top)/(rect.bottom-rect.top)*canvas.height)
	};
}


// Each sub div must know its parent and have a globaly unique name
function DivButton(parentDiv, divId, divName)
{	
	this.parentDiv=parentDiv;
	this.divId=divId;
	this.divName=divName;
	this.children=[];
}


// Returns the html code needed to show the div.
DivButton.prototype.defineDiv=function(divSize)
{

	var newPage='';
//	newPage+='<div style="width:'+divSize.x+'px; height:'+divSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';
//	newPage+='<div style="width:'+divSize.x+'px; height:'+divSize.y+'px;overflow: auto;">';
	newPage+='<div>';

	newPage+='<canvas id="Canvas'+this.divId+'" width="'+(divSize.x)+'" height="'+(divSize.y)+'"></canvas>';

	newPage+='</div>';

	this.canvas=null;
	
	return newPage;
}


DivButton.prototype.addEventListenersDiv=function()
{
	this.canvas = document.getElementById('Canvas'+this.divId);

	var div=this;
	this.canvas.addEventListener('mouseup', function (evt) {
		var mousePos = divBaseTranslateMousePos(div.canvas, evt);
			//console.log('div click position: ' + mousePos.x + "," + mousePos.y+" "+div.divName);
			div.clickDiv(mousePos);
	}, false);
}


DivButton.prototype.drawDiv=function()
{
	if (this.canvas==null)
	{
		this.addEventListenersDiv();
	}

	var context = this.canvas.getContext('2d');

	var fontSize=Math.round(this.canvas.height*0.4);

	//context.clearRect(0, 0, canvas.width, canvas.height);
	context.fillStyle="#E0E0E0";	
	context.fillRect(0, 0, this.canvas.width, this.canvas.height);

	context.font = fontSize + 'pt Calibri';
	context.fillStyle = 'black';
	context.fillText(this.divName, Math.round(fontSize/2), fontSize+Math.round((this.canvas.height-fontSize)*0.5));

}

DivButton.prototype.clickDiv=function(mousePos)
{
	console.log('div click position: ' + mousePos.x + "," + mousePos.y+" "+this.divName);
	if (this.parentDiv!=null)
	{
		this.parentDiv.childEvent(this.divId);
	}
}







