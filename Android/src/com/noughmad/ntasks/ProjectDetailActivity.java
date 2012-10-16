package com.noughmad.ntasks;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ProjectDetailActivity extends Activity
		implements LoaderManager.LoaderCallbacks<Cursor>
	{

	private long mProjectId;
	private static final String TAG = "ProjectDetailActivity";
	
	public static class TabsAdapter extends FragmentPagerAdapter
    	implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
		
		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		
		static final class TabInfo {
		    private final Class<?> clss;
		    private final Bundle args;
		
		    TabInfo(Class<?> _class, Bundle _args) {
		        clss = _class;
		        args = _args;
		    }
		}
		
		public TabsAdapter(Activity activity, ViewPager pager) {
		    super(activity.getFragmentManager());
		    mContext = activity;
		    mActionBar = activity.getActionBar();
		    mViewPager = pager;
		    mViewPager.setAdapter(this);
		    mViewPager.setOnPageChangeListener(this);
		}
		
		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
		    TabInfo info = new TabInfo(clss, args);
		    tab.setTag(info);
		    tab.setTabListener(this);
		    mTabs.add(info);
		    mActionBar.addTab(tab);
		    notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
		    return mTabs.size();
		}
		
		@Override
		public Fragment getItem(int position) {
		    TabInfo info = mTabs.get(position);
		    return Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}
		
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		public void onPageSelected(int position) {
		    mActionBar.setSelectedNavigationItem(position);
		}
		
		public void onPageScrollStateChanged(int state) {
		}
		
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
		    Object tag = tab.getTag();
		    for (int i=0; i<mTabs.size(); i++) {
		        if (mTabs.get(i) == tag) {
		            mViewPager.setCurrentItem(i);
		        }
		    }
		}
		
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
		}
		
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}
	}

	ViewPager mPager;
	TabsAdapter mTabsAdapter;

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
		
		mPager = new ViewPager(this);
		mPager.setId(R.id.project_detail_pager);
		setContentView(mPager);
		
		mTabsAdapter = new TabsAdapter(this, mPager);
		
//		Debug.startMethodTracing("ntasks_detail_short");
				
		final ActionBar bar = getActionBar();
		
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		Bundle args = new Bundle();
		args.putLong("projectId", mProjectId);
		mTabsAdapter.addTab(bar.newTab().setIcon(R.drawable.ic_menu_task_add), TaskListFragment.class, args);
		mTabsAdapter.addTab(bar.newTab().setIcon(android.R.drawable.ic_menu_recent_history), TimelineFragment.class, args);
		mTabsAdapter.addTab(bar.newTab().setIcon(android.R.drawable.ic_menu_edit), NotesFragment.class, args);
		
		if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
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
			getActionBar().setTitle(cursor.getString(0));
		} else {
			getActionBar().setTitle(R.string.app_name);
			finish();
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		getActionBar().setTitle(R.string.app_name);
	}
}
