/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.web_package;



// This class is deprecated. Write to WordWriter (internal string buffer), use WordWriter.getString then send the string using WebConnection.write.

public class WordWriterWebConnection extends WordWriter{

	
	WebConnection cc = null; // http://docs.oracle.com/javase/6/docs/api/java/io/PrintWriter.html

	public WordWriterWebConnection(WebConnection wc)
	{		
		super();
		this.cc=wc;
	}

	@Override
	protected void sendIt(String str)
	{
		super.sendIt(str);
		cc.write(str);
	}

	@Override
	public void writeEoln()
	{
		if ((cc!=null) && (cc.isWebSocket()))
		{
			sendIfLonger(0);
			indentNeeded=true;	
		}
		else
		{
			super.writeEoln();
		}
	}

	
	@Override
	public void close()
	{
		super.close();

		//cc.close(); // The WebConnection is perhaps used by more than this writer so we can not close it here.
		cc=null;	
	}

}
