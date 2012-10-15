package com.noughmad.ntasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.parse.ParseObject;

public class Utils {
	private final static String TAG = "Utils";
	
	public static int TRACKING_NOTIFICATION = 4;
	public static int SYNC_NOTIFICATION = 6;
	
	public static List<ParseObject> projects;
	public static ParseObject activeTask = null;
	public static String[] taskStatuses;
	
	public static int[] statusColors = new int[] {
		Color.parseColor("#bd362f"),
		Color.parseColor("#f89406"),
		Color.parseColor("#2f96b4"),
		Color.parseColor("#51a351")
	};
	
	public static int statusColor(int status)
	{
		if (status > -1 && status < 4) {
			return statusColors[status];
		} else {
			return Color.TRANSPARENT;
		}
	}

	public static CharSequence formatDuration(long duration) {
		if (duration < 1000 * 60 * 60 * 24) {
			// Show minutes for durations shorter than a day, otherwise show only hours
			SimpleDateFormat format = new SimpleDateFormat("H 'h' mm 'min'");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			return format.format(new Date(duration));
		} else {
			return Long.toString(duration / 1000 / 60 / 60) + " h";
		}
	}

	public static int[] categoryDrawables = new int[] {
		R.drawable.ic_category_office,
		R.drawable.ic_category_school,
		R.drawable.ic_category_hobby,
		R.drawable.ic_category_sport,
		R.drawable.ic_category_other
	};
	
	public static int[] largeCategoryDrawables = new int[] {
		R.drawable.ic_category_large_office,
		R.drawable.ic_category_large_school,
		R.drawable.ic_category_large_hobby,
		R.drawable.ic_category_large_sport,
		R.drawable.ic_category_large_other
	};

	public static int getCategoryDrawable(int position) {
		return categoryDrawables[Math.max(Math.min(position, 4), 0)];
	}
	
	public static int getLargeCategoryDrawable(int position) {
		return largeCategoryDrawables[Math.max(Math.min(position, 4), 0)];
	}
	
	public static void addNote(final long id, final Context context) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.note_add, null, false);
		builder.setView(view);
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				EditText edit = (EditText) view;
				if (edit.getText().toString().isEmpty()) {
					return;
				}
				
				ContentValues values = new ContentValues();
				values.put(Database.KEY_NOTE_TEXT, edit.getText().toString());
				values.put(Database.KEY_NOTE_TASK, id);
				
				Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.NOTE_TABLE_NAME);
				ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(uri);
				try {
					client.insert(uri, values);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				client.release();
			}
		});
		
		builder.create().show();
	}
	
	public static void startTracking(long taskId, Context context) {
		Log.i(TAG, "startTracking(): " + taskId);
		
		stopTracking(context);
		
		Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME), taskId);
		ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(uri);
		ContentValues values = new ContentValues();
		values.put(Database.KEY_TASK_ACTIVE, 1);
		values.put(Database.KEY_TASK_LASTSTART, System.currentTimeMillis());
		try {
			client.update(uri, values, null, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		Intent intent = new Intent(context.getApplicationContext(), TimeTrackingService.class);
		intent.putExtra("taskId", taskId);
		context.startService(intent);
	}
	
	public static void stopTracking(Context context) {
		Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME);
		ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(uri);
		try {
			Cursor cursor = client.query(uri, new String[] {Database.ID, Database.KEY_TASK_LASTSTART}, "active = 1", null, null);
			if (cursor.moveToFirst()) {
				stopTracking(cursor.getLong(0), cursor.getLong(1), context);
				cursor.close();
				client.release();
				return;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		client.release();
	}
	
	public static void stopTracking(long taskId, long lastStart, Context context) {
		Log.i(TAG, "stopTracking(): " + taskId);
		ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(Database.BASE_URI);
		ContentValues unitValues = new ContentValues();
		long currentTime = System.currentTimeMillis();
		
		unitValues.put(Database.KEY_WORKUNIT_START, lastStart);
		unitValues.put(Database.KEY_WORKUNIT_END, currentTime);
		unitValues.put(Database.KEY_WORKUNIT_DURATION, currentTime - lastStart);
		Uri unitUri = Uri.withAppendedPath(Database.BASE_URI, Database.WORKUNIT_TABLE_NAME);
		try {
			client.insert(unitUri, unitValues);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		Uri taskUri = Database.withId(Database.TASK_TABLE_NAME, taskId);

		ContentValues values = new ContentValues();
		values.put(Database.KEY_TASK_ACTIVE, 0);
		try {
			client.update(taskUri, values, null, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		Intent intent = new Intent(context.getApplicationContext(), TimeTrackingService.class);
		context.stopService(intent);
	}

	public static int getTaskStatusDrawable(int position) {
		switch (position) {
		case 0:
			return android.R.drawable.ic_dialog_alert;
		case 1:
			return android.R.drawable.ic_menu_agenda;
		case 2:
			return android.R.drawable.ic_menu_recent_history;
		case 3:
			return R.drawable.ic_menu_mark;
		}
		return 0;
	}

	public static void addTask(final long projectId, final Activity activity) {

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.add_task);
		
		final View view = activity.getLayoutInflater().inflate(R.layout.task_add, null, false);
		builder.setView(view);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();	
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				EditText edit = (EditText) view;
				if (edit.getText().toString().isEmpty()) {
					return;
				}
				
				Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME);
				ContentProviderClient client = activity.getContentResolver().acquireContentProviderClient(uri);
				Log.i(TAG, "Creating a task in project " + projectId);
				ContentValues values = new ContentValues();
				values.put(Database.KEY_TASK_PROJECT, projectId);
				values.put(Database.KEY_TASK_NAME, edit.getText().toString());
				values.put(Database.KEY_TASK_STATUS, 1);
				try {
					client.insert(uri, values);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				client.release();
			}
		});
		builder.create().show();
	}
}
