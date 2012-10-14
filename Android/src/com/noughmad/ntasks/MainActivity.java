package com.noughmad.ntasks;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.noughmad.ntasks.sync.SyncService;
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
		    
		case R.id.menu_add_task:
			ProjectListFragment fragment = (ProjectListFragment) getFragmentManager().findFragmentById(R.id.project_list);
			if (fragment == null) {
				Log.w(TAG, "Could not find project list fragment");
				return false;
			}
			ListView list = fragment.getListView();
			if (list.getCheckedItemCount() == 0) {
				return false;
			}
			long projectId = list.getItemIdAtPosition(list.getCheckedItemPosition());
			if (projectId == ListView.INVALID_ROW_ID) {
				Log.w(TAG, "Project list fragment has no selected projects " + fragment.getListView().getCount());
				return false;
			} 
			Utils.addTask(projectId, this);
			return true;
		    
		case R.id.menu_refresh:
			if (ParseUser.getCurrentUser() == null) {
				Log.i(TAG, "No current user, showing login dialog");
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivityForResult(intent, LOGIN_REQUEST);
			} else {
				startSync();
			}
			break;
			
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			break;
		}
		return true;
	}
	
	private void startSync() {
		Intent intent = new Intent(this, SyncService.class);
		intent.putExtra("manual", true);
		startService(intent);
	}
}