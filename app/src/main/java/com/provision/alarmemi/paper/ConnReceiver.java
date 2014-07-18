package com.provision.alarmemi.paper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.provision.alarmemi.paper.utils.Internet;
import com.provision.alarmemi.paper.utils.ServerUtilities;

public class ConnReceiver extends BroadcastReceiver {

	static void RemoveNotRemoved(final Context context) {
		new Thread() {
			@Override
			public void run() {
				if (Internet.Check(context)) {
					SharedPreferences prefs = context.getSharedPreferences(
							"forest", Context.MODE_PRIVATE);
					if (prefs.getString("need_remove", "").equals(""))
						return;
					String[] need_removes = prefs
							.getString("need_remove", "||").substring(2)
							.split("\\|\\|");

					String myUUID = ServerUtilities.getUUID(context);

					for (int i = 0; i < need_removes.length; i++) {
						String[] need_remove = need_removes[i].split("\\|");
						String url = null;
						try {
							url = "http://alarmemi.appspot.com/alarmemi/alarm/remove?owner_name="
									+ URLEncoder
											.encode(need_remove[0], "UTF-8")
									+ "&owner_password="
									+ prefs.getString(need_remove[0]
											+ "_password", "")
									+ "&key="
									+ need_remove[1] + "&my_uuid=" + myUUID;
							ServerUtilities.connect(url, context);
						} catch (UnsupportedEncodingException e) {
						}
					}

					SharedPreferences.Editor editor = prefs.edit();
					editor.remove("need_remove");
					editor.commit();
				}
			}
		}.start();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			RemoveNotRemoved(context);
		}
	}
}