package uf.cise.nui.todos.notifications;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.data.Task;
import uf.cise.nui.todos.data.TaskList;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.LocationManager;

/**
 * Manages task alarms and proximity alerts
 */
public class TaskAlarms
{
	private Context context;
	private LocationManager locationManager; 
	private TaskList taskListModel;

	public TaskAlarms(Context context)
	{
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		taskListModel = TaskList.getSingletonObject(context);
	}

	/** 
	 * Set an alarm notification for a specific task
	 * 
	 * @param repeatAlarmInterval
	 * @param calendar - alarm time
	 */
	public void setAlarm(long repeatAlarmInterval, Calendar calendar, int position)
	{
		long taskId = taskListModel.getTaskAt(position).getId();
		String taskTitle = taskListModel.getTaskAt(position).getTaskTitle();
		String taskDescription = taskListModel.getTaskAt(position).getTaskDescription();

		// This is the intent that is launched when the alarm goes off.
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		// send an extra message with the task id and description for notification details
		alarmIntent.putExtra("alarm_message", Long.toString(taskId) + "," + taskTitle + "," + taskDescription);
		PendingIntent sender = PendingIntent.getBroadcast(context, (int) taskId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		// set the time and interval for when the alarm will go off
		if (repeatAlarmInterval != -1)
		{
			am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), repeatAlarmInterval, sender);
		}
		else
		{
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
		}
		Utils.makeToast(context, "Alarm scheduled");
	}

	/** 
	 * Set a GPS proximity alert for the task
	 * 
	 * @param selectedLocation - user location choice from the UI
	 */
	public void setProximityAlert(Address selectedLocation, int position)
	{
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.putExtra("GPS_message", selectedLocation.getAddressLine(0) + " " + 
				selectedLocation.getAddressLine(1) + "," + taskListModel.getTaskAt(position).getTaskTitle());
		alarmIntent.putExtra("Id", (int) taskListModel.getTaskAt(position).getId());

		PendingIntent sender = PendingIntent.getBroadcast(context,
				(int) taskListModel.getTaskAt(position).getId(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		locationManager.addProximityAlert(selectedLocation.getLatitude(),
				selectedLocation.getLongitude(),Utils.SEARCH_RADIUS,-1,sender); 
	}

	/** 
	 * Given a task id and title, cancels a previously set alarm
	 * 
	 * @param taskId
	 * @param taskTitle
	 */
	public void cancelAlarm(long taskId, String taskTitle)
	{
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.putExtra("alarm_message", Long.toString(taskId) + "," + taskTitle);
		PendingIntent sender = PendingIntent.getBroadcast(context, (int) taskId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}

	/** 
	 * Cancels previously set GPS proximity alert 
	 * 
	 * @param taskId
	 * @param taskTitle
	 */
	public void cancelProximityAlert(long taskId, String taskTitle)
	{
		Intent alarmIntent = new Intent(context, AlarmReceiver.class);
		alarmIntent.putExtra("GPS_message", taskTitle);

		PendingIntent sender = 
				PendingIntent.getBroadcast(context, (int) taskId,
						alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		locationManager.removeProximityAlert(sender);
	}

	/** 
	 * Disable previously set alarms if any
	 * 
	 * @param taskToUndo
	 */ 
	public void disableTaskAlerts(Task taskToUndo)
	{
		if (! taskToUndo.getDate().equals(Utils.DEFUALT_NOTIFICATION))
		{
			cancelAlarm(taskToUndo.getId(),taskToUndo.getTaskTitle());
		}
		if (! taskToUndo.getLocation().equals(Utils.DEFUALT_LOCATION))
		{
			cancelProximityAlert(taskToUndo.getId(),taskToUndo.getTaskTitle());
		}
	}	

	/** 
	 * After given best locations match from Geocoder,
	 * display them to the user to choice and generate
	 * the proximity alert.
	 * 
	 * @param locations - Geocoder returned locations list
	 */ 
	public void createLocationChoiceAlert(final List<Address> locations, final int position)
	{
		List<String> temp = new ArrayList<String>();
		String buffer = null;

		for (int i=0; i < locations.size(); i++)
		{
			if (locations.get(i).getCountryName() != null)
			{
				buffer = locations.get(i).getCountryName() + ", ";
			}
			if (locations.get(i).getLocality() != null)
			{
				buffer += locations.get(i).getLocality() + ", ";
			}
			if (locations.get(i).getAddressLine(0) != null)
			{
				buffer += locations.get(i).getAddressLine(0);
			}
			
			temp.add(buffer);
		}

		// create an auxiliary char sequence to send to the choice alert dialog
		final CharSequence[] items = temp.toArray(new CharSequence[temp.size()]);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		// set title
		alertDialogBuilder.setTitle("Choice Best GPS Location");
		// set dialog message
		alertDialogBuilder
		.setItems(items, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				Address selectedLocation = locations.get(which);

				// set new task location
				taskListModel.getTaskAt(position).setLocation(items[which].toString());
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
				// set the proximity alert
				setProximityAlert(selectedLocation,position);
			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				dialog.cancel();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}