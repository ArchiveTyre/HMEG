// emp_win_terminal.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

EmpWinTerminal.prototype = Object.create(EmpWinBase.prototype);
EmpWinTerminal.prototype.constructor = EmpWinTerminal;




function EmpWinTerminal(parentWin)
{
	EmpWinBase.call(this, parentWin); // call super constructor
	this.count=0;
}

EmpWinTerminal.prototype.defineCentralArea=function(subWinSize)
{
	var newPage='';

	// The central area of the page	
	newPage+='<div style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';

	
	// The console 
	//newPage+='<div>';
	newPage+='<textarea id="consoleArea" class=emptext rows=24 readOnly="yes"></textarea><br/>'; // This creates the console text area
	
	if (!this.parentWin.mobileMode)
	{
		newPage+='<input type="text" id="inputText" size="88" onchange="empWin.empWinConsole(\'inputText\')"><br/>'; // This is where text can be written by user
		newPage+='<input type="button" value=enter onclick="empWin.empWinConsole(\'inputText\')">';
		newPage+='<input type="button" value=cancel onclick="empWin.mapCanvasCancel()">';
		newPage+='<input type="button" value=back onclick="empWin.mapSetShowState(0)"><br>';
	}
	//newPage+='</div>';


	
	newPage+='</div>';



	return newPage;
}

EmpWinTerminal.prototype.addEventListeners=function()
{
	//parentWin.mapAddEventListenerForMyCanvas();
	
	if (empWin.empDb!=null)
	{

		var cl=empWin.empDb.getEmpireWorld().getEmpireStatesList().children;
		var n=cl[empWin.mapNation];	
		if (n!=null)
		{
			var r=n.eRoundBuffer;
			this.count=r.head;
		}
	}
	
	
}

EmpWinTerminal.prototype.drawSubWin=function()
{
	if (empWin.empDb!=null)
	{

		var cl=empWin.empDb.getEmpireWorld().getEmpireStatesList().children;
		var n=cl[empWin.mapNation];	
		if (n!=null)
		{

			// redraw the messages text area
			//console.log("EmpWinTerminal.prototype.drawSubWin "+n.selfToString());
			var r=n.eRoundBuffer;
			//console.log("EmpWinTerminal.prototype.drawSubWin "+r.selfToString());
			//r.showSelfOnTextArea("textArea"); // will print on the messages text area
			


			// Add latest messages also to console text area
			// Is this usefull? Perhaps we should keep messages only in the message text area
			var i=0; 
			while((this.count!=r.tail) && (i<r.maxObjects)) 
			{
				if (this.count in r.children)
				{
					var c = r.children[this.count];
					//var str = c.selfToString();
					var str= c.objName+": "+c.order;
					r.textBoxAppend('consoleArea', str);			
				}
		
				this.count++;
				if (this.count>=r.maxObjects*2)
				{
					this.count=0;
				}
				++i;
			}


		}
		else
		{
			console.log('no nation '+empWin.mapNation);
		}			
	}
}


EmpWinTerminal.prototype.click=function(mouseUpPos)
{
	console.log("not implemented yet");
}


EmpWinTerminal.prototype.empConsoleBoxAppend=function(msg)
{
	var e = document.getElementById("consoleArea");
	if (e!=null)
	{
		e.value+=hlibRemoveQuotes(msg)+"\n";
		e.scrollTop = e.scrollHeight;
	}
	else
	{
		console.log(msg);
	}
}

