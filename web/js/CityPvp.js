
var mouseDownPos;




function cityPvpOpen(headingText)
{
	var newPage='';

        document.title = clientVersion;

        // Registrating our own handler for data from server, need to be restored if exiting CityPvp
	websocket.onmessage = function(evt) { cityPvpOnMessage(evt) };


	$("body").empty();

	console.log('cityPvpOpen '+headingText);

	newPage+='<h1>'+headingText+'</h1><br>';

	newPage+='<canvas id="myCanvas" width="512" height="512"></canvas><br>';


        // A text area for messages from server
	newPage+='<textarea id="textArea" cols="80" rows=4 readOnly="yes"></textarea><br/>';

        // A text input area for commands from user
	newPage+='<input type="text" id="inputText" size="70" onchange="cityPvpSendText(\'inputText\')"><br/>';

	// some buttons
	newPage+='<input type="button" value=enter onclick="cityPvpSendText(\'inputText\')">';
	newPage+='<input type="button" value=cancel onclick="cityPvpTextCancel()"><br>';

	newPage+='<audio id="horseAudio">'; // width="320" height="176"
	//newPage+='  <source src="mov_bbb.mp4" type="video/mp4">';
	newPage+='  <source src="horse.ogg" type="audio/ogg">';
	newPage+='  Your browser does not support HTML5 video.';
	newPage+='</audio>';


	$("body").append(newPage);

        cityPvpAddEventListenerForMyCanvas();


	/*$("input").keyup(function(event){
		console.log('key up: ' + event.which);
                websocket.send('keyUp "'+event.which+'"');
	}*/



	var canvas = document.getElementById('myCanvas');
	var ctx = canvas.getContext('2d');
	ctx.clearRect(0, 0, canvas.width, canvas.height);

	ctx.fillStyle="#F0F0F0";
	ctx.fillRect(0,0,canvas.width, canvas.height);

	ctx.moveTo(0,0);
	ctx.lineTo(canvas.width,0);
	ctx.lineTo(canvas.width,canvas.height);
	ctx.lineTo(0,canvas.height);
	ctx.lineTo(0,0);
	ctx.stroke();


        document.onkeypress=function(event)
	{
		console.log('keypress: ' + event.which);
                websocket.send('keypress "'+event.which+'"');
	}

}



function cityPvpAddEventListenerForMyCanvas()
{
	// HTML5 Canvas Mouse Coordinates Tutorial
	// http://www.html5canvastutorials.com/advanced/html5-canvas-mouse-coordinates/
	var canvas = document.getElementById('myCanvas');
	var context = canvas.getContext('2d');
	context.clearRect(0, 0, canvas.width, canvas.height);


	canvas.addEventListener('mousedown', function (evt) {
		mouseDownPos = cityPvpGetMousePos(canvas, evt);
	}, false);


	canvas.addEventListener('mouseup', function (evt) {
		var mouseUpPos = cityPvpGetMousePos(canvas, evt);
                
		//alert('mousedown '+sMessage);
		if ((mouseDownPos.x == mouseUpPos.x)  && (mouseDownPos.y == mouseUpPos.y))
		{
			console.log('Sector click position: ' + mouseUpPos.x + ',' + mouseUpPos.y);
                        websocket.send('mouseClick "'+mouseUpPos.x + ' ' + mouseUpPos.y+'"');

		}
		else
		{
			console.log('Sector down position: ' + mouseDownPos.x + ',' + mouseDownPos.y);
			console.log('Sector up position: ' + mouseUpPos.x + ',' + mouseUpPos.y);
                        websocket.send('mouseDrag "'+mouseDownPos.x + ' ' + mouseDownPos.y+' '+mouseUpPos.x + ' ' + mouseUpPos.y+'"');
		}
		delete mouseDownPos;
	}, false);


        /*canvas.onkeypress=function()
	{
		console.log('keypress: ' + event.which);
                websocket.send('keypress "'+event.which+'"');
	}*/

}

