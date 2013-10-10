package uf.cise.nui.todos.view;


import java.util.Calendar;

import uf.cise.nui.todos.MediaPlayerHandler;
import uf.cise.nui.todos.R;
import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.adapter.ItemListBaseAdapter;
import uf.cise.nui.todos.data.Task;
import uf.cise.nui.todos.data.TaskList;
import uf.cise.nui.todos.gestureListeners.ShakeEventListener;
import uf.cise.nui.todos.gestureListeners.SwipeListViewTouchListener;
import uf.cise.nui.todos.notifications.TaskAlarms;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/** 
 * Main application activity 
 */
public class MainActivity extends Activity 
	implements OnClickListener,  OnTouchListener,
	ItemListBaseAdapter.NoticeAlarmListener, AlarmAlertDialog.SetAlarmListener
{
	// flag to determine if shake gestures are allowed
	public static boolean		shakeGestures;
	private ExpandableListView 	expandableListView;
	// instance of the singleton class
	private TaskList			taskListModel;
	private EditText 			titleTextField;
	// popup window object for setting task notification alarm
	private AlarmAlertDialog 	notificationPopup;
	private AlertDialogs		alertDialog;
	// holds current list view item position
	private int 				currentPosition;
	// before task deletion, saves it in case of undone
	private Task				taskToUndo;
	// for undo button purposes
	private View 				viewContainer;
	// used for google analytics purposes
	private Tracker 			myTracker;
	// used for google analytics purposes
	private GoogleAnalytics 	myInstance; 
	// the user entered text in alert dialog
	private RelativeLayout 		mainLayout;
	private SensorManager 		mSensorManager;
	private ShakeEventListener 	mSensorListener;
	// manages sounds in the application
	private MediaPlayerHandler 	mediaPlayer;
	private TaskAlarms			taskAlarms;
	public static ImageView		noTasksImage;
	// custom base adapter
	private static ItemListBaseAdapter	adapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// disable the window title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		taskListModel = TaskList.getSingletonObject(this);
		mediaPlayer = new MediaPlayerHandler(this);
		shakeGestures = true;

		expandableListView = (ExpandableListView) findViewById(R.id.listV_main);
		adapter = new ItemListBaseAdapter(this);
		// set our custom adapter for the listView
		expandableListView.setAdapter(adapter);

		titleTextField = (EditText) findViewById(R.id.edit_message);
		Button addButton = (Button) findViewById(R.id.btnAdd);
		addButton.setOnClickListener(this);
		mainLayout = (RelativeLayout) findViewById(R.id.layout_main);
		mainLayout.setOnTouchListener(this);
		viewContainer = findViewById(R.id.undobar);		
		myInstance = GoogleAnalytics.getInstance(getApplicationContext());
		taskToUndo = new Task();
		noTasksImage = (ImageView) findViewById(R.id.no_tasks_image);
		noTasksImage.setVisibility(View.GONE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new ShakeEventListener();

		// Placeholder tracking ID.
		myTracker = myInstance.getTracker(Utils.GOOGLE_ANALYTICS_CODE);
		// Set newTracker as the default tracker globally.
		myInstance.setDefaultTracker(myTracker); 
		// prints actions to the logcat
		myInstance.setDebug(true);
		taskListModel.getDataBase().open();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, new IntentFilter("dataSetChanged"));

		mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() 
		{
			public void onShake() 
			{
				if (shakeGestures == true)
				{
					shakeGestures = false;
					alertDialog = new AlertDialogs();
					alertDialog.setArguments(getArguments(Utils.DIALOG_YES_NO_MESSAGE,currentPosition,Utils.DELETE_ALL_ALERT));
					alertDialog.show(getFragmentManager(),"ShowAlertDialog");
					adapter.notifyDataSetChanged();
				}
			}
		});
		// Create a ListView-specific touch listener.
		SwipeListViewTouchListener touchListener = 
				new SwipeListViewTouchListener(expandableListView, new SwipeListViewTouchListener.OnSwipeCallback()
				{
					// when the user swipes a list view item
					@Override
					public void onSwipeLeft(ListView listView, int[] reverseSortedPositions)
					{
						// Intentionally empty
					}
					@Override
					public void onSwipeRight(ListView listView, int[] reverseSortedPositions)
					{
						for (int position : reverseSortedPositions)
						{
							try
							{
								taskToUndo = taskListModel.getTaskAt(position);
								taskListModel.removeTask(position);
								taskAlarms.disableTaskAlerts(taskToUndo);
								mediaPlayer.playAudio(Utils.DELETE_SOUND); 
								adapter.notifyDataSetChanged();
								showUndo(viewContainer);
							}
							catch (IndexOutOfBoundsException e)
							{
								// intentionally empty
							}
						}
					}
				});

		expandableListView.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during ListView scrolling,
		// we don't look for swipes.
		expandableListView.setOnScrollListener(touchListener.makeScrollListener());
		registerForContextMenu(expandableListView);
		// retrieve all the application data stored in the data base 
		taskListModel.retrieveData();
		// add software menu button support
		Utils.addLegacyOverflowButton(this.getWindow());
		// show/hide no tasks image
		Utils.checkAlertImageTrigger(taskListModel.getTasks().size());
		// check for first activation of the application
		if (isFirstActivation())
		{
			startActivity(new Intent(this, HelpScreenActivity.class));
			savePreferences(Utils.LANGUAGE, true);
			savePreferences(Utils.LOUD_SOUND, true);
		}
		// set application language 
		Utils.ENGLISH_LANGUAGE = loadPreferences(Utils.LANGUAGE);
		Utils.IS_DEFAULT_SOUND = loadPreferences(Utils.LOUD_SOUND);

		if (Utils.ENGLISH_LANGUAGE == false)
		{
			titleTextField.setHint(Utils.HEBREW_TITLE_ENTER);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		// reopen database 
		taskListModel.getDataBase().open();
		// register sensors resources
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onPause()
	{
		// release sensors resources
		mSensorManager.unregisterListener(mSensorListener);
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		// free database and media player resources 
		taskListModel.getDataBase().close();
		mediaPlayer.killMediaPlayer();
	}

	// when clicking the add task button
	@Override
	public void onClick(View v)
	{
		long opt_value = 1;
		mediaPlayer.playAudio(Utils.ADD_SOUND);
		// Where myTracker is an instance of Tracker.
		// count task addition to the google analytics
		myTracker.trackEvent("ui_action", "button_press", "add_task_button", opt_value);
		// Assigns user entered text to "taskTitleStr" and adds a new task 
		String taskTitleStr = titleTextField.getText().toString();
		taskListModel.addTask(taskTitleStr, Utils.DEFAULT_DESCRIPTION, Utils.DEFUALT_NOTIFICATION);
		titleTextField.setText("");
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		// hide keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return false;
	} 

	public void onLoad(long loadTime) 
	{
		// track application load time in google analytics
		myTracker.trackTiming("resources", loadTime, "high_scores", null);
	}

	@Override
	public void onStart() 
	{
		super.onStart();
		// reallocate resources
		taskListModel.getDataBase().open();
		EasyTracker.getInstance().activityStart(this); 
		// location provider is enabled each time the activity resumes from the stopped state.
		taskAlarms = new TaskAlarms(this);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	// listen for messages from NotificationService
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			int taskId = intent.getIntExtra("id", -1);
			String alarmType = intent.getStringExtra("alarmType");

			if (taskId != -1 && alarmType != null)
			{
				int adapterPosition = adapter.findAdapterPosition(taskId);

				if (alarmType.equals("GPS"))
				{
					((Task) adapter.getGroup(adapterPosition)).setLocation(Utils.DEFUALT_LOCATION);
				}
				else
				{
					((Task) adapter.getGroup(adapterPosition)).setAlarmImage(Utils.ALARM_OFF_IMAGE);
					((Task) adapter.getGroup(adapterPosition)).setDate(Utils.DEFUALT_NOTIFICATION);
				}
				adapter.notifyDataSetChanged();
			}
		}
	};

	// creates the task properties menu on long click
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
		menu.setHeaderTitle("Task Menu");

		ExpandableListView.ExpandableListContextMenuInfo info =
				(ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

		// stores current item position
		int position = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		// current task description
		String taskDescription = taskListModel.getTaskAt(position).getTaskDescription();
		// is task marked as important
		boolean isImportant = taskListModel.getTaskAt(position).isImportant();

		if (Utils.ENGLISH_LANGUAGE == false)
		{
			// make hebrew menu
			menu.setHeaderTitle("מאפייני משימה");
			Utils.makeHebrewContextMenu(menu);

			if (taskDescription.equals(Utils.DEFAULT_DESCRIPTION)) 
			{
				MenuItem item = menu.findItem(R.id.editDescription);
				item.setTitle("הוסף תיאור");
			}
			if (isImportant) 
			{
				MenuItem item = menu.findItem(R.id.markImportant);
				item.setTitle("סמן כ-לא חשוב");
			}
		}
		else
		{
			// checks if description is empty
			if (taskDescription.equals(Utils.DEFAULT_DESCRIPTION)) 
			{
				MenuItem item = menu.findItem(R.id.editDescription);
				item.setTitle("Add Task Description");
			}

			if (isImportant) 
			{
				MenuItem item = menu.findItem(R.id.markImportant);
				item.setTitle("Mark As Unimportant");
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem)
	{
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();
		// holds current item position
		int index = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		currentPosition = index;

		// determines which item was selected at the menu
		switch (menuItem.getItemId()) 
		{
		case R.id.editTitle:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(getArguments(Utils.DIALOG_TEXT_ENTRY,index,Utils.EDIT_TITLE));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			return true;

		case R.id.editDescription:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(getArguments(Utils.DIALOG_LONG_TEXT_ENTRY,index,Utils.EDIT_DESCRIPTION));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			return true;

		case R.id.markImportant:
			// mark the task as important/unimportant
			boolean important = taskListModel.getTaskAt(index).isImportant();
			taskListModel.getTaskAt(index).setImportant(!important);
			taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(index));
			adapter.notifyDataSetChanged();
			return true;

		case R.id.deleteTask:
			taskToUndo = taskListModel.getTaskAt(index);			
			taskListModel.removeTask(index);
			mediaPlayer.playAudio(Utils.DELETE_SOUND); 
			taskAlarms.disableTaskAlerts(taskToUndo);
			adapter.notifyDataSetChanged();
			showUndo(viewContainer);
			return true;
		default:
			return super.onContextItemSelected(menuItem);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu item)
	{
		// every time the options menu opens check for the application language
		if (Utils.ENGLISH_LANGUAGE == false) 
		{
			Utils.makeHebrewOptionsMenu(item);
		}
		else 
		{
			Utils.makeEnglishOptionsMenu(item);
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		new MenuInflater(getApplicationContext()).inflate(R.menu.application_menu, menu); 
		// checks if current language is english
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.delete_all:
			alertDialog = new AlertDialogs();
			alertDialog.setArguments(
					getArguments(Utils.DIALOG_YES_NO_MESSAGE,currentPosition,Utils.DELETE_ALL_ALERT));
			alertDialog.show(getFragmentManager(),"ShowAlertDialog");
			adapter.notifyDataSetChanged();
			return true;

		case R.id.help:
			startActivity(new Intent(this, HelpScreenActivity.class));
			return true;

		default: 
			return true;
		}
	}

	/** 
	 * When user clicks on alarm clock button.
	 * (callback from the list adapter)
	 */
	@Override
	public void onAlarmClick(int position)
	{
		// checks if alarm button was clicked to turn it on or off
		if (taskListModel.getTaskAt(position).getAlarmImage() == Utils.ALARM_ON_IMAGE)
		{
			taskListModel.getTaskAt(position).setAlarmImage(Utils.ALARM_OFF_IMAGE);

			if (! taskListModel.getTaskAt(position).getDate().equals(Utils.DEFUALT_NOTIFICATION))
			{
				// set to no notification and cancel alarm
				taskListModel.getTaskAt(position).setDate(Utils.DEFUALT_NOTIFICATION);
				taskAlarms.cancelAlarm(taskListModel.getTaskAt(position).getId(),
						taskListModel.getTaskAt(position).getTaskTitle());
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
			}
			return;
		}

		// create and show the notification alert dialog
		currentPosition = position;
		notificationPopup = new AlarmAlertDialog();
		notificationPopup.show(getFragmentManager(),"notificationAlertDialog");
		return;
	}

	/**
	 * When user clicks OK button on set notification alert dialog 
	 */
	@Override
	public void onSetAlarmPositiveClick(DialogFragment dialog)
	{
		// get user selected time and date from the date and time pickers
		long repeatAlarmInterval;
		Calendar calendar = notificationPopup.getSelectedTimeAndDate();
		boolean repeating = notificationPopup.getAlarmInterval();
		String fullDate = Utils.convertCalendarToString(calendar);

		if (repeating == true)
		{
			// get the user selected time interval
			repeatAlarmInterval = notificationPopup.getSelectedInterval();

			fullDate += "\n(" + notificationPopup.getSelectedIntervalString() + ")";
		}
		else
		{
			// indicates no interval
			repeatAlarmInterval = -1;
		}

		// update task data
		taskListModel.getTaskAt(currentPosition).setDate(fullDate);
		taskListModel.getTaskAt(currentPosition).setAlarmImage(Utils.ALARM_ON_IMAGE);
		taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(currentPosition));
		// set the alarm for notification
		taskAlarms.setAlarm(repeatAlarmInterval,calendar,currentPosition);
		adapter.notifyDataSetChanged();
	}

	/** 
	 * Shows the undo task deletion window.
	 * 
	 * @param viewContainer
	 */
	public static void showUndo(final View viewContainer) 
	{
		viewContainer.setVisibility(View.VISIBLE);
		viewContainer.setAlpha(1);
		viewContainer.animate().alpha(0.4f).setDuration(5000)
		.withEndAction(new Runnable() 
		{
			@Override
			public void run() 
			{
				viewContainer.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * when user clicks undone (after task deletion).
	 *  
	 * @param view
	 */
	public void undoTaskDeletion(View view)
	{
		viewContainer.setVisibility(View.GONE);
		// restore the deleted task
		taskListModel.addExistingTask(taskToUndo);
		adapter.notifyDataSetChanged();
	}


	/**
	 * Get bundle with arguments to send to alert dialogs
	 * 
	 * @param id - alert dialog type
	 * @param position - task position
	 * @param title - alert dialog title
	 * @return bundle with arguments
	 */
	public Bundle getArguments(int id, int position, String title)
	{
		Bundle args = new Bundle();
		args.putInt("id",id);
		args.putInt("position",position);
		args.putString("dialogTitle",title);
		return args;
	}

	/**
	 * Shares task data among applications
	 * 
	 * @param position - task to share position 
	 */
	public void shareTask(int position)
	{
		Task taskToShare = taskListModel.getTaskAt(position);
		String shareMessage = "Title: " + taskToShare.getTaskTitle() +
				"\nDescription: " + taskToShare.getTaskDescription();
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
	}

	/**
	 * Check if user has activated the application for the first time
	 * 
	 * @return true if first activation, otherwise return false
	 */
	public boolean isFirstActivation()
	{
		if (loadPreferences("firstActivation") == false)
		{
			savePreferences("firstActivation", true);
			return true;
		}
		else
		{
			return false;
		}
	}

	private void savePreferences(String key, boolean value)
	{
		SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		// Commit the edits!
		editor.commit();
	}

	private boolean loadPreferences(String key)
	{
		SharedPreferences settings = getSharedPreferences("MyPrefsFile", 0);
		return settings.getBoolean(key, false);
	}

	public static ItemListBaseAdapter getAdapter()
	{
		return adapter;
	}
}