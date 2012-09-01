package com.noughmad.ntasks;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TaskListAdapter extends BaseAdapter {
	private final static String TAG = "TaskListAdapter";
	
	private List<ParseObject> mTasks;
	private Context mContext;
	
	public TaskListAdapter(Context context, ParseObject project) {
		mContext = context;
		ParseQuery query = new ParseQuery("Task");
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo("project", project);
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> tasks, ParseException e) {
				if (e == null) {
					setTasks(tasks);
				} else {
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}});
	}
	
	public void setTasks(List<ParseObject> tasks)
	{
		mTasks = tasks;
		notifyDataSetChanged();
	}
	
	public int getCount() {
		return mTasks.size();
	}

	public Object getItem(int position) {
		return mTasks.get(position);
	}

	public long getItemId(int position) {
		return Long.parseLong(mTasks.get(position).getObjectId(), 36);
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ParseObject task = mTasks.get(position);
		View view = convertView;
		if (view == null) {
			view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_item, parent, false);

			Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
			        R.array.task_status_array, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(adapter);

			((LinearLayout)view.findViewById(R.id.task_item_layout)).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
			((LinearLayout)view.findViewById(R.id.task_item_layout)).setDividerPadding(8);
		}
		
		Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
		if (task.has("status") && task.getInt("status") > -1 && task.getInt("status") < 5) {
			spinner.setSelection(task.getInt("status"));
		} else {
			spinner.setSelection(1);
		}
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> spinner, View item,
					int position, long id) {
				task.put("status", position);
				task.saveEventually();
			}

			public void onNothingSelected(AdapterView<?> spinner) {
				
			}
		});
		((TextView)view.findViewById(R.id.task_name)).setText(task.getString("name"));
				
		ImageButton button = (ImageButton) view.findViewById(R.id.task_track_button);
		if (task.getBoolean("active")) {
			button.setImageResource(android.R.drawable.ic_media_pause);
			view.setBackgroundResource(R.drawable.list_selector_background_selected);
		} else {
			button.setImageResource(android.R.drawable.ic_media_play);
			view.setBackgroundResource(android.R.drawable.list_selector_background);
		}
		
		button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if (task.getBoolean("active")) {
					stopTracking(task);
				} else {
					startTracking(task);
				}
			}
		});
		
		TextView durationView = (TextView)view.findViewById(R.id.task_duration);
		long duration = task.getLong("duration"); 
		if (duration < 1000 * 60 * 60 * 24) {
			// Show minutes for durations shorter than a day, otherwise show only hours
			SimpleDateFormat format = new SimpleDateFormat("H 'h' mm 'min'");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			durationView.setText(format.format(new Date(duration)));
		} else {
			durationView.setText(Integer.toString((int) (duration / 1000 / 60 / 60)) + " h");
		}
		
		boolean showDuration = mContext.getResources().getBoolean(R.bool.task_item_show_duration); 
		durationView.setVisibility(showDuration ? View.VISIBLE : View.GONE);
		
		return view;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return mTasks.isEmpty();
	}

	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}
	
	public void startTracking(ParseObject task) {
		if (task.getBoolean("active")) {
			return;
		}
		
		Log.i(TAG, "Start tracking " + task.getString("name"));
		Utils.startTracking(task);
		
		Toast.makeText(mContext, "Now tracking '" + task.getString("name") + "'", Toast.LENGTH_LONG).show();
		notifyDataSetChanged();
	}
	
	public void stopTracking(ParseObject task) {
		if (!task.getBoolean("active")) {
			return;
		}
		
		Log.i(TAG, "Stop tracking " + task.getString("name"));
		Utils.stopTracking(task);
		
		Toast.makeText(mContext, "Stopped tracking '" + task.getString("name") + "'", Toast.LENGTH_LONG).show();
		notifyDataSetChanged();
	}
}