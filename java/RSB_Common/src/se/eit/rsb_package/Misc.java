// Misc.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2014 

package se.eit.rsb_package;

import se.eit.web_package.WordReader;

public class Misc {

	//public static final float G = 6.67384F*Misc.exp(-11); // N(m/Kg)2
    // http://www.nyteknik.se/popular_teknik/teknikrevyn/article3766124.ece
	public static final float G = 6.67545E-11F; // N(m/Kg)2  Big G
	
	
	public static float square(float f)
	{
		return f*f;
	}

	public static float square(int i)
	{
		return i*i;
	}

	public static float cube(float f)
	{
		return f*f*f;
	}

	public static float cube(int i)
	{
		return i*i*i;
	}

	public static float exp(int i) {
		if (i>0)
		{
			return 10*exp(i-1);
		}
		else if (i<0)
		{
			return exp(i+1)/10;
		}
		return 1;
	}
	
	public static String addWordToList(String word, String list)
	{
		if (list.length()>0)
		{
			list+=" ";
		}
		list+=word;
		return list;
	}
	
	
	// Returns the position of a word in a list of words
	// Returns -1 if not found.
	public static int getWordPosInList(String word, String list)
	{
		int n=0;
		WordReader wr=new WordReader(list);
		while(wr.isOpenAndNotEnd())
		{
			String a=wr.readWord();
			if (a.equals(word))
			{
				return n;
			}
			n++;
		}
		return -1;
	}

	// Returns the position of a word in a list of words
	// If word is null it will give number of words in list.
	// Returns -1 if not found.
	public static int getNWordInList(String word, String list)
	{
		int n=0;
		WordReader wr=new WordReader(list);
		while(wr.isOpenAndNotEnd())
		{
			String a=wr.readWord();
			if ((word==null) || (a.equals(word)))
			{
				n++;
			}
		}
		return n;
	}

	public static String removeWordFromList(String str, String list)
	{
		WordReader wr=new WordReader(list);
		String newList="";
		while(wr.isOpenAndNotEnd())
		{
			String a=wr.readWord();
			if ((str!=null) && (!str.equals(a)))
			{
				newList=addWordToList(a, newList);
			}
		}
		return newList;
	}
	
}
