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

// This creates the main web page for empire game
HmegWin.prototype.defineDiv=function()
{
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

	
// Is this used?
HmegWin.prototype.empWinConsole=function(srcId)
{
	var theSrcTextBox = document.getElementById(srcId);
	var str=theSrcTextBox.value;
	doSend('textMsg "'+str+'"');
	theSrcTextBox.value="";
}
	
	

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
		if (this.hmegDb!=null)
		{
			this.drawDiv();
		}
		doSend('mirrorAck ' + arg[1]);
	}
	else if (cmd=="mirrorWorld") // TODO: this command should be moved to mirror. But shall it be in MirrorDb or a new MirrorWin.js? It creates a new EmpDb which is a problem in moving it.
	{
		this.hmegDb=new EmpDb();
		this.subInputState = this.hmegDb;
	}
	else if (cmd=="return")
	{
		empireClose();
	}
	else if (cmd=="empConsoleAppend")
	{
		this.msgWin.textBoxAppend(arg[1]);
	}
	else if (cmd=="hmegShow")
	{			
		if (this.hmegDb!=null)
		{
			//this.hmegDb.debugDump(" ");
			
			this.defineAndDrawPage();
		}
	}
	else if (cmd=="empWorldClose")
	{			
		console.log("onMessageArg: this.empWorldClose:"); 
		empireClose();
	}
	else if (cmd=="avatarId")
	{			
		this.avatarId = parseInt(arg[1]);	
	}
	else
	{
		console.log("onMessageArg: unknown command: '" + cmd+"'"); 
		//empireClose();
	}	
}



