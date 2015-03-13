// on.js
// Created 2014-03-14 by Henrik Bjorkman www.eit.se/hb

//var pingTimer=null;

function onTestWebSocket(wsUrl)
{

        // open the web socket
	websocket = new WebSocket(wsUrl);

        // This registers callbacks for web socket events
	websocket.onopen = function(evt) { onOpen(evt) };
	websocket.onclose = function(evt) { onClose(evt) };
	websocket.onmessage = function(evt) { onMessage(evt) };
	websocket.onerror = function(evt) { onError(evt) };
}


// this is called by system when connection is up
function onOpen(evt)
{
	console.log("CONNECTED: "+ Date());

        // Tell sever we wish to use web game protocol.
	doSend("rsb_web_game");
}

// this is called by system when connection is closed
function onClose(evt)
{
	console.log("DISCONNECTED: "+ Date());
	$("body").empty();
	$("body").append("DISCONNECTED: "+ Date());
}


// this is called by system if there is a problem with the connection
function onError(evt)
{
	console.log('<span style="color: red;">ERROR:</span> ' + evt.data); // html tags can be used when writing to screen
}



// This is perhaps the most interesting function in this file.
// It receives commands from the server program.
function onMessage(evt)
{
	var str=evt.data;
	var arg=hlibSplitString(str);
	var i=0;
	var cmd=arg[0];
	var reply='';

	//console.log("onMessage: " + evt.data); 

	/*console.log('<span style="color: blue;">arg.length: "' + arg.length +'"</span>');
	for (i=0; i<arg.length; i++)
	{
	  console.log('<span style="color: blue;">arg['+i+']: "' + arg[i] +'"</span>');
	}

	console.log('<span style="color: blue;">Command is: "' + cmd +'"</span>');*/


	//doSend("www.eit.se/rsb/0.6/client");

	// Here we check what server want client program to do.
	// Commands sent by ConnectionThread need to be interpreted here.
	if (cmd=="query")
	{
		// Server will expect the number of the button as reply.
		if (arg[3]=="buttonPrompt")
		{
			// Format for this querry is:
			// query <tag> <heading text> buttonPrompt <number of buttons> "<button names>" ...
			// vi ska fråga efter "button" men tills vidare...
	                var alt='buttons: ';
			var n=arg[4];
			var i=0;
	                for (i=0;i<n;i++)
			{
				// console.log('button '+n+' '+i+' '+arg[5+i]);
				alt=alt+'\n '+i+' '+arg[5+i];
			}

			console.log('doButtonQuery ' + arg[2] + ' ' + n);

			// Commented out here is one way to do it. But it don't look good.
	        	//reply=prompt(arg[2]+'\n'+alt, 'reply with button number here (not name)');
			//doSend(reply);	
		
			doButtonQuery(arg[2], arg.slice(5,4+n));

			//window.open();

		}
		else if (arg[3]=="promptString")
		{
			// Format for this querry is:
			// query <tag> <heading> promptString

			// Commented out here is one way to do it. But it don't look good.
	        	//reply=prompt(arg[2], 'reply with a string here');
			//reply='"'+reply+'"';
			//doSend(reply);

			if (arg[1]=="enter_player_pw")
			{
				// Format for this querry is:
				// query enter_player_pw <heading> promptString

				doPwQuery(arg[2]);
			}
			else if (arg[1]=="enter_player_name")
			{
				doNameQuery(arg[2]);
			}
			else
			{
				doTextQuery(arg[2]);
			}
		}
		else if (arg[3]=="promptInt")
		{
			// Format for this querry is:
			// query <tag> <heading> promptString

			// Commented out here is one way to do it. But it don't look good.
	        	//reply=prompt(arg[2], 'reply with a string here');
			//reply='"'+reply+'"';
			//doSend(reply);

			doIntQuery(arg[2]);
		}
		else if (arg[3]=="listPrompt")
		{
			console.log("listPrompt??? "+ arg[2]);
			doListQuery(arg[2]);
		}
		else
 		{
	        	reply=prompt(arg[2]+' '+arg[1], '');	
			doSend(reply);	
		}
	}
	else if (cmd=="alertBox")
	{
		// format for alertBox is:
		//  alertBox <tag> "<text to be shown to user>"
		// The problem is that this is immediately overwritten when next message from server is in.
		// Currently the server will use buttonPrompt instead of alertBox as workaround.
		console.log("alert: '" + str+"'");
		alert(str);
	}
	else if (cmd==expectedServer)
	{
		// This is the identification message from RSB server.
      		// It will expect client to identify itself. 
		// This is done to avoid connecting with someting compleately different.
		doSend(clientVersion);
	}
	else if (cmd=="close")
	{
		// server disconnecting
		doClose();
	}
	else if (cmd=="openChatRoom")  // This command is sent from ChatServer.java method join
	{
		chatRoomOpen('chat room');
	}
	else if (cmd=="openCityPvp")
	{
		cityPvpOpen('CityPvp');
	}
	else if (cmd=="listClear")
	{
		console.log('listClear');
		globalList=new Array();
	}
	else if (cmd=="listAdd")
	{
		console.log('listAdd '+globalList.length+' '+arg[1]);
		globalList[globalList.length]=arg[1];
	}
	else if (cmd=="openEmpire")
	{
		// This will draw the main empire window
		empireOpen(arg[1]);
	}
	else if (cmd=="openHmeg")
	{
		// This will draw the main HMEG window
		hmegOpen(arg[1]);
	}
	else
	{
		doError("Did not understand: '" + cmd+"'");
	}

}
