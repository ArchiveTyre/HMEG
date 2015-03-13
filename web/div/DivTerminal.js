// emp_win_terminal.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

DivTerminal.prototype = Object.create(DivBase.prototype);
DivTerminal.prototype.constructor = DivTerminal;




function DivTerminal(parentWin)
{
	DivBase.call(this, parentWin); // call super constructor
	this.count=null;
	this.text='';
	this.textElement=null;
	
	this.changeConter=0;
	this.previousChangeConter=0;
}

DivTerminal.prototype.defineDiv=function(divSize)
{
	var newPage='';

	// The central area of the page	
	newPage+='<div style="width:'+divSize.x+'px; height:'+divSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';

	
	// The console 
	//newPage+='<div>';
	newPage+='<textarea id="consoleArea" class=emptext rows=24 readOnly="yes"></textarea><br/>'; // This creates the console text area
	
	if (!this.parentWin.mobileMode)
	{
		newPage+='<input type="text" id="inputText" size="88" onchange="rootDiv.empWinConsole(\'inputText\')"><br/>'; // This is where text can be written by user
		newPage+='<input type="button" value=enter onclick="rootDiv.empWinConsole(\'inputText\')">';
		newPage+='<input type="button" value=cancel onclick="rootDiv.mapCanvasCancel()">';
		newPage+='<input type="button" value=back onclick="rootDiv.DivTerminal.back();"><br>';
	}
	//newPage+='</div>';


	
	newPage+='</div>';



	return newPage;
}


DivTerminal.prototype.addEventListenersDiv=function()
{
	this.textElement = document.getElementById("consoleArea");

	this.textElement.value+=this.text;
}

DivTerminal.prototype.drawDiv=function()
{
    if (this.previousChangeConter!=this.changeConter)
    {
		var e = this.textElement;
		e.scrollTop = e.scrollHeight;
		this.previousChangeConter=this.changeConter;
	}
}


DivTerminal.prototype.click=function(mouseUpPos)
{
	console.log("not implemented yet");
}

DivTerminal.prototype.back=function()
{
	this.text=this.textElement.value+"---\n";
	this.textElement=null;
	rootDiv.mapSetShowState(0)
}

DivTerminal.prototype.textBoxAppend=function(msg)
{
	var str=hlibRemoveQuotes(msg)+"\n";

	if (this.text!=null)
	{
		this.text += str;
	}
	if (this.textElement!=null)
	{
		var e = this.textElement;
		e.value+=str;
		e.scrollTop = e.scrollHeight;
	}

	this.changeConter++;
	
	console.log(str);
}

