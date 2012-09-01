package com.noughmad.ntasks;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.parse.ParseObject;
import com.parse.ParseUser;


public class ProjectDialogFragment extends DialogFragment {

	private ParseObject mProject;

	public static ProjectDialogFragment create(ParseObject project) {
		ProjectDialogFragment dialog = new ProjectDialogFragment();
		dialog.mProject = project;
		return dialog;
	}
	
	public static ProjectDialogFragment create() {
		return create(null);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setIcon(R.drawable.ic_launcher);
		
		final View view = getActivity().getLayoutInflater().inflate(
				R.layout.project_dialog, 
				(ViewGroup)getActivity().findViewById(R.id.project_dialog_layout)
		);
		
		if (mProject != null) {
			((EditText)view.findViewById(R.id.project_title)).setText(mProject.getString("title"));
			((EditText)view.findViewById(R.id.project_client)).setText(mProject.getString("client"));
			
			builder.setTitle(mProject.getString("title"));
		} else {
			builder.setTitle(R.string.add_project);
		}
		
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {		
			public void onClick(DialogInterface dialog, int which) {
				String title = ((EditText)view.findViewById(R.id.project_title)).getText().toString();
				if (title.isEmpty()) {
					return;
				}
				if (mProject == null) {
            		mProject = new ParseObject("Project");
            		mProject.put("user", ParseUser.getCurrentUser());
            	}
        		mProject.put("title", title);
        		mProject.put("client", ((EditText)view.findViewById(R.id.project_client)).getText().toString());
        		mProject.saveEventually();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		return builder.create();
	}
}
