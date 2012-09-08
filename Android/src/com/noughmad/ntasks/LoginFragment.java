package com.noughmad.ntasks;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

public class LoginFragment extends DialogFragment {
	
	private final static String TAG = "LoginFragment";
	private final LogInCallback mCallback;
	private ProgressDialog mDialog;

	public interface OnLoginListener {
		void onLoggedIn();
	}
	
	public LoginFragment() {
		mCallback = new LogInCallback() {

			@Override
			public void done(ParseUser user, ParseException e) {
				if (mDialog != null) {
					mDialog.dismiss();
				}
				if (user != null) {
					OnLoginListener listener = (OnLoginListener)getActivity();
					listener.onLoggedIn();
				} else {
					TextView messageText = (TextView)getView().findViewById(R.id.loginMessageText);
					if (e != null) {
						Log.i(TAG, "Login error: " + e.getMessage());
						messageText.setText(e.getLocalizedMessage());
					} else {
						messageText.setText("Error Logging In");
					}
					messageText.setVisibility(View.VISIBLE);
				}
			}
		};
	}
	
	private void showLoadingDialog() {
		mDialog = ProgressDialog.show(getActivity(), "", "Loading...", true);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView " + this.toString());
		
        final View v = inflater.inflate(R.layout.login, container, false);
        v.findViewById(R.id.loginButton).setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				String username = ((EditText)v.findViewById(R.id.loginUsername)).getText().toString();
				String password = ((EditText)v.findViewById(R.id.loginPassword)).getText().toString();
				
				showLoadingDialog();
				ParseUser.logInInBackground(username, password, mCallback);
			}});

        v.findViewById(R.id.loginTwitterButton).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				ParseTwitterUtils.logIn(getActivity(), mCallback);
			}
		});
        v.findViewById(R.id.loginFacebookButton).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				ParseFacebookUtils.logIn(getActivity(), mCallback);
			}
		});
        return v;
    }
}
