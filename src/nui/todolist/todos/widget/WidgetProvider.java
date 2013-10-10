/*
 * Copyright (C) 2013 Ido Gold & Sahar Rehani
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package nui.todolist.todos.widget;

import nui.todolist.todos.R;
import nui.todolist.todos.Utils;
import nui.todolist.todos.data.TaskList;

import java.util.Timer;
import java.util.TimerTask;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Application widget 
 */
public class WidgetProvider extends AppWidgetProvider
{
	private TaskList taskListModel;
	private Timer timer;
	private final int UPDATE_RATE = 10000;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		final int N = appWidgetIds.length;
		taskListModel = TaskList.getSingletonObject(context);

		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) 
		{
			int appWidgetId = appWidgetIds[i];
			// Create an Intent to launch TextToSpeech service
			Intent intent = new Intent(context, TextToSpeechService.class);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_layout);
			views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

			// make a timer to switch text in the text field every few seconds
			timer = new Timer();
			timer.scheduleAtFixedRate(new MyTime(context, appWidgetManager), 1, UPDATE_RATE);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onDisabled(Context context)
	{
		if(timer != null)
		{
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	/**
	 *	Creates a timer cycle to update the widget
	 *	text field every constant amount of time  
	 *
	 */
	private class MyTime extends TimerTask
	{
		int listSize = 0; 
		String textToShow = "No Tasks";
		RemoteViews remoteViews;
		AppWidgetManager appWidgetManager;
		ComponentName thisWidget;

		public MyTime(Context context, AppWidgetManager appWidgetManager) 
		{
			this.appWidgetManager = appWidgetManager;
			remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider_layout);
			thisWidget = new ComponentName(context, WidgetProvider.class);
		}
		@Override
		public void run()
		{
			listSize = taskListModel.getTasks().size();

			if (listSize > 0)
			{
				textToShow = taskListModel.getTaskAt(Utils.randomNumber(0, listSize - 1)).getTaskTitle();
			}
			else // Application is down, pull the data from data base
			{
				taskListModel.getDataBase().open();
				textToShow = taskListModel.getDataBase().getRandomTaskTitle();
				taskListModel.getDataBase().close();
			}

			// show a random task in the widget text
			remoteViews.setTextViewText(R.id.widget_message,textToShow);
			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(thisWidget, remoteViews);
		}
	}
}