/*
Copyright (C) 2016 Henrik Björkman (www.eit.se/hb)
License: www.eit.se/rsb/license
*/
package se.eit.rsb_package;

import se.eit.db_package.*;
import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;


// TODO This class should not import yukigassen_pkg and not use YukigassenAvatar. 

// This class keeps score and will tell where the players avatar is and which team.


public class AvatarPlayerReferences extends DbThreadSafe {

	public PlayerData player=null;
	public boolean isActive=false;
	public int avatarId=-1;
	public int teamIndex=-1;

	public int fragScore=0;
	public int lossScore=0;

	public RsbRoundBuffer rsbRoundBuffer=null;

	public static String className()
	{	
		// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
		return AvatarPlayerReferences.class.getSimpleName();	
	}
	

	public AvatarPlayerReferences()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		avatarId=wr.readInt();
		isActive=wr.readBoolean();
		teamIndex=wr.readInt();
		fragScore=wr.readInt();
		lossScore=wr.readInt();

		isActive=false; // We only ever read AvatarPlayerReferences when we load from disk. At that point no player is active even if they were when saving to disk.
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(avatarId);
		ww.writeBoolean(isActive);
		ww.writeInt(teamIndex);
		ww.writeInt(fragScore);
		ww.writeInt(lossScore);
	}	
	
	@Override
	public void listInfo(WordWriter pw)
	{
		super.listInfo(pw);					
		pw.println("avatarId "+avatarId);		
		pw.println("isActive "+isActive);		
		pw.println("teamIndex "+teamIndex);		
		pw.println("fragScore "+fragScore);		
		pw.println("lossScore "+lossScore);		
	}
	
	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("avatarId"))
		{
			avatarId=wr.readInt();
			this.setUpdateCounter();
			return 1;
		}
		else if (infoName.equals("teamIndex"))
		{
			teamIndex=wr.readInt();
			this.setUpdateCounter();
			return 1;
		}
		else if (infoName.equals("isActive"))
		{
			isActive=wr.readBoolean();
			this.setUpdateCounter();
			return 1;
		}
		else if (infoName.equals("fragScore"))
		{
			fragScore=wr.readInt();
			this.setUpdateCounter();
			return 1;
		}
		else if (infoName.equals("lossScore"))
		{
			lossScore=wr.readInt();
			this.setUpdateCounter();
			return 1;
		}
		
		return super.setInfo(wr, infoName);
	}
	
	@Override
	public int getInfo(WordWriter ww, String infoName, WordReader wr)
	{
		if (infoName.equals("isActive"))
		{
			ww.writeBoolean(isActive);
			return 1;
		}
		else if (infoName.equals("teamIndex"))
		{
			ww.writeInt(teamIndex);
			return 1;
		}
		else if (infoName.equals("fragScore"))
		{
			ww.writeInt(fragScore);
			return 1;
		}
		else if (infoName.equals("lossScore"))
		{
			ww.writeInt(lossScore);
			return 1;
		}
		
		return super.getInfo(ww, infoName, wr);
	}
	
	public void setActive(boolean active)
	{
		if (isActive!=active)
		{
			isActive=active;
			this.setUpdateCounter();
		}
	}
	
	
	public void setTeam(int team)
	{
		if (this.teamIndex!=team)
		{
			this.teamIndex=team;
			this.setUpdateCounter();
			
		/*
			DbSubRoot r = this.getDbSubRoot();		
			DbBase a = r.getDbIdObj(avatarId);		
			if ((a!=null) && (a instanceof YukigassenAvatar))
			{
				// TODO This shall be rewritten so we don't need a Yukigassen class here.
				YukigassenAvatar avatar=(YukigassenAvatar)a;
				avatar.setTeam(team);
			}
		*/
		}
	}
		
	public void swapTeam()
	{
		DbSubRoot r = this.getDbSubRoot();		
		DbBase a = r.getDbIdObj(avatarId);
		
		/*
		if ((a!=null) && (a instanceof YukigassenAvatar))
		{
			// TODO This shall be rewritten so we don't need a Yukigassen class here. Perhaps we need an AvatarInterface?
			YukigassenAvatar avatar=(YukigassenAvatar)a;
					
			if (teamIndex<2)
			{
				setTeam(teamIndex+1);
			}
			else
			{
				setTeam(1);											
			}
			
			
			avatar.postMessageToThis("you are now in team "+teamIndex);
		}*/
		debug("not implemented yet");
	}


	// Find or create the message buffer
	public RsbRoundBuffer findOrCreateRsbRoundBuffer()
	{
		
		// Do we have a quick reference to it?
		if (rsbRoundBuffer!=null)
		{
			// We already have a short cut to it.
			return rsbRoundBuffer;
		}
		
		
		
		// Check if it already exits among our sub (AKA child) objects.
		// TODO: This should not be needed any longer, the reference is set by regNamedObject instead.
		if (this.listOfStoredObjects!=null)
		{
			for (DbBase db : this.listOfStoredObjects)
			{
				if (db instanceof RsbRoundBuffer)
				{
					debug("Did not think this was still used.");
					// Found it, remember it and return the reference.
					rsbRoundBuffer=(RsbRoundBuffer)db;
					return rsbRoundBuffer;
				}		
			}
		}
		
		// Create a new one.
		
		rsbRoundBuffer = new RsbRoundBuffer(this, "_rrb");
		//this.addObject(erb);
		//erb.setUpdateCounter(); // There is a problem with setting update counter, it does not work when addObject is done. When that is fixed this line can be removed.
		return rsbRoundBuffer;
	}

	
	// Register the objects created by getOrCreateSubRoom (or by createNamedObjects)
	@Override
	public void regNamedObject(DbNamed addedObject)
	{
		if ((addedObject instanceof RsbRoundBuffer) && (addedObject.getName().charAt(0)=='_'))
		{
			rsbRoundBuffer = (RsbRoundBuffer)addedObject;
		}
		else
		{
			super.regNamedObject(addedObject);
		}
	}

	// This is called when a message to this player is to be posted.
	@Override
	public void postMessageToThis(String str)
	{
		debugWriteLock();

		// find the round buffer and post message to it
		RsbRoundBuffer rrb = findOrCreateRsbRoundBuffer();
		rrb.postMessageToThis(str);
		//setUpdateCounter();
	}

	
	public void incFragScore()
	{
		fragScore++;
		this.setUpdateCounter();
	}
	
	public void incLostScore()
	{
		lossScore++;
		this.setUpdateCounter();
	}


}
