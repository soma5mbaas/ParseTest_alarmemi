package com.provision.alarmemi.paper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Internet {
	static boolean Check(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
