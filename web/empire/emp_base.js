// emp_base.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb




function EmpBase(world, parent, emType, arg)
{	
	this.world=world; // Reference to the EmpDb, not the EmpWorld as name would suggest.
	this.parent=parent;
	this.empType=emType;


	this.children=null;

	this.readSelf(arg);
	this.setQuickLinks(arg);
	
	if (this.id in this.world.byId)
	{
		console.log("empWorldInfo: ID already exist '"+this.getPathName()+"', "+this.id+" in byId");
	}
	
	//if ((this.id >=0) && (this.parent!=null))
	if (this.id >=0)  // 2014-10-05, will try like this instead to make update of root object and gameSpeed to work.
	{
		this.world.byId[this.id]=this;
	}
	else
	{
		console.log("empWorldInfo: could not add to byId");
	}

	//console.log("empWorldInfo: '"+this.getPathName()+"'");
	//this.showSelf();
}

EmpBase.prototype.readSelf=function(arg)
{
	this.index=parseInt(arg[0]);
	this.objName=arg[1];
	this.id=parseInt(arg[2]);

	this.info=arg.slice(3);
}


EmpBase.prototype.setQuickLinks=function(arg)
{
}




EmpBase.prototype.getNameOrIndex=function()
{
	if ((this.objName!=='undefined') && (this.objName.length>0))
	{
		return this.objName;
	}
	return ""+this.index;
}


EmpBase.prototype.getPathName=function()
{
	if (this.parent!=null)
	{
		return this.parent.getPathName()+"."+this.getNameOrIndex();
	}
	return this.getNameOrIndex();
}


EmpBase.prototype.getSectorIndexFromXY=function(x,y)
{
	return x+this.sizeX*y;
}


EmpBase.prototype.translateSectorIndexToColumn=function(index)
{
	return index % this.sizeX
}


EmpBase.prototype.translateSectorIndexToRow=function(index)
{
	return  Math.floor(index/this.sizeX);
}


EmpBase.prototype.fromXYToRowCol=function(xyPos)
{
	var ny = Math.floor(xyPos.y/empWin.mapSectorHeight);
	var nx = xyPos.x/empWin.mapSectorWidth;

	if (ny<0)
	{
		console.log("EmpBase.fromXYToRowCol nx="+nx);
	}

	if ((ny%2)!=0)
	{
		nx -= 0.5;
	}

	if (nx<0)
	{
		var w=empWin.empDb.getEmpireWorld();
		var t=w.getEmpireTerrain();
		console.log("EmpBase.fromXYToRowCol nx="+nx+", sizeX="+t.sizeX);
		nx+=t.sizeX;
	}

	nx = Math.floor(nx);

	return {
		col: nx,
		row: ny
	};
}


EmpBase.prototype.fromXYToPos=function(xyPos)
{
	var rc=this.fromXYToRowCol(xyPos);
	return rc.col + rc.row * this.sizeX;
}


EmpBase.prototype.getNChildUnits=function()
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

EmpBase.prototype.orderIsRelevant=function(order)
{
	if ((order=='unload') || (order=='cancelOrder'))
	{
		if (this.getNChildUnits()==0)
		{
			return false;
		}
	}
	return true;
}


EmpBase.prototype.getSectorPosRecursive=function()
{
	if (this.empType=="EmpireSector")
	{
		return this.index;
	}
	else if (this.parent!=null)
	{
		return this.parent.getSectorPosRecursive();
	}
	return -1;
}

EmpBase.prototype.selfToString=function()
{
	var str="name="+this.objName;

	str+=", empType="+this.empType;
	
	var n=this.getNChildUnits();
	if (n>0)
	{
		str+=", n="+n;
	}

	return str;
}


EmpBase.prototype.selfToDebugString=function()
{
	var str=selfToString();
	var pn=this.getPathName();
	str+=", id="+this.id;
	str+=", pn="+pn;
	str+=", idx="+this.index;
	str+=", empType="+this.empType;
	if (this.empType=="EmpireSector")
	{
		str+=", terrain="+this.sectorTerrain;
	}
	else if (this.empType=="EmpireUnit")
	{
		str+=", ut="+this.unitType;
		str+=", pos="+this.getSectorPosRecursive();
	}
	return str;
}

EmpBase.prototype.getUnitType=function()
{
	if (this.unitType in this.world.unitTypesList.children)
	{
		return this.world.unitTypesList.children[this.unitType];
	}
	return null;
}


EmpBase.prototype.getUnitTypeName=function()
{
	var t=this.getUnitType();
	if (t!=null)
	{
		return t.objName;
	}
	return 'unknown';
}




// Returns:
// >=0 : If only on nation have units in the sector
// -1  : If nobody have units in the sector
// -2  : If more than one nation have units in the sector
EmpBase.prototype.getSectorOwner=function()
{
	var owner=-1;
	// If there are sub units (aka children) then loop all and check for owners
	if (this.children!=null)
	{
		var len = this.children.length
		for (var i=0; i<len; ++i)
		{
			if (i in this.children)
			{
				c=this.children[i];
				if (typeof c.unitOwner !== 'undefined')
				{
					var o=c.unitOwner;
					if (owner != o)
					{
						if (owner==-1)
						{
							owner=o;
						}
						else if (owner>=0)
						{
							owner=-2;
						}
					}
				}
			}
		}
	}
	return owner;
}

