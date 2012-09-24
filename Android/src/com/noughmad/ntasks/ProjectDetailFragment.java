package com.noughmad.ntasks;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentUris;
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
import android.widget.ExpandableListView;

public class ProjectDetailFragment extends Fragment 
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private long mProjectId;
	private TaskListAdapter mAdapter;

	public static ProjectDetailFragment create(long projectId) {
		ProjectDetailFragment f = new ProjectDetailFragment();
		f.mProjectId = projectId;
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey("projectId")) {
			mProjectId = savedInstanceState.getLong("projectId");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("projectId", mProjectId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ProjectDetailFragment", "onCreateView(): " + mAdapter);
		ExpandableListView v = new ExpandableListView(inflater.getContext());
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d("ProjectDetailFragment", "onActivityCreated(): " + mAdapter);
		
		
		mAdapter = new TaskListAdapter(null, getActivity());
		getListView().setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);

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
						case 1: // Add Note
							Utils.addNote(id, getActivity());
							break;
						case 2: // Delete
							Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME), id);
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
	}
	
	ExpandableListView getListView() {
		return (ExpandableListView) getView();
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME);

		// Columns: _id, name, status, duration, active, lastStart
		String[] projection = new String[] {Database.ID, Database.KEY_TASK_NAME, 
				Database.KEY_TASK_STATUS, Database.KEY_TASK_DURATION, 
				Database.KEY_TASK_ACTIVE, Database.KEY_TASK_LASTSTART};
		
		String selection = Database.KEY_TASK_PROJECT + " = ?";
		String[] selectionArgs = new String[] {Long.toString(mProjectId)};
		
		return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i("ProjectDetailFragment", "Loaded cursor: " + cursor);
		mAdapter.changeCursor(cursor);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
