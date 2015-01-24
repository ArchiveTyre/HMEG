/*
WordReader.java

Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

*/

package se.eit.web_package;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;




public class WordReader {

	// TODO: We should use a base class and one extending class for each of these possible sources, instead of 3 references here of which we usually use only one at a time.
	BufferedReader bufferedReader=null;
	InputStream inputStream=null;
	WebConnection tcpConnection=null;
	
	int tcpConnectionTimeoutMs=0;
	
	String inputStr=null;
	int inputStrOffset=0;
	int inputStrLength=0;
	
	public static void debug(String str)
	{
		WordWriter.safeDebug("WordReader: "+str);
	}
		
	/*public WordReader(ClientConnection clientConnection)
	{
		this.clientConnection = clientConnection;
	}*/
	
	// http://docs.oracle.com/javase/6/docs/api/java/io/BufferedReader.html
	public WordReader(BufferedReader bufferedReader)
	{
		this.bufferedReader = bufferedReader;
	}
	
	public WordReader(String str)
	{
		inputStr=str;
		inputStrOffset=0;
		inputStrLength = str.length();
		//debug("WordReader \"" + inputStr + "\"");
	}


	// Use this one when reading from standard input only. Not a generic InputStream since we will not close the InputStream.
	public WordReader(InputStream is)
	{
		//this.bufferedReader =  new BufferedReader(new InputStreamReader(is));
		this.inputStream = is;
	}
	
	public WordReader(WebConnection tc, int timeoutMs)
	{
		//this.bufferedReader =  new BufferedReader(new InputStreamReader(is));
		this.tcpConnection = tc;
		tcpConnectionTimeoutMs = timeoutMs;
	}
	
	private void checkStrBuffer()
	{
		if (inputStrOffset>=inputStrLength)
		{
			// All chartacters in current inputStr has been processed, so we can forget that buffer.
			inputStr=null;
		}
		
		// If we have no inputStr buffer check if there is more data.
		if (inputStr==null)
		{						
			inputStrOffset = 0;
			inputStrLength = 0;
			/*if (clientConnection!=null)
			{
				inputStr = clientConnection.readLineBlocking();
			}
			else */ 
			if (bufferedReader!=null)
			{
				try {
					inputStr = bufferedReader.readLine();
				} catch (IOException e) {
					debug("could not read line");
					e.printStackTrace();

				}
				
				if (inputStr!=null)
				{
					inputStrLength=inputStr.length();
				}
			}
			else if (inputStream!=null)
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
			else if (tcpConnection!=null)
			{
				try {
					inputStr = tcpConnection.readLine(tcpConnectionTimeoutMs)+"\n";
				} catch (InterruptedException e) {
					// Just a timeout.
				} catch (IOException e) {
					tcpConnection.close();
					tcpConnection = null;					
				}
				
				if (inputStr!=null)
				{
					inputStrLength=inputStr.length();
				}
			}
		}
		
		/*if (inputStrLength==0)
		{
			debug("hmm, empty line, we should get next");
		}*/
		
	}		
	
	public char getWaitChar()
	{
		checkStrBuffer();
		
		if (inputStr==null)
		{
			return Character.MIN_VALUE;
		}
		
		if (inputStrLength==0)
		{
			debug("getWaitChar got empty string");
			return '\n';
		}
		
		final char ch=inputStr.charAt(inputStrOffset++);
		return ch;
	}
	
	/*
	public char getWaitChar()
	{	
		checkStrBuffer();
		
		
		while (inputStrLength==0)
		{
			if (inputStr==null) 
			{
				return Character.MIN_VALUE;
			}

			checkStrBuffer();	
		}
		
		final char ch=inputStr.charAt(inputStrOffset++);
		return ch;
	}
	*/
	
	/*
	public static boolean isSeparator(char ch, char separator) {
		return (ch==separator) || Character.isWhitespace(ch);
	}
	*/

	
	// Returns true is ch is a separator between words 
	public static boolean isSeparator(char ch, String separator) 
	{
		if (Character.isWhitespace(ch))
		{
			return true;
		}
		else if (ch == Character.MIN_VALUE)
		{
			return true;
		}
		else if (Character.isAlphabetic(ch))
		{
			return false;
		}
		for (int i=0; i<separator.length();i++)
		{
			if (ch==separator.charAt(i))
			{
				return true;	
			}
		}
		return false;
	}
	

