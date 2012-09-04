package com.noughmad.ntasks;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.parse.ParseObject;

public class ProjectDetailFragment extends ListFragment {
	
	private ParseObject mProject;

	public static ProjectDetailFragment create(ParseObject project) {
		ProjectDetailFragment f = new ProjectDetailFragment();
		f.mProject = project;
		return f;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		TaskListAdapter adapter = new TaskListAdapter(getActivity(), mProject);
		setListAdapter(adapter);
		getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> view, View item,
					final int position, long id) {
				if (position == 0) {
					return false;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setItems(R.array.task_actions, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0: // Rename
							// TODO;
							break;
						case 1: // Add Note
							// TODO;
							break;
						case 2: // Delete
							ParseObject task = (ParseObject) getListView().getItemAtPosition(position-1);
							TaskListAdapter adapter = (TaskListAdapter) getListAdapter();
							adapter.mTasks.remove(task);
							adapter.notifyDataSetChanged();
							task.deleteEventually();
						}
					}
				});
				builder.create().show();
				return true;
			}
		});
	}	
}
