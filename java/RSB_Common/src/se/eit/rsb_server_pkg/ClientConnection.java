/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/

package se.eit.rsb_server_pkg;



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.concurrent.locks.ReentrantReadWriteLock;





/*
Header format:

bytes	what
1 		MAGIC_NUMNER
1		reference number for receiver
1		Type of message
1		SequenceNumber
1		reference number for sender
3		reserved 

*/


public class ClientConnection {

	//int errSim=0;
	
	private class RcvPacket {		
		public byte dataNotIncludingHeader[]; // not including header
		public int dataLengthNotIncludingHeader; // Not including header 
		public byte cmd;
		
		public RcvPacket(DatagramPacket dp, byte cmd)
		{
			this.cmd = cmd;
			dataLengthNotIncludingHeader = dp.getLength() - HEADER_SIZE;
            byte d[] = dp.getData();    
		   	this.dataNotIncludingHeader = new byte[dataLengthNotIncludingHeader];
		   	for (int i=0;i<dataLengthNotIncludingHeader;i++)
		   	{
		   		this.dataNotIncludingHeader[i]=d[i+HEADER_SIZE];		   		
		   	}
		}
	}
	
	private class SndPacket {
		public DatagramPacket dp; // including header
		public long timeStamp;
		public int nRetransmissions=0;
		
		public SndPacket(DatagramPacket dp)
		{
			this.dp=dp;
		    timeStamp = System.nanoTime();
		}		
	}
	
	public final static int HEADER_SIZE = 8;
	public static final byte MAGIC_NUMNER = 17;
	public static final byte UNKNOWN_REFERENCE_NUMBER = 0;

	
	public static final byte NO_MSG = (byte)255;
	public static final byte CONNECT_REQ = (byte)254;
	public static final byte SINGLE_ACK = (byte)253;
	public static final byte STRING_PKT = (byte)252;
	public static final byte DISCONNECT = (byte)251;
	public static final byte DATA_PKT = (byte)250;
	public static final byte CONNECT_REPLY_OK = (byte)249;
	public static final byte CONNECT_REPLY_NOK = (byte)248;
	//public static final byte FORK_CHANNEL = (byte)247;
	public static final byte USER_START = (byte)0;
	public static final byte USER_END = (byte)127;
  

	
	private int ourRef; // myRef, pair should supply this so we know which sub channel it was.
	protected int yourRef; // yourRef, Reference number used by pair.
	protected ConnectionCentral connectionCentral;
	public SocketAddress socketAddess;
	
	//public byte rcvSequenceNumber;
	
	RcvPacket rcvDpQueue[]=new RcvPacket[256];
	byte rcvNextSequenceNumber=0;
	
	SndPacket sndDpQueue[]=new SndPacket[256];
	int sndNPacketsWaitingForAck=0;
	byte sndSequenceNumber=0;
	
	String service=null;
	
	//ClientConnection forkedFrom = null;

	public ReentrantReadWriteLock rrwl=null;
	
	public void debug(String str)
	{		
    	//WordWriter.safeDebug("ClientConnection("+ourRef +","+yourRef+"): "+str);
	}
	
	public ClientConnection(ConnectionCentral connectionCentral, byte sequenceNumber, byte yourRef /*, ClientConnection forkedFrom*/) 
	{
	    this.connectionCentral=connectionCentral;
	    this.yourRef=yourRef;
	    this.rcvNextSequenceNumber = sequenceNumber;
	    //this.forkedFrom = forkedFrom;
	    
	    rrwl=new ReentrantReadWriteLock();
	}

	public static int my_abs(int a)
	{
		if (a<0)
		{
			return -a;
		}  	  
		return a;
	}

	public static byte my_abs(byte a)
	{
		if (a<0)
		{
			return (byte)-a;
		}  	  
		return a;
	}

	
	public  void checkAndRetransmit()
	{
		if (sndNPacketsWaitingForAck!=0)
		{
			synchronized(this)
			{
			
				for(int i=0; i<256;i++)
				{
					SndPacket sp = sndDpQueue[i]; 
					if (sp!=null)
					{
						long st = System.nanoTime();
						long tp = st-sp.timeStamp;
						
						if (tp>(1000000000L*(1+sp.nRetransmissions*sp.nRetransmissions)))
						{
							debug("retransmitting " + i);					
							connectionCentral.sendPacket(sp.dp);
							sp.nRetransmissions++;
							if (sp.nRetransmissions>8)
							{
								close();
							}
						}
					}
				}
			}
		}
	}	
	
