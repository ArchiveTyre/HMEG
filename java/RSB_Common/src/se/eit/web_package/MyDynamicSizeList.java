/*
MyDynamicSizeList.java

Copyright (C) 2013 Henrik Bjorkman www.eit.se/hb

This implements a hopefully thread safe queue.
An added feature is that the caller can give a timeout time when retrieving data from the queue. 

History
Created by Henrik Bjorkman 2013-08-01 using code found on internet.

*/


// http://stackoverflow.com/questions/2536692/a-simple-scenario-using-wait-and-notify-in-java
// http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/BlockingQueue.html


package se.eit.web_package;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyDynamicSizeList<T> {


    private Queue<T> queue = new LinkedList<T>();
    private Lock lock = new ReentrantLock();

    public MyDynamicSizeList() {
    }

    // Put an element into the queue.
    // If queue is at max capacity the calling thread will wait for another task to remove something.
    public boolean add(T element) {
        lock.lock();
        try {
            return queue.add(element);
        } finally {
            lock.unlock();
        }
    }
    
    // Returns the number of elements in the queue
    public int size() {
        lock.lock();
        try {
        	return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(T element) {
        lock.lock();
        try {
            return queue.remove(element);
        } finally {
            lock.unlock();
        }
    }

    public Object[] toArray()
    {
        lock.lock();
        try {
            return queue.toArray();
        } finally {
            lock.unlock();
        }    	
    }
    
}
