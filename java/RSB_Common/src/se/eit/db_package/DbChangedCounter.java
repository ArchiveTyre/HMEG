package se.eit.db_package;

import se.eit.web_package.*;



public abstract class DbChangedCounter extends DbIdObj {

	private int counter=0; // This class maintains its own changed counter.



	public DbChangedCounter()
	{	
		super();	
	}	
	
	
	/*public DbChangedCounter(final DbChangedCounter org)
	{	
		super(org);	
	}*/	

	public void linkSelf(DbBase db)
	{
		super.linkSelf(db);
		setUpdateCounter();
	}
	
	public void unlinkSelf()
	{
		setUpdateCounter();
		super.unlinkSelf();
	}

	/*
	public void linkSelf(DbBase db, int index)
	{
		super.linkSelf(db, index);
		setUpdateCounter();
	}
	*/
	
	// Add object bo at position index
	/*
	public int addObject(DbStorable bo, final int index)
	{
		final int r = super.addObject(bo, index);
		setUpdateCounter();
		return r;
	}
	*/
	
	/*
	public int addAndRegObjAtIndex(DbStorable bo, final int index)
	{
		final int r = super.addAndRegObjAtIndex(bo, index);
		setUpdateCounter();		
		return r;
	}
	*/
	
	// To be called when something here has been changed. It shall be set only if something was changed.
	@Override
	public void setUpdateCounter()
	{
		DbRoot r=this.getDbRoot();
		if (r!=null)
		{
			counter=r.getDbRootUpdateCounter(); // is it OK to use getUpdateCounter also outside tick or should we set a local flag end then do this in our tick method? Will do it quick and dirty for now.
			if (r instanceof NotificationSender)
			{
				NotificationSender ns = (NotificationSender)r;
				ns.notifySubscribers(this.getId());
			}
		}
		else
		{
			//debug("no root obj, could not set update counter"); 
		}
	}
	
	
	// Returns true if the properties of this class has been update since value given by rootCounter.  
	@Override
	public boolean isChanged(int previousUpdateCounter)
	{
		final int d=this.counter-previousUpdateCounter;
		if (d>0)
		{
			return true;
		}
		return false;
	}
	
	
	
	// previousUpdateCounter shall be the value root counter had when object update was sent previous time
	// If a class adds its own listChangedObjects then a parse for the class name needs to be added in ClientThread where it interprets the "updatePartly" message.
	// Format here must match that used in ClientThread.updateObj. If this method is changed or moved, remember to update there also.
	@Override
	public void listChangedObjects(int previousUpdateCounter, WordWriter ww)
	{
		
		if (isChanged(previousUpdateCounter))
		{
			// Command to client
			ww.writeWord("updateObj");
			
			// The type of the object to update
			ww.writeWord(this.getType());

			// id of this object
			this.writeIdOrPath(ww);
			
			// id of the objects super object (the room that this object is residing in).
			DbBase so=getContainingObj();
			if (so!=null)
			{
				so.writeIdOrPath(ww);
			}
			else
			{
				ww.writeString("");
			}

			
			// All data for this object (not including sub objects)
			this.writeSelf(ww);
			
			// End of line
			ww.writeLine("");
			
			ww.flush();
		}

		super.listChangedObjects(previousUpdateCounter, ww);
	}

	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);
		
		pw.println(prefix+"counter "+counter);
		
	}

	public int getDbChangedCounter()
	{
		return counter;
	}

	public void notifyOthers()
	{
        final DbRoot dr = getDbRoot();
        final NotificationSender ns = (NotificationSender)dr;
        ns.notifySubscribers(this.getId());
	}

}
