package com.noughmad.ntasks;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;

public class ProjectDetailFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.project_detail, container, false);
	}

	public void setProject(ParseObject project) {
		// TODO Auto-generated method stub
		TextView text = (TextView) getView().findViewById(R.id.project_detail_name);
		text.setText(project.getString("name"));
		
		text = (TextView) getView().findViewById(R.id.project_detail_description);
		text.setText(project.getString("description"));
	}
	
}
