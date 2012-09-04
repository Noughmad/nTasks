package com.noughmad.ntasks;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

public class TasksApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, Secrets.PARSE_APPLICATION_ID, Secrets.PARSE_ANDROID_KEY);

		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(false);
		defaultACL.setPublicWriteAccess(false);
		ParseACL.setDefaultACL(defaultACL, true);
		
		Utils.taskStatuses = getResources().getStringArray(R.array.task_status_array);
	}

}
