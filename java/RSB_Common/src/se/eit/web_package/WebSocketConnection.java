/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.web_package;

public interface WebSocketConnection{

	public WebFileData takeSocketData(String string);

	public void start();

	public void close();
}
