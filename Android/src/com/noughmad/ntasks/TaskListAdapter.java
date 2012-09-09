package com.noughmad.ntasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TaskListAdapter extends BaseExpandableListAdapter {
	private final static String TAG = "TaskListAdapter";
	
	// Zero is reserved for children
	private final static int VIEW_TYPE_HEADER = 1;
	private final static int VIEW_TYPE_TASK = 2;
	private final static int VIEW_TYPE_COUNT = 3;
	
	List<ParseObject> mTasks;
	Map<String, List<ParseObject>> mNotes;
	private Context mContext;
	private ParseObject mProject;
	
	public TaskListAdapter(Context context, ParseObject project) {
		mContext = context;
		mProject = project;
		updateTasks(true);		
	}
	
	public void updateTasks(final boolean cache) {
		ParseQuery query = new ParseQuery("Task");
		if (cache) {
			query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		} else {
			query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
		}
		query.whereEqualTo("project", mProject);
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> tasks, ParseException e) {
				if (e == null) {
					setTasks(tasks, cache);
				} else {
					Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}});
	}
	
	private void setTasks(List<ParseObject> tasks, boolean cache)
	{
		mTasks = tasks;
		Log.i(TAG, "Received " + Integer.toString(tasks.size()) + " tasks");
		notifyDataSetChanged();
		
		ParseQuery query = new ParseQuery("Note");
		query.whereContainedIn("task", mTasks);
		if (cache) {
			query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		} else {
			query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
		}		

		query.findInBackground(new FindCallback() {
			@Override
			public void done(List<ParseObject> notes, ParseException e) {
				if (e == null) {
					setNotes(notes);
				} else {
					Log.w(TAG, "Error loading task notes");
				}
			}});
	}
	
	private void setNotes(List<ParseObject> notes) {
		if (mNotes == null) {
			mNotes = new HashMap<String, List<ParseObject>>();
		} else {
			mNotes.clear();
		}
		for (ParseObject note : notes) {
			ParseObject task = note.getParseObject("task");
			if (!mNotes.containsKey(task.getObjectId())) {
				mNotes.put(task.getObjectId(), new ArrayList<ParseObject>());
			}
			
			mNotes.get(task.getObjectId()).add(note);
		}
		Log.i(TAG, "Received " + Integer.toString(notes.size()) + " notes");
		notifyDataSetChanged();
	}
	
	public int getGroupCount() {
		if (mTasks == null) {
			return 1;
		} else {
			return mTasks.size() + 1;
		}
	}

	public Object getGroup(int position) {
		if (position == 0) {
			return null;
		}
		return mTasks.get(position-1);
	}

	public long getGroupId(int position) {
		if (position == 0) {
			return 0;
		}
		return Long.parseLong(mTasks.get(position-1).getObjectId(), 36);
	}

	public int getGroupType(int position) {
		if (position == 0) {
			return VIEW_TYPE_HEADER;
		} else {
			return VIEW_TYPE_TASK;
		}
	}

	public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
		int type = getGroupType(position);
		View view = convertView;

		if (type == VIEW_TYPE_HEADER) {
			if (view == null)
			{
				view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.project_status, parent, false);
			}
			ProgressBar bar = (ProgressBar) view.findViewById(R.id.project_progress);
			if (mTasks != null) {
				bar.setMax(mTasks.size());
				int done = 0;
				long duration = 0;
				for (ParseObject task : mTasks) {
					if (task.getInt("status") > 2) {
						done++;
					}
					duration += task.getLong("duration");
				}
				bar.setProgress(done);
				bar.setIndeterminate(false);
				
				String d = String.format("%d/%d tasks done, %s spent", done, mTasks.size(), Utils.formatDuration(duration));
				((TextView)view.findViewById(R.id.project_time)).setText(d);
			} else {
				bar.setIndeterminate(true);
				((TextView)view.findViewById(R.id.project_time)).setText(Utils.formatDuration(0));				
			}
			
			int category = 4; // Default to other
			if (mProject.has("category")) {
				category = mProject.getInt("category");
			}
			
			CategoryAdapter adapter = new CategoryAdapter(mContext, android.R.layout.simple_spinner_item, mContext.getResources().getStringArray(R.array.project_categories), false);
			Spinner spinner = (Spinner) view.findViewById(R.id.project_image);
			spinner.setAdapter(adapter);
			spinner.setSelection(category);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				public void onItemSelected(AdapterView<?> spinner, View view,
						int position, long id) {

					if (mProject.containsKey("category") && mProject.getInt("category") == position) {
						return;
					}
					mProject.put("category", position);
					mProject.saveEventually();
				}

				public void onNothingSelected(AdapterView<?> spinner) {
				}
			});
		} else {
			final ParseObject task = mTasks.get(position-1);
			if (view == null) {
				Log.d(TAG, "Creating a new task item view");
				view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.task_item, null, false);
				
				((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
				
				Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
				spinner.setAdapter(new TaskStatusAdapter(mContext));
	
				((LinearLayout)view.findViewById(R.id.task_item_layout)).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
				((LinearLayout)view.findViewById(R.id.task_item_layout)).setDividerPadding(8);
			}
			
			Spinner spinner = (Spinner)view.findViewById(R.id.task_status);
			spinner.setOnItemSelectedListener(null);
			if (task.has("status") && task.getInt("status") > -1 && task.getInt("status") < 5) {
				spinner.setSelection(task.getInt("status"));
			} else {
				spinner.setSelection(1);
			}
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
	
				public void onItemSelected(AdapterView<?> spinner, View item,
						int position, long id) {
					if (task.containsKey("status") && task.getInt("status") == position) {
						return;
					}
					
					task.put("status", position);
					notifyDataSetChanged();

					Log.d(TAG, "Setting status of " + task.getString("name") + " to " + Integer.toString(position) );
					task.saveEventually();
					Log.d(TAG, "Done");
				}
	
				public void onNothingSelected(AdapterView<?> spinner) {
					
				}
			});
			((TextView)view.findViewById(R.id.task_name)).setText(task.getString("name"));
					
			ImageButton button = (ImageButton) view.findViewById(R.id.task_track_button);
			if (task.getBoolean("active")) {
				button.setImageResource(android.R.drawable.ic_media_pause);
				// view.setBackgroundResource(R.drawable.list_selector_background_selected);
			} else {
				button.setImageResource(android.R.drawable.ic_media_play);
				// view.setBackgroundResource(android.R.drawable.list_selector_background);
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
			durationView.setText(Utils.formatDuration(task.getLong("duration"))); 
			
			boolean showDuration = mContext.getResources().getBoolean(R.bool.task_item_show_duration); 
			durationView.setVisibility(showDuration ? View.VISIBLE : View.GONE);
		}
		
		return view;
	}

	public int getGroupTypeCount() {
		return VIEW_TYPE_COUNT;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isEmpty() {
		return mTasks.isEmpty();
	}

	public boolean areAllItemsEnabled() {
		return false;
	}
	
	public void startTracking(ParseObject task) {
		if (task.getBoolean("active")) {
			return;
		}
		
		Log.i(TAG, "Start tracking " + task.getString("name"));
		Utils.startTracking(task);
		
		Toast.makeText(mContext, "Now tracking '" + task.getString("name") + "'", Toast.LENGTH_SHORT).show();
		notifyDataSetChanged();
	}
	
	public void stopTracking(ParseObject task) {
		if (!task.getBoolean("active")) {
			return;
		}
		
		Log.i(TAG, "Stop tracking " + task.getString("name"));
		Utils.stopTracking(task);
		
		Toast.makeText(mContext, "Stopped tracking '" + task.getString("name") + "'", Toast.LENGTH_SHORT).show();
		notifyDataSetChanged();
	}

	public Object getChild(int groupPosition, int childPosition) {
		ParseObject task = (ParseObject)getGroup(groupPosition);
		return mNotes.get(task.getObjectId()).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		ParseObject task = (ParseObject)getGroup(groupPosition);
		return Long.parseLong(mNotes.get(task.getObjectId()).get(childPosition).getObjectId(), 36);
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final ParseObject note = (ParseObject)getChild(groupPosition, childPosition);
		
		View view = convertView;
		if (view == null) {
			Log.d(TAG, "Creating a new note view");
			view = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.note_item, parent, false);
			view.findViewById(R.id.note_delete_button).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setMessage("Really delete this note?");
					builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String taskId = note.getParseObject("task").getObjectId();
							mNotes.get(taskId).remove(note);
							note.deleteEventually();
							notifyDataSetChanged();
						}
					});
					builder.create().show();
				}
			});
		} else {
			Log.d(TAG, "Converting a note view " + view);
		}
		TextView textView = (TextView)view.findViewById(R.id.note_text);
		textView.setText(note.getString("text"));
		return view;
	}

	public int getChildrenCount(int groupPosition) {
		if (groupPosition == 0 || mNotes == null || mNotes.get(mTasks.get(groupPosition-1).getObjectId()) == null) {
			return 0;
		} else {
			return mNotes.get(mTasks.get(groupPosition-1).getObjectId()).size();
		}
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}