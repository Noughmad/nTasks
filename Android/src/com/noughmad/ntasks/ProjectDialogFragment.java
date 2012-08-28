package com.noughmad.ntasks;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class ProjectDialogFragment extends DialogFragment {

	private ParseObject mProject;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String id = null;
		if (getArguments() != null) {
			id = getArguments().getString("project");
		}
		if (id == null) {
			mProject = null;
		} else {
			ParseQuery query = new ParseQuery("Project");
			query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
			query.getInBackground(id, new GetCallback() {

				@Override
				public void done(ParseObject project, ParseException e) {
					if (e == null) {
						mProject = project;
						
						View v = getView();
						if (v != null) {
							((EditText)v.findViewById(R.id.project_name)).setText(project.getString("name"));
							((EditText)v.findViewById(R.id.project_description)).setText(project.getString("description"));
						}
					} else {
						mProject = null;
					}
				}});
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.project_dialog, container, false);
            
        // Watch for button clicks.
        Button button = (Button)view.findViewById(R.id.button_ok);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (mProject == null) {
            		mProject = new ParseObject("Project");
            		mProject.put("user", ParseUser.getCurrentUser());
            	}
        		mProject.put("name", ((EditText)view.findViewById(R.id.project_name)).getText().toString());
        		mProject.put("description", ((EditText)view.findViewById(R.id.project_description)).getText().toString());
        		mProject.saveEventually();
        		dismiss();
            }
        });
        
        if (mProject != null) {
        	((EditText)view.findViewById(R.id.project_name)).setText(mProject.getString("name"));
			((EditText)view.findViewById(R.id.project_description)).setText(mProject.getString("description"));
        }

        return view;
    }
}
