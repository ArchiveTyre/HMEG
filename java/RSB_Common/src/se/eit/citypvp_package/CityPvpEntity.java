package se.eit.citypvp_package;
import se.eit.db_package.*;
import se.eit.web_package.*;

public class CityPvpEntity extends DbThreadSafe {
	
	public int velocityX = 0;
	public int velocityY = 0;
	
	public int x = 0;
	public int y = 0;
	
	
	public int movePoint = 0; // Number indicator for how many ticks until entity shall move
	public int gravMovePoint = 0; // Number indicator for how many ticks left until next gravity.
	public int walkMovePoint = 0;
	public int health = 20;
	public int Oldx = 0;
	public int Oldy = 0;
	public int mass = 1;	 
	public int speedrecoil = 250;
	public int force = 3;
	// Entity i entity (item) comer att display'a
	public int state = 0; //Also known as entity id
	public int stack = 0;
	public int itemtype = 0;
	public int fill_wood = 0;
	public int fill_stone = 0;
	public int fill_mineral = 0;
	
	
	
	public CityPvpEntity(DbContainer parent, String name) 
	{
		super();
		linkSelf(parent);
		this.setName(name);
	}
	
	
	public CityPvpEntity() 
	{
		super();	
	}
	

	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		x = wr.readInt();
		y = wr.readInt();
		health = wr.readInt();
		itemtype = wr.readInt();
		stack = wr.readInt();
		state = wr.readInt();
	}
	
	
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(x);
		ww.writeInt(y);
		ww.writeInt(health);
		ww.writeInt(itemtype);
		ww.writeInt(stack);
		ww.writeInt(state);
	}
	
	// Denna metod anropas av game engine periodiskt
	public void tickEntityMs(long deltaMs)
	{

		
		if (movePoint<=100 || deltaMs+movePoint >= 100 && movePoint<=100)
		{
			movePoint+=deltaMs;
		}
		
		if (walkMovePoint<=100 || deltaMs+walkMovePoint >= 100 && walkMovePoint<=100)
		{
			walkMovePoint+=deltaMs;
		}
		
		if (velocityY <= 0 && (gravMovePoint<=100 || deltaMs+gravMovePoint >= 100 && gravMovePoint<=100))
		{
			gravMovePoint+=deltaMs;
		}
		
		//System.out.println(movePoint+" "+ velocityX+ " "+velocityY+ " grav: "+ gravMovePoint);
		DbBase currentRoom = this.getParent();
		if (currentRoom instanceof CityPvpRoom)
		{
			int airresictance = CityPvpBlock.getAirrecistance(((CityPvpRoom) currentRoom).getTile(x, y, 0));
			DbSubRoot ro=this.getDbSubRoot();
			try
			{
				ro.lockWrite(); 
				
				
				
				
				if (movePoint >= 100)
				{
					if (velocityX > 0)
					{
						force = mass * velocityX;
						if (move(1, 0))
						{
							
						}
						else
						{
							CityPvpBlock.neutralise(mass, velocityX);
						}
					}
					if (velocityX < 0)
					{
						force = mass * velocityX;
						if (move(-1, 0))
						{
							
						}
						else
						{
							CityPvpBlock.neutralise(mass, velocityX);
						}
					}
					
					if (velocityY > 0)
					{
						force = mass * velocityY;
						if (move(0, 1))
						{
							
						}
						else
						{
							CityPvpBlock.neutralise(mass, velocityY);
						}
					}
					if (velocityY < 0)
					{
						force = mass * velocityY;
						if (move(0, -1))
						{
							
						}
						else
						{
							CityPvpBlock.neutralise(mass, velocityY);
						}
					}	
				}
			}
			finally
			{
				ro.unlockWrite();
			}
			this.setUpdateCounter();
			velocityY = CityPvpBlock.neutralise(airresictance, velocityY);
			velocityX = CityPvpBlock.neutralise(airresictance, velocityX);
			movePoint-=movePoint; // Reset movePoint
		}
		// Initilise Gravity
		if (velocityY <=0 && gravMovePoint >= 100)
		{
			// TODO use proper method of gravity measurement.
			
			velocityY += 1;
			gravMovePoint -= gravMovePoint;
		}
	
	
	
	}
		
	
		/*//b+=deltaMs;
		if (b<75+1 || deltaMs+b > 75+1 && b<75+1)
		{
			b+=deltaMs;
		}
		//a+=deltaMs;
		if (a<75+1)
		{
			a+=deltaMs;
		}
		if (a>75)
		{	
			if (this.getContainingObj() instanceof CityPvpRoom)
			{
			CityPvpRoom cpr=(CityPvpRoom)this.getContainingObj();
			
			
			
			// NONE GRAVITY EFFECT BLOCKS
			if (cpr.map[x][y]==2)
			{
				return;
			}
			if (mass < -34 )
			{
				move(0,1, 1);
				a-=1000;
			}
			if (mass < 0)
			{
				return;
			}
			
			
			// DO GRAVITY
			move(0,CityPvpBlock.inBlockGravity(cpr.getTile(x, y, 0)), 1);
			a-=1000;
			
			}
		}*/
	public void walk(int direction)
	{
		/*
		  	These are the directions.
		  	___
		    |0| 
		   |1-2|
		    |3|
			---
		
			*/
		if (walkMovePoint>=100)
		{
			if (direction == 0 )
			{
				velocityY-=2;
				
			}
			else if (direction == 1)
			{
				velocityX-=1;
			}
			else if (direction == 2)
			{
				velocityX+=1;
			}
			else if (direction == 3)
			{
				velocityY+=1;
			}
			else
			{
				System.out.println(direction+" is not an valid direction, valid directions are 0 - 3");
			}
			walkMovePoint-=walkMovePoint; // Reset it
		}
		else
		{
			System.out.println("not enough walk move points to move: "+walkMovePoint );
		}
	}
	


	public void doMoveAnimation(DbSubRoot ro)
	{	
		ro.lockWrite();
		try {
		if (state == 0) {state=2;}
		else if (state == 1) {state=3;}
		// Reverse
		else if (state == 2) {state=0;}
		else if (state == 3) {state=1;}	
		}
		finally
		{
			ro.unlockWrite();
		}
	}
	// This is pretty self explanatory
	public boolean moveToRoom(int newX,int newY, CityPvpRoom to, int force)
	{
		// TODO: Dont do this, try find a door in the room we wish to enter.
		/*
		if (checkIfEmpty(newX, newY, to, force, true)==true)
		{
			this.moveToRoomThreadSafe(to);
			return true;
		}
		
		debug("could not enter room ~"+to.getId());
		return false;
		*/
		
		// TODO: Translate coordinates, but perhaps not here, we need to know if we are exiting or entering (that we don't know here).
		
		this.moveToRoomThreadSafe(to);
		this.x=newX;
		this.y=newY;
		this.setUpdateCounter();
		return true;
	}
	
	// Returns true if there is something there
	public boolean checkIfEntities(int newX, int newY, CityPvpRoom cpr, boolean exiting)
	{
		// Here we check if we bump into other entities
		DbBase list[] = cpr.getListOfSubObjectsThreadSafe();
		for(int i=0;i<list.length;i++)
		{
		
			if (list[i] instanceof CityPvpEntity)
			{
				CityPvpEntity cpe = (CityPvpEntity)list[i];

				if (cpe != this) // Don't check for collision with self
				{

					// Get the size of entity
					int eXSize=1;
					int eYSize=1;
					if(cpe instanceof CityPvpRoom)
					{
						CityPvpRoom e=(CityPvpRoom)cpe;
						eXSize=e.outerX;
						eYSize=e.outerY;
					}
					
				
					{
	                    // Check for collision with another entity
						if ( ((newX>=cpe.x) && (newX <(cpe.x+eXSize))) && ((newY>=cpe.y) && (newY<(cpe.y+eYSize))))
						{
							debug("collision detected ~"+this.getId()+" bumped into ~"+cpe.getId());
	
							
							// Check if room
							if (cpe.itemtype!=CityPvpBlock.doorIn)
							{
								
								// Move not possible, we can not move into or beside this entity
								debug("not a room that can be entered");
								return true;
							}
							else
							{
								
								
									if (exiting == false)
									{
										System.out.println("was true");
										//return true;
										
										// This entity was a room. We can move into it.
										debug("moveToRoom "+cpe.getId());
										//this.moveToRoomThreadSafe(cpe);
										//this.moveToRoom(cpe);
										((NotificationSender)getDbSubRoot()).notifySubscribers(-2);
										if (cpe == cpr)
										{
											error("the parent room can not be in itself");
										}
										else
										{
											if (cpe instanceof CityPvpRoom)
											{
												
												CityPvpRoom cpeRoom = (CityPvpRoom)cpe;
												
												int tmpX = cpeRoom.translateFromParentCoordinateX(newX);
												int tmpY = cpeRoom.translateFromParentCoordinateY(newX);
												
												this.moveToRoom(tmpX, tmpY, (CityPvpRoom) cpe, force);
											}
											else
											{
		
											}
										
										}
									}
									return true;
								}
								
							}
							
						}
				}
			}
		}
		return false;
	}
	
	
	
	public boolean doExit(int newX, int newY, CityPvpRoom cpr)
	{ 
		int beforeX = x;
		int beforeY = y;
		int tmpX = x;
		int tmpY = y;
		CityPvpRoom cprRoom = (CityPvpRoom) cpr;
		DbContainer pp=this.getParent().getParent();
		debug("move from ~"+cpr.getId()+ " to ~"+pp.getId());	
		// Check if after leaving object shall be to left, right, above or under
		//tmpX=cprRoom.x+beforeX/(cprRoom.xSectors/cprRoom.outerX);
		//tmpY=cprRoom.y+beforeY/(cprRoom.ySectors/cprRoom.outerY);
		tmpX=cprRoom.translateToParentCoordinateX(beforeX);
		tmpY=cprRoom.translateToParentCoordinateY(beforeY);
		
		
		// TODO: For now dont check destination.
		//if(checkIfEmpty(tmpX,tmpY,(CityPvpRoom)pp, force, true)==true)
		{
			
			System.out.println("moving to room ~"+pp.getId()+" from ~"+cpr.getId());
			//y = cprRoom.y;
			/*
			this.moveToRoomThreadSafe(pp);
			y = tmpY;
			x = tmpX;
			*/
			this.moveToRoom(tmpX, tmpY, (CityPvpRoom) pp, force);
			System.out.println("ExitPos "+ x +" "+ y);
			return true;
		}
		/*else
		{
			System.out.println("couldent move to room ~"+pp.getId()+" from ~"+getId()+ ", destination block is not empty");
			return false;
		}*/
	}
	
	// This method checks if there are blocks there.
	// Returns true if there is some block there
	public boolean checkIfWalkableBlock(int newX, int newY, CityPvpRoom cpr)
	{
		
		// 2 = ladder    walkable block [trancperant]
		if (CityPvpBlock.isWalkable(cpr.getTile(newX, newY, 0))==true)
		{ 
			if (cpr.getTile(newX, newY, 0) == CityPvpBlock.doorOut)
			{
				return doExit(newX, newY, cpr);
			}
			else
			{
				return true;
			}
		}
		else
		{
			return false;
		}
	}
	
	// Returns true if empty
	// Side effect: will move into rooms if possible.
	public boolean checkIfEmpty(int newX, int newY, CityPvpRoom cpr, int force, boolean exiting)
	{
		if (checkIfWalkableBlock(newX, newY, cpr)==true)
		{
			System.out.println("No block in the way for ~"+this.getId()+" in room ~"+cpr.getId());
			//return true;
		}
		else
		{
			// Try to break
			if (CityPvpBlock.recistance(cpr.getTile(newX,newY, 0), force)>=1)
			{
				cpr.changeTile(newX, newY, 0, 0);
				if (checkIfEmpty(newX, newY, cpr, 0, false))
				{
					//return 'nikolajisbest'
					System.out.println("could break.");
				}
				else
				{
					System.out.println("Couldent continue.");
					return false;
				}
			}
			return false;
		}
		
		
		// Quick and dirty, if the above code changed room, then don't do checkIfEntities on old room
		if(cpr!=this.getParent())
		{
			return false;	
		}
		
	  
	   // Check if bumping into other entities				
	   if (checkIfEntities(newX, newY, cpr, exiting)==true)
	   {
		   System.out.println("true");
		   return false;
	   }

	   return true;
	}
	
	public boolean move(int dx, int dy)
	{
	
		DbSubRoot ro=this.getDbSubRoot();

		ro.lockWrite();
		try
		{
			///
			
			
			DbBase p = this.getParent();
			
			if (p instanceof CityPvpRoom)
			{
	
				CityPvpRoom cpr=(CityPvpRoom)p;
				
				final int newX=x+dx;
				final int newY=y+dy;
				
		        // Here we check that we will not move outside current room
				if ( (newX<0) || (newX>=cpr.xSectors) || (newY<0) || (newY>=cpr.ySectors) )
				{
					return false;
				}
				/*	// check if tile is an good place to move to;
				if (checkIfBlocks(newX, newY, cpr)==true)
				{ 
					if (checkIfEntities(newX, newY, cpr, false))
					{
						//Possible
					}
					else
					{
						return;
					}
				*/	
			    // Check if not empty
				if (checkIfEmpty(newX, newY, cpr, force, false) == false)
				{
					// Fail
					return false;
				}
				else
				{
				//	finally
				//{
				//		ro.unlockRead();
				//	}
				
					if (cpr.map[x][y]!=2 && y+1<cpr.ySectors)
					{	
						if (cpr.map[x][y+1]==0 && newY<y)
						{
							if (mass < 0 )
							{
								
							}
							else
							{	
							return false;
							}
						}
					}
					
					// komihåg rutan den var i
					Oldx= x;
					Oldy= y;
					
					
					
					
					
					
					// DO zaaa animation.
					
					if (newX > x )
					{
					 if (state==2)
					 {
						state = 1; 
					 }
					
						
					}
					if (newX < x )
					{	
						if (state==1)
						 {
							state = 2; 
						 }
					}
					doMoveAnimation(ro);
					// Här flyttas denna entity
					x=newX;
					y=newY;
					
					
					return true;
					
					//// SPEED CONTROL

			
			
					// Uppdatera rutan den var i, och rutan den kommer till 
					//CityPvpRoom.updateTile(Oldx, Oldy)
					
					// Tala om för servers att något har blivit ändrat
			        //DbRoot dr = getDbRoot();
			        //NotificationSender ns = (NotificationSender)dr;
			       // ns.notifySubscribers(this.getId());
			      //  this.setUpdateCounter();	
				}
			}
		}
		finally
		{
			ro.unlockWrite();
		}
		this.setUpdateCounter();
		return true;

	}

	public CityPvpEntity findItem(int id)
	{
		if (this.listOfStoredObjects!=null)
		{
			for(DbStorable ds: this.listOfStoredObjects)
			{
				if (ds instanceof CityPvpEntity)
				{
					CityPvpEntity cpe=(CityPvpEntity)ds;
					if (cpe.itemtype==id)
					{
						return cpe;
					}
				}
			}
		}
		return null;
	}

	
	public int haveItem(int id)
	{
		int count=0;
		for(DbStorable ds: this.listOfStoredObjects)
		{
			if (ds instanceof CityPvpEntity)
			{
				CityPvpEntity cpe=(CityPvpEntity)ds;
				if (cpe.itemtype==id)
				{
					count += cpe.stack ;
				}
			}
		}
		return count;
	}
	
	
//	en natt natt natt min båt jag styrde
	public boolean giveItem(int itemtype , int count)
	{
		debugWriteLock();
		
		
		CityPvpEntity cpe = this.findItem(itemtype);
		
		if (cpe!=null)
		{
			// 0 <= -9
			if (cpe.stack<=count)
			{
				System.out.println("couldent remove items");
				return false;
			}
			else
			{
				cpe.stack+=count;
				cpe.setUpdateCounter();
				return true;
			}
			
		}
		else
		{	
			// Create a new object
			if (count <= 0)
			{
				System.out.println("err");
				return false;
			}
			cpe = new CityPvpEntity ();
			cpe.setName("i"+itemtype); // All objects shall have some name in RSB
			cpe.itemtype = itemtype;
			cpe.stack = count;
			
			cpe.linkSelf(this);			
	
			cpe.setUpdateCounter(); // should not be needed here.

			// assign coordinates to the new object.
			// Objects in the inventory are placed from upper left according to index
			if (this instanceof CityPvpRoom)
			{
				CityPvpRoom cpr=(CityPvpRoom)this;
				cpr.x = cpe.getIndex()%cpr.xSectors;
				cpr.y = cpe.getIndex()/cpr.xSectors;
			}
			return true;
		}
	}
}
