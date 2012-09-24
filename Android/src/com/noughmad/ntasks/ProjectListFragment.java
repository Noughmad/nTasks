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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ProjectListFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private static String[] from = new String[] {"title", "client", "category"};
	private static int[] to = new int[] {R.id.project_title, R.id.project_client, R.id.project_image};
	
	private SimpleCursorAdapter mAdapter;
	
	private static class ProjectItemBinder implements SimpleCursorAdapter.ViewBinder {

		private final static int PROJECT_CATEGORY_COLUMN_INDEX = 3;

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			switch (columnIndex) {
			case PROJECT_CATEGORY_COLUMN_INDEX:
				((ImageView)view).setImageResource(Utils.getLargeCategoryDrawable(cursor.getInt(columnIndex)));
				return true;			
			}
			return false;
		}
		
	}
	
	public void updateProjectList()
	{
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
		intent.putExtra("projectId", id);
		startActivity(intent);
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
				case 1: // Delete
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

		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.project_item, null, from, to, 0);
		mAdapter.setViewBinder(new ProjectItemBinder());
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
