/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;

public class AlarmProvider extends ContentProvider {
	private SQLiteOpenHelper mOpenHelper;

	private static final int ALARMS = 1;
	private static final int ALARMS_ID = 2;
	private static final UriMatcher sURLMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		sURLMatcher.addURI("com.provision.alarmemi", "alarm", ALARMS);
		sURLMatcher.addURI("com.provision.alarmemi", "alarm/#", ALARMS_ID);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "alarms.db";
		private static final int DATABASE_VERSION = 9;
		String insertMe;
		Context context;

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE alarms (" + "_id INTEGER PRIMARY KEY,"
					+ "hour INTEGER, " + "minutes INTEGER, "
					+ "daysofweek INTEGER, " + "alarmtime INTEGER, "
					+ "enabled INTEGER, " + "vibrate INTEGER, "
					+ "message TEXT, " + "alert TEXT, " + "intent TEXT,"
					+ "cloud_enabled INTEGER, " + "cloud_name TEXT, "
					+ "cloud_devices TEXT, " + "cloud_key TEXT, "
					+ "cloud_uid TEXT, " + "memi_count INTEGER, "
					+ "snooze_strength INTEGER, " + "snooze_count INTEGER, "
					+ "color INTEGER, no_dialog INTEGER);");

			// insert default alarms
			insertMe = "INSERT INTO alarms "
					+ "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, "
					+ "message, alert, intent, cloud_enabled, cloud_name, cloud_devices, cloud_key, cloud_uid, "
					+ "memi_count, snooze_strength, snooze_count, color, no_dialog) "
					+ "VALUES ";
			// db.execSQL(insertMe +
			// "(8, 30, 31, 0, 0, 1, '', '', '', 0, '', '', '', '[]', 0);");
			// db.execSQL(insertMe +
			// "(9, 00, 96, 0, 0, 1, '', '', '', 0, '', '', '', '[]', 0);");
		}

		public String nullToBlank(String str) {
			return str == null ? "" : str.replace("'", "''");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion,
				int currentVersion) {
			android.util.Log.d("ALARMEMI_DB",
					"Upgrading alarms database from version " + oldVersion
							+ " to " + currentVersion
							+ ", which will destroy all old data");

			android.util.Log.d("ALARMEMI_DB", oldVersion + "");
			if (oldVersion == 8) {
				SharedPreferences prefs = context.getSharedPreferences(
						"alarmsetting", Context.MODE_PRIVATE);

				int ID = 0;
				int HOUR = 1;
				int MINUTES = 2;
				int DAYS_OF_WEEK = 3;
				int TIME = 4;
				int ENABLED = 5;
				int VIBRATE = 6;
				int MESSAGE = 7;
				int ALERT = 8;
				int INTENT = 9;
				int CLOUD_ENABLED = 10;
				int CLOUD_NAME = 11;
				int CLOUD_DEVICES = 12;
				int CLOUD_KEY = 13;
				int CLOUD_UID = 14;
				int NO_DIALOG = 15;

				Cursor c = db.rawQuery("SELECT * FROM alarms", null);
				android.util.Log.d("ALARMEMI_DB", c.getCount() + "");
				db.execSQL("DROP TABLE IF EXISTS alarms");
				onCreate(db);
				c.moveToFirst();
				while (!c.isAfterLast()) {
					db.execSQL(insertMe
							+ String.format(
									"(%d, %d, %d, %d, %d, %d, '%s', '%s', '%s', %d, '%s', '%s', '%s', '%s', %d, %d, %d, %d, %d);",
									c.getInt(HOUR), c.getInt(MINUTES),
									c.getInt(DAYS_OF_WEEK), c.getInt(TIME),
									c.getInt(ENABLED), c.getInt(VIBRATE),
									nullToBlank(c.getString(MESSAGE)),
									nullToBlank(c.getString(ALERT)),
									nullToBlank(c.getString(INTENT)),
									c.getInt(CLOUD_ENABLED),
									nullToBlank(c.getString(CLOUD_NAME)),
									nullToBlank(c.getString(CLOUD_DEVICES)),
									nullToBlank(c.getString(CLOUD_KEY)),
									nullToBlank(c.getString(CLOUD_UID)),
									prefs.getInt("memicount", 12),
									prefs.getInt("snoozestrength", 50),
									prefs.getInt("snoozecount", 24),
									Color.BLACK, c.getInt(NO_DIALOG)));
					c.moveToNext();
				}
				c.close();

			}
		}
	}

	public AlarmProvider() {
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri url, String[] projectionIn, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		// Generate the body of the query
		int match = sURLMatcher.match(url);
		switch (match) {
		case ALARMS:
			qb.setTables("alarms");
			break;
		case ALARMS_ID:
			qb.setTables("alarms");
			qb.appendWhere("_id=");
			qb.appendWhere(url.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null,
				null, sort);

		if (ret == null) {
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), url);
		}

		return ret;
	}

	@Override
	public String getType(Uri url) {
		int match = sURLMatcher.match(url);
		switch (match) {
		case ALARMS:
			return "vnd.android.cursor.dir/alarms";
		case ALARMS_ID:
			return "vnd.android.cursor.item/alarms";
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		long rowId = 0;
		int match = sURLMatcher.match(url);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (match) {
		case ALARMS_ID: {
			String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = db.update("alarms", values, "_id=" + rowId, null);
			break;
		}
		default: {
			throw new UnsupportedOperationException("Cannot update URL: " + url);
		}
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		if (sURLMatcher.match(url) != ALARMS) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}

		ContentValues values = new ContentValues(initialValues);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert("alarms", Alarm.Columns.MESSAGE, values);
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + url);
		}

		Uri newUrl = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI,
				rowId);
		getContext().getContentResolver().notifyChange(newUrl, null);
		return newUrl;
	}

	public int delete(Uri url, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		long rowId = 0;
		switch (sURLMatcher.match(url)) {
		case ALARMS:
			count = db.delete("alarms", where, whereArgs);
			break;
		case ALARMS_ID:
			String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}
			count = db.delete("alarms", where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}
}
