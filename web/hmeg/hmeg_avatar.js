
// hmeg_avatar.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-27 by Henrik Bjorkman www.eit.se/hb


// subclass extends superclass
HmegAvatar.prototype = Object.create(HmegEntity.prototype);
HmegAvatar.prototype.constructor = HmegAvatar;




function HmegAvatar(world, parent, arg)
{
	HmegEntity.call(this, world, parent, arg); // call super constructor
}


//HmegAvatar.prototype.initSelf=function()
//{
//	this.img[0] = new DivImg("avatar_0");
//	this.img[1] = new DivImg("avatar_1");
//	this.img[2] = new DivImg("avatar_2");
//	this.img[3] = new DivImg("avatar_3");
//}



HmegAvatar.prototype.getRoundBuffer = function()
{
	if (this.roundBufferObj==null)
	{
		this.roundBufferObj=this.findSubObjectByName("roundBuffer");
	}
	return this.roundBufferObj;
}


//HmegAvatar.prototype.showSelfContextXY=function(context, x, y, width, height)
//{
//	if (this.state < this.img.length)
//	{
//		this.img[this.state].showSelfContextXY(context, x, y, width, height);
//	}
//	else
//	{
//		this.unknown.showSelfContextXY(context, x, y, width, height);  
//	}
//}
