package com.noughmad.ntasks;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;

import com.parse.ParseObject;

public class ProjectDetailFragment extends Fragment {
	
	private ParseObject mProject;
	private TaskListAdapter mAdapter;

	public static ProjectDetailFragment create(ParseObject project) {
		ProjectDetailFragment f = new ProjectDetailFragment();
		f.mProject = project;
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("ProjectDetailFragment", "onCreate(): " + mProject + ", " + mAdapter);
		if (mProject == null && savedInstanceState.containsKey("projectId")) {
			String projectId = savedInstanceState.getString("projectId");
			for (ParseObject project : Utils.projects) {
				if (project.getObjectId().equals(projectId)) {
					mProject = project;
					break;
				}
			}
		}
		Log.d("ProjectDetailFragment", "onCreate(): " + mProject + ", " + mAdapter);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("projectId", mProject.getObjectId());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d("ProjectDetailFragment", "onCreateView(): " + mAdapter);
		ExpandableListView v = new ExpandableListView(inflater.getContext());
		if (mAdapter != null) {
			v.setAdapter(mAdapter);
		}
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Log.d("ProjectDetailFragment", "onActivityCreated(): " + mAdapter);

		if (mAdapter == null) {
			
			mAdapter = new TaskListAdapter(getActivity(), mProject);
			getListView().setAdapter(mAdapter);
			
			getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
	
				public boolean onItemLongClick(AdapterView<?> view, View item,
						final int position, long id) {
					final ParseObject task = (ParseObject) getListView().getItemAtPosition(position);
					if (task == null) {
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
								addNote(task);
								break;
							case 2: // Delete
								TaskListAdapter adapter = (TaskListAdapter) getListView().getExpandableListAdapter();
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
		} else {
			updateTaskList();
		}
	}
	
	ExpandableListView getListView() {
		return (ExpandableListView) getView();
	}
	
	private void addNote(final ParseObject task) {
		final ParseObject note = new ParseObject("Note");
		note.put("task", task);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final EditText edit = new EditText(getActivity());
		edit.setSingleLine(false);
		builder.setView(edit);
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				note.put("text", edit.getText().toString());
				note.saveEventually();
				
				TaskListAdapter adapter = (TaskListAdapter) getListView().getExpandableListAdapter();
				if (adapter != null && adapter.mNotes != null) {
					if (!adapter.mNotes.containsKey(task.getObjectId())) {
						adapter.mNotes.put(task.getObjectId(), new ArrayList<ParseObject>());
					}
					adapter.mNotes.get(task.getObjectId()).add(note);
					adapter.notifyDataSetChanged();
				}
			}
		});
		
		builder.create().show();
	}
	
	public void updateTaskList() {
		TaskListAdapter adapter = (TaskListAdapter) getListView().getExpandableListAdapter();
		adapter.updateTasks(false);
	}
}
