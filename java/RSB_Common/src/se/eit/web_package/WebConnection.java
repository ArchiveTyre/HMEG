/*
WebConnection.java

Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license

Send and receive full lines of ascii strings.


Can also do The WebSocket Protocol. Read more about WebSockets here: 
http://tools.ietf.org/html/rfc6455
http://www.altdevblogaday.com/2012/01/23/writing-your-own-websocket-server/
http://www.jmarshall.com/easy/http/


The ambition for this class is to comply with the minimum requirements for a HTTP 1.1 server.
This instruction was used:
http://www.jmarshall.com/easy/http/#http1.1s7


Simplest use (this text may be outdated):

* Create an instance of this class, there is one constructor for servers and one for clients.
* Start its thread by calling start()
* From the main thread call "readLine" every cycle.
  Calling thread will need to also do "wait" somewhere to avoid using 100% of CPU. 
* To send something to server call writeLine


History:
Created by Henrik Björkman 2006-08-08 for ChartPlotter
2013-05-16 Modified and renamed to be used in RSB. Henrik 
2013-07-31 Trying to implement the WebSocket protocol. Henrik

*/



package se.eit.web_package;

import java.io.File;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


// See also Class Endpoint, perhaps we did not need to implement so much of the web socket protocol here.
// http://docs.oracle.com/javaee/7/api/javax/websocket/Endpoint.html



//This class is the thread that handles all communication with a client
public class WebConnection implements Runnable //extends Thread
{
	static final int INITIAL=0;
	static final int LINE_INPUT=1; // We process all input line by line.
	static final int WEB_SOCKET_HANDSHAKE=2; // We process all input as web socket packets.
	static final int WEB_SOCKET_INPUT=3; // We process all input as web socket packets.
	static final int WEB_SOCKET_INPUT_1=4;
	static final int WEB_SOCKET_INPUT_PAYLOAD=5;
	static final int WEB_SOCKET_INPUT_MASK=6;
	static final int WEB_SOCKET_INPUT_16=7;
	static final int INPUT_CLOSED=8;
	
	public boolean keepAlive=true;
		
	//protected int refNumber; // This is not used locally buy WebConnection, its an optional number that the server can use to remember which client this is. Server can set it to 0 or -1 if not needed.
	public int inputState=INITIAL;
	
	protected Socket socket = null;
	protected DataInputStream in = null;
	protected PrintStream out = null;
	
	protected StringBuffer lineBeingReceived=new StringBuffer(); // Data being received 
	
	public MyBlockingQueue<String> myBlockingQueue;  // Received messages are put in the blocking queue mbq and user can pick them up from there.
	
	protected java.util.Random random=new java.util.Random();
	
	protected static SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz"); // http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
	
	protected String httpVersion="HTTP/1.1";  // http version used by client for connection. Default is 1.1. 
	
	public String httpRootDir; // Used for default http root dir etc
	protected WebFileServer fileServer=null;
	protected WebSocketServer webSocketServer=null;
	WebSocketConnection pct=null;
	WebServer webServer=null;
	int webServerIndex=0;
	long connectedTimeMs=0;
	
	protected class HandShake
	{
		String get=null,
		host=null, 
		post=null, 
		upgrade=null, 
		connection=null, 
		key=null, 
		origin=null,
	    head=null,
	    ifModifiedSince=null,		
	    ifUnmodifiedSince=null;
	}
	HandShake hs=new HandShake();
	
	static protected int nDebug=1000;  // Don't log too much
	
	// WebSocket header
	protected class WebSocketHeader
	{
	    int hdr1;
	    int hdr2;
	    int maskPresent;
	    int len;
	    byte[] buf;
	    int n;
	    byte[] mask=new  byte[4];
	    int opCode=0;
	}
	WebSocketHeader rcv=new WebSocketHeader();
	
	
	public static String className()
	{	
		return "WebConnection";
	}
	
	
	void error(String str)
	{
		WordWriter.safeError("WebConnection("+webServerIndex+"): " + str);
		//WordWriter.safeError("WebConnection("+System.currentTimeMillis()+"): " + str);
	    close();
	    //System.exit(1);
	}
	
	
	// Just for debugging.
	private void debug(String str)
	{
		if (nDebug>0)
		{
	    	WordWriter.safeDebug("WebConnection("+webServerIndex+"): "+str);
			nDebug--;
		}
		else
		{
			if (nDebug==0)
			{
		    	WordWriter.safeDebug("WebConnection("+webServerIndex+"): debugging closed");
				nDebug--;				
			}
		}
	}
	
	
	// Send a line to client
	private void println(String str)
	{
		//debug("println " + str);
		out.print(str+"\r\n");
	}
	
	//To get the first word in a string.
	static public String getFirstWord(String str) 
	{
		int i=0;
		while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {i++;}
		return(str.substring(0,i));
	}
	
	
	//To get a string without the first n words in string str.
	static public String skipWords(String str, int n)
	{
		int i=0;
		 
		while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {i++;}
		 
		while (n>0)
		{
		   while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {i++;}
		   while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {i++;}
		   n--;
		}
		 
		return(str.substring(i));
	}
	
