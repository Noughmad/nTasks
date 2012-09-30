package com.noughmad.ntasks.tasks;

import com.noughmad.ntasks.Utils;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class TimelineAdapter extends CursorAdapter {

	public TimelineAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((TextView)view.findViewById(android.R.id.text1)).setText(cursor.getString(1));
		((TextView)view.findViewById(android.R.id.text1)).setText(Utils.formatDuration(cursor.getLong(5)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.two_line_list_item, parent, false);
		bindView(view, context, cursor);
		return view;
	}
}
