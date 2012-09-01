package com.noughmad.ntasks;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.parse.ParseObject;

public class ProjectDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		final ActionBar bar = getActionBar();
		bar.setTitle("");
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		List<String> titles = new ArrayList<String>();
		for (ParseObject project : Utils.projects) {
			titles.add(project.getString("title"));
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titles);
		bar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
			
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				try {
					showProject(Utils.projects.get(itemPosition));
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}
		});
		
		
		if (getIntent().getExtras().containsKey("project")) {
			int position = getIntent().getExtras().getInt("project");
			showProject(Utils.projects.get(position));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showProject(ParseObject project) {
		String tag = "project-detail-" + project.getObjectId();
		
		FragmentManager fm = getFragmentManager();
		ListFragment tasksFragment = (ListFragment) fm.findFragmentByTag(tag);
		FragmentTransaction ft = fm.beginTransaction();

		if (tasksFragment == null) {
			tasksFragment = new ListFragment();
			tasksFragment.setListAdapter(new TaskListAdapter(this, project));
		}
		
		ft.replace(android.R.id.content, tasksFragment, tag);
		ft.addToBackStack(null);
		ft.commit();	
	}
	
	
}
