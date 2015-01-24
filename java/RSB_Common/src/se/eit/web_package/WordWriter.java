/*
WordReader.java

Copyright 2013 Henrik Björkman (www.eit.se/hb)

These are some methods for parsing strings.


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

*/

package se.eit.web_package;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;


// 
public class WordWriter {

	public static final String BEGIN_MARK="{";
	static final String END_MARK="}";
	
	int max_packet_size=10000;
	
	// TODO: We should use a base class and one extending class for each of these possible sources, instead of 4 references here of which we use only one at a time.
	WebConnection cc=null;
	OutputStream os = null;	 // http://docs.oracle.com/javase/6/docs/api/java/io/OutputStream.html
	PrintWriter pw = null; // http://docs.oracle.com/javase/6/docs/api/java/io/PrintWriter.html
	StringBuffer sb = null;

	// Using StringBuffer but perhaps we should use StringBuilder for faster code?
	// http://docs.oracle.com/javase/6/docs/api/java/lang/StringBuilder.html
	StringBuffer tmpBuf=new StringBuffer(1400); // http://docs.oracle.com/javase/6/docs/api/java/lang/StringBuffer.html 
	
	//public long visibilityMask = -1;
	int indent=0;
    boolean indentNeeded=false;

    // This was added as a workaround. When TcpConnection is in web socket mode then we should send lines one by one.
    // When this has been sorted out this variable shall be removed.
    //boolean flushAfterLf=false;
    
	// Make a safe string for debug output (lots of linefeed etc could make log unreadable);
	public static String safeStr(String str, int maxLen)
	{		
		// List all characters allowed (in addition to letters and digits) here. 
		// Not allowed in debug log:
		// char replacement comment:
		// "    &#34;       used to mark strings begin and end
		// #    &#35;       used to show that the line was truncated 
		// &    &#38;       used to show non allowed characters)
		// \    &#92;       used elsewhere as an escape char
		
		//final String allowed=" !$¤%'()*+,-./:;<=>?@[]_^{|}~";
		final String allowed=" !$¤%'()*+,-./:;<=>?@[]_^|~";

		StringBuffer tmpBuf=new StringBuffer();
		int len=str.length();
		if (len>maxLen)
		{
			len=maxLen;
		}
		for(int i=0;i<len;i++)
		{
			final char ch=str.charAt(i);
			if (isAllowedChar(ch,allowed))
			{
				tmpBuf.append(ch);				
			}
			else
			{
				if (ch=='\"')
				{
					// For readability just make single quote of this one.
					tmpBuf.append("'");					
				}
				else
				{
					tmpBuf.append("&#"+(int)ch+";");
				}
			}
		}
		if (str.length()>maxLen)
		{
			tmpBuf.append("#");					
		}
		return tmpBuf.toString();
	}
	

	public static void safeDebug(String str)
	{
		System.out.println(" "+safeStr(str, 160));
	}

	
	public static void safeError(String str)
	{
	    System.out.flush();
	    System.err.println(safeStr(str,1024));
	    System.out.flush();
		Thread.dumpStack();
	    System.out.flush();
	}
	
	public static void debug(String str)
	{
		safeDebug("WordWriter: "+str);
	}
	
	
	public WordWriter(WebConnection cc)
	{
		//cc.rwl.writeLock().lock();
	   this.cc=cc;
	}
	
	public WordWriter(OutputStream os)
	{		
		this.os = os;
	}
	
	public WordWriter(PrintWriter pw)
	{		
		this.pw=pw;
	}

	public WordWriter(StringBuffer sb)
	{		
		this.sb=sb;
	}

	// With this constructor the data is stored in an internal StringBuffer and can be retrieved by calling "getString"
	public WordWriter()
	{
		this.sb=new StringBuffer();
		max_packet_size=0x7FFFFFFF;
	}

	public String getString()
	{	
		if (sb!=null)
		{
			sendIfLonger(0);
			String str=sb.toString();
			return str;
		}
		else
		{
			debug("getString without sb");
			String str=tmpBuf.toString();
			return str;			
		}
	}

	// This can be used for debugging etc, everything written to the WordWriter is also written to the StringBuffer.
	public void addStringBuffer(StringBuffer sb)
	{		
		this.sb=sb;
	}

	// Stop the recording to a string buffer.
	public StringBuffer getRemoveStringBuffer()
	{		
		StringBuffer tmp=this.sb;
		this.sb=null;
		return tmp;
	}
	
	
	
	// The word shall not contain spaces etc since then readWorld in WordReader will not be able 
	// to read it back. It there are spaces then writeString/readString can be used.
	public void writeWord(String str)
	{
		writeIndentation();
	
		if (str==null)
		{
			debug("writeWord null");
			str="null";
		}
		else if (!WordWriter.isOneWord(str))
		{
			// When writing strings the method writeString shall be used, it will replace space with an escape sequence. 
			debug("string with space sent using writeWord "+str);
		}
		tmpBuf.append(str);		
		tmpBuf.append(" ");
		sendIfLonger(max_packet_size);
	}

	
	public void writeEoln()
	{
		if ((cc!=null) && (cc.isWebSocket()))
		{
			sendIfLonger(0);
		}
		else
		{
			tmpBuf.append("\n");
			sendIfLonger(max_packet_size);			
		}
		indentNeeded=true;	
	}
	
