// hmeg_main.js
// Copyright (C) 2015 Henrik Bjorkman www.eit.se/hb
// Created 2015-02-19 by Henrik Bjorkman www.eit.se/hb




function hmegOpen(headingText)
{
	document.title = clientVersion;

	rootDiv=new HmegWin(headingText);
	rootDiv.init();

	// Register our own handler for data from server, need to be restored if exiting empire
	websocket.onmessage = function(evt) { hmegOnMessage(evt) };
}


function hmegClose()
{
	websocket.onmessage = function(evt) { onMessage(evt) };
}


function hmegOnMessage(evt)
{
	var str=evt.data;

	if ((str==='undefined') || (str==null) || (str.length<=0))
	{
		console.log("hmegOnMessage("+rootDiv.inputState+"): undefined, null or zero length"); 	
	}
	else
	{
		var arg=hlibSplitString(str);
		rootDiv.onMessageArg(arg);
	}
}





