package com.noughmad.ntasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ProjectListFragment extends ListFragment {

	private static String[] from = new String[] {"title", "client", "category"};
	private static int[] to = new int[] {R.id.project_title, R.id.project_client, R.id.project_image};
	
	private static class ProjectItemBinder implements SimpleAdapter.ViewBinder {

		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			switch (view.getId()) {
			case R.id.project_image:
				int category = 4;
				if (data != null) {
					category = Integer.parseInt(textRepresentation);
				}
				((ImageView)view).setImageResource(Utils.getLargeCategoryDrawable(category));
				return true;
			}
			return false;
		}
		
	}
	
	public void updateProjectList()
	{
		ParseQuery query = new ParseQuery("Project");
		query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
		query.whereEqualTo("user", ParseUser.getCurrentUser());
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> projects, ParseException e) {
				if (e == null) {
					Log.i("ProjectListFragment", "Retrieved " + projects.size() + " projects");
					Utils.projects = projects;
					setProjects(projects);
				} else {
					Log.e("ProjectListFragment", e.getMessage());
				}
			}
		});
	}
	
	private void setProjects(List<ParseObject> projects) {
		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
		for (ParseObject project : projects) {
			Map<String, Object> map = new HashMap<String, Object>();
			Log.d("ProjectListFragment", "Found project " + project.getString("title"));
			for (String key : from)
			{
				map.put(key, project.get(key));
			}
			list.add(map);
		}
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.project_item, from, to);
		adapter.setViewBinder(new ProjectItemBinder());
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i("ProjectListFragment", "Starting project detail activity for project " + position + " => "+ Utils.projects.get(position).getString("title"));
		Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
		intent.putExtra("project", position);
		startActivity(intent);
	}
	
	public void onListItemLongClick(ListView l, View v, final int position, long id) {
		new AlertDialog.Builder(getActivity()).setItems(R.array.project_actions, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0: // Edit
					Log.i("ProjectListFragment", "Starting project detail activity for project " + position + " => "+ Utils.projects.get(position).getString("title"));
					Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
					intent.putExtra("project", position);
					startActivity(intent);
					break;
				case 1: // Delete
					final ParseObject project = Utils.projects.get(position);
					new AlertDialog.Builder(getActivity())
						.setTitle("Really delete " + project.getString("title") + "?")
						.setMessage("Deleting a project will delete all its tasks, and cannot be undone. Do you really want to delete " + project.getString("title") + "?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								project.deleteEventually();
								Utils.projects.remove(project);
								updateProjectList();
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.create().show();
				}
			}
		}).create().show();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
				
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemLongClick((ListView) parent, view, position, id);
			    return true;
			}});
		
		if (Utils.projects != null) {
			setProjects(Utils.projects);
		}
		updateProjectList();
	}
	
	
}
