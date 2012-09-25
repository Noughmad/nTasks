package com.noughmad.ntasks;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.parse.ParseUser;

public class MainActivity extends Activity {
	
	static final int LOGIN_REQUEST = 0;
	static final String TAG = "MainActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
			startSync();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_actions, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_logout:
			ParseUser.logOut();
			Intent i = new Intent(this, AccountActivity.class);
			startActivityForResult(i, LOGIN_REQUEST);
			break;
			
		case R.id.menu_add_project:
			FragmentTransaction ft = getFragmentManager().beginTransaction();
		    Fragment prev = getFragmentManager().findFragmentByTag("project-dialog");
		    if (prev != null) {
		        ft.remove(prev);
		    }
		    ft.addToBackStack(null);

		    // Create and show the dialog.
		    ProjectDialogFragment newFragment = ProjectDialogFragment.create();
		    newFragment.show(ft, "project-dialog");
		    break;
		    
		case R.id.menu_refresh:
			if (ParseUser.getCurrentUser() == null) {
				Log.i(TAG, "No current user, showing login dialog");
				Intent intent = new Intent(this, AccountActivity.class);
				startActivityForResult(intent, LOGIN_REQUEST);
			} else {
				startSync();
			}
			break;
			
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
		return true;
	}
	
	private void startSync() {
		startService(new Intent(this, SyncService.class));
	}
}