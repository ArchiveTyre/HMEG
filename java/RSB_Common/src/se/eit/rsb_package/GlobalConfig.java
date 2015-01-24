/*
GlobalConfig.java

Copyright 2013 Henrik Björkman (www.eit.se/hb)


History:
2013-02-27
Created by Henrik Bjorkman (www.eit.se/hb)

2014-11-20
Moved from web package to RSB package
*/

package se.eit.rsb_package;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GlobalConfig {
	public static boolean light=true;
	
	// Some options for using external libraries
	//public static boolean use_mouse_manager=false;
	//public static boolean use_jinput=true;
	//public static boolean use_mouse_adapter=false;
	
	
	//public static final float zFar = 1000000.0f; // Specifies the distance from the viewer to the far clipping plane (always positive).
	public static final float zFar = 1000000000000f; // Specifies the distance from the viewer to the far clipping plane (always positive).
	//public static final float zNear = zFar/1000; // Specifies the distance from the viewer to the near clipping plane (always positive).        
	
	
	public static final int defaultIpPort=8080;


    //public static final int scale=1; // default unit of length in the program in meters. E.g. 1000 here means we use km for all distances if nothing else specified. This will hopefully be set to 1 eventually. For all else SI units are used if nothing else said. E.g if milliseconds are used ms shall be part of the variable name.
    //public static final int massScale=1000000000;


	public static final boolean DEBUG_READ_LOCKS=true;  // If true code will check that database has been read locked when it needs to be. If this is false the code will run faster but not check for that error.


	// Only one of these shall be true
	static final boolean SOLAR_SYSTEM=false;
	static final boolean FLAT_WORLD=true;
	
	public int port=8080;
	public String httpRootDir=System.getProperty("user.dir");
			
	public String savesRootDir=System.getProperty("user.dir");
	
	public boolean startWebBrowser=false;
	
	public GlobalConfig()
	{
	}

	public String toString()
	{
		StringBuffer sb=new StringBuffer();
 	    Path http = Paths.get(httpRootDir);
 	    sb.append("http dir: "+ http.toAbsolutePath()+"\n");
		
 	    Path saves = Paths.get(savesRootDir);
 	    sb.append("save dir: "+ saves.toAbsolutePath()+"\n");
		
  	    sb.append("server ip port: "+ port+"\n");
				
  	    sb.append("start web browser: "+startWebBrowser+"\n");
  	    
		return sb.toString();
	}
	
	public void logConfig(PrintStream out)
	{
		out.println(toString());
	}
	
	
}
