/*
WordReader.java

Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

2016-01-01 
Cleanup by Henrik Bjorkman (www.eit.se/hb)
*/

package se.eit.web_package;





public class WordReader {

	
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

	
	// This constructor is to be used by extending classes, it would make little sense to create a WordReader without something to read.
	public WordReader()
	{
	}
	
	
	// This is used to read words etc from a string.
	public WordReader(String str)
	{
		inputStr=str;
		inputStrOffset=0;
		inputStrLength = str.length();
		//debug("WordReader \"" + inputStr + "\"");
	}


	
	
	protected void checkStrBuffer()
	{
		if (inputStrOffset>=inputStrLength)
		{
			// All chartacters in current inputStr has been processed, so we can forget that buffer.
			inputStr=null;
		}
		
		if (inputStr==null)
		{						
			inputStrOffset = 0;
			inputStrLength = 0;
		}
				
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

	public static boolean isCharInStr(char ch, String separator)
	{
		for (int i=0; i<separator.length();i++)
		{
			if (ch==separator.charAt(i))
			{
				return true;	
			}
		}
		return false;
	}
	
	
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
		else if (isLetterOrDigitOrUnderscore(ch))
		{
			return false;
		}
		else if (isCharInStr(ch, separator))
		{
			return true;	
		}
		return false;
	}
	

	// To get one word from the input, words are separated by separator
	// deprecated, use readToken
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
			     case 0: // initial state, skipping spaces and separators
			     {
			 		// skip spaces, if this was not a separator add it to the buffer and change state.
			 		if (!isSeparator(ch, separator))
			 		{
			 			sb.append(ch);
			 			state=1;
			 		}			 		
			 		break;
			     }
			     case 1: // a normal word
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

	// deprecated, use readToken
	public String readWord(char separator) 
	{
		return readWord(""+separator);
	}

	// deprecated, use readToken
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

		if (inputStr.length()<=inputStrOffset)
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
	
	
	
    // true if next thing to read looks like its a number, an int or a float.
	public boolean isNextIntOrFloat() 
	{
		boolean found=false;
		try
		{
			if (inputStr==null)
			{
				return false;
			}
	
			skipWhiteAndCheckStrBuffer();
	
			int n=inputStr.length();
			
			if (n<1)
			{
				return false;
			}

			int i=inputStrOffset;

			char ch=inputStr.charAt(i);
			i++;
			if ((ch>='0' && ch<='9'))
			{
				// OK, first char is a digit.
				found=true;
			}
			else if (ch=='-' || ch=='+')
			{
				// Ok if digits follow
				if (n<=1)
				{
					// No the string ended after +/-, not an int
					return false;
				}
			}
			else
			{
				// Not a number. First char is a letter or space or something.
				return false;
			}
			

			while(i<n)
			{				
				ch=inputStr.charAt(i);				
				i++;
				if ((ch>='0' && ch<='9'))
				{
					found=true;
					// OK
				}
				else if (((ch=='.') || (ch=='-') || (ch=='E')) && (found))
				{
					// OK, will accept parts of a float also. 
					// Floats can look like this: 9.223372E18
					// This code does not check that there is only one E and one '.'.
				}
				else if (isWhiteSpaceCrLf(ch))
				{
					// String ends here, if it was numbers this far then it is a number.
					return found;
				}
				else
				{
					// Not a number.
					return false;
				}								
			}
		}
		catch (NullPointerException e)
		{
			return false;
		}

		return found;
	}

		
	
    // true if next thing to read is a string that begins with same as str
	public boolean isNext(String str1)
	{
		skipWhiteAndCheckStrBuffer();

		if (inputStr.length()<str1.length()+inputStrOffset)
		{
			return false;
		}

		int n=str1.length();
		
		int i=0;

		
		while(i<n)
		{
			char ch1=str1.charAt(i);
			char ch2=inputStr.charAt(inputStrOffset+i);
		
			if (ch1!=ch2)
			{
				return false;
			}
			i++;
		}
		
		// TODO We might want to check that a word in input string ended here. 
		
		return true;
	}
	
	
	// Read a string, a string is a number of words surrounded by quotes '"'.
	public String readString() 
	{
		String tmp = readToken(",;:/[");
		return tmp;
	}

