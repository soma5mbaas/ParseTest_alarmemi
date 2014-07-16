package com.provision.alarmemi.paper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

public class Invited extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences prefs = getSharedPreferences("forest",
				Context.MODE_PRIVATE);
		Uri uri = getIntent().getData();

		String name = uri.getQueryParameter("name"), pw = uri
				.getQueryParameter("pw"), inviter = uri
				.getQueryParameter("inviter");
		String message_key = Notify.FOREST_INVITE + name + "|" + pw + "|"
				+ inviter + "|ask|no";
		GCMIntentService.notify(this,
				Notify.getString(this, message_key), new Intent(this, SplashActivity.class));
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("notify",
				message_key + "||" + prefs.getString("notify", ""));
		editor.commit();

		Intent i = new Intent(this, SplashActivity.class);
		i.putExtra("goto", "cloud_notify");
		startActivity(i);
		finish();
	}
}