	// To get one word from the input, words are separated by separator
	public String readWord(String separator) 
	{
		StringBuffer sb=new StringBuffer();		
		int state=0;
		
		while (state<3)
		{
			char ch=getWaitChar();
			
			// Check for end of file or closed socket
			if (ch==Character.MIN_VALUE)
			{
				break;
			}
			
			switch(state)
			{
			     case 0:
			     {
			 		// skip spaces, if this was not a separator add it to the buffer and change state.
			 		if (!isSeparator(ch, separator))
			 		{
			 			sb.append(ch);
			 			state=1;
			 		}			 		
			 		break;
			     }
			     case 1:
			     {
			    	// Now look for the trailing space
				 	if (isSeparator(ch, separator))
				 	{
				 		state=4;
				 	}
					else
					{
						sb.append(ch);
					}
					break;
			     }
			     default:
			     {
			    	 break;
			     }
			}
		}

		return sb.toString(); 			
	}

	public String readWord(char separator) 
	{
		return readWord(""+separator);
	}

	public String readWord()
	{
		return readWord("");
	}

	public String readName()
	{
		return readWord('.'); // Instead of period '.' we should perhaps have use slash '/' here.
	}

	
	
	public void skipWhiteAndCheckStrBuffer()
	{
		// skip trailing spaces after previous word
		while ((inputStrOffset<inputStrLength) && (isSeparator(inputStr.charAt(inputStrOffset), "")))
		{		
			inputStrOffset++;
		}
	
		checkStrBuffer();

		// skip leading space
		while ((inputStrOffset<inputStrLength) && (isSeparator(inputStr.charAt(inputStrOffset), "")))
		{		
			inputStrOffset++;
		}		
	}
	
    // true if next thing to read is a string
	public boolean isNextString() 
	{
		skipWhiteAndCheckStrBuffer();		

		if (inputStr.length()<1)
		{
			return false;
		}
		
		char ch=inputStr.charAt(inputStrOffset);
		
		return ch=='"';
	}
	

	public boolean isNextBegin() 
	{
		skipWhiteAndCheckStrBuffer();		

		if (inputStr.length()<1)
		{
			return false;
		}
		
		char ch=inputStr.charAt(inputStrOffset);
		
		return ch=='{';
	}
	
	public boolean isNextEnd() 
	{
		skipWhiteAndCheckStrBuffer();		

		if (inputStr.length()<1)
		{
			return false;
		}
		
		char ch=inputStr.charAt(inputStrOffset);
		
		return ch=='}';
	}

	// true if the string looks like it can be an int (or other number).
	public static boolean isInt(String str)
	{
		if (str.length()==0)
		{
			return false;
		}
		char ch=str.charAt(0);		
		return (ch>='0' && ch<='9') || ch=='-' || ch=='+' ;			
	}
	
    // true if next thing to read looks like its a number (it only looks at one character so it can still be an incorrect number)
	public boolean isNextInt() 
	{
		skipWhiteAndCheckStrBuffer();		
		
		if (inputStr.length()<1)
		{
			return false;
		}
		
		char ch=inputStr.charAt(inputStrOffset);
		
		return (ch>='0' && ch<='9') || ch=='-' || ch=='+' ;
	}

		
	
	
	
	
	// Read a string, a string is a number of words surrounded by quotes '"'.
	public String readString() 
	{
		StringBuffer sb=new StringBuffer();
		int state=0;
		char quoteChar='"';
		
		while (state<4)
		{
			char ch=getWaitChar();

			// Check for end of file or closed socket
			if (ch==Character.MIN_VALUE)
			{
				break;
			}
			
			switch(state)
			{
			     case 0:
			     {
			 		// skip spaces, look for leading '"'
			 		if (!isSeparator(ch, ""))
			 		{	 			
				        // First char in a string shall be a '"'
				 		if (ch=='"')
						{
				 			state=1;
						}	
				 		else if (ch=='\'')
						{
				 			quoteChar='\'';
				 			state=1;
						}	
				 		else
				 		{
				 			// This string did not begin with quotes, so it will end with space instead
							//debug("String shall start with '\"'");
							sb.append(ch);
							state=3;
				 		}
			 		}
			 		break;
			     }
			     case 1:
			     {
			    	// Now look for the trailing '"' or the escape char '\'
					if (ch==quoteChar)
					{				
						// This marks the end of the string
						state=4;
					}
					else if (ch=='\\')
					{
						// This is the escape char, special char follows-
						state=2;
					}
					else
					{
						sb.append(ch);
					}
					break;
			     }
			     case 2:
			     {
			    	 // This was the character after an escape char.
			    	 if ((ch!=quoteChar) && (ch!='\\'))
			    	 {
			    		 debug("expected \" or \\ after string esc char");
			    	 }
			    	 sb.append(ch);
			    	 state=1;
 					 break;
			     }
			     case 3:
			     {
			    	// Now look for the trailing space
				 	if (isSeparator(ch, ""))
				 	{
				 		state=4;
				 	}
					else
					{
						sb.append(ch);
					}
					break;
			     }
			}
		}


		return sb.toString(); 	
	}

