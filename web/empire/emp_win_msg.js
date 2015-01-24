// emp_win_msg.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

EmpWinMsg.prototype = Object.create(EmpWinBase.prototype);
EmpWinMsg.prototype.constructor = EmpWinMsg;





function EmpWinMsg(parentWin)
{
	EmpWinBase.call(this, parentWin); // call super constructor

	// constants
	this.offsetY=36;

	this.sectorSizeX=empWin.mapSectorWidth;
	this.sectorSizeY=empWin.mapSectorHeight;
	this.textOffsetY=(this.sectorSizeY*3)/4;
	
	this.parentWin=parentWin;

	this.orderList=[];
}




EmpWinMsg.prototype.defineArea = function(subWinSize)
{ 
	var newPage='';

	// The messages are shown here in the messages text area
	newPage+='<div style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px;">';
	if (this.parentWin.mobileMode)
	{
		newPage+='<textarea id="textArea" class=emptext readOnly="yes" onmouseup="empWin.mapSetShowState(6)" style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px;"></textarea><br/>';
	}
	else
	{
		newPage+='<textarea id="textArea" class=emptext readOnly="yes" style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px;"></textarea><br/>';
	}
	newPage+='</div>';

	return newPage;
}


EmpWinMsg.prototype.drawWin = function()
{
	// show messages from the round buffer
	var w=empWin.empDb.getEmpireWorld();
	if (w!=null)
	{		
		var cl=w.getEmpireStatesList().children;
		var n=cl[empWin.mapNation];
		if (n!=null)
		{
			var r=n.eRoundBuffer;
			if (r!=null)
			{
				r.showSelfOnTextArea("textArea");
			}
		}
		else
		{
			console.log('no nation '+empWin.mapNation);
		}	
	}		
}
