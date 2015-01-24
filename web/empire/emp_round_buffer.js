// emp_round_buffer.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb



// subclass extends superclass
EmpRoundBuffer.prototype = Object.create(EmpBase.prototype);
EmpRoundBuffer.prototype.constructor = EmpRoundBuffer;

function EmpRoundBuffer(world, parent, emType, arg)
{	
	EmpBase.call(this, world, parent, emType, arg); // call super constructor
	
	parent.eRoundBuffer=this;
}

EmpRoundBuffer.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.head = parseInt(arg[3]);
	this.tail = parseInt(arg[4]);
	this.msgCount = parseInt(arg[5]);
	this.maxObjects = parseInt(arg[6]); // max number of messages stored in the round buffer, max index is: maxObjects*2-1

    //console.log("EmpRoundBuffer.prototype.readSelf "+arg);

	this.info=arg.slice(3);
}

EmpRoundBuffer.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", head="+this.head;
	str+=", tail="+this.tail;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}


// deprecated, to be removed when no longer used.
/*
EmpRoundBuffer.prototype.showSelfContext=function(context)
{
	this.showSelfRoundBufferContext(context);
}
*/

// Actually this will not show self on context but on 'textArea'
// deprecated, to be removed when no longer used.
EmpRoundBuffer.prototype.showSelfRoundBufferContext=function(context)
{
	this.showSelfOnTextArea("textArea");
}

EmpRoundBuffer.prototype.showSelfOnTextArea=function(textAreaName)
{
	var nation=this.parent;
	var nationList=nation.parent;
	var nationNr=nationList.children.indexOf(nation);
	//console.log("nr "+empWin.mapNation+" "+nationNr);
	if (empWin.mapNation==nationNr)
	{

		this.textBoxClear(textAreaName);

		//var len = this.children.length;
		var j=this.head;
		for (var i=0; i<this.maxObjects; ++i)
		{
			if (j in this.children)
			{
				var c = this.children[j];
				//var str = c.selfToString();
				var str= c.objName+": "+c.order;
				this.textBoxAppend(textAreaName, str);
			}

			j++;
			if (j>=this.maxObjects*2)
			{
				j=0;
			}
		}
	}
}

EmpRoundBuffer.prototype.textBoxAppend=function(textAreaName, msg)
{
	var e = document.getElementById(textAreaName);

	e.value+=hlibRemoveQuotes(msg)+"\n";
	e.scrollTop = e.scrollHeight;
}


EmpRoundBuffer.prototype.textBoxClear=function(textAreaName)
{
	var e = document.getElementById(textAreaName);

	e.value="";
	e.scrollTop = e.scrollHeight;
}

