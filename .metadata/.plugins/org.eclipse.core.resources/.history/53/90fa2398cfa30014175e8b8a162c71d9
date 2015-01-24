package se.eit.rsb_server_pkg;

import se.eit.rsb_package.Player;
import se.eit.rsb_package.GlobalConfig;
import se.eit.db_package.*;

// this import is probably needed due to some mistake.
import se.eit.empire_package.*;


public class BlockyGame extends ServerBase {

	public BlockyGame(GlobalConfig config, Player player, ServerTcpConnection stc) {
		super(config, player, stc);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void join(DbBase bo) 
		// TODO Auto-generated method stub

		{
			//w.addNotificationReceiver(this, 0);		
			//defaultMibEntry = ro;
			
			if (bo instanceof EmpireWorld)
			{
				join((EmpireWorld)bo);			
			}
			else
			{
				error("not an EmpireWorld");
			}
		}	
	

	@Override
	protected String createAndStore(String worldName) {
		// TODO Auto-generated method stub
		return null;
	}

}
