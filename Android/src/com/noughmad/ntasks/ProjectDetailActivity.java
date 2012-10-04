package com.noughmad.ntasks;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;

public class ProjectDetailActivity extends Activity {

	private long mProjectId;
	private SimpleCursorAdapter mNavigationAdapter;
	private static final String TAG = "ProjectDetailActivity";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getResources().getBoolean(R.bool.two_pane_layout)) {
			finish();
			return;
		}
		
//		Debug.startMethodTracing("ntasks_detail_short");
				
		final ActionBar bar = getActionBar();
		bar.setTitle("");
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		String[] from = new String[] {Database.KEY_PROJECT_TITLE, Database.ID};
		int[] to = new int[] {android.R.id.text1};
		
		Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME);
		ContentProviderClient client = getContentResolver().acquireContentProviderClient(uri);
		Cursor cursor;
		try {
			cursor = client.query(uri, from, null, null, null);
		} catch (RemoteException e1) {
			cursor = null;
			e1.printStackTrace();
		}
		
		mNavigationAdapter = new SimpleCursorAdapter(bar.getThemedContext(), android.R.layout.simple_spinner_item, cursor, from, to);
		
		mNavigationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bar.setListNavigationCallbacks(mNavigationAdapter, new ActionBar.OnNavigationListener() {
			
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				try {
					Log.i(TAG, "Navigation item " + itemPosition + " selected");
					showProject(itemId);
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}
		});
		
		onNewIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		long projectId = intent.getLongExtra("projectId", -1);
		if (projectId > -1) {
			showProject(projectId);
		}
	}

	/*
	@Override
	protected void onDestroy() {
		Debug.stopMethodTracing();
		super.onDestroy();
	}
	*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_add_task) {
			Utils.addTask(mProjectId, this);
		} else if (item.getItemId() == R.id.menu_refresh) {
			
			// TODO: Replace refresh with sync
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showProject(long projectId) {	
		mProjectId = projectId;
		String tag = "project-detail-" + projectId;
		
		FragmentManager fm = getFragmentManager();
		ProjectDetailFragment tasksFragment = (ProjectDetailFragment) fm.findFragmentByTag(tag);
		
		if (tasksFragment == null) {
			Log.i(TAG, "Creating a new fragment for the project " + projectId);
			tasksFragment = ProjectDetailFragment.create(projectId);
		}

		Fragment currentFragment = fm.findFragmentById(android.R.id.content);
		if (currentFragment != tasksFragment) {
			FragmentTransaction transaction = fm.beginTransaction();
			transaction.replace(android.R.id.content, tasksFragment, tag);
			if (currentFragment != null) {
				transaction.addToBackStack(null);
			}
			transaction.commit();					
		}
	}



	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.project_actions, menu);
			return true;
		}

	public void setProject(long id) {
		if (mNavigationAdapter.getItemId(getActionBar().getSelectedNavigationIndex()) == id) {
			showProject(id);
			return;
		}
		for (int i = 0; i < getActionBar().getNavigationItemCount(); ++i) {
			if (mNavigationAdapter.getItemId(i) == id) {
				getActionBar().setSelectedNavigationItem(i);
				break;
			}
		}
	}
}
