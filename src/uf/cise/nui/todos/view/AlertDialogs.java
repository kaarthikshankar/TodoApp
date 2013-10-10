package uf.cise.nui.todos.view;

import uf.cise.nui.todos.MediaPlayerHandler;
import uf.cise.nui.todos.R;
import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.data.TaskList;
//import uf.cise.nui.todos.notifications.GeocoderProgressTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/** 
 * Creates an AlertDialog for various purposes 
 */
public class AlertDialogs extends DialogFragment 
{	
	private Context context;
	private int id = 0;
	private int currentPosition = 0;
	private String dialogTitle;
	private View layout;
	private EditText editText;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		context = getActivity();
		final TaskList taskListModel = TaskList.getSingletonObject(context);
		id =  getArguments().getInt("id");
		currentPosition = getArguments().getInt("position");
		dialogTitle = getArguments().getString("dialogTitle");
		String newTitle = "Title";
		final String action = dialogTitle;
		
		if (Utils.ENGLISH_LANGUAGE == false)
		{
			if (dialogTitle.equals(Utils.EDIT_TITLE))
			{
				newTitle = "ערוך כותרת";
			}
			else if (dialogTitle.equals(Utils.EDIT_DESCRIPTION))
			{
				newTitle = "ערוך תיאור";
			}
			else if (dialogTitle.equals(Utils.DELETE_ALL_ALERT))
			{
				newTitle = "מחק את כל המשימות ?";
			}
			else
			{
				newTitle = "הכנס מיקום";
			}
			dialogTitle = newTitle;
		}
		
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getActivity().getLayoutInflater();

		switch (id)
		{
		case Utils.DIALOG_YES_NO_MESSAGE:
			builder.setIconAttribute(android.R.attr.alertDialogIcon)
			.setTitle(dialogTitle)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					taskListModel.deleteAllTasks();
					new MediaPlayerHandler(context).playAudio(Utils.DELETE_SOUND);
					// allow sensor shake gestures
					MainActivity.shakeGestures = true;
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					MainActivity.shakeGestures = true;
					dialog.cancel();
				}
			});
			return builder.create();

		case Utils.DIALOG_TEXT_ENTRY:
			layout = inflater.inflate(R.layout.alert_dialog_text_entry, null);
			editText = (EditText) layout.findViewById(R.id.field_edit);
			
			if (action.equals(Utils.EDIT_TITLE))
			{
				editText.setText(taskListModel.getTaskAt(currentPosition).getTaskTitle());
			}
			else
			{
				if (Utils.ENGLISH_LANGUAGE)
				{
					editText.setHint("Enter Location");
				}
				else
				{
					editText.setHint("הכנס מיקום");
				}
			}
			
			builder.setTitle(dialogTitle)
			.setView(layout)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					if (action.equals(Utils.EDIT_TITLE))
					{
						taskListModel.updateTitle(editText.getText().toString(), currentPosition);
					}
					else
					{
						String location = editText.getText().toString();
						
						if (location != null && !location.equals(""))
						{
							// check for Geocoder matches with the user entered location 
							//new GeocoderProgressTask(context,currentPosition,location).execute();
						}
					}
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					dialog.cancel();
				}
			});
			return builder.create();

		case Utils.DIALOG_LONG_TEXT_ENTRY:
			layout = inflater.inflate(R.layout.alert_dialog_long_text_entry, null);
			editText = (EditText) layout.findViewById(R.id.long_field_edit);
			
			if (taskListModel.getTaskAt(currentPosition).
					getTaskDescription().equals(Utils.DEFAULT_DESCRIPTION))
			{
				if (Utils.ENGLISH_LANGUAGE)
				{
					editText.setHint("Enter Description");
				}
				else
				{
					editText.setHint("הכנס תיאור");
				}
			}
			else
			{
				editText.setText(taskListModel.getTaskAt(currentPosition).getTaskDescription());
			}
			
			builder.setTitle(dialogTitle)
			.setView(layout)
			.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					
					taskListModel.updateDescription(editText.getText().toString(), currentPosition);
				}
			})
			.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					dialog.cancel();
				}
			});
			return builder.create();
		}
		return null;
	}
}