	// my unsigned value sort of (java suck sometimes)
	// http://www.darksleep.com/player/JavaAndUnsignedTypes.html
	public static int unsignedByte(byte b)
	{
		return (int)b & 0xFF;
	}

	
	// To be called from server thread when there is a new packet for this client
	// server thread shall have checked that the header is a valid one already
	// All data in the datagram packet will be copied so dp can be reused.
	public  void receivePacket(DatagramPacket dp)
	{	
		if (socketAddess==null)
		{
			System.err.println("socket address is null");
			return;			
		}
		else if (!dp.getSocketAddress().equals(socketAddess))
		{
			System.err.println("socket address did not match "+socketAddess.toString()+" "+dp.getSocketAddress().toString());
			return;
		}
		

	    byte d[] = dp.getData();

		byte clientCommand = (byte)d[2];
		byte clientSequenceNumber = (byte)d[3];

		
		switch(clientCommand)
		{		
			//case FORK_CHANNEL:
			case STRING_PKT:
			case DATA_PKT:
			{
				// here we copy all data in the datagram packet so that input buffer can be reused.
				RcvPacket r = new RcvPacket(dp, clientCommand);
				
				if (clientCommand == STRING_PKT)
				{
					debug("receive_string " + unsignedByte(clientSequenceNumber) + " "+r.dataLengthNotIncludingHeader+" \""+ copyString(r)+"\"");
				}
				else
				{
					debug("receive_data " + unsignedByte(clientSequenceNumber)+ " "+r.dataLengthNotIncludingHeader);
				}

				synchronized(this)
				{				    
				  	// Other end should not send lots of packets without ack
					byte nPkt = (byte)(clientSequenceNumber-rcvNextSequenceNumber); 
				  	if (my_abs(nPkt)>96)
				  	{
				  		System.err.println("received many packets without ack "+clientSequenceNumber+" "+rcvNextSequenceNumber);
				  		close();
				  	}
				  	else if (my_abs(nPkt)>32)
				  	{
				  		// Don't send ack, this way other end will slow down eventually 
				  	}
				  	else
				  	{
					    rcvDpQueue[unsignedByte(clientSequenceNumber)]=r;
				  		sendAck(clientSequenceNumber);
				  	}
						
			        this.notify();
				}
			    break;
			}
			case SINGLE_ACK:
			{
				debug("single ack " + unsignedByte(clientSequenceNumber));
				
				
				synchronized(this)
				{
					handleAck(clientSequenceNumber);
					this.notify();
				}
			    break;
			}
			case CONNECT_REQ:
			{
				debug("connect request " + unsignedByte(clientSequenceNumber)+ " "+yourRef);
				
				// here we copy all data in the datagram packet so that input buffer can be reused.
				service = new String(d, ClientConnection.HEADER_SIZE, dp.getLength()-ClientConnection.HEADER_SIZE);
				
				synchronized(this)
				{
					sendAck(clientSequenceNumber);
					rcvNextSequenceNumber=clientSequenceNumber;
					rcvNextSequenceNumber++;
					yourRef = (byte)d[4];
					this.notify();
				}
				break;
			}
			case CONNECT_REPLY_OK:
			{
				debug("connect reply ok " + unsignedByte(clientSequenceNumber));
				
				yourRef = (byte)d[4];

				
				synchronized(this)
				{				    
				  	// Other end should not send lots of packets without ack 
				  	if (my_abs((int)clientSequenceNumber-(int)rcvNextSequenceNumber)>64)
				  	{
				  		System.err.println("received many packets without ack "+clientSequenceNumber+" "+rcvNextSequenceNumber);
				  		close();
				  	}
				  	else
				  	{
						rcvNextSequenceNumber=clientSequenceNumber;
						rcvNextSequenceNumber++;
				  		sendAck(clientSequenceNumber);
				  	}
						
			        this.notify();
				}
			    break;
			}							
			case CONNECT_REPLY_NOK:
			case DISCONNECT:
			{
				debug("disconnect from pair " + unsignedByte(clientSequenceNumber)+ " "+socketAddess);
				
				synchronized(this)
				{
					//sendAck(clientSequenceNumber);
					socketAddess=null;					
					close();
					this.notify();
				}
			    break;
			}
			default:
			{
				System.err.println("unknown command " +clientCommand);
			    break;				
			}				
		}
	}

