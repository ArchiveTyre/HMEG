//
// Copyright (C) 2014 Henrik Björkman www.eit.se
//
// History:
// Created by Henrik 2014-03-08


package se.eit.empire_package;


//import java.util.Random;
import se.eit.db_package.*;
import se.eit.web_package.*;



public class EmpireOrder extends EmpireBase {

	String order;
  
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireOrder.class.getSimpleName();	
	}

	public EmpireOrder(DbBase parent, String name, String order) 
	{
		super();
		parent.addObject(this);
		this.setName(name);

		this.order=order;
	}
	

	public EmpireOrder()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		order=wr.readString();
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeString(order);
	}	
	
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"order "+order);		
	}
	
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("order"))
		{
			order=wr.readString();
			return 1;
		}
		return super.setInfo(wr, infoName);
	}
	
	
	public String getOrder()
	{
		return order;
	}

	public void setOrder(String order)
	{
		if (this.order!=order)
		{
			setUpdateCounter();
			this.order=order;
		}
	}
	
}