	// Reads a small number -128 to 127 that is written in ascii. This does not read a binary byte from the input stream.
	public byte readByte() throws NumberFormatException
	{
		String str=readWord(",;:/");
		try {
			byte i = Byte.parseByte(str);
			return i;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readInt: NumberFormatException expected byte but found " + str + " ");
			throw(new NumberFormatException("expected byte but found '" + str + "' "));
		}
	}
	
	// Reads a number that is written in ascii.
	// The integer numbers are usually separated by space. But they can also be separated by ',;:/'
	/*public int readInt()
	{
		String str=readWord(",;:/");
		try {
			int i = Integer.parseInt(str);
			return i;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readInt: NumberFormatException expected int but found '" + str + "' ");
			e.printStackTrace();
		}
		return 0;
	}*/
	public int readInt() throws NumberFormatException
	{
		final String str=readWord(",;:/");
		try {
			final int i = Integer.parseInt(str);
			return i;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("WordReader.readInt: NumberFormatException expected int but found '" + str + "' ");
			throw(new NumberFormatException("expected int but found '" + str + "' "));
		}
	}

	
	// Reads a big number that is written in ascii.
	// The long integer numbers are usually separated by space. But they can also be separated by ',;:/'
	public long readLong() throws NumberFormatException
	{
		String str=readWord(",;:/");
		try {
			long i = Long.parseLong(str);
			return i;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readInt: NumberFormatException expected long but found " + str + " ");
			throw(new NumberFormatException("expected long but found '" + str + "' "));
		}
	}

	public float readFloat() throws NumberFormatException
	{
		String str=readWord(",;:/");
		try {
			float f = Float.parseFloat(str);
			return f;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readInt: NumberFormatException expected float but found " + str + " ");
			throw(new NumberFormatException("expected float but found '" + str + "' "));
		}
	}
	
	/*
	// read an entire line of input, until a '\n' is found.
	public String readLine()
	{
		//debug("readLine");		
		checkStrBuffer();
		if (inputStr!=null) {
			int n = inputStrOffset;
			inputStrOffset=inputStrLength;
			String str=inputStr.substring(n,inputStrOffset);
			inputStr=null;
			//debug("line \""+getLineWithoutLf(str)+"\"");
			return str;
		}
		debug("line null");
		return null;
	}
*/
	
