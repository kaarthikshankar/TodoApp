package uf.cise.nui.todos.data;


import java.util.ArrayList;

import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.notifications.TaskAlarms;
import uf.cise.nui.todos.view.MainActivity;

import android.content.Context;
import android.database.Cursor;

/** 
 * Singleton class to hold the global list of tasks  
 */
public class TaskList 
{
	// the class singleton object 
	private static TaskList singletonObject = null;
	// a list array of the current tasks in the application
	private ArrayList<Task> tasks;
	// object of the application database 
	private DBAdapter 		dataBase;
	private Context 		context;

	// private constructor
	private TaskList(Context context) 
	{
		this.context = context;
		this.tasks = new ArrayList<Task>();
		this.dataBase = new DBAdapter(context);
	}

	// Prevent object cloning 
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Handles singleton object creation
	 * 
	 * @param context
	 * @return TaskList singleton object
	 */
	public static synchronized TaskList getSingletonObject(Context context)
	{
		if (singletonObject == null)
		{
			singletonObject = new TaskList(context);
		}
		return singletonObject;
	}
	
	public ArrayList<Task> getTasks()
	{
		return tasks;
	}

	public Task getTaskAt(int position)
	{
		return tasks.get(position);
	}

	public void addTaskToStart(Task task)
	{
		tasks.add(0,task);
	}

	public void clearList()
	{
		tasks.clear();
	}

	public void assign(ArrayList<Task> arr)
	{
		tasks = arr;
	}

	public DBAdapter getDataBase() 
	{
		return dataBase;
	}

	public void setDataBase(DBAdapter dataBase)
	{
		this.dataBase = dataBase;
	}

	public Context getContext() 
	{
		return context;
	}

	public void setContext(Context context) 
	{
		this.context = context;
	}

	/**
	 * Adds a task to the database and tasks array
	 * 
	 * @param title
	 * @param description
	 * @param fullDate
	 * @return true if successful, false otherwise
	 */
	public boolean addTask(String title, String description, String fullDate)
	{
		/* check that the user has entered a valid task description */
		if (title != null && !title.equals("") && !title.equals("Enter task title"))
		{
			// insert the new task to the data base
			long tmpID = dataBase.insertTask(title.trim(),description.trim(),fullDate.trim(),
					"No Location","false","false");
			// insert the new task to the task list
			addTaskToStart(new Task(tmpID,title,description,fullDate));
			Utils.checkAlertImageTrigger(getTasks().size());
			return true;
		}
		return false;
	}
	
	/**
	 * Adds a deleted task to the 
	 * database and tasks array
	 * 
	 * @param taskToAdd
	 */ 
	public void addExistingTask(Task taskToAdd)
	{
		long tmpId = 0;
		// insert the new task to the data base
		tmpId = dataBase.insertTask(taskToAdd);
		// insert the new task to the task list
		taskToAdd.setId(tmpId);
		addTaskToStart(taskToAdd);
		Utils.checkAlertImageTrigger(getTasks().size());
	}

	/** 
	 * Removes a task by given item position.
	 * 
	 * @param position - position in the listview
	 */
	public void removeTask(int position)
	{
		Task taskToDelete = tasks.get(position);
		dataBase.deleteTask(taskToDelete.getId());
		tasks.remove(position);
		Utils.checkAlertImageTrigger(getTasks().size());
	}
	
	/**
	 * Delete all tasks and alarms
	 */
	public void deleteAllTasks()
	{
		int tasksListSize = getTasks().size();
		TaskAlarms taskAlarms = new TaskAlarms(context);
		
		for (int i=0; i < tasksListSize; i++)
		{
			// remove all tasks and cancel all available notification and GPS alarms
			taskAlarms.disableTaskAlerts(getTaskAt(0));
			removeTask(0);
		}
	}
	
	/**
	 * Update task title
	 * 
	 * @param input - the new title
	 * @param position - item position
	 */
	public void updateTitle(String input, int position)
	{
		if (input != null && !input.equals(""))
		{
			getTaskAt(position).setTaskTitle(input.trim());
			dataBase.updateTask(getTaskAt(position));
			MainActivity.getAdapter().notifyDataSetChanged();
		}
	}
	
	/**
	 * Update task description
	 * 
	 * @param input - the new title
	 * @param position - item position
	 */
	public void updateDescription(String input, int position)
	{
		if (input != null && !input.equals(""))
		{
			getTaskAt(position).setTaskDescription(input.trim());
			dataBase.updateTask(getTaskAt(position));
			MainActivity.getAdapter().notifyDataSetChanged();
		}
	}
	
	/** 
	 * Pulls all data from database on startup
	 * 
	 * return - the list of tasks
	 */
	public ArrayList<Task> retrieveData()
	{
		clearList();
		// recover all tasks from the data base
		Cursor cursor = dataBase.getAllTasks();

		if (cursor.moveToFirst())
		{
			do 
			{          
				addTaskToStart(createTaskFromDatabase(cursor)); 
			}
			while (cursor.moveToNext());
		}
		cursor.close();
		return tasks;
	}

	/** 
	 * Translates a database item to a Task item 
	 * and adds it to the list
	 * 
	 * @param cursor
	 */
	public Task createTaskFromDatabase(Cursor cursor)
	{
		// creates a task from the database fields
		Task obj = new Task(
				Long.parseLong(
						cursor.getString(Utils.FIELD_ID)),
						cursor.getString(Utils.FIELD_TITLE),
						cursor.getString(Utils.FIELD_DESCRIPTION),
						cursor.getString(Utils.FIELD_DATE));
		
		obj.setLocation(cursor.getString(Utils.FIELD_LOCATION));

		// indicates if the task was 
		// marked as important or not
		if (cursor.getString(Utils.FIELD_IMPORTANT).equals("true"))
		{
			obj.setImportant(true);
		}
		else
		{
			obj.setImportant(false);
		}

		// indicates the check box state
		if (cursor.getString(Utils.FIELD_CHECKBOX).equals("true"))
		{
			obj.setCheckBoxState(true);
		}
		else
		{
			obj.setCheckBoxState(false);
		}

		if (! obj.getDate().equals(Utils.DEFUALT_NOTIFICATION))
		{
			obj.setAlarmImage(Utils.ALARM_ON_IMAGE);
		}

		return obj;
	} 	
}
