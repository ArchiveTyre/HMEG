// WorldWithPlayers.java
//
// Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
// License: www.eit.se/rsb/license
//
// History:
// Created by Henrik 2015 

package se.eit.rsb_package;

import se.eit.db_package.*;

public abstract class WorldWithPlayers extends ActiveObjectList  {
	
	//public DbList<PlayerData> listOfActivePlayers=null;

	ActivePlayerList listOfActivePlayers=null;
	
	public WorldWithPlayers()
	{
		super();
		//listOfActivePlayers=new DbList<PlayerData>();		
	}
	
	// Call this to add the player to the list of active players
	// Remember to call removePlayer when leaving
	public AvatarPlayerReferences playerConnected(PlayerData player, int team)
	{
		getListOfActivePlayers();
		
		final String playerName=player.getName();
		
		// First check that player is not already playing here
		AvatarPlayerReferences apr = (AvatarPlayerReferences)listOfActivePlayers.findObjectByNameAndType(playerName, "AvatarPlayerReferences");

		DbSubRoot ro = this.getDbSubRoot();
		ro.lockWrite();
		try
		{

			if (apr!=null)			
			{
				if (apr.isActive==false)
				{
					// This is a returning player, the player can now start playing
					
					playerJoinedFindOrCreateAvatar(apr);				
					apr.setTeam(0); // TODO: Force update by setting 0 first. Should not be needed.
					apr.setTeam(team);
					apr.setActive(true);
				}
				else
				{
					// This player is already playing in at this world, can not connect one more.
					return null;
				}
			}
			else
			{
				// This is a new player, create a new avatar.
				apr = playerJoinedCreateApr(player, team);		
				
				setTeam(apr, team);
			}
		}
		finally
		{
			ro.unlockWrite();
		}
		
		
		return apr;
	}
	
	
	public void playerDisconnected(AvatarPlayerReferences apr)
	{				
		
		apr.setActive(false);
			
		DbSubRoot ro = this.getDbSubRoot();
		ro.lockWrite();
		try
		{
			DbIdObj a = ro.getDbIdObj(apr.avatarId);
			apr.avatarId=-1;
			if (a!=null)
			{
				a.unlinkSelf(); // TODO instead of unlink perhaps it should be moved to the apr object. Or in some other way remember its position, so it can reenter at same place.
			}
		}
		finally
		{
			ro.unlockWrite();
		}

		
		//apr.unlinkSelf();
	}		
	
	/*
	public PlayerData getPlayerAvatar(int playerIndex)
	{
		return listOfActivePlayers.get(playerIndex);		
	}
	*/
	
	
	public DbNamed getListOfActivePlayers()
	{
		if (listOfActivePlayers!=null)
		{
			return listOfActivePlayers;
		}
		
		listOfActivePlayers = (ActivePlayerList)findOrCreateChildObject("playerList", "ActivePlayerList");
	    
		return listOfActivePlayers;
	}

	
	public int genNActivePlayers()
	{
		getListOfActivePlayers();
		return listOfActivePlayers.getNActivePlayers();
	}

	
	// Creates the AvatarPlayerReferences (APR) for a player
	// Will also create the avatar for the player.
	// When player leaves playerDisconnected shall be called.
	private AvatarPlayerReferences playerJoinedCreateApr(PlayerData player, int team)
	{
		final String playerName=player.getName();
		AvatarPlayerReferences apr = (AvatarPlayerReferences)listOfActivePlayers.findOrCreateChildObject(playerName, "AvatarPlayerReferences"); 
		apr.player=player;
		apr.setTeam(team);
	
		playerJoinedFindOrCreateAvatar(apr); 
		
		return apr;
	}

	// extending classes 
	public abstract DbThreadSafe playerJoinedFindOrCreateAvatar(AvatarPlayerReferences apr);
	/*{
		// Game worlds where players have avatars must override this method so it creates one.
		return null;
	}*/
	
	public void setTeam(AvatarPlayerReferences apr, int team)
	{
		error("No team in this game");
	}

}
