/*
 * Copyright (C) 2012 The Provision Team.
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
 */

package com.provision.alarmemi.paper;

import static com.provision.alarmemi.CommonUtilities.SENDER_ID;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

// SplashActivity
// - Splash of the Provision Alarmemi
public class SplashActivity extends Activity {
	boolean paused = false;
	static int versionCode = 0;
	static String myUUID = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		try {
			PackageInfo i = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionCode = i.versionCode;
		} catch (NameNotFoundException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
		}

		myUUID = ServerUtilities.getUUID(this);

		CloudAccountFragment.regId = GCMRegistrar.getRegistrationId(this);
		if (CloudAccountFragment.regId.equals("")) {
			GCMRegistrar.register(this, SENDER_ID);
		}

		handler.sendEmptyMessageDelayed(0, 900);
	}

	/** The handler. */
	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			if (!paused) {
				SplashActivity.this.startActivity(new Intent(
						SplashActivity.this, FragmentChangeActivity.class));
				finish();

			} else {
				finish();
			}
		}
	};
}