	static public String charToHex(int ch)
	{
		return String.format("%02X", ch);
	}
	
	
	static public String bufferToHex(byte[] buf)
	{
	    StringBuffer hex=new StringBuffer();
	    byte[] b=buf;
	    
	    for(int i=0;i<b.length;i++)
	    {
	    	hex.append(String.format("%02X", b[i])); 
	    }
	    
	    return hex.toString();		
	}
	
	
	// http://stackoverflow.com/questions/4400774/java-calculate-a-sha1-of-a-string
	static public byte[] SHA_1(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
	    md.reset();
	    md.update(key.getBytes("utf8"));
	    
	    byte[] b=md.digest();
	    //debug(bufferToHex(b));
	    
	    return b;
	}
	
	
	
	
	// http://www.altdevblogaday.com/2012/01/23/writing-your-own-websocket-server/
	static public String calcWebSocketKey(String sec_WebSocket_Key)
	{
		String SpecifcationGUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
		String FullWebSocketKey = sec_WebSocket_Key + SpecifcationGUID;
	
		/*
		byte[] KeyHash;
		try {
			KeyHash = SHA_1(FullWebSocketKey);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		*/
	
		byte[] KeyHash;
			try {
				KeyHash = SHA_1(FullWebSocketKey);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		
		
		// http://iharder.sourceforge.net/current/java/base64/
		// http://sourceforge.net/projects/iharder/files/
		String Sec_Websocket_Accept = Base64.encodeBytes(KeyHash);
		//debug(Sec_Websocket_Accept);
		
		return Sec_Websocket_Accept;
	}
		
	public void debug2(String str1, String str2)
	{
		/*if (str2!=null)
		{
			debug(str1+" "+str2);
		}*/
	}
	
	public boolean isWebSocket()
	{
		switch(inputState)
		{
			case WEB_SOCKET_HANDSHAKE:
			case WEB_SOCKET_INPUT:
			case WEB_SOCKET_INPUT_1:
			case WEB_SOCKET_INPUT_PAYLOAD:
			case WEB_SOCKET_INPUT_MASK:
			case WEB_SOCKET_INPUT_16: return true;
			case LINE_INPUT: return false;
			default: debug("isWebSocket don't know"); break; 
		}
		return false;
	}
	
	// This is to be called when a client has connected for plain TCP/IP or WebSocket connection.
	// It is not to be called for if was a simple file request.
	public void createPlayerConnectionThread()
	{
		if (this.myBlockingQueue==null)
		{
		    this.myBlockingQueue=new MyBlockingQueue<String>(32);  // A queue to which WebConnection can put web socket messages 
		    pct = webSocketServer.newSocketServer(this); //new PlayerConnectionThread(config, this, webSocketServer);
		    pct.start();
		}
	}

	
	/*public static String getFilenameWithoutAbsoluterUrl(String filename)
	{
	    URI uri = URI.create(filename);	
		return uri.getPath();
	}*/
	
	// http://stackoverflow.com/questions/4716503/best-way-to-read-a-text-file
		public String readFile(File file)
		{
	       String content = null;
	   
		   try {		   
			   debug2("getAbsolutePath",file.getAbsolutePath());
		   
	       FileReader reader = new FileReader(file);
	       char[] chars = new char[(int) file.length()];
	       reader.read(chars);
	       debug("chars.length "+chars.length);
	       content = new String(chars);
	       reader.close();
	   } catch (IOException e) {
		   debug("readFile "+e);
	       e.printStackTrace();
	   }
	   return content;
	}
	   
	
	// http://stackoverflow.com/questions/4716503/best-way-to-read-a-text-file
	public String readFile(String filename)
	{
	   debug2("readFile", filename);
	   	   
	   File file = new File(filename);
	   
	   return readFile(file);
	}
	
	byte[] readBinFile(Path path)
	{
	  byte[] b=null;
	  try {
		b = Files.readAllBytes(path);
	    } catch (IOException e) {
	    debug("readBinFile "+e);
		e.printStackTrace();
	  }	      	
	  return b;
	}
	
	
	byte[] readBinFile(String filename) 
	{
	  Path path = Paths.get(filename);
	  return readBinFile(path);
	}
	
	public String closeOrKeepStr(boolean keepAlive)
	{
		if (!keepAlive)
		{
			return "close"; 
		}
		else
		{
			return "Keep-Alive";
		}
	}	
	
	private int closeOrKeep(boolean keepAlive)
	{
		if (!keepAlive)
		{
			close();
			return INPUT_CLOSED;
		}
		else
		{
			return INITIAL;
		}
	}
	
	
	public int reply(String reply, String msg, boolean keepAlive)
	{
		String lms=timeMsToString(System.currentTimeMillis());
		debug2("SimpleDateFormat", lms);
		
		println(httpVersion+" "+reply);
		println("Date: "+lms); // Format example: "Date: Fri, 31 Dec 1999 23:59:59 GMT"
		println("Connection "+closeOrKeepStr(keepAlive));
		if (msg!=null)
		{
			println("Content-Type: text/html");
			println("Content-Length: "+msg.length());
			println("");  // this empty line marks the end of the header
			out.print(msg);
		}
		else
		{
			println("");  // this empty line marks the end of the header
		}
		out.flush();
		
		return closeOrKeep(keepAlive);
	}
	
	// Translate a time in ascii string format "Fri, 31 Dec 1999 23:59:59 GMT" 
	// to a long with unit milliseconds, returns zero if if failed.
	public long parseTime(String time)
	{
		try {    
			//debug("time "+time);
			Date date=sdf.parse(time);
			return date.getTime();
		} catch (Exception e) {
			debug("parseTime: Exception '"+e+"' in '"+time+"'");
			//e.printStackTrace();
		}		
		return 0;
	}
	
	
	public String timeMsToString(long lm) throws ArrayIndexOutOfBoundsException
	{
		try{
			final String str=sdf.format(lm);
			//debug("timeMsToString "+str);
			return str;
		}
		catch (Exception e)
		{
			debug("timeMsToString failed with " + lm);
		}
		return "failed to get time & date";
	}
	
	public String getNameAndPath(String httpRootDir, String filename)
	{		
		return httpRootDir+filename;
	}
	

	
	public int	sendFile(WebFileData wfd)
	{
		final long ctms=System.currentTimeMillis();
		String lms=timeMsToString(wfd.lastModified);
		String expiryTimeStr=timeMsToString(ctms+(wfd.maxTimeS*1000));
	    String dateStr=timeMsToString(ctms);
		
		println(httpVersion+" 200 OK");
		println("Date: "+dateStr); // Example: "Date: Fri, 31 Dec 1999 23:59:59 GMT"
		println("Server: www.eit.se/rsb");
		println("Connection "+closeOrKeepStr(keepAlive));
		println("Content-Type: text/html; charset=UTF-8");
		println("Last-Modified: "+lms);
		println("Expires: "+expiryTimeStr);
		if (wfd.maxTimeS>0)
		{
			// http://code.tutsplus.com/tutorials/http-headers-for-dummies--net-8039
			println("Cache-Control: max-age="+wfd.maxTimeS+", public");  
			//println("Cache-Control: max-age="+wfd.maxTimeS);
			//println("Cache-Control: max-age="+wfd.maxTimeS+", must-revalidate");
		}
		else
		{
			debug("no-cache");
			println("Cache-Control: no-cache");
		}
		if (hs.get!=null)
		{
			println("Content-Length: "+wfd.data.length);
			println(""); // One empty line to mark end of header.
			try {
				out.write(wfd.data);
			} catch (IOException e) {
				e.printStackTrace();
				error("failed to write binMsg "+e);
			}
		}
		else if (hs.head!=null)
	    {
			println("");
	    }
		else
		{
			error("not get or head");
			println("");					
		}
		out.flush();

		return closeOrKeep(keepAlive);
	}

	
	// Internal file are generated, they are delivered to WebServer as a binary blob.
	public int sendWebFile(String filename, WebFileData webFileData)
	{
		if ((hs.ifModifiedSince!=null) && (hs.ifModifiedSince.length()>0))
		{
			final long ims = parseTime(hs.ifModifiedSince);
			final long imss= ims/1000; // truncate to seconds since web browser have only seconds not the milliseconds
			final long lm = webFileData.lastModified/1000; 
			if (imss>=lm)
			{
				// the file has not been modified since so we shall not send the file, just tell client that.
				debug("not modified, file "+ filename + ", since '"+hs.ifModifiedSince+"', "+imss+", lm "+lm+", sys time "+System.currentTimeMillis()/1000);
				
				// TODO: "304 Not Modified" does not always work. Sometimes I must comment out this line. But Why?
				// return reply("304 Not Modified",null , keepAlive); 
			}
			else
			{
				debug("modified, file "+ filename + ", since '"+hs.ifModifiedSince+"', "+imss+", lm "+lm+", sys time "+System.currentTimeMillis()/1000);
			}
		}
		else
		{
			debug("no ifModifiedSince, file "+filename);
		}
		
		// not sure on this one
		if (hs.ifUnmodifiedSince!=null)
		{
			final long iums = parseTime(hs.ifUnmodifiedSince);
			if (iums<=webFileData.lastModified)
			{
				// the file has been modified since so we shall not send the file, just tell client that
				debug(filename+" Precondition Failed");
				return reply("412 Precondition Failed",null, keepAlive);
			}
			else
			{
				debug(filename+" unmodified");
			}
		}
	
		return sendFile(webFileData);
	}
	
	public String getPathWithinHttpDir(String path)
	{
		final int rootLen=httpRootDir.length();
		return path.substring(rootLen);		
	}

	
	public int sendDirectoryListing(String filename, Path path)
	{
		debug("sendDirectoryListing"+filename+" "+path.toString());
	   	WebFileData wfd=new WebFileData();

		StringBuffer msg=new StringBuffer();
		File folder = path.toFile();
	
		File[] listOfFiles = folder.listFiles();
		
		//File f = new File("ugl_save_world.txt");
		
		for (int i = 0; i < listOfFiles.length; i++) 
		{					
			String url=getPathWithinHttpDir(listOfFiles[i].toString());
			msg.append("<a href="+url+">"+listOfFiles[i].getName()+"</a><br>\n");
		}
		wfd.data=msg.toString().getBytes();
		wfd.lastModified = path.toFile().lastModified();
		return sendWebFile(filename ,wfd);
	}

	public int sendRegularFile(String filename, Path path)
	{
		debug("sendRegularFile"+filename+" "+path.toString());

		WebFileData wfd=new WebFileData();
		wfd.lastModified = path.toFile().lastModified();

		// Special for this file. 
		if (path.toFile().getName().equals("set_server_url.js"))
		{
			// Special treatment for this file. TODO: This should be done via the WebFileServer, so that WebServer can be generic.
			String msg=null;
					
			msg="var wsUri = \"ws://"+hs.host+"/\";\r\n"+msg;					
			//msg += readFile(path.toFile());
			wfd.data=msg.getBytes();
		}
		else
		{
			// Read the file
			wfd.data = readBinFile(path);
		}
    	return sendWebFile(filename, wfd);
	}
	
	public int sendSpecialFile(String filename)
	{
		String msg=null;
				
		WebFileData wfd=new WebFileData();
		wfd.lastModified = System.currentTimeMillis();			
		msg="var wsUri = \"ws://"+hs.host+"/\";\r\n"+msg;					
		wfd.data=msg.getBytes();
		return sendWebFile(filename, wfd);
	}
	
	public int loadAndSendFromFileSystem(String filename)
	{

		final String nameAndPath=getNameAndPath(httpRootDir,filename);
	
	    // Make file path
	    Path path = Paths.get(nameAndPath);
	    debug("loadAndSendFromFileSystem '"+ filename+"' "+path.toAbsolutePath());

	    
	    // If it is a directory, perhaps we shall send the index.html file instead?
	    if (path.toFile().isDirectory())
	    {     	    	
	    	Path indexFile = Paths.get(path.toAbsolutePath()+"/index.html");
	        //debug("indexFile "+ path.toAbsolutePath());
	    	
	    	if ((indexFile.toFile().exists()) && (indexFile.toFile().canRead()))
	    	{
	    		path = indexFile;
			    debug("Remapped to '"+path.toAbsolutePath()+"'");
	    	}
	    }
	    
	    
	    if (path.toFile().exists()) 
	    {
	    	if (path.toFile().canRead())
	    	{
		    	// File exist at httpRootDir and is readable
		    	
		 		if (!path.toFile().isFile())
		 		{
		  	    	if (path.toFile().isDirectory())
		  	    	{
			  	    	//debug("directory '"+filename+"' '"+nameAndPath+"'");
		  	    		return sendDirectoryListing(filename, path);
		  	    	}
		  	    	else
		  	    	{
			  	    	// We shall send some error message.
			  	    	debug("not file and not directory '"+filename+"' '"+nameAndPath+"'"); 
		  	    		return reply("403 Forbidden","403 Forbidden", false);
		  	    	}
		 		}
		 		else
		 		{
		 			return sendRegularFile(filename, path);
		 		}
		    }
	    	else
	    	{
	  	    	debug("forbidden '"+filename+"' '"+nameAndPath+"'"); 
	  	    	return reply("403 Forbidden","403 Forbidden", false);	    		
	    	}
	    }
	    else
	    {
			if (path.toFile().getName().equals("set_server_url.js"))
			{
				// Special treatment for this file. TODO: This should be done via the WebFileServer, so that WebServer can be generic. Problem is: that code don't know the host name/ip.
				return sendSpecialFile(filename);
			}
		    else
		    {
	  	    	debug("not found '"+filename+"' '"+nameAndPath+"'"); 
	  	    	return reply("404 Not Found","404 Not Found", false);
		    }
	    }
	}
	
	// This is to be called when we think we have received the full http header
	public int handleWebSocketHeaderAndSendReply() throws NumberFormatException, ArrayIndexOutOfBoundsException
	{		
		//debug("handleWebSocketHeaderAndSendReply");		
	
		String filename=null;
	
		if (hs.post!=null)
		{
			debug2("POST",hs.post);
			filename=WordReader.getWord(hs.get);
			httpVersion=WordReader.getRemainingLine(hs.get);
			return reply("501 Not Implemented", "501 Not Implemented", false);						
		}
		else if (hs.get!=null)
		{
			debug2("GET",hs.get);
			filename=WordReader.getWord(hs.get); // filename is the first word in the line that is stored in hs.get
			httpVersion=WordReader.getRemainingLine(hs.get); // httpVersion is the rest of the line
		}
		else if (hs.head!=null)
		{
			debug2("HEAD",hs.head);
			filename=WordReader.getWord(hs.head);
			httpVersion=WordReader.getRemainingLine(hs.head);			
		}
		else
		{
			return reply("400 Bad Request", "<html><body>No head or get received</body></html>", false);			
		}
	
		// This is a preparation for expected future changes in HTTP, currently with version 1.1 is does nothing.
	    URI uri = URI.create(filename);	
	    filename = uri.getPath();
	
		// Check that it is a supported version of HTTP.
		if (! ((httpVersion.equals("HTTP/1.0")) || (httpVersion.equals("HTTP/1.1")) || (httpVersion.equals("HTTP/1.2"))) )
		{
			debug("not yet supported http version "+httpVersion);
			return reply("400 Bad Request", "<html><body>Only HTTP/1.0 and HTTP/1.1 is supported by this server</body></html>", false);			
		}
		
		debug2("Host:",hs.host);
		debug2("Upgrade:",hs.upgrade);
		debug2("Connection:",hs.connection);
		debug2("Sec-WebSocket-Key:",hs.key);
		debug2("Origin: ",hs.origin);
				
		debug2("filename: ",filename);
		
		if (hs.host==null) 
		{	
			if (httpVersion.equals("HTTP/1.1"))
			{
				// Client did not say host, in 1.1 it must do so.
				return reply("400 Bad Request", "<html><body><h2>No Host: header received</h2>HTTP 1.1 requests must include the Host: header.</body></html>", false);
			}
			else if (httpVersion.equals("HTTP/1.2"))
			{
				hs.host=uri.getHost()+":"+uri.getPort();
			}
		}
		
		// hs.ifModifiedSince="Fri, 31 Dec 1999 23:59:59 GMT";
		
		if ((hs.upgrade!=null) && (hs.connection!=null) && (hs.key!=null))
		{
			debug("all mandatory parts of header received for web socket upgrade");
	
			String replyKey=calcWebSocketKey(hs.key);
			
			if (webSocketServer==null)
			{
				// We have received upgrade request to web socket but we do not support web socket connection in this configuration.
				// Will try to reply with 404.
     	    	reply("404 Not Found","404 Not Found", false); 	    						
		    	return INPUT_CLOSED;				
			}
			else if (hs.upgrade.equals("websocket"))
			{
				debug("sending reply websocket upgrade "+webServerIndex+" "+this.getTcpInfo());
				println("HTTP/1.1 101 Switching Protocols");
				println("Upgrade: websocket");
				println("Connection: Upgrade");
				println("Sec-WebSocket-Accept: "+replyKey);
				println("");
				flush();
				//tc.writeWebSocketLine("hello");
				
				// Create a PlayerConnectionThread to handle the client that has connected via web socket.
				createPlayerConnectionThread();
				
		    	return WEB_SOCKET_INPUT;
			}
			else
			{
				throw new NumberFormatException("it was not a valid upgrade request");
			}
		}
		else
		{
			// Probably normal http GET or HEAD request.
			// http://www.jmarshall.com/easy/http/
	
			// Special, we don't support directories yet anyway.
			/*if (filename.equals("/")) 
			{
				filename="/index.html";
				debug("Remapped to "+filename);
			}*/
			
			
			
			//String currentDir = System.getProperty("user.dir");
			debug2("httpRootDir", httpRootDir);
	
			// Check that filename does not contain forbidden characters.
			if (!WordWriter.isFilenameOk(filename))
			{
	     	    	debug("permission denied"); 
	     	    	// We should send a "you are not permitted..." , but for now we pretend it didn't exist instead
	     	    	reply("404 Not Found","404 Not Found", false); 	    						
			    	return INPUT_CLOSED;
			}
	
			if (fileServer!=null)
			{
		    	WebFileData webFileData = fileServer.getFileData(filename);
	
	    		if (webFileData!=null)
	    		{
	    			return sendWebFile(filename, webFileData);
	    		}
	    		else
	    		{
	    			return loadAndSendFromFileSystem(filename);			
	    		}
			}
			else
			{
				return loadAndSendFromFileSystem(filename);				
			}
		}
	}
	
	
	/*public void setHttpRootDir(String httpRootDir)
	{
	  this.httpRootDir=httpRootDir;
	}*/
	
	
	// Connect with a client. This is used by servers.
	// Received messages are put in the blocking queue myBlockingQueue and user can pick them up from there.
	public WebConnection(Socket client_socket, String httpRootDir, WebFileServer fileServer, WebSocketServer webSocketServer, WebServer webServer, int webServerIndex) 
	{
	  socket = client_socket;
	  //this.refNumber=refNumber;
	  this.httpRootDir=httpRootDir;
	  this.fileServer=fileServer;
	  this.webSocketServer=webSocketServer;
	  this.webServer=webServer;
	  this.webServerIndex=webServerIndex;
	  this.connectedTimeMs=System.currentTimeMillis();
	  
	  //debug("test "+ getFilenameWithoutAbsoluterUrl("http://www.somehost.com/path/file.html"));
	  
	  sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	  
	  try 
	  { 
	    in = new DataInputStream(socket.getInputStream());
	    out = new PrintStream(socket.getOutputStream());
	    
	    //in.
	  }
	  catch (IOException e) 
	  {
		  System.out.flush();
	      System.err.println("Exception while getting socket streams: " + e);
	      close();
	      return;
	  }
	}
	
	
	
	
	// Make a connection to server. This is used by clients.
	// This is currently only used by our out dated native java client. But the plan is that servers shall connect to a master server. Then this may needed more.
	public WebConnection(String hostname, int port, MyBlockingQueue<String> myBlockingQueue)
	{
		this.myBlockingQueue=myBlockingQueue;
		this.inputState=LINE_INPUT;
		
		if ((hostname!=null) && (port>0) && (socket==null))
	    {
	       debug("trying to connect to "+hostname+":"+port);
	       try 
	       {
	         // Create a socket to communicate to the specified host and port
	         socket = new Socket(hostname, port);
	    
	         // Create streams for reading and writing lines of text
	         // from and to this socket.
	         in = new DataInputStream(socket.getInputStream());
	         out = new PrintStream(socket.getOutputStream());
	
	         debug("Connected to " + socket.getInetAddress() + ":"+ socket.getPort());        
	       }      
	       catch (IOException e) 
	       {
	    	 close();
	         error("connect failed "+e);	         
	       }
	       finally 
	       {
	         // Always be sure to close the socket if any
	         //try { if (socket != null) socket.close(); } catch (IOException e2) { ; }
	       }
	    }
	    else
	    {
	    	close();
	    	error("could not connect");
	    }
	}
	
	
	private void handleReceivedHandShake(String str)	
	{
		debug("handleReceivedHandShake: "+str);
		String cmd=getFirstWord(str);
		
		if (cmd.equals("Host:"))
		{
			hs.host=skipWords(str,1);
		}
		else if (cmd.equals("Upgrade:"))
		{
			hs.upgrade=skipWords(str,1);
		}
		else if (cmd.equals("Connection:"))
		{
			hs.connection=skipWords(str,1);
		}
		else if (cmd.equals("Sec-WebSocket-Key:"))
		{
			hs.key=skipWords(str,1);
		}
		else if (cmd.equals("Origin:"))
		{
			hs.origin=skipWords(str,1);
		}
		else if (cmd.equals("If-Modified-Since:"))
		{
			hs.ifModifiedSince=skipWords(str,1);			
		}
		else if (cmd.equals("If-Unmodified-Since:"))
		{
			hs.ifUnmodifiedSince=skipWords(str,1);			
		}
		else if (cmd.equals("User-Agent:"))
		{
			//debug("ignored header "+str);
		}
		else if (cmd.equals("Accept:"))
		{
			//debug("ignored header "+str);
		}
		else if (cmd.equals("Accept-Language:"))
		{
			//debug("ignored header "+str);
		}
		else if (cmd.equals("Accept-Encoding:"))
		{
			//debug("ignored header "+str);
		}
		else if (cmd.equals("Cookie:"))
		{
			//debug("ignored header "+str);
		}
		else if (cmd.equals("Sec-WebSocket-Version:"))
		{
			//debug("ignored header "+str);
		}
		else if (cmd.equals("Pragma:"))
		{
			//debug("ignored header "+str);
		}		
		else if (cmd.equals("Cache-Control:"))
		{
			//debug("ignored header "+str);
		}		
		else if (cmd.equals("Referer:"))
		{
			//debug("ignored header "+str);
		}		
		else
		{
			debug("unknown header "+str);
		}
	
		
	
	}
	
	private void handleReceivedData()
	{
		//String str=new String(rcv.buf);
		//debug("handleReceivedData: "+str);
		
		if (rcv.opCode==8)
		{
			// This was a close form other end
			debug("pair close");
			writeWebSocketLine("",8);
			close();
		}
		else if (rcv.opCode==1)
		{
			// This was normal data, put it in the queue
			myBlockingQueue.put(new String(rcv.buf));
		}
		else if (rcv.opCode==9)
		{
			// This was ping from client, we should reply with pong
            // https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API/Writing_WebSocket_servers
            // Pings and Pongs: The Heartbeat of WebSockets
			writeWebSocketLine(new String(rcv.buf),0xA); //TODO Perhaps writeWebSocketLine should be changed to take byte[] as input so we dont need to convert to string here.
		}
		else if (rcv.opCode==0xA)
		{
			// This was pong from client, just ignore
		}
		else
		{
			error("opCode "+rcv.opCode+" is not supported");
		}
	}
	
	// This will figure out the next state to go to when we have payload length.
	public void nextStateAfterLen()
	{	
		rcv.n=0;
		if (rcv.maskPresent!=0)
		{
			inputState=WEB_SOCKET_INPUT_MASK;
		}
		else 
		{
			nextStateAfterMask();
		}
	}	
	
	public void nextStateAfterMask()
	{
		// create input buffer
		rcv.buf=new byte[rcv.len];
		rcv.n=0;
	
		if (rcv.len>0)
		{
			inputState=WEB_SOCKET_INPUT_PAYLOAD;
		}
		else
		{
			handleReceivedData();
	    	inputState=WEB_SOCKET_INPUT;
		}
	}
	
	// This class is not a thread on its own.
	// So the user is expected to call this method regularly.
	public void run()
	{
	    //debug("begin");
	    try
	    {
			while (socket!=null)
			{
				// Reading just one character at a time. 
				// TODO: we might need to optimize this. Read a buffer of available characters and then evaluate them.
	            final int ch=in.read();
		            
	            if (ch<0)
	            {
	            	throw new IOException("connection lost, in.read() gave "+ch);
	            }
	
	            // Logging this will result in very much logging, but useful in some rare cases like writing a new client.
				//debug("ch "+ch);
	            
	            // main state machine of the connection
				switch(inputState)
				{
					case INITIAL:
					{
						// Receive character by character until there is a line feed (LF).
						if (ch=='\n')
			            {
							// We have a complete line (it ended with line feed)
							String str=lineBeingReceived.toString();
							String cmd=getFirstWord(str);
							
							if (cmd.equals("GET"))
							{
								// example: GET /path/to/file/index.html HTTP/1.1
								debug("INITIAL: GET: looks like WebSocket or HTTP, "+webServerIndex+", '"+this.getTcpInfo()+"'");
								hs.get=skipWords(str,1);
								inputState=WEB_SOCKET_HANDSHAKE;
							}
							else if (cmd.equals("HEAD"))
							{
								// example: HEAD /path/to/file/index.html HTTP/1.1
								debug("INITIAL: HEAD: looks like WebSocket or HTTP, "+webServerIndex+", '"+this.getTcpInfo()+"'");
								hs.head=skipWords(str,1);
								inputState=WEB_SOCKET_HANDSHAKE;
							}
							else if ((cmd.equals("POST:")) || (cmd.equals("PUT:")) || (cmd.equals("DELETE:")) || (cmd.equals("OPTIONS:")) || (cmd.equals("TRACE:")))
							{
								debug("INITIAL: '"+str+"'");
								
								hs.post=skipWords(str,1);
							}
							else
							{
								// probably plain TCP/IP from our native clients.
								// Dont't expect HTTP header or web socket.
								debug("INITIAL: plain tcp/ip, "+webServerIndex+", '"+this.getTcpInfo()+"'");

								createPlayerConnectionThread();

								myBlockingQueue.put(str);
								inputState=LINE_INPUT;								
							}
		            	    lineBeingReceived = new StringBuffer();
			            }
			            else if (ch=='\r')
			            {
			            	// just ignore all carriage return
			            	debug("INITIAL: ignored carriage return");
			            }
			            else
			            {
							// We don't have a complete line yet, append to the one we are receiving.
			            	lineBeingReceived.append((char)ch);
			            }
			            break;												
					}
					case LINE_INPUT:
					{
						// In this state we receive characters until we have a line, not using WebSockets. Lines are ended with LF.  
						if (ch=='\n')
			            {
							// We have a complete line (it ended with line feed)
							// Put it in the receive queue.
							
		            	    // Logging here may be to much in normal operation but useful sometimes.
		            	    //debug("LINE_INPUT: "+lineBeingReceived);
		            	    
		            	    myBlockingQueue.put(lineBeingReceived.toString());
		            	    lineBeingReceived = new StringBuffer();
			            }
			            else if (ch=='\r')
			            {
			            	// just ignore all carriage return
			            	debug("LINE_INPUT: ignored carriage return");
			            }
			            else
			            {
							// We don't have a complete line yet, append to the one we are receiving.
			            	lineBeingReceived.append((char)ch);
			            }
			            break;
					}
					case WEB_SOCKET_HANDSHAKE:
					{
						// Receive character by character until there is an LF.
			            if (ch=='\n')
			            {
							// We have a complete line (it ended with line feed)
			            	// Process the received line
	
			            	if (lineBeingReceived.length()>0)
		            	    {
			            		// Non empty lines extends the header.
			            	    //debug("HANDSHAKE "+lineBeingReceived);
			            	    String str=lineBeingReceived.toString();
			            	    handleReceivedHandShake(str);
			            	    lineBeingReceived = new StringBuffer();
		            	    }
			            	else
			            	{			            	
		            	    	// An empty line marks then end of the header, we have received the full header, change state
			            		inputState=handleWebSocketHeaderAndSendReply();
			            	    //mbq.put("");
			            	}
				            	    
			            }
			            else if (ch=='\r')
			            {
			            	// just ignore all carriage return
			            	debug("WEB_SOCKET_HANDSHAKE: ignored carriage return");
			            }
			            else
			            {
			            	lineBeingReceived.append((char)ch);
			            }
			            break;
					}
					case WEB_SOCKET_INPUT:
					{
						//debug("WEB_SOCKET_INPUT: 0x" + charToHex(ch)+" "+ch);
						
		            	// Got first byte of header, now expecting second
		            	// Read more about the header at: http://stackoverflow.com/questions/14174184/what-is-a-mask-in-a-tcp-frame-websockets
						rcv.hdr1=ch;
		            	//debug("hdr1 "+rcv.hdr1);
		            	inputState=WEB_SOCKET_INPUT_1;
			    	    
						break;
					}
					case WEB_SOCKET_INPUT_1:
					{
						//debug("WEB_SOCKET_INPUT_1: 0x" + charToHex(ch)+" "+ch);
	
						// Got second byte of header 			            	
						rcv.hdr2=ch;
		            	//debug("hdr2 "+rcv.hdr2);
	
		            	
		            	// Figure out size of header.
		            	int finalFragment=rcv.hdr1>>7;
		   				rcv.opCode=rcv.hdr1&15;
		            	rcv.len=rcv.hdr2&0x7F;
		            	rcv.maskPresent=rcv.hdr2>>7;
		            	
			            if (finalFragment!=1)
			            {
		            		error("not implemented finalFragment "+finalFragment);				            	
			            }
				            
			            /*if ((rcv.opCode!=1) && (rcv.opCode!=8))
			            {
		            		debug("opCode " + rcv.opCode + " is not implemented");				            	
			            }*/
		            			            	
			            rcv.n=0;
	
			            if (rcv.len==126)
		            	{
			            	rcv.len=0;
		            		inputState=WEB_SOCKET_INPUT_16;
		            	}
		            	else if (rcv.len==127)
		            	{
		            		error("64 bit length is not implemented");
		            		//inputState=WEB_SOCKET_INPUT_64;
		            	}
		            	else
		            	{
		            		nextStateAfterLen();		            		
		            	}			            	
			            	
			    	    
						break;
					}
					case WEB_SOCKET_INPUT_16:
					{
						//debug("WEB_SOCKET_INPUT_16: 0x" + charToHex(ch)+" "+ch);
						rcv.len=(rcv.len<<8)+(ch&0xFF);
						rcv.n++;
						if (rcv.n>=2)
						{	
							//debug("rcv.len " + rcv.len);
	            			nextStateAfterLen();		            			
						}
						break;
					}
					case WEB_SOCKET_INPUT_MASK:
					{
						//debug("WEB_SOCKET_INPUT_MASK: 0x" + charToHex(ch)+" "+ch);
			            
						rcv.mask[rcv.n++]=(byte)ch;
						
			            if (rcv.n>=rcv.mask.length)
			            {
			            	//debug("all of mask received");
			            				   
			            	nextStateAfterMask();
			            }
			            
						break;
					}
					case WEB_SOCKET_INPUT_PAYLOAD:
					{
			            int m=ch ^ rcv.mask[rcv.n&3];
	
			            //debug("ch "+ ch+" "+m);
			            
			            rcv.buf[rcv.n]=(byte)m;
			            rcv.n++;
						
			            if (rcv.n>=rcv.buf.length)
			            {
			            	handleReceivedData();
			            			            	    
			            	rcv.n=0;
			            	inputState=WEB_SOCKET_INPUT;
			            }
			            
						break;
					}
					default:
					{
						error("Illegal state "+inputState);
						break;
					}
				}
		     }
	    }
	    catch( IOException e ) 
	    {
	        debug("IOException " + e );
	        close();
	    }
	    catch(NumberFormatException e)
	    {
	        debug("NumberFormatException " + e );
	        close();	    	
	    }	    
	    catch( ArrayIndexOutOfBoundsException e)
	    {
	        debug("ArrayIndexOutOfBoundsException " + e );
	        close();	    	
	    }
	    
	    //debug("end");
	}
	
	/*
	// deprecated	
	private synchronized String readLine()
	{
		//debug("readLine");
		return readLineNonBlocking();		
	}
	*/
	
	// Non blocking read.
	// To be called by users of this class when they expect to get a full line, terminated by CR or LF.
	// The CR or LF are implicit, that is not included in the line returned.
	// If this is used the caller must either call this regularly (or call "process") and do some waiting somewhere.
	// Returns non zero if a full line has been received.
	public String readLineNonBlocking() throws IOException
	{
		try {
			return readLine(0);
		} catch (InterruptedException e) {
			// do nothing, this is normal
		}
		return null;
	}
	
	
	// Blocking read.
	// The call will block until a full line has been received.
	// It will throw InterruptedException if there is no reply from other end within time given by timeout_ms.
	// It will throw IOException if connection is no longer open.
	// Perhaps we should move this method to ConnectionThread
	public String readLine(long timeout_ms) throws InterruptedException, IOException
	{
		String str=myBlockingQueue.take(timeout_ms);
		if ((str==null) && !isOpen())
		{
			debug("not open");
			throw new IOException("closed");
		}
		return str;
	}
	
	
	/*
	public synchronized void myWait(int time_ms)
	{
	    try 
	    {
	    	this.wait(time_ms);
	    	//wait();
	    }  
	    catch (InterruptedException e) {;}
	}
	*/
	
	
	
	public static String localHostName()
	{
	     String localName="unknown";
	     try
	     {
	           InetAddress inet_address=InetAddress.getLocalHost();
	           localName=inet_address.getHostName();
	     }
	     catch (UnknownHostException e)
	     { 
	    	 System.err.println("localHostName "+e);
	     }
	     return localName;
	}
	
	
	// This might block, will consider fixing that later.
	// This method will not consider WebSockets so for now don't use it.
	public synchronized void write(String str)
	{		
		/*
	    debug("write "+str);
		if (isOpen())
		{
	        out.print(str);
		}
	    else
	    {
	        error("not connected");
	    }
	    */
		switch(inputState)
		{
			case INITIAL:
			{
				debug("Don't know if client is on WebSocket or plain socket yet, dropped message: "+str);
				break;
			}
			case WEB_SOCKET_HANDSHAKE:
			{
				debug("Can not send to client yet, dropped message: "+str);
				break;
			}
			case LINE_INPUT:
			{
				out.print(str);
				break;
			}
			case WEB_SOCKET_INPUT:
			case WEB_SOCKET_INPUT_1:
			case WEB_SOCKET_INPUT_PAYLOAD:
			case WEB_SOCKET_INPUT_MASK:
			case WEB_SOCKET_INPUT_16:
			{
				writeWebSocketLine(encodeAAO(str), 1);
				break;
			}
			case INPUT_CLOSED:
			{
				debug("INPUT_CLOSED: "+str);
				break;
			}
			default:
			{
				error("Illegal state in writeWebSocketLine");
				break;
			}
		}
		
	}
	
	// This might block, will consider fixing that later.
	/*
	public synchronized void writeLine(String str)
	{		
	    //debug("writeLine "+str);
	    
		if (isOpen())
		{
	        out.println(str);
	        //out.flush(); // is this really needed or is it superfluous?
		}
	    else
	    {
	        error("not connected");
	    }
	}
	*/
	
	public synchronized void writeLine(String str)
	{
	    //debug("writeWebSocketLine: "+str);
		
		switch(inputState)
		{
			case INITIAL:
			{
				debug("Don't know if client is on WebSocket or plain socket yet, dropped message: "+str);
				break;
			}
			case WEB_SOCKET_HANDSHAKE:
			{
				debug("Can not send to client yet, dropped message: "+str);
				break;
			}
			case LINE_INPUT:
			{
				out.println(str);
				break;
			}
			case WEB_SOCKET_INPUT:
			case WEB_SOCKET_INPUT_1:
			case WEB_SOCKET_INPUT_PAYLOAD:
			case WEB_SOCKET_INPUT_MASK:
			case WEB_SOCKET_INPUT_16:
			{
				writeWebSocketLine(encodeAAO(str), 1); // 2014-02-01
				break;
			}
			case INPUT_CLOSED:
			{
				debug("INPUT_CLOSED: "+str);
				break;
			}
			default:
			{
				error("Illegal state in writeWebSocketLine");
				break;
			}
		}
	}
		
	public String encodeAAO(String str)
	{
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<str.length();i++)
		{
			char ch=str.charAt(i);
	
			//debug("ch "+ch+" "+String.format("%02X", ch));				
			
			// http://www.tiger.se/dok/koder.html
			// http://www.w3.org/TR/REC-html40/sgml/entities.html
			if (((ch>=' ') && (ch!='&') && (ch<127)) || (ch=='\n') || (ch=='\t'))
			{
				sb.append(ch);				
			}
			else
			{
				sb.append("&#"+(int)ch+";");
			}						
		}
		return sb.toString();
	}
	
	
	// In this method there is a warning about "Comparing identical expressions". There don't seem to be a way to turn only that one off.
	// http://stackoverflow.com/questions/6996631/how-to-specifically-suppress-comparing-identical-expressions-in-eclipse-helios
	//@SuppressWarnings("all")
	// Commented out final instead.
	//
	public synchronized void writeWebSocketLine(String str, int opCode)
	{		
	    
		if (isOpen())
		{
			final int finalFragment=1;
		    /*final*/ int maskPresent=0; // This is supposed to be 1 but that did not work for some unknown reason.
			final long payLoadLen=str.length();
			final int r=random.nextInt();
			
			int[] mask= {(r>>24)&0xff,(r>>16)&0xff,(r>>8)&0xff,(r>>0)&0xff}; 
			//int[] mask= {0,0,0,0}; 
			//int[] mask= {1,1,1,1}; 
			
			final int hdr1=(finalFragment<<7) | (opCode&0xF);
			int hdr2=(maskPresent<<7);
			long bufLen=payLoadLen+2+((maskPresent!=0)?4:0);
	
			
			if (payLoadLen<126)
			{
				hdr2|=payLoadLen;
			}
			else if (payLoadLen>0xFFFF)
			{
				// See https://tools.ietf.org/html/rfc6455 page 31

				hdr2|=127;  // 127 = 0x7F    frame-payload-length-63
	    		bufLen+=8;			
				if (bufLen>0x7FFFFFFF)
				{
					error("Larger than 0x7FFFFFFF will not work with 'new byte(bufLen)'");
				}
			}
			else
			{
	    		hdr2|=126;  // 126 = 0x7E    frame-payload-length-16
	    		bufLen+=2;
			}
			
	
			//debug("hdr1 "+hdr1);
			//debug("hdr2 "+hdr2);
			
		
			byte[] buf=new byte[(int)bufLen];
			int n=0;
			
			buf[n++]=(byte)hdr1;
			buf[n++]=(byte)hdr2;
			
			if (payLoadLen>0xFFFF)
			{
				buf[n++]=(byte)((payLoadLen>>56)&0xFF);
				buf[n++]=(byte)((payLoadLen>>48)&0xFF);
				buf[n++]=(byte)((payLoadLen>>40)&0xFF);
				buf[n++]=(byte)((payLoadLen>>32)&0xFF);
				buf[n++]=(byte)((payLoadLen>>24)&0xFF);
				buf[n++]=(byte)((payLoadLen>>16)&0xFF);
				buf[n++]=(byte)((payLoadLen>>8)&0xFF);
				buf[n++]=(byte)(payLoadLen&0xFF);				
			}
			else if (payLoadLen>=126)
			{
				buf[n++]=(byte)((payLoadLen>>8)&0xFF);
				buf[n++]=(byte)(payLoadLen&0xFF);				
			}
			
			
			if (maskPresent!=0)
			{
				buf[n++]=(byte)mask[0];
				buf[n++]=(byte)mask[1];
				buf[n++]=(byte)mask[2];
				buf[n++]=(byte)mask[3];
	    		for (int i=0;i<payLoadLen;i++)
	    		{
	    			int ch=str.charAt(i);
	    			buf[n]=(byte)(ch ^ mask[i&3]);
	    			//debug("ch "+ch+ " "+(int)buf[n]);
	    			n++;
	    		}
			}
			else
			{
	    		for (int i=0;i<payLoadLen;i++)
	    		{
	    			int ch=str.charAt(i);
	    			//debug("ch "+ch+" "+String.format("%02X", ch));
	    			buf[n++]=(byte)(ch);
	    		}
				
			}
					
			
			//debug("sending packet "+bufferToHex(buf));
			/*{
			    String tmp=new String(buf);
			    debug("tmp: "+tmp);
			}*/
			
			try {
				out.write(buf);
			} catch (IOException e) {
				e.printStackTrace();
				error(e.getMessage());
			}
			out.flush();
		}
	    else
	    {
	        error("not connected");
	    }
	    
	}
	
	
	
