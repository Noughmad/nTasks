package com.noughmad.ntasks.sync;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.facebook.AsyncFacebookRunner;
import com.parse.facebook.AsyncFacebookRunner.RequestListener;
import com.parse.facebook.FacebookError;
import com.parse.facebook.Util;

public class FacebookLinkPreference extends CheckBoxPreference {
	
	public FacebookLinkPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		ParseUser user = ParseUser.getCurrentUser();
		
		if (user != null && ParseFacebookUtils.isLinked(user)) {
			setAccountName();
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return ParseUser.getCurrentUser() != null && ParseFacebookUtils.isLinked(ParseUser.getCurrentUser());
	}

	@Override
	protected void onClick() {
		ParseUser user = ParseUser.getCurrentUser();

		if (user == null) {
			ParseFacebookUtils.logIn((Activity) getContext(), new LogInCallback() {
				@Override
				public void done(ParseUser user, ParseException e) {
					if (e == null) {
						setChecked(true);
					} else {
						e.printStackTrace();
					}
				}});
		} else {
			boolean linked = ParseFacebookUtils.isLinked(user);
			if (linked) {
				ParseFacebookUtils.unlinkInBackground(user, new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							setChecked(false);
						} else {
							e.printStackTrace();
						}
					}});
			} else {
				ParseFacebookUtils.link(user, (Activity)getContext(), new SaveCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							setChecked(true);
							setAccountName();
						} else {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
	
	private void setAccountName() {
		setSummaryOn("Linked");
		
		AsyncFacebookRunner runner = new AsyncFacebookRunner(ParseFacebookUtils.getFacebook());
		
		runner.request("me", new RequestListener() {

			public void onComplete(String response, Object state) {
				try {
					JSONObject o = Util.parseJson(response);
					final String name = o.getString("name");
					((Activity)getContext()).runOnUiThread(new Runnable() {
						public void run() {
							setSummaryOn("Linked as " + name);
						}});
				} catch (FacebookError e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			public void onFacebookError(FacebookError e, Object state) {
				
			}

			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			public void onIOException(IOException e, Object state) {
			}

			public void onMalformedURLException(MalformedURLException e,
					Object state) {				
			}
		});
	};
	
}
