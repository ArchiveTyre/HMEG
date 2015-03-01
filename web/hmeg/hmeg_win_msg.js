// hemg_win_msg.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-02-19 by Henrik Bjorkman www.eit.se/hb

HmegWinMsg.prototype = Object.create(DivBase.prototype);
HmegWinMsg.prototype.constructor = HmegWinMsg;





function HmegWinMsg(parentWin)
{
	DivBase.call(this, parentWin); // call super constructor

	// constants
	this.offsetY=36;

	this.sectorSizeX=parentWin.sectorWidth;
	this.sectorSizeY=parentWin.sectorHeight;
	
	this.textOffsetY=(this.sectorSizeY*3)/4;
	
	this.parentWin=parentWin;

	this.orderList=[];
}




HmegWinMsg.prototype.defineArea = function(divSize)
{ 
	console.log('defineArea');

	var newPage='';

	// The messages are shown here in the messages text area
	newPage+='<div style="width:'+divSize.x+'px; height:'+divSize.y+'px;">';
	newPage+='<textarea id="textMsgArea" class=emptext readOnly="yes" style="width:'+divSize.x+'px; height:'+divSize.y+'px;"></textarea><br/>';
	newPage+='</div>';

	return newPage;
}


HmegWinMsg.prototype.drawWin = function()
{
	//console.log('drawWin');

	var p = this.parentWin;
	var d = p.hmegDb;

	var a = d.getById(p.avatarId);

	if (a!=null)
	{
		var r = a.getRoundBuffer();
		if (r!=null)
		{
			this.showMessagesOnTextArea("textMsgArea", r);
		}
		else
		{
			console.log("did not find RoundBuffer for ~"+p.avatarId);
		}
	}
	else
	{
		console.log('no avatar');
	}
	
	
}

HmegWinMsg.prototype.addEventListenersDiv=function()
{
	this.element = document.getElementById("textMsgArea");
	this.addText("hello world");
}

HmegWinMsg.prototype.addText=function(str)
{
	console.log(str);
	
	if (this.element!=null)
	{
		var e = this.element;
		e.value+=str;
		e.scrollTop = e.scrollHeight;
	}
}



HmegWinMsg.prototype.showMessagesOnTextArea=function(textAreaName, r)
{
	this.textBoxClear(textAreaName);

	var j=r.head;
	for (var i=0; i<r.maxObjects; ++i)
	{
		if (j in r.children)
		{
			var c = r.children[j];
			var str= c.objName+": "+c.order;
			this.textBoxAppend(textAreaName, str);
		}

		j++;
		if (j>=r.maxObjects*2)
		{
			j=0;
		}
	}
}


HmegWinMsg.prototype.textBoxAppend=function(textAreaName, msg)
{
	console.log("textBoxAppend "+msg);
	var e = document.getElementById(textAreaName);

	e.value+=hlibRemoveQuotes(msg)+"\n";
	e.scrollTop = e.scrollHeight;
}


HmegWinMsg.prototype.textBoxClear=function(textAreaName)
{
	var e = document.getElementById(textAreaName);

	e.value="";
	e.scrollTop = e.scrollHeight;
}

