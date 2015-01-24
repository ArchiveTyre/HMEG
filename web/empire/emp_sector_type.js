// emp_unit_type.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-01-15 by Henrik Bjorkman www.eit.se/hb

var empImagesBeingLoaded=0;
var empImageDefaultSize=32;

function EmpSectorType(typeName)
{	
	this.typeName=typeName;

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
	context.fillText(t.typeName, 0, empImageDefaultSize);

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
	//img.onerror = function() {
	//	context.font = '16pt Calibri';
	//	context.fillStyle = 'black';
	//	context.fillText(t.objName, 0, empImageDefaultSize);
	//};
	img.src = this.typeName+".png";

}




EmpSectorType.prototype.showSelfContextXY=function(context, x, y, width, height)
{
	context.drawImage(this.canvas, x, y-Math.round((width-height)/2), width, width); // Images are square but map on the rows are rectangular, so using width as height.
}