function cityPvpGetMousePos(canvas, evt) {
	var rect = canvas.getBoundingClientRect();
	return {
		x: Math.round(evt.clientX - rect.left),
		y: Math.round(evt.clientY - rect.top)
	};
}


function cityPvpOnMessage(evt)
{
        //str=getAsciiGraphPartOnly(evt.data);
        var str=evt.data;
	//var arg=str.split(' ');
	var arg=hlibSplitString(str);
	var i=0;
        var cmd=arg[0];
        var reply='';

	//console.log("cityPvpOnMessage: " + str); 

	// Here we check what server want client program to do.
	// Commands sent by ConnectionThread need to be interpreted here.
	if (cmd=="return")
	{
		websocket.onmessage = function(evt) { onMessage(evt) };
	}
	else if (cmd=="TextBoxAppend")
	{
		var a = document.getElementById("horseAudio"); 
		a.play();

		cityPvpTextBoxAppend(arg[1]);
	}
	else if (cmd=="AddImg")
	{
		var n=arg[1];
		var x=arg[2];
		var y=arg[3];
		var w=arg[4];
		var h=arg[5];
		if (arg.length>=6)
		{
			var t=arg[6];
			var ty=y/1+8;
		}

		var imageObj = new Image();
		imageObj.onload = function() {
			var canvas = document.getElementById('myCanvas');
			var context = canvas.getContext('2d');
			context.drawImage(imageObj,x, y, w, h);
			context.font = '8pt Calibri';
			context.fillStyle = 'black';
			if (typeof t !== 'undefined')
			{
				context.fillText(t, x, ty);
			}
		};
		imageObj.onerror = function() {
			var canvas = document.getElementById('myCanvas');
			var context = canvas.getContext('2d');
			context.font = '8pt Calibri';
			context.fillStyle = 'black';
			//context.fillText(n, x, y);
			if (typeof t !== 'undefined')
			{
				context.fillText(t, x, ty);
			}
		};
		imageObj.src = n+".png";

	}
	else if (cmd=="FillText")
	{
		var n=arg[1];
		var x=arg[2];
		var y=arg[3];
		//var w=arg[4];
		//var h=arg[5];

		var canvas = document.getElementById('myCanvas');
		var context = canvas.getContext('2d');

		context.font = '8pt Calibri';
		context.fillStyle = 'black';
		context.fillText(n, x, y+8);


	}
	else if (cmd=="EmptyTile")
	{
		var x=arg[1];
		var y=arg[2];
		var w=arg[3];
		var h=arg[4];

		var canvas = document.getElementById('myCanvas');
		var ctx = canvas.getContext('2d');
		ctx.clearRect(x, y, w, h);

		ctx.fillStyle="#CCEEFF";
		ctx.fillRect(x, y, w, h);

	}
	else if (cmd=="ClearTile")
	{
		var x=arg[1];
		var y=arg[2];
		var w=arg[3];
		var h=arg[4];

		var canvas = document.getElementById('myCanvas');
		var ctx = canvas.getContext('2d');
		ctx.clearRect(x, y, w, h);

		ctx.fillStyle="#F0F0F0";
		ctx.fillRect(x, y, w, h);

	}
	else if (cmd=="PlayAudio")
	{
		var x=arg[1];
		var a = document.getElementById("horseAudio"); 
		a.play();
	}
}



function cityPvpTextBoxAppend(msg)
{
	var e = document.getElementById("textArea");

	e.value+=hlibRemoveQuotes(msg)+"\n";
	e.scrollTop = e.scrollHeight;
}


function cityPvpSendText(id)
{
	var e = document.getElementById(id);
	var str=e.value;
	console.log("cityPvpSendText: " + str); 
	//doSend('textMsg "'+hlibAddBackslash(str)+'"');
        websocket.send('textMsg "'+hlibAddBackslash(str)+'"');
	e.value="";
}

function cityPvpTextCancel()
{
	doSend('cancel');
	websocket.onmessage = function(evt) { onMessage(evt) };
}




