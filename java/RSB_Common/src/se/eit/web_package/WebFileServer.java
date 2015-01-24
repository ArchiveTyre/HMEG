package se.eit.web_package;

public interface WebFileServer {

	// Returns null if not found
	public WebFileData getFileData(String fileName);

	
}
