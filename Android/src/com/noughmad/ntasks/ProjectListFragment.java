package com.noughmad.ntasks;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ProjectListFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private ProjectListAdapter mAdapter;
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (getResources().getBoolean(R.bool.two_pane_layout)) {
			ProjectDetailFragment fragment = (ProjectDetailFragment) getActivity().getFragmentManager().findFragmentById(R.id.project_detail);
			fragment.showProject(id);
		} else {
			Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
			intent.putExtra("projectId", id);
			startActivity(intent);
		}
	}
	
	public void onListItemLongClick(ListView l, View v, int position, final long id) {
		new AlertDialog.Builder(getActivity()).setItems(R.array.project_actions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: // Edit
					FragmentTransaction ft = getFragmentManager().beginTransaction();
				    ProjectDialogFragment newFragment = ProjectDialogFragment.create(id);
				    newFragment.show(ft, "project-dialog");
					break;
				case 1: // Change icon
					((IconGetterActivity)getActivity()).getIconForProject(id);
					break;

					
				case 2: // Delete
					String[] columns = new String[] {Database.KEY_PROJECT_TITLE};
					Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME), id);
					ContentProviderClient client = getActivity().getContentResolver().acquireContentProviderClient(uri);
					Cursor cursor;
					try {
						cursor = client.query(uri, columns, null, null, null);
					} catch (RemoteException e1) {
						e1.printStackTrace();
						return;
					}
					if (!cursor.moveToFirst()) {
						return;
					}
					String title = cursor.getString(0);
					client.release();
					
					new AlertDialog.Builder(getActivity())
						.setTitle("Really delete " + title + "?")
						.setMessage("Deleting a project will delete all its tasks, and cannot be undone. Do you really want to delete " + title + "?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Uri uri = Uri.withAppendedPath(Database.BASE_URI, Uri.encode(Database.PROJECT_TABLE_NAME));
								uri = ContentUris.withAppendedId(uri, id);
								ContentProviderClient client = getActivity().getContentResolver().acquireContentProviderClient(uri);
								try {
									client.delete(uri, null, null);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
								client.release();
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create().show();
				}
			}
		}).create().show();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
				
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemLongClick((ListView) parent, view, position, id);
			    return true;
			}});
		
		mAdapter = new ProjectListAdapter(getActivity(), null);
		setListAdapter(mAdapter);
		
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.i("ProjectListFragment", "Creating loader for " + Uri.withAppendedPath(Database.BASE_URI, Uri.encode(Database.PROJECT_TABLE_NAME)));
		return new CursorLoader(getActivity(), Uri.withAppendedPath(Database.BASE_URI, Uri.encode(Database.PROJECT_TABLE_NAME)), Database.projectColumns, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		
		Log.i("ProjectListFragment", "Loaded " + cursor.getCount() + " projects");
		mAdapter.swapCursor(cursor);
		
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}
}
