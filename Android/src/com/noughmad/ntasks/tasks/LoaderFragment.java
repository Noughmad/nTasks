package com.noughmad.ntasks.tasks;

import com.noughmad.ntasks.Database;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;

public abstract class LoaderFragment extends ListFragment
		implements LoaderManager.LoaderCallbacks<Cursor>
{

	final static int PROJECT_LOADER_ID = 0;
	final static int LIST_LOADER_ID = 1;
	
	int mProjectId;

	CursorLoader onCreateProjectLoader() {
		Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME), mProjectId);
		return new CursorLoader(getActivity(), uri, Database.projectColumns, null, null, null);
	}
	
	abstract CursorLoader onCreateListLoader();

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case PROJECT_LOADER_ID:
			return onCreateProjectLoader();
			
		case LIST_LOADER_ID:
			return onCreateListLoader();
		}
		
		return null;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == PROJECT_LOADER_ID) {
			
		} else {
			((CursorAdapter)getListAdapter()).swapCursor(cursor);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == PROJECT_LOADER_ID) {
		
		} else {
			((CursorAdapter)getListAdapter()).swapCursor(null);
		}
	}
}