	public synchronized String getTcpInfo()
	{
		if (socket!=null)
		{
			SocketAddress sa = socket.getRemoteSocketAddress();
			return sa.toString();
		}
		return "null";
	}
	
	public synchronized void flush()
	{
		out.flush();
	}
	
	public synchronized void close()
	{
		// TODO: 
		// If this was a web socket, send close message to clients websocket
		// According to the protocol spec v76 (which is the version that browser with current support implement):
		// To close the connection cleanly, a frame consisting of just a 0xFF byte followed by a 0x00 byte is sent from one peer to ask that the other peer close the connection.
		// Or is the above out dated so it no longer needed?
		
		
		if (out!=null)
		{
			flush();
		}
		
		if (inputState!=INPUT_CLOSED)
		{
			inputState=INPUT_CLOSED;
			debug2("closing", httpVersion);
		}
		
		try 
		{
		   // tell PlayerConnectionThread that we closed.
		   if (pct!=null)
		   {
			   // PlayerConnectionThread will probably also call close so to avoid eternal recursion set pct to null before calling close.
			   WebSocketConnection tmp=pct;
			   pct=null;
			   tmp.close();
		   }
			
		   if (in!=null)
		   {
		     in.close();
		     in=null;
		   }
		   if (out!=null)
		   {
		     out.close();
		     out=null;
		   }
		   if (socket!=null)
		   {
			 debug("close socket "+socket.getInetAddress()+" "+socket.getPort()+" "+socket.getLocalPort());
		     socket.close();
		     socket=null;
		   }
		   
		   // Tell webServer that we have closed
		   if (webServer!=null)
		   {
			   // WebServer will probably also call close so to avoid eternal recursion set webServer to null before calling close.
			   WebServer tmp=webServer;
			   webServer=null;
			   tmp.removePlayerConnectionThread(webServerIndex);
		   }
		}
		catch( IOException e ) 
		{
		    error("close: IOException " + e );
		}
		
	}
		
	
	public boolean isOpen()
	{
		/*if (!this.isAlive())
		{
			error("thread not running");
			return false;
		}*/
		
		return (socket!=null);
	}
	
	
	public void finalize()
	{
	    debug("TcpClient: finalize");
	    close();
	}
	
	
	
	public String getConnectionTime()
	{
		return timeMsToString(connectedTimeMs);
	}
	
	
	
}