// Returns:
//  0  : If nobody own the sector
//  1  : State 'state' own this sector (alone) 
//  2  : State 'state' have no units in the sector but other states do.
//  3  : State 'state' have units in the sector and other states too.
EmpBase.prototype.getSectorOwnerState=function(state)
{
	var ownerState=0;
	// If there are sub units (AKA children) then loop all and check for owners
	if (this.children!=null)
	{
		for (var i in this.children)
		{
			var c=this.children[i];
			if (typeof c.unitOwner !== 'undefined')
			{
				if (c.unitOwner==state)
				{
					ownerState|=1;
				}
				else if (c.unitOwner>=0)
				{
					ownerState|=2;
				}
			}
		}
	}
	return ownerState;
}








EmpBase.prototype.showSelf=function()
{
	var str=this.selfToString();
	//chatRoomTextBoxAppend(str);
	console.log(str);
}


// deprecated, to be removed when no longer used.
/*
EmpBase.prototype.showSelfContext=function(context)
{
}
*/

// deprecated, to be removed when no longer used.
/*
EmpBase.prototype.showRecursiveContext=function(context)
{
	this.showSelfContext(context);

	if (this.children!=null)
	{
		// http://stackoverflow.com/questions/3010840/loop-through-array-in-javascript
		var len = this.children.length
		for (var i=0; i<len; ++i) 
		{
			if (i in this.children) 
			{
				var c = this.children[i];
				if (c!=null)
				{
					if (c.index!=i)
					{
						console.log("showRecursiveContext: inconsistent index "+c.index+" "+i+" "+c.selfToString());
						//delete this.children[i];
					}
					else
					{
						//console.log("empWorldInfo: child '"+c.getPathName()+"'");				
						c.showRecursiveContext(context);
					}
				}
			}
		}
	}
}
*/

EmpBase.prototype.showRecursive=function()
{
	this.showSelf();

	if (this.children!=null)
	{
		// http://stackoverflow.com/questions/3010840/loop-through-array-in-javascript
		var len = this.children.length
		for (var i=0; i<len; ++i) 
		{
			if (i in this.children) 
			{
				var c = this.children[i];
				if (c!=null)
				{
					if (c.index!=i)
					{
						console.log("showRecursive: inconsistent index "+c.index+" "+i+" "+c.selfToString());
					}

					//console.log("empWorldInfo: child '"+c.getPathName()+"'");				
					c.showRecursive();
				}
			}
		}
	}
}


EmpBase.prototype.addChild=function(cType, arg)
{
	if (this.children==null)
	{
		this.children=[];
	}

	var n = EmpBaseFactory(this.world, this, cType, arg);

	var index=n.index;

	if (n.index>=0)
	{
		if (this.children[n.index]!=null)
		{
			console.log("empWorldInfo: slot "+n.index+" is occupied '"+this.getPathName()+"'");
		}

		this.children[n.index]=n;
	}
	else
	{
		this.children.push(n);
		console.log("child had no index");
	}

	return n;
}


// this code is not tested
EmpBase.prototype.unlinkSelfFromParent=function()
{
	if (this.parent!=null)
	{
		this.parent.children[this.index]=null;
		delete this.parent.children[this.index];
		this.parent=null;
	}
}


// this code is not tested
EmpBase.prototype.unlinkSelfFromWorld=function()
{
	this.unlinkSelfFromParent();
	this.world.byId[this.id]=null;
	delete this.world.byId[this.id];
	this.id=-1;
}


EmpBase.prototype.getParentId=function()
{
	if (this.parent!=null)
	{
		return this.parent.id;
	}
	return -1;
}


EmpBase.prototype.linkSelf=function(newParent, newIndex)
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
				console.log("changeParent index taken "+newIndex);
				this.parent.children.push(this);
				this.index=-1;
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
}


// this code is not tested
EmpBase.prototype.changeParent=function(newParent, newIndex)
{	
	this.unlinkSelfFromParent();
	this.linkSelf();
}


EmpBase.prototype.updateSelf=function(newParentId, empType, arg)
{
	if (empType!=this.empType)
	{
		console.log("empWorldInfo: changing type is not allowed '"+this.getPathName()+"' "+empType+' '+this.empType);
	}

	this.unlinkSelfFromParent();

	this.readSelf(arg);

	var newParent=this.world.byId[newParentId];

	if (newParent!=null)
	{
		this.linkSelf(newParent, this.index);
	}
	else
	{
		console.log("updateSelf: new parent is null");
	}

}



EmpBase.prototype.getNext=function(current)
{
	if (this.children!=null)
	{
		var i=0;
		if (current!=null)
		{
			i=current.index+1;
		}
		var len = this.children.length;
		while (i<len)
		{
			if (i in this.children) 
			{
				var c = this.children[i];
				return c;
			}
			i++;
		}
	}
	return null;
}


