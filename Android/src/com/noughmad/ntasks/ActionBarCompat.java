package com.noughmad.ntasks;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

public class ActionBarCompat {
	
	@TargetApi(11)
	static class ActionBarCompatImpl {

		public static void setDisplayUpAsHomeEnabled(Activity activity,
				boolean enabled) {
			activity.getActionBar().setDisplayHomeAsUpEnabled(enabled);
		}

		public static View setNavigationTabs(Activity activity,
				List<TabData> tabs) {
			
			final ActionBar bar = activity.getActionBar();
			
			ViewPager pager = new ViewPager(activity);
			TabsAdapter adapter = new TabsAdapter((FragmentActivity)activity, pager);
			pager.setAdapter(adapter);
			
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			Bundle args = new Bundle();
			for (TabData tab : tabs) {
				adapter.addTab(bar.newTab().setIcon(tab.icon), tab.fragmentClass, args);
			}
			
			return pager;
		}

		public static void saveSelectedNavigationIndex(Activity activity, Bundle outState,
				String key) {
			outState.putInt(key, activity.getActionBar().getSelectedNavigationIndex());
		}

		public static void setSelectedNavigationIndex(
				Activity activity, int index) {
			activity.getActionBar().setSelectedNavigationItem(index);
		}
		
	}
	
	static class TabData {
		String tag;
		int icon;
		Class<?> fragmentClass;
	}
	
	@TargetApi(11)
	static class TabsAdapter extends FragmentPagerAdapter
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
		
		public TabsAdapter(FragmentActivity activity, ViewPager pager) {
		    super(activity.getSupportFragmentManager());
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
		public android.support.v4.app.Fragment getItem(int position) {
		    TabInfo info = mTabs.get(position);
		    return android.support.v4.app.Fragment.instantiate(mContext, info.clss.getName(), info.args);
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

	static class OldTabsCompatImpl {
		static View setNavigationTabs(Activity activity, List<TabData> tabs) {
			
			TabHost host = (TabHost) activity.findViewById(android.R.id.tabhost);
			host.setup();
			ViewPager pager = new ViewPager(activity);
			OldTabsAdapter adapter = new OldTabsAdapter((FragmentActivity)activity, host, pager);
			
			for (TabData tab : tabs) {
				TabSpec spec = host.newTabSpec(tab.tag);
				spec.setIndicator(tab.tag, activity.getResources().getDrawable(tab.icon));
				adapter.addTab(spec, tab.fragmentClass, null);
			}
			
			return pager;
		}
	}
	
	public static class OldTabsAdapter extends FragmentPagerAdapter
    implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final TabHost mTabHost;
		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
		
		static final class TabInfo {
		    private final String tag;
		    private final Class<?> clss;
		    private final Bundle args;
		
		    TabInfo(String _tag, Class<?> _class, Bundle _args) {
		        tag = _tag;
		        clss = _class;
		        args = _args;
		    }
		}
		
		static class DummyTabFactory implements TabHost.TabContentFactory {
		    private final Context mContext;
		
		    public DummyTabFactory(Context context) {
		        mContext = context;
		    }
		
		    public View createTabContent(String tag) {
		        View v = new View(mContext);
		        v.setMinimumWidth(0);
		        v.setMinimumHeight(0);
		        return v;
		    }
		}
		
		public OldTabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
		    super(activity.getSupportFragmentManager());
		    mContext = activity;
		    mTabHost = tabHost;
		    mViewPager = pager;
		    mTabHost.setOnTabChangedListener(this);
		    mViewPager.setAdapter(this);
		    mViewPager.setOnPageChangeListener(this);
		}
		
		public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
		    tabSpec.setContent(new DummyTabFactory(mContext));
		    String tag = tabSpec.getTag();
		
		    TabInfo info = new TabInfo(tag, clss, args);
		    mTabs.add(info);
		    mTabHost.addTab(tabSpec);
		    notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
		    return mTabs.size();
		}
		
		@Override
		public android.support.v4.app.Fragment getItem(int position) {
		    TabInfo info = mTabs.get(position);
		    return android.support.v4.app.Fragment.instantiate(mContext, info.clss.getName(), info.args);
		}
		
		public void onTabChanged(String tabId) {
		    int position = mTabHost.getCurrentTab();
		    mViewPager.setCurrentItem(position);
		}
		
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		public void onPageSelected(int position) {
		    // Unfortunately when TabHost changes the current tab, it kindly
		    // also takes care of putting focus on it when not in touch mode.
		    // The jerk.
		    // This hack tries to prevent this from pulling focus out of our
		    // ViewPager.
		    TabWidget widget = mTabHost.getTabWidget();
		    int oldFocusability = widget.getDescendantFocusability();
		    widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		    mTabHost.setCurrentTab(position);
		    widget.setDescendantFocusability(oldFocusability);
		}
		
		public void onPageScrollStateChanged(int state) {
		}
	}
	
	public static void setDisplayUpAsHomeEnabled(Activity activity, boolean enabled) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBarCompatImpl.setDisplayUpAsHomeEnabled(activity, enabled);
		}
	}
	
	public static View setNavigationTabs(Activity activity, List<TabData> tabs) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return ActionBarCompatImpl.setNavigationTabs(activity, tabs);
		} else {
			return OldTabsCompatImpl.setNavigationTabs(activity, tabs);
		}
	}

	public static void setSelectedNavigationItem(
			Activity activity, int index) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBarCompatImpl.setSelectedNavigationIndex(activity, index);
		}
	}

	public static void saveSelectedNavigationIndex(Activity activity, Bundle outState,
			String key) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBarCompatImpl.saveSelectedNavigationIndex(activity, outState, key);
		}
	}
}
