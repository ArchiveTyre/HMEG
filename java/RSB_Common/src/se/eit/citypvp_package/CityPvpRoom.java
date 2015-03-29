package se.eit.citypvp_package;

//import java.awt.Graphics;
import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;

//import javax.imageio.ImageIO;

import se.eit.db_package.*;
//import se.eit.rsb_srv_main_pkg.GlobalConfig;
import se.eit.web_package.*;


public class CityPvpRoom extends CityPvpEntity{
	
	
	public CityPvpRoom doors[] = new CityPvpRoom[6];
	public String door_name[] = new String[6];
	
	// Inner size of room
	public int xSectors=32;
	public int ySectors=16;
	
	// Outer size of room
	public int outerX=2; // TODO: Rename to outerSizeX
	public int outerY=1; // TODO: Rename to outerSizeY
	
	
	
	public long [][] map = new long[xSectors][ySectors];
	
	public BufferedImage image=null; /*ImageComponent2D*/
	public WebFileData fileData=null;
	
	
	// Size of each block in this room
	public final int gridSizeX=16; 
	public final int gridSizeY=16;

	
	public CityPvpRoom(DbBase parent, String name) 
	{
		super(parent, name);	

	    // TODO: vad mer behövs här?	
		
		
	}
	
	public CityPvpRoom()
	{	
		super();
	}

	
	