	// Reads a small number -128 to 127 that is written in ascii. This does not read a binary byte from the input stream.
	public byte readByte() throws NumberFormatException
	{
		String str=readToken(",;:/[");
		try {
			int i = Integer.parseInt(str);
			return (byte)i;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readInt: NumberFormatException expected byte but found " + str + " ");
			throw(new NumberFormatException("expected byte but found '" + str + "'"));
		}
	}
	
	// Reads a number that is written in ascii.
	// The integer numbers are usually separated by space. But they can also be separated by ',;:/'
	/*public int readInt()
	{
		String str=readWord(",;:/[");
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
		final String str=readToken(",;:/[");
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
	
	// Reads a number that is written in ascii.
	// The long numbers are usually separated by space. But they can also be separated by ',;:/'
	public long readLong() throws NumberFormatException
	{
		String str=readToken(",;:/[");
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
		String str=readToken(",;:/[");
		try {
			float f = Float.parseFloat(str);
			return f;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readFloat: NumberFormatException expected float but found " + str + " ");
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
		if ((inputStr!=null) && (inputStrOffset!=inputStrLength))
		{
			return true;
		}
		return false;
	}
	
	
	public  void close()
	{
		
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


	// Reads a boolean that is written in ascii.
    public boolean readBoolean() throws NumberFormatException
    {
		String str=readToken(",;:/[");
		try {
			int i = Integer.parseInt(str);
			return (i==0)?false:true;
		}
		catch (NumberFormatException e)
		{
			WordWriter.safeError("\nWordReader.readInt: NumberFormatException expected long but found " + str + " ");
			throw(new NumberFormatException("expected long but found '" + str + "' "));
		}
    	
    }

    
    
	public char previewChar()
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
		
		final char ch=inputStr.charAt(inputStrOffset);
		return ch;
	}

	
	public static boolean isInternalPartOfNumber(char ch)
	{
		if ((ch>='0' && ch<='9'))
		{
			return true;
		}
		else if (((ch=='.') || (ch=='-') || (ch=='E')))
		{
			// OK, will accept parts of a float also. 
			// Floats can look like this: 9.223372E18
			// This code does not check that there is only one E and one '.'.
			return true;
		}
		return false;
	}

	public enum ReadTokenState {
	    InitialState, 
	    ParsingNumber, 
	    ParsingName, 
	    NumberOrOpcode,
	    ParsingOpcode,  
	    InsideString,
	    EscapeChar,
	    EndState
	}
	
	
	
	
	// To get one word from the input
	public String readToken(String separatorChars)
	{
		final String singleCharTokens=",.;[](){}";
		final String multiCharTokens="~^&|<>:=!*/%?";
		StringBuffer sb=new StringBuffer();		
		ReadTokenState state=ReadTokenState.InitialState;
		char quoteChar=0;

		
		while (state!=ReadTokenState.EndState)
		{
			char ch=previewChar();
			
			// Check for end of file or closed socket
			if (ch==Character.MIN_VALUE)
			{
				break;
			}
			
			switch(state)
			{
				case InitialState: // initial state, skipping spaces and separators
				{
					// if this is not a separator add it to the buffer and change state.
					if (isSeparator(ch, separatorChars))
					{
						// skip leading spaces
						getWaitChar();
					}
					else if (Character.isDigit(ch))
					{
						sb.append(getWaitChar());
						state=ReadTokenState.ParsingNumber;			    		
					}
					else if (isLetterOrDigitOrUnderscore(ch))
				 	{
						sb.append(getWaitChar());
						state=ReadTokenState.ParsingName;
				 	}
					else if (ch=='-')
					{
						sb.append(getWaitChar());
						state=ReadTokenState.NumberOrOpcode;			    		
					}
					else if (ch=='"')
					{
						quoteChar=getWaitChar();
						state=ReadTokenState.InsideString;			    		
					}
					else if (ch=='\'')
					{
						// single quoted string
						quoteChar=getWaitChar();
						state=ReadTokenState.InsideString;			    		
					}
					else if (isCharInStr(ch, singleCharTokens))
					{
						sb.append(getWaitChar());
				 		state=ReadTokenState.EndState;			 			
					}
					else if (isCharInStr(ch, multiCharTokens))
					{
						sb.append(getWaitChar());
				 		state=ReadTokenState.ParsingOpcode;			 			
					}
					else
					{
						// what to do with this one?
						sb.append(getWaitChar());
						state=ReadTokenState.EndState;
					}
					break;
				}
				case ParsingNumber: // a number, 
				{
					// Now look for the trailing space
				 	if (isInternalPartOfNumber(ch))
				 	{
						sb.append(getWaitChar());
				 	}
					else
					{
				 		state=ReadTokenState.EndState;
					}
					break;
				}
				case ParsingName: // a normal word or name, 
				{
					// Now look for the trailing space
				 	if (isLetterOrDigitOrUnderscore(ch))
				 	{
						sb.append(getWaitChar());
				 	}
					else
					{
						final String r=sb.toString();
						if (r.equals("null"))
						{
							//debug("null");
							return null;
						}
				 		state=ReadTokenState.EndState;
					}
					break;
				}
				case NumberOrOpcode: // a number or an opcode?
				{
				 	if (Character.isDigit(ch))
				 	{
				 		// Part of a number, as in -9.01
						sb.append(getWaitChar());
						state=ReadTokenState.ParsingNumber;
				 	}
					else if (isCharInStr(ch, multiCharTokens))
					{
						sb.append(getWaitChar());
				 		state=ReadTokenState.ParsingOpcode;			 			
					}
					else
					{
				 		state=ReadTokenState.EndState;
					}
					break;
				}
				case ParsingOpcode: // part of an opcode 
				{
					if (isCharInStr(ch, multiCharTokens))
					{
						sb.append(getWaitChar());
					}
					else
					{
				 		state=ReadTokenState.EndState;
					}
					break;
				}
			    
				case InsideString:
				{
					// Now look for the trailing '"' or the escape char '\'
					if (ch==quoteChar)
					{				
						// This marks the end of the string
						getWaitChar();
						state=ReadTokenState.EndState;
					}
					else if (ch=='\\')
					{
						// This is the escape char, special char follows-
						getWaitChar();
						state=ReadTokenState.EscapeChar;
					}
					else
					{
						sb.append(getWaitChar());
					}
					break;
				}
				case EscapeChar:
				{
					getWaitChar();
					// https://en.wikipedia.org/wiki/Escape_sequences_in_C
					switch(ch)
					{
						/*case 'a':
						sb.append('\a');
						break;*/
					case 'b':
						sb.append('\b');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'u':
					case 'U':
						debug("unicode escape sequence is not supported yet");
						break;
					/*case 'v':
						sb.append('\v');
						break;*/
					case 'x':
						debug("hex escape sequence is not supported yet");
						break;
					case '\\':
					case '\'':
					case '\"':
					case '?':
						sb.append(ch);
						break;
					default:
					{
						if ((ch>='0') && (ch<='9'))
						{
							debug("octal escape sequence is not supported yet");
						}
						else
						{
							debug("incorrect escape sequence");
							}
							sb.append(ch);
							break;
						}
					}
				
					state=ReadTokenState.InsideString;
					break;
				 }
				 
			    default:
			    {
			    	break;
			    }
			}
		}

		final String r=sb.toString();
		//debug("'"+r+"'");
		return r; 			
	}

}
