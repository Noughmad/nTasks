package com.noughmad.ntasks;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ProjectDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.project_detail_container);
		
		final ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		
		ParseQuery query = new ParseQuery("Project");
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
		query.getInBackground(getIntent().getStringExtra("project"), new GetCallback() {

			@Override
			public void done(ParseObject project, ParseException e) {
				if (e == null) {
					Log.d("ProjectDetailActivity", "Got project " + project.getString("name"));
					setTitle(project.getString("name"));
					ProjectDetailFragment fragment = (ProjectDetailFragment) getFragmentManager().findFragmentByTag("project-detail");
					fragment.setProject(project);
					
					TaskListFragment tasksFragment = (TaskListFragment) getFragmentManager().findFragmentByTag("project-task-list");
					tasksFragment.setProject(project);
					
				} else {
					Log.e("ProjectDetailActivity", e.getLocalizedMessage());
				}
			}
			
		});
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
	
	
	
}
