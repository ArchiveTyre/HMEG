// RsbLong.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
//History:
//Created by Henrik 2014-03-08


package se.eit.rsb_package;



import se.eit.db_package.DbThreadSafe;
import se.eit.web_package.*;



public class RsbLong extends DbThreadSafe {

	long value;

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return RsbLong.class.getSimpleName();	
	}
	

	public RsbLong()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		value=wr.readLong();
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeLong(value);
	}	
	
	@Override
	public void listInfo(WordWriter pw)
	{
		super.listInfo(pw);
		pw.println("value "+value);		
	}
	
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("value"))
		{
			value=wr.readLong();
			return 1;
		}
		return super.setInfo(wr, infoName);
	}
	
	
	public long getValue()
	{
		return value;
	}

	public void setValue(long value)
	{
		if (this.value!=value)
		{
			this.value=value;
			setUpdateCounter();
		}
	}
	
	/*
	@Override
	public boolean isVisibleTo(DbBase observingObj)
	{
		return ((observingObj!=null) && (observingObj.isRecursiveParentOf(this)));
	}
	*/

}