package se.eit.rsb_package;


// Perhaps this can be done with collections queue?
// http://docs.oracle.com/javase/tutorial/collections/interfaces/index.html
// http://docs.oracle.com/javase/tutorial/collections/interfaces/queue.html

// We may need a blocking queue, like described here:
// http://stackoverflow.com/questions/2536692/a-simple-scenario-using-wait-and-notify-in-java
// http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/BlockingQueue.html


public class StringFifo {

	//Class Members:
	String fifo[];
	int head=0;
	int tail=0;
	int store=0;
	
	public StringFifo(int size) {
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
			System.out.println("[WARNING]:The Fifo is over loaded.");
			take();
		}
			
		//noOFslotsUsed++;	
		fifo[tail]=str;
		
		tail=incWrap(tail);
	}
	
	public synchronized String take()
	{
		if (isEmpty())
		return null;
		
		store=head;
		
		
		head=incWrap(head);
	
		return fifo[store];                       
	}
	
	
}