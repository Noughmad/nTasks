package com.noughmad.ntasks.sync;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.noughmad.ntasks.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LogInPreference extends DialogPreference {
	
	private EditText mUsernameView;
	private EditText mPasswordView;
	private ProgressDialog mLoadingDialog;

	public LogInPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		mUsernameView = (EditText) view.findViewById(R.id.loginUsername);
		mPasswordView = (EditText) view.findViewById(R.id.loginPassword);
		
		if (ParseUser.getCurrentUser() != null) {
			mUsernameView.setText(ParseUser.getCurrentUser().getUsername());
		}
	}
	
	

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setNeutralButton(R.string.register, new DialogInterface.OnClickListener() {
			
			public void onClick(final DialogInterface dialog, int which) {
				final ParseUser user = new ParseUser();
				user.setUsername(mUsernameView.getText().toString());
				user.setPassword(mPasswordView.getText().toString());
				
				showLoadingAlert();
				user.signUpInBackground(new SignUpCallback() {

					@Override
					public void done(ParseException e) {
						hideLoadingAlert();
						if (e == null) {
							Toast.makeText(getContext(), "Registered as " + user.getUsername(), Toast.LENGTH_SHORT).show();
							getContext().startService(new Intent(getContext(), SyncService.class));
							dialog.dismiss();
						} else {
							Toast.makeText(getContext(), "Error during registration", Toast.LENGTH_LONG).show();
							Log.w("LogInPreference", e.getMessage());
						}
					}});
			}
		});
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (positiveResult) {
			String username = mUsernameView.getText().toString();
			String password = mPasswordView.getText().toString();
			
			showLoadingAlert();
			ParseUser.logInInBackground(username, password, new LogInCallback() {

				@Override
				public void done(ParseUser user, ParseException e) {
					hideLoadingAlert();
					if (e == null) {
						Toast.makeText(getContext(), "Logged in as " + user.getUsername(), Toast.LENGTH_SHORT).show();
						
						getContext().startService(new Intent(getContext(), SyncService.class));
					} else {
						Toast.makeText(getContext(), "Error logging in", Toast.LENGTH_LONG).show();
						Log.w("LogInPreference", e.getMessage());
					}
				}});
		}
	}
	
	private void showLoadingAlert() {
		hideLoadingAlert();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setMessage(R.string.loading);
		mLoadingDialog = ProgressDialog.show(getContext(), "", getContext().getResources().getString(R.string.loading), true);
	}
	
	private void hideLoadingAlert() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
	}
}