	private RcvPacket getNextPacketNonBlocking()
	{
	    RcvPacket dp=rcvDpQueue[unsignedByte(rcvNextSequenceNumber)];
	    if (dp!=null)
		{		
			synchronized(this)
			{
				dp = rcvDpQueue[unsignedByte(rcvNextSequenceNumber)];
				rcvDpQueue[unsignedByte(rcvNextSequenceNumber)]=null;
				//debug("getNextPacketNonBlocking " + rcvNextSequenceNumber + " "+dp.dataLengthNotIncludingHeader);
				if ((dp.cmd!=DATA_PKT) && (dp.cmd!=STRING_PKT))
				{
					System.err.println("ignored packet " +dp.cmd);
					dp=null;
				}
				rcvNextSequenceNumber++;				
			    this.notify();			    
			}
		}
		return dp;
	}

	// To be called from client thread to check what kind of message next packet contains
	// Returns NO_MSG if there is no message.
	public  byte checkNextPacket()
	{
	    if (rcvDpQueue[unsignedByte(rcvNextSequenceNumber)]!=null)
		{		
	    	return rcvDpQueue[unsignedByte(rcvNextSequenceNumber)].cmd; 
		}
	    myWait();
		return NO_MSG;
	}

	public int available() {

	    if (rcvDpQueue[unsignedByte(rcvNextSequenceNumber)]!=null)
		{		
	    	return rcvDpQueue[unsignedByte(rcvNextSequenceNumber)].dataLengthNotIncludingHeader; 
		}
	    myWait();
		return 0;
	}

	
	// Called by user/client thread to send a binary buffer to the client application.
	// Adds header.
	public  void sendData(byte[] data, int offset, int length)
	{
		if (socketAddess!=null)
		{
			
			int dataLen=length-offset;
			
			byte[] b= new byte[HEADER_SIZE+dataLen];
			
			//debug("sendData: " + unsignedByte(sndSequenceNumber) + " " + dataLen);
	
		    b[0] = (byte)MAGIC_NUMNER;
		    b[1] = (byte)yourRef;
		    b[2] = DATA_PKT;
		    b[3] = (byte)sndSequenceNumber;
			
		    for (int i=0; i<dataLen; i++)
		    {
		      b[HEADER_SIZE+i]= data[offset+ i];
		    }
		    
			DatagramPacket dp=new DatagramPacket(b, HEADER_SIZE+dataLen);
			
			dp.setSocketAddress(socketAddess);
			
			synchronized(this)
			{
				sndNPacketsWaitingForAck++;
				
				if (sndNPacketsWaitingForAck>32)
				{
					myWait();
					while (sndNPacketsWaitingForAck>48)
					{
						myWait();
					}
				}
				
				try
				{			
					writeLock();
				
					// keep message in case we need to retransmit it
					sndDpQueue[unsignedByte(sndSequenceNumber)]=new SndPacket(dp);
					
			        /*if (++errSim == 20)
			        {
			        	System.err.println("simulating packet loss");
			        }
			        else*/
			        {
			 		   connectionCentral.sendPacket(dp);
			        }
					
					sndSequenceNumber++;
					this.notify();
				}
				finally
				{
					writeUnlock();
				}
			}
		}
		else
		{
			debug("connection not open");
		}
	}
	
	
	// Called by user/client thread to send a string to the client application.
	// Adds header.
	public  void println(String str)
	{
		if (socketAddess!=null)
		{
			
			int strLen=str.length();
			
			byte[] b= new byte[HEADER_SIZE+strLen+1];
			
			debug("println: " + unsignedByte(sndSequenceNumber) + " " + strLen + " \""+ str+"\"");
	
		    b[0] = (byte)MAGIC_NUMNER;
		    b[1] = (byte)yourRef;
		    b[2] = STRING_PKT;
		    b[3] = (byte)sndSequenceNumber;
		    b[4] = (byte)ourRef;
			
		    // Only ascii here, unicode not supported yet
		    for (int i=0; i<strLen; i++)
		    {
		      b[HEADER_SIZE+i]=(byte) str.charAt(i);
		    }
		    
		    b[HEADER_SIZE+strLen]='\n';
		    
			DatagramPacket dp=new DatagramPacket(b, HEADER_SIZE+strLen+1);
			
			dp.setSocketAddress(socketAddess);

			synchronized(this)
			{
				sndNPacketsWaitingForAck++;
				
				if (sndNPacketsWaitingForAck>32)
				{
					myWait();
					while (sndNPacketsWaitingForAck>48)
					{
						myWait();
					}
				}
				
				try
				{			
					writeLock();
					
					// keep message in case we need to retransmit it
					sndDpQueue[unsignedByte(sndSequenceNumber)]=new SndPacket(dp);
					
			        /*if (++errSim == 20)
			        {
			        	System.err.println("simulating packet loss");
			        }
			        else*/
			        {
			 		   connectionCentral.sendPacket(dp);
			        }
					
					sndSequenceNumber++;
					this.notify();
				}
				finally
				{
					writeUnlock();
				}
			}
		}
		else
		{
			debug("connection not open");
		}
	}

