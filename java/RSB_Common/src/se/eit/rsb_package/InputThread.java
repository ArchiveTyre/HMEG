// InputThread.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2014 

package se.eit.rsb_package;

import java.io.InputStream;
import se.eit.web_package.*;


public class InputThread extends Thread {

	private InputStream in; 
	private MyBlockingQueue<String> stringFifo;
	private boolean done=false;
	
	public InputThread(InputStream in, MyBlockingQueue<String> stringFifo)
	{
		this.in=in;
		this.stringFifo=stringFifo;
	}
	
	public void run()
	{
    	System.out.println("InputThread: run");
    	WordReader wr=new WordReaderInputStream(in);
    	while ((!done) && (wr.isOpenAndNotEnd()))
    	{
	    	String str=wr.readLine();
	    	//System.out.println("InputThread: "+str);
	    	stringFifo.put(str);
		}
	}
	
	public void close()
	{
		done=true;
	}
	
}
