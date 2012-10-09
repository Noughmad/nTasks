package com.noughmad.ntasks;

import java.util.ArrayList;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ListView.FixedViewInfo;
import android.widget.TextView;

import com.noughmad.ntasks.tasks.TimelineAdapter;


public class TimelineFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		v.setBackgroundResource(android.R.color.holo_green_light);
		return v;
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("projectId", mProjectId);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setListShown(false);
		
		ListView.FixedViewInfo header = getListView().new FixedViewInfo();
		header.data = null;
		header.isSelectable = false;
		TextView edit = new TextView(getActivity());
		edit.setText("Recent actions");
		header.view = edit;
		
		ArrayList<FixedViewInfo> headers = new ArrayList<FixedViewInfo>();
		headers.add(header);
		HeaderViewListAdapter adapter = new HeaderViewListAdapter(headers, null, new TimelineAdapter(getActivity(), null));
		setListAdapter(adapter);
		
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
		((CursorAdapter)((HeaderViewListAdapter)getListAdapter()).getWrappedAdapter()).swapCursor(cursor);		
		
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		((CursorAdapter)((HeaderViewListAdapter)getListAdapter()).getWrappedAdapter()).swapCursor(null);		
	}

	public void showProject(long id) {
		mProjectId = id;
		getLoaderManager().restartLoader(0,  null,  this);
	}
}
