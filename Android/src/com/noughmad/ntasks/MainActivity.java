package com.noughmad.ntasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;

import com.parse.ParseUser;

public class MainActivity extends Activity {
	
	static final int LOGIN_REQUEST = 0;
	static final String TAG = "MainActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if (ParseUser.getCurrentUser() == null) {
			Log.i(TAG, "No current user, showing login dialog");
			Intent i = new Intent(this, AccountActivity.class);
			startActivityForResult(i, LOGIN_REQUEST);
		} else {
			Log.i(TAG, "Current user is " + ParseUser.getCurrentUser().getUsername());
			showProjects();
		}
		
		if (getPreferences(MODE_PRIVATE).getBoolean("warnNoNetwork", true)) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isAvailable()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setTitle(R.string.no_network_title);
				builder.setMessage(R.string.no_network_message);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setNeutralButton("Don't show again", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						getPreferences(MODE_PRIVATE).edit().putBoolean("warnNoNetwork", false).apply();
						dialog.dismiss();
					}
				});
				builder.create().show();
			}			
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LOGIN_REQUEST) {
			if (resultCode == RESULT_OK) {
				showProjects();
			} else {
				finish();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_actions, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
			
		case R.id.menu_search:
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
			ProjectListFragment fragment = (ProjectListFragment) getFragmentManager().findFragmentByTag("project-list");
			if (fragment != null) {
				fragment.updateProjectList();
			}
		}
		return true;
	}

	private void showProjects() {
		Log.d(TAG, "showProjects()");
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = fm.findFragmentByTag("project-list");
		if (fragment != null) {
			ft.attach(fragment);
			Log.d(TAG, "Re-using existing fragment");
		} else {
			ft.add(R.id.main_container, new ProjectListFragment(), "project-list");
			Log.d(TAG, "Created a ProjectListFragment");
		}
		ft.commit();
		
		Log.d(TAG, "Created a fragment transaction");
	}
}