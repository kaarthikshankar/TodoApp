package uf.cise.nui.todos.data;

import uf.cise.nui.todos.Utils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/** 
 * Application database
 */
public class DBAdapter
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_DATE = "date";
	public static final String KEY_LOCATION = "location";
	public static final String KEY_IMPORTANT = "important";
	public static final String KEY_CHECKBOX = "checkBoxState";

	private static final String TAG = "DBAdapter";

	private static final String DATABASE_NAME = "tasks";
	private static final String DATABASE_TABLE = "todo_list";
	private static final int 	DATABASE_VERSION = 3;

	private static final String DATABASE_CREATE =
			"create table todo_list (_id integer primary key autoincrement, title text not null, " +
					"description text not null, " +
					"date text not null, " +
					"location text not null, " +
					"important text not null, " +
					"checkBoxState text not null)";

	private final Context context; 

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) 
	{
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion 
					+ " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS todo_list");
			onCreate(db);
		}
	}    

	/** Opens the data base. */
	public DBAdapter open() throws SQLException 
	{
		db = DBHelper.getWritableDatabase();
		return this;
	}

	/** Closes the data base. */
	public void close() 
	{
		DBHelper.close();
	}

	/**  
	 * Inserts a task into the data base.
	 * 
	 * @param title
	 * @param description
	 * @param date
	 * @param location
	 * @param important
	 * @param checkBoxState
	 * @return
	 */
	public long insertTask(String title, String description, String date,
			String location, String important, String checkBoxState) 
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_DESCRIPTION, description);
		initialValues.put(KEY_DATE, date);
		initialValues.put(KEY_LOCATION, location);
		initialValues.put(KEY_IMPORTANT, important);
		initialValues.put(KEY_CHECKBOX, checkBoxState);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/** 
	 * Inserts a task into the data base. (overload version)
	 * 
	 * @param taskToAdd
	 * @return long id 
	 */
	public long insertTask(Task taskToAdd) 
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, taskToAdd.getTaskTitle());
		initialValues.put(KEY_DESCRIPTION, taskToAdd.getTaskDescription());
		initialValues.put(KEY_DATE, taskToAdd.getDate());
		initialValues.put(KEY_LOCATION, taskToAdd.getLocation());
		initialValues.put(KEY_IMPORTANT, taskToAdd.isImportantString());
		initialValues.put(KEY_CHECKBOX, taskToAdd.isCheckBoxCheckedString());
		return db.insert(DATABASE_TABLE, null, initialValues);
	}

	/** Deletes a particular task. */
	public boolean deleteTask(long rowId) 
	{
		return db.delete(DATABASE_TABLE, KEY_ROWID + 
				"=" + rowId, null) > 0;
	}

	/** 
	 * Retrieves all tasks from the data base.
	 */
	public Cursor getAllTasks() 
	{
		return db.query(DATABASE_TABLE, new String[] {
				KEY_ROWID,
				KEY_TITLE,
				KEY_DESCRIPTION,
				KEY_DATE,
				KEY_LOCATION,
				KEY_IMPORTANT,
				KEY_CHECKBOX}, 
				null, 
				null, 
				null, 
				null, 
				null);
	}

	/** 
	 * Retrieves a particular task.
	 * 
	 * @param rowId
	 * @return Cursor
	 * @throws SQLException
	 */
	public Cursor getTask(long rowId) throws SQLException 
	{
		Cursor mCursor =
				db.query(true, DATABASE_TABLE, new String[] {
						KEY_ROWID,
						KEY_TITLE,
						KEY_DESCRIPTION,
						KEY_DATE,
						KEY_LOCATION,
						KEY_IMPORTANT,
						KEY_CHECKBOX}, 
						KEY_ROWID + "=" + rowId, 
						null,
						null, 
						null, 
						null, 
						null);
		if (mCursor != null)
		{
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/** 
	 * Updates a task in the data base.
	 * 
	 * @param taskToUpdate
	 * @return true - if update succeeded
	 */
	public boolean updateTask(Task taskToUpdate) 
	{
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, taskToUpdate.getTaskTitle());
		args.put(KEY_DESCRIPTION, taskToUpdate.getTaskDescription());
		args.put(KEY_DATE, taskToUpdate.getDate());
		args.put(KEY_LOCATION, taskToUpdate.getLocation());
		args.put(KEY_IMPORTANT, taskToUpdate.isImportantString());
		args.put(KEY_CHECKBOX, taskToUpdate.isCheckBoxCheckedString());
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + taskToUpdate.getId(), null) > 0;
	}

	/** 
	 * Updates a task location alert to none.
	 * 
	 * @param taskToUpdate
	 * @return true - if update succeeded
	 */
	public boolean disableTaskLocationAlert(int id) 
	{
		ContentValues args = new ContentValues();
		args.put(KEY_LOCATION, Utils.DEFUALT_LOCATION);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + id, null) > 0;
	}

	/** 
	 * Updates a task notification to none.
	 * 
	 * @param taskToUpdate
	 * @return true - if update succeeded
	 */
	public boolean disableTaskNotification(int id) 
	{
		ContentValues args = new ContentValues();
		args.put(KEY_DATE, Utils.DEFUALT_NOTIFICATION);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + id, null) > 0;
	}

	/**
	 * Get number of items in the DataBase
	 * 
	 * @return - number of items in the DataBase
	 */
	public long getRowCount()
	{
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		String count = "SELECT count(*) FROM todo_list";
		Cursor mcursor = db.rawQuery(count, null);
		mcursor.moveToFirst();
		int icount = mcursor.getInt(0);
		return icount;
	}

	/**
	 * Get a random task title from the data base
	 * 
	 * @return - the task title
	 */
	public String getRandomTaskTitle() 
	{
		Cursor cursor = this.db.rawQuery("SELECT * FROM todo_list ORDER BY RANDOM() LIMIT 1",  null);
		String result = "No Tasks";

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
		{
			result = result + cursor.getString(Utils.FIELD_TITLE);
		}
		return result;
	}
}