	// read an entire line of input, until a '\n' is found.
	// skip leading spaces
	public String readLine() 
	{
		StringBuffer sb=new StringBuffer();		
		int state=0;
		
		while (state<3)
		{
			char ch=getWaitChar();
			
			// Check for end of file or closed socket
			if (ch==Character.MIN_VALUE)
			{
				break;
			}
			
			switch(state)
			{
			     case 0:
			     {
			 		// skip spaces
			 		if (!isSeparator(ch, ""))
			 		{
			 			sb.append(ch);
			 			state=1;
			 		}
			 		break;
			     }
			     case 1:
			     {
			    	// Now look for the trailing '\n'
				 	if (ch=='\n')
				 	{
				 		state=4;
				 	}
					else
					{
						sb.append(ch);
					}
					break;
			     }
			     default:
			     {
			    	 break;
			     }
			}
			//debug("readLine: "+sb.toString());
		}

		return sb.toString(); 			
	}

	
	// If we are reading a string then this is: "isNotAtEnd".
	// If it is a tcp/ip connection then this is: "isOpen"
	public  boolean isOpenAndNotEnd()
	{
		/*if (clientConnection!=null)
		{
			return clientConnection.isOpen();
		}
		else*/ 
		if ((inputStr!=null) && (inputStrOffset!=inputStrLength))
		{
			return true;
		}
		if (bufferedReader!=null)
		{
			return true;
		}
		else if (inputStream!=null)
		{
			return true;
		}
		else if (tcpConnection!=null)
		{
			return true;
		}
		return false;
	}
	
	
	public  void close()
	{
		/*if (clientConnection!=null)
		{
			//clientConnection.close();
			clientConnection=null;
		}*/
		if (bufferedReader!=null)
		{
			try {
				bufferedReader.close();
				bufferedReader=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (inputStream!=null)
		{
			// We assume input stream was standard input so we don't clos it here. 
			inputStream=null;
		}
		if (tcpConnection!=null)
		{
			tcpConnection.close();
			tcpConnection=null;
		}
		
		inputStr=null;
		inputStrOffset=0;
		inputStrLength=0;		
	}
	
	protected static String skipLeadingSpace(String str) {
        int i=0;
    	
		while (str.charAt(i)==' ')
		{
			i++;
		}

		return str.substring(i);
	}	
	
	
	// This gives a string with the first word from str, words are separated by white space.
    public static String getWord(String str) {
    	
    	if (str==null)
    	{
    		return null;
    	}
    	
		int b=0;
		
		// skip leading space
		while ((b<str.length()) && (Character.isWhitespace(str.charAt(b))))
		{
			b++;
		}

		int e=b;
		
		// find the end of the word
		while ((e<str.length()) && (!Character.isWhitespace(str.charAt(e))))
		{
			e++;
		}
					
		return str.substring(b, e);
	}

	// This gives a string with the first word from str, words are separated by white space.
    public static String getWord(String str, String separator) {
    	
    	if (str==null)
    	{
    		return null;
    	}
    	
		int b=0;
		
		// skip leading space
		while ((b<str.length()) && (isSeparator(str.charAt(b), separator)))
		{
			b++;
		}

		int e=b;
		
		// find the end of the word
		while ((e<str.length()) && (!isSeparator(str.charAt(e), separator)))
		{
			e++;
		}
					
		return str.substring(b, e);
	}
    
    
    // This gives a string without the first word in str, to be used in combination with getWord
    public static String getRemainingLine(String str) {
		
    	if (str==null)
    	{
    		return null;
    	}
    	
		int b=0;
		
		// skip leading space
		while ((b<str.length()) && (Character.isWhitespace(str.charAt(b))))
		{
			b++;
		}

		int e=b;
		
		// find the end of the word
		while ((e<str.length()) && (!Character.isWhitespace(str.charAt(e))))
		{
			e++;
		}

		// skip leading space of second word
		while ((e<str.length()) && (Character.isWhitespace(str.charAt(e))))
		{
			e++;
		}
		
		return str.substring(e);
	}
	

    // This gives a string without the first word in str, to be used in combination with getWord
    public static String getRemainingLine(String str, String separator) {
		
    	if (str==null)
    	{
    		return null;
    	}
    	
		int b=0;
		
		// skip leading space
		while ((b<str.length()) && (isSeparator(str.charAt(b), separator)))
		{
			b++;
		}

		int e=b;
		
		// find the end of the word
		while ((e<str.length()) && (!isSeparator(str.charAt(e), separator)))
		{
			e++;
		}

		// skip leading space of second word
		while ((e<str.length()) && (isSeparator(str.charAt(e), separator)))
		{
			e++;
		}
		
		return str.substring(e);
	}

    
	public static boolean isWhiteSpaceCrLf(char ch)
	{
		return Character.isWhitespace(ch) || (ch=='\r') || (ch=='\n'); 
	}
	
	public static boolean isLetterOrDigitOrUnderscore(char ch)
	{
		return (Character.isLetterOrDigit(ch) || (ch=='_')); 
	}
	

	
	// Get the line without leading spaces and trailing line feeds.
	public static String getLineWithoutLf(String str) {
		
    	if (str==null)
    	{
    		return null;
    	}
    	
		int b=0;
		
		// skip leading space
		while ((b<str.length()) && (Character.isWhitespace(str.charAt(b))))
		{
			b++;
		}

		int e=str.length()-1;
		
		// skip trailing space
		while ((e>b) && ( isWhiteSpaceCrLf(str.charAt(e))))
		{
			e--;
		}
		
		return str.substring(b,e+1);
	}
	

	// Replace all a characters with b characters in a string
	public static String replaceCharacters(String str, char a, char b)
	{	
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<str.length();i++)
		{
			final char ch=str.charAt(i);
			if (ch==a)
			{
				sb.append(b);
			}
			else
			{
				sb.append(ch);				
			}
		}
		return sb.toString();
	}
	
	
    
    public static String getLastWord(String str, char sep)
    {
    	str=getLineWithoutLf(str);
    	
		int e=str.length()-1;
		int b=e;
		
		// skip trailing space
		while ((b>0) && (!isWhiteSpaceCrLf(str.charAt(b-1))) && (str.charAt(b-1)!=sep))
		{
			b--;
		}
		
		return str.substring(b,e+1);    	    	
    }
    

    public static String removeQuotes(String str)
    {
    	int len=str.length();
    	if (len>=2)
    	{
    		if ((str.charAt(0)=='"') && (str.charAt(len-1)=='"'))
    		{
    	    	return str.substring(1,len-1);   			
    		}
    	}
    	return str;
    }

    public static int genNumberOfWords(String str)
    {
    	int n=0;
    	WordReader wr=new WordReader(str);
    	while(wr.isOpenAndNotEnd())
    	{
    		wr.readString();
    		++n;
    	}    	
    	return n;
    }
    
    public static String[] split(String str)
    {
    	int n=genNumberOfWords(str);
    	String a[]=new String[n];
    	int i=0;
    	WordReader wr=new WordReader(str);
    	while(wr.isOpenAndNotEnd())
    	{
    		a[i]=wr.readString();
    		i++;
    	}    	
    	return a;
    }
}
