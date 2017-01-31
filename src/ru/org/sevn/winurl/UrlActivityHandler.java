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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

class UrlActivityHandler extends ActivityHandler {
		private final ExtraData extraData = new ExtraData("ya.ru", "яндекс url");
		
		public ExtraData getExtraData() {
			return extraData;
		}

		@Override
		public void handleSend(Intent intent, Activity activity) {
		    Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
		    if (uri != null) {
				handleUri(uri);
		    } else {
			    ExtraData ed = new ExtraData(intent.getStringExtra(Intent.EXTRA_TEXT), intent.getStringExtra(Intent.EXTRA_SUBJECT));
				updateExtraData(ed);
		    }
		}

		@Override
		public void handleMultipleSend(Intent intent, Activity activity) {
		}

		private void handleUri(Uri uri) {
			File file2load = new File(uri.getPath());
			try {
				updateExtraData(loadExtraData(file2load));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				
			}
		}
		
		@Override
		public void handleView(Intent intent, Activity activity) {
			Uri uri=intent.getData();
			handleUri(uri);
		}
		
		public static ExtraData loadExtraData(File file2load) throws IOException {
			String fileContent = IOUtil.loadFileContent(file2load, "UTF-8");
			String fileName = file2load.getName();
			return ExtraData.parseUrlContent(
							fileContent, 
							fileName ).setFileName(file2load.getAbsolutePath());
		}
		
		private void updateExtraData(ExtraData ed) {
			extraData.copyFrom(ed);
		}
	}