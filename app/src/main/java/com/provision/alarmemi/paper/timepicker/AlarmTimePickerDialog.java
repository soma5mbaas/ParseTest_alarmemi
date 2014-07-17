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

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.provision.alarmemi.paper.Alarm;
import com.provision.alarmemi.paper.R;

/**
 * Dialog to set alarm time.
 */
public class AlarmTimePickerDialog extends Dialog {

	Context context;
	static ImageButton mDelete;
	private ImageButton mSet;
	private ImageButton mCancel;
	private static TimePicker mPicker;
	AlarmTimePickerDialogHandler mAlarmTimePickerDialogHandler;

	public AlarmTimePickerDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.time_picker_dialog);
		mDelete = (ImageButton) findViewById(R.id.delete_button);
		mSet = (ImageButton) findViewById(R.id.set_button);
		mCancel = (ImageButton) findViewById(R.id.cancel_button);
		mCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				cancel();
			}
		});
		mPicker = (TimePicker) findViewById(R.id.time_picker);
		mPicker.setSetButton(mSet);
		mDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mPicker.delete();
			}
		});
		mDelete.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				mPicker.mAmPmState = mPicker.AMPM_NOT_SELECTED;
				mPicker.reset();
				mPicker.updateKeypad();
				return true;
			}
		});
		mDelete.setEnabled(false);
		mSet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mAlarmTimePickerDialogHandler != null) {
					mAlarmTimePickerDialogHandler.onDialogTimeSet(null,
							mPicker.getHours(), mPicker.getMinutes());
				}
				dismiss();
			}
		});
	}

	public static void updateDeleteButton(TimePicker mPicker) {
		boolean enabled = mPicker.mInputPointer != -1;
		if (mDelete != null) {
			mDelete.setEnabled(enabled);
		}
	}

	public void setAlarmTimePickerDialogHandler(
			AlarmTimePickerDialogHandler listener) {
		mAlarmTimePickerDialogHandler = listener;
	}

	public interface AlarmTimePickerDialogHandler {
		void onDialogTimeSet(Alarm alarm, int hourOfDay, int minute);
	}
}
