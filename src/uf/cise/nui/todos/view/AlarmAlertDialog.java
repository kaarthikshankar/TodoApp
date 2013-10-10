package uf.cise.nui.todos.view;


import java.util.Calendar;

import uf.cise.nui.todos.R;
import uf.cise.nui.todos.Utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

/** 
 * Creates an alarm set alert dialog
 */
public class AlarmAlertDialog extends DialogFragment
{	
	private Switch 		alarmInterval;
	private TimePicker 	time;
	private DatePicker 	date;
	private String 		spinnerSelected = "Once a day";
	private Spinner 	spinner;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    View layout = inflater.inflate(R.layout.notification_popup, null);
	    
	    if (! Utils.ENGLISH_LANGUAGE)
	    {
	    	TextView repeating = (TextView) layout.findViewById(R.id.set_repeating_text);
	    	repeating.setText("      התראה חוזרת");
	    }
	    alarmInterval = (Switch) layout.findViewById(R.id.switcRepeating);
		time = (TimePicker) layout.findViewById(R.id.timePicker_create_act);
		date = (DatePicker) layout.findViewById(R.id.datePicker_create_act);
		spinner = (Spinner) layout.findViewById(R.id.interval_spinner);
		spinner.setVisibility(View.GONE);
		
		alarmInterval.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked)
			{
				if (isChecked)
				{
					spinner.setVisibility(View.VISIBLE);
				}
				else
				{
					spinner.setVisibility(View.GONE);
				}
			}
		});
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{

			@Override
			public void onItemSelected(AdapterView<?> parent, View view ,int pos, long id)
			{
				spinnerSelected = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				
			}
		});
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(layout.getContext(),
		        R.array.interval_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(spinnerAdapter);
		
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
		builder.setView(layout)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int id)
			{
				if (Calendar.getInstance().getTimeInMillis() >= getSelectedTimeAndDate().getTimeInMillis())
				{
					Utils.makeToast(getActivity(), "Please enter a valid date and time");
				}
				else
				{
					// Send the positive button event back to the host activity
					mListener.onSetAlarmPositiveClick(AlarmAlertDialog.this);
				}
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{
                dialog.cancel();
			}
		});
		return builder.create();
	}

	/* The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks.
	 * Each method passes the DialogFragment in case the host needs to query it. */
	public interface SetAlarmListener 
	{
		public void onSetAlarmPositiveClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	SetAlarmListener mListener;

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try 
		{
			// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (SetAlarmListener) activity;
		}
		catch (ClassCastException e)
		{
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	/**
	 * Check repeat switch state
	 * 
	 * @return true if alarm repeat was checked, false otherwise
	 */
	public boolean getAlarmInterval()
	{
		return alarmInterval.isChecked();
	}
	
	/**
	 * Get the selected alarm interval
	 * 
	 * @return the interval chosen
	 */
	public long getSelectedInterval()
	{
		if (spinnerSelected.equals("Once a day"))
		{
			return AlarmManager.INTERVAL_DAY;
		}
		else if (spinnerSelected.equals("Once a week"))
		{
			return AlarmManager.INTERVAL_DAY * 7;
		}
		return 0;
	}
	
	/**
	 * Get the selected alarm interval in string
	 * 
	 * @return the interval chosen
	 */
	public String getSelectedIntervalString()
	{
		return spinnerSelected;
	}
	
	/**
	 * Get time and date selected by the user
	 * 
	 * @return the time set by the user
	 */
	public Calendar getSelectedTimeAndDate()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
		calendar.set(Calendar.MONTH, date.getMonth());
		calendar.set(Calendar.YEAR, date.getYear());
		calendar.set(Calendar.HOUR_OF_DAY, time.getCurrentHour());
		calendar.set(Calendar.MINUTE, time.getCurrentMinute());
		return calendar;
	}
}
