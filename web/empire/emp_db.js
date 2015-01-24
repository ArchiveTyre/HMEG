// emp_world.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-27 by Henrik Bjorkman www.eit.se/hb


function EmpDb()
{
	this.rootObj=null; // Reference to the top most EmpBase object stored
	this.latestObj=null;
	this.stateDepth=0;
	this.byId=[];
	this.terrain=null;       // Todo: replace use of this with getEmpireWorld().getEmpireTerrain()
	this.unitTypesList=null; // Todo: replace use of this with getEmpireWorld().getEmpUnitTypeList()
	this.nationList=null;  	// Todo: replace use of this with getEmpireWorld().getEmpireStatesList()
	this.empireWorld=null; // Reference to the EmpWorld object
}




EmpDb.prototype.showRecursive = function()
{
	this.rootObj.showRecursive();	
}


EmpDb.prototype.getSector=function(pos)
{
	return this.terrain.children[pos];
}


EmpDb.prototype.getById=function(id)
{
	var len = this.byId.length
	console.log("EmpDb.getById "+id+" "+len);
	if (id in this.byId) 
	{
		var u = this.byId[id];
		
		// This is just for debugging
		if (u.id!=id)
		{
			console.log("showById: inconsistent id "+u.id+" != "+id);
		}
				
		return u;		
	}
	return null;
}


EmpDb.prototype.showById=function()
{
	var len = this.byId.length
	console.log("EmpDb.showById "+len);
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




// Incoming messages are redirected to here when a game world is being down loaded to client.
EmpDb.prototype.onMessageArg=function(arg)
{
	var i=0;
	var cmd=arg[0];
	var reply='';

	//console.log("empWin.inputState1("+this.stateDepth+"): '" +arg+"'"); 

	if (cmd=="{")
	{
		this.stateDepth++;
		//console.log("empWin.empWorldOnMessage: begin mark");
	}
	else if (cmd=="}")
	{
		this.stateDepth--;	
		//console.log("empWin.empWorldOnMessage: end mark");

		if (this.latestObj!=null)
		{
			this.latestObj=this.latestObj.parent;
		}
		else
		{
			console.log("empWin.empWorldOnMessage: its null");
		}

		if ((this.stateDepth==0) || (this.latestObj==null))
		{			
			// tmp test, remove later
			if (this.stateDepth!=0)
			{
				console.log("empWin.empWorldOnMessage: stateDepth!=0"); 
			}

			if (this.latestObj!=null)
			{
				console.log("empWin.empWorldOnMessage: latestObj!=null"); 
				this.latestObj.show();
			}

			//websocket.onmessage = function(evt) { empireOnMessage(evt) };
			return null;
		}
	}
	else if (arg.length>2)
	{
		if (this.latestObj==null)
		{
			var eType=arg[0];
			console.log("first "+eType);
			this.rootObj=EmpBaseFactory(this, null, eType, arg.slice(1));
			this.latestObj=this.rootObj;
			if (this.latestObj==null)
			{
				console.log("new empWin.empWorldInfo failed, unexpected empWin.empWorldLatest==null"); 
			}
		}
		else
		{
			var eType=arg[0];
			//console.log("empWin.empWorldOnMessage: not first "+eType);
			this.latestObj = this.latestObj.addChild(eType, arg.slice(1));
			if (this.latestObj==null)
			{
				console.log("addChild failed, unexpected latestObj==null"); 
			}
		}
	}
	else
	{
		console.log("empWin.empWorldOnMessage: unknown cmd '"+cmd+"'"); 
	}
	return this;
}



EmpDb.prototype.getEmpireWorld=function()
{
	return this.empireWorld;
}


EmpDb.prototype.selfToString=function()
{
	return 'EmpDb';
}
