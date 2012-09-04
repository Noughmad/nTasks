package com.noughmad.ntasks;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

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
	public static String[] taskStatuses;
	
	public static int[] statusColors = new int[] {
		Color.parseColor("#bd362f"),
		Color.parseColor("#f89406"),
		Color.parseColor("#2f96b4"),
		Color.parseColor("#51a351")
	};
	
	public static int statusColor(int status)
	{
		if (status > -1 && status < 4) {
			return statusColors[status];
		} else {
			return Color.TRANSPARENT;
		}
	}
}
