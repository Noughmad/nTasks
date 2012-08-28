package com.noughmad.ntasks;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterFragment extends Fragment {
	
	private final static String TAG = "RegisterFragment";
	
	public interface OnRegisterListener {
		public void onRegistered();
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView");
		
        View v = inflater.inflate(R.layout.register, container, false);
        v.findViewById(R.id.registerButton).setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				getActivity().findViewById(R.id.registerButton).setVisibility(View.GONE);
				getActivity().findViewById(R.id.registerMessageText).setVisibility(View.GONE);
				getActivity().findViewById(R.id.registerProgress).setVisibility(View.VISIBLE);

				String username = ((EditText)getActivity().findViewById(R.id.registerUsername)).getText().toString();
				String email = ((EditText)getActivity().findViewById(R.id.registerEmail)).getText().toString();
				String password = ((EditText)getActivity().findViewById(R.id.registerPassword)).getText().toString();
				
				ParseUser user = new ParseUser();
				user.setUsername(username);
				user.setEmail(email);
				user.setPassword(password);
				user.signUpInBackground(new SignUpCallback() {
					
					@Override
					public void done(ParseException e) {
						if (e == null) {
							OnRegisterListener listener = (OnRegisterListener)getActivity();
							listener.onRegistered();
						} else {
							Log.i(TAG, "Error: " + e.getMessage());
							getActivity().findViewById(R.id.registerProgress).setVisibility(View.GONE);
							getActivity().findViewById(R.id.registerButton).setVisibility(View.VISIBLE);
							TextView messageText = (TextView)getActivity().findViewById(R.id.registerMessageText);
							messageText.setText(e.getLocalizedMessage());
							messageText.setVisibility(View.VISIBLE);
						}
					}
				});
			}});
        return v;
    }

}
