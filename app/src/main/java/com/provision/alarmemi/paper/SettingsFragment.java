/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.slidingmenu.lib.SlidingMenu;

/**
 * Settings for the Alarm Clock.
 */
public class SettingsFragment extends SettingsPreferenceFragment implements
		OnPreferenceChangeListener,
		SettingsPreferenceFragment.OnPreferenceAttachedListener,
		FragmentChangeActivity.OnLifeCycleChangeListener {

	private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;

	private static final String KEY_ALARM_IN_SILENT_MODE = "alarm_in_silent_mode";
	static final String KEY_ALARM_SNOOZE = "snooze_duration";
	static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";
	static final String KEY_ALARM_LIMIT = "alarm_limit";
	static SettingsPreferenceFragment _context;
	static SharedPreferences sharedPref;

	public static final String SHARED_PREFS_NAME = "settings";

	static Context context;
    FragmentChangeActivity mActivity;
    SlidingMenu menu;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentChangeActivity) activity;
        menu = mActivity.getSlidingMenu();
    }

    @Override
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		_context = this;
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);
		addPreferencesFromResource(R.xml.settings);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		((FragmentChangeActivity) getActivity())
				.setOnLifeCycleChangeListener(this);
		View root = super.onCreateView(inflater, container, bundle);
		final ImageView moreAlarm = (ImageView) root
				.findViewById(R.id.more_alarm);
		FragmentChangeActivity.moreAlarm = moreAlarm;
		moreAlarm.setOnClickListener(new View.OnClickListener() {
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

		View.OnClickListener back_click = new View.OnClickListener() {
			public void onClick(View v) {
				mActivity.switchContent(new MainFragment());
			}
		};
		ImageView b = (ImageView) root.findViewById(R.id.back);
		b.setOnClickListener(back_click);

		b = (ImageView) root.findViewById(R.id.logo);
		b.setOnClickListener(back_click);

		FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);

		final Preference snooze = _context.findPreference(KEY_ALARM_SNOOZE);
		final String[] snooze_lists = getResources().getStringArray(
				R.array.snooze_duration_entries);
		final String[] snooze_values = getResources().getStringArray(
				R.array.snooze_duration_values);
		snooze.setSummary(CustomListPreference.getEntryOfValue(
				sharedPref.getString(snooze.getKey(), "10"), snooze_lists,
				snooze_values));
		snooze.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showListPreference(snooze.getKey(), snooze.getTitle(),
						snooze_lists, snooze_values, "10", false);
				return true;
			}

		});

		final Preference volume = _context.findPreference(KEY_VOLUME_BEHAVIOR);
		final String[] volume_lists = getResources().getStringArray(
				R.array.volume_button_setting_entries);
		final String[] volume_values = getResources().getStringArray(
				R.array.volume_button_setting_values);
		volume.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				showListPreference(volume.getKey(), volume.getTitle(),
						volume_lists, volume_values, "0", false);
				return true;
			}

		});

		final Preference alarmlimit = _context.findPreference(KEY_ALARM_LIMIT);
		final String[] limit_lists = getResources().getStringArray(
				R.array.alarm_limit_entries);
		final String[] limit_values = getResources().getStringArray(
				R.array.alarm_limit_values);
		alarmlimit.setSummary(CustomListPreference.getEntryOfValue(
				sharedPref.getString(alarmlimit.getKey(), "30"), limit_lists,
				limit_values));
		alarmlimit
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						showListPreference(alarmlimit.getKey(),
								alarmlimit.getTitle(), limit_lists,
								limit_values, "30", false);
						return true;
					}
				});
		return root;
	}

	private void showListPreference(String key, CharSequence title,
			String[] lists, String[] values, String defaultValue,
			boolean isMultiChoice) {
		Intent intent = new Intent(context, CustomListPreference.class);
		intent.putExtra("key", key);
		intent.putExtra("title", title);
		intent.putExtra("lists", lists);
		intent.putExtra("values", values);
		intent.putExtra("multi", isMultiChoice);
		intent.putExtra("default", sharedPref.getString(key, defaultValue));
		intent.putExtra("mode", 0);
		startActivity(intent);
	}

	public static void onActivityResult(String key, String entry, String value) {
		if (KEY_ALARM_SNOOZE.equals(key) || KEY_VOLUME_BEHAVIOR.equals(key)
				|| KEY_ALARM_LIMIT.equals(key)) {
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(key, value);
			editor.commit();
			if (!KEY_VOLUME_BEHAVIOR.equals(key))
				_context.findPreference(key).setSummary(entry);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
			CheckBoxPreference pref = (CheckBoxPreference) preference;
			int ringerModeStreamTypes = Settings.System.getInt(getActivity()
					.getContentResolver(),
					Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

			if ((Boolean) newValue) {
				ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
			} else {
				ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
			}

			Settings.System.putInt(getActivity().getContentResolver(),
					Settings.System.MODE_RINGER_STREAMS_AFFECTED,
					ringerModeStreamTypes);

			return true;
		}
		return false;
	}

	static void refresh() {
		final CheckBoxPreference alarmInSilentModePref = (CheckBoxPreference) _context
				.findPreference(KEY_ALARM_IN_SILENT_MODE);
		final int silentModeStreams = Settings.System.getInt(_context
				.getActivity().getContentResolver(),
				Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
		alarmInSilentModePref
				.setChecked((silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);
		alarmInSilentModePref
				.setOnPreferenceChangeListener((OnPreferenceChangeListener) _context);
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		if (root == null)
			return;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	}

	@Override
	public void onBackPressed() {
		mActivity.switchContent(new MainFragment());
	}
}
