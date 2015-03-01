// emp_win_terminal.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb

DivTerminal.prototype = Object.create(DivBase.prototype);
DivTerminal.prototype.constructor = DivTerminal;




function DivTerminal(parentWin)
{
	DivBase.call(this, parentWin); // call super constructor
	this.count=null;
	this.text='';
	this.element=null;
	
	this.changeConter=0;
	this.previousChangeConter=0;
}

DivTerminal.prototype.defineDiv=function(divSize)
{
	var newPage='';

	// The central area of the page	
	newPage+='<div style="width:'+divSize.x+'px; height:'+divSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';

	
	// The console 
	//newPage+='<div>';
	newPage+='<textarea id="consoleArea" class=emptext rows=24 readOnly="yes"></textarea><br/>'; // This creates the console text area
	
	if (!this.parentWin.mobileMode)
	{
		newPage+='<input type="text" id="inputText" size="88" onchange="rootDiv.empWinConsole(\'inputText\')"><br/>'; // This is where text can be written by user
		newPage+='<input type="button" value=enter onclick="rootDiv.empWinConsole(\'inputText\')">';
		newPage+='<input type="button" value=cancel onclick="rootDiv.mapCanvasCancel()">';
		newPage+='<input type="button" value=back onclick="rootDiv.DivTerminal.back();"><br>';
	}
	//newPage+='</div>';


	
	newPage+='</div>';



	return newPage;
}


DivTerminal.prototype.addEventListenersDiv=function()
{
	this.element = document.getElementById("consoleArea");

	this.element.value+=this.text;

/*	if (rootDiv.empDb!=null)
	{
		var cl=rootDiv.empDb.getEmpireWorld().getEmpireStatesList().children;
		var n=cl[rootDiv.mapNation];	
		var r=n.eRoundBuffer;

		if (n!=null)
		{
	
			if (this.count==null)
			{
				this.count=r.head;
			}

			// Add latest messages also to console text area
			// Is this usefull? Perhaps we should keep messages only in the message text area
			var i=0; // this variable is just to avoid eternal loop if something is wrong 
			while((this.count!=r.tail) && (i<r.maxObjects)) 
			{
				if (this.count in r.children)
				{
					var c = r.children[this.count];
					var str= c.objName+": "+c.order;
					this.textBoxAppend(str);
				}
		
				this.count++;
				if (this.count>=r.maxObjects*2)
				{
					this.count=0;
				}
				++i;
			}

		}
	}*/
}

DivTerminal.prototype.drawDiv=function()
{
    if (this.previousChangeConter!=this.changeConter)
    {
		var e = this.element;
		e.scrollTop = e.scrollHeight;
		this.previousChangeConter=this.changeConter;
	}
}


DivTerminal.prototype.click=function(mouseUpPos)
{
	console.log("not implemented yet");
}

DivTerminal.prototype.back=function()
{
	this.text=this.element.value+"---\n";
	this.element=null;
	rootDiv.mapSetShowState(0)
}

DivTerminal.prototype.textBoxAppend=function(msg)
{
	var str=hlibRemoveQuotes(msg)+"\n";

	if (this.text!=null)
	{
		this.text += str;
	}
	if (this.element!=null)
	{
		var e = this.element;
		e.value+=str;
		e.scrollTop = e.scrollHeight;
	}

	this.changeConter++;
	
	console.log(str);
}

