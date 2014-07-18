package com.provision.alarmemi.paper.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.provision.alarmemi.paper.Alarm;
import com.provision.alarmemi.paper.utils.AlarmUtils;
import com.provision.alarmemi.paper.Alarms;
import com.provision.alarmemi.paper.ui.AlertDialogBuilder;
import com.provision.alarmemi.paper.ui.CustomAlertDialog.CustomAlertDialogListener;
import com.provision.alarmemi.paper.utils.KakaoLink;
import com.provision.alarmemi.paper.LogInForest;
import com.provision.alarmemi.paper.R;
import com.provision.alarmemi.paper.ui.RegisterAlertDialog;
import com.provision.alarmemi.paper.ui.RegisterAlertDialog.RegisterAlertDialogListener;
import com.provision.alarmemi.paper.utils.ServerUtilities;
import com.provision.alarmemi.paper.ui.ShowcaseView;
import com.provision.alarmemi.paper.SplashActivity;
import com.provision.alarmemi.paper.Welcome;

public class CloudAccountFragment extends BaseFragment {
	static SharedPreferences prefs;
	static Handler registerHandler;
	public static String regId = "";
	static String myUUID;
    public static Handler refresh_list_handler;
	static ForestAdapter adapter;
	static ListView mForestsList;
	public static String needToRegister = null;
	static Handler registerDialog = new RegisterDialog();