	// The difference from writeLine is only that it will send what's in tmpBuf right away while writeLine will do so only if buffer is filled.
	public void println(String str)
	{
		writeIndentation();
		tmpBuf.append(str);
		writeEoln();
		sendIfLonger(0);
	}

	// Write a line and line feed
	public void writeLine(String line)
	{
		writeIndentation();
		tmpBuf.append(line);		
		writeEoln();
	}	
	
	public static String addEscSequence(String str)
	{	
		StringBuffer tmpBuf=new StringBuffer();
		copyAndAddEscSequence(str,tmpBuf);
		return tmpBuf.toString();
	}
	
	public static void copyAndAddEscSequence(final String str, StringBuffer sb)
	{	
		if (str!=null)
		{
			for(int i=0;i<str.length();i++)
			{
				final char ch=str.charAt(i);
				if ((ch=='"') || (ch=='\\'))
				{
					sb.append("\\");
				}
				sb.append(ch);
			}
		}
	}

	
	// When using writeWord the string shall not contain spaces etc since then readWorld in WordReader will not be able to read it back.
	// If there are spaces then writeString/readString shall be used instead.
	public void writeString(String str)
	{
		writeIndentation();
		
		tmpBuf.append("\"");
		copyAndAddEscSequence(str, tmpBuf);
		
		tmpBuf.append("\" ");
		sendIfLonger(max_packet_size);
	}

	
    
    // gives true if string is made up of letters, digits and/or allowedChars
    public static boolean isStringOk(String str, String allowedChars, int minLength)
    {
  	    if (str==null)
    	{
  		    return false;
  	    }
	    if (str.length()<minLength)
	    {
	    	debug("to short string");
	    	return false;
	    }
	    if (str.length()>256)
	    {
	    	debug("to long string " + str.length());  				
	    	return false;
	    }

	    for(int i=0;i<str.length();i++)
	    {
	    	if (!(isAllowedChar(str.charAt(i), allowedChars)))
	    	{
	    		debug("some characters in the string are not allowed, letters, digits and "+allowedChars+" are allowed");	    		
		    	return false;
	    	}
	    }
	  
	    return true;
    }
	
    // Returns true if string contain just one word (that is no spaces, tabs etc)
    public static boolean isOneWord(String str)
    {
  	    if (str==null)
    	{
  		    return false;
  	    }
    	
	    for(int i=0;i<str.length();i++)
	    {
	    	if (WordReader.isSeparator(str.charAt(i), ""))
	    	{
	    		return false;
	    	}
	    }
	    return true;
    }
    
	public static boolean isFirstLetterOkForAName(String str)
	{
		if (str.length()>0)
		{
			final char ch=str.charAt(0);
						
			// names should not begin on 0-9, '-', '+' or '#' since that is reserved for numbers.
			if (((ch>='0') && (ch<='9')) || (ch=='-') || (ch=='+' ) || (ch=='#' ) || (ch=='"' ))
			{				
				System.out.println("Characters '0-9', '-', '+' and '#' are reserved for numbers");
				return false;
			}

			// it shall not begin with '"' since that is internally used to tell if it is a string.
			// and '/' is used as separator between names in a hierarchy.
			if ((ch=='"') || (ch=='/')) 
			{				
				System.out.println("Characters '\"' and '/' are reserved for internal use");
				return false;
			}
					
			// we will require that the first character is a letter.
			if (!Character.isLetter(ch))
			{
				System.out.println("names shall begin with a letter");
				return false;				
			}		
			return true;
		}	
		return false;				
	}

	public static boolean isAllowedChar(char ch, String allowedChars)
	{
		if (Character.isLetterOrDigit(ch))
		{
			return true;
		}
			
		for (int i=0; i<allowedChars.length(); i++)
		{
			if (ch==allowedChars.charAt(i))
			{
				return true;			
			}
		}
		
		return false;
	}
		
	
    // gives true if string is made up of letters, digits
    public static boolean isStringOkAsNameOrNumber(String str)
    {
    	return isStringOk(str, "_", 0);
    }

