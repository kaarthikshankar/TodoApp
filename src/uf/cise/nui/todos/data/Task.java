
package uf.cise.nui.todos.data;

import uf.cise.nui.todos.R;

/**
 * Represents a task
 */
public class Task 
{
	private long 	id;
	private String 	taskTitle;
	private String 	taskDescription;
	// task notification date
	private String 	date;
	// task location for GPS alert
	private String 	location;
	// task done checkBox state
	private boolean checkBoxState;
	// task alarm button image
	private int 	alarmImage;
	// determines if the task title will be marked with a line (done state)
	private int 	textResource;
	// marked as important or not
	private boolean isImportant;
	
	public Task()
	{	
		setId(0);
		setLocation("No Location");
		setAlarmImage(R.drawable.alarm_gray3);
		setImportant(false);
	}
	
	public Task(long id, String taskTitle, String itemDescription, String date)
	{	
		setId(id);
		setTaskTitle(taskTitle);
		setTaskDescription(itemDescription); 
		setDate(date);
		setLocation("No Location");
		setAlarmImage(R.drawable.alarm_gray3);
		setImportant(false);
	}
	
	public Task(long id, String itemTitle, String itemDescription, String date,
			boolean checkBoxState, int alarmImage, int textResource, boolean isImportant)
	{	
		setId(id);
		setTaskTitle(itemTitle);
		setTaskDescription(itemDescription); 
		setDate(date);
		setCheckBoxState(checkBoxState);
		setAlarmImage(alarmImage);
		setTextResource(textResource);
		setImportant(isImportant);
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public String getTaskTitle() 
	{
		return taskTitle;
	}
	
	public void setTaskTitle(String taskTitle) 
	{
		this.taskTitle = taskTitle;
	}
	
	public String getTaskDescription()
	{
		return taskDescription;
	}
	
	public void setTaskDescription(String taskDescription) 
	{
		this.taskDescription = taskDescription;
	}
	
	public String getDate()
	{
		return date;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public String getLocation()
	{
		return location;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	public boolean isCheckBoxState()
	{
		return checkBoxState;
	}
	
	public void setCheckBoxState(boolean checkBoxState)
	{
		this.checkBoxState = checkBoxState;
	}
	
	public int getAlarmImage() 
	{
		return alarmImage;
	}
	
	public void setAlarmImage(int alarmImage)
	{
		this.alarmImage = alarmImage;
	}
	
	public int getTextResource()
	{
		return textResource;
	}
	
	public void setTextResource(int textResource)
	{
		this.textResource = textResource;
	}
	
	public boolean isImportant()
	{
		return isImportant;
	}
	
	public void setImportant(boolean isImportant)
	{
		this.isImportant = isImportant;
	}
	
	public String isImportantString()
	{
		if (isImportant)
		{
			return "true";
		}
		return "false";
	}
	
	public String isCheckBoxCheckedString()
	{
		if (checkBoxState)
		{
			return "true";
		}
		return "false";
	}
}
