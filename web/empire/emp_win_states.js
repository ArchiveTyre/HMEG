// emp_win_sector.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-31 by Henrik Bjorkman www.eit.se/hb


EmpWinStates.prototype = Object.create(EmpWinBase.prototype);
EmpWinStates.prototype.constructor = EmpWinStates;




function EmpWinStates(parentWin)
{
	EmpWinBase.call(this, parentWin); // call super constructor

	this.parentWin=parentWin;
}

EmpWinStates.prototype.defineCentralArea=function(subWinSize)
{
	var newPage='';

	newPage+='<div style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';

	// The central area of the page	
	newPage+='<div style="width:400px;height:460px;float:left;">';
	newPage+='<canvas id="myCanvas" width="'+(subWinSize.x-32)+'" height="'+(subWinSize.y-32)+'"></canvas>';
	newPage+='</div>';

	newPage+='</div>';

	return newPage;
}

EmpWinStates.prototype.addEventListeners=function()
{
	this.mapAddEventListenerForMyCanvas("myCanvas");
}

EmpWinStates.prototype.drawSubWin=function()
{
	if (empWin.empDb!=null)
	{
		var element=document.getElementById("myCanvas");
		var context=element.getContext("2d");
		context.clearRect(0, 0, element.width, element.height);

		/*
		var len = empWin.empDb.nationList.children.length
		for (var i=0; i<len; ++i) 
		{
			if (i in empWin.empDb.nationList.children) 
			{
				var c = empWin.empDb.nationList.children[i];
				//var str=c.selfToString();
				var str=c.index+" "+c.money+" "+c.moneyChange;
		
				context.font = '8pt Calibri';
				context.fillStyle = 'black';
				context.fillText(str, 2, 12+i*12);

			}
		}*/
		
		
		var cl=empWin.empDb.getEmpireWorld().getEmpireStatesList().children;
		if (cl!=null)
		{
			var i;
			for(i in cl)
			{
				//console.log("EmpWinStates: for "+i+" "+cl[i].selfToString());
				context.font = '8pt Calibri';
				context.fillStyle = 'black';

				var c = cl[i];
				if (c instanceof EmpState)
				{
					var str=c.index+" "+c.money+" "+c.moneyChange;
			
					context.fillText(str, 2, 12+i*12);
				}
				else
				{
					context.fillText(c.selfToString(), 2, 12+i*12);
				}
			}
		}

		
	}
}

EmpWinStates.prototype.click=function(mouseUpPos)
{
	console.log("not implemented yet");
	empWin.mapSetShowState(0);
	
}


