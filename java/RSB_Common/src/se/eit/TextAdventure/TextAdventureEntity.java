package se.eit.TextAdventure;
import se.eit.db_package.*;
import se.eit.rsb_package.*;


public class TextAdventureEntity extends GameBase {

	public TextAdventureEntity(DbBase parent, String name) 
	{
		super();
		parent.addObject(this);
		this.setName(name);

	    // TODO: vad mer behövs här?	
		
		
	}
	int HP;
	String E_helmnet;
	String E_chestplate;
	String E_leggings;
	String E_shoes;
	String E_cape;
	String A_first;
	String A_second;
	
}
