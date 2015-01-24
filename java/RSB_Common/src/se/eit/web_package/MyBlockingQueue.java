/*
MyBlockingQueue.java

Copyright (C) 2013 Henrik Bjorkman www.eit.se/hb

This implements a hopefully thread safe queue.
An added feature is that the caller can give a timeout time when retrieving data from the queue. 

History:

Created by Henrik Bjorkman 2013-08-01 using code found on internet.

*/


// http://stackoverflow.com/questions/2536692/a-simple-scenario-using-wait-and-notify-in-java
// http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/BlockingQueue.html


package se.eit.web_package;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {


    private Queue<T> queue = new LinkedList<T>();
    private final int capacity;
    private Lock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity()
    {
        return capacity;
    }
    
    // Put an element into the queue.
    // If queue is at max capacity the calling thread will wait for another task to remove something.
    public void put(T element) {
        lock.lock();
        try {
            while(queue.size() == capacity) {
                try {
					notFull.await();
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
            }
            queue.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }


    // put an element into the queue.
    // If queue is full then remove one element first.
    /*
    public void putRoundBuffer(T element) {
        lock.lock();
        try {
            if(queue.size() == capacity) {
                try {
                	take(0);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
            }
            queue.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    */
    
    
    // Returns the number of elements in the queue
    public int size() {
        lock.lock();
        try {
        		return queue.size();
        } finally {
            lock.unlock();
        }
    }


    // Returns the number of elements in the queue
    public void clear() {
        lock.lock();
        try {
        		queue.clear();
        } finally {
            lock.unlock();
        }
    }

    
    // Take the first element in the queue.
    // If queue is empty the calling thread will wait for an element to be put there by some other thread.
    // A timeout can be given in milliseconds, if timeout is not desired a very high number can be used.
    public T take(long timeout_ms) throws InterruptedException {

    	final long t1=System.currentTimeMillis()+timeout_ms;
    	
        lock.lock();
        try 
        {        	
            while(queue.isEmpty()) 
            {
	        	// Calculate timeout
		    	final long t2=System.currentTimeMillis();
		    	final long timeElapsed=t2-t1;
		    	final long timeRemaining=timeout_ms-timeElapsed;

	        	// Check for timeout
		    	if (timeRemaining<=0)
			   	{
		    		// no time remaining, throw exception
	        		throw new InterruptedException("timeout");
			   	}
		    	
	        	try {
	        		// Wait for not empty signal, with timeout
					notEmpty.awaitNanos(timeRemaining*1000000);
				} catch (InterruptedException e) {
					// do nothing, this is normal
				}
            }

            T item = queue.remove();
            notFull.signal();
            return item;
        } 
        finally 
        {
            lock.unlock();
        }
    }
    
}
