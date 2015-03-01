// hmeg_room.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-02-21 by Henrik Bjorkman www.eit.se/hb


// subclass extends superclass
HmegRoom.prototype = Object.create(HmegEntity.prototype);
HmegRoom.prototype.constructor = HmegRoom;




function HmegRoom(world, parent, arg)
{
	HmegEntity.call(this, world, parent, arg); // call super constructor
	
	
	this.img = [];
	
	this.initSelf();
	

	
	
}




HmegRoom.prototype.readSelf=function(arg)
{
	var n = HmegEntity.prototype.readSelf.call(this, arg);


	this.outerX=parseInt(arg[n++]);
	this.outerY=parseInt(arg[n++]);
	this.xSectors=parseInt(arg[n++]);
	this.ySectors=parseInt(arg[n++]);

	this.map=[];
		
	for(var x=0;x<this.xSectors;x++)
	{
		this.map[x]=[];
		for(var y=0;y<this.ySectors;y++)
		{
			this.map[x][y] = parseInt(arg[n++]);
		}
	}


	return n;	
}


HmegRoom.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", x="+this.x;
	str+=", y="+this.y;


	var n=this.getNChildObjects();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}


HmegRoom.prototype.isControlPanel=function(x, y)
{
	var CityPvpBlock_controlPanel=11;
	
   if (this.map[x][y]==CityPvpBlock_controlPanel)
   {
	   return true;
   }
   return false;
}

