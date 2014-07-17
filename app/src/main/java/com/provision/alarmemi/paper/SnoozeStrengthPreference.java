package com.provision.alarmemi.paper;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.provision.alarmemi.paper.CustomAlertDialog.CustomAlertDialogListener;

public class SnoozeStrengthPreference extends Activity implements
		SensorEventListener {

	private long lastTime;

	private static int SHAKE_COUNTLINE = 50;

	private SensorManager sensorManager;
	private Sensor accelerormeterSensor;

	private int snoozeShakeCount = 0, snoozeShakeStrength = 2147483647,
			savedSame;

	private int directions[] = new int[3];
	private float last_dirs[] = new float[3];
	private static final int DATAS[] = { SensorManager.DATA_X,
			SensorManager.DATA_Y, SensorManager.DATA_Z };

	static boolean sensorPause = false;

	TextView result;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		snoozeShakeCount = 0;
		snoozeShakeStrength = 2147483647;
		setContentView(R.layout.snooze_strength);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerormeterSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		ImageButton apply = (ImageButton) findViewById(R.id.apply);
		apply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SaveSnoozeStrength();
			}

		});
		ImageButton reset = (ImageButton) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				snoozeShakeCount = 0;
				snoozeShakeStrength = 2147483647;
				result.setText(String.valueOf(snoozeShakeCount));
			}

		});
		result = (TextView) findViewById(R.id.result);
	}

	@Override
	public void onResume() {
		super.onResume();
		snoozeShakeCount = 0;
		snoozeShakeStrength = 2147483647;
		result.setText(String.valueOf(snoozeShakeCount));
	}

	public void SaveSnoozeStrength() {
		if (snoozeShakeCount < 10) {
			snoozeShakeCount = 0;
			snoozeShakeStrength = 2147483647;
			result.setText(String.valueOf(snoozeShakeCount));
			Toast.makeText(this, R.string.shaking_at_least, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		sensorPause = true;
		new AlertDialogBuilder(this, R.string.settings, String.format(
				getString(R.string.save_shaking_question), snoozeShakeCount),
				true, new CustomAlertDialogListener() {
					@Override
					public void onOk() {
						sensorPause = false;
						SetAlarmFragment.snooze_strength = snoozeShakeStrength;
						SetAlarmFragment.snooze_count = snoozeShakeCount;
						snoozeShakeCount = 0;
						snoozeShakeStrength = 2147483647;
						result.setText(String.valueOf(snoozeShakeCount));
						SetAlarmFragment.isChanged = true;
						SetAlarmFragment.updateSnoozeStrength();
						startActivity(new Intent(SnoozeStrengthPreference.this,
								SnoozeTest.class));
					}

					@Override
					public void onCancel() {
						sensorPause = false;
						snoozeShakeCount = 0;
						snoozeShakeStrength = 2147483647;
						result.setText(String.valueOf(snoozeShakeCount));
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();

		if (accelerormeterSensor != null)
			sensorManager.registerListener(this, accelerormeterSensor,
					SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onStop() {
		super.onStop();

		if (sensorManager != null)
			sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public int Direction(float last_a, float a) {
		if (Math.abs(last_a - a) < 5)
			return 0;
		return last_a - a > 0 ? 1 : -1;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int i;
		float sum;
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (sensorPause)
				return;
			long currentTime = System.currentTimeMillis();
			long gabOfTime = (currentTime - lastTime);

			if (gabOfTime > 10) {
				lastTime = currentTime;

				sum = 0;
				for (i = 0; i < 3; i++) {
					sum += Math.pow(
							Math.abs(event.values[DATAS[i]] - last_dirs[i]), 2);
				}

				double tempStrength = Math.sqrt(sum) / gabOfTime * 100;
				if (tempStrength > SHAKE_COUNTLINE) {
					int same = 0;
					for (i = 0; i < 3; i++) {
						if (Direction(last_dirs[i], event.values[DATAS[i]]) == directions[i])
							same++;
					}
					if (same == 3 - savedSame) {
						for (i = 0; i < 3; i++) {
							directions[i] = Direction(last_dirs[i],
									event.values[DATAS[i]]);
						}
						snoozeShakeCount++;
						if (snoozeShakeStrength > tempStrength)
							snoozeShakeStrength = (int) tempStrength;
						result.setText(String.valueOf(snoozeShakeCount));
					} else if (same != savedSame) {
						for (i = 0; i < 3; i++) {
							directions[i] = Direction(last_dirs[i],
									event.values[DATAS[i]]);
							same = savedSame;
						}
					}
				}

				for (i = 0; i < 3; i++) {
					last_dirs[i] = event.values[DATAS[i]];
				}
			}
		}
	}
}
