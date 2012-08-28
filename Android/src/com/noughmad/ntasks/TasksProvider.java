package com.noughmad.ntasks;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class TasksProvider extends ContentProvider {
	
	private final static String TAG = "TasksProvider";

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		String q = selectionArgs[0];
		Log.i(TAG, "query: " + q);
		
		if (ParseUser.getCurrentUser() == null) {
			return null;
		}
		
		Log.i(TAG, "A user is logged in");
		
		MatrixCursor cursor = new MatrixCursor(new String[] {
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_TEXT_2,
				SearchManager.SUGGEST_COLUMN_ICON_1,
				SearchManager.SUGGEST_COLUMN_LAST_ACCESS_HINT,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
		}, 10);
		
		ParseQuery projectQuery = new ParseQuery("Project");
		projectQuery.whereEqualTo("user", ParseUser.getCurrentUser());
		ParseQuery taskQuery = new ParseQuery("Task");
		taskQuery.whereEqualTo("project", projectQuery);
		
		if (q.isEmpty()) {
			// On an empty query, only show the last used task
			taskQuery.setLimit(1);
			taskQuery.orderByDescending("updatedAt");
		} else {
			taskQuery.whereContains("name", q);
		}

		int i = 0;
		try {
			for (ParseObject task : taskQuery.find()) {
				cursor.addRow(new Object[] {i, task.getString("name"), "Continue", R.drawable.ic_launcher, task.getUpdatedAt().getTime(), task.getObjectId()});
				++i;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			cursor = null;
		}
		
		// Add a row for starting a new task, with lowest priority
		cursor.addRow(new Object[] {i, q, "Start new task", R.drawable.ic_launcher, 0, new String()});
		
		Log.i(TAG, "Found " + cursor.getCount() + " matching tasks");
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
