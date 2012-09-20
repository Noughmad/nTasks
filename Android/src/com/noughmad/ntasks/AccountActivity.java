package com.noughmad.ntasks;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParseFacebookUtils;

public class AccountActivity extends Activity implements LoginFragment.OnLoginListener, RegisterFragment.OnRegisterListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.account);
		
		// TODO: Only use tabs if the screen is narrow or small
		// On large, wide screens, simply show both login and register views
		
		Log.i("AccountActivity", "Starting an AccountActivity");

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        bar.addTab(bar.newTab()
                .setText("Log In")
                .setTabListener(new TabListener<LoginFragment>(
                        this, "login", LoginFragment.class)));
        
        Log.d("AccountActivity", "Added the login tab");
        
        bar.addTab(bar.newTab()
                .setText("Sign Up")
                .setTabListener(new TabListener<RegisterFragment>(
                        this, "register", RegisterFragment.class)));
        
        Log.d("AccountActivity", "Added the register tab");

        if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        } else {
        	bar.setSelectedNavigationItem(0);
        }
	}
	
	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final Bundle mArgs;
        private Fragment mFragment;

        public TabListener(Activity activity, String tag, Class<T> clz) {
            this(activity, tag, clz, null);
        }

        public TabListener(Activity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;
            
            Log.d("TabListener", "TabListener");

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
            	Log.d("TabListener", "Fragemnt is attached");
                FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
            
            Log.d("TabListener", "Created a tab listener");
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
        	Log.d("TabListener", "onTabSelected");
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(R.id.account_container, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        	Log.d("TabListener", "onTabUnselected");
            if (mFragment != null) {
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        	Log.d("TabListener", "onTabReselected");
        }
    }

	public void onRegistered() {
		onLoggedIn();
	}

	public void onLoggedIn() {
		Intent i = getIntent();
		setResult(RESULT_OK, i);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}
}
