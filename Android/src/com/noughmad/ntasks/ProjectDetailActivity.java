package com.noughmad.ntasks;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.noughmad.ntasks.ActionBarCompat.TabData;

public class ProjectDetailActivity extends FragmentActivity
		implements LoaderManager.LoaderCallbacks<Cursor>
	{

	private long mProjectId;
	private static final String TAG = "ProjectDetailActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getResources().getBoolean(R.bool.two_pane_layout)) {
			finish();
			return;
		}
		
		mProjectId = getIntent().getLongExtra("com.noughmad.ntasks.projectId", -1);
		Log.i(TAG, "Opening project " + mProjectId);
		if (mProjectId < 0) {
			finish();
		}
		
		List<TabData> tabs = new ArrayList<TabData>();
		
		TabData tasks = new TabData();
		tasks.tag = "Tasks";
		tasks.icon = R.drawable.ic_menu_task_add;
		tasks.fragmentClass = TaskListFragment.class;
		tabs.add(tasks);

		TabData timeline = new TabData();
		timeline.tag = "Timeline";
		timeline.icon = android.R.drawable.ic_menu_recent_history;
		timeline.fragmentClass = TimelineFragment.class;
		tabs.add(timeline);

		TabData notes = new TabData();
		notes.tag = "Notes";
		notes.icon = android.R.drawable.ic_menu_edit;
		notes.fragmentClass = NotesFragment.class;
		tabs.add(notes);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			View view = ActionBarCompat.setNavigationTabs(this, tabs);
			view.setId(R.id.project_detail_pager);
			setContentView(view);
		} else {
			setContentView(R.layout.fragment_tabs_pager);
			ActionBarCompat.setNavigationTabs(this, tabs);
		}
				
//		Debug.startMethodTracing("ntasks_detail_short");

		if (savedInstanceState != null) {
			ActionBarCompat.setSelectedNavigationItem(this, savedInstanceState.getInt("tab", 0));
        }
		
		getSupportLoaderManager().initLoader(0, null, this);
	}
	
	@TargetApi(11)
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ActionBarCompat.saveSelectedNavigationIndex(this, outState, "tab");
    }

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.project_actions, menu);
		return true;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME), mProjectId);
		return new CursorLoader(this, uri, new String[] {Database.KEY_PROJECT_TITLE}, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.i(TAG, "onLoadFinished()");
		if (cursor.moveToFirst()) {
			setTitle(cursor.getString(0));
		} else {
			setTitle(R.string.app_name);
			finish();
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		setTitle(R.string.app_name);
	}
}
