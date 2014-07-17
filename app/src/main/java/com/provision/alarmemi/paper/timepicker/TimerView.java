/*
 * Copyright (C) 2012 The Android Open Source Project
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
 * limitations under the License
 */

package com.provision.alarmemi.paper.timepicker;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.provision.alarmemi.paper.R;

public class TimerView extends LinearLayout {

	private TextView mHoursOnes, mMinutesOnes;
	private TextView mHoursTens, mMinutesTens;
	private TextView mSeconds;
	private final Typeface mAndroidClockMonoThin;

	public TimerView(Context context) {
		this(context, null);
	}

	public TimerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mAndroidClockMonoThin = Typeface.createFromAsset(context.getAssets(),
				"fonts/AndroidClockMono-Thin.ttf");
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mHoursTens = (TextView) findViewById(R.id.hours_tens);
		mMinutesTens = (TextView) findViewById(R.id.minutes_tens);
		mHoursOnes = (TextView) findViewById(R.id.hours_ones);
		mMinutesOnes = (TextView) findViewById(R.id.minutes_ones);
		mSeconds = (TextView) findViewById(R.id.seconds);
		// If we have hours tens, we are in the alarm time picker, set the hours
		// font to thin
		// to prevent the need to set the top paddings (see b/7407383).
		if (mHoursTens != null) {
			mHoursTens.setTypeface(mAndroidClockMonoThin);
			if (mHoursOnes != null) {
				mHoursOnes.setTypeface(mAndroidClockMonoThin);
			}
		}
		// Set the lowest time unit with thin font (excluding hundredths)
		if (mSeconds != null) {
			mSeconds.setTypeface(mAndroidClockMonoThin);
		} else {
			if (mMinutesTens != null) {
				mMinutesTens.setTypeface(mAndroidClockMonoThin);
			}
			if (mMinutesOnes != null) {
				mMinutesOnes.setTypeface(mAndroidClockMonoThin);
			}
		}
	}

	public void setTime(int hoursTensDigit, int hoursOnesDigit,
			int minutesTensDigit, int minutesOnesDigit, int seconds) {
		if (mHoursTens != null) {
			// Hide digit
			if (hoursTensDigit == -2) {
				mHoursTens.setVisibility(View.INVISIBLE);
			} else if (hoursTensDigit == -1) {
				mHoursTens.setBackgroundResource(R.drawable.picker_underbar);
				mHoursTens.setText(" ");
				mHoursTens.setVisibility(View.VISIBLE);
			} else {
				mHoursTens.setBackgroundResource(0);
				mHoursTens.setText(String.format("%d", hoursTensDigit));
				mHoursTens.setVisibility(View.VISIBLE);
			}
		}
		if (mHoursOnes != null) {
			if (hoursOnesDigit == -1) {
				mHoursOnes.setBackgroundResource(R.drawable.picker_underbar);
				mHoursOnes.setText(" ");
			} else {
				mHoursOnes.setBackgroundResource(0);
				mHoursOnes.setText(String.format("%d", hoursOnesDigit));
			}
		}
		if (mMinutesTens != null) {
			if (minutesTensDigit == -1) {
				mMinutesTens.setBackgroundResource(R.drawable.picker_underbar);
				mMinutesTens.setText(" ");
			} else {
				mMinutesTens.setBackgroundResource(0);
				mMinutesTens.setText(String.format("%d", minutesTensDigit));
			}
		}
		if (mMinutesOnes != null) {
			if (minutesOnesDigit == -1) {
				mMinutesOnes.setBackgroundResource(R.drawable.picker_underbar);
				mMinutesOnes.setText(" ");
			} else {
				mMinutesOnes.setBackgroundResource(0);
				mMinutesOnes.setText(String.format("%d", minutesOnesDigit));
			}
		}

		if (mSeconds != null) {
			mSeconds.setText(String.format("%02d", seconds));
		}
	}
}