	// To be called by user-client when it wants to connect to server. Servers shall not call this.
	// Adds header.
	public  void connectToServer(SocketAddress socketAddess, int ourRef, String service)
	{		
		if (this.socketAddess!=null)
		{
			System.err.println("already connected");			
		}
		
		if (yourRef != UNKNOWN_REFERENCE_NUMBER)
		{
			System.err.println("yourRef was defined already "+yourRef);
		}
		
		this.socketAddess = socketAddess;
	    this.ourRef = ourRef;

	    
		int strLen=service.length();
		
		byte[] b= new byte[HEADER_SIZE+strLen];
	    
		
		//			str.getBytes();
		debug("connect: " + unsignedByte(sndSequenceNumber)+ " "+ourRef);

	    b[0] = (byte)MAGIC_NUMNER;
	    b[1] = (byte)yourRef;
	    b[2] = CONNECT_REQ;
	    b[3] = (byte)sndSequenceNumber;
	    b[4] = (byte)ourRef;
			    
	    for (int i=0; i<strLen; i++)
	    {
	      b[HEADER_SIZE+i]=(byte) service.charAt(i);
	    }
	    
		DatagramPacket dp=new DatagramPacket(b, HEADER_SIZE+strLen);
		
		dp.setSocketAddress(this.socketAddess);
		
		synchronized(this)
		{

			sndNPacketsWaitingForAck++;
			
			if (sndNPacketsWaitingForAck>64)
			{
				debug("We have sent lots of packets without ack " + sndNPacketsWaitingForAck + " " + yourRef);
				close();
			}
			
			// keep message in case we need to retransmit it
			sndDpQueue[unsignedByte(sndSequenceNumber)]=new SndPacket(dp);
			
	 		connectionCentral.sendPacket(dp);
			
			sndSequenceNumber++;
			this.notify();
		}
	}

	public  void disconnect()
	{		
		debug("disconnect: " + unsignedByte(sndSequenceNumber));
		
		sendEmptyPacket(DISCONNECT);
	}

	
	
