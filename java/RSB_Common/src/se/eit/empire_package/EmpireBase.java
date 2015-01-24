//
//Copyright (C) 2014 Henrik Björkman www.eit.se
//
//History:
//Created by Henrik 2014-03-08

package se.eit.empire_package;

import se.eit.rsb_package.*;
import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;
import se.eit.db_package.*;

//import java.util.Random;



public class EmpireBase extends GameBase {

	//public long unitTime=0;
	
	
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireBase.class.getSimpleName();	
	}


	public EmpireBase()
	{
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		//unitTime=wr.readLong();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		//ww.writeLong(unitTime);
	}
	
	@Override
	public void listInfo(WordWriter ww, String prefix)
	{
		super.listInfo(ww, prefix);					
		//ww.println(prefix+"unitTime "+unitTime);		
	}
	
	
	public EmpireWorld getEmpireWorld()
	{
	    DbBase bo = this.getContainingObj();
	    
	    if (bo instanceof EmpireWorld)
	    {
			return (EmpireWorld)bo;
	    }
	    else  if (bo instanceof EmpireBase)
	    {
	    	EmpireBase eb=(EmpireBase)bo;
			return eb.getEmpireWorld();
	    }
	    return null;
	}
	
	/*public void generate()
	{
	}*/
	
	public int getOwner()
	{
		return -1;
	}

	/*
	public void gameTick(long gameTime)
	{
	}
	*/

	
	public void gameTickPerformOrders()
	{
	}
	
	public void gameTickInteract()
	{
	}
	
	public void gameTickCleanup()
	{
	}

}