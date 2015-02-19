package se.eit.db_package;

import java.util.Iterator;

// TODO: Perhaps DbTickReceiver and NotificationSender should swap places in the hierarchy. And if last NotificationReceiver disconnects then ticks are canceled.

public class NotificationSender extends DbIdList {

	class NotificationData
	{
		int ref=0;
		NotificationReceiver rcv=null;

		NotificationData(NotificationReceiver rcv, int ref)
		{
			this.rcv=rcv;
			this.ref=ref;
		}
		
		public void doNotify(int idOfSenderOrEventId)
		{
			rcv.notify(ref, idOfSenderOrEventId);
		}
	}
	
	public DbList<NotificationData> notificationDataList=new DbList<NotificationData>();;

	
	// http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java		
	public static String className()
	{	
		return NotificationSender.class.getSimpleName();	
	}


	public NotificationSender()
	{	
		super();
	}

	// Objects that need notification when something is changed in the database shall call this method to register a subscription to notifications.
	// Parameters:
	// notificationReceiver the object to be notified when something has been changed.
	// notificationReference an optional reference, it can be used by caller to keep track of the notifications it receives, it is only needed if caller has more than one subscription, set to zero if no needed.
	// Returns an index to use when removing the notification request.
	public int addNotificationReceiver(NotificationReceiver notificationReceiver, int notificationReference)
	{
		NotificationData nd = new NotificationData(notificationReceiver, notificationReference);
		return notificationDataList.add(nd);
	}

	public void removeNotificationReceiver(int index)
	{
		notificationDataList.remove(index);
	}

	// Call this if notifications are no longer wanted.
	public int removeNotificationReceiver(NotificationReceiver notificationReceiver)
	{
		Iterator<NotificationData> i=notificationDataList.iterator();
		
		while (i.hasNext())
		{
			NotificationData nd = i.next();
		
			if (nd.rcv==notificationReceiver)
			{
				i.remove();
			}
		}
		
		return 0;
	}

	// Call this to tell subscribers that something has happened.
	public void notifySubscribers(int idOfSenderOrEventId)
	{
		for (NotificationData nd : notificationDataList)
		{
			nd.doNotify(idOfSenderOrEventId);
		}
	}
	
	
	@Override
	public void unlinkSelf()
	{
		notifySubscribers(-1);
		notificationDataList.clear();
		super.unlinkSelf();
	}
	
}
