package com.noughmad.ntasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.noughmad.ntasks.sync.Bridge;


public class Database {
	public static final String ID = BaseColumns._ID;
	
	public static final String PROJECT_TABLE_NAME = "Project";
	public static final String TASK_TABLE_NAME = "Task";
	public static final String NOTE_TABLE_NAME = "Note";
	public static final String WORKUNIT_TABLE_NAME = "WorkUnit";

	public static final String KEY_PROJECT_TITLE = "title";
	public static final String KEY_PROJECT_CLIENT = "client";
	public static final String KEY_PROJECT_DESCRIPTION = "description";
	public static final String KEY_PROJECT_CATEGORY = "category";
	public static final String KEY_PROJECT_ICON = "icon";

	public static final String KEY_TASK_NAME = "name";
	public static final String KEY_TASK_PROJECT = "project";
	public static final String KEY_TASK_STATUS = "status";
	public static final String KEY_TASK_DURATION = "duration";
	public static final String KEY_TASK_ACTIVE = "active";
	public static final String KEY_TASK_LASTSTART = "lastStart";
	
	public static final String KEY_NOTE_TEXT = "text";
	public static final String KEY_NOTE_TASK = "task";

	public static final String KEY_WORKUNIT_TASK = "task";
	public static final String KEY_WORKUNIT_START = "start";
	public static final String KEY_WORKUNIT_END = "end";
	public static final String KEY_WORKUNIT_DURATION = "duration";
	
	public static final String KEY_OBJECT = "object";
	
	public static final Uri BASE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://com.noughmad.ntasks.provider");

	public static final String EXISTS_PARSE_ID = "Exists";
	public static final String IS_LOCAL = "Local";
	public static final String TIMELINE = "Timeline";
	public static final String PROJECT_TIMELINE = "ProjectTimeline";

	private Helper mHelper;
	
	public static String[] projectColumns = new String[] {
		 ID, KEY_PROJECT_TITLE, KEY_PROJECT_CLIENT, KEY_PROJECT_CATEGORY, KEY_PROJECT_DESCRIPTION, KEY_PROJECT_ICON, KEY_OBJECT
	};
	
	static String[] taskColumns = new String [] {
		ID, KEY_TASK_NAME, KEY_TASK_PROJECT, KEY_TASK_STATUS, KEY_TASK_DURATION, KEY_TASK_ACTIVE, KEY_TASK_LASTSTART, KEY_OBJECT
	};
	
	static String[] noteColumns = new String [] {
		ID, KEY_NOTE_TEXT, KEY_NOTE_TASK, KEY_OBJECT
	};
	
	static String[] workUnitColumns = new String [] {
		ID, KEY_WORKUNIT_TASK, KEY_WORKUNIT_START, KEY_WORKUNIT_END, KEY_WORKUNIT_DURATION, KEY_OBJECT
	};
	
	public Database(Context context) {
		mHelper = new Helper(context);
	}
	
	private class Helper extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_NAME = "nTasks";


