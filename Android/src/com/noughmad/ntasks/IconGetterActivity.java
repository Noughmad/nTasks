package com.noughmad.ntasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderClient;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

public class IconGetterActivity extends Activity {

	static final int REQUEST_CAMERA = 10;
	static final int REQUEST_GALLERY = 11;
	
	private static final String TAG = "IconGetterActivity";
	private static final String CURRENT_PROJECT = "currentProjectId";

	private long mCurrentProjectId;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == REQUEST_CAMERA) {
			if (resultCode == RESULT_OK) {
				if (mCurrentProjectId < 0) {
					Log.w(TAG, "Project became invalid while taking the picture");
					return;
				}
				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				File file = new File(
					getExternalFilesDir(Environment.DIRECTORY_PICTURES),
					"project_icon_" + mCurrentProjectId + ".png"
				);
				FileOutputStream stream;
				try {
					stream = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return;
				}
				bitmap.compress(CompressFormat.PNG, 6, stream);
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				
				Uri uri = Uri.withAppendedPath(Database.BASE_URI, Database.PROJECT_TABLE_NAME);
				uri = ContentUris.withAppendedId(uri, mCurrentProjectId);
				ContentProviderClient client = getContentResolver().acquireContentProviderClient(uri);
				
				ContentValues values = new ContentValues();
				values.put(Database.KEY_PROJECT_ICON, file.getAbsolutePath());
				try {
					client.update(uri, values, null, null);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				client.release();
				mCurrentProjectId = -1;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mCurrentProjectId = savedInstanceState.getLong(CURRENT_PROJECT, -1);
		} else {
			mCurrentProjectId = -1;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(CURRENT_PROJECT, mCurrentProjectId);
		super.onSaveInstanceState(outState);
	}
	
	void getIconForProject(final long projectId) {
		PackageManager pm = getPackageManager();
		final boolean hasCamera = 
				pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
				&& !pm.queryIntentActivities(
						new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 
						PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
		
		Log.i(TAG, "has camera: " + pm.hasSystemFeature(PackageManager.FEATURE_CAMERA));
		Log.i(TAG, "can use camera: " + pm.queryIntentActivities(
						new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 
						PackageManager.MATCH_DEFAULT_ONLY).size());

		if (hasCamera) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(R.array.icon_sources, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0: // Camera
						getIconFromCamera(projectId);
						break;
					case 1: // Gallery
						getIconFromGallery(projectId);
						break;
					}
				}
			});
			builder.create().show();
		} else {
			getIconFromGallery(projectId);
		}
	}
	
	void getIconFromCamera(long projectId) {
		mCurrentProjectId = projectId;
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, REQUEST_CAMERA);
	}
	
	void getIconFromGallery(long projectId) {}
}