	ViewGroup root;
	static LinearLayout addForest, addForestDivider;

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle s) {
		mActivity.setOnLifeCycleChangeListener(this);

		prefs = mActivity.getSharedPreferences("forest", Context.MODE_PRIVATE);
		myUUID = SplashActivity.myUUID;
		registerHandler = new RegisterHandler();
		refresh_list_handler = new RefreshListHandler();

		root = (ViewGroup) inflater.inflate(R.layout.cloud_account, null);
		ViewTreeObserver vto = root.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(this);

		final ImageView moreAlarm = (ImageView) root.findViewById(R.id.more_alarm);
		FragmentChangeActivity.moreAlarm = moreAlarm;
		moreAlarm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (menu.isMenuShowing()) menu.showContent();
				else menu.showMenu(true);
			}
		});
		// Make the entire view selected when focused.
		moreAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				v.setSelected(hasFocus);
			}
		});

		OnClickListener back_click = new OnClickListener() {
			public void onClick(View v) {
				mActivity.switchContent(new MainFragment());
			}
		};
		ImageView b = (ImageView) root.findViewById(R.id.back);
		b.setOnClickListener(back_click);

		b = (ImageView) root.findViewById(R.id.logo);
		b.setOnClickListener(back_click);

		Log.d("url", prefs.getString("name", ""));

		mForestsList = (ListView) root.findViewById(R.id.forests_list);
		if (!prefs.getString("name", "").equals("")) {
			List<String> forests_ = Arrays.asList(prefs.getString("name", "")
					.substring(1).split("\\|"));
			Log.d("url", forests_.toString());
			ArrayList<String> forests = new ArrayList<String>(forests_);
			adapter = new ForestAdapter(mActivity, R.layout.forest_row, forests);
			mForestsList.setAdapter(adapter);
		} else {
			ArrayList<String> forests = new ArrayList<String>();
			adapter = new ForestAdapter(mActivity, R.layout.forest_row, forests);
			mForestsList.setAdapter(adapter);
		}
		addForest = (LinearLayout) root.findViewById(R.id.add_forest);
		addForest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Welcome.hideWelcomeContent(true);
				startActivity(new Intent(mActivity, LogInForest.class));
			}
		});
		addForestDivider = (LinearLayout) root
				.findViewById(R.id.add_forest_divider);

		ListViewResizing();

		FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);
		return root;
	}

	public static void ListViewResizing() {
		int row_height = MainFragment.row_height2;
		int row_height_ = MainFragment.row_height2_;
		LinearLayout.LayoutParams params;
		params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, (row_height + 1)
						* adapter.getCount() + row_height_);
		mForestsList.setLayoutParams(params);

		if (adapter.getCount() == 0) {
			addForest.setBackgroundResource(R.drawable.pref_rounded);
			addForestDivider.setVisibility(View.GONE);
		} else {
			addForest.setBackgroundResource(R.drawable.pref_bottom_rounded);
			addForestDivider.setVisibility(View.VISIBLE);
		}
	}

	public static void RegisterDevice(final String forest_name) {
		if (needToRegister == null) return;
		needToRegister = null;

		GCMRegistrar.checkDevice(mActivity);
		GCMRegistrar.checkManifest(mActivity);

		FragmentChangeActivity.progressDialog.show();
		new Thread() {
			@Override
			public void run() {
				String message_result;
				String result;
				try {
					String name = forest_name;
					result = ServerUtilities.connect(
                            "http://alarmemi.appspot.com/alarmemi/forest/getdevice?name="
                                    + URLEncoder.encode(name, "UTF-8")
                                    + "&password="
                                    + prefs.getString(name + "_password", "")
                                    + "&uid=" + myUUID, mActivity
                    );
				} catch (Exception e) {
					return;
				}
				Log.i("url", result);
				if (result == null)
					message_result = "FAILED";
				else if (result.equals("CONNECTION_FAILED")) {
					message_result = "CONNECTION_FAILED";
				} else if (result.equals("FAILED")) {
					message_result = "FAILED";
				} else if (result.equals("NOT_AVAILABLE")) {
					Message m = new Message();
					m.obj = forest_name;
					registerDialog.sendMessage(m);
					message_result = "SUCCEED";
				} else {
					try {
						JSONObject json = new JSONObject(result);
						if (json.toString().equals("{}")) {
							Message m = new Message();
							m.obj = forest_name;
							registerDialog.sendMessage(m);
							message_result = "SUCCEED";
						} else {
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString(forest_name + "_registeredDevice",
									json.getString("registeredDevice"));
							editor.putString(forest_name + "_deviceName",
									json.getString("name"));
							editor.commit();
							message_result = getAllAboutForest(forest_name, true, null);
							refresh_list_handler.sendEmptyMessage(0);

							message_result = "SUCCEED";
						}
					} catch (JSONException e) {
						message_result = "FAILED";
					}
				}
				if (!message_result.equals("SUCCEED")) {
					RemoveAccount(forest_name);
					refresh_list_handler.sendEmptyMessage(0);
				}
				Message m = new Message();
				m.obj = message_result;
				registerHandler.sendMessage(m);
			}
		}.start();
	}

	static void RemoveAccount(String name) {
		String names = prefs.getString("name", "");
		int index = names.indexOf("|" + name);
		String new_names = names.substring(0, index)
				+ names.substring(index + name.length() + 1);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("name", new_names);
		editor.remove(name + "_password");
		editor.remove(name + "_deviceName");
		editor.remove(name + "_registeredDevice");
		editor.commit();
	}

	static class RegisterDialog extends Handler {
		@Override
		public void handleMessage(Message msg) {
			final String forest_name = msg.obj.toString();
			RegisterAlertDialog.listener = new RegisterAlertDialogListener() {
				@Override
				public void onOk(final String device_name) {
					if (device_name.length() == 0) {
						RemoveAccount(forest_name);
						refresh_list_handler.sendEmptyMessage(0);
						Toast.makeText(mActivity, R.string.input_device,
								Toast.LENGTH_SHORT).show();
						return;
					} else if (!AlarmUtils.Check(device_name)) {
						RemoveAccount(forest_name);
						refresh_list_handler.sendEmptyMessage(0);
						Toast.makeText(mActivity, R.string.bann_char,
								Toast.LENGTH_SHORT).show();
						return;
					}
					FragmentChangeActivity.progressDialog.show();
					new Thread() {
						@Override
						public void run() {
							String message_result;
							String result;
							regId = GCMRegistrar.getRegistrationId(mActivity);
							if (regId.equals(""))
								message_result = "RID_FAILED";
							else {

								try {
									String name = forest_name;
									result = ServerUtilities.connect(
											"http://alarmemi.appspot.com/alarmemi/forest/register?name="
													+ URLEncoder.encode(name,
															"UTF-8")
													+ "&password="
													+ prefs.getString(name
															+ "_password", "")
													+ "&device_name="
													+ URLEncoder.encode(
															device_name,
															"UTF-8") + "&uid="
													+ myUUID + "&rid=" + regId,
											mActivity);
								} catch (Exception e) {
									return;
								}
								if (result == null)
									message_result = "FAILED";
								else if (result.equals("CONNECTION_FAILED")) {
									message_result = "CONNECTION_FAILED";
								} else if (result.equals("SUCCEED")) {
									Log.d("Alarmemi",
											"Register succeed");

									message_result = getAllAboutForest(
											forest_name, false, device_name);
								} else {
									message_result = "FAILED";
								}
							}
							Log
									.e("url", "url : " + message_result);
							if (!message_result.equals("SUCCEED")) {
								RemoveAccount(forest_name);
								refresh_list_handler.sendEmptyMessage(0);
							}
							// Send message to the UI Handler
							Message m = new Message();
							m.obj = message_result;
							registerHandler.sendMessage(m);
						}
					}.start();
				}
			};
			mActivity.startActivity(new Intent(mActivity, RegisterAlertDialog.class));
		}
	}

	public static String getAllAboutForest(String forest_name,
			boolean isRegistered, String device_name) {
		String message_result, result;
		try {
			result = ServerUtilities.connect(
					"http://alarmemi.appspot.com/alarmemi/forest/getinfo?name="
							+ URLEncoder.encode(forest_name, "UTF-8")
							+ "&password="
							+ prefs.getString(forest_name + "_password", ""),
					mActivity);
		} catch (UnsupportedEncodingException e1) {
			result = null;
		}

		if (result == null) {
			message_result = "FAILED";
		} else if (result.equals("CONNECTION_FAILED")) {
			message_result = "CONNECTION_FAILED";
		} else {

			try {
				message_result = "SUCCEED";
				Log.e("url", result);
				// Convert result to
				// json
				JSONObject json = new JSONObject(result);

				try {
					result = ServerUtilities.connect(
							"http://alarmemi.appspot.com/alarmemi/alarm/all?owner_name="
									+ URLEncoder.encode(forest_name, "UTF-8")
									+ "&owner_password="
									+ prefs.getString(
											forest_name + "_password", ""),
							mActivity);
					if (result == null) {
						message_result = "FAILED";
					} else if (result.equals("CONNECTION_FAILED")) {
						message_result = "CONNECTION_FAILED";
					} else if (result.equals("FAILED")) {
						message_result = "FAILED";
					} else {
						JSONObject json2 = new JSONObject(result);

						SharedPreferences.Editor editor = prefs.edit();
						editor.putInt(forest_name + "_no",
								json2.getInt("last_notify_no")
										- (isRegistered ? 0 : 1));
						editor.commit();
						Log.e("NotifyPref",
								prefs.getInt(forest_name + "_no", -1) + "");

						for (int i = 0; i < json2.length() - 1; i++) {
							JSONObject json3 = new JSONObject(
									json2.getString("alarm" + i));
							Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(
									0);
							String repeat_str = json3.getString("repeat");
							for (int j = 0; j < 7; j++) {
								mNewDaysOfWeek.set(j,
										repeat_str.charAt(j) == 't' ? true
												: false);
							}
							Alarm alarm = new Alarm();
							alarm.id = -1;
							alarm.hour = Integer.valueOf(json3
									.getString("time").substring(0, 2));
							alarm.minutes = Integer.valueOf(json3.getString(
									"time").substring(2));
							alarm.daysOfWeek = mNewDaysOfWeek;
							alarm.vibrate = json3.getBoolean("vibrate");
							alarm.label = json3.getString("memi_name");
							alarm.cloudEnabled = true;
							alarm.cloudName = json3.getString("owner_name");
							alarm.cloudDevices = json3
									.getString("target_device");
							alarm.cloudKey = json3.getString("key");
							alarm.cloudUID = json3
									.getString("target_device_uid");
							alarm.enabled = alarm.cloudUID.contains(myUUID);
							Alarms.addAlarm(mActivity, alarm);
						}
					}
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				if (!isRegistered) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString(forest_name + "_registeredDevice",
							json.getString("registeredDevice"));
					editor.putString(forest_name + "_deviceName", device_name);
					editor.commit();
				}

				refresh_list_handler.sendEmptyMessage(0);
			} catch (JSONException e) {
				e.printStackTrace();
				Log.d("Alarmemi", "Update failed");
				message_result = "FAILED";
			}
		}
		return message_result;
	}

	class RegisterHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String result = (String) msg.obj;

			int failed_message = -1;
			if (FragmentChangeActivity.progressDialog != null
					&& FragmentChangeActivity.progressDialog.isShowing())
				FragmentChangeActivity.progressDialog.dismiss();

			if (result.equals("CONNECTION_FAILED")) {
				failed_message = R.string.connection_chk;
			} else if (result.equals("RID_FAILED")) {
				failed_message = R.string.rid_chk;
			} else if (!result.startsWith("SUCCEED")) {
				failed_message = R.string.server_error;
			}

			if (failed_message != -1)
				new AlertDialogBuilder(mActivity, R.string.register_failed,
						failed_message, true, null);
		}
	}

	static class ForestAdapter extends ArrayAdapter<String> {

		private ArrayList<String> items;
        private Context mContext;

		public ForestAdapter(Context context, int textViewResourceId,
				ArrayList<String> items) {
			super(context, textViewResourceId, items);
            mContext = context;
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.forest_row, null);
			}
			if (position == 0)
				v.setBackgroundResource(R.drawable.pref_top_rounded);
			final String name = items.get(position);
			Log.d("url", ", " + name);
			((TextView) v.findViewById(R.id.name)).setText(name);
			((TextView) v.findViewById(R.id.my_device)).setText(" - "
					+ prefs.getString(name + "_deviceName", ""));

			String devices = "";
			try {
				String json_string = prefs.getString(
						name + "_registeredDevice", "");
				if (json_string.equals(""))
					json_string = "[]";
				JSONArray json = new JSONArray(json_string);
				for (int j = 0; j < json.length(); j++) {
					JSONObject jsonObj = json.getJSONObject(j);
					devices += jsonObj.getString("name") + ", ";
				}
			} catch (JSONException e) {
				Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
			}
			if (!devices.equals(""))
				devices = devices.substring(0, devices.length() - 2);
			((TextView) v.findViewById(R.id.devices)).setText(devices);

			((LinearLayout) v.findViewById(R.id.kakao))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {

							ArrayList<Map<String, String>> metaInfoArray = new ArrayList<Map<String, String>>();
							Map<String, String> metaInfoAndroid = new Hashtable<String, String>(
									1);
							metaInfoAndroid.put("os", "android");
							metaInfoAndroid.put("devicetype", "phone");
							metaInfoAndroid
									.put("installurl",
											"market://details?id=com.provision.alarmemi");
							metaInfoAndroid.put(
									"executeurl",
									"alarmemi://cloud?name="
											+ name
											+ "&pw="
											+ prefs.getString(name
													+ "_password", "")
											+ "&inviter="
											+ prefs.getString(name
													+ "_deviceName", ""));
							metaInfoArray.add(metaInfoAndroid);

							KakaoLink kakaoLink = KakaoLink.getLink(mContext
									.getApplicationContext());

							// check, intent is available.
							if (!kakaoLink.isAvailableIntent()) {
								Toast.makeText(mContext,
										"Not installed KakaoTalk.",
										Toast.LENGTH_SHORT).show();
								return;
							}

							/**
							 * @param activity
							 * @param url
							 * @param message
							 * @param appId
							 * @param appVer
							 * @param appName
							 * @param encoding
							 * @param metaInfoArray
							 */
							try {
								kakaoLink.openKakaoAppLink(
										(Activity) mContext,
										"http://www.provisionmod.com/",
										String.format(
												mActivity.getString(R.string.invite_str),
												prefs.getString(name
														+ "_deviceName", ""),
												name),
										mContext.getPackageName(),
										mContext.getPackageManager()
												.getPackageInfo(
														mContext.getPackageName(),
														0).versionName, mActivity
												.getString(R.string.app_label),
										"UTF-8", metaInfoArray);
							} catch (NameNotFoundException e) {
								Toast.makeText(mContext, e.toString(),
										Toast.LENGTH_SHORT).show();
							}

						}
					});

			((LinearLayout) v.findViewById(R.id.remove))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							new AlertDialogBuilder(
									mContext,
									R.string.forest_out_title,
									String.format(
											mActivity.getString(R.string.forest_out_msg),
											name), true,
									new CustomAlertDialogListener() {
										@Override
										public void onOk() {
											FragmentChangeActivity.progressDialog
													.show();
											new Thread() {
												@Override
												public void run() {
													regId = GCMRegistrar.getRegistrationId(mContext);
													String message_result;
													if (regId.equals(""))
														message_result = "RID_FAILED";
													else {
														// Unregister
														// the device!
														String result;
														Log.d(
																"Provision",
																"Starto");
														try {
															result = ServerUtilities
																	.connect(
																			"http://alarmemi.appspot.com/alarmemi/forest/unregister?name="
																					+ URLEncoder
																							.encode(name,
																									"UTF-8")
																					+ "&password="
																					+ prefs.getString(
																							name
																									+ "_password",
																							"")
																					+ "&device_name="
																					+ URLEncoder
																							.encode(prefs
																									.getString(
																											name
																													+ "_deviceName",
																											""),
																									"UTF-8")
																					+ "&uid="
																					+ myUUID
																					+ "&rid="
																					+ regId,
																			mContext);

															if (result == null) {
																message_result = "FAILED";
															} else if (result
																	.equals("CONNECTION_FAILED")) {
																message_result = "CONNECTION_FAILED";
															} else {

																Cursor mCursor = Alarms
																		.getAlarmsCursorWhere(
																				mContext.getContentResolver(),
																				Alarm.Columns.ALARM_QUERY_COLUMNS,
																				"cloud_name='"
																						+ name
																						+ "'");
																mCursor.moveToFirst();
																Log
																		.e("url",
																				mCursor.getCount()
																						+ "");

																while (!mCursor
																		.isAfterLast()) {
																	Alarm alarm = new Alarm(
																			mCursor);
																	Log
																			.i("url",
																					"list : "
																							+ alarm.cloudDevices);
																	if (alarm.enabled) {
																		MainFragment
																				.EnableAlarm(alarm);
																	}
																	Alarms.deleteAlarm(
																			mContext,
																			alarm.id);

																	mCursor.moveToNext();
																}
																mCursor.close();

																RemoveAccount(name);
																refresh_list_handler
																		.sendEmptyMessage(0);

																message_result = "SUCCEED";
															}

														} catch (Exception e) {
															message_result = "FAILED";
														}
													}
													Message m = new Message();
													m.obj = message_result;
													registerHandler
															.sendMessage(m);

												}
											}.start();
										}

										@Override
										public void onCancel() {
										}
									});
						}
					});
			return v;
		}
	}

	class RefreshListHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			onForestChanged();
		}
	}

    public static void onForestChanged() {
		ArrayList<String> forests;
		if (!prefs.getString("name", "").equals("")) {
			List<String> forests_ = Arrays.asList(prefs.getString("name", "")
					.substring(1).split("\\|"));
			Log.d("url", forests_.toString());
			forests = new ArrayList<String>(forests_);
		} else {
			forests = new ArrayList<String>();
		}

		adapter.clear();
		for (int i = 0; i < forests.size(); i++) {
			adapter.add(forests.get(i));
		}
		// adapter.addAll(forests);
		adapter.notifyDataSetChanged();
		ListViewResizing();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	}

	@Override
	public void onBackPressed() {
		if (ShowcaseView.opened) ShowcaseView.hideShowcase();
		else super.onBackPressed();
    }

	@Override
	public void onGlobalLayout() {
		onWindowFocusChanged(true);
		ViewTreeObserver obs = root.getViewTreeObserver();
		obs.removeGlobalOnLayoutListener(this);

		Welcome.welcomeContent
				.add(new Welcome.WelcomeContent(
						Welcome.ADD_FOREST_ID,
						"숲 들어가기",
						"이 버튼을 눌러 새로운 숲을 만들거나 기존의 숲에 들어갈 수 있습니다. 알람을 추가할 때에 이 숲에 들어온 다른 기기들과 함께 울리게 할 수 있습니다.",
						addForest, false));
		Welcome.showWelcomeContents(getActivity());
	}

}