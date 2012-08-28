package com.noughmad.ntasks;

import android.app.DialogFragment;
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
import com.parse.ParseUser;

public class LoginFragment extends DialogFragment {
	
	private final static String TAG = "LoginFragment";

	public interface OnLoginListener {
		void onLoggedIn();
	}
	
	public LoginFragment() {
		
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView " + this.toString());
		
        final View v = inflater.inflate(R.layout.login, container, false);
        v.findViewById(R.id.loginButton).setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				v.findViewById(R.id.loginButton).setVisibility(View.GONE);
				v.findViewById(R.id.loginMessageText).setVisibility(View.GONE);
				v.findViewById(R.id.loginProgress).setVisibility(View.VISIBLE);
				
				String username = ((EditText)v.findViewById(R.id.loginUsername)).getText().toString();
				String password = ((EditText)v.findViewById(R.id.loginPassword)).getText().toString();
				ParseUser.logInInBackground(username, password, new LogInCallback() {
					
					@Override
					public void done(ParseUser user, ParseException e) {
						if (e == null) {
							OnLoginListener listener = (OnLoginListener)getActivity();
							listener.onLoggedIn();
						} else {
							Log.i(TAG, "Login error: " + e.getMessage());
							v.findViewById(R.id.loginProgress).setVisibility(View.GONE);
							v.findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
							TextView messageText = (TextView)v.findViewById(R.id.loginMessageText);
							messageText.setText(e.getLocalizedMessage());
							messageText.setVisibility(View.VISIBLE);
						}
					}
				});
			}});
        return v;
    }
}
