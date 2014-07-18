package com.provision.alarmemi.paper;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.provision.alarmemi.paper.fragments.SetAlarmFragment;

public class SnoozeTest extends Activity implements SensorEventListener {

	private long lastTime, lastTime2;
	int gabOfCount;

	private static int SHAKE_COUNTLINE = 50, SHAKE_COUNTPOINT = 24;

	private SensorManager sensorManager;
	private Sensor accelerormeterSensor;

	private int snoozeShakeCount = 0, savedSame;

	private int directions[] = new int[3];
	private float last_dirs[] = new float[3];
	private static final int DATAS[] = { SensorManager.DATA_X,
			SensorManager.DATA_Y, SensorManager.DATA_Z };

	static boolean sensorPause = false;

	TextView result, result_ing;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sensorPause = false;
		snoozeShakeCount = 0;
		setContentView(R.layout.snooze_test);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerormeterSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		result = (TextView) findViewById(R.id.result);
		result_ing = (TextView) findViewById(R.id.result_ing);
		result.setTextColor(Color.BLACK);
		result_ing.setText(String.valueOf(SetAlarmFragment.snooze_count));
		ImageButton reset = (ImageButton) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				result.setText("0");
				snoozeShakeCount = 0;
				result.setTextColor(Color.BLACK);
				sensorPause = false;
			}

		});
		ImageButton ok = (ImageButton) findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});
		SHAKE_COUNTLINE = SetAlarmFragment.snooze_strength;
		SHAKE_COUNTPOINT = SetAlarmFragment.snooze_count;
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
			long gabOfTime2 = (currentTime - lastTime2);

			if (gabOfTime2 > 2000) {
				lastTime2 = currentTime;
				if (gabOfCount == snoozeShakeCount) {
					snoozeShakeCount = 0;
					result.setTextColor(Color.BLACK);
					result.setText("0");
				}
				gabOfCount = snoozeShakeCount;
			}

			if (gabOfTime > 10) {
				lastTime = currentTime;

				sum = 0;
				for (i = 0; i < 3; i++) {
					sum += Math.pow(
							Math.abs(event.values[DATAS[i]] - last_dirs[i]), 2);
				}

				// Log.i(Math.sqrt(sum) / gabOfTime * 100+"");

				if (Math.sqrt(sum) / gabOfTime * 100 > SHAKE_COUNTLINE) {
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
						if (snoozeShakeCount < SHAKE_COUNTPOINT)
							snoozeShakeCount++;
						if (snoozeShakeCount >= SHAKE_COUNTPOINT) {
							result.setTextColor(Color.parseColor("#a3d900"));
							sensorPause = true;
						}
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
