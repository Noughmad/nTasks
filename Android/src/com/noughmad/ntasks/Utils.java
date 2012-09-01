package com.noughmad.ntasks;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseObject;

public class Utils {
	public static void startTracking(ParseObject task) {
		if (activeTask != null && activeTask != task) {
			stopTracking(activeTask);
		}
		
		task.put("active", true);
		task.put("lastStart", new Date());
		task.saveEventually();
		
		activeTask = task;
	}
	
	public static void stopTracking(ParseObject task) {
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
		
		activeTask = null;
	}
	
	public static List<ParseObject> projects;
	public static ParseObject activeTask = null;
}
