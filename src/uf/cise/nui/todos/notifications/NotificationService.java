package uf.cise.nui.todos.notifications;

import uf.cise.nui.todos.MediaPlayerHandler;
import uf.cise.nui.todos.R;
import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.data.Task;
import uf.cise.nui.todos.data.TaskList;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;

/** 
 * Service that creates notifications
 */
public class NotificationService extends IntentService
{
	private int vibrationTime = 500;
	private int taskId;
	private TaskList taskListModel;
	private String alarmType;

	public NotificationService()
	{
		super("NotificationService");
	}

	@Override
	public void onHandleIntent(Intent intent)
	{
		taskListModel = TaskList.getSingletonObject(this);
		// Vibrate the mobile phone
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		//vibrator.vibrate(vibrationTime);
		vibrator.vibrate(vibrationTime);
		taskListModel.getDataBase().open();

		String[] tokens = intent.getStringExtra("description").split(",");
		taskId = Integer.parseInt(tokens[0]);
		String title = tokens[1];
		String windowTitle = null;
		String toSend = null;
		Task triggeredTask = null;
		boolean alarmRepeating = true;

		// check if alert was triggered by the location alert
		if (title.equals("GPS location nearby !"))
		{
			alarmType = "GPS";
			alarmRepeating = false;
			taskId = intent.getIntExtra("Id", 0);
			windowTitle = "GPS location nearby !";
			toSend = "Title: " + tokens[3] + "\n\nLocation: " + tokens[2];
			triggeredTask = Utils.findTaskById(this,taskId);

			if (triggeredTask != null)
			{
				// remove the alarm
				new TaskAlarms(this).cancelProximityAlert(taskId, triggeredTask.getTaskTitle());
				taskListModel.getDataBase().disableTaskLocationAlert(taskId);
			}
		}
		else // triggered by time alert
		{
			alarmType = "Time";
			windowTitle = "Notification";
			toSend = "Title: " + title + "\n\nDescription: " + tokens[2];
			triggeredTask = Utils.findTaskById(this,taskId);

			if (triggeredTask != null)
			{
				String taskNotification = triggeredTask.getDate();
				// checks if the alarm has an interval
				if (! taskNotification.contains("("))
				{
					alarmRepeating = false;
					new TaskAlarms(this).cancelAlarm(taskId, triggeredTask.getTaskTitle());
					taskListModel.getDataBase().disableTaskNotification(taskId);
				}
			}
		}

		// Prepare intent which is triggered if the
		// notification is selected
		Intent myIntent = new Intent(this, NotificationReceiverActivity.class);
		myIntent.putExtra("windowTitle", windowTitle);
		myIntent.putExtra("taskContent", toSend);
		PendingIntent pIntent = PendingIntent.getActivity(this, taskId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Build notification
		Notification noti = new Notification.Builder(this)
		.setContentTitle(title)
		.setContentText(tokens[2]).setSmallIcon(R.drawable.app_icon)
		.setContentIntent(pIntent).build();

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		noti.flags |= Notification.FLAG_SHOW_LIGHTS;
		noti.ledARGB = 0xff00ffff;
		noti.ledOnMS = 300;
		noti.ledOffMS = 1000;
		
		if (Utils.IS_DEFAULT_SOUND)
		{
			//To play the default sound with your notification:
			noti.defaults |= Notification.DEFAULT_SOUND;		
		}
		else
		{
			new MediaPlayerHandler(this).playAudio(Utils.LOUD_NOTIFICATION__SOUND);
		}
		notificationManager.notify(taskId, noti);
		
		if (! alarmRepeating)
		{
			// brodcast a message to main activity
			messageToActivity();
		}
	}

	private void messageToActivity()
	{
		Intent intent = new Intent("dataSetChanged");
		sendLocationBroadcast(intent);
	}

	private void sendLocationBroadcast(Intent intent)
	{
		intent.putExtra("id", taskId);
		intent.putExtra("alarmType", alarmType);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}