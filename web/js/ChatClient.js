// Created 2014-03-14 by Henrik Bjorkman www.eit.se/hb


// http://www.websocket.org/echo.html
// http://www.w3schools.com/js/js_obj_string.asp

//var wsUri = "ws://localhost:54321/";
//var wsUri = "ws://192.168.42.153:23456/";
//var wsUri = "ws://b3.eit.se:53456/";


var expectedServer="www.eit.se/rsb/0.9/server"
var clientVersion="www.eit.se/rsb/0.9/client"



var docOutput;

var wsUri;




// Our init function, the first of our functions to be called
function init()
{
	docOutput = document.getElementById("divOutput");

	// http://stackoverflow.com/questions/519145/how-can-i-check-whether-a-variable-is-defined-in-javascript
	// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof
	if (typeof wsUri === 'undefined') {
   	    // wsUri is undefined, ask user for which server to connect to.
   	    wsUri=prompt("Please enter url", "ws://b3.eit.se:8080/");
	}

	//window.resizeTo(640,400);
	console.log("windowSize "+window.innerWidth + " "+window.innerHeight);

	onTestWebSocket(wsUri);
}


// This registers our init function so system will call it when page is loaded.
window.addEventListener("load", init, false);




