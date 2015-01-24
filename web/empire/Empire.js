// Empire.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-12-26 by Henrik Bjorkman www.eit.se/hb


var empWin=null;


function empireOpen(headingText)
{
	document.title = clientVersion;

	empWin=new EmpWin();
	empWin.init();

	// Register our own handler for data from server, need to be restored if exiting empire
	websocket.onmessage = function(evt) { empireOnMessage(evt) };

	empWin.mapInitData();
	empWin.mapHeadingText=headingText;
	//empWin.mapOpenCreateCanvas(headingText);
	empWin.mapSetShowState(0);
	empWin.mapClearSelections();

}


function empireClose()
{
	websocket.onmessage = function(evt) { onMessage(evt) };
}


function empireOnMessage(evt)
{
	var str=evt.data;

	if ((str==='undefined') || (str==null) || (str.length<=0))
	{
		console.log("empireOnMessage("+empWin.inputState+"): undefined, null or zero length"); 	
	}
	else
	{
		//console.log("empireOnMessage("+empWin.inputState+"): " + str); 	

		var arg=hlibSplitString(str);

		empWin.onMessageArg(arg);
	}
}





