package uf.cise.nui.todos.adapter;
import uf.cise.nui.todos.R;
import uf.cise.nui.todos.Utils;
import uf.cise.nui.todos.data.Task;
import uf.cise.nui.todos.data.TaskList;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

/** 
 * Custom adapter for the apllication's list view  
 */
public class ItemListBaseAdapter extends BaseExpandableListAdapter
{	
	private Context					context;
	private TaskList				taskListModel;
	private LayoutInflater 			l_Inflater;
	private NoticeAlarmListener 	mListener;
	private static final int 		IMPORTANT_ON = R.drawable.red_spacer2;
	private static final int 		IMPORTANT_OFF = R.drawable.spacer;

	public ItemListBaseAdapter(Context context)
	{
		this.context = context;
		taskListModel = TaskList.getSingletonObject(context);
		l_Inflater = LayoutInflater.from(context);

		try 
		{	// Instantiate the NoticeDialogListener so we can send events to the host
			mListener = (NoticeAlarmListener) context;
		}
		catch (ClassCastException e)
		{	// The activity doesn't implement the interface, throw exception
			throw new ClassCastException("MainActivty must implement NoticeAlarmListener");
		}
	}

	public interface NoticeAlarmListener 
	{
		public void onAlarmClick(int position);
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return taskListModel.getTaskAt(groupPosition);
	}
	
	@Override
    public long getChildId(int groupPosition, int childPosition)
	{
        return taskListModel.getTaskAt(groupPosition).getId();
    }

    @Override
    public int getChildrenCount(int groupPosition) 
    {
        return 1;
    }
    
    @Override
    public Object getGroup(int groupPosition)
    {
        return taskListModel.getTaskAt(groupPosition);
    }

    @Override
    public int getGroupCount() 
    {
        return taskListModel.getTasks().size();
    }

    @Override
    public long getGroupId(int groupPosition) 
    {
        return groupPosition;
    }
    
    @Override
    public boolean hasStableIds()
    {
        return true;
    }


    @Override
    public boolean isChildSelectable(int arg0, int arg1)
    {
        return false;
    }
    
    /**
     * Find a specific task position in the adapter
     * 
     * @param taskId - id of the desired task
     * @return - the position in the adapter
     */
    public int findAdapterPosition(int taskId)
    {
    	for (int i=0; i < getGroupCount(); i++)
    	{
    		if (((Task) getGroup(i)).getId() == taskId)
    		{
    			return i;
    		}
    	}
    	return -1;
    }

	private final OnCheckedChangeListener doneCheckBoxOnClickListener = new OnCheckedChangeListener()
	{
		@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			int position = (Integer) buttonView.getTag();
			// set text to strike through
			if (isChecked)
			{
				taskListModel.getTaskAt(position).setCheckBoxState(isChecked);
				taskListModel.getTaskAt(position).
				setTextResource(Paint.STRIKE_THRU_TEXT_FLAG);
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
			}
			// remove strike through paint
			else
			{
				taskListModel.getTaskAt(position).setCheckBoxState(isChecked);
				taskListModel.getTaskAt(position).
				setTextResource((~ Paint.STRIKE_THRU_TEXT_FLAG));
				taskListModel.getDataBase().updateTask(taskListModel.getTaskAt(position));
			}
			notifyDataSetChanged();
		}
	};

	//set the alarm button click listener
	private final OnClickListener notificationClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			int position = (Integer) view.getTag();
			mListener.onAlarmClick(position);
			notifyDataSetChanged();
		}
	};

	/**
	 * Synchronize list view item appearance
	 * to updated data
	 * 
	 * @param holder
	 * @param position
	 */
	public void syncItemAppearance(GroupViewHolder holder, int position)
	{
		// update task title text
		holder.txt_itemTitle.setText(taskListModel.getTaskAt(position).getTaskTitle());
		// update task alarm button image
		holder.notificationButton.setBackgroundResource(taskListModel.getTaskAt(position).getAlarmImage());
		// update task check box state
		holder.doneCheckBox.setChecked(taskListModel.getTaskAt(position).isCheckBoxState());

		if (taskListModel.getTaskAt(position).isCheckBoxState())
		{
			holder.txt_itemTitle.setPaintFlags(holder.txt_itemTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		}
		else
		{
			holder.txt_itemTitle.setPaintFlags(holder.txt_itemTitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
		}

		if (taskListModel.getTaskAt(position).isImportant())
		{
			holder.spacer.setBackgroundResource(IMPORTANT_ON);
		}
		else
		{
			holder.spacer.setBackgroundResource(IMPORTANT_OFF);
		}
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) 
	{
		final GroupViewHolder holder;

		// only when adding a new object to the list, allocate new memory
		// and use findViewById, saves huge amount of resources
		if (convertView == null) 
		{
			convertView = l_Inflater.inflate(R.layout.item_details_view, null);
			holder = new GroupViewHolder();

			holder.txt_itemTitle = (TextView) convertView.findViewById(R.id.title);
			holder.doneCheckBox = (CheckBox) convertView.findViewById(R.id.btnDone);
			holder.notificationButton = (ImageButton) convertView.findViewById(R.id.setNotification);
			holder.spacer = (ImageButton) convertView.findViewById(R.id.setImportent);

			holder.doneCheckBox.setOnCheckedChangeListener(doneCheckBoxOnClickListener);
			holder.notificationButton.setOnClickListener(notificationClickListener);

			convertView.setTag(holder);
		} 
		else 
		{
			holder = (GroupViewHolder) convertView.getTag();
		}

		holder.doneCheckBox.setTag(groupPosition);
		holder.notificationButton.setTag(groupPosition);

		syncItemAppearance(holder,groupPosition);
		return convertView;
	}

	@Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		final ChildViewHolder holder;
		
        if (convertView == null) 
        {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.task_child_description, null);
            holder = new ChildViewHolder();
            
            holder.descriptionText = (TextView) convertView.findViewById(R.id.taskDescription);
            holder.notificationText = (TextView) convertView.findViewById(R.id.taskNotification);            
            convertView.setTag(holder);
        }
        else 
		{
			holder = (ChildViewHolder) convertView.getTag();
		}
        
        holder.descriptionText.setTag(childPosition);
        holder.notificationText.setTag(childPosition);        
        holder.descriptionText.setText(taskListModel.getTaskAt(groupPosition).getTaskDescription());
    	holder.notificationText.setText(taskListModel.getTaskAt(groupPosition).getDate());
    	   	
        if (Utils.ENGLISH_LANGUAGE == false)
        {
        	if (taskListModel.getTaskAt(groupPosition).getTaskDescription().equals(Utils.DEFAULT_DESCRIPTION))
        	{
        		holder.descriptionText.setText("ללא תיאור");
        	}
        	if (taskListModel.getTaskAt(groupPosition).getDate().equals(Utils.DEFUALT_NOTIFICATION))
        	{
        		holder.notificationText.setText("ללא התראה");
        	}
        }
        return convertView;
    }
	
	// stores list view data in a static way to avoid
	// using every time the expansive findViewById function
	static class GroupViewHolder
	{
		TextView txt_itemTitle;
		CheckBox doneCheckBox;
		ImageButton notificationButton;
		ImageButton spacer;
	}
	
	static class ChildViewHolder
	{
		TextView descriptionText;
		TextView notificationText;
	}
}
