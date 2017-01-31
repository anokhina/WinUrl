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
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	public static final String SAVED_STATE_KEY = "ExtraData";
	public static String PREF_NAME = "WinUrlSettings";
	private UrlActivityHandler ahandler = new UrlActivityHandler();
    private Handler updateHandler;

	@Override
	protected void onResume() {
		if (!ahandler.getExtraData().isInitialized()) {
			loadExtraData();
		}
        updateHandler = new Handler();
		super.onResume();
	}

	private void loadExtraData() {
		ahandler.getExtraData().load(getPreferences(MODE_PRIVATE));
		if (!ahandler.getExtraData().isChanged() && Util.fileExists(ahandler.getExtraData().getFileName())) {
			try {
				ahandler.getExtraData().copyFrom(UrlActivityHandler.loadExtraData(new File(ahandler.getExtraData().getFileName())));
			} catch (IOException e) {
				Util.toast("Can't open:" + ahandler.getExtraData().getFileName(), this);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static View findViewById(View rv, int id) {
		return rv.findViewById(id);
	}
	private static View findViewById(Activity rv, int id) {
		return rv.findViewById(id);
	}
	
	private static String getTextFromEditText(Object rv, int id, String defval) {
		EditText et = null;
		if (rv instanceof Activity) {
			et = (EditText)findViewById((Activity) rv, id);
		} else {
			et = (EditText)findViewById((View) rv, id);
		}
		if (et != null) {
			return et.getText().toString();
		}
		return defval;
	}
	
	public static ExtraData getExtraData(ExtraData ret, Object rv) {
		ret.setSharedText(getTextFromEditText(rv, R.id.inputUrl, ret.getSharedText()));
		ret.setSharedSubj(getTextFromEditText(rv, R.id.inputComment, ret.getSharedSubj()));
		ret.setSharedName(getTextFromEditText(rv, R.id.inputFieName, ret.getSharedName()));
		return ret;
	}
	
	public ExtraData getExtraData(Object rv) {
		ExtraData ret = ahandler.getExtraData();
		
		return getExtraData(ret, rv);
	}

	@Override
	protected void onPause() {
		getExtraData(this).save(getPreferences(MODE_PRIVATE));
        if (updateHandler != null) {
        	updateHandler.removeCallbacksAndMessages(null);
        	updateHandler = null;
        }
		super.onPause();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadExtraData();
		
		IntentHandler.handleIntent(getIntent(), this, ahandler);
		
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment().setUrlActivityHandler(ahandler)).commit();
//		} else {
//			ExtraData ed = (ExtraData)savedInstanceState.getSerializable(SAVED_STATE_KEY);
//			if (ed != null) {
//				ahandler.getExtraData().copyFrom(ed);
//			}
		}
	}
	private static String getUrl(String url) {
		if (url.toLowerCase().startsWith("http")) {
			
		} else {
			url = "http://" + url;
		}
//		if(url.matches("^\\shttp[s]*")) {
//
//		} else {
//			url = "http://" + url;
//		}
		return url;
	}
	private static void share(String shareType, ExtraData extraData, Activity activity) {
		boolean asFile = false;
		String url = extraData.getSharedText(); //"http://www.twitter.com/intent/tweet?url=YOURURL&text=YOURTEXT";
		//url = getUrl(url);//???
		if (url == null) return;
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		
		if ("text/plain".equals(shareType)) {
			sendIntent.setType(shareType);
			if (extraData.getFileName() != null && !extraData.isChanged()) {
				File sendFile = new File(extraData.getFileName());
				if (sendFile.exists()) {
					sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sendFile));
					asFile = true;
				}
			}
			if (!asFile) {
				sendIntent.putExtra(Intent.EXTRA_TEXT, extraData.getSharedText());
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, extraData.getSharedSubj());
			}
			activity.startActivity(Intent.createChooser(sendIntent, activity.getResources().getText(R.string.send_to)));
		} else {
			sendIntent.setType("text/plain");
			ExtraData extraDataTmp = new ExtraData();
			extraDataTmp.copyFrom(extraData);
			//TODO remove files
			File sdCardDir = Environment.getExternalStorageDirectory();
			if (sdCardDir != null) {
				File tmpFileDir = new File(sdCardDir, "WinUrl");
				if (!tmpFileDir.exists()) {
					tmpFileDir.mkdirs();
				}
				File tmpFile = new File(tmpFileDir, "untitled");
				tmpFile = extraDataTmp.saveInFile(Uri.fromFile(tmpFile), activity);
				if (tmpFile != null && tmpFile.exists()) {
					sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpFile));
					Intent intent = Util.makeCustomChooserIntent(activity.getApplicationContext(), activity.getResources().getText(R.string.send_to), sendIntent, null, null,
							activity.getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString("winurl_first_intent", "winurl"));
					//Intent intent = Intent.createChooser(sendIntent, activity.getResources().getText(R.string.send_to));
					activity.startActivityForResult(intent, CHOOSE_INTENT_TO_SEND);
				}
			}
		}
		
		//startActivityForResult
	}
	private static void open(ExtraData extraData, Activity activity) {
		String url = extraData.getSharedText(); //"http://www.twitter.com/intent/tweet?url=YOURURL&text=YOURTEXT";
		if (url == null) return;
		url = getUrl(url);
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		Uri parsedUri = Uri.parse(url); 
		i.setData(parsedUri);
		try {
			activity.startActivity(i);
		} catch (Exception e) {
			Util.toast("Can't open:" + parsedUri.getPath(), activity);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
			return true;
		}
			
		return super.onOptionsItemSelected(item);
	}
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//toast("onActivityResult>" + requestCode);
        if (requestCode == CHOOSE_FILE_TO_SAVE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    getExtraData(this).saveInFile(uri, this);
                }

            }
        } else if (requestCode == CHOOSE_INTENT_TO_SEND) {
			Util.toast("CHOOSE_INTENT_TO_SEND" + data.getComponent().getPackageName() +":" + data.getComponent().getShortClassName(), this);
			System.err.println("CHOOSE_INTENT_TO_SEND" + data.getComponent().getPackageName() +":" + data.getComponent().getShortClassName());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
	public static final int CHOOSE_FILE_TO_SAVE = 1;
	public static final int CHOOSE_INTENT_TO_SEND = 1;
	public static class PlaceholderFragment extends Fragment {

		private UrlActivityHandler ahandler;
		private View rootView;
		
		public PlaceholderFragment() {
			ahandler = new UrlActivityHandler();
		}

		public PlaceholderFragment setUrlActivityHandler(UrlActivityHandler ahandler) {
			this.ahandler = ahandler;
			return this;
		}
		
		private void updateField(TextView tv, final PropertyChangeEvent e, String propertyName, String v) {
			if (tv != null) {
				if (e == null || propertyName == null || propertyName.equals(e.getPropertyName())) {
					if (!tv.getText().toString().equals(v)) {
						
						tv.setText(v);
					}
				}
			}
		}
		private void updateViews(final PropertyChangeEvent e) {
			if (e == null) {
				
			}
			
			final ExtraData ed = ahandler.getExtraData();
			
			MainActivity activity = (MainActivity)getActivity();
			Runnable updateFields = new Runnable() {

				@Override
				public void run() {
					updateField(textView, e, "sharedText", ed.urlContent());
					updateField(textView, e, "sharedSubj", ed.urlContent());
					
					updateField(urlInp, e, "sharedText", ed.getSharedText());
					updateField(commentInp, e, "sharedSubj", ed.getSharedSubj());
					updateField(fileNameInp, e, "sharedName", ed.getSharedName());
					updateField(textViewFileName, e, "fileName", ed.getFileName());
					
					//buttonShare.setEnabled(!ed.isChanged());
					if (!ed.isChanged() && Util.fileExists(ed.getFileName())) {
						buttonShare.setText("Share file");
					} else {
						buttonShare.setText("Share content");
					}
				}

			};
			if (activity.updateHandler != null) {
				activity.updateHandler.post(updateFields);
			} else {
				updateFields.run();
			}
		}
		
		TextView textViewFileName;
		TextView textView;
		EditText urlInp;
		EditText commentInp;
		EditText fileNameInp;
        Button buttonShare;
        Button buttonShareUrl;
        Button buttonSave;
        Button buttonOpen;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_main_lin, container,
					false);

			textView = (TextView) rootView.findViewById(R.id.section_label);
			textViewFileName = (TextView) rootView.findViewById(R.id.textViewFileName);
			
			urlInp = (EditText)rootView.findViewById(R.id.inputUrl);
			commentInp = (EditText)rootView.findViewById(R.id.inputComment);
			fileNameInp = (EditText)rootView.findViewById(R.id.inputFieName);

	        buttonShare = (Button) rootView.findViewById(R.id.buttonShare);
	        buttonShareUrl = (Button) rootView.findViewById(R.id.buttonShareUrl);
	        buttonSave = (Button) rootView.findViewById(R.id.buttonSave);
	        buttonOpen = (Button) rootView.findViewById(R.id.buttonOpen);
			
			updateViews(null);
			
			PropertyChangeListener dataListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					updateViews(event);
				}
			};
			ahandler.getExtraData().setPropertyChangeListener(dataListener);
			urlInp.addTextChangedListener(new EditTextTextWatcher() {
				
				@Override
				public void setBoundValue(String s) {
					if (ahandler != null) {
						ahandler.getExtraData().setSharedText(s);
					}
				}
				
				@Override
				public String getBoundValue() {
					if (ahandler != null) {
						return ahandler.getExtraData().getSharedText();
					}
					return null;
				}
			});
			commentInp.addTextChangedListener(new EditTextTextWatcher() {
				
				@Override
				public void setBoundValue(String s) {
					if (ahandler != null) {
						ahandler.getExtraData().setSharedSubj(s);
					}
				}
				
				@Override
				public String getBoundValue() {
					if (ahandler != null) {
						return ahandler.getExtraData().getSharedSubj();
					}
					return null;
				}
			});
			fileNameInp.addTextChangedListener(new EditTextTextWatcher() {
				
				@Override
				public void setBoundValue(String s) {
					if (ahandler != null) {
						ahandler.getExtraData().setSharedName(s);
					}
				}
				
				@Override
				public String getBoundValue() {
					if (ahandler != null) {
						return ahandler.getExtraData().getSharedName();
					}
					return null;
				}
			});
			
			{
		        buttonShare.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
						getExtraData(ahandler.getExtraData(), rootView);
		            	share("text/plain", ahandler.getExtraData(), getActivity());
		            }
		        });
			}

			{
		        buttonShareUrl.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
						getExtraData(ahandler.getExtraData(), rootView);
		            	share("text/url", ahandler.getExtraData(), getActivity());
		            }
		        });
			}
			
			{
		        buttonSave.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
						getExtraData(ahandler.getExtraData(), rootView);
		            	Util.chooseFile2selectDir2save(getActivity(), CHOOSE_FILE_TO_SAVE);
		            }
		        });
			}
			
			{
		        buttonOpen.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
						getExtraData(ahandler.getExtraData(), rootView);
		            	open(ahandler.getExtraData(), getActivity());
		            }
		        });
			}			
			return rootView;
		}
	}
	
	static abstract class EditTextTextWatcher implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			String bval = getBoundValue();
			String newVal = s.toString();
			if (bval == null) {
				if (newVal.trim().length() > 0) {
					setBoundValue(newVal);
				}
			} else {
				if (!bval.equals(newVal)) {
					setBoundValue(newVal);
				}
			}
		}
		
		public abstract String getBoundValue();
		public abstract void setBoundValue(String s);
	}
	
}
