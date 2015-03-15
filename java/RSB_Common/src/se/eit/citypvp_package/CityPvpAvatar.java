
package se.eit.citypvp_package;
import se.eit.db_package.*;
import se.eit.rsb_package.RsbRoundBuffer;

public class CityPvpAvatar extends CityPvpEntity {

	
	RsbRoundBuffer rsbRoundBuffer=null;
	
	public CityPvpAvatar(DbBase parent, String name) 
	{
		super(parent, name);	

	    // TODO: vad mer behövs här?
		// Structur.
		itemtype = CityPvpBlock.avatarFigure;
		
	}


	public CityPvpAvatar() 
	{
		super();	

	}

	
	
	// Find or create the message buffer
	public RsbRoundBuffer findRsbRoundBuffer()
	{
		if (rsbRoundBuffer!=null)
		{
			return rsbRoundBuffer;
		}
		
		int n=this.getListCapacity();
		for(int i=0;i<n;i++)
		{
			DbBase b=this.getObjFromIndex(i);
			if (b instanceof RsbRoundBuffer)
			{
				rsbRoundBuffer=(RsbRoundBuffer)b;
				return rsbRoundBuffer;
			}
		}
		rsbRoundBuffer = new RsbRoundBuffer(this, "erb"+this.getIndex());
		//this.addObject(erb);
		//erb.setUpdateCounter(); // There is a problem with setting update counter, it does not work when addObject is done. When that is fixed this line can be removed.
		return rsbRoundBuffer;
	}
	
	
	public void postMessage(String str)
	{
		debugWriteLock();

		// find the round buffer and post message to it
		RsbRoundBuffer erb = findMsgRoundBuffer();
		erb.postMessage(str);
		setUpdateCounter();
	}

	// Find or create the message buffer
	public RsbRoundBuffer findMsgRoundBuffer()
	{
		if (rsbRoundBuffer!=null)
		{
			return rsbRoundBuffer;
		}
		
		int n=this.getListCapacity();
		for(int i=0;i<n;i++)
		{
			DbBase b=this.getObjFromIndex(i);
			if (b instanceof RsbRoundBuffer)
			{
				rsbRoundBuffer=(RsbRoundBuffer)b;
				return rsbRoundBuffer;
			}
		}
		rsbRoundBuffer = new RsbRoundBuffer(this, "roundBuffer");
		//this.addObject(erb);
		//erb.setUpdateCounter(); // There is a problem with setting update counter, it does not work when addObject is done. When that is fixed this line can be removed.
		return rsbRoundBuffer;
	}
	
	
}
