/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.rsb_package;

import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;

public class RsbInvItem extends RsbString {

	long numberOf;
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return RsbInvItem.class.getSimpleName();	
	}
	

	public RsbInvItem()
	{	
		super();
	}

	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		numberOf=wr.readLong();
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeLong(numberOf);
	}	
	
	@Override
	public void listInfo(WordWriter pw)
	{
		super.listInfo(pw);
		pw.println("numberOf "+numberOf);		
	}
	
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("numberOf"))
		{
			numberOf=wr.readLong();
			return 1;
		}
		return super.setInfo(wr, infoName);
	}

	public long getNumberOf()
	{
		return numberOf;
	}

	public void setNumberOf(long numberOf)
	{
		if (this.numberOf!=numberOf)
		{
			this.numberOf=numberOf;
			setUpdateCounter();
		}
	}
	

}
