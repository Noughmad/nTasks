package com.noughmad.ntasks;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;


public class TimelineFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private long mProjectId;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setListShown(false);
		
		ListView.FixedViewInfo header = getListView().new FixedViewInfo();
		header.data = null;
		header.isSelectable = false;
		EditText edit = new EditText(getActivity());
		edit.setText("Recent actions");
		
		HeaderViewListAdapter adapter = new HeaderViewListAdapter(
				(ArrayList<FixedViewInfo>) Arrays.asList(header), null, null);
		this.setListAdapter(adapter);
		
		getLoaderManager().initLoader(0, null, this);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader loader = new CursorLoader(getActivity());
		Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.TIMELINE);
		if (mProjectId > -1) {
			uri = ContentUris.withAppendedId(uri, mProjectId);
		}
		loader.setUri(uri);
		loader.setProjection(Database.workUnitColumns);
		return loader;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		((CursorAdapter)((HeaderViewListAdapter)getListAdapter()).getWrappedAdapter()).swapCursor(null);		
		
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		((CursorAdapter)((HeaderViewListAdapter)getListAdapter()).getWrappedAdapter()).swapCursor(null);		
	}
}
