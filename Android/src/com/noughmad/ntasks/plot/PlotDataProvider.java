package com.noughmad.ntasks.plot;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.noughmad.ntasks.Database;

public class PlotDataProvider extends ContentProvider {

	private Database db;
	
	private final static String sAuth = "com.noughmad.ntasks.plot.provider";
	private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sUriMatcher.addURI(sAuth, "project/#", 1);
		sUriMatcher.addURI(sAuth, "", 2);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		Log.i("PlotDataProvider", "getType(): " + uri);
		return "vnd.android.cursor.dir/vnd.com.googlecode.chartdroid.graphable";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		db = new Database(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.i("PlotDataProvider", "Query: " + uri);
		String type = uri.getQueryParameter("aspect");
		if (type == null || type.equals("data")) {
			String[] columns = new String[] {
					Database.PROJECT_TABLE_NAME + "." + Database.ID + " AS _id",
					"0 AS COLUMN_SERIES_INDEX",
					Database.PROJECT_TABLE_NAME + "." + Database.KEY_PROJECT_TITLE + " AS COLUMN_DATUM_LABEL", 
					"rowid AS AXIS_A", 
					"(SELECT SUM(" + Database.KEY_TASK_DURATION + ") FROM " + Database.TASK_TABLE_NAME + 
					" WHERE " + Database.KEY_TASK_PROJECT + " = " + Database.PROJECT_TABLE_NAME + "." + Database.ID + ") AS AXIS_B"};
			Log.i("PlotDataProvider", "Query: " + columns[0] + "," + columns[1] + "," + columns[2]);
			Cursor cursor = db.getReadableDatabase().query(Database.PROJECT_TABLE_NAME, columns, null, null, null, null, sortOrder);
			Log.d("PlotDataProvider", "Found " + cursor.getCount() + " projects");
			return cursor;
		} else if (type.equals("series")) {
			MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "COLUMN_SERIES_LABEL"});
			cursor.addRow(new Object[] {1, "Projects"});
			return cursor;
		} else if (type.equals("axes")) {
			String[] columns = new String[] {"COLUMN_AXIS_LABEL"};
			MatrixCursor cursor = new MatrixCursor(columns);
			cursor.addRow(new String[] {"Project"});
			cursor.addRow(new String[] {"Time spent"});
			return cursor;
		}
		
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
