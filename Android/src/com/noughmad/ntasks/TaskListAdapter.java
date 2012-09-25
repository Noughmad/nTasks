package com.noughmad.ntasks;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorTreeAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class TaskListAdapter extends CursorTreeAdapter
	implements LoaderManager.LoaderCallbacks<Cursor> {
	

	private final static String TAG = "TaskListAdapter";
	
	
	// Zero is reserved for children
	private final static int VIEW_TYPE_HEADER = 1;
	private final static int VIEW_TYPE_TASK = 2;
	private final static int VIEW_TYPE_COUNT = 3;
	
	private final static int CHILD_TYPE_NOTE = 1;
	private final static int CHILD_TYPE_ADD_NOTE = 2;
	private final static int CHILD_TYPE_COUNT = 3;
	
	private Activity mActivity;
	private Map<Integer, Cursor> mChildrenCursors;
	
	public TaskListAdapter(Cursor cursor, Activity activity) {
		super(cursor, activity);
		mActivity = activity;
		mChildrenCursors = new HashMap<Integer,Cursor>();
	}


	@Override
	protected void bindChildView(View view, Context context, Cursor cursor,
			boolean isLastChild) {
		TextView textView = (TextView)view.findViewById(R.id.note_text);
		textView.setText(cursor.getString(1));		
	}

	@Override
	protected void bindGroupView(View view, final Context context, Cursor cursor,
			boolean isExpanded) {
		
		// Columns: _id, name, status, duration, active, lastStart
		final long taskId = cursor.getLong(0);
		final Uri taskUri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME), taskId);
		
		Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
		spinner.setSelection(cursor.getInt(2));
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> spinner, View item,
					int position, long id) {
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
				
		ImageButton button = (ImageButton) view.findViewById(R.id.task_track_button);
		if (cursor.getInt(4) != 0) {
			button.setImageResource(android.R.drawable.ic_media_pause);
			// view.setBackgroundResource(R.drawable.list_selector_background_selected);
		} else {
			button.setImageResource(android.R.drawable.ic_media_play);
			// view.setBackgroundResource(android.R.drawable.list_selector_background);
		}
		
		final boolean active = cursor.getInt(4) != 0;
		view.setActivated(active);
		button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(taskUri);
				if (active) {
					Utils.stopTracking(context);
				} else {
					Utils.startTracking(taskId, context);
				}
				client.release();
			}
		});
		
		TextView durationView = (TextView)view.findViewById(R.id.task_duration);
		durationView.setText(Utils.formatDuration(cursor.getLong(3))); 
		
		boolean showDuration = context.getResources().getBoolean(R.bool.task_item_show_duration); 
		durationView.setVisibility(showDuration ? View.VISIBLE : View.GONE);		
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		int pos = groupCursor.getPosition();
		if (mChildrenCursors.containsKey(pos) && mChildrenCursors.get(pos) != null) {
			return mChildrenCursors.get(pos);
		} else {
			Bundle args = new Bundle();
			args.putLong("taskId", groupCursor.getLong(0));
			mActivity.getLoaderManager().initLoader(groupCursor.getPosition(), args, this);
			return null;
		}
	}

	@Override
	protected View newChildView(final Context context, Cursor cursor,
			boolean isLastChild, ViewGroup parent) {
		Log.d(TAG, "Creating a new note view");
		
		// _id, text, task
		
		final long noteId = cursor.getLong(0);
		
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.note_item, parent, false);
		view.findViewById(R.id.note_delete_button).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Really delete this note?");
				builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Uri noteUri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.NOTE_TABLE_NAME), noteId);
						ContentProviderClient client = mActivity.getContentResolver().acquireContentProviderClient(noteUri);
						try {
							client.delete(noteUri, null, null);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						client.release();
					}
				});
				builder.create().show();
			}
		});
		bindChildView(view, context, cursor, isLastChild);
		return view;
	}

	@Override
	protected View newGroupView(Context context, Cursor cursor,
			boolean isExpanded, ViewGroup parent) {
		Log.d(TAG, "Creating a new task item view");
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_item, null, false);
		
		((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
		spinner.setAdapter(new TaskStatusAdapter(context));

		((LinearLayout)view.findViewById(R.id.task_item_layout)).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		((LinearLayout)view.findViewById(R.id.task_item_layout)).setDividerPadding(8);
		bindGroupView(view, context, cursor, isExpanded);
		return view;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.i(TAG, "Creating cursor for id " + id);
		CursorLoader loader = new CursorLoader(mActivity);
		loader.setUri(Uri.withAppendedPath(Database.BASE_URI, Database.NOTE_TABLE_NAME));
		loader.setProjection(new String[] {Database.ID, Database.KEY_NOTE_TEXT, Database.KEY_NOTE_TASK});
		loader.setSelection(Database.KEY_NOTE_TASK + " = ?");
		loader.setSelectionArgs(new String[] {Long.toString(args.getLong("taskId"))});
		return loader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i(TAG, "Loaded cursor for id " + loader.getId());
		mChildrenCursors.put(loader.getId(), cursor);
		setChildrenCursor(loader.getId(), cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		
	}
	
	
}