// DivButtonList.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-01-17 by Henrik Bjorkman www.eit.se/hb




// subclass extends superclass
DivButtonList.prototype = Object.create(DivBase.prototype);
DivButtonList.prototype.constructor = DivButtonList;



function DivButtonList(parent, divId, divName, buttonTexts, headingText)
{	
	DivBase.call(this, parent, divId, divName); // call super constructor
	this.buttonTexts=buttonTexts;
	this.headingText=headingText;

	for (i=0;i<buttonTexts.length;i++)
	{
		this.children[i]=new DivBase(this, i, hlibRemoveQuotes(buttonTexts[i]));

		console.log('extraButton '+i+' '+buttonTexts[i]);
	}
}



// Returns the html code needed to show the div.
DivButtonList.prototype.defineDiv=function(divSize)
{
	this.canvas=null;

	var newPage='';
	var i=0;
	var n=this.buttonTexts.length+2;
	var ySize=Math.floor(window.innerWidth/4);
	if ((ySize*n)>window.innerHeight)
	{
		ySize=Math.floor(window.innerHeight/n);
	}
	if (ySize>100)
	{
		ySize=100;
	}
	newPage+='<div style="overflow-x: hide; overflow-y: auto;">';
	newPage+='<h3>'+this.headingText+'</h3><br>';

	for (i=0;i<this.children.length;i++)
	{
		newPage+=this.children[i].defineDiv({x: divSize.x-16, y: ySize});
	}

	newPage+='</div>';
	
	return newPage;
}


DivButtonList.prototype.addEventListenersDiv=function()
{
	for (i=0;i<this.children.length;i++)
	{
		this.children[i].addEventListenersDiv();
	}

	this.canvas = document.getElementById('Canvas'+this.divId);

	var div=this;
	this.canvas.addEventListener('mouseup', function (evt) {
		var mousePos = divBaseTranslateMousePos(div.canvas, evt);
			console.log('div click position: ' + mousePos.x + "," + mousePos.y+" "+div.divName);
			div.clickDiv(mousePos);
	}, false);

}


DivButtonList.prototype.drawDiv=function()
{
	if (this.canvas==null)
	{
		this.addEventListenersDiv();
	}

	for (i=0;i<this.children.length;i++)
	{
		this.children[i].drawDiv();
	}

/*
	var context = this.canvas.getContext('2d');

	var fontSize=Math.round(this.canvas.height*0.4);

	//context.clearRect(0, 0, canvas.width, canvas.height);
	context.fillStyle="#E0E0E0";	
	context.fillRect(0, 0, this.canvas.width, this.canvas.height);

	context.font = fontSize + 'pt Calibri';
	context.fillStyle = 'black';
	context.fillText(this.divName, Math.round(fontSize/2), fontSize+Math.round((this.canvas.height-fontSize)*0.5));
*/
}


DivButtonList.prototype.childEvent=function(eventId)
{
	console.log('childEvent '+eventId);
	doSend(eventId);	
        $("body").empty();
}
