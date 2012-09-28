package com.noughmad.ntasks.tasks;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;
import android.widget.WrapperListAdapter;

import com.noughmad.ntasks.Database;

public class TaskTreeAdapter extends AdapterTreeAdapter
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private final static String TAG = "TaskTreeAdapter";
	private Context mContext;
	private long mProjectId;
	private ListView mListView;
	
	public TaskTreeAdapter(Context context, long projectId, ListView listView) {
		super(context, new HeaderViewListAdapter(null, null, new TaskListAdapter(context, null)));
		mContext = context;
		mListView = listView;
		setProject(projectId);
	}
	
	@Override
	public ListAdapter newChildrenAdapter(Context context, int groupPosition) {
		NoteListAdapter adapter = new NoteListAdapter(context, null);
		
		ListView.FixedViewInfo footer = mListView.new FixedViewInfo();
		footer.isSelectable = false;
		footer.data = null;
		Button button = new Button(context);
		button.setText("Add Note");
		footer.view = button;
		ArrayList<FixedViewInfo> infos = new ArrayList<FixedViewInfo>();
		infos.add(footer);
		HeaderViewListAdapter headerAdapter = new HeaderViewListAdapter(null, infos, adapter);
		((Activity)context).getLoaderManager().initLoader(groupPosition, null, this);
		return headerAdapter;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.i(TAG, "Creating cursor for id " + id);
		CursorLoader loader = new CursorLoader(mContext);
		if (id < 0) {
			loader.setUri(Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME));
			// Columns: _id, name, status, duration, active, lastStart
			loader.setProjection(new String[] {Database.ID, Database.KEY_TASK_NAME, 
					Database.KEY_TASK_STATUS, Database.KEY_TASK_DURATION, 
					Database.KEY_TASK_ACTIVE, Database.KEY_TASK_LASTSTART});
			loader.setSelection(Database.KEY_TASK_PROJECT + " = ?");
			loader.setSelectionArgs(new String[] {Long.toString(mProjectId)});
		} else {
			loader.setUri(Uri.withAppendedPath(Database.BASE_URI, Database.NOTE_TABLE_NAME));
			loader.setProjection(new String[] {Database.ID, Database.KEY_NOTE_TEXT, Database.KEY_NOTE_TASK});
			loader.setSelection(Database.KEY_NOTE_TASK + " = ?");
			loader.setSelectionArgs(new String[] {Long.toString(getGroupId(id))});			
		}
		return loader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i(TAG, "Loaded cursor for id " + loader.getId());
		ListAdapter adapter;
		if (loader.getId() < 0) {
			adapter = getGroupAdapter();
		} else {
			adapter = getChildrenAdapter(loader.getId());
		}
		((CursorAdapter)((WrapperListAdapter)adapter).getWrappedAdapter()).swapCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		
	}

	public void setProject(long id) {
		mProjectId = id;
		((Activity)mContext).getLoaderManager().initLoader(-1, null, this);

	}	
}