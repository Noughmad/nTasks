package com.noughmad.ntasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
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

	private static String[] from = new String[] {"title", "client", "icon"};
	private static int[] to = new int[] {R.id.project_title, R.id.project_client, R.id.project_image};
	
	private static class ProjectItemBinder implements SimpleAdapter.ViewBinder {

		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			switch (view.getId()) {
			case R.id.project_image:
				// TODO: Create icons and map them to projects
				((ImageView)view).setImageResource(R.drawable.ic_launcher);
				return true;
			}
			return false;
		}
		
	}
	
	private void updateProjectList()
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
				} else {
					Log.e("ProjectListFragment", e.getMessage());
				}
			}
		});
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
		intent.putExtra("project", position);
		startActivity(intent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
				
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
			    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("project-dialog");
			    if (prev != null) {
			        ft.remove(prev);
			    }
			    ft.addToBackStack(null);

			    // Create and show the dialog.
			    
			    ProjectDialogFragment newFragment = ProjectDialogFragment.create(Utils.projects.get(position));
			    newFragment.show(ft, "project-dialog");
			    
			    return true;
			}});
		
		updateProjectList();
	}
	
	
}
