/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.provision.alarmemi.paper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.provision.alarmemi.CustomAlertDialog.CustomAlertDialogListener;
import com.provision.alarmemi.colorpicker.AmbilWarnaPreference;
import com.provision.alarmemi.timepicker.AlarmTimePickerDialog;
import com.provision.alarmemi.timepicker.AlarmTimePickerDialog.AlarmTimePickerDialogHandler;
import com.slidingmenu.lib.SlidingMenu;

public class SetAlarmFragment extends SetAlarmPreferenceFragment implements
		AlarmTimePickerDialogHandler,
		SettingsPreferenceFragment.OnPreferenceAttachedListener,
		SharedPreferences.OnSharedPreferenceChangeListener,
		FragmentChangeActivity.OnLifeCycleChangeListener {
	static final String KEY_MEMI_COUNT = "memi_count";
	static final String KEY_SNOOZE_STRENGTH = "snooze_strength";

	private static Preference mLabel;
	private static CheckBoxPreference mEnabledPref;
	private Preference mTimePref;
	private CheckBoxPreference mVibratePref;
	private static Preference mRepeatPref;
	private static Preference mForestName;
	private static Preference mForest;
	private AmbilWarnaPreference mColorPref;

	private int mId;
	private int mHour;
	private int mMinutes;
	private boolean mTimePickerCancelled;
	private Alarm mOriginalAlarm;

	private static boolean isCloud;
	private boolean wasCloud;
	static JSONArray json = null;
	static JSONArray tempjson = null;
	static String selectedDevice = "";
	static String[] items = null;
	static CharSequence UIDitems[] = null;
	static boolean checkedItems[] = null;
	boolean tempVibrate;
	static String myUUID = null;
	static int nameCheckedIndex = -1;
	static String names[] = null;
	static String mLabelText;

	private static Alarm alarm;
	private static SharedPreferences prefs;
	static boolean isChanged = false, isRunning = false;

	static Handler toastHandler = new ToastHandler();
	static Handler finishHandler = new FinishHandler();
	public static Context context;
	static SetAlarmFragment _this;

	static int memi_count, snooze_strength, snooze_count;

	ViewGroup root;
	static SlidingMenu menu;
	static boolean runSelf = false;

	public SetAlarmFragment(Context c, SlidingMenu menu) {
		context = c;
		this.menu = menu;
		_this = this;
	}

	public static void finish() {
		((FragmentChangeActivity) _this.getActivity())
				.switchContent(new MainFragment(context, menu));
	}

	/**
	 * Set an alarm. Requires an Alarms.ALARM_ID to be passed in as an extra.
	 * FIXME: Pass an Alarm object like every other Activity.
	 */

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		isRunning = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (runSelf) {
			((FragmentChangeActivity) _this.getActivity())
					.switchContent(new SetAlarmFragment(context, menu));
			runSelf = false;
		}
	}

	public static void DontSaveDialog(final boolean mode,
			final Fragment fragment, final boolean isSetAlarm) {
		if (!isChanged) {
			if (mode) {
				if (isSetAlarm) {
					runSelf = true;
					((FragmentChangeActivity) _this.getActivity())
							.switchContent(new NothingFragment());
				} else
					((FragmentChangeActivity) _this.getActivity())
							.switchContent(fragment);
			} else
				finish();
			return;
		}
		new AlertDialogBuilder(context, R.string.app_name,
				R.string.dont_save_ask, true, new CustomAlertDialogListener() {
					@Override
					public void onOk() {
						if (mode) {
							if (isSetAlarm) {
								runSelf = true;
								((FragmentChangeActivity) _this.getActivity())
										.switchContent(new NothingFragment());
							} else
								((FragmentChangeActivity) _this.getActivity())
										.switchContent(fragment);
						} else
							finish();
					}

					@Override
					public void onCancel() {
					}
				});
	}

	PreferenceCategory category1, category2;

	private void hideCategory() {
		category1 = (PreferenceCategory) findPreference("category1");
		category2 = (PreferenceCategory) findPreference("category2");
		PreferenceScreen preferenceScreen = getPreferenceScreen();
		preferenceScreen.removePreference(category1);
		preferenceScreen.removePreference(category2);
	}

	private void showCategory() {
		PreferenceScreen preferenceScreen = getPreferenceScreen();
		preferenceScreen.addPreference(category1);
		preferenceScreen.addPreference(category2);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		((FragmentChangeActivity) getActivity())
				.setOnLifeCycleChangeListener(this);

		isChanged = isCloud = false;
		// Override the default content view.
		root = (ViewGroup) super.onCreateView(inflater, container, bundle);
		final ImageView moreAlarm = (ImageView) root
				.findViewById(R.id.more_alarm);
		FragmentChangeActivity.moreAlarm = moreAlarm;
		moreAlarm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (menu.isMenuShowing()) {
					menu.showContent();
				} else {
					menu.showMenu(true);
				}
			}
		});
		// Make the entire view selected when focused.
		moreAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				v.setSelected(hasFocus);
			}
		});

		addPreferencesFromResource(R.xml.alarm_prefs);
		myUUID = SplashActivity.myUUID;

		// Get each preference so we can retrieve the value later.
		mLabel = findPreference("label");
		mLabel.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showEditTextPreference(mLabel.getKey(), mLabel.getTitle(),
						mLabelText);
				return true;
			}
		});

		Preference.OnPreferenceChangeListener preferceChangedListener = new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference p, Object newValue) {
				isChanged = true;
				return true;
			}
		};

		mEnabledPref = (CheckBoxPreference) findPreference("enabled");
		mEnabledPref
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (!isCloud) {
							isChanged = true;
							if ((Boolean) newValue)
								showCategory();
							else
								hideCategory();
							return true;
						}
						if ((Boolean) newValue) {
							try {
								tempjson = new JSONArray("[]");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							selectedDevice = "";
							for (int i = 0; i < json.length(); i++) {
								if (UIDitems[i].toString().equals(myUUID))
									checkedItems[i] = true;
								if (checkedItems[i]) {
									Map<String, String> map = new HashMap<String, String>();
									map.put("name", URLDecoder.decode(items[i]
											.toString()));
									map.put("uid", UIDitems[i].toString());
									tempjson.put(map);
									selectedDevice += items[i] + ", ";
								}
							}
							if (!selectedDevice.equals(""))
								selectedDevice = selectedDevice.substring(0,
										selectedDevice.length() - 2);
						} else {
							try {
								tempjson = new JSONArray("[]");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							selectedDevice = "";
							for (int i = 0; i < json.length(); i++) {
								if (UIDitems[i].toString().equals(myUUID))
									checkedItems[i] = false;
								if (checkedItems[i]) {
									Map<String, String> map = new HashMap<String, String>();
									map.put("name", URLDecoder.decode(items[i]
											.toString()));
									map.put("uid", UIDitems[i].toString());
									tempjson.put(map);
									selectedDevice += items[i] + ", ";
								}
							}
							if (!selectedDevice.equals(""))
								selectedDevice = selectedDevice.substring(0,
										selectedDevice.length() - 2);
						}
						mForest.setSummary(selectedDevice);
						isChanged = true;
						return true;
					}
				});
		mTimePref = findPreference("time");
		mVibratePref = (CheckBoxPreference) findPreference("vibrate");
		mVibratePref.setOnPreferenceChangeListener(preferceChangedListener);
		mRepeatPref = findPreference("setRepeat");
		mRepeatPref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						String[] values = new String[] {
								DateUtils.getDayOfWeekString(Calendar.MONDAY,
										DateUtils.LENGTH_LONG),
								DateUtils.getDayOfWeekString(Calendar.TUESDAY,
										DateUtils.LENGTH_LONG),
								DateUtils.getDayOfWeekString(
										Calendar.WEDNESDAY,
										DateUtils.LENGTH_LONG),
								DateUtils.getDayOfWeekString(Calendar.THURSDAY,
										DateUtils.LENGTH_LONG),
								DateUtils.getDayOfWeekString(Calendar.FRIDAY,
										DateUtils.LENGTH_LONG),
								DateUtils.getDayOfWeekString(Calendar.SATURDAY,
										DateUtils.LENGTH_LONG),
								DateUtils.getDayOfWeekString(Calendar.SUNDAY,
										DateUtils.LENGTH_LONG) };
						Intent intent = new Intent(context,
								RepeatListPreference.class);
						intent.putExtra("key", mRepeatPref.getKey());
						intent.putExtra("title", mRepeatPref.getTitle());
						intent.putExtra("lists", values);
						intent.putExtra("multi", true);
						startActivity(intent);
						return true;
					}
				});
		mForestName = findPreference("forest_name");
		mForest = findPreference("forest");
		mColorPref = (AmbilWarnaPreference) findPreference("color");
		prefs = context.getSharedPreferences("forest", Context.MODE_PRIVATE);

		Intent i = ((FragmentChangeActivity) context).setAlarmGetIntent;
		mId = i.getIntExtra(Alarms.ALARM_ID, -1);

		alarm = null;
		if (mId == -1) {
			// No alarm id means create a new alarm.
			alarm = new Alarm();
			isChanged = true;
		} else {
			// * load alarm details from database
			alarm = Alarms.getAlarm(context.getContentResolver(), mId);
			// Bad alarm, bail to avoid a NPE.
			if (alarm == null) {
				finish();
				return root;
			}
			isCloud = wasCloud = alarm.cloudEnabled;
		}
		mOriginalAlarm = alarm;

		if (wasCloud) {
			try {
				Log.e("url", " : " + alarm.cloudName);
				json = new JSONArray(prefs.getString(alarm.cloudName
						+ "_registeredDevice", ""));
				String cloud_uid = alarm.cloudUID;
				if (cloud_uid.equals(""))
					cloud_uid = "[]";
				Log.e("url", cloud_uid);
				tempjson = new JSONArray(cloud_uid);
				items = new String[json.length()];
				UIDitems = new CharSequence[json.length()];
				checkedItems = new boolean[json.length()];
				for (int j = 0; j < json.length(); j++) {
					JSONObject jsonObj = json.getJSONObject(j);
					items[j] = jsonObj.getString("name");
					UIDitems[j] = jsonObj.getString("uid");
					checkedItems[j] = alarm.cloudUID.contains(jsonObj
							.getString("uid"));
				}
			} catch (Exception e) {
				Log.e("url", e.toString());
			}
			selectedDevice = alarm.cloudDevices;
			mForestName.setEnabled(false);
		} else {
			if (prefs.getString("name", "").length() > 0) {
				names = prefs.getString("name", "").substring(1).split("\\|");
				nameCheckedIndex = -1;
			} else
				mForestName.setEnabled(false);
			mForest.setEnabled(false);
		}
		memi_count = alarm.memiCount;
		snooze_strength = alarm.snoozeStrength;
		snooze_count = alarm.snoozeCount;

		updatePrefs(mOriginalAlarm);

		mTimePref
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						showTimePicker();
						return false;
					}

				});

		mForestName
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						showListPreference(mForestName.getKey(),
								mForestName.getTitle(), names,
								String.valueOf(nameCheckedIndex), false);
						return true;
					}
				});

		mForest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				showListPreference(mForest.getKey(), mForest.getTitle(), items,
						booleanArrayToString(checkedItems), true);
				return true;
			}
		});
		mColorPref.setOnPreferenceChangeListener(preferceChangedListener);

		// We have to do this to get the save/cancel buttons to highlight on
		// their own.
		((ListView) root.findViewById(android.R.id.list))
				.setItemsCanFocus(true);

		// Attach actions to each button.
		View.OnClickListener back_click = new View.OnClickListener() {
			public void onClick(View v) {
				DontSaveDialog(false, null, false);
			}
		};
		ImageView b = (ImageView) root.findViewById(R.id.back);
		b.setOnClickListener(back_click);

		b = (ImageView) root.findViewById(R.id.logo);
		b.setOnClickListener(back_click);

		b = (ImageView) root.findViewById(R.id.alarm_save);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveAlarm();
			}
		});
		b = (ImageView) root.findViewById(R.id.alarm_delete);
		if (mId == -1) {
			b.setEnabled(false);
		} else {
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					deleteAlarm();
				}
			});
		}

		// The last thing we do is pop the time picker if this is a new alarm.
		if (mId == -1) {
			// Assume the user hit cancel
			mTimePickerCancelled = true;
			showTimePicker();
		}

		if (!isCloud && !alarm.enabled)
			hideCategory();

		FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);
		return root;
	}

	public static String booleanArrayToString(boolean[] array) {
		int i;
		String string = "";
		for (i = 0; i < array.length; i++) {
			Log.d("hello", i + " : " + array[i]);
			string += array[i] ? "1" : "0";
			if (i < array.length - 1)
				string += "|";
			Log.d("hello", string);
		}
		return string;
	}

	public static void setRepeatSummary(String summary) {
		mRepeatPref.setSummary(summary);
	}

	private void showListPreference(String key, CharSequence title,
			String[] lists, String defaultValue, boolean isMultiChoice) {
		Intent intent = new Intent(context, CustomListPreference.class);
		intent.putExtra("key", key);
		intent.putExtra("title", title);
		intent.putExtra("lists", lists);
		intent.putExtra("multi", isMultiChoice);
		intent.putExtra("default", defaultValue);
		intent.putExtra("mode", 1);
		startActivity(intent);
	}

	private void showEditTextPreference(String key, CharSequence title,
			String defaultValue) {
		Intent intent = new Intent(context, CustomEditTextPreference.class);
		intent.putExtra("key", key);
		intent.putExtra("title", title);
		intent.putExtra("default", defaultValue);
		intent.putExtra("mode", 1);
		startActivity(intent);
	}

	public static void onActivityResult(String key, String which_str) {
		if (mForestName.getKey().equals(key)) {
			int which = Integer.parseInt(which_str);
			mForestName.setSummary(names[which]);
			String json_string = prefs.getString(names[which]
					+ "_registeredDevice", "");
			if (json_string.equals(""))
				json_string = "[]";
			try {
				json = new JSONArray(json_string);
				tempjson = new JSONArray("[]");
				items = new String[json.length()];
				UIDitems = new CharSequence[json.length()];
				checkedItems = new boolean[json.length()];
				for (int j = 0; j < json.length(); j++) {
					JSONObject jsonObj = json.getJSONObject(j);
					items[j] = jsonObj.getString("name");
					UIDitems[j] = jsonObj.getString("uid");
					checkedItems[j] = alarm.cloudUID.contains(jsonObj
							.getString("uid"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			nameCheckedIndex = which;

			selectedDevice = "";
			if (mEnabledPref.isChecked()) {
				for (int i = 0; i < json.length(); i++) {
					if (UIDitems[i].toString().equals(myUUID))
						checkedItems[i] = true;
					if (checkedItems[i]) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("name", URLDecoder.decode(items[i].toString()));
						map.put("uid", UIDitems[i].toString());
						tempjson.put(map);
						selectedDevice += items[i] + ", ";
					}
				}
			} else {
				for (int i = 0; i < json.length(); i++) {
					if (UIDitems[i].toString().equals(myUUID))
						checkedItems[i] = false;
					if (checkedItems[i]) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("name", URLDecoder.decode(items[i].toString()));
						map.put("uid", UIDitems[i].toString());
						tempjson.put(map);
						selectedDevice += items[i] + ", ";
					}
				}
			}
			if (!selectedDevice.equals(""))
				selectedDevice = selectedDevice.substring(0,
						selectedDevice.length() - 2);
			mForest.setSummary(selectedDevice);
			mForest.setEnabled(true);
			isChanged = isCloud = true;

		} else if (mForest.getKey().equals(key)) {
			String[] str_array = which_str.split("\\|");
			boolean[] values = new boolean[str_array.length];
			for (int i = 0; i < str_array.length; i++) {
				values[i] = str_array[i].equals("1");
			}
			checkedItems = values;

			boolean hasMyDevice = false;
			try {
				tempjson = new JSONArray("[]");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			selectedDevice = "";
			for (int i = 0; i < json.length(); i++) {
				if (values[i]) {
					if (UIDitems[i].toString().equals(myUUID))
						hasMyDevice = true;
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", URLDecoder.decode(items[i].toString()));
					map.put("uid", UIDitems[i].toString());
					tempjson.put(map);
					selectedDevice += items[i] + ", ";
				}
			}
			if (!selectedDevice.equals(""))
				selectedDevice = selectedDevice.substring(0,
						selectedDevice.length() - 2);
			mForest.setSummary(selectedDevice);
			mEnabledPref.setChecked(hasMyDevice);
			isChanged = true;
		} else if (mLabel.getKey().equals(key)) {
			mLabelText = which_str;
			mLabel.setSummary(which_str);
			isChanged = true;
		}
	}

	private void updatePrefs(Alarm alarm) {
		mId = alarm.id;
		mEnabledPref.setChecked(alarm.enabled);
		mLabelText = alarm.label;
		mLabel.setSummary(alarm.label);
		mHour = alarm.hour;
		mMinutes = alarm.minutes;
		RepeatListPreference.setDaysOfWeek(context, alarm.daysOfWeek);
		mVibratePref.setChecked(alarm.vibrate);
		mForestName.setSummary(alarm.cloudName);
		mForest.setSummary(alarm.cloudDevices);
		mColorPref.forceSetValue(alarm.color);
		updateMemiCount();
		updateSnoozeStrength();

		updateTime();
	}

	public static void updateMemiCount() {
		_this.findPreference(KEY_MEMI_COUNT).setSummary(
				(memi_count - 2) + " ~ " + (memi_count + 2)
						+ context.getString(R.string.times));
	}

	public static void updateSnoozeStrength() {
		_this.findPreference(KEY_SNOOZE_STRENGTH).setSummary(
				snooze_count + context.getString(R.string.times));
	}

	@Override
	public void onBackPressed() {
		// In the usual case of viewing an alarm, mTimePickerCancelled is
		// initialized to false. When creating a new alarm, this value is
		// assumed true until the user changes the time.
		if (!mTimePickerCancelled) {
			DontSaveDialog(false, null, false);
		}
	}

	private void showTimePicker() {
		AlarmTimePickerDialog timePickerDialog = new AlarmTimePickerDialog(
				context, R.style.SettingDialog);
		timePickerDialog.setAlarmTimePickerDialogHandler(this);
		timePickerDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				mTimePickerCancelled = false;
			}
		});
		timePickerDialog.show();
	}

	@Override
	public void onDialogTimeSet(Alarm alarm, int hourOfDay, int minute) {
		// onTimeSet is called when the user clicks "Set"
		mTimePickerCancelled = false;
		mHour = hourOfDay;
		mMinutes = minute;
		updateTime();
		// If the time has been changed, enable the alarm.
		if (!isCloud) {
			if (!mEnabledPref.isChecked()) {
				showCategory();
			}
			mEnabledPref.setChecked(true);
		}
		// Save the alarm and pop a toast.
		isChanged = true;
		// popAlarmSetToast(this, saveAlarm());
	}

	private void updateTime() {
		Calendar c = Alarms.calculateAlarm(mHour, mMinutes,
				RepeatListPreference.getDaysOfWeek());
		SimpleDateFormat sdf = new SimpleDateFormat("aa", Locale.US);
		mTimePref.setTitle(DateFormat.format("hh:mm", c));
		mTimePref.setSummary(sdf.format(c.getTime()));
	}

	private void saveAlarm() {
		if (!isChanged) {
			finish();
			return;
		}
		if (!AlarmUtils.Check(mLabelText)) {
			Toast.makeText(context, R.string.bann_char, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (isCloud) {
			FragmentChangeActivity.progressDialog.show();
			tempVibrate = mVibratePref.isChecked();
			Thread alarmAddThread = new AlarmAddThread();
			alarmAddThread.start();
		} else {
			Alarm alarm = new Alarm();
			alarm.id = mId;
			alarm.enabled = mEnabledPref.isChecked();
			alarm.hour = mHour;
			alarm.minutes = mMinutes;
			alarm.daysOfWeek = RepeatListPreference.getDaysOfWeek();
			alarm.vibrate = mVibratePref.isChecked();
			alarm.label = mLabelText;
			alarm.cloudEnabled = false;
			alarm.cloudName = "";
			alarm.cloudDevices = "";
			alarm.cloudKey = "";
			alarm.cloudUID = "[]";
			alarm.memiCount = memi_count;
			alarm.snoozeStrength = snooze_strength;
			alarm.snoozeCount = snooze_count;
			alarm.color = mColorPref.getValue();

			if (alarm.id == -1) {
				Alarms.addAlarm(context, alarm);
				// addAlarm populates the alarm with the new id. Update mId so
				// that
				// changes to other preferences update the new alarm.
				mId = alarm.id;
			} else {
				Alarms.setAlarm(context, alarm);
			}
			finish();
		}
	}

	class AlarmAddThread extends Thread {
		@Override
		public void run() {
			SharedPreferences prefs = context.getSharedPreferences("forest",
					Context.MODE_PRIVATE);
			String result = null;
			String cloud_name = wasCloud ? mOriginalAlarm.cloudName
					: names[nameCheckedIndex];
			Log.e("url", " : " + cloud_name);
			try {
				String repeat = "";
				boolean[] days = RepeatListPreference.getDaysOfWeek()
						.getBooleanArray();
				for (int i = 0; i < days.length; i++) {
					repeat += days[i] == true ? "t" : "f";
				}
				String url = "http://alarmemi.appspot.com/alarmemi/alarm/"
						+ (!wasCloud ? "add" : "edit")
						+ "?owner_name="
						+ URLEncoder.encode(cloud_name, "UTF-8")
						+ "&owner_password="
						+ prefs.getString(cloud_name + "_password", "")
						+ "&time="
						+ String.format("%02d%02d", mHour, mMinutes)
						+ "&repeat="
						+ repeat
						+ "&target_device="
						+ URLEncoder.encode(selectedDevice, "UTF-8")
						+ "&target_device_uid="
						+ URLEncoder.encode(tempjson.toString(), "UTF-8")
						+ "&vibrate="
						+ tempVibrate
						+ "&memi_name="
						+ URLEncoder.encode(mLabelText == null ? ""
								: mLabelText, "UTF-8")
						+ (wasCloud ? "&key=" + mOriginalAlarm.cloudKey : "")
						+ "&my_uuid=" + myUUID;
				result = ServerUtilities.connect(url, context);
			} catch (UnsupportedEncodingException e) {
				Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			}
			// Result?
			if (result == null) {
				showToast(getString(R.string.cloud_failed), true);
			} else if (result.equals("CONNECTION_FAILED")) {
				showToast(getString(R.string.connection_chk), true);
			} else if (result.equals("FAILED")) {
				showToast(getString(R.string.cloud_failed), true);
			} else {
				Log.e("url", " : " + cloud_name);

				Alarm alarm = new Alarm();
				alarm.id = mId;
				alarm.enabled = mEnabledPref.isChecked();
				alarm.hour = mHour;
				alarm.minutes = mMinutes;
				alarm.daysOfWeek = RepeatListPreference.getDaysOfWeek();
				alarm.vibrate = mVibratePref.isChecked();
				alarm.label = mLabelText;
				alarm.cloudEnabled = true;
				alarm.cloudName = cloud_name;
				alarm.cloudDevices = selectedDevice;
				alarm.cloudKey = wasCloud ? mOriginalAlarm.cloudKey : result;
				alarm.cloudUID = tempjson.toString();
				alarm.memiCount = memi_count;
				alarm.snoozeStrength = snooze_strength;
				alarm.snoozeCount = snooze_count;
				alarm.color = mColorPref.getValue();

				if (alarm.id == -1) {
					Alarms.addAlarm(context, alarm);
					// addAlarm populates the alarm with the new id. Update mId
					// so that
					// changes to other preferences update the new alarm.
					mId = alarm.id;
				} else {
					Alarms.setAlarm(context, alarm);
				}
				finishHandler.sendEmptyMessage(0);
			}
		}
	}

	static void showToast(String result, boolean dismiss) {
		if (dismiss) {
			FragmentChangeActivity.progressDialog.dismiss();
		}
		Message m = new Message();
		m.obj = result;
		toastHandler.sendMessage(m);
	}

	static class ToastHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String result = (String) msg.obj;
			Log.d("result", result);
			Toast.makeText(SetAlarmFragment.context, result, Toast.LENGTH_SHORT).show();
		}
	}

	static class FinishHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			FragmentChangeActivity.progressDialog.dismiss();
			finish();
		}
	}

	static Handler ProgressDismiss = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			FragmentChangeActivity.progressDialog.dismiss();
		}
	};

	public static void deleteCloudAlarm(final Context c, final Alarm tempAlarm) {
		FragmentChangeActivity.progressDialog.show();
		new Thread() {
			@Override
			public void run() {
				if (prefs == null) {
					prefs = c.getSharedPreferences("forest",
							Context.MODE_PRIVATE);
				}
				String url = null;
				try {
					url = "http://alarmemi.appspot.com/alarmemi/alarm/remove?owner_name="
							+ URLEncoder.encode(tempAlarm.cloudName, "UTF-8")
							+ "&owner_password="
							+ prefs.getString(
									tempAlarm.cloudName + "_password", "")
							+ "&key="
							+ tempAlarm.cloudKey
							+ "&my_uuid="
							+ myUUID;
				} catch (UnsupportedEncodingException e) {
					showToast(e.toString(), false);
				}
				String result = ServerUtilities.connect(url, c);
				if (result == null) {
					showToast(c.getString(R.string.cloud_failed), false);
				} else if (result.equals("CONNECTION_FAILED")) {
					showToast(c.getString(R.string.connection_chk), false);
				} else if (result.equals("FAILED")) {
					showToast(c.getString(R.string.cloud_failed), false);
				} else {
					Alarms.deleteAlarm(c, tempAlarm.id);
				}
				ProgressDismiss.sendEmptyMessage(0);
			}
		}.start();
	}

	private void deleteAlarm() {
		new AlertDialogBuilder(context, R.string.delete_alarm,
				R.string.delete_alarm_confirm, true,
				new CustomAlertDialogListener() {
					@Override
					public void onOk() {
						if (wasCloud)
							deleteCloudAlarm(context, mOriginalAlarm);
						else
							Alarms.deleteAlarm(context, mId);
						finish();
					}

					@Override
					public void onCancel() {
					}
				});
	}

	/**
	 * Display a toast that tells the user how long until the alarm goes off.
	 * This helps prevent "am/pm" mistakes.
	 */
	static void popAlarmSetToast(Context context, int hour, int minute,
			Alarm.DaysOfWeek daysOfWeek) {
		popAlarmSetToast(context,
				Alarms.calculateAlarm(hour, minute, daysOfWeek)
						.getTimeInMillis());
	}

	static void popAlarmSetToast(Context context, long timeInMillis) {
		String toastText = Alarms.formatToast(context, timeInMillis);
		Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
		ToastMaster.setToast(toast);
		toast.show();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		if (root == null)
			return;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub

	}

}