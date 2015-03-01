// MirrorRoundBuffer.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb



// subclass extends superclass
MirrorRoundBuffer.prototype = Object.create(MirrorBase.prototype);
MirrorRoundBuffer.prototype.constructor = MirrorRoundBuffer;

function MirrorRoundBuffer(world, parent, emType, arg)
{	
	MirrorBase.call(this, world, parent, emType, arg); // call super constructor
	
	parent.eRoundBuffer=this;
}

MirrorRoundBuffer.prototype.readSelf=function(arg)
{
	var n = MirrorBase.prototype.readSelf.call(this, arg);

	this.head = parseInt(arg[n]);
	this.tail = parseInt(arg[n+1]);
	this.msgCount = parseInt(arg[n+2]);
	this.maxObjects = parseInt(arg[n+3]); // max number of messages stored in the round buffer, max index is: maxObjects*2-1

    //console.log("MirrorRoundBuffer.prototype.readSelf "+arg);

	//this.info=arg.slice(3);
	
	return arg.length;
}

MirrorRoundBuffer.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", head="+this.head;
	str+=", tail="+this.tail;
	
	var n=this.getNChildObjects();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}




