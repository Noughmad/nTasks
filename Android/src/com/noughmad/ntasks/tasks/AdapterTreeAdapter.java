package com.noughmad.ntasks.tasks;

import java.util.Map;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ListAdapter;

public abstract class AdapterTreeAdapter extends BaseExpandableListAdapter {
	
	private SparseArray<ListAdapter> mChildAdapters;
	private ListAdapter mGroupAdapter;
	private Context mContext;

	private class TreeObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			notifyDataSetInvalidated();
		}
	}
	
	private TreeObserver mObserver;
	
	public AdapterTreeAdapter(Context context, ListAdapter groupAdapter) {
		mContext = context;
		mGroupAdapter = groupAdapter;
		mChildAdapters = new SparseArray<ListAdapter>();
		
		mObserver = new TreeObserver();
		mGroupAdapter.registerDataSetObserver(mObserver);
	}
	
	public abstract ListAdapter newChildrenAdapter(Context context, int groupPosition);
	
	public ListAdapter getChildrenAdapter(int groupPosition) {
		if (mChildAdapters.get(groupPosition) != null) {
			return mChildAdapters.get(groupPosition);
		} else {
			ListAdapter adapter = newChildrenAdapter(mContext, groupPosition);
			mChildAdapters.put(groupPosition, adapter);
			adapter.registerDataSetObserver(mObserver);
			return adapter;
		}
	}
	
	public ListAdapter getGroupAdapter() {
		return mGroupAdapter;
	}

	public Object getChild(int groupPosition, int childPosition) {
		return getChildrenAdapter(groupPosition).getItem(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return getChildrenAdapter(groupPosition).getItemId(childPosition);
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		return getChildrenAdapter(groupPosition).getView(childPosition, convertView, parent);	
	}

	public int getChildrenCount(int groupPosition) {
		return getChildrenAdapter(groupPosition).getCount();	
	}

	public Object getGroup(int groupPosition) {
		return mGroupAdapter.getItem(groupPosition);
	}

	public int getGroupCount() {
		return mGroupAdapter.getCount();
	}

	public long getGroupId(int groupPosition) {
		return mGroupAdapter.getItemId(groupPosition);
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		return mGroupAdapter.getView(groupPosition, convertView, parent);
	}

	public boolean hasStableIds() {
		return mGroupAdapter.hasStableIds();
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return getChildrenAdapter(groupPosition).isEnabled(childPosition);
	}
}