	public  void sendEmptyPacket(byte cmd)
	{		
		byte[] b= new byte[HEADER_SIZE];
		
		//			str.getBytes();
		debug("sendEmptyPacket: " + unsignedByte(sndSequenceNumber)+ " "+unsignedByte(cmd));

	    b[0] = (byte)MAGIC_NUMNER;
	    b[1] = (byte)yourRef;
	    b[2] = cmd;
	    b[3] = (byte)sndSequenceNumber;
	    b[4] = (byte)ourRef;
			    
		DatagramPacket dp=new DatagramPacket(b, HEADER_SIZE);
		
		dp.setSocketAddress(this.socketAddess);
		
		synchronized(this)
		{
			sndNPacketsWaitingForAck++;

			try
			{			
				writeLock();
				
			
				// keep message in case we need to retransmit it.
				sndDpQueue[unsignedByte(sndSequenceNumber)]=new SndPacket(dp);
			
	 			connectionCentral.sendPacket(dp);
			
	 			sndSequenceNumber++;
				this.notify();
			}
			finally
			{
				writeUnlock();
			}
				
		}
	}

	
	// This is to be called by a server when it wishes to connect to a client
	// There is only one thread that may call this.
	public  void connectToClient(SocketAddress socketAddess, int ourRef)
	{
		synchronized(this)
		{
		
			if (this.socketAddess!=null)
			{
				System.err.println("already connected");			
			}
		
			this.socketAddess = socketAddess;
			this.ourRef=ourRef;
			//this.yourRef = yourRef; 
			this.notify();
		}
	}
	
	private  void sendAck(byte sequenceNumber)
	{	
		byte[] b = new byte[HEADER_SIZE];
		
	    b[0] = (byte)MAGIC_NUMNER;
	    b[1] = (byte)yourRef;
	    b[2] = SINGLE_ACK;
	    b[3] = (byte)sequenceNumber;
	    b[4] = (byte)ourRef;
		
		DatagramPacket dp=new DatagramPacket(b, HEADER_SIZE);
		
		dp.setSocketAddress(socketAddess);
			
		debug("send ack " + unsignedByte(sequenceNumber) + " "+ourRef+" "+yourRef);

		connectionCentral.sendPacket(dp);
	}
	  
	private void handleAck(byte sequenceNumber)
	{
		  
		if (sndDpQueue[unsignedByte(sequenceNumber)]!=null)
		{
		    sndNPacketsWaitingForAck--;
		    sndDpQueue[unsignedByte(sequenceNumber)]=null;	  
		}

		// If other packets are not acknowledged yet perhaps we send some of them again.
		//checkAndRetransmit();
	}

	
	// Copy string from received packet
	private String copyString(RcvPacket dp)
	{
	    byte d[] = dp.dataNotIncludingHeader;
	    int len=dp.dataLengthNotIncludingHeader;

	    if (len>0)
	    {
			  String str = new String(d, 0, len);
			  if (str!=null)
			  {
				return str;
			  }
	    }

		return "";
	}
	
	
	// Check if there is line to read, returns null if there is nothing yet.
	// Use checkNextPacket first to check that the message is a string.
	// To be called from user threads
	public  String readLineNotBlocking()
	{
		RcvPacket dp = getNextPacketNonBlocking();
		if (dp!=null)
		{
			return copyString(dp);
	    }
		myWait();	      
		dp = getNextPacketNonBlocking();
		if (dp!=null)
		{
			return copyString(dp);
	    }
		return null;
    }

	// Check if there is data to pick up, returns null if there is nothing yet.
	// Use checkNextPacket first to check that the message is binary data.
	// To be called from user threads
	public  byte[] readDataNotBlocking()
	{
		RcvPacket dp = getNextPacketNonBlocking();
		if (dp!=null)
		{
	    	return dp.dataNotIncludingHeader;
		}
		myWait();
		dp = getNextPacketNonBlocking();
		if (dp!=null)
		{
	    	return dp.dataNotIncludingHeader;
		}
		return null;
    }
	
	
    // Waits (blocks) until there is something
	public String readLineBlocking()
	{
		  String str=null;
		  for(;;)
		  {
			  str = readLineNotBlocking();
			  if (str!=null) break;
			  myWait();
			  if (!isOpen()) 
			  {
				  System.out.println("connection closed");				  
				  break;
			  }
		  }
		  return str;
	}
	
    // Waits (blocks) until there is something
	public byte[] readDataBlocking()
	{
		byte[] data=null;
		  for(;;)
		  {
			  data = readDataNotBlocking();
			  if (data!=null) break;
			  myWait();
			  if (!isOpen()) 
			  {
				  System.out.println("connection closed");				  
				  break;
			  }
		  }
		  return data;
	}
	

