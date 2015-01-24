// do.js
// Copyright (C) 2014 Henrik Bjorkman www.eit.se/hb
// Created 2014-03-14 by Henrik Bjorkman www.eit.se/hb




// This sends a message to the server (and logs it for debugging)
function doSend(message)
{
	if (message!='pong')
	{
		console.log("SENT: " + message); 
	}
	websocket.send(message);
}

/*
function console.log(message)
{
	var pre = document.createElement("p"); // create a html element of type paragraph, <p> </p>
	pre.style.wordWrap = "break-word";  // just some formating options, probably not important
	pre.innerHTML = message; // embedd the message in the html element.
	docOutput.appendChild(pre); // Send the html element to the actual page that the user see.
}
*/

/*
function console.log(msg) {
    setTimeout(function() {
        throw new Error(msg);
    }, 0);
}
*/

/*
function doDebug(msg) {
    console.log(msg);
}
*/

function doError(msg) {
    console.log(msg);
    $("body").empty();
    $("body").append('<h3>'+msg+'</h3><br>');
}



// We can call this when we wish to dissconnect.
// It can be called from html when a button is pressed.
function doClose()
{
	console.log("DISCONNECTING");
	websocket.close();
}




function doInsertText(id,val)
{
	var theTextBox = document.getElementById(id);
	theTextBox.value+=val;
	theTextBox.scrollTop = theTextBox.scrollHeight;
}

function doMoveText(srcId,dstId)
{
	var theSrcTextBox = document.getElementById(srcId);
	var str=theSrcTextBox.value;
	theSrcTextBox.value="";

	doInsertText(dstId, str+'\n'); 
}

function doSendText(srcId)
{
	var theSrcTextBox = document.getElementById(srcId);
	var str=theSrcTextBox.value;
	doSend(str);
	theSrcTextBox.value="";
}

function doClearText(id)
{
	document.getElementById(id).value="";;
}

function doNext()
{
	docOutput = document.getElementById("divOutput");
	userName='';
	userName=prompt("Please enter your name",userName); 
	console.log("user name is: " + userName); 

	var theNameTextBox = document.getElementById("nameBox");
	theNameTextBox.value=userName;
}

function doLogin()
{
	doSend('loginRequest');

}


function doDrawRedBox()
{
	var c=document.getElementById("myCanvas");
	var ctx=c.getContext("2d");
	ctx.fillStyle="#FF0000";
	ctx.fillRect(0,0,150,75);

	ctx.fillStyle="#00FF00";
	ctx.fillRect(10,10,150-20,75-20);

	ctx.moveTo(0,0);
	ctx.lineTo(200,100);
	ctx.stroke();

	ctx.beginPath();
	ctx.arc(95,50,40,0,2*Math.PI);
	ctx.stroke();
}

var doIsMobileVar=null;
function doIsMobile()
{
/*	if (doIsMobileVar==null)
	{
		// TODO: Examine:
		//http://www.w3schools.com/jsref/prop_nav_useragent.asp
	    //var x = "User-agent header sent: " + navigator.userAgent;
   		//document.getElementById("demo").innerHTML = x;
	
		if (window.innerWidth>window.innerHeight)
		{
			doIsMobileVar = false;
		}
		else
		{
			doIsMobileVar = true;
		}
	}
	return doIsMobileVar;*/
	return false;
}


function doButtonQuery(headingText, buttonTexts)
{

	//var subDiv1=new DivBase(null, "testDiv1");
	//newPage+=subDiv1.defineDiv({x: 200, y: 50});


	if (!doIsMobile())
	{
		var newPage='';
		document.title = clientVersion;

		newPage+='<h3>'+hlibRemoveQuotes(headingText)+'</h3><br>';
		var i=0;
		for (i=0;i<buttonTexts.length;i++)
		{
			var buttonText=buttonTexts[i];
			var extraButton='<input type="button" value='+buttonText+' id="button_'+i+'" onclick="doButtonQueryCallback('+i+')">';
			newPage+=extraButton;
			console.log('extraButton '+i+' '+buttonTexts[i]);
		}
		$("body").empty();
		$("body").append(newPage);
	
	    document.getElementById("button_0").focus();
	}
	else
	{
		var doDiv=null;
		doDiv=new DivButtonList(null, 0, "doDiv", buttonTexts,headingText);
		var newPage=doDiv.defineDiv({x: window.innerWidth, y: window.innerHeight});
		console.log(newPage);
		$("body").empty();
		$("body").append(newPage);
		doDiv.drawDiv();
	}

    
    //subDiv1.drawDiv();
}

