// WordReader.js
// Copyright (C) 2016 Henrik Bjorkman www.eit.se/hb
// Created 2016-02-20 by Henrik Bjorkman www.eit.se/hb


// Constructor.
function WordReader(arg_)
{
	this.arg=arg_;
	this.n=0;
}



// Read next word
WordReader.prototype.readNext = function()
{
	if (this.n<this.arg.length)
	{
		var str= this.arg[this.n];
		this.n++;
		return str;
	}
	return null;
}

// Read a specific word in the list. Not regarding word many have been read.
WordReader.prototype.getAbsArg = function(i)
{
	if (this.n<this.arg.length)
	{
		var str= this.arg[i];
		return str;
	}
	return null;
}

// Read a few words ahead from current read position.
WordReader.prototype.getRel = function(i)
{
	if ((this.n+i)<this.arg.length)
	{
		str= this.arg[this.n+i];
		return str;
	}
	return null;
}

// Skip a number of words.
WordReader.prototype.skip = function(i)
{
	this.n+=i;
}

// Gives number of "words" remaining in the list.
WordReader.prototype.getNRemaining = function()
{
	return (this.arg.length-this.n);
}


// Gives number of "words" read from list
WordReader.prototype.getNRead = function()
{
	return this.n;
}

// Gives number of "words" in the list.
WordReader.prototype.getArgLen = function()
{
	return this.arg.length;
}

