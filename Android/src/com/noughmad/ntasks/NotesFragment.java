package com.noughmad.ntasks;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NotesFragment extends Fragment 
	implements TextWatcher, LoaderManager.LoaderCallbacks<Cursor> {
	
	private long mProjectId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && savedInstanceState.containsKey("projectId")) {
			mProjectId = savedInstanceState.getLong("projectId");
		} else if (getArguments() != null) {
			mProjectId = getArguments().getLong("projectId", -1);
		} else {
			mProjectId = -1;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("projectId", mProjectId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.notes, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}

	public void afterTextChanged(Editable s) {
		Uri uri = Database.withId(Database.PROJECT_TABLE_NAME, mProjectId);
		ContentProviderClient client = getActivity().getContentResolver().acquireContentProviderClient(uri);
		
		ContentValues values = new ContentValues();
		values.put(Database.KEY_PROJECT_NOTE, s.toString());
		
		try {
			client.update(uri, values, null, null);
		} catch (RemoteException e) {
			e.printStackTrace();
		} finally {
			client.release();
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {		
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Database.withId(Database.PROJECT_TABLE_NAME, mProjectId);
		return new CursorLoader(getActivity(), uri, new String[] {Database.KEY_PROJECT_NOTE}, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor.moveToFirst()) {
			EditText edit = (EditText) getView().findViewById(R.id.note_text);
			if (edit == null) {
				return;
			}
			edit.removeTextChangedListener(this);
			edit.setText(cursor.getString(0));
			edit.addTextChangedListener(this);
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		EditText edit = (EditText) getView().findViewById(R.id.note_text);
		edit.removeTextChangedListener(this);
		edit.setText("");
	}

	public void showProject(long id) {
		mProjectId = id;
		getLoaderManager().restartLoader(0,  null,  this);		
	}
}
