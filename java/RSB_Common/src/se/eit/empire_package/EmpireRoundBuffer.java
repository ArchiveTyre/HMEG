package se.eit.empire_package;

import se.eit.db_package.*;
import se.eit.web_package.*;


public class EmpireRoundBuffer  extends EmpireBase {

	int maxObjects=256; // max number of messages stored in the round buffer, max index is: maxObjects*2-1
    int head=0; // This is the first (oldest) message in the queue
    int tail=0; // This is where messages are added to the queue
	int msgCount=0;

	public EmpireRoundBuffer(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);
	}

	public EmpireRoundBuffer()
	{	
		super();
	}
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		head=wr.readInt();
		tail=wr.readInt();
		msgCount=wr.readInt();
		maxObjects=wr.readInt();
	}

	// serialize to ww
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(head);
		ww.writeInt(tail);
		ww.writeInt(msgCount);
		ww.writeInt(maxObjects);
	}	
	
	private int incAndWrap(int i)
	{
		if (++i==maxObjects*2)
		{
			i=0;
		}
		return i;
	}

	public void postMessage(String str)
	{
		debugWriteLock();

		// First remove oldest message (if the round buffer is full = maxObjects)
		for(int i=0;i<this.getMaxIndex();i++)
		{
			if (getNSubObjects()<maxObjects)
			{
				// There is now less than maxObjects stored in round buffer
				break;
			}
			if (head<this.getMaxIndex())
			{
				DbBase d=this.getObjFromIndex(head);
				if (d!=null)
				{
					this.deleteObject(d);
				}
			}
			head=incAndWrap(head);
		}
		
		// Make sure tail position is really free (it usually will be)
		if (tail<this.getMaxIndex())
		{
			DbBase d=this.getObjFromIndex(tail);
			if (d!=null)
			{
				debug("tail position was occupied");
				this.deleteObject(d);
			}
		}

		// Add the new object
		EmpireOrder eo = new EmpireOrder();
		eo.setName("msg"+msgCount); // WordReader.getWord(str)
		eo.setOrder(/*""+msgCount+": "+*/str);
		eo.setIndex(tail); // want to add this in tail part of round buffer
		final int r = addObject(eo);
		if (r<0)
		{
			debug("failed to add "+eo.getName());
		}
		eo.setUpdateCounter(); // There is a problem with setting update counter, it does not work when addObject is done. When that is fixed this line can be removed. It is perhaps fixed now so this line can be removed?
	
		msgCount++;
		tail=incAndWrap(tail);
		this.setUpdateCounter();
	}

	
}
