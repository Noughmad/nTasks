package com.noughmad.ntasks.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.BaseColumns;

import com.noughmad.ntasks.Database;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class Bridge {
	
	public final static String OBJECT_TABLE_NAME = "Object";
	public final static String KEY_OBJECT_PARSEID = "parseObjectId";
	public final static String KEY_OBJECT_CLASSNAME = "className";
	public final static String KEY_OBJECT_CREATED = "createdAt";
	public final static String KEY_OBJECT_MODIFIED = "updatedAt";
	public final static String KEY_OBJECT_LOCAL = "local";
	
	public final static String OBJECT_TABLE_CREATE = 
			"CREATE TABLE " + OBJECT_TABLE_NAME + " (" +
	                BaseColumns._ID + " INTEGER PRIMARY KEY, " +
	                KEY_OBJECT_PARSEID + " TEXT, " +
	                KEY_OBJECT_CLASSNAME + " TEXT, " +
	                KEY_OBJECT_CREATED + " INTEGER, " +
	                KEY_OBJECT_MODIFIED + " INTEGER, " +
	                KEY_OBJECT_LOCAL + " INTEGER);";
	
	public final static String[] objectColumns = new String[] {
		KEY_OBJECT_PARSEID
	};
	
	public static class Options {
		String className;
		Map<String, String> foreignKeys;
		List<String> dateColumns;
		List<String> textColumns;
		List<String> intColumns;
		List<String> fileColumns;
	}

	private Context mActivity;
	
	Bridge(Context context) {
		mActivity = context;
	}
	
	void upload(final Options options) throws RemoteException, ParseException {
		Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.IS_LOCAL + "/" + options.className);
		ContentProviderClient client = mActivity.getContentResolver().acquireContentProviderClient(uri);
		
		List<String> columns = new ArrayList<String>();
		columns.add(options.className + "." + Database.ID);
		columns.add(Database.KEY_OBJECT);
		columns.add(KEY_OBJECT_PARSEID);
		if (options.foreignKeys != null) {
			columns.addAll(options.foreignKeys.keySet());
		}
		if (options.textColumns != null) {
			columns.addAll(options.textColumns);
		}
		if (options.intColumns != null) {
			columns.addAll(options.intColumns);
		}
		if (options.dateColumns != null) {
			columns.addAll(options.dateColumns);
		}
		if (options.fileColumns != null) {
			columns.addAll(options.fileColumns);
		}
		Map<Long,ParseObject> objects = new HashMap<Long,ParseObject>();
		
		Cursor cursor = client.query(uri, columns.toArray(new String[] {}), null, null, null);
		if (!cursor.moveToFirst()) {
			return;
		}
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(0);
			
			ParseObject object = new ParseObject(options.className);
			int i = 3;
			for (Entry<String,String> entry : options.foreignKeys.entrySet()) {
				Uri keyUri = ContentUris.withAppendedId(Uri.withAppendedPath(Database.BASE_URI, OBJECT_TABLE_NAME), cursor.getLong(i));
				Cursor keyCursor = client.query(keyUri, new String[] {KEY_OBJECT_PARSEID}, null, null, null);
				ParseObject key = new ParseObject(entry.getValue());
				key.setObjectId(keyCursor.getString(0));
				object.put(entry.getKey(), key);
				++i;
			}
			for (String entry : options.textColumns) {
				object.put(entry, cursor.getString(i));
				++i;
			}
			for (String entry : options.intColumns) {
				object.put(entry, cursor.getInt(i));
				++i;
			}
			for (String entry : options.dateColumns) {
				object.put(entry, new Date(cursor.getLong(i)));
				++i;
			}
			for (String entry : options.fileColumns) {
				File file = new File(cursor.getString(i));
				if (file.exists() && file.isFile()) {
					byte[] buffer = new byte[(int) file.length()];
					try {
						FileInputStream stream = new FileInputStream(file);
						stream.read(buffer);
						stream.close();

						ParseFile pf = new ParseFile("icon.png", buffer);
						pf.save();
						
						object.put(entry, file);
					} catch (IOException e) {
						e.printStackTrace();
						object.remove(entry);
					}
				}
				++i;
			}
			
			objects.put(id, object);
		}
		
		client.release();
		
		ParseObject.saveAll(new ArrayList<ParseObject>(objects.values()));

		uri = Uri.withAppendedPath(Database.BASE_URI, OBJECT_TABLE_NAME);
		client = mActivity.getContentResolver().acquireContentProviderClient(uri);
		ContentValues values = new ContentValues();
		for (Entry<Long,ParseObject> entry : objects.entrySet()) {
			values.put(KEY_OBJECT_PARSEID, entry.getValue().getObjectId());
			client.update(ContentUris.withAppendedId(uri, entry.getKey()), values, null, null);
		}
		values.clear();
		values.put(KEY_OBJECT_LOCAL, 0);
		client.update(uri, values, KEY_OBJECT_CLASSNAME + " = ?", new String[] {options.className});
		client.release();
	}
	
	void download(final Options options) throws ParseException, RemoteException {		
		// TODO:
		long lastSync = 1000; 
		
		final Uri uri = Uri.withAppendedPath(Database.BASE_URI, options.className);

		ParseQuery query = new ParseQuery(options.className);
		query.whereGreaterThan("updatedAt", lastSync);
		List<ParseObject> objects = query.find();
		ContentProviderClient client = mActivity.getContentResolver().acquireContentProviderClient(uri);
		for (ParseObject object : objects) {
			Cursor existing = client.query(Uri.withAppendedPath(uri, Database.EXISTS_PARSE_ID + "/" + object.getObjectId()), new String[] {options.className + '.' + Database.ID}, null, null, null);
			boolean exists = existing.moveToFirst();
			existing.close();
			
			ContentValues values = new ContentValues();
			for (String entry : options.textColumns) {
				values.put(entry, object.getString(entry));
			}
			for (String entry : options.intColumns) {
				values.put(entry, object.getInt(entry));
			}
			for (String entry : options.dateColumns) {
				values.put(entry, object.getDate(entry).getTime());
			}
			for (String entry : options.fileColumns) {
				ParseFile pf = (ParseFile) object.get(entry);
				byte[] buffer = pf.getData();
				
				File file = new File(
					mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
					"project_icon_" + object.getObjectId() + ".png"
				);
				try {
					FileOutputStream stream = new FileOutputStream(file);
					stream.write(buffer);
					stream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				values.put(entry, file.getAbsolutePath());
			}
			if (exists) {
				// A record for this object already exists
				long id = existing.getLong(0);
				client.update(ContentUris.withAppendedId(uri, id), values, null, null);
			} else {
				client.insert(uri, values);
			}
		}
	}
	
	void sync(Options options) throws RemoteException, ParseException {
		upload(options);
		download(options);
		
		// TODO: Delete records from either the server or the client, depending on their age
		
	}
	
}
