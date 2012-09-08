package com.noughmad.ntasks;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CategoryAdapter extends ArrayAdapter<String> {

	private boolean mShowText;

	public CategoryAdapter(Context context, int textViewResourceId,
			String[] strings, boolean showText) {
		super(context, textViewResourceId, strings);
		mShowText = showText;
	}

	@Override
	public View getDropDownView(int position, View convertView,
	ViewGroup parent) {
		return getCustomView(position, convertView, parent, true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getCustomView(position, convertView, parent, mShowText);
	}

	public View getCustomView(int position, View convertView, ViewGroup parent, boolean showText) {
		
		TextView view = (TextView)convertView;
		if (view == null) {
			view = new TextView(getContext(), null, android.R.attr.textAppearanceListItem);
			view.setGravity(Gravity.CENTER_VERTICAL);
		}
		
		if (showText) {
			view.setText(getItem(position));
		}
		Drawable icon = getContext().getResources().getDrawable(Utils.categoryDrawables[position]);
		icon.setBounds(0,  0,  48,  48);
		view.setCompoundDrawables(icon, null, null, null);

		return view;
	}
}
