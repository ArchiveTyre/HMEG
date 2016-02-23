/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.web_package;

import java.io.IOException;
import java.io.InputStream;

public class WordReaderInputStream extends WordReader {

	InputStream inputStream=null;

	
	
	// Use this one when reading from standard input only. Not a generic InputStream since we will not close the InputStream.
	public WordReaderInputStream(InputStream is)
	{
		super();

		//this.bufferedReader =  new BufferedReader(new InputStreamReader(is));
		this.inputStream = is;
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
			if (inputStream!=null)
			{
				try 
				{
					for(;;)
					{
						int n=inputStream.available();
						if (n>0)
						{
						    byte d[] = new byte[n];
							int len = inputStream.read(d);
								
						    if (len>0)
						    {
						    	inputStr = new String(d, 0, len);
						    	break;
						    }			
						}
						synchronized(this)
						{
							try {
								wait(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStrLength=inputStr.length();
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
		else if (inputStream!=null)
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public  void close()
	{
		super.close();

		if (inputStream!=null)
		{
			// We assume input stream was standard input so we don't close it here. 
			inputStream=null;
		}
		
		inputStr=null;
		inputStrOffset=0;
		inputStrLength=0;		
	}

}
