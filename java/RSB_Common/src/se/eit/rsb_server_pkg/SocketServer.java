package se.eit.rsb_server_pkg;

import se.eit.db_package.DbRoot;
import se.eit.rsb_srv_main_pkg.GlobalConfig;
import se.eit.web_package.WebConnection;
import se.eit.web_package.WebSocketConnection;
import se.eit.web_package.WebSocketServer;

public class SocketServer implements WebSocketServer {

    private GlobalConfig config;
	private DbRoot dbRoot;


	public SocketServer(GlobalConfig config, DbRoot dataBase) 
    {
    	this.config=config;
    	this.dbRoot=dataBase;
    }
	
	
	@Override
	public WebSocketConnection newSocketServer(WebConnection webConnection) {
		return new PlayerConnectionThread(config, webConnection, dbRoot);
	}

}
