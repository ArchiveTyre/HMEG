// DivImg.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-02-21 by Henrik Bjorkman www.eit.se/hb


var DivImagesBeingLoaded=0;

function DivImg(imgName)
{	
	this.imgName=imgName;

	var canvas = document.createElement('canvas');
	var context = canvas.getContext('2d');

	canvas.width = empImageDefaultSize;
	canvas.height = empImageDefaultSize;
	
	//context.fillStyle="#C0C0E0";				
	//context.fillRect(0, 0, canvas.width, canvas.height);

	this.canvas=canvas;
	var t=this;


	DivImagesBeingLoaded++; // global counter, number of images being loaded, when all are loaded the window needs to be redrawn
	
	var img = new Image();
	img.onload = function() {
		// image is now loaded, put it on the local canvas.
		context.clearRect(0, 0, canvas.width, canvas.height);
		context.drawImage(img, 0, 0, empImageDefaultSize, empImageDefaultSize);
		DivImagesBeingLoaded--;
		if (DivImagesBeingLoaded<=0) // this was the last image to be loaded, redraw the window, now with images.
		{
			rootDiv.drawDiv();
			DivImagesBeingLoaded=0;
		}
	};
	img.onerror = function() {
		// loading image failed, write some text on the local canvas instead.
		context.font = '8pt Calibri';
		context.fillStyle = 'black';
		context.fillText(t.imgName, 0, empImageDefaultSize*0.8);
		DivImagesBeingLoaded--;
		if (DivImagesBeingLoaded<=0) // this was the last image to be loaded. It failed, but others may have been loaded OK so redraw window anyway.
		{
			rootDiv.drawDiv();
			DivImagesBeingLoaded=0;
		}
	};
	img.src = this.imgName+".png"; // Setting src triggers loading of image. Assuming all images are in png format.
}


DivImg.prototype.showSelfContextXY=function(context, x, y, width, height)
{
	context.drawImage(this.canvas, x, y-Math.round((width-height)/2), width, width); // Images are square but map on the rows are rectangular, so using width as height.
}




