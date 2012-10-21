package com.noughmad.ntasks.tasks;

import com.noughmad.ntasks.Database;
import com.noughmad.ntasks.R;
import com.noughmad.ntasks.Utils;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class TaskListAdapter extends CursorAdapter {
	
	private final static String TAG = "TaskListAdapter";

	public TaskListAdapter(Context context, Cursor c) {
		super(context, c, false);
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {

		// Columns: _id, name, status, duration, active, lastStart
		final long taskId = cursor.getLong(0);
		final Uri taskUri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME), taskId);
		
		Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(cursor.getInt(2));
		Log.d(TAG, "bindView: " + cursor.getInt(2));
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> spinner, View item,
					int position, long id) {
				Log.d(TAG, "onItemSelected: " + position);
				ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(taskUri);
				ContentValues values = new ContentValues();
				values.put(Database.KEY_TASK_STATUS, position);
				try {
					client.update(taskUri, values, null, null);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				client.release();
			}

			public void onNothingSelected(AdapterView<?> spinner) {
				
			}
		});
		((TextView)view.findViewById(R.id.task_name)).setText(cursor.getString(1));
				
		final boolean active = cursor.getInt(4) != 0;
		ImageButton button = (ImageButton) view.findViewById(R.id.task_track_button);
		if (active) {
			button.setImageResource(android.R.drawable.ic_media_pause);
			// view.setBackgroundResource(R.drawable.list_selector_background_selected);
		} else {
			button.setImageResource(android.R.drawable.ic_media_play);
			// view.setBackgroundResource(android.R.drawable.list_selector_background);
		}
		
		view.setActivated(active);
		button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (active) {
					Utils.stopTracking(context);
				} else {
					Utils.startTracking(taskId, context);
				}
			}
		});
		
		TextView durationView = (TextView)view.findViewById(R.id.task_duration);
		durationView.setText(Utils.formatDuration(cursor.getLong(3))); 
		
		boolean showDuration = context.getResources().getBoolean(R.bool.task_item_show_duration); 
		durationView.setVisibility(showDuration ? View.VISIBLE : View.GONE);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.d(TAG, "Creating a new task item view");
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_item, null, false);
		
		((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
		spinner.setAdapter(new TaskStatusAdapter(context));

		((LinearLayout)view.findViewById(R.id.task_item_layout)).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		((LinearLayout)view.findViewById(R.id.task_item_layout)).setDividerPadding(8);
		return view;
	}
}
