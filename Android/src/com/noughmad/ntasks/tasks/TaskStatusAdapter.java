package com.noughmad.ntasks.tasks;

import com.noughmad.ntasks.Utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TaskStatusAdapter extends BaseAdapter {
	
	private Context mContext;

	TaskStatusAdapter(Context context) {
		mContext = context;
	}

	public int getCount() {
		return 4;
	}

	public Object getItem(int position) {
		return Utils.taskStatuses[position];
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.simple_spinner_item, parent, false);
		}
		((TextView)view).setText(Utils.taskStatuses[position]);
		((TextView)view).setTextColor(Color.WHITE);
		view.setBackgroundColor(Utils.statusColor(position));
		return view;
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
		}
		((TextView)view).setText(Utils.taskStatuses[position]);
		((TextView)view).setTextColor(Color.WHITE);
		view.setBackgroundColor(Utils.statusColor(position));
		return view;
	}
}
