package com.provision.alarmemi.paper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.provision.alarmemi.JoinForestAlertDialog.JoinForestAlertDialogListener;

public class LogInForest extends Activity {
	EditText forest_name, forest_pw;
	String new_name, new_pw, new_pw_again;
	SharedPreferences prefs;
	static Context context;
	static CustomProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		progressDialog = new CustomProgressDialog(context);

		prefs = getSharedPreferences("forest", Context.MODE_PRIVATE);
		setContentView(R.layout.login_forest);

		OnClickListener back_click = new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		};
		ImageView b = (ImageView) findViewById(R.id.back);
		b.setOnClickListener(back_click);

		b = (ImageView) findViewById(R.id.logo);
		b.setOnClickListener(back_click);

		forest_name = (EditText) findViewById(R.id.forest_name);
		forest_pw = (EditText) findViewById(R.id.forest_pw);
		LinearLayout login = (LinearLayout) findViewById(R.id.login);
		LinearLayout join = (LinearLayout) findViewById(R.id.join);
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String id_value = forest_name.getText().toString();
				String pw_value = forest_pw.getText().toString();
				if (id_value.equals("")) {
					Toast.makeText(context, R.string.input_forest_name,
							Toast.LENGTH_SHORT).show();
				} else if (pw_value.equals("")) {
					Toast.makeText(context, R.string.input_forest_pw,
							Toast.LENGTH_SHORT).show();
				} else {
					if (!prefs.getString(id_value + "_password", "").equals("")) {
						Toast.makeText(
								context,
								String.format(context
										.getString(R.string.already_entered),
										id_value), Toast.LENGTH_SHORT).show();
						return;
					}
					progressDialog.show();
					Thread thread = new LoginThread();
					thread.start();
				}
			}

		});
		join.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				JoinForestAlertDialog.listener = new JoinForestAlertDialogListener() {
					@Override
					public void onOk(String name, String pw, String pw_chk) {
						new_name = name;
						new_pw = pw;
						new_pw_again = pw_chk;

						if (!new_pw.equals(new_pw_again)) {
							Toast.makeText(context, R.string.mismatch,
									Toast.LENGTH_SHORT).show();
							return;
						} else if (new_name.length() < 4 || new_pw.length() < 4) {
							Toast.makeText(context, R.string.tooshort,
									Toast.LENGTH_SHORT).show();
							return;
						} else if (!AlarmUtils.Check(new_name)) {
							Toast.makeText(context, R.string.bann_char,
									Toast.LENGTH_SHORT).show();
							return;
						}

						progressDialog.show();

						Thread thread = new MakeForestThread();
						thread.start();
					}

					@Override
					public void onCancel() {
					}
				};
				context.startActivity(new Intent(context,
						JoinForestAlertDialog.class));
			}

		});
	}

	class MakeForestThread extends Thread {
		@Override
		public void run() {

			String id, password_hash = getMD5Hash(new_pw), message_result;
			try {
				id = URLEncoder.encode(new_name, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// Connect to the server - to answer the "it's already available?"
			// :p
			String result = ServerUtilities.connect(
					"http://alarmemi.appspot.com/alarmemi/forest/available?name="
							+ id, context);
			android.util.Log.d("Alarmemi", result);

			if (result == null) {
				message_result = "SERVER_ERROR";

			} else if (result.equals("CONNECTION_FAILED")) {
				message_result = "CONNECTION_FAILED";
			} else if (result.startsWith("NOT_AVAILABLE")) {
				// Yes, we can make new forest.
				// Connect to the server again!
				result = ServerUtilities.connect(
						"http://alarmemi.appspot.com/alarmemi/forest/makenew?name="
								+ id + "&password=" + password_hash, context);

				// Result?
				if (result == null)
					message_result = "SERVER_ERROR";
				else if (result.equals("FAILED"))
					message_result = "FAILED";
				else
					message_result = "SUCCEED";
			} else {
				// This forest is already used!
				message_result = "FAILED_ALREADY";
			}

			// Send message to UI Handler
			Message m = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("msg", message_result);
			bundle.putString("id", new_name);
			bundle.putString("pw", password_hash);
			m.setData(bundle);
			makenew_handler.sendMessage(m);
		}

		public String getMD5Hash(String s) {
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
	}

	class LoginThread extends Thread {
		@Override
		public void run() {
			String id, password_hash = ServerUtilities.getMD5Hash(forest_pw
					.getText().toString());
			try {
				id = URLEncoder.encode(forest_name.getText().toString(),
						"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return;
			}

			// Connect to the server
			String result = ServerUtilities.connect(
					"http://alarmemi.appspot.com/alarmemi/forest/getinfo?name="
							+ id + "&password=" + password_hash, context);

			// Send message to UI Thread
			Message m = new Message();
			m.obj = result;
			handler.sendMessage(m);
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String result = (String) msg.obj;

			progressDialog.dismiss();
			int failed_message = -1;

			if (result == null) {
				// Show error dialog
				failed_message = R.string.server_error;
			} else if (result.equals("CONNECTION_FAILED")) {
				failed_message = R.string.connection_chk;
			} else if (!result.equals("NOT_AVAILABLE")) {

				try {
					// Convert result to json
					JSONObject json = new JSONObject(result);
					String name = json.getString("name");

					// Write to shared preferences
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("name", prefs.getString("name", "") + "|"
							+ name);
					editor.putString(name + "_password",
							json.getString("password"));
					editor.putString(name + "_registeredDevice",
							json.getString("registeredDevice"));
					editor.commit();

					SlideMenu.MarkAlreadyInvite();
					SlideMenu.onNotifyChanged();

					((Activity) context).finish();
					CloudAccountFragment.onForestChanged();
					CloudAccountFragment.needToRegister = name;
					CloudAccountFragment.RegisterDevice(CloudAccountFragment.needToRegister);
				} catch (JSONException e) {
					// Show error dialog
					failed_message = R.string.idpw_error;
				}
			} else if (result.equals("NOT_AVAILABLE")) {
				// Show error dialog
				failed_message = R.string.idpw_error;
			}
			if (failed_message != -1) {
				new AlertDialogBuilder(context, R.string.login_error,
						failed_message, false, null);
			}
		}
	};

	Handler makenew_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String result = msg.getData().getString("msg");
			String id = msg.getData().getString("id");
			String pw = msg.getData().getString("pw");

			progressDialog.dismiss();

			int failed_message = -1;
			if (result.equals("SERVER_ERROR")) {
				// Show error dialog
				failed_message = R.string.server_error;
			} else if (result.equals("SUCCEED")) {

				// Write to shared preferences
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("name", prefs.getString("name", "") + "|" + id);
				editor.putString(id + "_password", pw);
				editor.putString(id + "_registeredDevice", "");
				editor.commit();

				SlideMenu.MarkAlreadyInvite();
				SlideMenu.onNotifyChanged();

				CloudAccountFragment.needToRegister = id;
				((Activity) context).finish();
				CloudAccountFragment.onForestChanged();
				CloudAccountFragment.RegisterDevice(CloudAccountFragment.needToRegister);

			} else if (result.equals("FAILED_ALREADY")) {
				// Show error dialog
				failed_message = R.string.already;
			} else if (result.equals("CONNECTION_FAILED")) {
				// Show error dialog
				failed_message = R.string.connection_chk;
			} else {
				// Show error dialog
				failed_message = R.string.server_error;
			}
			if (failed_message != -1) {
				new AlertDialogBuilder(context, R.string.make_error,
						failed_message, false, null);
			}
		}
	};

}
