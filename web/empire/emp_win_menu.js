// emp_win_menu.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-01-11 by Henrik Bjorkman www.eit.se/hb

EmpWinMenu.prototype = Object.create(EmpWinBase.prototype);
EmpWinMenu.prototype.constructor = EmpWinMenu;





function EmpWinMenu(parentWin)
{
	EmpWinBase.call(this, parentWin); // call super constructor

	this.parentWin=parentWin;
}


EmpWinMenu.prototype.mapUpdateRightTextArea=function()
{
	if (empWin.nSelected()>0)
	{
		var strInfo=""+empWin.nSelected();
		this.mapSetUpperTextArea2(strInfo);
	}
	else
	{
		this.mapSetUpperTextArea2('');
	}


	var w=empWin.empDb.empireWorld;
	if (w!=null)
	{
		this.mapSetUpperTextArea3(w.gameSpeed);
		this.mapSetUpperTextArea4(w.getState().savedMoney);
	}

}



EmpWinMenu.prototype.mapSetUpperTextAreaN=function(areaName, msg)
{
	var e = document.getElementById(areaName);
	if (e != null)
	{
		e.value=hlibRemoveQuotes(msg);
		e.scrollTop = e.scrollHeight;
	}
}


EmpWinMenu.prototype.mapSetUpperTextArea1=function(msg)
{
	this.mapSetUpperTextAreaN("upperTextArea1", msg);
}

EmpWinMenu.prototype.mapSetUpperTextArea2=function(msg)
{
	this.mapSetUpperTextAreaN("upperTextArea2", msg);
}

EmpWinMenu.prototype.mapSetUpperTextArea3=function(msg)
{
	this.mapSetUpperTextAreaN("upperTextArea3", msg);
}

EmpWinMenu.prototype.mapSetUpperTextArea4=function(msg)
{
	this.mapSetUpperTextAreaN("upperTextArea4", msg);
}


EmpWinMenu.prototype.mapUpdateUpperTextArea=function()
{
	var strInfo='';

	if (this.mapSelection != null)
	{
		// show info about this unit
		strInfo=this.mapSelection.selfToString();
		/*
	    if (this.mapSelection instanceof EmpSector)
		{
			var n=this.mapSelectionSector.getNChildUnits();
			if (n>0)
			{
				strInfo+='. there are '+n+' units in sector';
			}
			else
			{
				strInfo+='. empty sector';
			}
		}*/
	}
	else
	{
		//strInfo+='';
	}

	if (this.mapOrder != null)
	{
		if (empWin.nSelected()>0)
		{
			if (this.mapOrder == "moveTo")
			{
				strInfo+='  select a sector for selected units to move to';				
			}
			else if (this.mapOrder == "goTo")
			{
				strInfo+='  select a unit for selected units to go aboard';
			}
		}
		else
		{
			strInfo+='  select units for '+this.mapOrder;
		}
	}



	this.mapSetUpperTextArea1(strInfo);
}


EmpWinMenu.prototype.mapSetButtonArea=function(d)
{
	if (document.getElementById('moveButton')!=null)
	{
		document.getElementById('moveButton').disabled=d;
		document.getElementById('boardButton').disabled=d;
		document.getElementById('unselectButton').disabled=d;
	}
}
	
	
EmpWinMenu.prototype.mapUpdateButtonArea=function()
{
	if (empWin.nSelected()>0)
	{	
		this.mapSetButtonArea(null);
	}
	else
	{
		this.mapSetButtonArea('disabled');
	}
}
	

EmpWinMenu.prototype.mapUpdateUpperTextAreas=function()
{
	this.mapUpdateUpperTextArea();
	//mapUpdateLeftTextAreas();
	this.mapUpdateRightTextArea();
	this.mapUpdateButtonArea();
}


