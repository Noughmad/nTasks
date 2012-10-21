package com.noughmad.ntasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProjectListAdapter extends CursorAdapter {

	public ProjectListAdapter(Context context, Cursor c) {
		super(context, c, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// Columns: _id, title, client, category, description, icon, object

		((TextView)view.findViewById(R.id.project_title)).setText(cursor.getString(1));
		((TextView)view.findViewById(R.id.project_client)).setText(cursor.getString(2));
		
		ImageView image = (ImageView) view.findViewById(R.id.project_image);
		String iconFile = cursor.getString(5);
		if (TextUtils.isEmpty(iconFile)) {
			image.setImageResource(Utils.getLargeCategoryDrawable(cursor.getInt(4)));
		} else {
			image.setImageBitmap(BitmapFactory.decodeFile(iconFile));
		}
	}

	@Override
	public View newView(final Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.project_item, parent, false);
		
		return view;
	}

}
