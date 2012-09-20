package com.noughmad.ntasks;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.parse.ParseException;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class TwitterLinkPreference extends CheckBoxPreference {

	public TwitterLinkPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		ParseUser user = ParseUser.getCurrentUser();
		setEnabled(user != null && user.isAuthenticated());
		
		if (ParseTwitterUtils.isLinked(ParseUser.getCurrentUser())) {
			setSummaryOn("Linked as " + ParseTwitterUtils.getTwitter().getScreenName());
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return ParseTwitterUtils.isLinked(ParseUser.getCurrentUser());
	}

	@Override
	protected void onClick() {
		ParseUser user = ParseUser.getCurrentUser();
		boolean linked = ParseTwitterUtils.isLinked(user);
		if (linked) {
			ParseTwitterUtils.unlinkInBackground(user, new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null) {
						setChecked(false);
					} else {
						e.printStackTrace();
					}
				}});
		} else {
			ParseTwitterUtils.link(user, getContext(), new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null) {
						setSummaryOn("Linked with " + ParseTwitterUtils.getTwitter().getUserId());
						setChecked(true);
					} else {
						e.printStackTrace();
					}
				}});
		}
	}

	
}