	    private static final String PROJECT_TABLE_CREATE =
	                "CREATE TABLE " + PROJECT_TABLE_NAME + " (" +
	                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
	                KEY_OBJECT + " INTEGER, " +
	                KEY_PROJECT_TITLE + " TEXT, " +
	                KEY_PROJECT_CLIENT + " TEXT, " +
	                KEY_PROJECT_DESCRIPTION + " TEXT, " +
	                KEY_PROJECT_CATEGORY + " INTEGER, " + 
	                KEY_PROJECT_ICON + " TEXT, " +
	                "FOREIGN KEY(" + KEY_OBJECT + ") REFERENCES " + Bridge.OBJECT_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE);";

	    private static final String TASK_TABLE_CREATE =
	                "CREATE TABLE " + TASK_TABLE_NAME + " (" +
	                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
	                KEY_OBJECT + " INTEGER, " +
	                KEY_TASK_NAME + " TEXT, " +
	                KEY_TASK_PROJECT + " INTEGER, " +
	                KEY_TASK_STATUS + " INTEGER, " + 
	                KEY_TASK_DURATION + " INTEGER, " +
	                KEY_TASK_ACTIVE + " INTEGER, " +
	                KEY_TASK_LASTSTART + " INTEGER, " + 
	                "FOREIGN KEY(" + KEY_TASK_PROJECT + ") REFERENCES " + PROJECT_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE, " +
	                "FOREIGN KEY(" + KEY_OBJECT + ") REFERENCES " + Bridge.OBJECT_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE);";

	    private static final String NOTE_TABLE_CREATE =
                "CREATE TABLE " + NOTE_TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                KEY_OBJECT + " INTEGER, " +
                KEY_NOTE_TEXT + " TEXT, " +
                KEY_NOTE_TASK + " INTEGER, " +
                "FOREIGN KEY(" + KEY_NOTE_TASK + ") REFERENCES " + TASK_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + KEY_OBJECT + ") REFERENCES " + Bridge.OBJECT_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE);";
	
	    private static final String WORKUNIT_TABLE_CREATE =
                "CREATE TABLE " + WORKUNIT_TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                KEY_OBJECT + " INTEGER, " +
                KEY_WORKUNIT_TASK + " INTEGER, " +
                KEY_WORKUNIT_START + " INTEGER, " +
                KEY_WORKUNIT_END + " INTEGER, " +
                KEY_WORKUNIT_DURATION + " INTEGER, " +
                "FOREIGN KEY(" + KEY_WORKUNIT_TASK + ") REFERENCES " + TASK_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + KEY_OBJECT + ") REFERENCES " + Bridge.OBJECT_TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE);";
	    
	    private static final String TIMELINE_VIEW_CREATE = 
	    		"CREATE VIEW " + TIMELINE + " AS SELECT " + 
	    		TASK_TABLE_NAME + "." + ID + " AS " + ID + ", " +
				TASK_TABLE_NAME + "." + KEY_TASK_NAME + " AS " + KEY_TASK_NAME + ", " +
				TASK_TABLE_NAME + "." + KEY_TASK_PROJECT + " AS " + KEY_TASK_PROJECT + ", " +
				WORKUNIT_TABLE_NAME + "." + KEY_WORKUNIT_START + " AS " + KEY_WORKUNIT_START + ", " +
				WORKUNIT_TABLE_NAME + "." + KEY_WORKUNIT_END + " AS " + KEY_WORKUNIT_END + ", " +
				WORKUNIT_TABLE_NAME + "." + KEY_WORKUNIT_DURATION + " AS " + KEY_WORKUNIT_DURATION +  
				" FROM " + TASK_TABLE_NAME + ", " + WORKUNIT_TABLE_NAME + 
				" WHERE " + TASK_TABLE_NAME + "." + ID + " = " + WORKUNIT_TABLE_NAME + "." + KEY_WORKUNIT_TASK;
    
	    Helper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	    	try {
	    		db.execSQL(Bridge.OBJECT_TABLE_CREATE);
	    		db.execSQL(PROJECT_TABLE_CREATE);
	    		db.execSQL(TASK_TABLE_CREATE);
	    		db.execSQL(NOTE_TABLE_CREATE);
	    		db.execSQL(WORKUNIT_TABLE_CREATE);
	    		
	    		db.execSQL(TIMELINE_VIEW_CREATE);
	    	} catch (SQLiteException e) {
	    		e.printStackTrace();
	    		Log.e("Database.Helper", e.getMessage());
	    	}
	    }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
		    super.onOpen(db);
		    if (!db.isReadOnly()) {
		        db.execSQL("PRAGMA foreign_keys=ON;");
		    }
		}
	}

	public Cursor getProject(long id, String[] columns) {
		return mHelper.getReadableDatabase().query(PROJECT_TABLE_NAME, columns, ID + " = ?", new String[] {Long.toString(id)}, null, null, null);
	}

	public void deleteProject(long id) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.delete(TASK_TABLE_NAME, KEY_TASK_PROJECT + " = ?", new String[] {Long.toString(id)});
		db.delete(PROJECT_TABLE_NAME, ID + " = ?", new String[] {Long.toString(id)});		
	}

	public SQLiteDatabase getReadableDatabase() {
		return mHelper.getReadableDatabase();
	}
	
	public SQLiteDatabase getWritableDatabase() {
		return mHelper.getWritableDatabase();
	}
}
