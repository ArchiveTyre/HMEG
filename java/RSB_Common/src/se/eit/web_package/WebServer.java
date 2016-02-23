/*
WebServer.java

Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license

The plan is to release this web server package under GPL v3, (but it is not yet so).


This is the main class in the web package.

To start a web server create an instance of this class. See the constructor.



History:
2014 Created by Henrik Björkman
*/


package se.eit.web_package;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
//import se.eit.db_package.DbSubRoot;

public class WebServer implements Runnable {
	String httpRootDir;
	WebSocketServer webSocketServer=null;
	
	WebConnection pctList[]=new WebConnection[4];  // Perhaps we should used DbList here? But then we need to rename DbList to WebList and move it to this package since we don't want this package to be depending on db_package. Will leave it as is for now (it works after all). Oh wait, a class called MyDynamicSizeList has been created for this but is not yet used.
	
    public final static int DEFAULT_PORT = 8080;
    protected int port;  // This is the port number to use when opening the tcp/ip server port.
    protected ServerSocket serverSocket;  // this is our tcp/ip server socket, clients connect to it.
    WebFileServer webFileServer;
    
	public void debug(String str)
	{
        WordWriter.safeDebug("WebServer: "+str);
	}
	  
	public void error(String str)
	{
        WordWriter.safeError("WebServer: "+str);
	    close();
	    System.exit(1);
	}
	
	  
	// Create a ServerSocket to listen for connections on.
	// This class is a runnable (see java documentation) remember to start the thread after creating the object. 
	// Parameters:
	// port             used tell server which TCP/IP port to use 
	// httpRootDir      which directory to use as root for HTTP server.
	// webSocketServer  optional (give it as null if not needed), the class the web server shall call if a new web socket connection is made.
	// webFileServer    optional (give it as null if not needed), this is a callback so that server can provide virtual files (files not on disk but created on the fly)
	public WebServer(int port, String httpRootDir, WebSocketServer webSocketServer, WebFileServer webFileServer) 
	{
		this.httpRootDir=httpRootDir;
	    this.webSocketServer=webSocketServer;	    
	    this.webFileServer=webFileServer;
    	printLocalHostName();
	    setupServer(port);
	}
	

	// Open a server tcp/ip. 
    public void setupServer(int port)
    {
        if (port == 0) port = DEFAULT_PORT;
        this.port = port;
        try 
        { 
            serverSocket = new ServerSocket(port); 
        }
        catch (IOException e) 
        {
        	System.out.println("Exception creating server socket, port="+port);
        	System.out.println(""+e);
            //fail(e, "Exception creating server socket, port="+port);
        	System.exit(1);
        }
        System.out.println("listening on port " + port);
    }

	
	// The body of the server thread.  Loop forever, listening for and
	// accepting connections from clients.  For each connection, 
	// create a Connection object to handle communication through the
	// new Socket.
	public void run() 
	{
        while(serverSocket!=null)
        {
            Socket client_socket = null;
            
            try 
            {
            	// Wait for a client to connect to server.
                client_socket = serverSocket.accept();
            	
	            // A new client has connected.
    			// Create the runnable object that will serve the client.
	            
        		int i = findOrCreateEmptyPctSlot();

	            WebConnection clientConnection = new WebConnection(client_socket, httpRootDir, webFileServer, webSocketServer, this, i);   // WebConnection will collect individual bytes into lines and also handle HTTP.
    	        addPlayerConnectionThread(clientConnection, i);
	            Thread t = new Thread(clientConnection);
	            t.start();
	        	
	    		final String tmpStr=clientConnection.getConnectionTime();
    			debug("connect, slot="+i+", from="+clientConnection.getTcpInfo() + ", time="+tmpStr);

            }
            catch (IOException e) 
            { 
            	if (e instanceof java.net.SocketException)
            	{
            		//java.net.SocketException se=(java.net.SocketException)e;
            		System.out.println("SocketException "+e.getMessage());
            		serverSocket=null;
            	}
            	else
            	{
            		error("run: Exception while listening for connections: "+e);
            		serverSocket=null;
            	}
            }
            
	    }
	}	  
	

	
	public void finalize()
	{
	    debug("finalize");
	    close();    	
	}
	

	
	protected void addPlayerConnectionThread(WebConnection pct, int index)
	{
		pctList[index] = pct;
		//debug("added WebConnection at slot "+index);
	}

	protected void removePlayerConnectionThread(int index)
	{
		if (index<pctList.length)
		{
			if (pctList[index]!=null)
			{
				WebConnection tmp=pctList[index];
				debug("disconnect, slot="+index);
				pctList[index]=null;
				tmp.close();
			}
		}
	}
	
	protected void removePlayerConnectionThread(WebConnection pct)
	{
		int n=0;
		for(int i=0;i<pctList.length;i++)
		{
			if (pctList[i]==pct)
			{
				removePlayerConnectionThread(i);
			}
		}
        if (n==0)
        {
        	debug("did not find WebConnection to remove");
        }
	}
	
	// Find empty slot, create more slots if needed.
	protected int findOrCreateEmptyPctSlot()
	{
		
		for(int i=0;i<pctList.length;i++)
		{
			if (pctList[i]==null)
			{
				return i;
			}
		}

		final int n=pctList.length;

		if (n>1024)
		{
			debug("many sub objects "+pctList.length);
		}

		makePctListLarger();
				
		return n;
	}
	
	
	// Make the array for sub objects larger
	private void makePctListLarger()
	{
		final int n=pctList.length;
		final int t=n*2;

		// make a larger array
		WebConnection newList[] = new WebConnection[t];

		// copy data over to the new array
		for(int i=0;i<n;i++)
		{
			newList[i]=pctList[i];
		}

		// use the new larger array
		pctList=newList;
	}
	
	public void close(WebConnection pct)
	{
		removePlayerConnectionThread(pct);
	}
	
/*	  
    public void close(WebConnection connection)
    {
      for (int i=0;i<this.listOfConnections.length;i++)
      {
        if (this.listOfConnections[i]==connection) 
        {
          System.out.println("TalkServer: Disconnect "+i);
          this.listOfConnections[i].close();
          this.listOfConnections[i]=null;
        }
      }
    }
  */
    
    // close all connections
    public void close()
    {
        if (serverSocket!=null)
        {
	         try 
	         {
	            serverSocket.close();
	         } catch (IOException e) {
	            e.printStackTrace();
	            error("failed to close serverSocket");
	         }
	         serverSocket=null;
        }  	
	
	
    	for(int i=0;i<pctList.length;i++)
		{
			if (pctList[i]!=null)
			{
				WebConnection tmp=pctList[i];
				pctList[i]=null;
				tmp.close();
			}
		}
		
    	
    }

    
    public static void printLocalHostName()
    {
      try 
      {
         InetAddress inet_address=InetAddress.getLocalHost();
         String local_name=inet_address.getHostName();
         System.out.println("local host name: " + local_name);
      }
      catch (UnknownHostException e)
      { 
    	  System.out.println("printLocalHostName failed: "+e);
      }
    }
    
}
