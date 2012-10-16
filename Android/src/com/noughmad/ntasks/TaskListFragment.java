package com.noughmad.ntasks;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;

import com.noughmad.ntasks.tasks.TaskListAdapter;

public class TaskListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private long mProjectId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey("projectId")) {
			mProjectId = savedInstanceState.getLong("projectId");
		} else if (getArguments() != null) {
			mProjectId = getArguments().getLong("projectId", -1);
		} else {
			mProjectId = -1;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("projectId", mProjectId);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d("ProjectDetailFragment", "onActivityCreated(): " + mProjectId);
		
		setListAdapter(new TaskListAdapter(getActivity(), null));

		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
	
			public boolean onItemLongClick(AdapterView<?> view, View item,
					int position, final long id) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setItems(R.array.task_actions, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: // Rename
							// TODO;
							break;
						case 1: // Delete
							Uri uri = Database.withId(Database.TASK_TABLE_NAME, id);
							ContentProviderClient client = getActivity().getContentResolver().acquireContentProviderClient(uri);
							try {
								client.delete(uri, null, null);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
							client.release();
						}
					}
				});
				builder.create().show();
				return true;
			}
		});
		
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}
	
	public void showProject(long id) {
		setListShown(false);
		if (mProjectId < 0) {
			mProjectId = id;
			getLoaderManager().initLoader(0, null, this);
		} else {
			mProjectId = id;
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Database.withTable(Database.TASK_TABLE_NAME);
		// Columns: _id, name, status, duration, active, lastStart
		String[] columns = new String[] {
				Database.ID,
				Database.KEY_TASK_NAME,
				Database.KEY_TASK_STATUS,
				Database.KEY_TASK_DURATION,
				Database.KEY_TASK_ACTIVE,
				Database.KEY_TASK_LASTSTART
		};
		return new CursorLoader(getActivity(), uri, columns, Database.KEY_TASK_PROJECT + " = ?", new String[] {Long.toString(mProjectId)}, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		((CursorAdapter)getListAdapter()).swapCursor(cursor);		
		
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		((CursorAdapter)getListAdapter()).swapCursor(null);		
	}
}