	public  void myWait()
	{
		synchronized(this)
		{
			try {
				this.wait(100);
				checkAndRetransmit();
			} catch (InterruptedException e) {
			}
		}
	}

	public  boolean isOpen()
	{
		return this.socketAddess!=null;
	}
	
	
	public  void close()
	{
		synchronized(this)
		{
			if (socketAddess!=null)
			{
				debug("close " + socketAddess.toString());	
				disconnect();
				socketAddess=null;		
			}
			if ((connectionCentral!=null) && (ourRef!=UNKNOWN_REFERENCE_NUMBER))
			{
				int tmpOurRef=ourRef;
				ourRef=UNKNOWN_REFERENCE_NUMBER;				
				connectionCentral.close(tmpOurRef);				
			}
			yourRef=UNKNOWN_REFERENCE_NUMBER;
			ourRef=UNKNOWN_REFERENCE_NUMBER;
			this.notify();
		}
	}

	protected void finalize() throws Throwable {
		debug("finalize");
		try {
	         close();
	     } finally {
	         super.finalize();
	     }
	 }

	public void writeLock()
	{
		debug("writeLock");
		rrwl.writeLock().lock();
	}
	

	public void writeUnlock()
	{
		debug("writeUnlock");
		rrwl.writeLock().unlock();
	}
	
	
	
	public static ClientConnection process(ConnectionCentral connectionCentral) throws IOException
	{
		DatagramPacket dp = connectionCentral.dp;
		connectionCentral.datagramSocket.receive(dp);
  		
		//connectionCentral.debug("ClientConnection process");
  	    byte d[] = dp.getData();
    
        if (d.length>=ClientConnection.HEADER_SIZE)
        {        	       
	        // decode the message
        	byte magicNumber = (byte)d[0];
        	byte clientReferenceNumber = (byte)d[1]; // Our ref number
        	byte clientCommand = (byte)d[2];
        	byte clientSequenceNumber = (byte)d[3];
    	    byte sendersRef = (byte)d[4]; // Their ref number
	        
    	    
	        //String msg=readLine(dp);
	        
	        if (magicNumber==ClientConnection.MAGIC_NUMNER)
	        {
	        	if (clientReferenceNumber == ClientConnection.UNKNOWN_REFERENCE_NUMBER)
	        	{
		        	if (clientCommand == ClientConnection.CONNECT_REQ)
			        {		        		
		        		/*if (connectionCentral.isConnected(dp.getSocketAddress()))
		        		{
		        			System.err.println("is already connected");
		        		}
		        		else*/
		        		{
		        			ClientConnection cc = new ClientConnection(connectionCentral, clientSequenceNumber, sendersRef /*, null*/);
			        	    return cc;
		        			
		        		}
			        }
			        else
			        {
			          System.out.println("unknown message from client "+clientCommand);
			        }
	        	}
	        	else
	        	{
	        		ClientConnection cc = connectionCentral.getClientConnection(clientReferenceNumber);

					if (cc!=null)
					{
		        	    if (clientReferenceNumber!=cc.ourRef) 
		        	    {
		    	             System.out.println("internal error in reference number "+clientReferenceNumber+" "+cc.ourRef);		        	    	
		        	    }
		        	    /*else if (clientCommand == ClientConnection.FORK_CHANNEL)
		        	    {
		        	    	System.out.println("fork request from other end");
		        	    	ClientConnection cc2 = new ClientConnection(connectionCentral, clientSequenceNumber, sendersRef, cc);
		        	    	cc.receivePacket(dp);
			        	    return cc2;
		        	    }*/
		        	    else
		        	    {
		        	    	cc.receivePacket(dp);
		        	    }
					}
					else
					{
				        System.out.println("unknown client");	        			
					}

	        	}
	        }
	        else
	        {	        	
	              System.out.println("wrong magic number from client");
	        }        
        }
        else
        {
	          System.out.println("to short message from client");       	
        }
        
		return null;
	}
	
}