package com.provision.alarmemi.paper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.provision.alarmemi.paper.fragments.SetAlarmFragment;

public class RepeatListPreference extends CustomListPreference {
	// Initial value that can be set with the values saved in the database.
	private static Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);
	// New value that will be set if a positive result comes back from the
	// dialog.
	private static Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);
	boolean[] checkedDaysOfWeek;

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				checkedDaysOfWeek[position] = !checkedDaysOfWeek[position];
				mNewDaysOfWeek.set(position, checkedDaysOfWeek[position]);
			}
		});
		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mDaysOfWeek.set(mNewDaysOfWeek);
				SetAlarmFragment.setRepeatSummary(mDaysOfWeek.toString(
                        RepeatListPreference.this, true));
				SetAlarmFragment.isChanged = true;
				finish();
			}
		});
	}

	@Override
	public void setDefault(String value) {
		Log.d("hello", "setDefault");
		boolean[] values = checkedDaysOfWeek = mDaysOfWeek.getBooleanArray();
		for (int i = 0; i < values.length; i++) {
			listView.setItemChecked(i, values[i]);
		}

	}

	public static void setDaysOfWeek(Context context, Alarm.DaysOfWeek dow) {
		mDaysOfWeek.set(dow);
		mNewDaysOfWeek.set(dow);
		SetAlarmFragment.setRepeatSummary(dow.toString(context, true));
	}

	public static Alarm.DaysOfWeek getDaysOfWeek() {
		return mDaysOfWeek;
	}
}
