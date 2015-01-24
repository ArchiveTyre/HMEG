/* 
ConnectionCentral.java

To be used in server

Copyright (C) 2013 Henrik Björkman www.eit.se

History:
Adapted for use with RSB. Henrik 2013-05-04

*/


package se.eit.rsb_server_pkg;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
//import java.net.InetAddress;
import java.net.SocketAddress;
//import java.net.UnknownHostException;



public class ConnectionCentral {
	public final static int DEFAULT_PORT = 23456;  // Or use Config.defaultIpPort ?
	  
	protected int port;
	protected DatagramSocket datagramSocket; // http://docs.oracle.com/javase/6/docs/api/index.html?java/io/OutputStreamWriter.html
	ClientConnection[] connection=new ClientConnection[256];    
	byte[] buf = new byte[0x10000];
	DatagramPacket dp = new DatagramPacket(buf, buf.length); // http://docs.oracle.com/javase/6/docs/api/index.html?java/io/OutputStreamWriter.html
	//ServerThread serverThread;
	
	
	public void debug(String str)
	{
		//WordWriter.safeDebug("ConnectionCentral: "+str);
	}

	
	public ConnectionCentral(/*ServerThread serverThread*/)
	{
		//this.serverThread = serverThread;
	}
	
	public void setupServer(int port)
	{
	    if (port == 0) port = ConnectionCentral.DEFAULT_PORT;
	        
	    this.port = port;
	    try { datagramSocket = new DatagramSocket(port); }
	    catch (IOException e) { System.err.println("Exception creating server socket"+e); }
	    System.out.println("ServerThread: listening on port " + port);
	}
	
	
	public int findFreeSlotIndex()
	{
	    int i=0;
	    while(i<connection.length)
	    {
	      if ((connection[i]==null) && (i!=ClientConnection.UNKNOWN_REFERENCE_NUMBER)) {
	    	  return i;
	      }
	      i++;
	    }
	    return ClientConnection.UNKNOWN_REFERENCE_NUMBER;
	}

	public boolean isConnected(SocketAddress sa)
	{
	    for (int i=0;i<connection.length;i++)
	    {
		    if (connection[i]!=null)
		    {
               if (connection[i].socketAddess.equals(sa))
               {
              	 return true;
               }
		    }	  
	  }
	  return false;
	}
	 
	public void close(int client)
	{
		synchronized(this)
		{	
			ClientConnection cc = connection[client];
			if (cc!=null)
			{
				connection[client]=null;
				debug("closing "+client);
				cc.close();
			}
		}
	}

	public void close()
	{
		debug("close");
		synchronized(this)
		{	
			for(int i=0; i<connection.length; i++)
			{
				close(i);
			}

			
			if (datagramSocket != null)
			{
				datagramSocket.close();
				datagramSocket=null;
			}
			/*
			buf=null;
			dp=null;
			*/
		}
	}
		
	
	public ClientConnection process() throws IOException
	{
		ClientConnection cc = ClientConnection.process(this);
		if (cc!=null)
		{
	    	int i = findFreeSlotIndex();
	        
	        if (i!=ClientConnection.UNKNOWN_REFERENCE_NUMBER)
	        {          
	          System.out.println("ServerThread: Connect "+i + " "+ dp.getPort());
	          cc.connectToClient(dp.getSocketAddress(), i);
	          //serverThread.hwc.newPlayer(connection[i]);
	          cc.receivePacket(dp);
	          connection[i]=cc;
	          cc.sendEmptyPacket(ClientConnection.CONNECT_REPLY_OK);
	        }
	        else
	        {
	          // To many connected already, deny the caller
	          SocketAddress	sa = dp.getSocketAddress();
	          System.out.println("To many clients "+sa);
	          
	          println(datagramSocket, sa, "disconnect request");
	        }
			
		}
		return  cc;
	}
	

	public synchronized void sendPacket(DatagramPacket dp)
	{
		  try {
			  datagramSocket.send(dp);
		} catch (IOException e) {
		    System.err.println("failed to send " + dp.getLength() + " " + dp.getPort());
			e.printStackTrace();
		}	  
	}
	
	
	
	public static void printLocalHostName()
	{
	   try 
	   {
	     InetAddress inet_address=InetAddress.getLocalHost();
	     String local_name=inet_address.getHostName();
	     System.out.println("ServerThread: local host name " + local_name);
	   }
	   catch (UnknownHostException e)
	   { 
	     System.err.println("printLocalHostName: "+e);
	   }
	}
	
	public synchronized void println(DatagramSocket ds, SocketAddress	sa, String str)
	{
		byte[] b=str.getBytes();
		DatagramPacket dp=null;
		try {
			dp = new DatagramPacket(b, b.length, sa);
		} catch (SocketException e1) {
	        System.err.println("println socket exeption");
			e1.printStackTrace();
		}
		try {
			ds.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ClientConnection getClientConnection(byte clientReferenceNumber)
	{
		return connection[ClientConnection.unsignedByte(clientReferenceNumber)];
	}
	
	protected void finalize() throws Throwable {
		debug("finalize");
		try {
	         close();        // close open files
	         if (datagramSocket != null)
	         {
	        	 datagramSocket.close();
	        	 datagramSocket=null;
	         }

	     } finally {
	         super.finalize();
	     }
	 }
	
}
