package uf.cise.nui.todos;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import uf.cise.nui.todos.R;
import uf.cise.nui.todos.data.Task;
import uf.cise.nui.todos.data.TaskList;
import uf.cise.nui.todos.view.MainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Application's utilities
 */
public class Utils
{
	// the default task description
	public static final String DEFAULT_DESCRIPTION = "No Description";
	// the default task location alert
	public static final String DEFUALT_LOCATION = "No Location";
	// the default task notification time
	public static final String DEFUALT_NOTIFICATION = "No Notification";
	// code for google analytics purpose
	public static final String GOOGLE_ANALYTICS_CODE = "UA-37448489-1";
	public static final String GPS_ALERT = "Enable GPS ?";
	public static final String DELETE_ALL_ALERT = "Delete all tasks ?";
	public static final String EDIT_TITLE = "Edit Title";
	public static final String EDIT_DESCRIPTION = "Edit Description";
	public static final String SET_LOCATION = "Set Location";
	
	public static final int FIELD_ID = 0;
	public static final int FIELD_TITLE = 1;
    public static final int FIELD_DESCRIPTION = 2;
    public static final int FIELD_DATE = 3;
    public static final int FIELD_LOCATION = 4;
    public static final int FIELD_IMPORTANT = 5;
    public static final int FIELD_CHECKBOX = 6;

	public static final int DELETE_SOUND = R.raw.delete;
	public static final int ADD_SOUND = R.raw.button_add;
	public static final int LOUD_NOTIFICATION__SOUND = R.raw.loud_alarm;
	
	public static final int ALARM_ON_IMAGE = R.drawable.alarm_turquoise;
	public static final int ALARM_OFF_IMAGE = R.drawable.alarm_gray3;
	// number of similar GPS search results 
	public static final int NUMBER_OF_GPS_RESULTS = 3;
	// GPS search radius in meters
	public static final int SEARCH_RADIUS = 1000;
	
	// alert dialog options
	public static final int DIALOG_YES_NO_MESSAGE = 1;
	public static final int DIALOG_LONG_TEXT_ENTRY = 2;
	public static final int DIALOG_TEXT_ENTRY = 3;
	
	public static final String ENGLISH_TITLE_ENTER = "Enter Task Title...";
	public static final String HEBREW_TITLE_ENTER = "    הכנס משימה...";
    
	public static final String LANGUAGE = "language";
	public static final String LOUD_SOUND = "sound";
	
	public static boolean ENGLISH_LANGUAGE = true;
	public static boolean IS_DEFAULT_SOUND = true;

	/**
	 * Make long toast message
	 * 
	 * @param context
	 * @param text - string to show
	 */
	public static void makeToast(Context context, String text)
	{
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
	}

	/**
	 * Shows and hides the no tasks image
	 * according to list size
	 */
	public static void checkAlertImageTrigger(int listSize)
	{
		if (listSize == 0)
		{
			MainActivity.noTasksImage.setVisibility(View.VISIBLE);
		}
		else if (listSize == 1)
		{
			MainActivity.noTasksImage.setVisibility(View.GONE);
		}
	}

	/**
	 * For handling non hardware menu button phones.
	 * shows the software button if necessary
	 * 
	 * @param window - activity's window
	 */
	public static void addLegacyOverflowButton(Window window)
	{
		if (window.peekDecorView() == null) 
		{
			throw new RuntimeException("Must call addLegacyOverflowButton() after setContentView()");
		}

		try 
		{
			window.addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));
		}
		catch (NoSuchFieldException e)
		{
			// Ignore since this field won't exist in most versions of Android
		}
		catch (IllegalAccessException e) 
		{

		}
	}

	/**
	 * Create's a random number between two given numbers
	 * 
	 * @param first - lowest number
	 * @param second - highest number
	 * @return the random number
	 */
	public static int randomNumber(int first, int second)
	{
		int randomNum;

		if (first < second)
		{
			randomNum = (int) (second * Math.random() + first);
			return randomNum;
		}

		else if (second < first)
		{
			randomNum = (int) (first * Math.random() + second);
			return randomNum;
		}

		else // first and second are equal
		{
			System.out.println("The two numbers are equal");
			return first;
		}
	}
	
	/**
     * Checks if two calendars represent the same day ignoring time.
     * 
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) 
    {
        if (cal1 == null || cal2 == null)
        {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
    
    /**
     * Change the context menu to Hebrew
     * 
     * @param menu
     */
    public static void makeHebrewContextMenu(ContextMenu menu)
    {
    	MenuItem item = menu.findItem(R.id.editTitle);
    	item.setTitle("ערוך כותרת");
    	item = menu.findItem(R.id.editDescription);
    	item.setTitle("ערוך תיאור");
    	item = menu.findItem(R.id.markImportant);
    	item.setTitle("סמן כחשוב");
    	item = menu.findItem(R.id.deleteTask);
    	item.setTitle("מחק משימה");
    }
    
    /**
     * Change the options menu to Hebrew
     * 
     * @param menu
     */
    public static void makeHebrewOptionsMenu(Menu menu)
    {
    	MenuItem item = menu.findItem(R.id.delete_all);
    	item.setTitle("מחק את כל המשימות");
    	item.setTitle("שנה שפה לאנגלית");
    	
    	if (Utils.IS_DEFAULT_SOUND)
    	{
    		item.setTitle("שנה צליל התראה (חזק)");
    	}
    	else 
    	{
    		item.setTitle("שנה צליל התראה (רגיל)");
    	}
    	item = menu.findItem(R.id.help);
    	item.setTitle("עזרה");
    }
    
    /**
     * Change the options menu to English
     * 
     * @param menu
     */
    public static void makeEnglishOptionsMenu(Menu menu)
    {
    	MenuItem item = menu.findItem(R.id.delete_all);
    	item.setTitle("Delete all tasks");
    	item = menu.findItem(R.id.help);
    	item.setTitle("Help");
    }
    
    /**
     * Finds a task by id
     * 
     * @param context
     * @param id - task id
     * @return - the founded task, null otherwise
     */
    public static Task findTaskById(Context context, int id)
    {
    	TaskList taskListModel = TaskList.getSingletonObject(context);
    	Cursor cursor = taskListModel.getDataBase().getTask(id);
    	return taskListModel.createTaskFromDatabase(cursor);
    }
    
    /**
     * Custom conversion of a Calendar object to string
     * 
     * @param calendar - to convert
     * @return - the date as string
     */
    @SuppressLint("SimpleDateFormat")
	public static String convertCalendarToString(Calendar calendar)
    {
    	String strDate = null;
    	SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy  |  HH:mm:ss");

    	if (calendar != null) 
    	{
    		strDate = sdf.format(calendar.getTime());
    	}
    	return strDate;
    }
}
