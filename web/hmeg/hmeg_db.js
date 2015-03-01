// hmeg_dbjs
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-27 by Henrik Bjorkman www.eit.se/hb


// subclass extends superclass
HmegDb.prototype = Object.create(MirrorDb.prototype);
HmegDb.prototype.constructor = HmegDb;




function HmegDb()
{
	MirrorDb.call(this); // call super constructor
}




HmegDb.prototype.showRecursive = function()
{
	this.rootObj.showRecursive();	
}


HmegDb.prototype.getSector=function(pos)
{
	return this.terrain.children[pos];
}




HmegDb.prototype.showById=function()
{
	var len = this.byId.length
	console.log("HmegDb.showById "+len);
	for (var i=0; i<len; ++i) 
	{
		if (i in this.byId) 
		{
			var c = this.byId[i];
			if (c.id!=i)
			{
				console.log("showById: inconsistent id "+c.id+" "+i);
			}				
			c.showSelf();
		}
	}
}






HmegDb.prototype.selfToString=function()
{
	return 'HmegDb';
}
