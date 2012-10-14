package com.noughmad.ntasks;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class TwoPaneLayout extends ViewGroup {

	public TwoPaneLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d("TwoPaneLayout", String.format("%s, %d, %d, %d, %d)", changed ? "changed" : "not changed", l, t, r, b));
		
		View projectList = getChildAt(0);
		View taskList = getChildAt(1);
		View timeline = getChildAt(2);
		View note = getChildAt(3);
		int timelineWidth;
		
		if (getChildCount() == 4) {
			projectList = getChildAt(0);
			taskList = getChildAt(1);
			timeline = getChildAt(2);
			note = getChildAt(3);
			timelineWidth = timeline.getLayoutParams().width;
		} else if (getChildCount() == 3) {
			projectList = getChildAt(0);
			taskList = getChildAt(1);
			timeline = null;
			note = getChildAt(2);
			timelineWidth = 0;
		} else {
			Log.e("TwoPaneLayout", "onLayout called with only " + getChildCount() + " children");
			return;
		}
		
		final int notesHeight = note.getLayoutParams().height;
		final int projectListWidth = projectList.getLayoutParams().width;
		
		if (b - t >= notesHeight * 1.5) {
			projectList.layout(l, t, l + projectListWidth, b);
			taskList.layout(l + projectListWidth, t, r - timelineWidth, b - notesHeight);
			if (timeline != null) {
				timeline.layout(r - timelineWidth, t, r, b - notesHeight);
			}
			note.layout(l + projectListWidth, b - notesHeight, r, b);
		} else {
			projectList.layout(l, t, l + projectListWidth, b);
			note.layout(l + projectListWidth, t, r, b);

			taskList.layout(0, 0, 0, 0);
			if (timeline != null) {
				timeline.layout(0, 0, 0, 0);
			}
		}
	}

}
