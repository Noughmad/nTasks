package com.noughmad.ntasks;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;

public class TimeTrackingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.noughmad.ntasks.ACTION_START_TRACKING")) {
			long id = ContentUris.parseId(intent.getData());
			Utils.startTracking(id, context);
		} else {
			Utils.stopTracking(context);
		}
	}

}
