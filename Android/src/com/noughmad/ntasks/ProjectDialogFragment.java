package com.noughmad.ntasks;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

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
		
		builder.setIcon(R.drawable.ic_launcher_light);
		
		final View view = getActivity().getLayoutInflater().inflate(
				R.layout.project_dialog, 
				(ViewGroup)getActivity().findViewById(R.id.project_dialog_layout)
		);
		
		
		CategoryAdapter adapter = new CategoryAdapter(getActivity(), android.R.layout.simple_spinner_item, getActivity().getResources().getStringArray(R.array.project_categories), true);
		Spinner spinner = (Spinner) view.findViewById(R.id.project_category);
		spinner.setAdapter(adapter);
		int category = 4;
		
		if (mProject != null) {
			((EditText)view.findViewById(R.id.project_title)).setText(mProject.getString("title"));
			((EditText)view.findViewById(R.id.project_client)).setText(mProject.getString("client"));
			if (mProject.has("category")) {
				category = Math.min(Math.max(mProject.getInt("category"), 0), 4);
			}
			
			builder.setTitle(mProject.getString("title"));
		} else {
			builder.setTitle(R.string.add_project);
		}
		
		spinner.setSelection(category);
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
        		mProject.put("category", ((Spinner)view.findViewById(R.id.project_category)).getSelectedItemPosition());
        		mProject.saveEventually();
        		
        		ProjectListFragment list = (ProjectListFragment) getActivity().getFragmentManager().findFragmentByTag("project-list");
        		if (list != null) {
        			list.updateProjectList();
        		}
        		
        		ProjectDetailFragment detail = (ProjectDetailFragment) getActivity().getFragmentManager().findFragmentByTag("project-detail-" + mProject.getObjectId());
        		if (detail != null) {
        			detail.updateTaskList();
        		}
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