    public static boolean isStringOkAsName(String str)
    {
    	if (!isFirstLetterOkForAName(str))
    	{
    		return false;
    	}
    	if (!isStringOkAsNameOrNumber(str))
    	{
    		return false;
    	}
    	return true;
    }
    

    
    // Returns:
	// true if filename is approved.
	// false if it contains characters that we do not allow in filenames.
	public static boolean isFilenameOk(String filename)
	{
		final String allowedCharacters=" !$¤%'()+,-./=?@[]_^~"; // This is the list of characters that we allow. We could probably allow more characters but its not needed now.
		final int len=filename.length();
		int dotState=0;
		
		if (len<=0)
		{
			return false; // Its too short	
		}

		if (len>1024)
		{
			return false; // Its too long, *NIX usually allow 4096 bytes but we don't need to support that long names.
		}
				
		for (int i=0; i<len; i++)
		{
			final char ch=filename.charAt(i);

			if (!isAllowedChar(ch, allowedCharacters))
			{
				return false; // It contain characters that we do not allow (or just don't need to support).		
			}

			
			// We will not allow filenames beginning with .. or containing /.. since then it would make it possible to get files outside the httpRootDir
			switch (dotState)
			{
				case 0: // Base state
					if (ch=='.')
					{
						// This was first dot.
						dotState=1;
					}
					else if (ch=='/')
					{
						dotState=0;
					}
					else
					{
						// We are inside the filename (not in the beginning of it)
						dotState=2;
					}
					break;
				case 1: // This is after one dot.
					if (ch=='.')
					{
						// This was second dot. Not OK.
						return false; 					
					}
					else if (ch=='/')
					{
						dotState=0;
					}
					else
					{
						// We are inside the filename (not in the beginning of it)
						dotState=2;
					}
					break;
				case 2: // This is inside a filename or directory name 
					if (ch=='/')
					{
						dotState=0;
					}
					else
					{
						// We are inside the filename (not in the beginning of it)
						dotState=2;
					}
					break;
				default:
					return false;					
			}					
			
		}
		return true;
	}

    
    
	public void writeName(String str)
	{
		if (!isFirstLetterOkForAName(str))
		{
			debug("writeName: looks like a number");			
		}
		writeWord(str);
	}
	
	
	public void writeByte(byte i)
	{
		writeWord(""+i);
	}
	
	public void writeInt(int i)
	{
		writeWord(""+i);
	}

	public void writeLong(long i)
	{
		writeWord(""+i);
	}

	
	public void writeFloat(float f)
	{
		writeWord(""+f);
	}
	
	public void writeBegin()
	{
		indentNeeded=true;
		writeIndentation();
		tmpBuf.append(BEGIN_MARK+" ");
		sendIfLonger(max_packet_size);
		indentNeeded=true;
		indent++;
	}

	public void writeEnd()
	{
		indent--;
		indentNeeded=true;
		writeIndentation();
		tmpBuf.append(END_MARK+" ");
		indentNeeded=true;
		sendIfLonger(max_packet_size);
	}
	
	/*private boolean atBeginingOfLine()
	{
		if (tmpBuf.length()<1)
		{
			return true;
		}
	
		if (tmpBuf.charAt(tmpBuf.length()-1)=='\n')
		{
			return true;			
		}
		return false;
	}*/
	
	private void writeIndentation()
	{		
		if (indentNeeded)
		{
			writeEoln();
			for (int i=0;i<indent;i++)
			{
				tmpBuf.append("\t");
			}
			indentNeeded=false;			
		}
	}
	
	private void sendIfLonger(int limit)
	{
		int strLen = tmpBuf.length(); 
		if (strLen>limit)
		{
			//debug("sending \""+tmpBuf.toString()+"\"");
			if (cc!=null)
			{
				//cc.writeLine(tmpBuf.toString());
				//tmpBuf.append(" ");
				final String str=tmpBuf.toString();
				cc.write(str);
				//cc.flush();
			}
			if (pw!=null)
			{
				//pw.println(tmpBuf.toString());
				pw.print(tmpBuf.toString()+" ");
			}
			if (os!=null)
			{			
				byte[] b= new byte[strLen+1];
	
			    // Only ascii here, unicode not supported yet
			    for (int i=0; i<strLen; i++)
			    {
			    	b[i]=(byte) tmpBuf.charAt(i);
			    }
			    b[strLen]='\n';

				try {
					os.write(b);
				} catch (IOException e) {
					e.printStackTrace();
					os=null;
				}
			}
			if (sb!=null)
			{
				sb.append(tmpBuf.toString());			
			}
			tmpBuf=new StringBuffer(1400);
		}
		
	}
	
	public void flush()
	{
		sendIfLonger(0);
	}
	
	public void close()
	{
		flush();
		tmpBuf=null;

		if (cc!=null)
		{
			//cc.rwl.writeLock().unlock();
			cc=null;
		}

		if (pw!=null)
		{
			pw.close();
			pw=null;
		}
		
		if (os!=null)
		{
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			os=null;
		}
	}
	
	
    public static boolean isNameOk(String name, int minLength)
    {
    	if (!WordWriter.isStringOkAsName(name))
    	{
  		    return false;
    	}
    	if (name.equals("null"))
    	{
    		debug("null is a reserved word");
  		    return false;
  	    }
	    if (name.length()<minLength)
	    {
	    	debug("name is to short");
	    	return false;
	    }
	    if (name.length()>32)
	    {
	    	//cc.writeLine("to_long name");  				
	    	debug("name is to long");
	    	return false;
	    }
	    /*if (hwc.world.findGameObjRecursive(name) != null)
        {
    		cc.writeLine("Already taken");
	    	return false;
        }*/
	  
	    return true;
    }

	public void endLine()
	{
		writeLine("");
		flush();
	}
    
	/*public void setFlushAfterLf()
	{
		flushAfterLf=true;
	}*/
}