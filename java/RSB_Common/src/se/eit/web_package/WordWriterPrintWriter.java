/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.web_package;

import java.io.PrintWriter;


// This class is deprecated. Write to WordWriter (internal string buffer), use WordWriter.getString then send the string using PrintWriter.print.

public class WordWriterPrintWriter extends WordWriter{

	
	PrintWriter pw = null; // http://docs.oracle.com/javase/6/docs/api/java/io/PrintWriter.html

	public WordWriterPrintWriter(PrintWriter pw)
	{		
		super();
		this.pw=pw;
	}

	@Override
	protected void sendIt(String str)
	{
		super.sendIt(str);

		pw.print(str+" ");  // TODO remove +" " here
	}

	
	@Override
	public void close()
	{
		super.close();

		pw.close();
		pw=null;	
	}

}
