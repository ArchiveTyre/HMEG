// hlib.js
// Henrik JavaScript library files
// Created 2014-03-14 by Henrik Bjorkman www.eit.se/hb


// Returns a substring with only the first word in the string.
function hlibGetFirstWordInString(str)
{
  var len=str.length;
  var a=0;

  // skip leading spaces
  while((a<len) && (!hlibIsAsciiGraph(str[a])))
  {
    a++;
  }

  // Find end of word (include all non space characters)
  var b=a;
  while((b<len) && (hlibIsAsciiGraph(str[b])))
  {
    b++;
  }
  
  //doDebug("getAsciiGraphPartOnly '"+str+"' "+len+" "+a+" "+b);
  return str.substring(a,b);
}

function hlibStrncmp(a, b, n){
    return a.substring(0, n) == b.substring(0, n);
}


function hlibIsAsciiGraph(ch)
{
  //doDebug("hlibIsAsciiGraph "+ch);

  //return ((ch>' ') && (ch<='~') && (ch!='\&'))
  return ((ch>' ') && (ch<='~'))
}


// This is similar to standard str.split(' ') but it will keep strings together.
//
// Our strings contain sub strings such as this line: 
// query login_or_reg "Welcome to RSB" buttonPrompt 4 "Login" "Reg new account" "Recover pw" "Cancel"
// It shall be split up like this:
// query
// login_or_reg
// Welcome to RSB
// buttonPrompt
// 4
// Login
// Reg new account
// Recover pw
// Cancel
//
function hlibSplitString(str)
{
	if (str==='undefined')
	{
		console.log("hlibSplitString: undefined");
		return;
	}
	if (str==null)
	{
		console.log("hlibSplitString: null");
		return;
	}


	var len=str.length;
	var arg = [];
	var n=0; // number of words/strings found
	var b=0; // begin
	var e=0; // end

	while(b<len)
	{
		var tmpStr="";
		// skip space etc, find beginning
		while((b<len) && (!hlibIsAsciiGraph(str[b])))
		{
			b++;
		}
		e=b;
		if (str[b]=='"')
		{
			var d=b;
			//b++;
			e++;
			// This is a quoted string, find the end '"' 
			while((e<len) && ( str[e]!='"' ))
			{
				if(str[e]=='\\')
				{
					tmpStr+=str.substring(d,e);
					e++;
					if(e<len)
					{
						tmpStr+=str[e];
						e++;
					}
					d=e;
				}
				else
				{
					e++;
				}
			}

			if (str[e]=='"')
			{
				e++;
			}

			tmpStr+=str.substring(d,e);
		}
		else
		{
			// Not quoted, find the end
			while((e<len) && (hlibIsAsciiGraph(str[e])))
			{
				e++;
			}

			tmpStr=str.substring(b,e);
		}

		if (b==e)
		{
			break;
		}


		arg[n]=tmpStr;
		n++;
		b=e;
	}

	return arg;
}

function hlibAddBackslash(str)
{
  var len=str.length;
  var tmpStr="";
  for(var i=0; i<len; i++)
  {
    if ((str[i]=='\\') || (str[i]=='"'))
    {
      tmpStr+='\\';
    }

    tmpStr+=str[i];
  }
  return tmpStr;
}


function hlibRemoveQuotes(str)
{
	if (str==='undefined')
	{
		console.log("hlibSplitString: undefined");
		return;
	}
	if (str==null)
	{
		console.log("hlibSplitString: null");
		return;
	}


	var len=str.length;
	if (len>=2)
	{
		if ((str[0]=='"') && (str[len-1]=='"'))
 		{
			return str.substring(1,len-1);
		}
	}
	return str;
}


/*
function displayDate()
{
	document.getElementById("WhatEver").innerHTML=Date();
}
*/

function sqr(a)
{
	return a*a;
}

function calcDist(p1, p2)
{
	return Math.sqrt(sqr(p1.x-p2.x)+sqr(p1.y-p2.y));
}



