package com.noughmad.ntasks;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseTwitterUtils;

public class TasksApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, Secrets.PARSE_APPLICATION_ID, Secrets.PARSE_ANDROID_KEY);

		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(false);
		defaultACL.setPublicWriteAccess(false);
		ParseACL.setDefaultACL(defaultACL, true);
		
		ParseTwitterUtils.initialize(Secrets.TWITTER_CONSUMER_KEY, Secrets.TWITTER_CONSUMER_SECRET);
		// ParseFacebookUtils.initialize(appId);
		
		Utils.taskStatuses = getResources().getStringArray(R.array.task_status_array);
	}

}
