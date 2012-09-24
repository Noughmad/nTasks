package com.noughmad.ntasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;


public class Database {
	static final String ID = BaseColumns._ID;
	
	static final String PROJECT_TABLE_NAME = "Project";
	static final String TASK_TABLE_NAME = "Task";
	static final String NOTE_TABLE_NAME = "Note";
	static final String WORKUNIT_TABLE_NAME = "WorkUnit";

	static final String KEY_PROJECT_TITLE = "title";
	static final String KEY_PROJECT_CLIENT = "client";
	static final String KEY_PROJECT_DESCRIPTION = "description";
	static final String KEY_PROJECT_CATEGORY = "category";

	static final String KEY_TASK_NAME = "name";
	static final String KEY_TASK_PROJECT = "project";
	static final String KEY_TASK_STATUS = "status";
	static final String KEY_TASK_DURATION = "duration";
	static final String KEY_TASK_ACTIVE = "active";
	static final String KEY_TASK_LASTSTART = "lastStart";
	
	static final String KEY_NOTE_TEXT = "text";
	static final String KEY_NOTE_TASK = "task";

	static final String KEY_WORKUNIT_TASK = "task";
	static final String KEY_WORKUNIT_START = "start";
	static final String KEY_WORKUNIT_END = "end";
	static final String KEY_WORKUNIT_DURATION = "duration";
	
	static final String KEY_OBJECT = "object";
	
	static final Uri BASE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://com.noughmad.ntasks.provider");

	public static final String EXISTS_PARSE_ID = "exists";
	public static final String IS_LOCAL = "local";

	private Helper mHelper;
	
