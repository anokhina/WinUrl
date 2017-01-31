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

import android.app.Activity;
import android.content.Intent;

public class IntentHandler {
	public static void handleIntent(Intent intent, Activity activity, ActivityHandler ahandler) {
	    // Get intent, action and MIME type
	    String action = intent.getAction();
	    String type = intent.getType();
	    if (action != null && type != null) {
	    	if (action.equals(Intent.ACTION_SEND)) {
	    		ahandler.handleSend(intent, activity);
	    	} else if (action.equals(Intent.ACTION_SEND_MULTIPLE)) {
	    		ahandler.handleMultipleSend(intent, activity);
	    	} else if (action.equals(Intent.ACTION_VIEW)) {
	    		ahandler.handleView(intent, activity);
	    	}
	    }
		
	}
}