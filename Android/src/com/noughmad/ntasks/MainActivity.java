package com.noughmad.ntasks;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.parse.ParseUser;

public class MainActivity extends IconGetterActivity {
	
	static final int LOGIN_REQUEST = 0;
	static final int CAMERA_REQUEST = 1;
	static final int GALLERY_REQUEST = 2;
	static final String TAG = "MainActivity";
	
	long mIconProjectId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Log.i(TAG, "Is two-pane: " + getResources().getBoolean(R.bool.two_pane_layout));
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Log.i(TAG, "Display width: " + metrics.widthPixels + ", " + metrics.xdpi);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LOGIN_REQUEST:
			if (resultCode == RESULT_OK) {
				startSync();
			}
			break;
			
		default:
			super.onActivityResult(requestCode, resultCode, data);
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
			break;
			
		case R.id.menu_statistics:
			showPlots();
			break;
		}
		return true;
	}
	
	private void startSync() {
		startService(new Intent(this, SyncService.class));
	}
	
	private void showPlots() {
		// TODO:
	}
}