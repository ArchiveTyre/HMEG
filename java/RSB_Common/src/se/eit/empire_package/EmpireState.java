//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;

import se.eit.db_package.*;
import se.eit.web_package.*;



public class EmpireState extends ActiveObject {
	
	final int startMoney=10;


	public int savedMoney=0;

    public String headOfState="";
    public String allyIdList=""; // TODO: Rename this to allies.
    public int homeSectorId=0;

    public int moneyChange=0;
    
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return EmpireState.class.getSimpleName();	
	}


	public EmpireState(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);
		generateNation();
	}
	

	public EmpireState()
	{	
		super();
	}

	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		savedMoney=wr.readInt();
		moneyChange=wr.readInt();
		headOfState=wr.readString();
		allyIdList=wr.readString();
		homeSectorId=wr.readInt();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(savedMoney);
		ww.writeInt(moneyChange);
		ww.writeString(headOfState);
		ww.writeString(allyIdList);
		ww.writeInt(homeSectorId);
	}	
	
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);
		pw.println(prefix+"savedMoney "+savedMoney);
		pw.println(prefix+"moneyChange "+moneyChange);
		pw.println(prefix+"headOfState "+headOfState);
		pw.println(prefix+"allyIdList "+allyIdList);
		pw.println(prefix+"homeSectorId "+homeSectorId);
	}
	
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("savedMoney"))
		{
			savedMoney=wr.readInt();
			return 1;
		}
		else if (infoName.equals("headOfState"))
		{
			headOfState=wr.readString();
			return 1;
		}
		else if (infoName.equals("allyIdList"))
		{
			allyIdList=wr.readString();
			return 1;
		}
		else if (infoName.equals("homeSectorId"))
		{
			homeSectorId=wr.readInt();
			return 1;
		}
		else
		{
		  return super.setInfo(wr, infoName);
		}
	}
	
	
	public void generateNation()
	{
		savedMoney=startMoney;
		homeSectorId=-1; // no home sector yet, shall be assigned by EmpireTerrain.generate.
	}
	
	
	@Override	
	public void gameTick(long gameTime)
	{
		final EmpireWorld ew=getEmpireWorld();
		
		DbIdList il=this.getDbIdList();					
		DbBase db=il.getDbIdObj(homeSectorId);
		if ((db!=null) && (db instanceof EmpireSector))
		{
			EmpireSector es=(EmpireSector)db;
			if (es.owner==this.getIndex())
			{
				// The state does own its home sector. It will receive some extra money for that.
				moneyChange+=EmpireUnitType.HomeSectorBonus;
			}
		}
		
		//ew.setTickTimeCallback(this);
		ew.setTickCleanupCallback(this);
	}
	
	@Override	
	public void gameTickCleanup()
	{
		/*
		if (moneyChange<-100)
		{
			debug(""+savedMoney+" "+moneyChange);
		}
		*/
		
		// Apply a little inflation so that players can't save up infinite amounts of money.
		if (savedMoney>0)
		{
			final int inflationLoss=(savedMoney+99)/100;
			moneyChange-=inflationLoss;
		}
		
		if (moneyChange!=0)
		{
			this.setUpdateCounter();
		}
		
		savedMoney+=moneyChange;
		moneyChange=0;
	}

	// Find or create the message buffer
	public EmpireRoundBuffer findEmpireRoundBuffer()
	{
		int n=this.getListCapacity();
		for(int i=0;i<n;i++)
		{
			DbBase b=this.getObjFromIndex(i);
			if (b instanceof EmpireRoundBuffer)
			{
				return (EmpireRoundBuffer)b;
			}
		}
		EmpireRoundBuffer erb = new EmpireRoundBuffer(this, "erb"+this.getIndex());
		//this.addObject(erb);
		//erb.setUpdateCounter(); // There is a problem with setting update counter, it does not work when addObject is done. When that is fixed this line can be removed.
		return erb;
	}
	
	
	public void postMessage(String str)
	{
		debugWriteLock();

		// find the round buffer and post message to it
		EmpireRoundBuffer erb = findEmpireRoundBuffer();
		erb.postMessage(str);
		setUpdateCounter();
	}
	
	public void addAlly(String str)
	{
		if (allyIdList.length()>0)
		{
			allyIdList+=" ";
		}
		allyIdList+=str;
	}
	
	// Str must be an object id, e.g. ~33
	public boolean isAlly(String str)
	{
		WordReader wr=new WordReader(allyIdList);
		while(wr.isOpenAndNotEnd())
		{
			String a=wr.readWord();
			if (a.equals(str))
			{
				return true;
			}
		}
		return false;
	}

	public int rmAlly(String str)
	{
		int n=0;
		WordReader wr=new WordReader(allyIdList);
		allyIdList="";
		while(wr.isOpenAndNotEnd())
		{
			String a=wr.readWord();
			if ((str!=null) && (!str.equals(a)))
			{
				addAlly(a);
			}
			else
			{
				n++;
			}
		}
		return n;
	}

}