function doButtonQueryCallback(buttonNr)
{
	console.log('doButtonClick '+buttonNr);
	doSend(buttonNr);	
        $("body").empty();
}


function doTextQuery(headingText)
{
	console.log('doTextQuery '+headingText);
	//newPage+='text query<br>';

	document.title = clientVersion;
	var newPage='';

	if (!doIsMobile())
	{
		newPage+='<h3>'+hlibRemoveQuotes(headingText)+'</h3><br>';
		//newPage+='<input type="text" id="textArea" size="64" onchange="doTextQueryCallback()"><br/>';
		newPage+='<input type="text" id="textArea" onchange="doTextQueryCallback()"><br/>';
	
		newPage+='<input type="button" value=enter onclick="doTextQuery()">';	
		newPage+='<input type="button" value=cancel onclick="doCancelTextQuery()"><br>';
	}
	else
	{
		newPage+='<label for="info">'+hlibRemoveQuotes(headingText)+'</label>';
		newPage+='<textarea name="textArea" id="textArea" onchange="doNameQueryCallback()"></textarea>';
		newPage+='<input type="button" value=enter onclick="doNameQueryCallback()">';	
	}

	$("body").empty();
	$("body").append(newPage);

	document.getElementById("textArea").focus();
}

function doTextQueryCallback()
{
	var theSrcTextBox = document.getElementById("textArea");
	var str=theSrcTextBox.value;
	doSend('"'+str+'"');
	//theSrcTextBox.value="";
        $("body").empty();
}

function doCancelTextQuery()
{
	var theSrcTextBox = document.getElementById("doTextQuery");
	var str=theSrcTextBox.value;
	doSend('cancel');
	//theSrcTextBox.value="";
	$("body").empty();
}


function doIntQuery(headingText)
{
	document.title = clientVersion;

	console.log('doIntQuery '+headingText);
	var newPage='';
	newPage+=hlibRemoveQuotes(headingText)+'<br>';

	
	newPage+='<input type="text" id="doIntQuery" onchange="doIntQueryCallback()"><br/>';
	newPage+='<input type="button" value=enter onclick="doIntQueryCallback()"><br>';


	$("body").empty();
	$("body").append(newPage);

	document.getElementById("doIntQuery").focus();

}

function doIntQueryCallback()
{
	var theSrcTextBox = document.getElementById("doIntQuery");
	var str=theSrcTextBox.value;
	doSend(str);
	//theSrcTextBox.value="";
	$("body").empty();
}




function doCookieSet(cname,cvalue,exdays)
{
	var d = new Date();
	d.setTime(d.getTime()+(exdays*24*60*60*1000));
	var expires = "expires="+d.toGMTString();
	document.cookie = cname + "=" + cvalue + "; " + expires;
}

function doCookieGet(cname)
{
	var name = cname + "=";
	var ca = document.cookie.split(';');
	for(var i=0; i<ca.length; i++)
	{
		var c = ca[i].trim();
		if (c.indexOf(name)==0) return c.substring(name.length,c.length);
	}
	return "";
}



function doNameQuery(headingText)
{
	document.title = clientVersion;

	console.log('doNameQuery '+headingText);


	var newPage='';

	//newPage+='name query<br>';

	var user=doCookieGet("username");
	

	if (!doIsMobile())
	{
		newPage+='<div style="text-align:center">';
		newPage+='<h3>'+headingText+'</h3><br>';
		newPage+='<input type="text" id="textArea" onchange="doNameQueryCallback()" value="'+user+'"><br>';
		newPage+='<input type="button" value=enter onclick="doNameQueryCallback()">';	
		newPage+='<input type="button" value=cancel onclick="doNameQueryCancel()"><br>';
		newPage+='</div>';
	}
	else
	{
		/*
		newPage+='<input type="text" id="textArea" onchange="doNameQueryCallback()" value="'+user+'"><br>';
		newPage+='<input type="button" style="width:'+ window.innerWidth/3+'px; height : 36px;" value=enter onclick="doNameQueryCallback()">';
		newPage+='<input type="button" style="width:'+ window.innerWidth/3+'px; height : 36px;" value=cancel onclick="doNameQueryCancel()"><br>';
		*/
		
		newPage+='<label for="info">'+hlibRemoveQuotes(headingText)+'</label>';
		newPage+='<textarea name="textArea" id="textArea" onchange="doNameQueryCallback()" value="'+user+'"></textarea>';
		newPage+='<input type="button" value=enter onclick="doNameQueryCallback()">';	
	}

	console.log(newPage);

	$("body").empty();
	$("body").append(newPage);


	document.getElementById("textArea").focus();
}


