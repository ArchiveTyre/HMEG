package se.eit.rsb_package;





public class RoundBuffer {

	//Class Members:
	String fifo[];
	int head=0;
	int tail=0;
	int store=0;
	int totMsg=0;
	
	public RoundBuffer(int size) {
		//Creates An Array
		fifo=new String[size];	
	
	}
	   //Methods    
	
	
	// Synchronized
	public synchronized boolean isEmpty()
	{
		return (head == tail);
	}
	
	
	// Returns index plus one, wrapped to zero if it reached max index
	public synchronized int incWrap(int i)
	{
		if (i == fifo.length-1) 
		{
			i = 0;
	         return(i);
		}
	    else
	    {        
	         i++;
	       	 return(i);
	    }
	}      
	
	//last was here 0
	public synchronized boolean isFull()	
	{
	    	if (incWrap(tail)==head)		
	    		return (true);
	    	else return (false);
	}


	public synchronized void put(String str)        
	{
		//tail
		if (isFull())
		{
			// buffer is full, remove one position to make room.
			take();
		}
			
		//noOFslotsUsed++;	
		fifo[tail]=str;		
		totMsg++;
		tail=incWrap(tail);
	}
	
	public synchronized String take()
	{
		if (isEmpty())
		{
			return null;
		}
		
		store=head;
		
		
		head=incWrap(head);
	
		return fifo[store];                       
	}
	

	// 
	public synchronized String get(int msgNr)
	{
		final int messagesToGet=totMsg-msgNr;
		
		if ((totMsg-msgNr)>0)
		{
			if (messagesToGet>fifo.length)
			{
				// buffer has been full and wrapped. Don't have that message.
				// Return an empty string.
				return "";				
			}
		
			final int i=msgNr%fifo.length;
			return fifo[i];			
		}
		else
		{
			// Don't have that message yet, try later.
			return null;			
		}
		
	
	}
	

	
}