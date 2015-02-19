//
//Copyright (C) 2014 Henrik Björkman www.eit.se
//
//History:
//Created by Henrik 2014-03-08


package se.eit.rsb_package;



import se.eit.web_package.*;



public class RsbBigBitMap extends GameBase {

	BigBitMap value=null;

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return RsbBigBitMap.class.getSimpleName();	
	}
	

	public RsbBigBitMap()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		value=new BigBitMap(wr.readString());
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeString(value.toString());
	}	
	
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"value "+value);		
	}
	
	public int getBit(int bitNo)
	{
		if (value==null)
		{
			return 0;
		}
		return value.getBit(bitNo);
	}
	
	public void setBit(int bitNo)
	{
		if (getBit(bitNo)==0)
		{
			if (value==null)
			{
				value=new BigBitMap("0");
			}
			value.setBit(bitNo);
			setUpdateCounter();
		}
	}

	public void clrBit(int bitNo)
	{
		if (getBit(bitNo)!=0)
		{
			if (value==null)
			{
				value=new BigBitMap("0");
			}
			value.clrBit(bitNo);
			setUpdateCounter();
		}
	}

}