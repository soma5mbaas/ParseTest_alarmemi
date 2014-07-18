package com.provision.alarmemi.paper.utils;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.provision.alarmemi.paper.SplashActivity;

public class ServerUtilities {
	public static String connect(String url, Context context) {
		if (!Internet.Check(context)) {
			return "CONNECTION_FAILED";
		}

		url += "&version=" + SplashActivity.versionCode;
		InputStream content = null;
		try {
			String split_url[] = new String[2];
			split_url[0] = url.substring(0, url.indexOf("?"));
			split_url[1] = url.substring(url.indexOf("?") + 1);
			String split_datas[] = split_url[1].split("&");
			ArrayList<NameValuePair> post_datas = new ArrayList<NameValuePair>();
			android.util.Log.e("url", context.getClass().getName());
			android.util.Log.e("url", split_url[0]);
			for (int i = 0; i < split_datas.length; i++) {
				String split_data[] = split_datas[i].split("=");
				if (split_data.length == 2) {
					post_datas.add(new BasicNameValuePair(split_data[0],
							split_data[1]));
				} else {
					post_datas.add(new BasicNameValuePair(split_data[0], ""));
				}
				android.util.Log.e("url", post_datas.get(i).toString());
			}

			HttpPost httpPost = new HttpPost(split_url[0]);
			UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(
					post_datas, "UTF-8");
			httpPost.setEntity(entityRequest);
			HttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter(
					"http.protocol.expect-continue", false);
			httpclient.getParams()
					.setParameter("http.connection.timeout", 5000);
			httpclient.getParams().setParameter("http.socket.timeout", 5000);
			// Execute HTTP Get Request
			HttpResponse response = httpclient.execute(httpPost);
			content = response.getEntity().getContent();

			// Read
			StringBuffer sb = new StringBuffer();
			byte[] b = new byte[4096];
			for (int n; (n = content.read(b)) != -1;) {
				sb.append(new String(b, 0, n));
			}

			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isPushNotify(String str) {
		String pushes[] = { "/alarmemi/forest/register",
				"/alarmemi/forest/unregister", "/alarmemi/alarm/add",
				"/alarmemi/alarm/edit", "/alarmemi/alarm/enable",
				"/alarmemi/alarm/remove" };
		for (int i = 0; i < pushes.length; i++) {
			if (str.endsWith(pushes[i]))
				return true;
		}
		return false;
	}

	public static String getMD5Hash(String s) {
		MessageDigest m = null;
		String hash = null;

		try {
			m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes(), 0, s.length());
			hash = new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return hash;
	}

	static int times = 0;

	public static String getUUID(Context c) {
		if (times > 0) {
			times = 0;
			return "null";
		}
		times++;

		final TelephonyManager tm = (TelephonyManager) c
				.getSystemService(Context.TELEPHONY_SERVICE);

		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						c.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);

		UUID deviceUuid = new UUID(androidId.hashCode(),
				((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		if (deviceUuid != null)
			times = 0;
		return deviceUuid == null ? getUUID(c) : deviceUuid.toString();
	}
}