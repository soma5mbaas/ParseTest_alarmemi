package com.provision.alarmemi.paper;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MemiCountPreference extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memi_count);

		final TextView handler_v = (TextView) findViewById(R.id.handler_v);
		Typeface face = Typeface.createFromAsset(getAssets(),
				"fonts/AndroidClockMono-Light.ttf");
		handler_v.setTypeface(face);

		final SeekBar seekbar = (SeekBar) findViewById(R.id.seekbar);
		seekbar.setProgress(SetAlarmFragment.memi_count - 5);
		handler_v.setText(SetAlarmFragment.memi_count + "±2");
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				handler_v.setText((progress + 5) + "±2");
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
		});

		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SetAlarmFragment.memi_count = seekbar.getProgress() + 5;
				SetAlarmFragment.isChanged = true;
				SetAlarmFragment.updateMemiCount();
				finish();
			}
		});
		findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
