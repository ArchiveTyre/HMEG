// MirrorDb.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb, All rights reserved
// Created 2015-01-28 by Henrik Bjorkman





function MirrorDb()
{
	this.rootObj=null; // Reference to the top most EmpBase object stored
	this.latestObj=null; // used only during initial download
	this.stateDepth=0; // used only during initial download
	this.byId=[];
}



MirrorDb.prototype.getById=function(id)
{
	var len = this.byId.length
	//console.log("MirrorDb.getById "+id+" "+len);
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



// Incoming messages are redirected to here when a game world is being down loaded to client.
// This is not used any more.
MirrorDb.prototype.onMessageArg=function(arg)
{
	var i=0;
	var cmd=arg[0];
	var reply='';

	//console.log("empWin.inputState1("+this.stateDepth+"): '" +arg+"'"); 

	if (cmd=="{")
	{
		this.stateDepth++;
	}
	else if (cmd=="}")
	{
		this.stateDepth--;	

		if (this.latestObj!=null)
		{
			this.latestObj=this.latestObj.parent;
		}
		else
		{
			console.log("latestObj is null");
		}

		if ((this.stateDepth==0) || (this.latestObj==null))
		{			
			// tmp test, remove later
			if (this.stateDepth!=0)
			{
				console.log("stateDepth!=0"); 
			}

			if (this.latestObj!=null)
			{
				console.log("latestObj!=null"); 
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
			this.rootObj=MirrorFactory(this, null, eType, arg.slice(1));
			this.latestObj=this.rootObj;
			if (this.latestObj==null)
			{
				console.log("new latestObj failed, unexpected latestObj==null"); 
			}
		}
		else
		{
			var eType=arg[0];
			this.latestObj = this.latestObj.addChild(eType, arg.slice(1));
			if (this.latestObj==null)
			{
				console.log("addChild failed, unexpected latestObj==null"); 
			}
		}
	}
	else
	{
		console.log("unknown cmd '"+cmd+"'"); 
	}
	return this;
}


// This shall be called when a mirror message was received
// Returns non zero if message was handled here, 0 otherwise.
MirrorDb.prototype.onMirrorMessage=function(arg)
{
	//console.log("onMirrorMessage ", arg);


	var cmd=arg[0];
	if ((cmd=="mirrorUpdate") || (cmd=="mirrorAdd"))
	{
		var id=parseInt(arg[1]);
		var parentId=parseInt(arg[2]);
		var objType=arg[3];

		if (id in this.byId)
		{
			// update an existing object
			var u = this.byId[id];

			u.updateSelf(parentId, objType, arg.slice(4));
			//this.needRedraw(); // on second thought, will let the server send a special message to trigger redrawing.
		}
		else
		{
			// add a new object

			// does it have a parent?
			if (parentId in this.byId)
			{
				// yes, get the parent object
				var p = this.byId[parentId];

				console.log("this.mirrorUpdate: add new object "+id+" "+parentId+" "+objType);

				p.addChild(objType, arg.slice(4));

				//this.needRedraw();
			}
			else
			{
				// There is no parent

				if (parentId>=0)
				{				
					// This shall not happen, server shall send parent before child.
					console.log("unknown parent "+parentId+" for object "+id);
				}

				// No parent object. But perhaps it is the top most object?
				if (this.rootObj==null)
				{
				
					// This is the top most object in our database, that is why it has no parent, OK.
					this.rootObj=MirrorFactory(this, null, objType, arg.slice(4));
				}
				else
				{	
					// This shall not happen, we already have a top most object in our database.
					console.log("can't add "+id+" "+" because "+parentId+ " is not known");
				}
			}
		}
		return 1;				
	}
	else if (cmd=="mirrorRemove")
	{
		if (arg.length>=2)
		{
			var id=parseInt(arg[1]);
			if (id in this.byId)
			{
				var u = this.byId[id];

				console.log("mirrorRemove: "+id+" "+u.selfToString());

				u.unlinkSelfFromWorld();

				//this.needRedraw();			
			}
			else
			{
				console.log("mirrorRemove: did not find unit id "+id+" in this.empDb.byId");
			}					
		}
		else
		{
			doError("to few arguments for removeUnit");
		}		
		return 1;				
	}

	return 0;

}

MirrorDb.prototype.debugDump=function(prefix)
{
	console.log("debugDump");
	if (this.rootObj!=null)
	{
		this.rootObj.debugDump(prefix+" ");
	}
	else
	{
		console.log("rootObj is null");
	}
}