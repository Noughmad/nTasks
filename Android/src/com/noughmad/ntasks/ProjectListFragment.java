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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ProjectListFragment extends ListFragment {

	private static String[] from = new String[] {"name", "description", "icon"};
	private static int[] to = new int[] {R.id.project_name, R.id.project_description, R.id.project_image};
		
	private List<ParseObject> mProjects;
	
	private static class ProjectItemBinder implements SimpleAdapter.ViewBinder {

		public boolean setViewValue(View view, Object data,
				String textRepresentation) {
			Log.d("ProjectListFragment", "Binding text " + textRepresentation + " to view " + view.toString());
			switch (view.getId()) {
			case R.id.project_image:
				// TODO: Create icons and map them to projects
				((ImageView)view).setImageResource(R.drawable.ic_launcher);
				return true;
				
			case R.id.project_name:
				TextView textView = (TextView)view;
				textView.setText(textRepresentation);
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
					mProjects = projects;
					List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
					for (ParseObject project : projects) {
						Map<String, Object> map = new HashMap<String, Object>();
						Log.d("ProjectListFragment", "Found project " + project.getString("name"));
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.project_list, container, false);
		
		Button button = (Button)v.findViewById(R.id.project_add);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
			    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("project-dialog");
			    if (prev != null) {
			        ft.remove(prev);
			    }
			    ft.addToBackStack(null);

			    // Create and show the dialog.
			    ProjectDialogFragment newFragment = new ProjectDialogFragment();
			    newFragment.show(ft, "project-dialog");
			}
		});
		
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
				intent.putExtra("project", mProjects.get(position).getObjectId());
				startActivity(intent);
			}
		});
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
			    Fragment prev = getActivity().getFragmentManager().findFragmentByTag("project-dialog");
			    if (prev != null) {
			        ft.remove(prev);
			    }
			    ft.addToBackStack(null);

			    // Create and show the dialog.
			    ProjectDialogFragment newFragment = new ProjectDialogFragment();
			    Bundle args = new Bundle();
			    args.putString("project", mProjects.get(position).getObjectId());
			    newFragment.setArguments(args);
			    newFragment.show(ft, "project-dialog");
			    
			    return true;
			}});
		
		updateProjectList();
	}
	
	
}
