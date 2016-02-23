// hmeg_win.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-02-19 by Henrik Bjorkman www.eit.se/hb



HmegWin.prototype = Object.create(DivBase.prototype);
HmegWin.prototype.constructor = HmegWin;









function HmegWin(headingText)
{
	DivBase.call(this, null); // call super constructor

	this.hmegDb=new HmegDb();

	this.sectorWidth=32;
	this.sectorHeight=32;
	
	this.mouseDownPos;

	this.headingText=headingText;


	this.subWin = null;
	this.msgWin = null;

}	
	

HmegWin.prototype.init=function()
{
	this.subWin = new HmegMapWin(this);
	this.msgWin = new HmegWinMsg(this);
}	

// This creates (make HTML layout) for the main window page of this game
// The main window contains 3 sub windows (AKA DIVs)
// * header on the to of the window
// * middle, different things can be shown here such as the world map.
// * messages text area in the lower part
HmegWin.prototype.defineDiv=function()
{
	console.log("defineDiv");

	var msgAreaHeight=120;
	var scrollbarSize=8;

	
	if (msgAreaHeight*4>window.innerHeight)
	{
		msgAreaHeight=Math.floor(window.innerHeight/4);
	}


	var subWinSize={x: window.innerWidth-scrollbarSize, y: window.innerHeight-msgAreaHeight-scrollbarSize};
	var msgWinSize={x: window.innerWidth-scrollbarSize, y: msgAreaHeight-scrollbarSize};


	var newPage='';


	document.title = this.headingText + ' - ' + clientVersion;

	

	console.log('defineDiv');

	


	newPage+='<section>';

	newPage+=this.subWin.defineDiv(subWinSize);

	newPage+=this.msgWin.defineArea(msgWinSize);
	
	newPage+='</section>';


	$("body").empty();
	$("body").append(newPage);


		
		
	// Remember our main window	
	//rootDiv.element = document.getElementById('myCanvas');

}

HmegWin.prototype.defineAndDrawPage=function()
{
			this.defineDiv();
			this.addEventListenersDiv();
			this.drawDiv();
}	


HmegWin.prototype.addEventListenersDiv=function()
{
	this.subWin.addEventListenersDiv();
	this.msgWin.addEventListenersDiv();
	var t=this;
	document.onkeypress=function(event)
	{
		console.log('keypress: ' + event.which);
		
		switch(event.which)
		{
			case 101:
				console.log('keypress: inventory');
				
				t.subWin = new HmegWinInventory(t);
				t.defineAndDrawPage();
			default:
				if (event.which<32)
				{
					console.log('keypress: return');
				
					t.subWin = new HmegMapWin(t);
					t.defineAndDrawPage();

				}
				else
				{
				        websocket.send('keypress "'+event.which+'"');
				}
		        break;
		}
		
	}
}

HmegWin.prototype.drawDiv=function()
{
	this.subWin.drawDiv();
	this.msgWin.drawWin();
}

HmegWin.prototype.empWinConsole=function(srcId)
{
	var theSrcTextBox = document.getElementById(srcId);
	var str=theSrcTextBox.value;
	doSend('textMsg "'+str+'"');
	theSrcTextBox.value="";
}
	
	

// This handles all input from server when this game is running in server.	
HmegWin.prototype.onMessageArg=function(arg)
{
	var i=0;
	var cmd=arg[0];
	var reply='';


	//console.log("onMessageArg: '" +arg+"'"); 

	// Here we check what server want client program to do.
	// Commands sent by ConnectionThread need to be interpreted here.


	if (this.hmegDb.onMirrorMessage(arg)!=0)
	{
		// Message was handled by mirror
	}
	else if (cmd=="mirrorUpdated")
	{
		// The server sends this when it has finished sending a batch of updates.
		// When this is received it it time to redraw the map.			
		if (this.hmegDb!=null)
		{
			this.drawDiv();
		}
		doSend('mirrorAck ' + arg[1]);
	}
	else if (cmd=="return")
	{
		empireClose();
	}
	else if (cmd=="consoleMessage")
	{
		this.msgWin.textBoxAppend(arg[1]);
	}
	else if (cmd=="empConsoleAppend") // Deprecated, use consoleMessage
	{
		this.msgWin.textBoxAppend(arg[1]);
	}
	else if (cmd=="showWorld")
	{
		// When this command is received client will define the main game window (and its initial sub windows AKA div)			
		if (this.hmegDb!=null)
		{
			this.defineAndDrawPage();
		}
	}
	else if (cmd=="hmegShow") // Deprecated, use showWorld
	{			
		if (this.hmegDb!=null)
		{
			//this.hmegDb.debugDump(" ");
			
			this.defineAndDrawPage();
		}
	}
	else if (cmd=="avatarId")
	{
		// This tells the client which object it shall view the world from.
		this.avatarId = parseInt(arg[1]);
	}
	else
	{
		console.log("onMessageArg: unknown command: '" + cmd+"'"); 
		//empireClose();
	}	
}



