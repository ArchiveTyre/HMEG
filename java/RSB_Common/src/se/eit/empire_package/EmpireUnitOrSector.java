package se.eit.empire_package;

import se.eit.web_package.WordReader;
import se.eit.web_package.WordWriter;

public class EmpireUnitOrSector extends EmpireBase {

	static final int NO_OWNER=-1;
	static final int CONTESTED_PROPERTY=-2;

	
	int owner=NO_OWNER; // we shall probably make an EmpireProperty class since more than EmpireUnits can be owned

	
	
	@Override
	public void readSelf(WordReader wr)	
	{
		super.readSelf(wr);
		owner=wr.readInt();
	}

	@Override
	public void writeSelf(WordWriter ww)
	{		
		super.writeSelf(ww);
		ww.writeInt(owner);
	}	
	
	@Override
	public void listInfo(WordWriter pw, String prefix)
	{
		super.listInfo(pw, prefix);					
		pw.println(prefix+"owner "+owner);		
	}

	@Override
	public int setInfo(WordReader wr, String infoName)
	{
		if (infoName.equals("owner"))
		{
			owner=wr.readInt();
			return 1;
		}
		else
		{
			return super.setInfo(wr, infoName);
		}
	}
		
	// This is to be overridden in EmpireUnit and EmpireSector
	public int getPosRecursive()
	{
		return -1;
	}

	
	public void setOwner(int newOwner)
	{
		// has owner changed?
		if (owner!=newOwner)
		{
			owner=newOwner;
			/*if (owner<0)
			{
				final EmpireWorld ew=getEmpireWorld();
				ew.setTickCleanupCallback(this);
			}*/
			setUpdateCounter();
		}
	}

	public int getOwner()
	{
		return owner;
	}

    public void tellOwner(String str)
    {
		final EmpireWorld ew=getEmpireWorld();
		final EmpireStatesList enl = ew.getEmpireNationsList();
		final EmpireState en=enl.getEmpireNation(owner);
		if (en!=null)
		{
			en.postMessage("#"+this.getId()+" "+this.getName()+" "+str);
		}
		setUpdateCounter();					
    }

    /*
	public boolean ownOrClaimSector(int owner)
	{
		if (this.owner==owner)
		{
			// This state already own this sector or unit
			return true;
		}
		
		if (this.owner==EmpireUnitOrSector.NO_OWNER)
		{
			// The sector is not claimed by anyone, will claim it now
			setOwner(this.owner);
			return true;
		}
		
		// Someone else own this sector or unit
		setOwner(CONTESTED_PROPERTY);
		
		return false;
	}

	@Override
	public void gameTickCleanup()
	{
		if (this.owner==CONTESTED_PROPERTY)
		{
			setOwner(NO_OWNER);
		}	
	}
*/
}
