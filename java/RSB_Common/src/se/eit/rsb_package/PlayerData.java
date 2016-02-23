// PlayerData.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Adapted for use with RSB. Henrik 2013-05-04


package se.eit.rsb_package;

import se.eit.db_package.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import se.eit.rsb_factory_pkg.GlobalConfig;
import se.eit.web_package.*;




public class PlayerData extends DbTickReceiver {

    public final static String nameOfPlayersDb = "playersDatabase"; 
    public final static String typeOfPlayersDb = "DbNoSaveRoot"; 
    
    public String password=null; // TODO: Do not store password in clear text!
    protected String registeredFromAdr; // IP from which player registered
    public String emailAddress;
    public int regMinutes; // In minutes since 1970-01-01
    public int emailVerificationCode; // Set to 0 if the email is verified.
    protected String realName="unknown";
    protected String phoneNumber="unknown";
    protected String snailAdress="unknown";
    protected String realCountry="unknown";
    public int playerCode=0;
    static Random generator = new Random();

    
    static public int defaultActionForKey[]=new int[256]; // Which action to be taken in default setting for a given key (the key is coded using internal key codes)

    public int actionForKey[]=new int[256]; // Which action to be taken for a given key (the key is coded using internal key codes)

    
    // Map an integer key code to a string
	public HashMap<Integer, String> quickKeyCmdHashMap = new HashMap<Integer, String>(); // Key, Value
    
    
	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return PlayerData.class.getSimpleName();	
	}
    
	public PlayerData()
	{
		super();
		playerCode=generator.nextInt();
		setDefault();
	}
    
    public PlayerData(DbContainer parent, String name, String registeredFromAdr, String emailAddress, GlobalConfig globalConfig) {
        super();
        if (parent!=null)
        {
        	this.linkSelf(parent);
        }
        this.regName(name);
        this.registeredFromAdr = registeredFromAdr;
        this.emailAddress = emailAddress;
        this.regMinutes = (int)(System.currentTimeMillis()/(60000L));
        this.emailVerificationCode = 1 + generator.nextInt(999999);
        //this.setGlobalConfig(globalConfig);
		playerCode=generator.nextInt();
		setDefault();
    }

    // deserialize from wr
	@Override
    public void readSelf(WordReader wr)    
    {
        super.readSelf(wr);
        password=wr.readString();
        registeredFromAdr=wr.readString();
        emailAddress=wr.readString();
        regMinutes=wr.readInt();
        emailVerificationCode=wr.readInt();
        realName=wr.readString();
        phoneNumber=wr.readString();
        snailAdress=wr.readString();
        realCountry=wr.readString();

        {
	        String tmp=wr.readString();
	        WordReader wr2=new WordReader(tmp);
	        while (wr2.isOpenAndNotEnd())
	        {
	        	String keyName=wr2.readWord();
	        	String actionName=wr2.readWord();
	        	int internalActionCode=getCodeForUserAction(actionName);
	        	int internalKeyCode=getCodeForInternalKeyCode(keyName);
	        	set(internalActionCode, internalKeyCode);
	        }
        }

        {
	        String tmp=wr.readString();
	        WordReader wr2=new WordReader(tmp);
	        while (wr2.isOpenAndNotEnd() && wr2.isNextIntOrFloat())
	        {
	        	int key=wr2.readInt();
	        	String actionName=wr2.readString();
	        	quickKeyCmdHashMap.put(key, actionName);
	        }
        }
        
    }

    // serialize to ww
	@Override
    public void writeSelf(WordWriter ww)
    {        
        super.writeSelf(ww);
        ww.writeString(password);
        ww.writeString(registeredFromAdr);
        ww.writeString(emailAddress);
        ww.writeInt(regMinutes);
        ww.writeInt(emailVerificationCode);
        ww.writeString(realName);
        ww.writeString(phoneNumber);
        ww.writeString(snailAdress);
        ww.writeString(realCountry);

        {
	        WordWriter ww2=new WordWriter();
	    	for(int i=0; i<actionForKey.length;i++)
	    	{
	    		int s=actionForKey[i];
	    		if (s!=0)
	    		{
	    			ww2.writeWord(getNameOfInternalKeyCode(i));
	    			ww2.writeWord(getNameOfUserActionCode(s));
	    		}
	   	   	}
	    	String tmp=ww2.getString();
	    	ww.writeString(tmp);
        }
    	
        {
	        WordWriter ww2=new WordWriter();
	    	for (Integer key: quickKeyCmdHashMap.keySet())
	    	{
	    		String value=quickKeyCmdHashMap.get(key);	    		
	    		ww2.writeInt(key);
	    		ww2.writeString(value);
	    	}
	    	String tmp=ww2.getString();
	    	ww.writeString(tmp);
        }
    }
    
	public String getRegisteredFromAdr()
	{
		return registeredFromAdr;
	}
	
	public void set(int internalActionCode, int internalKeyCode)
	{
		// Find previous uses and clear those
    	for(int i=0; i<actionForKey.length; i++)
    	{
    		if (actionForKey[i]==internalActionCode)
    		{
    			actionForKey[i]=0;
    		}
    	}
    	
    	// Set new mapping
    	actionForKey[internalKeyCode]=internalActionCode;
    	
    	this.setUpdateCounter();
		
	}

    public void addPlayerPrefThreadSafe(int internalActionCode, int internalKeyCode)
    {	
    	String prefName=getNameOfUserActionCode(internalActionCode);
    	String prefValue=getNameOfInternalKeyCode(internalKeyCode);
    	
    	debug("addPlayerPrefThreadSafe "+prefName+" "+prefValue);
   	
    	
		this.lockWrite();
		try
		{
	    	set(internalActionCode, internalKeyCode);
/*
	    	DbBase b = this.findObjectByNameAndType(prefName, "RsbString");
			if (b!=null)
			{
				WordReader wr=new WordReader(prefValue);
				b.setInfo(wr, "order");
			}
			else
			{
				RsbString rs = new RsbString();
				rs.setName(prefName);
				rs.setValue(""+prefValue);
				rs.linkSelf(this);
			}*/
		}
		finally
		{
			this.unlockWrite();
		}
    }

    /*
    public String getPlayerPrefThreadSafe(String prefName)
    {
    	debug("getPlayerPrefThreadSafe");
		this.lockRead();
		try
		{
			DbBase b = this.findObjectByNameAndType(prefName, "RsbString");
			if (b==null)
			{
				return null;
			}
			return b.getStringValue();
		}
		finally
		{
			this.unlockRead();
		}
    }
    */

    
    // See also defaultKeyForCommand in main_sdl2.cpp
    static public int defaultKeyCodeForCommand(String cmd)    	
    {    	
    	for(int i=0; i<defaultActionForKey.length; i++)
    	{
    		if (cmd.equals(getNameOfUserActionCode(defaultActionForKey[i])))
    		{
    			return i;
    		}
    	}

		return '?';
    }
    
    
    // See also defaultKeyForCommand in main_sdl2.cpp
    static public String defaultKeyForCommand(String cmd)    	
    {
    	int c = defaultKeyCodeForCommand(cmd);
    	
    	String n = getNameOfInternalKeyCode(c);
    	return n;
    }
    
    	

    // internal key codes
    // codes used for keys outside the range ((i>' ') && (i<='~'))
	final static public char specUnknown=0;
	final static public char specBackspace=8; // this is nr 8 in ascii table
	final static public char specTab=9; // 9
	final static public char specRightShift=11;
	final static public char specEnter=13; // 13
	final static public char specRightCtrl=15;
	final static public char specLeftShift=16;
	final static public char specLeftCtrl=17;
	final static public char specEsc=27; // 27
	final static public char specSpace=32; // 32
	final static public char specPageUp=33;
	final static public char specPageDown=34;
	final static public char specEnd=35;
	final static public char specHome=36;
	final static public char specRight=37; // should be 39?
	final static public char specUp=38;
	final static public char specLeft=39; // should be 37?
	final static public char specDown=40;

	final static public char spec0=48;  // 48 in ascii table
	final static public char spec1=49;
	final static public char spec2=50;
	final static public char spec3=51;
	final static public char spec4=52;
	final static public char spec5=53;
	final static public char spec6=54;
	final static public char spec7=55;
	final static public char spec8=56;
	final static public char spec9=57;

	final static public char specA=65; // 65 in ascii
	final static public char specB=66;
	final static public char specC=67;
	final static public char specD=68;
	final static public char specE=69;
	final static public char specF=70;
	final static public char specG=71;
	final static public char specH=72;
	final static public char specI=73;
	final static public char specJ=74;
	final static public char specK=75;
	final static public char specL=76;
	final static public char specM=77;
	final static public char specN=78;
	final static public char specO=79;
	final static public char specP=80;
	final static public char specQ=81;
	final static public char specR=82;
	final static public char specS=83;
	final static public char specT=84;
	final static public char specU=85;
	final static public char specV=86;
	final static public char specW=87;
	final static public char specX=88;
	final static public char specY=89;
	final static public char specZ=90;

	final static public char specNumPad0=96;
	final static public char specNumPad1=97;
	final static public char specNumPad2=98;
	final static public char specNumPad3=99;
	final static public char specNumPad4=100;
	final static public char specNumPad5=101;
	final static public char specNumPad6=102;
	final static public char specNumPad7=103;
	final static public char specNumPad8=104;
	final static public char specNumPad9=105;
	final static public char specNumPadMult=106;
	final static public char specNumPadPlus=107;
	final static public char specNumPadDel=108;
	final static public char specNumPadMinus=109;
	final static public char specNumPadDiv=111;

	final static public char specF1=112;
	final static public char specF2=113;
	final static public char specF3=114;
	final static public char specF4=115;
	final static public char specF5=116;
	final static public char specF6=117;
	final static public char specF7=118;
	final static public char specF8=119;
	final static public char specF9=110;
	final static public char specF10=121;
	final static public char specF11=122;
	final static public char specF12=123;
	final static public char specPlus=171;
	final static public char specStar=222;
	final static public char specMinus=173;
	final static public char specAltGr=225;
	

    // Translate an internal key code to a string name
    static public String getNameOfInternalKeyCode(int internalKeyCode)
    {
		switch(internalKeyCode)
		{
			case specUnknown: return "unknownKey";
			case specUp: return "upArrow";
			case specDown: return "downArrow";
			case specLeft: return "leftArrow";
			case specRight: return "rightArrow";
			case specHome: return "home";
			case specEnd: return "end";
			case specBackspace: return "backspace";
			case specTab: return "tab";
    		case specEnter: return "enter";
    		case specEsc: return "esc";
    		case specSpace: return "space";
    		case specLeftShift: return "leftShift";
    		case specRightShift: return "rightShift";
    		case specLeftCtrl: return "leftCtrl";
    		case specRightCtrl: return "rightCtrl";
    		case spec0: return "0";
    		case spec1: return "1";
    		case spec2: return "2";
    		case spec3: return "3";
    		case spec4: return "4";
    		case spec5: return "5";
    		case spec6: return "6";
    		case spec7: return "7";
    		case spec8: return "8";
    		case spec9: return "9";
    		case specA: return "A";
    		case specB: return "B";
    		case specC: return "C";
    		case specD: return "D";
    		case specE: return "E";
    		case specF: return "F";
    		case specG: return "G";
    		case specH: return "H";
    		case specI: return "I";
    		case specJ: return "J";
    		case specK: return "K";
    		case specL: return "L";
    		case specM: return "M";
    		case specN: return "N";
    		case specO: return "O";
    		case specP: return "P";
    		case specQ: return "Q";
    		case specR: return "R";
    		case specS: return "S";
    		case specT: return "T";
    		case specU: return "U";
    		case specV: return "V";
    		case specW: return "W";
    		case specX: return "X";
    		case specY: return "Y";
    		case specZ: return "Z";
    		case specNumPad0: return "NumPad0";
    		case specNumPad1: return "NumPad0";
    		case specNumPad2: return "NumPad0";
    		case specNumPad3: return "NumPad0";
    		case specNumPad4: return "NumPad0";
    		case specNumPad5: return "NumPad0";
    		case specNumPad6: return "NumPad0";
    		case specNumPad7: return "NumPad0";
    		case specNumPad8: return "NumPad0";
    		case specNumPad9: return "NumPad0";
    		case specNumPadMult: return "NumPadMultiply";
    		case specNumPadPlus: return "NumPadPlus";
    		case specNumPadDel: return "NumPadPeriod";
    		case specNumPadMinus: return "NumPadMinus";
    		case specNumPadDiv: return "NumPadDivide";
			case specPageUp: return "pageUp";
			case specPageDown: return "pageDown";
    		case specF1: return "F1";
    		case specF2: return "F2";
    		case specF3: return "F3";
    		case specF4: return "F4";
    		case specF5: return "F5";
    		case specF6: return "F6";
    		case specF7: return "F7";
    		case specF8: return "F8";
    		case specF9: return "F9";
    		case specF10: return "F10";
    		case specF11: return "F11";
    		case specF12: return "F12";
    		case specPlus: return "Plus";
    		case specStar: return "Star";
    		case specMinus: return "Minus";
    		case specAltGr: return "AltGr";
    		default: return "n"+(char)internalKeyCode;
		}
    }

    
    // user action codes, these must have same numbers as actionCodes in SymMap.h
    // These codes are mainly used on the client side.
	static public final int undefinedKey=0;
	static public final int actionKey=1;
	static public final int backOrCancel=2;
	static public final int enterText=5;
	static public final int jumpUp=6;
	static public final int moveForward=7;
	static public final int moveBackwards=8;
	static public final int moveRight=9;
	static public final int moveLeft=10;
	static public final int showScore=12;
	static public final int mouseCapture=15;
	static public final int endCode=16;
	static public final int rollRight_deprecated=17;
	static public final int rollLeft_deprecated=18;
	static public final int yawRight_deprecated=19;
	static public final int yawLeft_deprecated=20;
	static public final int pitchUp_deprecated=22;
	static public final int pitchDown_deprecated=23;
	static public final int testBeep=24;
	static public final int clearText=26;
	static public final int upOne=27;
	static public final int downOne=28;
	static public final int leftOne=29;
	static public final int rightOne=30;
	static public final int pageUp=31;
	static public final int pageDown=32;
	static public final int returnCode=33;
	static public final int mouseRelease=34;
	static public final int crouchOrDuck=35;
	static public final int invScreen=36;
	
	// See also symMapgetSymFromName in PlayerData.java
    static public String getNameOfUserActionCode(int userActionCode)
	{
		switch(userActionCode)
		{
		case undefinedKey: return "undefinedKey";
		case actionKey: return "actionKey";
		case backOrCancel: return "backOrCancel";
		case enterText: return "enterText";
		case jumpUp: return "jumpUp";
		case moveForward: return "moveForward";
		case moveBackwards: return "moveBackwards";
		case moveRight: return "moveRight";
		case moveLeft: return "moveLeft";
		case showScore: return "showScore";
		case mouseCapture: return "mouseCapture";
		case endCode: return "endCode";
		case rollRight_deprecated: return "rollRight";
		case rollLeft_deprecated: return "rollLeft";
		case yawRight_deprecated: return "yawRight";
		case yawLeft_deprecated: return "yawLeft";
		case pitchUp_deprecated: return "pitchUp";
		case pitchDown_deprecated: return "pitchDown";
		case testBeep: return "testBeep";
		case clearText: return "clearText";
		case upOne: return "upOne";
		case downOne: return "downOne";
		case leftOne: return "leftOne";
		case rightOne: return "rightOne";
		case pageUp: return "pageUp";
		case pageDown: return "pageDown";
		case returnCode: return "returnCode";
		case mouseRelease: return "mouseRelease";
		case crouchOrDuck: return "crouchOrDuck";
		case invScreen: return "invScreen";
		default: return null;
		}
	}

	// See also symMapgetSymFromName in PlayerData.java
    static public String getDescriptionOfUserActionCode(int userActionCode)
	{
		switch(userActionCode)
		{
		case undefinedKey: return "undefined key";
		case actionKey: return "action key";
		case backOrCancel: return "back or cancel";
		case enterText: return "enter text";
		case jumpUp: return "jump";
		case moveForward: return "move forward";
		case moveBackwards: return "move backwards";
		case moveRight: return "move right";
		case moveLeft: return "move left";
		case showScore: return "show score";
		case mouseCapture: return "mouse capture";
		case endCode: return "end";
		case rollRight_deprecated: return "roll right";
		case rollLeft_deprecated: return "roll left";
		case yawRight_deprecated: return "yaw right";
		case yawLeft_deprecated: return "yaw left";
		case pitchUp_deprecated: return "pitch up";
		case pitchDown_deprecated: return "pitch down";
		case testBeep: return "test beep";
		case clearText: return "clear text";
		case upOne: return "up one";
		case downOne: return "down one";
		case leftOne: return "left one";
		case rightOne: return "right one";
		case pageUp: return "scroll page up";
		case pageDown: return "scroll page down";
		case returnCode: return "return code";
		case mouseRelease: return "mouse release";
		case crouchOrDuck: return "crouch";
		case invScreen: return "open inventory";
		default: return null;
		}
	}

    // Translate an integer key code to a string name
    static public String defaultKeyName(String keyCode)    	
    {
    	WordReader wr=new WordReader(keyCode);
    	if (wr.isNextIntOrFloat())
    	{
    		int i = wr.readInt();
    		return getNameOfInternalKeyCode(i);
    	}
    	return keyCode;
    }
 
    
    
    /*
    public String getPlayerPrefOrDefaultThreadSafe(String prefName)
    {
    	final String str=getPlayerPrefThreadSafe(prefName);

    	if (str!=null)
    	{
        	return str;
    	}    	
    	
    	final String key = defaultKeyForCommand(prefName);
    	return key;
    }
    */

    public int getPlayerPrefKeyForAction(int actionCode)
    {
    	for(int i=0; i<actionForKey.length;i++)
    	{
    		if (actionForKey[i]==actionCode)
    		{
    			return i;
    		}
    	}
    	return -1;
    }
 
    /*
    public String optionString(String description, String action)
    {
    	String key=getPlayerPrefOrDefaultThreadSafe(action);
    	String keyName=(key!=null)?PlayerData.defaultKeyName(key):"undefined";
    	String str=String.format("%-4s %-19s %s","key", description, keyName);
    	return str;
    }
    */

    public String optionString(int actionCode)
    {
    	String description=getDescriptionOfUserActionCode(actionCode);
    	int curKey=getPlayerPrefKeyForAction(actionCode);
    	String curKeyName=getNameOfInternalKeyCode(curKey);

    	String str=String.format("%-31s %s", description, curKeyName);
    	return str;
    }
    
    
    
    // set the user prefered key mapping
    /*
    public void setPref(String prefName, int prefValue)
    {
    	//int pref=defaultKeyCodeForCommand(prefName);
    	addPlayerPrefThreadSafe(prefName, ""+prefValue);
    	
    }*/

    // set the default key mapping (does not set the user preference, use setPref for that)
    public static void setD(int prefUserActionCode, char prefInternalKeyCode)
    {
    	if (defaultActionForKey[prefInternalKeyCode]!=0)
    	{
    		System.out.println("InternalKeyCode already used "+defaultActionForKey[prefInternalKeyCode]+" "+prefUserActionCode+" "+prefInternalKeyCode);
    	}
    	defaultActionForKey[prefInternalKeyCode]=prefUserActionCode;
    }
    
    
    // This will set the user mapping to default (not same thing as setting the defaults)
    public void setDefault()
    {
		DbSubRoot r=this.getDbSubRoot();

		r.lockWrite();
		try
		{
    	
	    	quickKeyCmdHashMap.clear();
	    	
	    	for(int i=0; i<actionForKey.length;i++)
	    	{
	    		actionForKey[i]=defaultActionForKey[i];
	    	}
	    	    	
	    	//quickKeyCmdHashMap.put((int)specNumPadDiv, "do left");
	    	//quickKeyCmdHashMap.put((int)specNumPadMult, "do center");
	    	//quickKeyCmdHashMap.put((int)specNumPadMinus, "do right");
	    	
	    	quickKeyCmdHashMap.put((int)specNumPadPlus, "do on");
	    	quickKeyCmdHashMap.put((int)specPlus, "do on");
	    	
	    	quickKeyCmdHashMap.put((int)spec0, "do off");

	    	quickKeyCmdHashMap.put((int)spec1, "do left");
	    	quickKeyCmdHashMap.put((int)spec2, "do center");
	    	quickKeyCmdHashMap.put((int)spec3, "do right");
	    	
	    	quickKeyCmdHashMap.put((int)spec4, "do reverse");
	    	quickKeyCmdHashMap.put((int)spec5, "do stop");
	    	quickKeyCmdHashMap.put((int)spec6, "do forward");

	    	quickKeyCmdHashMap.put((int)spec7, "do down");
	    	quickKeyCmdHashMap.put((int)spec8, "do level");
	    	quickKeyCmdHashMap.put((int)spec9, "do up");
	    	
	    	quickKeyCmdHashMap.put((int)specR, "teleportHome");
	    	quickKeyCmdHashMap.put((int)specH, "colorNext");
	    	quickKeyCmdHashMap.put((int)specJ, "colorPrevious");
	    	quickKeyCmdHashMap.put((int)specG, "selectAction");
	    	quickKeyCmdHashMap.put((int)specT, "swapTeam");


	    	
			this.setUpdateCounter();
		}
		finally
		{
			r.unlockWrite();
		}
    }

    static {
		setD(actionKey, specF);
		setD(backOrCancel, specBackspace);
		setD(enterText, specV);
		setD(jumpUp, specSpace);
		setD(moveForward, specW);
		setD(moveBackwards, specS);
		setD(moveRight, specD);
		setD(moveLeft, specA);
		setD(showScore, specTab);
		setD(mouseCapture, specHome);
		setD(endCode, specEnd);
		//setD(rollRight, specC);
		//setD(rollLeft, specX);
		//setD(yawRight, specU);
		//setD(yawLeft, specY);
		//setD(swapTeam, specT);
		//setD(pitchUp, specP);
		//setD(pitchDown, specO);
		setD(testBeep, specN);
		setD(clearText, specB);
		setD(upOne, specUp);
		setD(downOne, specDown);
		setD(leftOne, specLeft);
		setD(rightOne, specRight);
		setD(pageUp, specPageUp);
		setD(pageDown, specPageDown);
		setD(returnCode, specEnter);
		setD(mouseRelease, specEsc);
		setD(crouchOrDuck, specLeftShift);
		setD(invScreen, specI);
    }


  
    
    // This will not only send, it will remember the codes to be used in listPlayerPreferences.
    // The comment above must be out dated.
	public void sendPlayerPreferences(WordWriter ww) throws IOException
	{
		int n=0;
		String cmd="playerPreference";

    	for(int i=0; i<actionForKey.length;i++)
    	{
    		int s=actionForKey[i];
    		if (s!=0)
    		{
    			ww.writeWord(cmd);
    			ww.writeWord(getNameOfUserActionCode(s));
    			ww.writeString(""+i);
    			ww.writeEoln();
    			++n;
    		}
   	   	}

    	// If player did not have any mappings, send the defaults.    	
    	if (n==0)
    	{
        	for(int i=0; i<defaultActionForKey.length;i++)
        	{
        		int s=defaultActionForKey[i];
        		if (s!=0)
        		{
        			ww.writeWord(cmd);
        			ww.writeWord(getNameOfUserActionCode(s));
        			ww.writeString(""+i);
        			ww.writeEoln();
        			++n;
        		}
       	   	}  		
    	}
  	}

	
	public void listPlayerPreferences(WordWriter ww) throws IOException
	{
		/*
		if (this.listOfStoredObjects!=null)
	    {
        	for (DbBase b: this.listOfStoredObjects)
        	{
        		if (b instanceof RsbString)
        		{
        			RsbString rs=(RsbString)b;
        			ww.writeWord(cmd);
        			int i=Integer.parseInt(rs.getValue());
        			ww.writeString(rs.getName()+" "+getNameOfInternalKeyCode(i));
        			ww.writeEoln();
        		}
        	}
	    }*/


		
		ww.writeString("Keys that are mapped to a client function:");
    	for(int i=0; i<actionForKey.length;i++)
    	{
    		int s=actionForKey[i];
    		if (s!=0)
    		{
    			String m=String.format("%-15s %s",getNameOfInternalKeyCode(i), getNameOfUserActionCode(s));
    			ww.writeString(m);
    			//ww.writeEoln();
    		}
   	   	}

		ww.writeString("Keys that activate a custom command:"); 	
    	for (Integer key: quickKeyCmdHashMap.keySet())
    	{
    		String value=quickKeyCmdHashMap.get(key);	    		
			String m=String.format("%-15s %s",getNameOfInternalKeyCode(key), value);
    		ww.writeString(m);
    	}

    	
	}
	
	public int getCodeForUserAction(String name)
	{
		for(int i=0;i<actionForKey.length;i++)
		{
			if (name.equals(getNameOfUserActionCode(i)))
			{
				return i;
			}
		}
		return 0;
	}
	
	public int getCodeForInternalKeyCode(String name)
	{
		for(int i=0;i<actionForKey.length;i++)
		{
			if (name.equals(getNameOfInternalKeyCode(i)))
			{
				return i;
			}
		}
		return 0;
	}
	
	
	public String getQuickKeyCmd(int key)
	{		
		/*if ((key>='a') && (key<='z'))
		{
			key = key-'a'+'A';
		}*/

		String value=quickKeyCmdHashMap.get(key);	    		

		return value;
	}

	public void setQuickKeyCmd(int key, String cmd)
	{		
		DbSubRoot r=this.getDbSubRoot();

		/*if ((key>='a') && (key<='z'))
		{
			key = key-'a'+'A';
		}*/

		r.lockWrite();
		try
		{
			debug("setQuickKeyCmd "+key+" '"+cmd+"'");
			quickKeyCmdHashMap.put(key, cmd);	
			this.setUpdateCounter();
		}
		finally
		{
			r.unlockWrite();
		}
	}

	public void removeQuickKeyCmd(int key)
	{		
		DbSubRoot r=this.getDbSubRoot();

		/*if ((key>='a') && (key<='z'))
		{
			key = key-'a'+'A';
		}*/

		r.lockWrite();
		try
		{
			debug("removeQuickKeyCmd "+key);
			quickKeyCmdHashMap.remove(key);	
			this.setUpdateCounter();
		}
		finally
		{
			r.unlockWrite();
		}
	}

	public boolean helpCommand(String cmd, WordReader wr, WordWriter ww, String hist) throws IOException
	{
		ww.writeLine("Player commands:");											
		ww.writeLine("  listKeyMapping");											
		return true;
	}
	
	@Override
	public boolean interpretCommand(String cmd, WordReader wr,WordWriter ww) throws IOException
	{
		// we should call super but LuaBase will try to cast YukigassenWorld w=(YukigassenWorld)this.getDbSubRoot(); and that don't work with this class.
		/*if (super.interpretCommand(cmd, wr, ww))
		{
			return true;
		}*/				
		
		final char ch=cmd.charAt(0);

		switch(ch)
		{
			case 'l':
				if (cmd.equals("listKeyMapping"))
				{
					ww.writeWord("consoleMessage");
					listPlayerPreferences(ww);
					return true;
				}
			case 'h':
				if (cmd.equals("help"))
				{		
					String subCmd = wr.readWord();						
					return helpCommand(subCmd, wr, ww, cmd);
				}
/*
			case 'i':
				if (cmd.equals("invSave"))
				{
					String fileNameUsr=wr.readString();
					
					if (WordWriter.isFilenameOk(fileNameUsr))
					{
						
						// Get saved games folder
						File savesRootDir = new File(getGlobalConfig().savesRootDir);
						final String savesRootPath=savesRootDir.getPath();
						
						// Store in the folder of parent object
						String parentPath = getParentPath();
						File parentDir = new File(savesRootPath+"/"+parentPath);
						final String path=parentDir.getPath();
	
						// Make sure that folder exist
						parentDir.mkdirs();
						
						// Figure out the filename to use
						final String name=this.getNameOrIndex();
						String fileExtension=(!getGlobalConfig().useJson) ? ".txt" : ".json";
						String fileNameIncPath=path+"/"+fileNameUsr+fileExtension;
						System.out.println("saving to " + fileNameIncPath);
						
						// Not writing to that name directly, first saving to temporary file name.
						File f1 = new File(fileNameIncPath+"_part~");
	
						// Create the FileWriter/WordWriter
						FileWriter fw = new FileWriter(f1);
						WordWriter fww = new WordWriterPrintWriter(new PrintWriter(fw));
			
				  		final String programNameAndVersion=Version.getProgramNameAndVersion();
	
				  		if (!getGlobalConfig().useJson)
						{
				  			// Saving in own proprietary format (not JSON).
				  			
							// First in the file Write name and version of this program. This so old incompatible versions can be avoided when reading it back later
					  		fww.writeString(programNameAndVersion);					
	
					  		// Write type of object
				        	fww.writeWord(getType());
				        	
				        	// Dump objects internal data
				        	writeRecursive(fww);
						}
						else
						{
							// Saving in JSON format.
							// To verify if the json file syntax is OK try for example:
							// http://www.jsoneditoronline.org/
							
							// In JSON everything is enclosed in {}, this is first
					  		fww.writeBegin();
					  		
					  		// Write the version info string, this is used so that if program is changed so that old save files can not be read it can ignore those gracefully.
					  		fww.writeString("versionInfo");					
					  		fww.writeWord(":");
					  		fww.writeString(programNameAndVersion);					
					  		fww.writeWord(",");
					  		
	
					  		// Write type of object
					  		fww.writeString(getType());					
					  		
					  		// Dump objects internal data
				        	writeRecursive(fww);
				        				  		
							// In JSON everything is enclosed in {}, this is last
					  		fww.writeEnd();
						}
							  
						
							
						
			        	
			        	// Close the writer and stream
			        	fww.close();
			        	fw.close();
			        	
			        	// Now that the file is saved remove old backup file if any.
						File f2 = new File(fileNameIncPath+"~");
						if (f2.exists())
						{
							f2.delete();
						}								
	
			        	// Rename old file to the backup name and the new _part file to the main name.
						File f3 = new File(fileNameIncPath);
						File f4 = new File(fileNameIncPath+"~");
						f3.renameTo(f4);
						f1.renameTo(f3);
			        	
			        	
						ww.writeLine("saved to path '" + f3.getAbsolutePath()+"'");
	
						
						
					}
					else
					{
						ww.writeLine("Usage:");						
						ww.writeLine("invSave <filename>");						
					}
					ww.writeWord("");
					return true;
				}
*/
			default: 
				break;
		}
		
		return super.interpretCommand(cmd, wr, ww);
	}
}
