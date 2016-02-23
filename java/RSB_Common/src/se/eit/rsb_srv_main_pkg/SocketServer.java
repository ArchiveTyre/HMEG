// SocketServer.java
//
// Copyright (C) 2015 Henrik Björkman www.eit.se/hb
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_srv_main_pkg;

import se.eit.db_package.DbSubRoot;
import se.eit.rsb_factory_pkg.GlobalConfig;
import se.eit.rsb_srv_main_pkg.PlayerConnectionThread;
import se.eit.web_package.WebConnection;
import se.eit.web_package.WebSocketConnection;
import se.eit.web_package.WebSocketServer;

public class SocketServer implements WebSocketServer {

    private GlobalConfig config;
	private DbSubRoot dbRoot;
	private LoginLobbyConnection loginServerConnection;


	public SocketServer(GlobalConfig config, DbSubRoot dataBase, LoginLobbyConnection loginServerConnection) 
    {
    	this.config=config;
    	this.dbRoot=dataBase;
    	this.loginServerConnection=loginServerConnection;
    }
	
	
	@Override
	public WebSocketConnection newSocketServer(WebConnection webConnection) {
		return new PlayerConnectionThread(config, webConnection, dbRoot, loginServerConnection);
	}

}
