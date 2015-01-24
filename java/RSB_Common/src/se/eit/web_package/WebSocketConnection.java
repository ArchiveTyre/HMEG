package se.eit.web_package;

public interface WebSocketConnection{

	public WebFileData takeSocketData(String string);

	public void start();

	public void close();
}
