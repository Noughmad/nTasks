package com.noughmad.ntasks;

import java.util.Arrays;
import java.util.List;

import com.noughmad.ntasks.sync.Bridge;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class TasksProvider extends ContentProvider {
	
	private final static String TAG = "ContentProvider";
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final String sAuth = "com.noughmad.ntasks.provider";
	private static final ContentValues localValues = new ContentValues();
	
	static {
		sUriMatcher.addURI(sAuth, Database.PROJECT_TABLE_NAME, 1);
		sUriMatcher.addURI(sAuth, Database.PROJECT_TABLE_NAME + "/#", 2);

		sUriMatcher.addURI(sAuth, Database.TASK_TABLE_NAME, 3);
		sUriMatcher.addURI(sAuth, Database.TASK_TABLE_NAME + "/#", 4);

		sUriMatcher.addURI(sAuth, Database.NOTE_TABLE_NAME, 5);
		sUriMatcher.addURI(sAuth, Database.NOTE_TABLE_NAME + "/#", 6);

		sUriMatcher.addURI(sAuth, Database.WORKUNIT_TABLE_NAME, 7);
		sUriMatcher.addURI(sAuth, Database.WORKUNIT_TABLE_NAME + "/#", 8);


		sUriMatcher.addURI(sAuth, Database.PROJECT_TABLE_NAME + "/#/" + Database.TASK_TABLE_NAME, 9);
		sUriMatcher.addURI(sAuth, Database.TASK_TABLE_NAME + "/#/" + Database.NOTE_TABLE_NAME, 10);
		sUriMatcher.addURI(sAuth, Database.TASK_TABLE_NAME + "/#/" + Database.WORKUNIT_TABLE_NAME, 11);

		sUriMatcher.addURI(sAuth, Database.IS_LOCAL + "/*", 12);
		
		sUriMatcher.addURI(sAuth, Database.TIMELINE, 14);
		sUriMatcher.addURI(sAuth, Database.TIMELINE + "/#", 15);

		sUriMatcher.addURI(sAuth, "*/" + Database.EXISTS_PARSE_ID + "/*", 13);
		
		localValues.put(Bridge.KEY_OBJECT_LOCAL, 1);
	}
	
	private Database db;
	
	@Override
	public boolean onCreate() {
		Log.i(TAG, "Creating");
		db = new Database(getContext());
		return true;
	} 
	
	String getTableName(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case 1:
		case 2:
			return Database.PROJECT_TABLE_NAME;
			
		case 3:
		case 4:
			return Database.TASK_TABLE_NAME;
			
		case 5:
		case 6:
			return Database.NOTE_TABLE_NAME;
			
		case 7:
		case 8:
			return Database.WORKUNIT_TABLE_NAME;
		}
		
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.i(TAG, "Delete: " + uri);
		String tableName = getTableName(uri);
		long id = ContentUris.parseId(uri);
		String objectSelection = Bridge.OBJECT_TABLE_NAME + "." + Database.ID + 
				" IN (SELECT " + Database.KEY_OBJECT + " FROM " + tableName + " WHERE " + Database.ID + " = ?)";
		
		int count = db.getWritableDatabase().delete(Bridge.OBJECT_TABLE_NAME, objectSelection, new String[] {Long.toString(id)});
		Log.i(TAG, "Deleted " + count + " records with query " + objectSelection + " and args " + id);
		
		getContext().getContentResolver().notifyChange(Uri.withAppendedPath(Database.BASE_URI, tableName), null);
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		List<String> path = uri.getPathSegments();
		switch (path.size()) {
		case 1: 
			return "vnd.android.cursor.dir/vnd.com.noughmad.ntasks.provider." + path.get(0);
		case 2:
			return "vnd.android.cursor.item/vnd.com.noughmad.ntasks.provider." + path.get(0);
		}
		
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.i(TAG, "Insert: " + uri);
		String table = getTableName(uri);
		
		// TODO: Create a new Object as well
		ContentValues objectValues = new ContentValues();
		objectValues.put(Bridge.KEY_OBJECT_LOCAL, 1);
		objectValues.put(Bridge.KEY_OBJECT_CLASSNAME, table);
		long objectId = db.getWritableDatabase().insert(Bridge.OBJECT_TABLE_NAME, null, objectValues);
		
		values.put(Database.KEY_OBJECT, objectId);
		long id = db.getWritableDatabase().insert(table, null, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(uri, id);
	}
	
	private Cursor queryLocal(String className, String[] columns) {
		String table = className + " JOIN " + Bridge.OBJECT_TABLE_NAME + " ON " + className + "." + Database.KEY_OBJECT + " = " + Bridge.OBJECT_TABLE_NAME + "." + Database.ID;
		String selection = Bridge.OBJECT_TABLE_NAME + "." + Bridge.KEY_OBJECT_LOCAL + " = 1";
		return db.getReadableDatabase().query(table, columns, selection, null, null, null, null);
	}
	
	private Cursor queryParseId(List<String> pathSegments, String[] columns) {
		String className = pathSegments.get(0);
		String table = className + " JOIN " + Bridge.OBJECT_TABLE_NAME + " ON " + className + "." + Database.KEY_OBJECT + " = " + Bridge.OBJECT_TABLE_NAME + "." + Database.ID;
		String selection = Bridge.OBJECT_TABLE_NAME + "." + Bridge.KEY_OBJECT_PARSEID + " = ?";
		String[] args = new String[] {pathSegments.get(2)};
		return db.getReadableDatabase().query(table, columns, selection, args, null, null, null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		int match = sUriMatcher.match(uri);
		Log.i(TAG, "Query: " + uri + " => " + match);
		
		if (match == 12) {
			// Objects with local changes
			Cursor cursor = queryLocal(uri.getLastPathSegment(), projection);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		} else if (match == 13) {
			// Check if a record with parseId exists
			Cursor cursor = queryParseId(uri.getPathSegments(), projection);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		} else if (match == 14 || match == 15) {
			String sel = null;
			String[] args = null;
			if (match == 15) {
				sel = Database.KEY_TASK_PROJECT + " = ?";
				args = new String[] {uri.getLastPathSegment()};
			}
			
			Cursor cursor = db.getReadableDatabase().query(Database.TIMELINE, null, sel, args, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;
		}
		
		if (match == 2 || match == 4 || match == 6 || match == 8) {
			if (selection == null) {
				selection = Database.ID + " = ?";
			} else {
				selection = selection + " AND " + Database.ID + " = ?";
			}
			if (selectionArgs == null) {
				selectionArgs = new String[] {uri.getLastPathSegment()};
			} else {
				int n = selectionArgs.length;
				selectionArgs = Arrays.copyOf(selectionArgs, n + 1);
				selectionArgs[n] = uri.getLastPathSegment();
			}
		}
		Log.d(TAG, "Query: " + getTableName(uri) + ", " + selection);
		if (selectionArgs != null && selectionArgs.length > 0) {
			Log.d(TAG, "Query: " + selectionArgs[0]);
		}
		Cursor cursor = db.getReadableDatabase().query(getTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.i(TAG, "Update: " + uri);
		int match = sUriMatcher.match(uri);
		if (match == 2 || match == 4 || match == 6 || match == 8) {
			if (selection == null) {
				selection = Database.ID + " = ?";
			} else {
				selection = selection + " AND " + Database.ID + " = ?";
			}
			if (selectionArgs == null) {
				selectionArgs = new String[] {uri.getLastPathSegment()};
			} else {
				int n = selectionArgs.length;
				selectionArgs = Arrays.copyOf(selectionArgs, n + 1);
				selectionArgs[n] = uri.getLastPathSegment();
			}
		}
		int count = db.getWritableDatabase().update(getTableName(uri), values, selection, selectionArgs);
		setLocal(uri);
		getContext().getContentResolver().notifyChange(Uri.withAppendedPath(Database.BASE_URI, getTableName(uri)), null);
		return count;
	}

	private void setLocal(Uri uri) {
		String tableName = getTableName(uri);
		long id = ContentUris.parseId(uri);
		String selection = Bridge.OBJECT_TABLE_NAME + "." + Database.ID + 
				" IN (SELECT " + Database.KEY_OBJECT + " FROM " + tableName + " WHERE " + Database.ID + " = ?)";
		
		db.getWritableDatabase().update(Bridge.OBJECT_TABLE_NAME, localValues, selection, new String[] {Long.toString(id)});
	}
	
	

}