EmpWinMenu.prototype.defineASide=function(subWinSize)
{
	var newPage='';

	// The list of buttons to the left
	newPage+='<aside id="buttonList" class="center" style="width:'+subWinSize.x+';float:left;">';
	
	newPage+='<input type="text" id="upperTextArea2" class=emptext size="10" readOnly="yes"></br>';
	newPage+='<input class=empbutton type="button" value="clear" onclick="empWin.mapMemoryClear()"></br>';
	newPage+='<p>'
	newPage+='<input class=empbutton id="mapButton"         type="button" value="map"      onclick="empWin.mapSetShowState(0)"></br>';
	newPage+='<input class=empbutton id="moneyButton"       type="button" value="money"    onclick="empWin.mapSetShowState(1)"></br>';
	newPage+='<input class=empbutton id="terminalButton"    type="button" value="terminal" onclick="empWin.mapSetShowState(5)"></br>';
	newPage+='<input class=empbutton id="menuButton"        type="button" value="menu"     onclick="empWin.mapSetShowState(6)"></br>';
	
	newPage+='<input class=empbutton id="zoomInButton"  type="button" value="zoomIn" onclick="empWin.mapZoom*=2; empWin.mapSetShowState(2)"></br>';		
	newPage+='<input class=empbutton id="zoomOutButton"  type="button" value="zoomOut" onclick="empWin.mapZoom/=2; empWin.mapSetShowState(2)"></br>';		
	newPage+='<p>'
	newPage+='<input class=empbutton id="mapQuitButton"     type="button" value="quit"     onclick="empWin.mapCanvasCancel()"><br>';
	newPage+='<p>'
	newPage+='<input type="text" id="upperTextArea3" class=emptext size="10" readOnly="yes"></br>';
	newPage+='<input type="text" id="upperTextArea4" class=emptext size="10" readOnly="yes"></br>';

	newPage+='<p>'
	newPage+='<a href="empire/help.html" target="_blank">help</a>';

	newPage+='</aside>';


	return newPage;
}

EmpWinMenu.prototype.defineCentralArea=function(subWinSize)
{
	var newPage='';

	// The central area of the page	
	newPage+='<div id="buildDiv" style="width:'+subWinSize.x+'px; height:'+subWinSize.y+'px; overflow-x: scroll; overflow-y: scroll;">';

	newPage+='<div class="center" style="float:left;">';
	newPage+='<input type="text" id="upperTextArea2" class=emptext size="10" readOnly="yes"></br>';
	if (this.parentWin.mobileMode)
	{
		newPage+='<input class=empbutton type="button" value="clear" onclick="empWin.mapMemoryClear()"></br>';
		newPage+='<p>'
		newPage+='<input class=empbutton id="mapButton"         type="button" value="map"       onclick="empWin.mapSetShowState(0)"></br>';
		newPage+='<input class=empbutton id="moneyButton"       type="button" value="money"     onclick="empWin.mapSetShowState(1)"></br>';
		newPage+='<input class=empbutton id="terminalButton"    type="button" value="terminal"  onclick="empWin.mapSetShowState(5)"></br>';
		newPage+='<input class=empbutton id="textinputButton"   type="button" value="enterText" onclick="empWin.mapSetShowState(7)"></br>';
		newPage+='<input class=empbutton id="desktopButton"  type="button" value="desktopMode" onclick="empWin.mobileMode=0; empWin.mapSetShowState(6)"></br>';
		newPage+='<input class=empbutton id="zoomInButton"  type="button" value="zoomIn" onclick="empWin.mapZoom*=2; empWin.mapSetShowState(0)"></br>';		
		newPage+='<input class=empbutton id="zoomOutButton"  type="button" value="zoomOut" onclick="empWin.mapZoom/=2; empWin.mapSetShowState(0)"></br>';		
		newPage+='<p>'
		newPage+='<input class=empbutton id="mapQuitButton"     type="button" value="quit"       onclick="empWin.mapCanvasCancel()"><br>';
		newPage+='<p>'
		newPage+='<a href="empire/help.html" target="_blank">help</a>';
	}
	else
	{
		newPage+='<input class=empbutton id="mobileButton"  type="button" value="mobileMode" onclick="empWin.mobileMode=1; empWin.mapSetShowState(6)"></br>';		
	}

	
	newPage+='<p>'
	newPage+='<input type="text" id="upperTextArea3" class=emptext size="10" readOnly="yes"></br>';
	newPage+='<input type="text" id="upperTextArea4" class=emptext size="10" readOnly="yes"></br>';
	newPage+='</div>';
	
	
	newPage+='</div>';


	
	return newPage;
}

EmpWinMenu.prototype.addEventListeners=function()
{
	//this.mapAddEventListenerForMyCanvas("myCanvas");
}

EmpWinMenu.prototype.drawSubWin=function()
{
}


EmpWinMenu.prototype.click=function(mouseUpPos)
{
}

