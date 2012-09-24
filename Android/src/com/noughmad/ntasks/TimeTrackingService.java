package com.noughmad.ntasks;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

public class TimeTrackingService extends IntentService {
	
	private static final int UPDATE_INTERVAL = 10000;

	public TimeTrackingService() {
		super("TimeTrackingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		long id = intent.getLongExtra("taskId", -1);
		
		Log.i("TimeTrackingService", "Handle intent " + id);
		
		Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.TASK_TABLE_NAME), id);
		ContentProviderClient client = getContentResolver().acquireContentProviderClient(uri);
		
		String columns[] = new String[] {Database.KEY_TASK_NAME, Database.KEY_TASK_PROJECT, Database.KEY_TASK_DURATION};
		try {
			Cursor task = client.query(uri, columns, null, null, null);
			if (!task.moveToFirst()) {
				stopSelf();
				return;
			}
			Cursor project = client.query(
					ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME), task.getLong(1)), 
					new String[] {Database.ID, Database.KEY_PROJECT_TITLE, Database.KEY_PROJECT_CATEGORY}, null, null, null);
			
			if (!project.moveToFirst()) {
				project.close();
				task.close();
				client.release();
				stopSelf();
				return;
			}
			
			int icon = Utils.getCategoryDrawable(project.getInt(2));
						
			Notification notification = new Notification(icon, getText(R.string.app_name), System.currentTimeMillis());
			Intent notificationIntent = new Intent(this, ProjectDetailActivity.class);
			intent.putExtra("projectId", project.getLong(0));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(getApplicationContext(), task.getString(0), Utils.formatDuration(task.getLong(2)), pendingIntent);
						
			Intent broadcastIntent = new Intent("com.noughmad.ntasks.ACTION_STOP_TRACKING");
			notification.deleteIntent = PendingIntent.getBroadcast(this, 0, broadcastIntent, 0);
			
			startForeground(Utils.TRACKING_NOTIFICATION, notification);
			
			project.close();
			task.close();
			client.release();
			
			while (true) {
				try {
					Thread.sleep(UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					break;
				}
				
				client = getContentResolver().acquireContentProviderClient(uri);
				task = client.query(uri, columns, null, null, null);
				
				if (!task.moveToFirst()) {
					break;
				}
				
				long duration = task.getLong(2) + UPDATE_INTERVAL;				
				notification.setLatestEventInfo(getApplicationContext(), task.getString(0), Utils.formatDuration(duration), pendingIntent);
				task.close();

				ContentValues values = new ContentValues();
				values.put("duration", duration);
				client.update(uri, values, null, null);
				
				client.release();
				startForeground(Utils.TRACKING_NOTIFICATION, notification);
			}
			
		} catch (RemoteException e) {
		}
		
		client.release();
		stopSelf();
	}
}
