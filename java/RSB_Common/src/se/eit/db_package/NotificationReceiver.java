package se.eit.db_package;

public interface NotificationReceiver {

	// subscribersRef (aka yourRef) is the reference number given by the subscriber when subscribing (if subscriber only subscribe on notifications from one class this is typically zero). So from perspective of receiving class your is my sort of.
	// sendersRef (aka myRef) is a reference number given by the class that generated the event. Typically an objId identifying a changed object. 
	public void notify(int subscribersRef, int sendersRef);
	
	// The object sending notifications is being unlinked (deleted).
	public void unlinkNotify(int subscribersRef);

}
