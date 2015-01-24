



function chatRoomOpen(headingText)
{
        document.title = clientVersion;

        // Registrating our own handler for data from server, need to be restored if exiting chatRoom
	websocket.onmessage = function(evt) { chatRoomOnMessage(evt) };


	$("body").empty();

	console.log('chatRoomOpen '+headingText);

	$("body").append('<h1>'+headingText+'</h1><br>');

        // A text area for messages from server
	$("body").append('<textarea id="textArea" cols="80" rows=24 readOnly="yes"></textarea><br/>');

        // A text input area for commands from user
	$("body").append('<input type="text" id="inputText" size="70" onchange="chatRoomSendText(\'inputText\')"><br/>');

	// some buttons
	$("body").append('<input type="button" value=enter onclick="chatRoomSendText(\'inputText\')">');
	$("body").append('<input type="button" value=cancel onclick="chatRoomTextCancel()"><br>');

}



function chatRoomOnMessage(evt)
{
        //str=getAsciiGraphPartOnly(evt.data);
        var str=evt.data;
	//var arg=str.split(' ');
	var arg=hlibSplitString(str);
	var i=0;
        var cmd=arg[0];
        var reply='';

	console.log("chatRoomOnMessage: " + str); 

	// Here we check what server want client program to do.
	// Commands sent by ConnectionThread need to be interpreted here.
	if (cmd=="return")
	{
		websocket.onmessage = function(evt) { onMessage(evt) };
	}
	else if (cmd=="TextBoxAppend")
	{
		chatRoomTextBoxAppend(arg[1]);
	}

}



function chatRoomTextBoxAppend(msg)
{
	var e = document.getElementById("textArea");

	e.value+=hlibRemoveQuotes(msg)+"\n";
	e.scrollTop = e.scrollHeight;
}


function chatRoomTextBoxClear()
{
	var e = document.getElementById("textArea");

	e.value="";
	e.scrollTop = e.scrollHeight;
}


function chatRoomSendText(id)
{
	var e = document.getElementById(id);
	var str=e.value;
	console.log("chatRoomSendText: " + str); 
	//doSend('textMsg "'+hlibAddBackslash(str)+'"');
        websocket.send('textMsg "'+hlibAddBackslash(str)+'"');
	e.value="";
}

function chatRoomTextCancel()
{
	doSend('cancel');
	websocket.onmessage = function(evt) { onMessage(evt) };
}




