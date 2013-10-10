package uf.cise.nui.todos.widget;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.data.Task;
import uf.cise.nui.todos.data.TaskList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

/**
 * Service that triggers on widget button press.
 * speaks today's tasks 
 */
public class TextToSpeechService extends Service implements TextToSpeech.OnInitListener
{
	private TextToSpeech speakEngine;
	private String spokenText;
	private TaskList taskListModel;
	private final String NO_TASKS = "There are no tasks for today";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		speakEngine = new TextToSpeech(this, this);
		taskListModel = TaskList.getSingletonObject(this);
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onInit(int status) 
	{
		spokenText = getSpokenText();

		speakEngine.setOnUtteranceProgressListener(new UtteranceProgressListener()
		{
			@Override
			public void onDone(String utteranceId)
			{
				if (speakEngine != null) 
				{
					speakEngine.stop();
					speakEngine.shutdown();
				}
			}
			@Override
			public void onError(String utteranceId) 
			{

			}
			@Override
			public void onStart(String utteranceId) 
			{

			}
		});

		if (status == TextToSpeech.SUCCESS)
		{
			int result = speakEngine.setLanguage(Locale.US);
			if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
			{
				speakEngine.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}

	/**
	 * Builds a string to speak with today's tasks
	 * 
	 * @return the string to speak
	 */
	private String getSpokenText()
	{
		String speakText = null;
		int taskNumber = 1;
		Calendar currentDate = Calendar.getInstance();		
		Calendar taskDate = Calendar.getInstance();
		Locale local = new Locale("EEE MMM dd HH:mm:ss z yyyy");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",local);
		speakText = "Today's tasks,";
		
		ArrayList<Task> tasks;
		
		if (taskListModel.getTasks().size() == 0)
		{
			taskListModel.getDataBase().open();
			tasks = taskListModel.retrieveData();
			taskListModel.getDataBase().close();
		}
		else
		{
			taskListModel.getDataBase().open();
			tasks = taskListModel.getTasks();
			taskListModel.getDataBase().close();
		}

		for (int i=0; i < tasks.size(); i++)
		{
			try 
			{	// check if there is a notification time
				if (! tasks.get(i).getDate().equals(Utils.DEFUALT_NOTIFICATION))
				{
					taskDate.setTime(sdf.parse(tasks.get(i).getDate()));
				}
				else { continue; }
			} 
			catch (ParseException e)
			{
				e.printStackTrace();
			}

			// speak only tasks for today
			if (Utils.isSameDay(currentDate, taskDate))
			{
				speakText += "task " + taskNumber + "," + tasks.get(i).getTaskTitle() + ",";
				taskNumber++;
			}
		}

		if (taskNumber == 1)
		{
			speakText = NO_TASKS;
		}
		return speakText;
	}
}