	@Override
	public void readSelf(WordReader wr)	
	{		
		super.readSelf(wr);
		outerX=wr.readInt();
		outerY=wr.readInt();
		xSectors=wr.readInt();
		ySectors=wr.readInt();
		for(int x=0;x<xSectors;x++)
		{	
			for(int y=0;y<ySectors;y++)
			{
				map[x][y] = wr.readLong();
			}
		}
		image=null;
	}
	
	
	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(outerX);
		ww.writeInt(outerY);
		ww.writeInt(xSectors);
		ww.writeInt(ySectors);
		for(int x=0;x<xSectors;x++)
		{	
			for(int y=0;y<ySectors;y++)
			{
				ww.writeLong(map[x][y]);
			}
		}		
	}
	public int getTile (int x, int y, int argument)
	{
		if ( ((x<xSectors) && (y<ySectors)) && ((x>-1)&&(y>-1)) ) 
        {
		  // Decomplie Block into small ints
	      long a = map[x][y];
	      int id = (int) (a & 0xFF);
	      a = a >> 8;
	      int damage = (int) (a & 0xFFFF);
	      a = a  >> 16;
	      int rotate = (int) (a & 0x3);
	      a = a >> 2;
	      if (argument == 0)
	      {
	    	  return id;
	      }
	      else if (argument == 1)
	      {
	    	  return damage;
	      }
	      else if (argument == 2)
	      {
	    	  return rotate;
	      }
        }
		return -1;
	}
	public int getMass ()
	{
		int m = 0;
		for (int x = 0; x < xSectors; x++)
		{
		
			for (int y = 0; y < ySectors; y++)
			{
			/// TODO	m += CityPvpBlock.blockmass
			if (this.map[x][y] > 0)
			{
				m +=1;
			}
			if (this.map [x] [y] == CityPvpBlock.ballon)
			{
				m = m -2;
			}
		}

		}
	return m ;
	}
	
	public void changeTile(int x, int y, int argument, int index)
	{
        if ( ((x<xSectors) && (y<ySectors)) && ((x>-1)&&(y>-1)) ) 
        {
	        long a = map[x][y];
	        
	        switch(argument)
	        {
		        case 0:
		        {
		        	final long m = 0xFF;
		        	a &= ~m;
		        	a |= (index & m);
	        		break;
		        }
		        case 1:
	        	{
	        		final long m = 0xFFFF;
	        		index &= m;
	        		a &= ~(m<<8);
	        		a |= index<<8;
	        		break;
	        	}
		        case 2:
		        {
	        		final long m = 0x3;
	        		index &= m;
	        		a &= ~(m<<24);
	        		a |= index<<24;
	        		break;
		        }
	        	default:
	        		System.out.println("Invalid argument");
	        		break;
	        }
	        
	        
	        
        	map[x][y] = a;
        
        }

        
        /*DbRoot dr = getDbRoot();
        NotificationSender ns = (NotificationSender)dr;
        ns.notifySubscribers(this.getId());*/
        mass = getMass();
     	this.setUpdateCounter();

        image=null;   
        
	}
	/*
	public int list(PlayerCommandInterpreter pci)
	{
	
	    // list doors
	    listDoors();

	    // list objects in this room
	    listObjects();
	}
	*/
	
	/*
	public int listDoors(PlayerCommandInterpreter pci)
	{
	
	    // list doors
		int n=0;
		pci.println("From this room you can go:");
		for(int i=0;i<6;i++)
		{		
			if (doors[i]!=null)
			{
				pci.println("  "+ door_name[i] + " to " + doors[i].name);
				n++;
			}
		}	
	    if (n==0) 
	    {
	    	pci.println("nowhere");
	    }
	}

	public int listObjects(PlayerCommandInterpreter pci)
	{
	
	    // list doors
	    n=0;
	    pci.println("In this room there is:");
		for(int i=0;i<64;i++)
		{
			if (listOfsubGameObj[i]!=null)
			{
				pci.println("  "+ listOfsubGameObj[i].name);
				n++;
			}
		}
	    if (n==0) 
	    {
	    	pci.println("nothing");
	    }
		return n;
	}
	*/
	
	
	public int connect(CityPvpRoom other_room, String door_name)
	{
		for(int i=0;i<6;i++)
		{
			if (doors[i]==null)
			{
				doors[i]= other_room;
				this.door_name[i] = door_name;
				return 0;
			}
		}
		System.out.println("Room is full.");
		return -1;		
	}
	
	
	public final CityPvpRoom findDoor(String name)
	{
		for(int i=0;i<6;i++)
		{
			if (doors[i]!=null)
			{
				if (this.door_name[i].equals(name))
				{
				    return doors[i];
				}
			}
		}
		return null;
	}
	
	
	public int translateLocalGridToPixelX(int gridPosX)
	{
		return gridPosX*16;
	}

	public int translateLocalGridToPixelY(int gridPosY)
	{
		return gridPosY*16;
	}
	
	
	/*public BufferedImage remakeImage(GlobalConfig config)
	{
		final int imageSizeX=xSectors*gridSizeX;
		final int imageSizeY=ySectors*gridSizeY;
		
		BufferedImage image = new BufferedImage(imageSizeX, imageSizeY, BufferedImage.TYPE_4BYTE_ABGR); // Or is TYPE_INT_ARGB better?
		
		// http://stackoverflow.com/questions/2318020/merging-two-images
		Graphics g = image.getGraphics();

		for(int x=0;x<xSectors;x++)
		{	
			for(int y=0;y<ySectors;y++)
			{
				int sx=translateLocalGridToPixelX(x);
				int sy=translateLocalGridToPixelY(y);
				//if ((sx>=0) && (sx<imageSizeX) && (sy>=0) && (sy<imageSizeY))
				{				
					final int blockId=map[x][y]; 
					if (blockId!=0)
					{
						//String blockImageName=CityPvpBlock.getBlockTexture(blockId);
						//BufferedImage blockImage=null;
						//File file=null;
						try
						{
							final String filename=config.httpRootDir+"/"+blockImageName+".png";
							file=new File(filename);
							blockImage=ImageIO.read(file);
							
							
							g.drawImage(blockImage, sx, sy, gridSizeX, gridSizeY, null);
							
							
						}
						catch (IOException e)
						{
							WordWriter.safeError("failed to read image "+file.getAbsolutePath());
						}

						//stc.writeLine("AddImg "+CityPvpBlock.getBlockTexture(blockId)+" "+sx*xSize+" "+sy*ySize+" "+xSize+" "+ ySize);
						
					}
				}
			}
		}	
		
		/*
		// Här skriver man ut entitys
		DbBase[] list = this.getListOfSubObjectsThreadSafe();
		
		for(int i=0;i<list.length;i++)
		{
			if (list[i] instanceof CityPvpEntity)
			{
				CityPvpEntity e = (CityPvpEntity)list[i];
				
				int sx=e.x;
				int sy=e.y;
				
				if ((sx>=0) && (sx<xSectors) && (sy>=0) && (sy<ySectors))
				{
				
					int px=translateLocalGridToPixelX(sx);
					int py=translateLocalGridToPixelY(sy);


					int eXSize=1;
					int eYSize=1;
					
					String entityImageName = getEntityTypeName(e.itemtype);
					file=new File("/tmp/"+entityImageName+".png");
					blockImage=ImageIO.read(file);

					g.drawImage(entityImage, sx, sy.null);
					y++;
					stc.writeLine("AddImg "+getEntityTypeName(e.itemtype) + " "+px+" "+py+" "+gridSizeX*eXSize+" "+ gridSizeY*eYSize);

					// TODO  Display data of cpm:
					
					
					
 				    // mark this position on screen as needing update.
					//map[sx][sy]=-1;
				}
			}			
			
		}
		
		

		
		return image;
	}
    */
	public String getImageName()
	{
		//final String n="tmp"+this.getIndexPath("/");
        DbRoot dr = getDbRoot();
		final String n="tmp"+dr.getIndexPath("/")+"/~"+this.getId();
		return n;
	}
	
	/*public String prepareImageAndGetName(GlobalConfig config)
	{
		getImage(config);
		return getImageName();
	}
	*(
	
	/*public BufferedImage getImage(GlobalConfig config)
	{
		if (image==null)
		{
			image=remakeImage(config);
			fileData=null;
		}
		return image;
	}
	*(
	/*public WebFileData getFile(GlobalConfig config)
	{		
		if (image==null)
		{
			image=remakeImage(config);
			fileData=null;
		}
		
		if (fileData==null)
		{
			if (image!=null)
			{
				debug("getFile");
				fileData = new WebFileData();

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        try {
					ImageIO.write(image, "png", baos);
			        baos.flush();
			        fileData.data = baos.toByteArray();
					fileData.lastModified = System.currentTimeMillis();
					fileData.maxTimeS=1;
			        baos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
		}
		return fileData;
	}
	*/	
	
	public boolean isControlPanel(int x, int y)
	{
		
	   if (map[x][y]==CityPvpBlock.controlPanel)
	   {
		   return true;
	   }
	   return false;
	}
	
	// Translates coordinates within this room to the coordinates of the room that this room is in.
	public int translateToParentCoordinateX(int beforeX)
	{	
		int newX = this.x+beforeX/(this.xSectors/this.outerX);

		CityPvpRoom p = (CityPvpRoom)this.getParent();
		
		if (newX<0)
		{
			newX=0;
		}
		if (newX>=p.xSectors)
		{
			newX=p.xSectors-1;
		}
		
		return newX; 
	}

	public int translateToParentCoordinateY(int beforeY)
	{
		int newY = this.y+beforeY/(this.ySectors/this.outerY);

		CityPvpRoom p = (CityPvpRoom)this.getParent();
		
		if (newY<0)
		{
			newY=0;
		}
		if (newY>=p.ySectors)
		{
			newY=p.ySectors-1;
		}
		
		return newY;
	}
	

	// Translates from the coordinates of the room that this room is in to coordinates of this room.
	public int translateFromParentCoordinateX(int beforeX)
	{
		int newX=(beforeX-this.x) * (this.xSectors/this.outerX);
		if (newX<0)
		{
			newX=0;
		}
		if (newX>=this.xSectors)
		{
			newX=this.xSectors-1;
		}
		return newX;
		
	}

	
	public int translateFromParentCoordinateY(int beforeY)
	{
		int newY=(beforeY-this.y) * (this.ySectors/this.outerY);
		if (newY<0)
		{
			newY=0;
		}
		if (newY>=this.ySectors)
		{
			newY=this.ySectors-1;
		}
		return newY;
	}
	
}
