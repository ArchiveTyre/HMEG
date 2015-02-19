//
//Copyright (C) 2014 Henrik Björkman www.eit.se
//
//History:
//Created by Henrik 2014-03-08


package se.eit.rsb_package;



import se.eit.web_package.*;



public class RsbLong extends GameBase {

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
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"value "+value);		
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
			setUpdateCounter();
			this.value=value;
		}
	}
	
	

}