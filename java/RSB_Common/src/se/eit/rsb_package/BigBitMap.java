// BigBitMap.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_package;

import se.eit.web_package.WordReader;

public class BigBitMap {

	
	long a[]=new long[1];

	public BigBitMap(String str)
	{
		int n=0;
		WordReader wr=new WordReader(str);
		while(wr.isOpenAndNotEnd())
		{
			String s=wr.readWord();
			final long i=Long.parseLong(s, 16);
			while (n>=a.length)
			{
				// Need a bigger array, make one and copy existing data
				doubleTheCapacity();
			}
			a[n]=i;
			n++;
		}
	}
	
	public String toString()
	{
		StringBuffer sb=new StringBuffer();
		
		//sb.append(""+Long.toHexString(a[0]));
		sb.append(""+Long.toString(a[0], 16));
		for(int i=1; i<a.length;i++)
		{
			//sb.append(" "+Long.toHexString(a[i]));
			sb.append(" "+Long.toString(a[i], 16));
		}
		
		return sb.toString();
	}
	
	public void doubleTheCapacity()
	{
		long tmp[] = new long[a.length*2];
		for(int j=0;j<a.length;++j)
		{
			tmp[j]=a[j];
		}
		a=tmp;		
	}
	
	
	public void setBit(int bit)
	{
		if (bit<0)
		{
			throw new IndexOutOfBoundsException("negative index "+bit);
		}
		
		final int i=bit/64;
		final int b=bit%64;
		
		while (i>=a.length)
		{
			// Need a bigger array, make one and copy existing data
			doubleTheCapacity();
		}
		
		final long m=1L << b;
		a[i] |= m;
	}
	
	public void clrBit(int bit)
	{
		if (bit<0)
		{
			throw new IndexOutOfBoundsException("negative index "+bit);
		}
		
		final int i=bit/64;
		final int b=bit%64;
		
		if (i>=a.length)
		{
			return;
		}
		
		final long m=1L << b;
		a[i] &= ~m;
	}
	
	public int getBit(int bit)
	{
		if (bit<0)
		{
			return 0;
		}

		final int i=bit/64;
		final int b=bit%64;

		if (i>=a.length)
		{
			return 0;
		}

		final long m=1L << b;
		if (((a[i]) & m)!=0)
		{
			return 1;
		}
		
		return 0;
	}
	
}
