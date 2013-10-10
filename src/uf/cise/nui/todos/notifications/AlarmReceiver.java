package uf.cise.nui.todos.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 
 * Listens to notification and proximity alerts
 */
public class AlarmReceiver extends BroadcastReceiver
{	
	Intent myIntent = null;

	public void onReceive(Context context, Intent intent)
	{
		// check for a message from GPS alert
		String locationMessage = intent.getStringExtra("GPS_message");
		myIntent = new Intent(context, NotificationService.class); 
		
		// message was null, it wasn't triggered from the GPS
		if (locationMessage == null)
		{
			myIntent.putExtra("description", intent.getStringExtra("alarm_message"));
		}
		else 
		{
			myIntent.putExtra("description", "0,GPS location nearby !" + "," + locationMessage);
			myIntent.putExtra("Id", intent.getIntExtra("Id", 0));
		}
		
		context.startService(myIntent);
	} 
}