	static String[] projectColumns = new String[] {
		 ID, KEY_PROJECT_TITLE, KEY_PROJECT_CLIENT, KEY_PROJECT_CATEGORY, KEY_PROJECT_DESCRIPTION, KEY_OBJECT
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
	
	Database(Context context) {
		mHelper = new Helper(context);
	}
	
	Cursor getProjects() {
		SQLiteDatabase db = mHelper.getReadableDatabase();
		return db.query(PROJECT_TABLE_NAME, projectColumns, null, null, null, null, null);
	}
	
	Cursor getTasks(long projectId) {
		String[] args = new String[] {Long.toString(projectId)};
		return mHelper.getReadableDatabase().query(TASK_TABLE_NAME, taskColumns, KEY_TASK_PROJECT + " = ?", args, null, null, null);
	}
	
	Cursor getNotes(long taskId) {
		String[] args = new String[] {Long.toString(taskId)};
		return mHelper.getReadableDatabase().query(NOTE_TABLE_NAME, noteColumns, KEY_NOTE_TASK + " = ?", args, null, null, null);
	}
	
	long createObject(SQLiteDatabase db, String className) {
		ContentValues values = new ContentValues();
		values.put(Bridge.KEY_OBJECT_CLASSNAME, className);
		values.put(Bridge.KEY_OBJECT_LOCAL, true);
		return db.insert(Bridge.OBJECT_TABLE_NAME, null, values);
	}
	
	void updateObject(SQLiteDatabase db, long objectId) {
		ContentValues values = new ContentValues();
		values.put(Bridge.KEY_OBJECT_LOCAL, true);
		db.update(Bridge.OBJECT_TABLE_NAME, values, ID + " = ?", new String[] {Long.toString(objectId)});
	}
	
	long createProject(ContentValues values) {
		long id = 0;
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			long objectId = createObject(db, PROJECT_TABLE_NAME);
			values.put(KEY_OBJECT, objectId);
			id = db.insert(PROJECT_TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		return id;
	}
	
	void updateProject(long id, ContentValues values) {
		Cursor cursor = getProject(id, new String[] {KEY_OBJECT});
		long objectId = cursor.getLong(0);
		cursor.close();
		
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			updateObject(db, objectId);
			values.put(KEY_OBJECT, objectId);
			db.update(PROJECT_TABLE_NAME, values, ID + " = ?", new String[] {Long.toString(id)});
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	long createTask(String name, long projectId) {
		long id = 0;
		ContentValues values = new ContentValues();
		values.put(KEY_TASK_NAME, name);
		values.put(KEY_TASK_PROJECT, projectId);
		values.put(KEY_TASK_DURATION, 0);
		values.put(KEY_TASK_ACTIVE, false);

		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			values.put(KEY_OBJECT, createObject(db, PROJECT_TABLE_NAME));
			id = db.insert(PROJECT_TABLE_NAME, null, values);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return id;
	}
	
	void startTask(long taskId) {
		ContentValues values = new ContentValues();
		values.put(KEY_TASK_ACTIVE, 1);
		values.put(KEY_TASK_LASTSTART, System.currentTimeMillis());
		String[] args = new String[] {Long.toString(taskId)};
		mHelper.getWritableDatabase().update(TASK_TABLE_NAME, values, ID + " = ?", args);
	}
	
	void stopTask(long taskId) {
		String where = ID + " = ? AND " + KEY_TASK_ACTIVE + " = 1";
		String[] args = new String[] {Long.toString(taskId)};

		ContentValues values = new ContentValues();
		
		Cursor cursor = mHelper.getReadableDatabase().query(TASK_TABLE_NAME, taskColumns, where, args, null, null, null);
		if (!cursor.moveToFirst()) {
			return;
		}
		
		long start = cursor.getLong(cursor.getColumnIndex(KEY_TASK_LASTSTART));
		long unitDuration = System.currentTimeMillis() - start;
		long taskDuration = cursor.getLong(cursor.getColumnIndex(KEY_TASK_DURATION)) + unitDuration;
		
		SQLiteDatabase db = mHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			values.put(KEY_TASK_DURATION, taskDuration);
			values.put(KEY_TASK_ACTIVE, 0);
			db.update(TASK_TABLE_NAME, values, where, args);
			
			ContentValues unitValues = new ContentValues();
			unitValues.put(KEY_WORKUNIT_START, start);
			unitValues.put(KEY_WORKUNIT_END, System.currentTimeMillis());
			unitValues.put(KEY_WORKUNIT_DURATION, unitDuration);
			
			db.insert(WORKUNIT_TABLE_NAME, null, unitValues);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	private class Helper extends SQLiteOpenHelper {
		private static final int DATABASE_VERSION = 10;
		private static final String DATABASE_NAME = "nTasks";


	    private static final String PROJECT_TABLE_CREATE =
	                "CREATE TABLE " + PROJECT_TABLE_NAME + " (" +
	                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
	                KEY_OBJECT + " INTEGER, " +
	                KEY_PROJECT_TITLE + " TEXT, " +
	                KEY_PROJECT_CLIENT + " TEXT, " +
	                KEY_PROJECT_DESCRIPTION + " TEXT, " +
	                KEY_PROJECT_CATEGORY + " INTEGER, " + 
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
	                "FOREIGN KEY(" + KEY_TASK_PROJECT + ") REFERENCES " + PROJECT_TABLE_NAME + "(" + BaseColumns._ID + "), " +
	                "FOREIGN KEY(" + KEY_OBJECT + ") REFERENCES " + Bridge.OBJECT_TABLE_NAME + "(" + BaseColumns._ID + "));";

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
    
	    Helper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	    	String TAG = "Database.Helper.onCreate()";
	    	Log.d(TAG, Bridge.OBJECT_TABLE_CREATE);
	    	Log.d(TAG, PROJECT_TABLE_CREATE);
	    	Log.d(TAG, TASK_TABLE_CREATE);
	    	Log.d(TAG, NOTE_TABLE_CREATE);
	    	Log.d(TAG, WORKUNIT_TABLE_CREATE);
	    	try {
	    		db.execSQL(Bridge.OBJECT_TABLE_CREATE);
	    		db.execSQL(PROJECT_TABLE_CREATE);
	    		db.execSQL(TASK_TABLE_CREATE);
	    		db.execSQL(NOTE_TABLE_CREATE);
	    		db.execSQL(WORKUNIT_TABLE_CREATE);
	    	} catch (SQLiteException e) {
	    		e.printStackTrace();
	    		Log.e("Database.Helper", e.getMessage());
	    	}
	    }

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO;
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
