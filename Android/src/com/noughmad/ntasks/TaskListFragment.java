package com.noughmad.ntasks;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListFragment;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TaskListFragment extends ListFragment {
	
	private static String TAG = "TaskListFragment";
	
	private ParseObject mProject;

	private static String[] from = new String[] {"name", "done"};
	private static int[] to = new int[] {R.id.task_name, R.id.task_done};
	
	public void setProject(ParseObject project) {
		mProject = project;
		updateTaskList();
	}
	
	private void updateTaskList()
	{
		ParseQuery query = new ParseQuery("Task");
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo("project", mProject);
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> tasks, ParseException e) {
				if (e == null) {
					Log.d(TAG, "Retrieved " + tasks.size() + " size");
					List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
					for (ParseObject project : tasks) {
						Map<String, Object> map = new HashMap<String, Object>();
						Log.d("ProjectListFragment", "Found project " + project.getString("name"));
						for (String key : from)
						{
							map.put(key, project.get(key));
						}
						list.add(map);
					}
					SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.task_item, from, to);
					setListAdapter(adapter);
				} else {
					Log.e(TAG, e.getMessage());
				}
			}
		});
	}
	
	public void startTracking(ParseObject task) {
		if (task.getBoolean("active")) {
			return;
		}
		
		task.put("active", true);
		task.put("lastStart", new Date());
		task.saveEventually();
	}
	
	public void stopTracking(ParseObject task) {
		if (!task.getBoolean("active")) {
			return;
		}
		
		Date start = task.getDate("lastStart");
		Date end = new Date();
		long duration = end.getTime() - start.getTime();
		
		task.put("active", false);
		task.increment("duration", duration);
		
		JSONObject unit = new JSONObject();
		try {
			unit.put("start", start);
			unit.put("end", end);
			unit.put("duration", duration);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		task.add("units", unit);
		task.saveEventually();
	}

}
