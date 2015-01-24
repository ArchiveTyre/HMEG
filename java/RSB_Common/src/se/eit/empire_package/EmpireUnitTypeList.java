//
//Copyright (C) 2013 Henrik Björkman www.eit.se
//
//History:
//Adapted for use with RSB. Henrik 2013-05-04


package se.eit.empire_package;



//import java.util.Random;


//import se.eit.rsb_package.*;
import se.eit.db_package.*;
import se.eit.web_package.*;


public class EmpireUnitTypeList extends EmpireBase {
	
    final int nTypes=3;
    final static public int InfE=EmpireUnitType.InfiniteBuildCostEconomical;
    final static public int InfCC=EmpireUnitType.InfiniteBuildCostCapacity;
    final static public int InfMC=EmpireUnitType.InfiniteMoveCost;
    final static public int InfM=EmpireUnitType.InfiniteMass;
    
    final static public int NothingType=0;
    final static public int TowerType=1;
    final static public int CityType=2;
    final static public int CrewType=3;
    
    /*
    public final EmpireUnitType types[]={
    		new EmpireUnitType("city", 0, 1, 10, 999, 999, 0),
    		new EmpireUnitType("inf", 10, 0, -1,   5, 15, 0),
    		new EmpireUnitType("ship",10, 0, -2, 999,   1, 0)
    };*/

    
	public static String className()
	{	
		return EmpireUnitTypeList.class.getSimpleName();	
	}

	public EmpireUnitTypeList(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);

		generateTypes();
	}

	public EmpireUnitTypeList()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
	}	
	
	public void generateTypes()
	{
		//EmpireUnitType c=null;
		//                         type               build    build  income     land     sea    mass   carry   carry      max     attack   attack     land    land      sea    crew             can transformation    disassembles                                                      possible order                              can build     material needed       required terrain
 		//                         name                cost capacity             move    move        capacity     cap   health        hit strength  defense defense  defense               transform       material              to	                                                                                                                   to be built
	 	//                                            money     cost             cost    cost            land     sea           probability    max   moving   still                             into         needed                                                                                                                                  
/*  0 */ new EmpireUnitType(this, "nothing",           InfE,   InfCC,      0,   InfMC,  InfMC,      0,      0,      0,       10,        0,       0,       0,      0,       0,      0,             "",            "",             "",                                                                 "",                                    "",                 "",                    "");
/*  1 */ new EmpireUnitType(this, "castle",             InfE,   InfCC,    100,   InfMC,  InfMC,   InfM,   InfM,      0,      800,      100,     100,     100,    200,       1,      0,             "",            "",            "",                                         "build unload cancelOrder",                       "crew engineer",                 "",           "land city");
/*  2 */ new EmpireUnitType(this, "townhall",          1000,      20,    100,   InfMC,  InfMC,   InfM,   InfM,      0,      100,      100,     100,     100,    200,       1,      0,             "",            "",             "",                                         "build unload cancelOrder",                       "crew engineer",                 "",           "land city");
/*  3 */ new EmpireUnitType(this, "crew",               200,      20,    -20,      10,     50,     10,     15,     10,      100,       50,      50,      50,    200,      10,      1,     "infantry",     "muskets",     "engineer", "build moveTo goTo unload transform disassemble scrap cancelOrder",            "docks farm mine townhall",                 "",                    "");
/*  4 */ new EmpireUnitType(this, "engineer",           200,      20,    -20,      10,     50,     10,     15,     10,      100,       50,      50,      50,    200,      10,      1,     "infantry",     "muskets",         "crew", "build moveTo goTo unload transform disassemble scrap cancelOrder",            "docks farm mine townhall",                 "",                    "");
/*  5 */ new EmpireUnitType(this, "infantry",           200,      20,    -20,      10,     50,     10,     15,      0,      100,      100,     100,     100,    200,      10,      0,"engineer crew",            "", "crew muskets",       "moveTo goTo unload transform disassemble scrap cancelOrder",                                    "",                 "",                "land");
/*  6 */ new EmpireUnitType(this, "horse",              200,      20,    -10,       3,     50,     30,     45,      0,      100,      100,     100,     100,    200,      10,     -1,      "cavalry",        "crew",        "wagon",       "moveTo goTo unload transform disassemble scrap cancelOrder",                                    "",                 "",                "land");
/*  6 */ new EmpireUnitType(this, "cavalry",            400,      20,    -30,       3,     50,     30,     45,      0,      100,      100,     100,     100,    200,      10,      0,   "wagon crew",            "",   "horse crew",       "moveTo goTo unload transform disassemble scrap cancelOrder",                                    "",                 "",                "land");
/*  7 */ new EmpireUnitType(this, "wagon",              200,      20,    -10,      10,     50,     30,     90,      0,      100,      100,     100,     100,    200,      10,     -1,      "cavalry",        "crew",        "horse",       "moveTo goTo unload transform disassemble scrap cancelOrder",                                    "",                 "",                "land");
/*  8 */ new EmpireUnitType(this, "cannon",             200,      20,     -3,   InfMC,  InfMC,     40,     45,      0,      100,      100,     800,     100,    200,      10,     -1,             "",            "",             "",                             "moveTo goTo unload scrap cancelOrder",                                    "",                 "",                "land");
/*  9 */ new EmpireUnitType(this, "muskets",            100,      10,     -1,   InfMC,  InfMC,     10,     11,      0,      100,      100,     100,     100,    200,      10,     -1,     "infantry",        "crew",             "",                   "moveTo goTo unload transform scrap cancelOrder",                                    "",                 "",                "land");
/* 10 */ new EmpireUnitType(this, "ship",               100,      10,     -3,   InfMC,      3,    200,      0,    400,      100,      100,      50,     100,    100,     100,     -1,             "",            "",             "",                             "moveTo goTo unload scrap cancelOrder",                                    "",                 "",                 "sea");
/* 12 */ new EmpireUnitType(this, "frigate",            400,      40,     -8,   InfMC,      2,    400,      0,    600,      400,      100,     400,     100,    100,     100,     -2,             "",            "",  "ship cannon",                 "moveTo goTo unload disassemble scrap cancelOrder",                                    "",   "cannon muskets",                 "sea");
/* 13 */ new EmpireUnitType(this, "docks",              200,      20,      0,   InfMC,  InfMC,   InfM,      0,      0,      100,        0,       0,       1,     10,       1,     -1,             "",            "",             "",                                          "build scrap cancelOrder",                        "ship frigate",                 "",            "land sea");
/* 14 */ new EmpireUnitType(this, "farm",               200,      20,      0,   InfMC,  InfMC,   InfM,      0,      0,      100,        0,       0,       1,     10,       1,     -1,             "",            "",             "",                                          "build scrap cancelOrder",                 "horse wagon cavalry",                 "",          "land field");
/* 15 */ new EmpireUnitType(this, "mine",               200,      20,      0,   InfMC,  InfMC,   InfM,      0,      0,      100,        0,       0,       1,     10,       1,     -1,             "",            "",             "",                                          "build scrap cancelOrder",             "muskets infantry cannon",                 "", "land mineralDeposit");
	} 
	
	
    public int getNUnitTypes()
    {
    	//return types.length;
    	return this.getNSubObjects();
    }

    public String getUnitTypeName(int i)
    {
    	//return types[i].getName();
    	return getUnitType(i).getName();
    }
	
    public EmpireUnitType getUnitType(int i)
    {
    	return (EmpireUnitType)this.getObjFromIndex(i);
   	
    }
    
}