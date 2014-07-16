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

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.text.format.DateUtils;

public final class Alarm implements Parcelable {

	// ////////////////////////////
	// Parcelable apis
	// ////////////////////////////
	public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
		public Alarm createFromParcel(Parcel p) {
			return new Alarm(p);
		}

		public Alarm[] newArray(int size) {
			return new Alarm[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel p, int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeInt(daysOfWeek.getCoded());
		p.writeLong(time);
		p.writeInt(vibrate ? 1 : 0);
		p.writeString(label);
		p.writeParcelable(alert, flags);
		p.writeInt(silent ? 1 : 0);
		p.writeInt(cloudEnabled ? 1 : 0);
		p.writeString(cloudName);
		p.writeString(cloudDevices);
		p.writeString(cloudKey);
		p.writeString(cloudUID);
		p.writeInt(memiCount);
		p.writeInt(snoozeStrength);
		p.writeInt(snoozeCount);
		p.writeInt(color);
		p.writeInt(noDialog ? 1 : 0);
	}

	// ////////////////////////////
	// end Parcelable apis
	// ////////////////////////////

	// ////////////////////////////
	// Column definitions
	// ////////////////////////////
	public static class Columns implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.provision.alarmemi/alarm");

		/**
		 * Hour in 24-hour localtime 0 - 23.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String HOUR = "hour";

		/**
		 * Minutes in localtime 0 - 59
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MINUTES = "minutes";

		/**
		 * Days of week coded as integer
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String DAYS_OF_WEEK = "daysofweek";

		/**
		 * Alarm time in UTC milliseconds from the epoch.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String ALARM_TIME = "alarmtime";

		/**
		 * True if alarm is active
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String ENABLED = "enabled";

		/**
		 * True if alarm should vibrate
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String VIBRATE = "vibrate";

		/**
		 * Message to show when alarm triggers Note: not currently used
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String MESSAGE = "message";

		/**
		 * Audio alert to play when alarm triggers
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String ALERT = "alert";

		/**
		 * Option to show dialog or not
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String NO_DIALOG = "no_dialog";

		public static final String CLOUD_ENABLED = "cloud_enabled";

		public static final String CLOUD_NAME = "cloud_name";

		public static final String CLOUD_DEVICES = "cloud_devices";

		public static final String CLOUD_KEY = "cloud_key";

		public static final String CLOUD_UID = "cloud_uid";

		public static final String MEMI_COUNT = "memi_count";

		public static final String SNOOZE_STRENGTH = "snooze_strength";

		public static final String SNOOZE_COUNT = "snooze_count";

		public static final String COLOR = "color";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = HOUR + ", " + MINUTES
				+ " ASC";

		// Used when filtering enabled alarms.
		public static final String WHERE_ENABLED = ENABLED + "=1";

		static final String[] ALARM_QUERY_COLUMNS = { _ID, HOUR, MINUTES,
				DAYS_OF_WEEK, ALARM_TIME, ENABLED, VIBRATE, MESSAGE, ALERT,
				CLOUD_ENABLED, CLOUD_NAME, CLOUD_DEVICES, CLOUD_KEY, CLOUD_UID,
				MEMI_COUNT, SNOOZE_STRENGTH, SNOOZE_COUNT, COLOR, NO_DIALOG };

		/**
		 * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT
		 * IN SYNC WITH ABOVE QUERY COLUMNS
		 */
		public static final int ALARM_ID_INDEX = 0;
		public static final int ALARM_HOUR_INDEX = 1;
		public static final int ALARM_MINUTES_INDEX = 2;
		public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;
		public static final int ALARM_TIME_INDEX = 4;
		public static final int ALARM_ENABLED_INDEX = 5;
		public static final int ALARM_VIBRATE_INDEX = 6;
		public static final int ALARM_MESSAGE_INDEX = 7;
		public static final int ALARM_ALERT_INDEX = 8;
		public static final int ALARM_CLOUD_ENABLED_INDEX = 9;
		public static final int ALARM_CLOUD_NAME_INDEX = 10;
		public static final int ALARM_CLOUD_DEVICES_INDEX = 11;
		public static final int ALARM_CLOUD_KEY_INDEX = 12;
		public static final int ALARM_CLOUD_UID_INDEX = 13;
		public static final int ALARM_MEMI_COUNT_INDEX = 14;
		public static final int ALARM_SNOOZE_STRENGTH_INDEX = 15;
		public static final int ALARM_SNOOZE_COUNT_INDEX = 16;
		public static final int ALARM_COLOR_INDEX = 17;
		public static final int ALARM_NO_DIALOG_INDEX = 18;
	}

	// ////////////////////////////
	// End column definitions
	// ////////////////////////////

	// Public fields
	public int id;
	public boolean enabled;
	public int hour;
	public int minutes;
	public DaysOfWeek daysOfWeek;
	public long time;
	public boolean vibrate;
	public String label;
	public Uri alert;
	public boolean silent;
	public boolean cloudEnabled;
	public String cloudName;
	public String cloudDevices;
	public String cloudKey;
	public String cloudUID;
	public int memiCount;
	public int snoozeStrength;
	public int snoozeCount;
	public int color;
	public boolean noDialog;

