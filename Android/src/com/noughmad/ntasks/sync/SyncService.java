package com.noughmad.ntasks.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.RemoteException;

import com.noughmad.ntasks.Database;
import com.noughmad.ntasks.R;
import com.noughmad.ntasks.Utils;
import com.noughmad.ntasks.sync.Bridge.Options;
import com.parse.ParseException;
import com.parse.ParseUser;

public class SyncService extends IntentService {
	
	Bridge mBridge;
	
	static Map<String,Options> sOptions;
	static List<Options> sAllOptions;
	
	static {
		sOptions = new HashMap<String, Options>();
		sAllOptions = new ArrayList<Options>();
		
		Options projectOptions = new Options();
		projectOptions.className = Database.PROJECT_TABLE_NAME;
		projectOptions.foreignKeys = new HashMap<String,String>();
		projectOptions.textColumns = Arrays.asList(Database.KEY_PROJECT_TITLE, Database.KEY_PROJECT_CLIENT, Database.KEY_PROJECT_DESCRIPTION);
		projectOptions.intColumns = Arrays.asList(Database.KEY_PROJECT_CATEGORY);
		projectOptions.fileColumns = Arrays.asList(Database.KEY_PROJECT_ICON);
		sOptions.put(Database.PROJECT_TABLE_NAME, projectOptions);
		sAllOptions.add(projectOptions);
		
		Options taskOptions = new Options();
		taskOptions.className = Database.TASK_TABLE_NAME;
		taskOptions.foreignKeys = new HashMap<String,String>();
		taskOptions.foreignKeys.put(Database.KEY_TASK_PROJECT, Database.PROJECT_TABLE_NAME);
		taskOptions.textColumns = Arrays.asList(Database.KEY_TASK_NAME);
		taskOptions.intColumns = Arrays.asList(Database.KEY_TASK_STATUS, Database.KEY_TASK_ACTIVE);
		taskOptions.dateColumns = Arrays.asList(Database.KEY_TASK_LASTSTART);
		sOptions.put(Database.TASK_TABLE_NAME, taskOptions);
		sAllOptions.add(taskOptions);

		Options noteOptions = new Options();
		noteOptions.className = Database.NOTE_TABLE_NAME;
		noteOptions.foreignKeys = new HashMap<String,String>();
		noteOptions.foreignKeys.put(Database.KEY_NOTE_TASK, Database.TASK_TABLE_NAME);
		noteOptions.textColumns = Arrays.asList(Database.KEY_NOTE_TEXT);
		sOptions.put(Database.NOTE_TABLE_NAME, noteOptions);
		sAllOptions.add(noteOptions);
		
		Options unitOptions = new Options();
		unitOptions.className = Database.WORKUNIT_TABLE_NAME;
		unitOptions.foreignKeys = new HashMap<String,String>();
		unitOptions.foreignKeys.put(Database.KEY_WORKUNIT_TASK, Database.TASK_TABLE_NAME);
		unitOptions.intColumns = Arrays.asList(Database.KEY_WORKUNIT_DURATION);
		unitOptions.dateColumns = Arrays.asList(Database.KEY_WORKUNIT_START, Database.KEY_WORKUNIT_END);
		sOptions.put(Database.WORKUNIT_TABLE_NAME, unitOptions);
		sAllOptions.add(unitOptions);
	}

	public SyncService() {
		super("SyncService");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent) {
		if (ParseUser.getCurrentUser() == null) {
			stopSelf();
			return;
		}
		
		if (!intent.getBooleanExtra("manual", false) && !getSharedPreferences("nTasks", MODE_PRIVATE).getBoolean("automaticSync", true)) {
			stopSelf();
			return;
		}
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isAvailable()) {
			stopSelf();
			return;
		}		
		
		mBridge = new Bridge(getApplicationContext());
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Resources res = getResources();
		
		Notification notification = new Notification(R.drawable.ic_launcher, res.getString(R.string.app_name), System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), res.getString(R.string.app_name), res.getString(R.string.notification_sync_text), null);
		nm.notify(Utils.SYNC_NOTIFICATION, notification);
		
		List<Options> options;
		
		if (intent.hasExtra("className")) {
			String className = intent.getStringExtra("className");
			options = Arrays.asList(sOptions.get(className));
		} else {
			options = sAllOptions;
		}
		
		try {
			for (Options opt : options) {
				mBridge.sync(opt);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		nm.cancel(1);
		mBridge = null;
	}
}
