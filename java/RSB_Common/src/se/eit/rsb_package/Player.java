//Player.java
//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.rsb_package;
import se.eit.db_package.*;


import java.util.Random;
//import se.eit.rsb_package.*;
import se.eit.web_package.*;




public class Player extends DbRoot {

    public final static String nameOfPlayersDb = "playersDatabase"; 
    
    //public int nationId=0;
    //public String name;
    public String password=null;
    protected String registeredFromAdr; // IP from which player registered
    public String emailAddress;
    public int regMinutes; // In minutes since 1970-01-01
    public int emailVerificationCode; // Set to 0 if the email is verified.
    protected String realName="unknown";
    protected String phoneNumber="unknown";
    protected String snailAdress="unknown";
    protected String realCountry="unknown";
    
    static Random generator = new Random();

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return Player.class.getSimpleName();	
	}
    
	public Player()
	{
		super();	
	}
    
    public Player(DbBase parent, String name, String registeredFromAdr, String emailAddress) {
        super();
        this.setName(name);
        if (parent!=null)
        {
        	this.linkSelf(parent);
        }
        this.registeredFromAdr = registeredFromAdr;
        this.emailAddress = emailAddress;
        this.regMinutes = (int)(System.currentTimeMillis()/(60000L));
        this.emailVerificationCode = 1 + generator.nextInt(999999);
    }

    // deserialize from wr
	@Override
    public void readSelf(WordReader wr)    
    {
        super.readSelf(wr);
        password=wr.readWord();
        registeredFromAdr=wr.readWord();
        emailAddress=wr.readWord();
        regMinutes=wr.readInt();
        emailVerificationCode=wr.readInt();
        realName=wr.readWord();
        phoneNumber=wr.readWord();
        snailAdress=wr.readWord();
        realCountry=wr.readWord();
    }

    // serialize to ww
	@Override
    public void writeSelf(WordWriter ww)
    {        
        super.writeSelf(ww);
        ww.writeWord(password);
        ww.writeWord(registeredFromAdr);
        ww.writeWord(emailAddress);
        ww.writeInt(regMinutes);
        ww.writeInt(emailVerificationCode);
        ww.writeWord(realName);
        ww.writeWord(phoneNumber);
        ww.writeWord(snailAdress);
        ww.writeWord(realCountry);
    }
    
	public String getRegisteredFromAdr()
	{
		return registeredFromAdr;
	}
	
/*
    public String getType()
    {
        return "Player";
    }
*/  


        
}