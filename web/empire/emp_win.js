// emp_win.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-03-14 by Henrik Bjorkman www.eit.se/hb










function EmpWin()
{
	this.subInputState=null;
	this.empDb=new EmpDb();

	this.mobileMode=null;
	this.mapZoom=1;

	this.mapSectorWidth=16*2;
	this.mapSectorHeight=14*2;
	
	
	this.mapIntervalVariable;
	this.mapMouseDownPos;
	this.mapHeadingText;
	this.mapOrder=null;
	
	
	this.mapSelection=null;
	this.selectionList=[];
	this.nSelections=0;

	this.empWinUnit = null;
	this.empWinBuild = null;
	this.empWinNations = null;
	this.empWinSectors = null;
	this.empWinTerrain = null;
	this.empWinTerminal = null;
	this.empWinMenu = null;
	this.empWinTextinput = null;

	this.subWinStateObj = null;
	
	this.mapNation=-1; // an integer index, to tell which state this player control. 
	
	
	
	this.init=function()
	{
		this.sectorTypesList=new EmpSectorTypeList();
		this.mapInitData();

		this.empWinUnit = new EmpWinUnit(this);
		this.empWinBuild = new EmpWinBuild(this);
		this.empWinNations = new EmpWinStates(this);
		this.empWinSectors = new EmpWinSectors(this);
		this.empWinTerrain = new EmpWinTerrain(this);
		this.empWinTerminal = new EmpWinTerminal(this);
		this.empWinMenu = new EmpWinMenu(this);
		this.empWinTextinput = new EmpWinTextinput(this);
		this.empWinMsg = new EmpWinMsg(this);
		this.subWinStateObj=this.empWinTerrain;
	}


	//this.buttonMode=1; // 0 = don't auto change window, 1 = change window directly
	



	
	
	
	
	
	
	this.mapClearSelections=function()
	{
		this.mapOrder=null;
		this.mapSelection=null;
		this.empWinMenu.mapUpdateUpperTextAreas();
		this.clearSelectionList();
	}	
	
	
	this.mapInitData=function()
	{
		//terrainMap=new terrainClass2(32,32,5, 0);
		//unitTypeNames=new Array();
		//knownUnits=new Array();
	
		this.mapIntervalVariable=null;
		this.mapMouseDownPos=null;
		this.mapHeadingText=null;
		this.mapOrder=null;
		this.mapSelection=null;
		this.clearSelectionList();	
	}
	
	

	// This creates the main web page for empire game
	this.mapOpenCreateCanvas=function()
	{
		var msgAreaHeight=120;
		var scrollbarSize=8;
		var buttonListWidth=0;
		
		if (this.mobileMode==null)
		{
			if (window.innerWidth>window.innerHeight)
			{
				this.mobileMode=0;
				//this.mapZoom=1;
			}
			else
			{
				this.mobileMode=1;			
				//this.mapZoom=2;
			}
		}
		
		
		if (msgAreaHeight*4>window.innerHeight)
		{
			msgAreaHeight=Math.floor(window.innerHeight/4);
		}
		
		if (!this.mobileMode)
		{
			//buttonListWidth=Math.floor(window.innerWidth*0.08);
			buttonListWidth=100;
		}

		this.mapSectorWidth=(32*this.mapZoom);
		this.mapSectorHeight=(28*this.mapZoom);

		var buttonWinSize={x: buttonListWidth-scrollbarSize, y: window.innerHeight-msgAreaHeight-scrollbarSize};
		var subWinSize={x: window.innerWidth-buttonListWidth-scrollbarSize, y: window.innerHeight-msgAreaHeight-scrollbarSize};
		var msgWinSize={x: window.innerWidth-scrollbarSize, y: msgAreaHeight-scrollbarSize};

		/*$( window ).resize(
			function() {empWin.mapSetShowState(0);
		});*/

		/*
		$('html, body').css({
		    'overflow': 'hidden',
		    'height': '100%'
		});
		*/
	
		var newPage='';


		document.title = empWin.headingText + ' - ' + clientVersion;

		//console.log('mapOpenCreateCanvas');

		

		//newPage='<div style="width:99%;">';
	
		if (buttonListWidth>0)
		{
			// The upper text area, shows info about selected unit and hints to user
			newPage+='<header>';	
			newPage+='<input type="text" id="upperTextArea1" class=emptext size="90" readOnly="yes"><br/>';
			newPage+='</header>';

			// Buttons to the left and map etc to the right here	
			newPage+=empWin.empWinMenu.defineASide(buttonWinSize);
	

			newPage+='<section>';
			newPage+=empWin.mapGetStateHandler().defineCentralArea(subWinSize);

			newPage+=empWin.empWinMsg.defineArea(msgWinSize);
			newPage+='</section>';
			
			/*
			newPage+='<footer>';
			//newPage+='<a href="http://www.eit.se/" target="_blank">www.eit.se</a>';
			newPage+='<a href="help.html" target="_blank">help</a>';
			newPage+='</footer>';
			*/
			
		}
		else
		{
	
			newPage+=empWin.mapGetStateHandler().defineCentralArea(subWinSize);
		
			newPage+=empWin.empWinMsg.defineArea(msgWinSize);
		
		}

	
		$("body").empty();
		$("body").append(newPage);
	
	
		empWin.mapGetStateHandler().addEventListeners();
			
			
	
/*		empWin.mapIntervalVariable = window.setInterval(function (evt) {
			doSend('tick');
		}, (5*60*1000));*/
	
	
	
	
	
	
		empWin.element = document.getElementById('myCanvas');
	
	}



	
	
	this.empWinConsole=function(srcId)
	{
		var theSrcTextBox = document.getElementById(srcId);
		var str=theSrcTextBox.value;
		doSend('textMsg "'+str+'"');
		theSrcTextBox.value="";
	}
	
	
	
	
	this.mapSetShowState=function(newState)
	{
		if (newState==0)
		{
			// state 0 is deprecated, set this.mapSelection to null and use newState 2 instead.
			this.mapSelection=null;
			newState=2;
		}
		
		if (newState==3) 
		{
			console.log('state 3 is deprecated, use 2 instead');
			newState=2;
		}

		
		this.subWinStateObj=this.mapGetStateHandlerObj(newState);
		this.mapOpenCreateCanvas();
		this.drawWin();
	}


	
	this.mapMoveOrder = function()
	{
		console.log("mapMoveOrder");
		if (this.nSelected()==0)
		{
			this.mapSetUpperTextArea1("select a unit first'");
		}
		else
		{
			//var element=document.getElementById("myCanvas");
	
			this.mapOrder='moveTo';
			this.mapSelection=null;			
			this.empWinMenu.mapSetUpperTextArea1("click on the destination to move to (on the map)");
			this.mapSetShowState(0);
			//this.mapGetStateHandler().drawSubWin();
			this.empWinMenu.mapUpdateUpperTextAreas();
		}
	}





	this.mapMemoryClear = function()
	{
		console.log("mapMemoryClear");
		this.empWinTerrain.center();
		this.mapClearSelections();
	}


/*
	this.mapRemember=function()
	{
		if (this.mapSelection!=null)
		{
			console.log("mapRemember "+this.mapSelection.selfToString());
			this.addSelection(this.mapSelection.id);
			this.mapUpdateUpperTextAreas();
		}
		else
		{
			console.log("mapRemember null");
		}
	}
*/


	this.mapGoAboard=function()
	{
		if ((this.mapSelection!=null) && (this.nSelected()>0) && (this.mapOrder!=null))
		{
			for(i in this.selectionList)
			{						
				console.log('unitOrder '+ i + ' "'+this.mapOrder+' '+ this.mapSelection.id +'"');
				doSend('unitOrder '+ i + ' "'+this.mapOrder+' '+ this.mapSelection.id +'"');
			}
			//this.mapClearSelections();
			this.clearSelectionList();
			this.mapOrder=null;
		}
		else if ((this.mapSelection!=null) && (this.nSelected()>0))
		{
			for(i in this.selectionList)
			{		
				console.log('unitOrder '+ i + ' "board '+this.mapSelection.id+'"');
				doSend('unitOrder '+ i + ' "goTo '+this.mapSelection.id+'"');
			}
			//this.mapClearSelections();
			this.clearSelectionList();
		}
		else if (this.nSelected()>0)
		{
			//this.mapRemember();
			this.mapOrder='goTo';
			this.mapSetUpperTextArea1("now select the unit to board, then press 'go aboard' again");
		}
		else
		{
			this.mapSetUpperTextArea1("select a unit first'");
		}
	}


	this.mapCanvasCancel=function() 
	{
		//empWinConsole=false;
		this.mapSetShowState(0);
		this.mapMemoryClear();
		this.mapClearSelections();
	
		$("p").empty();
		window.clearInterval(this.mapIntervalVariable)
	        document.title = this.mapHeadingText;
		doSend('cancel');
		empireClose();
	}

	/*
	this.mapWriteMessage = function(canvas, message) {
		var context = canvas.getContext('2d');
		context.clearRect(10, canvas.height-30, canvas.width, canvas.height);
		context.font = '8pt Calibri';
		context.fillStyle = 'black';
		context.fillText(message, 10, canvas.height-10);
	}
	*/

	this.mapSendOrderOne=function(theOrder)
	{
		if (this.mapSelection!=null)
		{
			doSend('unitOrder '+ this.mapSelection.id + ' "'+theOrder+'"');
			this.mapClearSelections();
		}
		else
		{
			this.mapSetUpperTextArea1("select a unit first");
		}
	}



	this.mapCancelOrder = function()
	{
		if (this.mapSelection!=null)
		{
			doSend('cancelOrder '+ this.mapSelection.id);
			//this.mapSelection=null;
			this.mapUpdateUpperTextAreas();
		}
		else
		{
			this.mapSetUpperTextArea1("select a unit first");
		}
	}


	/*
	this.mapTranslateMousePosToLine=function(mouseUpPos) {
		return Math.floor((mouseUpPos.y/this.mapSectorHeight)-0.2);
	}
	*/

	this.mapCalcOffSet=function(y)
	{
		var o=0;
		if ((y%2)!=0)
		{
			o=this.mapSectorWidth/2;
		}
		return(o);
	}




	this.mapGetStateHandlerObj=function(state)
	{
		switch (state)
		{
			default:
			case 2:
				// The big map
				if (empWin.mapSelection == null)
				{
				    console.log("EmpTerrain");
					return this.empWinTerrain;		
				}
				else if (empWin.mapSelection instanceof EmpSector)
				{
				    console.log("EmpSector");
					return this.empWinSectors;
				}
				else if (empWin.mapSelection instanceof EmpUnit)
				{
				    console.log("EmpUnit");
					return this.empWinUnit;
				}
				else
				{
				    console.log("EmpTerrain");
					return this.empWinTerrain;		
				}
			case 1:
				// Show relations with other states
				return this.empWinNations;
			case 4:
				// The build menu
				return this.empWinBuild;
			case 5:
				// The terminal for messages etc
				return this.empWinTerminal;
			case 6:
				// The menu to be used instead of side panel on mobile devices
				return this.empWinMenu;
			case 7:
				// To enter text on mobile devices.
				return this.empWinTextinput;

		}
	}
				

	this.mapGetStateHandler=function()
	{
		return this.subWinStateObj;
	}
	
	this.onMessageArg=function(arg)
	{
	    if (this.subInputState!=null)
	    {
	    	this.subInputState=this.subInputState.onMessageArg(arg);
	    }
	    else
	    {
		
			var i=0;
		        var cmd=arg[0];
		        var reply='';
		
		
			//console.log("onMessageArg: '" +arg+"'"); 
		
		
		
			// Here we check what server want client program to do.
			// Commands sent by ConnectionThread need to be interpreted here.
			if (cmd=="return")
			{
				empireClose();
			}
			else if (cmd=="empWorldUpdate")
			{
				// TODO: there is a problem with the worlds root object. In server it is
				// unclear what ID to use when updating that object. 
				// Currently it sends it as ID -1 and here we get:
				// "this.empWorldUpdate: can't add -1 EmpireWorld because -1 is not known"
				// Perhaps it should be sent with ID 0? Then perhaps this client don't need changing?
		
				var id=parseInt(arg[1]);
				var parentId=parseInt(arg[2]);
				var empType=arg[3];
		
				if (id in this.empDb.byId)
				{
					// update an existing object
					var u = this.empDb.byId[id];
	
					u.updateSelf(parentId, empType, arg.slice(4));
					//console.log("this.empWorldUpdate: "+id+" "+u.selfToString());
					//this.needRedraw();
				}
				else
				{
					// add a new object
		
					if (parentId in this.empDb.byId)
					{
						var p = this.empDb.byId[parentId];
		
						console.log("this.empWorldUpdate: add new object "+id+" "+parentId+" "+empType);
		
						p.addChild(empType, arg.slice(4));
		
						//this.needRedraw();
					}
					else
					{
						console.log("this.empWorldUpdate: can't add "+id+" "+empType+" because "+parentId+ " is not known");
					}
				}					
			}
			else if (cmd=="empRemoveUnit")
			{
				if (arg.length>=2)
				{
					var id=parseInt(arg[1]);
					if (id in this.empDb.byId)
					{
						var u = this.empDb.byId[id];
	
						console.log("empRemoveUnit: "+id+" "+u.selfToString());
	
						u.unlinkSelfFromWorld();
	
						//this.needRedraw();			
					}
					else
					{
						console.log("this.empWorldUpdate: empRemoveUnit: did not find unit id "+id+" in this.empDb.byId");
					}					
				}
				else
				{
					doError("to few arguments for removeUnit");
				}		
			}
			else if (cmd=="empConsoleAppend")
			{
				this.empWinTerminal.empConsoleBoxAppend(arg[1]);
			}
			else if (cmd=="empWorldUpdated")
			{			
				if (this.empDb!=null)
				{
					this.drawWin();
				}
			}
			else if (cmd=="empWorld")
			{
				this.empDb=new EmpDb();
				this.subInputState = this.empDb;
			}
			else if (cmd=="empWorldShow")
			{			
				if (this.empDb!=null)
				{
					this.mapOpenCreateCanvas();
					this.drawWin();
				}
			}
			else if (cmd=="joinEmpire")
			{			
				this.mapNation = parseInt(arg[1]);
				console.log("this.inputState0: joinEmpire: " + this.mapNation); 
			}
			else if (cmd=="empWorldClose")
			{			
				console.log("onMessageArg: this.empWorldClose:"); 
				empireClose();
			}
			else
			{
				console.log("onMessageArg: unknown command: '" + cmd+"'"); 
				//empireClose();
			}	
		}
    	return this;
	}
	
	this.drawWin=function()
	{
		this.mapGetStateHandler().drawSubWin();

		this.empWinMsg.drawWin();

		// show selected unit/sector etc
		this.empWinMenu.mapUpdateUpperTextAreas();	
	}


	this.clearSelectionList=function()
	{
		this.selectionList=[];
		this.nSelections=0;
	}	

	this.toggleSelection=function(id)
	{
		if (this.isSelected(id))
		{
			this.clearSelection(id);
		}
		else
		{
			this.addSelection(id);
		}		
	}

	this.clearSelection=function(id)
	{
		if (this.isSelected(id))
		{
			this.selectionList[id]=-1;		
			this.nSelections--;
		}
	}

	this.addSelection=function(id)
	{
		if (!this.isSelected(id))
		{
			this.selectionList[id]=id;
			this.nSelections++;
		}		
	}
	
	this.isSelected=function(id)
	{
		if (id in this.selectionList)
		{
			if (this.selectionList[id]>=0)
			{
				return true;
			}
		}
		return false;
	}
	
	this.nSelected=function()
	{
		return this.nSelections;
	}

}
