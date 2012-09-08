package com.noughmad.ntasks;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.parse.ParseObject;

public class ProjectDetailActivity extends Activity {

	private ParseObject mProject;
	private static final String TAG = "ProjectDetailActivity";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		final ActionBar bar = getActionBar();
		bar.setIcon(R.drawable.ic_launcher_light);
		bar.setTitle("");
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		List<String> titles = new ArrayList<String>();
		for (ParseObject project : Utils.projects) {
			titles.add(project.getString("title"));
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titles);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
			
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				try {
					Log.i(TAG, "Navigation item " + itemPosition + " selected");
					showProject(Utils.projects.get(itemPosition));
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
		int position = intent.getIntExtra("project", -1);
		if (position > -1) {
			getActionBar().setSelectedNavigationItem(position);
		}
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_add_task) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.add_task);
			
			final EditText edit = new EditText(this);
			builder.setView(edit);
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();	
				}
			});
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ParseObject task = new ParseObject("Task");
					task.put("project", mProject);
					task.put("name", edit.getText().toString());
					task.put("status", 1);
					// The task will be saved by the TaskListAdapter, no need to save it here
					
					ProjectDetailFragment fragment = (ProjectDetailFragment)getFragmentManager().findFragmentByTag("project-detail-" + mProject.getObjectId());
					if (fragment != null) {
						Log.d(TAG, "Found project detail fragment for " + mProject.getString("title"));
						TaskListAdapter adapter = (TaskListAdapter)fragment.getListView().getExpandableListAdapter();
						adapter.mTasks.add(task);
						adapter.notifyDataSetChanged();
					} else {
						Log.w(TAG, "Could not find project detail fragment for project " + mProject.getString("title"));
						task.saveEventually();
					}
				}
			});
			builder.create().show();
		} else if (item.getItemId() == R.id.menu_refresh) {
			ProjectDetailFragment fragment = (ProjectDetailFragment)getFragmentManager().findFragmentByTag("project-detail-" + mProject.getObjectId());
			if (fragment != null) {
				fragment.updateTaskList();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showProject(ParseObject project) {	
		mProject = project;
		String tag = "project-detail-" + project.getObjectId();
		Log.i(TAG, "Showing project " + project.getString("title"));
		
		FragmentManager fm = getFragmentManager();
		ProjectDetailFragment tasksFragment = (ProjectDetailFragment) fm.findFragmentByTag(tag);
		
		if (tasksFragment == null) {
			Log.i(TAG, "Creating a new fragment for the project");
			tasksFragment = ProjectDetailFragment.create(project);
		}

		Fragment currentFragment = fm.findFragmentById(android.R.id.content);
		if (currentFragment != tasksFragment) {
			fm.beginTransaction().replace(android.R.id.content, tasksFragment, tag).commit();					
		}
	}



	@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.project_actions, menu);
			return true;
		}
}
