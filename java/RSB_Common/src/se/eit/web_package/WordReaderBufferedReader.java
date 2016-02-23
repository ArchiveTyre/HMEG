/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.web_package;

import java.io.BufferedReader;
import java.io.IOException;

public class WordReaderBufferedReader extends WordReader {

	BufferedReader bufferedReader=null;

	// http://docs.oracle.com/javase/6/docs/api/java/io/BufferedReader.html
	public WordReaderBufferedReader(BufferedReader bufferedReader)
	{
		this.bufferedReader = bufferedReader;
	}
	

	@Override
	protected void checkStrBuffer()
	{
		super.checkStrBuffer();
		
		// If we have no inputStr buffer check if there is more data.
		if (inputStr==null)
		{						
			inputStrOffset = 0;
			inputStrLength = 0;
			if (bufferedReader!=null)
			{
				try {
					inputStr = bufferedReader.readLine()+'\n'; /// TODO can we do something more clever than +'\n' here? Since we loose performance with this.
				} catch (IOException e) {
					debug("could not read line");
					e.printStackTrace();

				}
				
				if (inputStr!=null)
				{
					inputStrLength=inputStr.length();
				}
			}
		}
		
		
	}
	
	// If we are reading a string then this is: "isNotAtEnd".
	// If it is a tcp/ip connection then this is: "isOpen"
	@Override
	public  boolean isOpenAndNotEnd()
	{
		if ((inputStr!=null) && (inputStrOffset!=inputStrLength))
		{
			return true;
		}
		if (bufferedReader!=null)
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public  void close()
	{
		super.close();
		if (bufferedReader!=null)
		{
			try {
				bufferedReader.close();  // TODO probably it is the one who created the WordReaderBufferedReader that shall close the bufferedReader that it gave use. But will not change that just now for backward compatibility. Should change it some day though.
				bufferedReader=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		inputStr=null;
		inputStrOffset=0;
		inputStrLength=0;		
	}

}
