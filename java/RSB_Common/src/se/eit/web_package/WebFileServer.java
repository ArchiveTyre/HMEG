/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license 
*/
package se.eit.web_package;

public interface WebFileServer {

	// Returns null if not found
	public WebFileData getFileData(String fileName);

	
}