function doNameQueryCallback()
{
	var theSrcTextBox = document.getElementById("textArea");
	var str=theSrcTextBox.value;
	doSend('"'+str+'"');

	// cookie, uncomment this line if cookie shall be saved.
	//doCookieSet("username",str,365);

	$("body").empty();
}


function doNameQueryCancel()
{
	$("body").empty();
	doSend('cancel');
}






// Ask for a password
function doPwQuery(headingText)
{
	document.title = clientVersion;

	console.log('doPwQuery '+headingText);
	var newPage='';

	if (!doIsMobile())
	{
		newPage+='<h3>'+hlibRemoveQuotes(headingText)+'</h3><br>';
		newPage+='<input type="password" id="doPwQuery" onchange="doPwQueryCallback()"><br/>';
		newPage+='<input type="button" value=enter onclick="doPwQueryCallback()">';
		newPage+='<input type="button" value=cancel onclick="doPwQueryCancel()"><br>';
	}
	else
	{
		newPage+='<label for="info">'+hlibRemoveQuotes(headingText)+'</label>';
		newPage+='<textarea name="textArea" id="doPwQuery" onchange="doPwQueryCallback()"></textarea>';
		newPage+='<input type="button" value=enter onclick="doNameQueryCallback()">';	
	}

	$("body").empty();
	$("body").append(newPage);

	document.getElementById("doPwQuery").focus();
}

function doPwQueryCallback()
{
	var theSrcTextBox = document.getElementById("doPwQuery");
	var str=theSrcTextBox.value;
	doSend('"'+str+'"');
	//theSrcTextBox.value="";
        $("body").empty();
}

function doPwQueryCancel()
{
        $("body").empty();
	doSend('cancel');
	//theSrcTextBox.value="";
}




var globalList = new Array();

function doListQuery(headingText)
{
	console.log('doListQuery');

        document.title = clientVersion;

	var newPage='';



	newPage+='<h3>'+hlibRemoveQuotes(headingText)+'</h3><br>';
	var i=0;


	var str='<select id="doListQuery">'
           
	for (i=0;i<globalList.length;i++)
	{
		str += '<option value="' + globalList[i] + '"> ' + globalList[i] + '</option>';
	}
	//str+='onchange="doListQueryCallback()</select>'
	str+='</select>'
	
	newPage+=str;

	newPage+='<br>';

	newPage+='<input type="button" id="enterButton" value=enter onclick="doListQueryCallback()">';

	newPage+='<input type="button" value=cancel onclick="doListQueryCancelCallback()"><br>';

	$("body").empty();
	$("body").append(newPage);

	var selectmenu=document.getElementById("doListQuery");
        selectmenu.onchange=function(){ //run some code when "onchange" event fires
		 var chosenoption=this.options[this.selectedIndex] //this refers to "selectmenu"
		 if (chosenoption.value!="nothing"){
		 	//window.open(chosenoption.value, "", "") //open target site (based on option's value attr) in new window
			doListQueryCallback();
		 };
	 }

	//document.getElementById("enterButton").focus();
	selectmenu.focus();
}

function doListQueryCallback()
{
	var e = document.getElementById("doListQuery");
	var str=e.value;

	console.log('doListQueryCallback ' + str);

	doSend(str);	
        $("body").empty();
}

function doListQueryCancelCallback()
{
	console.log('doListQueryCancelCallback');
	doSend('cancel');
        $("body").empty();
}


