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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

public class ExtraData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2402772120833604929L;
	private transient ArrayList<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
	private transient ArrayList<PropertyChangeListener> listenersBefore = new ArrayList<PropertyChangeListener>();
	
	public static final String PROP_sharedText = "sharedText";
	public static final String PROP_sharedSubj = "sharedSubj";
	public static final String PROP_sharedName = "sharedName";
	public static final String PROP_fileName = "fileName";
	public static final String PROP_changed = "changed";
	private String sharedText;
	private String sharedSubj;
	private String sharedName;
	private String fileName;
	
	private transient boolean initialized;
	private transient boolean changed;
	private transient ExtraData dataState;
	
	ExtraData() {}
	ExtraData(String url, String name) {
		sharedSubj = name;
		sharedText = url;
	}
	
	public void copyFrom(ExtraData ed) {
		setSharedName(ed.getSharedName());
		setSharedSubj(ed.getSharedSubj());
		setSharedText(ed.getSharedText());
		setFileName(ed.getFileName());
		initialized = true;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_changed, this.changed, false);
		this.changed = false;
		fireEvent(event, true);
	}
	
	public void save(SharedPreferences prefs) {
		Editor ed = prefs.edit();
		String prefixName = ExtraData.class.getName();
		ed.putString(prefixName + PROP_sharedText, sharedText);
		ed.putString(prefixName + PROP_sharedSubj, sharedSubj);
		ed.putString(prefixName + PROP_sharedName, sharedName);
		ed.putString(prefixName + PROP_fileName, fileName);
		ed.commit();
		initialized = true;
	}
	
	public void load(SharedPreferences prefs) {
		String prefixName = ExtraData.class.getName();
		setSharedName(prefs.getString(prefixName + PROP_sharedName, ""));
		setSharedSubj(prefs.getString(prefixName + PROP_sharedSubj, ""));
		setSharedText(prefs.getString(prefixName + PROP_sharedText, ""));
		setFileName(prefs.getString(prefixName + PROP_fileName, ""));
		initialized = true;
	}
	
	public void setPropertyChangeListener(PropertyChangeListener l) {
		listeners.clear();
		listeners.add(l);
	}
	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.add(l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l) {
		listeners.remove(l);
	}
	
	public String urlContent() {
	    String urlContent = "[InternetShortcut]\n";
	    if (sharedText != null) urlContent += ("URL=" + sharedText + "\n");
	    if (sharedSubj != null) urlContent += ("Comment=" + sharedSubj + "\n");
	    return urlContent;
	}
	public String getSharedText() {
		return sharedText;
	}
	public ExtraData setSharedText(String sharedText) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_sharedText, this.sharedText, sharedText);
		fireEventBefore(event);
		this.sharedText = sharedText;
		fireEvent(event);
		return this;
	}
	public String getSharedSubj() {
		return sharedSubj;
	}
	public ExtraData setSharedSubj(String sharedSubj) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_sharedSubj, this.sharedSubj, sharedSubj);
		fireEventBefore(event);
		this.sharedSubj = sharedSubj;
		fireEvent(event);
		return this;
	}
	public String getSharedName() {
		return sharedName;
	}
	public ExtraData setSharedName(String sharedName) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_sharedName, this.sharedName, sharedName);
		fireEventBefore(event);
		this.sharedName = sharedName;
		fireEvent(event);
		return this;
	}
	public String getFileName() {
		return fileName;
	}
	public ExtraData setFileName(String fileName) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_fileName, this.fileName, fileName);
		fireEventBefore(event);
		this.fileName = fileName;
		fireEvent(event);
		return this;
	}
	private void fireEvent(PropertyChangeEvent event) {
		fireEvent(event, false);
	}
	private void fireEventBefore(PropertyChangeEvent event) {
		boolean aboutChange = fireEvent(event, false, listenersBefore);
		if (!changed && aboutChange) {
			fixState();
		}
	}
	private void fireEvent(PropertyChangeEvent event, boolean dontchange) {
		boolean ret = fireEvent(event, dontchange, listeners);
		if (ret) {
			if (likeTo(dataState)) {
				ret = false;
			}
		}
		changed = ret;
	}
	private boolean fireEvent(PropertyChangeEvent event, boolean dontchange, ArrayList<PropertyChangeListener> listeners) {
		boolean ret = changed;
		if (event.getOldValue() == event.getNewValue()) return ret;
		if (event.getOldValue() != null && event.getOldValue().equals(event.getNewValue())) return ret;
		
		if (initialized && !dontchange) {
			ret = true;
		}
			
		for(PropertyChangeListener l : listeners) {
			l.propertyChange(event);
		}
		return ret;
	}
	public static ExtraData parseUrlContent(String s, String defaultSubj) {
		ExtraData ed = new ExtraData();
		String parsed[] = getUrlAndName(s);
		ed.setSharedText(parsed[0]);
		ed.setSharedSubj(parsed[1]);
		if (ed.getSharedSubj() == null) {
			ed.setSharedSubj(defaultSubj);
		}
		ed.setSharedName(defaultSubj);
		System.out.println(ed.urlContent());
		ed.changed = false;
		return ed;
	}
	
	public static String[] getUrlAndName(String input) {
		String[] urlname = new String[2];
        Pattern p = Pattern.compile("(?i)^(\\[InternetShortcut\\]).*(\n.*)*\nURL=(.*)((\n.*)*)?$");
        Matcher m = p.matcher(input);
        if (m.matches()) {
        	
        	int groups = m.groupCount();
        	
        	if(groups > 2) {
        		urlname[0] = m.group(3);//m.group("URL");
        	}
        	input = null;
        	if(groups > 3) {
        		input = m.group(4);//m.group("CMT");
        	}
        	if (input != null) {
                Pattern pCmt = Pattern.compile("(?i)^(\n)*(Comment=(.*))?(\n.*)*$");
                Matcher mCmt = pCmt.matcher(input);
                if (mCmt.matches()) {
	                int grps = mCmt.groupCount();
                	if(grps > 2) {
                		urlname[1] = mCmt.group(3);//mCmt.group("CMTVAL");
                	}
            	}
        	}
        }
        return urlname;
	}	
	
	private Uri getSaveUri(Uri uri, String newFile) {
		File selectedDir = null;
		File selectedFile = new File(uri.getPath());
		if (uri.getPath().matches("(?i).*\\.url")) {
			if (selectedFile.isDirectory()) {
				selectedDir = selectedFile;
			} else {
				return uri;
			}
		}
		if (selectedDir == null) {
			if (selectedFile.isDirectory()) {
				selectedDir = selectedFile;
			} else {
				selectedDir = selectedFile.getParentFile();
			}
		}
		File fileNew = new File(selectedDir, newFile);
		return Uri.fromFile(fileNew);
	}
	
	private static final String STR_URL = ".url";
	private String getNoExt(String s) {
		if (s != null) {
			if (s.toLowerCase().endsWith(STR_URL)) {
				return s.substring(0, s.length() - STR_URL.length());
			}
		}
		return s;
	}
	private boolean isNullName(String s) {
		return s == null || s.trim().length() == 0 || s.equalsIgnoreCase(STR_URL);
	}
	private String getAny(String s, String s2) {
		if (isNullName(s)) {
			return s2;
		}
		return s;
	}
	
	public JSONObject getJSON() throws JSONException {
		JSONObject jobj = new JSONObject();
		jobj.put(PROP_sharedText, sharedText);
		jobj.put(PROP_sharedSubj, sharedSubj);
		jobj.put(PROP_sharedName, sharedName);
		jobj.put(PROP_fileName, fileName);
		return jobj;
	}
	
	@Override
	public String toString() {
		try {
			return getJSON().toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.toString();
	}
	
	public File saveInFile(Uri uri, Activity activity) {
		File selectedFile = null;
		if (uri == null) return selectedFile;
		uri = getSaveUri(uri, IOUtil.normalizeFileName(getAny(getAny(IOUtil.trimStart(getNoExt(getSharedName()), '.'), IOUtil.trimStart(getNoExt(getSharedSubj()),'.')), "untitled")) + ".url");
    	if (uri.getPath().matches("(?i).*\\.url") && getSharedSubj() != null) {
    		//File sdCardDir = Environment.getExternalStorageDirectory();
    		selectedFile = new File(uri.getPath());
    		
    		FileOutputStream outStream = null;
    		try {
				outStream = new FileOutputStream(selectedFile);
				setSharedName(selectedFile.getName());
				outStream.write(urlContent().getBytes("UTF-8"));
				setFileName(selectedFile.getAbsolutePath());
				setChanged(false);
				Util.toast("Saved in " + selectedFile, activity);
				return selectedFile;
			} catch (FileNotFoundException e) {
				Util.toast("Can't write into file:" + selectedFile, activity);
			} catch (UnsupportedEncodingException e) {
				Util.toast("Can't write into file (unsupported exception):" + selectedFile, activity);
			} catch (IOException e) {
				Util.toast("Can't write into file (IO error):" + selectedFile, activity);
			} finally {
				IOUtil.close(outStream);
			}
    	}
		return null;
	}
	public boolean isInitialized() {
		return initialized;
	}
	public boolean isChanged() {
		return changed;
	}
	private void setChanged(boolean changed) {
		PropertyChangeEvent event = new PropertyChangeEvent(this, PROP_changed, this.changed, changed);
		this.changed = changed;
		if (!changed) {
			fixState();
		}
		fireEvent(event, true);
	}
	private void fixState() {
		dataState = new ExtraData();
		dataState.copyFrom(this);
	}
	private boolean eq(String s1, String s2) {
		if (s1 == null && s2 == null || s1 != null && s1.equals(s2)) {
			return true;
		}
		return false;
	}
	private boolean likeTo(ExtraData o) {
		if (o == null) return false;
		return eq(sharedText, o.sharedText)
				&& eq(sharedSubj, o.sharedSubj)
				&& eq(sharedName, o.sharedName)
				&& eq(fileName, o.fileName);
	}
}