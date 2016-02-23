// RsbString.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
//History:
//Created by Henrik 2014-03-08


package se.eit.rsb_package;


import se.eit.db_package.*;
import se.eit.web_package.*;



public class RsbString extends DbThreadSafe {

	String value; // TODO rename this to value

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return RsbString.class.getSimpleName();	
	}

	public RsbString(DbBase parent, String name, String order) 
	{
		super();
		parent.addObject(this);
		this.regName(name);

		this.value=order;
	}
	

	public RsbString()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		value=wr.readString();
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeString(value);
	}	
	
	@Override
	public void listInfo(WordWriter pw)
	{
		super.listInfo(pw);
		pw.println("order "+value);		
	}
	
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("order"))
		{
			value=wr.readString();
			return 1;
		}
		return super.setInfo(wr, infoName);
	}
	
	
	public String getOrder()
	{
		return value;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String getStringValue()
	{
		return value;		
	}
	
	public void setOrder(String order)
	{
		if (this.value!=order)
		{
			setUpdateCounter();
			this.value=order;
		}
	}
	
	public void setValue(String order)
	{
		if (this.value!=order)
		{
			this.value=order;
			setUpdateCounter();
		}
	}
	
	// When an order is unlinked the objects inside (other orders) are moved to the parent object
	@Override
	public void unlinkSelf()
	{
		if (this.listOfStoredObjects!=null)
		{
			for (DbStorable ds : this.listOfStoredObjects)
			{
				ds.moveToRoom(this.getParent());
			}
		}
		super.unlinkSelf();
	}

}