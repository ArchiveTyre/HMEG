// MirrorBase.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb, All rights reserved
// Created 2015-01-28 by Henrik Bjorkman



function MirrorBase(mirrorDb, parent, arg)
{	
	this.mirrorDb=mirrorDb; // Reference to the mirrorDb, not the EmpWorld as name would suggest.
	this.parent=parent;
	this.children=null;

	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	
	if (this.id in this.mirrorDb.byId)
	{
		console.log("MirrorBase: ID already exist '"+this.getPathName()+"', "+this.id+" in byId");
	}
	
	if (this.id >=0)
	{
		this.mirrorDb.byId[this.id]=this;
	}
	else
	{
		console.log("MirrorBase: could not add to byId, "+this.id+", objName="+this.objName);
	}

	this.readSelf(arg);

	//console.log("MirrorBase: '"+this.getPathName()+"'");
	//this.showSelf();
}

// Returns the number of arguments parsed by this function. Caller can take care of the remaining arguments.
MirrorBase.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	var id=parseInt(arg[2]);
	if (id!=this.id)
	{
		console.log("id must not change ~"+id +" ~"+this.id);
	}
	this.id=id;
	return 3;
}


MirrorBase.prototype.addChild=function(cType, arg)
{
	//console.log("addChild, cType "+cType+", arg "+arg);

	if (this.children==null)
	{
		this.children=[];
	}

	var n = MirrorFactory(this.mirrorDb, this, cType, arg);

	if (n != null)
	{
		if (n.index>=0)
		{
			if (this.children[n.index]!=null)
			{
				// This should never happen, but it did, don't know why. Aha it was because I had commented out lines "this.index=parseInt(arg[0]);" in readSelf.
				var c=this.children[n.index];
				console.log("slot "+n.index+" in ~"+this.id+" '"+this.getPathName()+"' is occupied by ~"+c.id+ " with index "+c.index);
				this.children.push(c);
				c.index=this.children.indexOf(c);
				console.log("moved ~"+c.id+" to "+c.index);
			}
	
			this.children[n.index]=n;
		}
		else
		{
			// This should not happen, server shall not send objects without an index.
			this.children.push(n);
			console.log("child had no index ~"+this.id+" ~"+n.id);
			n.index=this.children.indexOf(n);
			console.log("placed it in "+n.index);
	
		}
	}
	else
	{
		console.log("MirrorFactory could not create an object of type "+ cType+ " ignored it");
	}
	
	
	return n;
}

/*
MirrorBase.prototype.getNChildObjects=function()
{
	var n=0;
	if (this.children!=null)
	{
		var i=0;
		var len = this.children.length;
		while (i<len)
		{
			if (i in this.children) 
			{
				n++;
			}
			i++;
		}
	}
	return n;
}
*/
MirrorBase.prototype.getNChildObjects=function()
{
	var n=0;
	if (this.children!=null)
	{
		for(i in this.children)
		{
			// This is just for debugging, can be removed later
			var c = this.children[i];
			if (c.parent != this)
			{
				console.log("database is corrupt, this.id=~"+this.id+" c.id=~"+c.id);
			}
			
			n++;
		}
	}
	return n;
}


MirrorBase.prototype.unlinkSelfFromParent=function()
{
	if (this.parent!=null)
	{
		this.parent.children[this.index]=null;
		delete this.parent.children[this.index];
		this.parent=null;
	}
	else
	{
		console.log("can't unlink from null "+this.id);
	}
}


// this code is not tested
MirrorBase.prototype.unlinkSelfFromWorld=function()
{
	this.unlinkSelfFromParent();
	this.mirrorDb.byId[this.id]=null;
	delete this.mirrorDb.byId[this.id];
	this.id=-1;
}


MirrorBase.prototype.getParentId=function()
{
	if (this.parent!=null)
	{
		return this.parent.id;
	}
	return -1;
}

MirrorBase.prototype.getParent=function()
{
	return this.parent;
}


MirrorBase.prototype.linkSelf=function(newParent, newIndex)
{	
	this.parent=newParent;
	this.index=newIndex;
	if (this.parent!=null)
	{
		if (this.parent.children==null)
		{
			this.parent.children=[];
		}

		if (newIndex >= 0)
		{
			if (newIndex in this.parent.children)
			{
				// This should not happen
				console.log("linkSelf: index taken "+newIndex+" ~"+this.id+" ~"+this.parent.id);
				this.parent.children.push(this);
				this.index=this.parent.children.indexOf(this);
			}
			else
			{
				this.parent.children[newIndex]=this;
			}
		}
		else
		{
			this.parent.children.push(this)
		}
	}
	else
	{
		console.log("can't link under null");
	}
	
}


// this code is not tested, is it even used?
MirrorBase.prototype.changeParent=function(newParent, newIndex)
{	
	this.unlinkSelfFromParent();
	this.linkSelf();
}


MirrorBase.prototype.updateSelf=function(newParentId, empType, arg)
{

	// This is just an extra check for debugging, can be removed later.
	if (typeof this.empType !== 'undefined')
	{
		if (empType!=this.empType)
		{
			console.log("changing type is not allowed '"+this.objName+"' "+empType+' '+this.empType);
		}
	}

	var newParent=this.mirrorDb.byId[newParentId];

	// Only need to unlink and link if object shall change parent
	if (this.parent==null)
	{
		this.readSelf(arg);

		if (newParent!=null)
		{
			console.log("updateSelf: old parent is null but new is not, id=~"+this.id);
			this.linkSelf(newParent, this.index);
		}
	}
	else if (this.parent.id!=newParentId)
	{
		this.unlinkSelfFromParent();

		this.readSelf(arg);

		if (newParent!=null)
		{
			this.linkSelf(newParent, this.index);
		}
		else
		{
			console.log("updateSelf: new parent is null, id=~"+this.id);
		}
	}
	else
	{
		this.readSelf(arg);
	}
}


// If there are many objects this is inefficient.
MirrorBase.prototype.findSubObjectByName=function(objName)
{
	if (this.children!=null)
	{
		for (i in this.children) 
		{
			//console.log("examine: objName="+this.children[i].objName);
			if (this.children[i].objName===objName)
			{
				console.log("found: objName="+objName);
				return this.children[i];
			}
		}
	}
	console.log("did not find: objName="+objName+" in ~"+this.id);
	return null;		
}

/*
// If there are many objects this is inefficient.
MirrorBase.prototype.findSubObjectByName=function(objName)
{
	if (this.children!=null)
	{
		var i=0;
		var len = this.children.length;
		while (i<len)
		{
			if (i in this.children) 
			{
				//console.log("examine: objName="+this.children[i].objName);
				if (this.children[i].objName===objName)
				{
					console.log("found: objName="+objName);
					return this.children[i];
				}
			}
			i++;
		}
	}
	console.log("did not find: objName="+objName);
	return null;
}
*/


MirrorBase.prototype.debugDump=function(prefix)
{
	if (typeof prefix === 'undefined')
	{
		prefix = "";
	}
	
	console.log(prefix+"index="+this.index+", objName="+this.objName+", id=~"+this.id);
	
	if (this.children!=null)
	{
		for (x in this.children) 
		{
			var c = this.children[x];
			if (c.parent != this)
			{
				console.log("database is corrupt, this.id=~"+this.id+" c.id=~"+c.id);
			}
			c.debugDump(prefix+"  ");
		}
	}
}

