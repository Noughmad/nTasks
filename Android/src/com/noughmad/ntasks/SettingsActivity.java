package com.noughmad.ntasks;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.parse.ParseFacebookUtils;

public class SettingsActivity extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}		
}
