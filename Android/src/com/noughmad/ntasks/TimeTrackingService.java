package com.noughmad.ntasks;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TimeTrackingService extends IntentService {
	
	private static final int UPDATE_INTERVAL = 10000;
	private static final String TAG = "TimeTrackingService";
	
	private boolean running;

	public TimeTrackingService() {
		super("TimeTrackingService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		long id = intent.getLongExtra("taskId", -1);
		
		Log.i("TimeTrackingService", "Handle intent " + id);
		
		Uri uri = Database.withId(Database.TASK_TABLE_NAME, id);
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
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			builder.setContentTitle(task.getString(0));
			builder.setContentText(project.getString(1));
			builder.setContentInfo(Utils.formatDuration(task.getLong(2)));
			builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), Utils.getLargeCategoryDrawable(project.getInt(2))));
			builder.setSmallIcon(R.drawable.ic_notification);
			
			Intent notificationIntent = new Intent(this, ProjectDetailActivity.class);
			notificationIntent.putExtra("com.noughmad.ntasks.projectId", project.getLong(0));
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			builder.setContentIntent(pendingIntent);
			
			Intent stopIntent = new Intent("com.noughmad.ntasks.ACTION_STOP_TRACKING");
			PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);
			builder.setDeleteIntent(pendingStopIntent);
			
			builder.addAction(android.R.drawable.ic_media_pause, getString(R.string.task_stop), pendingStopIntent);
						
			startForeground(Utils.TRACKING_NOTIFICATION, builder.build());
			
			project.close();
			task.close();
			client.release();
			
			running = true;
			while (running) {
				try {
					Thread.sleep(UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					break;
				}
				
				if (!running) {
					break;
				}
				
				client = getContentResolver().acquireContentProviderClient(uri);
				task = client.query(uri, columns, null, null, null);
				
				if (!task.moveToFirst()) {
					break;
				}
				
				long duration = task.getLong(2) + UPDATE_INTERVAL;
				
				Log.i(TAG, "Update duration to " + duration);
				
				builder.setContentInfo(Utils.formatDuration(duration));
				task.close();

				ContentValues values = new ContentValues();
				values.put("duration", duration);
				client.update(uri, values, null, null);
				
				client.release();
				startForeground(Utils.TRACKING_NOTIFICATION, builder.build());
			}
			
		} catch (RemoteException e) {
			client.release();
		}
		
		stopSelf();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Destroying a TimeTrackingService");
		running = false;
	}
	
	
}