	public Alarm(Cursor c) {
		id = c.getInt(Columns.ALARM_ID_INDEX);
		enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
		hour = c.getInt(Columns.ALARM_HOUR_INDEX);
		minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
		daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
		time = c.getLong(Columns.ALARM_TIME_INDEX);
		vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
		label = c.getString(Columns.ALARM_MESSAGE_INDEX);
		String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
		cloudEnabled = c.getInt(Columns.ALARM_CLOUD_ENABLED_INDEX) == 1;
		cloudName = c.getString(Columns.ALARM_CLOUD_NAME_INDEX);
		cloudDevices = c.getString(Columns.ALARM_CLOUD_DEVICES_INDEX);
		cloudUID = c.getString(Columns.ALARM_CLOUD_UID_INDEX);
		cloudKey = c.getString(Columns.ALARM_CLOUD_KEY_INDEX);
		memiCount = c.getInt(Columns.ALARM_MEMI_COUNT_INDEX);
		snoozeStrength = c.getInt(Columns.ALARM_SNOOZE_STRENGTH_INDEX);
		snoozeCount = c.getInt(Columns.ALARM_SNOOZE_COUNT_INDEX);
		color = c.getInt(Columns.ALARM_COLOR_INDEX);

		if (Alarms.ALARM_ALERT_SILENT.equals(alertString)) {
			silent = true;
		} else {
			if (alertString != null && alertString.length() != 0) {
				alert = Uri.parse(alertString);
			}

			// If the database alert is null or it failed to parse, use the
			// default alert.
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
		}
		noDialog = c.getInt(Columns.ALARM_NO_DIALOG_INDEX) == 1;
	}

	public Alarm(Parcel p) {
		id = p.readInt();
		enabled = p.readInt() == 1;
		hour = p.readInt();
		minutes = p.readInt();
		daysOfWeek = new DaysOfWeek(p.readInt());
		time = p.readLong();
		vibrate = p.readInt() == 1;
		label = p.readString();
		alert = (Uri) p.readParcelable(null);
		silent = p.readInt() == 1;
		cloudEnabled = p.readInt() == 1;
		cloudName = p.readString();
		cloudDevices = p.readString();
		cloudKey = p.readString();
		cloudUID = p.readString();
		memiCount = p.readInt();
		snoozeStrength = p.readInt();
		snoozeCount = p.readInt();
		color = p.readInt();
		noDialog = p.readInt() == 1;
	}

	// Creates a default alarm at the current time.
	public Alarm() {
		id = -1;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		hour = c.get(Calendar.HOUR_OF_DAY);
		minutes = c.get(Calendar.MINUTE);
		vibrate = true;
		daysOfWeek = new DaysOfWeek(0);
		alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		cloudEnabled = false;
		cloudName = "";
		cloudDevices = "";
		cloudKey = "";
		cloudUID = "[]";
		memiCount = 12;
		snoozeStrength = 50;
		snoozeCount = 24;
		color = Color.BLACK;
	}

	public String getLabelOrDefault(Context context) {
		if (label == null || label.length() == 0) {
			return context.getString(R.string.default_label);
		}
		return label;
	}

	/*
	 * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
	 * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
	 * Sunday
	 */
	static final class DaysOfWeek {

		private static int[] DAY_MAP = new int[] { Calendar.MONDAY,
				Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
				Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, };

		// Bitmask of all repeating days
		private int mDays;

		DaysOfWeek(int days) {
			mDays = days;
		}

		public String toString(Context context, boolean showNever) {
			StringBuilder ret = new StringBuilder();

			// no days
			if (mDays == 0) {
				return showNever ? context.getText(R.string.never).toString()
						: "";
			}

			// every day
			if (mDays == 0x7f) {
				return context.getText(R.string.every_day).toString();
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1)
					dayCount++;
				days >>= 1;
			}

			// short or long form?
			int abbrev = dayCount > 1 ? DateUtils.LENGTH_SHORT
					: DateUtils.LENGTH_LONG;
			// selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & (1 << i)) != 0) {
					ret.append(DateUtils.getDayOfWeekString(DAY_MAP[i], abbrev));
					dayCount -= 1;
					if (dayCount > 0)
						ret.append(context.getText(R.string.day_concat));
				}
			}
			return ret.toString();
		}

		private boolean isSet(int day) {
			return ((mDays & (1 << day)) > 0);
		}

		public void set(int day, boolean set) {
			if (set) {
				mDays |= (1 << day);
			} else {
				mDays &= ~(1 << day);
			}
		}

		public void set(DaysOfWeek dow) {
			mDays = dow.mDays;
		}

		public int getCoded() {
			return mDays;
		}

		// Returns days of week encoded in an array of booleans.
		public boolean[] getBooleanArray() {
			boolean[] ret = new boolean[7];
			for (int i = 0; i < 7; i++) {
				ret[i] = isSet(i);
			}
			return ret;
		}

		public boolean isRepeatSet() {
			return mDays != 0;
		}

		/**
		 * returns number of days from today until next alarm
		 * 
		 * @param c
		 *            must be set to today
		 */
		public int getNextAlarm(Calendar c) {
			if (mDays == 0) {
				return -1;
			}

			int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

			int day = 0;
			int dayCount = 0;
			for (; dayCount < 7; dayCount++) {
				day = (today + dayCount) % 7;
				if (isSet(day)) {
					break;
				}
			}
			return dayCount;
		}
	}
}
