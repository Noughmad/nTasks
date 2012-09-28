package com.noughmad.ntasks.tasks;

import com.noughmad.ntasks.Database;
import com.noughmad.ntasks.R;

import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class NoteListAdapter extends CursorAdapter {

	public NoteListAdapter(Context context, Cursor c) {
		super(context, c, 0);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView textView = (TextView)view.findViewById(R.id.note_text);
		textView.setText(cursor.getString(1));
	}

	@Override
	public View newView(final Context context, Cursor cursor, ViewGroup parent) {
		final long noteId = cursor.getLong(0);
		
		View view = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.note_item, parent, false);
		view.findViewById(R.id.note_delete_button).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage("Really delete this note?");
				builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Uri noteUri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, Database.NOTE_TABLE_NAME), noteId);
						ContentProviderClient client = context.getContentResolver().acquireContentProviderClient(noteUri);
						try {
							client.delete(noteUri, null, null);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						client.release();
					}
				});
				builder.create().show();
			}
		});
		bindView(view, context, cursor);
		return view;
	}
}
