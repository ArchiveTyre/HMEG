// emp_unit_type.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb


// subclass extends superclass
EmpUnitType.prototype = Object.create(EmpBase.prototype);
EmpUnitType.prototype.constructor = EmpUnitType;

function EmpUnitType(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor

}

EmpUnitType.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.possibleOrders=hlibRemoveQuotes(arg[22]);
	this.possibleBuilds=hlibRemoveQuotes(arg[23]);

	this.info=arg.slice(3);

	var canvas = document.createElement('canvas');
	var context = canvas.getContext('2d');

	canvas.width = empImageDefaultSize;
	canvas.height = empImageDefaultSize;
	
	//context.fillStyle="#C0C0E0";				
	//context.fillRect(0, 0, canvas.width, canvas.height);

	this.canvas=canvas;
	var t=this;

	context.font = '16pt Calibri';
	context.fillStyle = 'black';
	context.fillText(t.objName, 0, empImageDefaultSize);

	empImagesBeingLoaded++;
	
	var img = new Image();
	img.onload = function() {
		context.clearRect(0, 0, canvas.width, canvas.height);
		context.drawImage(img, 0, 0, empImageDefaultSize, empImageDefaultSize);
		empImagesBeingLoaded--;
		if (empImagesBeingLoaded<=0)
		{
			empWin.drawWin();
			empImagesBeingLoaded=0;
		}
	};
	/*img.onerror = function() {
		context.font = '16pt Calibri';
		context.fillStyle = 'black';
		context.fillText(t.objName, 0, empImageDefaultSize);
	};*/
	img.src = this.objName+".png";

}


EmpUnitType.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", possibleOrders="+this.possibleOrders;
	str+=", possibleBuilds="+this.possibleBuilds;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}


EmpUnitType.prototype.showSelfContextXY=function(context, x, y, width, height)
{
	context.drawImage(this.canvas, x, y-Math.round((width-height)/2), width, width); // Images are square but map on the rows are rectangular, so using width as height.
}



