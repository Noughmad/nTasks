package com.noughmad.ntasks;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;


public class ProjectDialogFragment extends DialogFragment {

	private long mProjectId;
	private boolean mExisting;
	
	public static ProjectDialogFragment create(long id) {
		ProjectDialogFragment dialog = new ProjectDialogFragment();
		dialog.mExisting = true;
		dialog.mProjectId = id;
		return dialog;
	}
	
	public static ProjectDialogFragment create() {
		ProjectDialogFragment dialog = new ProjectDialogFragment();
		dialog.mExisting = false;
		return dialog;
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
		if (mExisting) {
			Database db = new Database(getActivity());
			Cursor cursor = db.getProject(mProjectId, new String[] {
					Database.KEY_PROJECT_TITLE, 
					Database.KEY_PROJECT_CLIENT, 
					Database.KEY_PROJECT_CATEGORY});
			
			String title = cursor.getString(0);
			
			((EditText)view.findViewById(R.id.project_title)).setText(title);
			((EditText)view.findViewById(R.id.project_client)).setText(cursor.getString(1));
			category = Math.min(Math.max(cursor.getInt(2), 0), 4);
			
			builder.setTitle(title);
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
				ContentValues values = new ContentValues();
				values.put(Database.KEY_PROJECT_TITLE, title);
        		values.put(Database.KEY_PROJECT_CLIENT, ((EditText)view.findViewById(R.id.project_client)).getText().toString());
        		values.put(Database.KEY_PROJECT_CATEGORY, ((Spinner)view.findViewById(R.id.project_category)).getSelectedItemPosition());
				
				Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME);
				if (mExisting) {
					uri = ContentUris.withAppendedId(uri, mProjectId);
				}
				ContentProviderClient client = getActivity().getContentResolver().acquireContentProviderClient(uri);
				if (mExisting) {
					try {
						client.update(uri, values, null, null);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
            	} else {
            		try {
						client.insert(uri, values);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
            	}
				client.release();
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
