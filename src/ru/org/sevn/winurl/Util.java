/*******************************************************************************
 * Copyright 2017 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.org.sevn.winurl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.widget.Toast;

public class Util {
	public static void toast(String text, Context context) {
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();		
	}

    public static void chooseFile2selectDir2save(Activity activity, int result_ID) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
        	activity.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), result_ID);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
    }

	public static String getFilename(Uri uri, Context context) { 
	    String fileName = null;
	    String scheme = uri.getScheme();
	    if (scheme.equals("file")) {
	        fileName = uri.getLastPathSegment();
	    }
	    else if (scheme.equals("content")) {
	        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
	        try {
	          if (cursor != null && cursor.moveToFirst()) {
	        	  fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
	          }
	        } finally {
	          cursor.close();
	        }	        
	    }
	    return fileName;
	}
	
	public static String noExt(String s) {
		return s;
	}
	public static boolean fileExists(String fileName) {
		if (fileName != null) {
			File fl = new File(fileName);
			return fl.exists();
		}
		return false;
	}
	public static Intent makeCustomChooserIntent(Context ctx, CharSequence chooserTitle, Intent protoIntent, Collection<String> forbiddenChoices, Collection<String> allowedChoices, final String firstChoice) {
		List<Intent> targetedShareIntents = new ArrayList();
		List<HashMap<String, String>> intentMetaInfo = new ArrayList<HashMap<String, String>>();
		Intent chooserIntent;

		Intent dummy = new Intent(protoIntent.getAction());
		dummy.setType(protoIntent.getType());
		List<ResolveInfo> resInfo = ctx.getPackageManager().queryIntentActivities(dummy, 0);

		if (!resInfo.isEmpty()) {
			for (ResolveInfo resolveInfo : resInfo) {
				if (resolveInfo.activityInfo == null || 
						forbiddenChoices != null && forbiddenChoices.contains(resolveInfo.activityInfo.packageName)
						) {
					
					continue;
				}
				if (allowedChoices == null ||
						allowedChoices != null && allowedChoices.contains(resolveInfo.activityInfo.packageName)) { 

					HashMap<String, String> info = new HashMap<String, String>();
					info.put("packageName", resolveInfo.activityInfo.packageName);
					info.put("className", resolveInfo.activityInfo.name);
					info.put("simpleName", String.valueOf(resolveInfo.activityInfo.loadLabel(ctx.getPackageManager())));
					intentMetaInfo.add(info);
				}
			}

			if (!intentMetaInfo.isEmpty()) {
				Collections.sort(intentMetaInfo, new Comparator<HashMap<String, String>>() {
					@Override
					public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
						if (contains(map.get("simpleName"), firstChoice)) {
							if (contains(map2.get("simpleName"), firstChoice)) {
							} else {
								return -1;
							}
						} else
						if (contains(map2.get("simpleName"), firstChoice)) {
							if (contains(map.get("simpleName"), firstChoice)) {
							} else {
								return 1;
							}
						}
						return map.get("simpleName").compareTo(map2.get("simpleName"));
					}
				});

				for (HashMap<String, String> metaInfo : intentMetaInfo) {
					Intent targetedShareIntent = (Intent) protoIntent.clone();
					targetedShareIntent.setPackage(metaInfo.get("packageName"));
					targetedShareIntent.setClassName(metaInfo.get("packageName"), metaInfo.get("className"));
					targetedShareIntents.add(targetedShareIntent);
				}

				chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), chooserTitle);
				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
				return chooserIntent;
			}
		}

		return Intent.createChooser(protoIntent, chooserTitle);
	}
	
	private static boolean contains(String s1, String s2) {
		if (s1 != null) {
			return s1.toLowerCase().contains(s2.toLowerCase());
		}
		return false;
	}
}
