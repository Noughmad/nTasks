package com.noughmad.ntasks;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

public class TasksApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Add your initialization code here
		Parse.initialize(this, Secrets.PARSE_APPLICATION_ID, Secrets.PARSE_ANDROID_KEY); 

		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(false);
		defaultACL.setPublicWriteAccess(false);
		ParseACL.setDefaultACL(defaultACL, true);
	}

}
