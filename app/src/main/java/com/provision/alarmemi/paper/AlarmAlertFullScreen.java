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
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm tone. This
 * activity is the full screen version which shows over the lock screen with the
 * wallpaper as the background.
 */
public class AlarmAlertFullScreen extends Activity implements
		SensorEventListener {

	SharedPreferences prefs;

	// These defaults must match the values in res/xml/settings.xml
	private static final String DEFAULT_SNOOZE = "10";
	private static final String DEFAULT_VOLUME_BEHAVIOR = "0";
	protected static final String SCREEN_OFF = "screen_off";

	private int a = 0, memi_count;

	protected Alarm mAlarm;
	private int mVolumeBehavior;

	Bitmap wallpaper;
	ImageView background;
	final UIHandler UiHandler = new UIHandler();

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

	// Receives the ALARM_KILLED action from the AlarmKlaxon,
	// and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other
	// applications
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
				snooze();
			} else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
				dismiss(false);
			} else {
				Alarm alarm = intent
						.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
				if (alarm != null && mAlarm.id == alarm.id) {
					dismiss(true);
				}
			}
		}
	};

	BroadcastReceiver mTickReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			updateTime();
		}

	};

	void updateTime() {
		ImageView i1 = (ImageView) findViewById(R.id.clock1);
		ImageView i2 = (ImageView) findViewById(R.id.clock2);
		ImageView i3 = (ImageView) findViewById(R.id.clock3);
		ImageView i4 = (ImageView) findViewById(R.id.clock4);
		ImageView ampm = (ImageView) findViewById(R.id.ampm);

		Calendar c = new GregorianCalendar();
		i1.setImageResource(getTimeResByInt((int) c.get(Calendar.HOUR) / 10));
		i2.setImageResource(getTimeResByInt((int) c.get(Calendar.HOUR) % 10));
		i3.setImageResource(getTimeResByInt((int) c.get(Calendar.MINUTE) / 10));
		i4.setImageResource(getTimeResByInt((int) c.get(Calendar.MINUTE) % 10));

		// This is for 12:00AM.
		if (c.get(Calendar.AM_PM) == Calendar.AM && c.get(Calendar.HOUR) == 0) {
			i1.setImageResource(R.drawable.num1);
			i2.setImageResource(R.drawable.num2);
		}
		ampm.setImageResource(c.get(Calendar.AM_PM) == Calendar.AM ? R.drawable.am
				: R.drawable.pm);
	}

	int getTimeResByInt(int time) {
		switch (time) {
		case 0:
			return R.drawable.num0;
		case 1:
			return R.drawable.num1;
		case 2:
			return R.drawable.num2;
		case 3:
			return R.drawable.num3;
		case 4:
			return R.drawable.num4;
		case 5:
			return R.drawable.num5;
		case 6:
			return R.drawable.num6;
		case 7:
			return R.drawable.num7;
		case 8:
			return R.drawable.num8;
		case 9:
			return R.drawable.num9;
		default:
			return R.drawable.num0;
		}
	}

	public static int randomRange(int n1, int n2) {
		return (int) (Math.random() * (n2 - n1 + 1)) + n1;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		prefs = getSharedPreferences("alarmsetting", MODE_PRIVATE);
		SHAKE_COUNTLINE = prefs.getInt("snoozestrength", 50);
		SHAKE_COUNTPOINT = prefs.getInt("snoozecount", 24);
		mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
		memi_count = randomRange(mAlarm.memiCount - 2, mAlarm.memiCount + 2);

		// Get the volume/camera button behavior setting
		final String vol = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(SettingsFragment.KEY_VOLUME_BEHAVIOR,
						DEFAULT_VOLUME_BEHAVIOR);
		mVolumeBehavior = Integer.parseInt(vol);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		// Turn on the screen unless we are being launched from the AlarmAlert
		// subclass.
		if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
			win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
					| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		}

		snoozeShakeCount = 0;
		updateLayout();

		// Register to get the alarm killed/snooze/dismiss intent.
		IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
		filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
		filter.addAction(Alarms.ALARM_DISMISS_ACTION);
		registerReceiver(mReceiver, filter);

		// Time tick receiver. (FOR UPDATING CLOCK)
		filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		registerReceiver(mReceiver, filter);

		registerReceiver(mTickReceiver, filter);

		// Initialize Sensor Manager
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerormeterSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Update the clock
		updateTime();
	}

	private void setTitle() {
		String label = mAlarm.getLabelOrDefault(this);
		TextView title = (TextView) findViewById(R.id.alertTitle);
		title.setText(label);
	}

	public void setTitle(String text) {
		TextView title = (TextView) findViewById(R.id.alertTitle);
		title.setText(text);
	}

	class UIThread extends Thread {
		@Override
		public void run() {
			Bitmap b = fastblur(wallpaper, 20, 0, 0, wallpaper.getWidth(),
					wallpaper.getHeight());

			Message result = new Message();
			result.obj = b;
			UiHandler.sendMessage(result);

			System.gc();
			Runtime.getRuntime().gc();
		}
	}

	class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			background.setImageBitmap((Bitmap) msg.obj);
			super.handleMessage(msg);
		}
	}

	static Bitmap fastblur(Bitmap sentBitmap, int radius, int fromX, int fromY,
			int width, int height) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = width;
		int h = height;

		int[] pix = new int[w * h];

		bitmap.getPixels(pix, 0, w, fromX, fromY, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];

		int roop = 256 * divsum;
		for (i = 0; i < roop; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		int originRadius = radius;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}

		radius = originRadius;

		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8)
						| dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		bitmap.setPixels(pix, 0, w, fromX, fromY, w, h);

		return (bitmap);
	}

	int img_rotate = 0;
	LinearLayout layout_x, layout_y;
	ImageView dayBackground;

	private void updateLayout() {
		LayoutInflater inflater = LayoutInflater.from(this);

		View contentView = inflater.inflate(R.layout.alarm_alert, null);
		setContentView(contentView);

		wallpaper = ((BitmapDrawable) WallpaperManager.getInstance(this)
				.getDrawable()).getBitmap();
		background = (ImageView) findViewById(R.id.background);
		background.setImageBitmap(wallpaper);

		dayBackground = (ImageView) findViewById(R.id.day_background);
		dayBackground.setVisibility(View.GONE);

		layout_x = (LinearLayout) findViewById(R.id.layout_x);
		layout_y = (LinearLayout) findViewById(R.id.layout_y);

		final Bitmap memi_img = BitmapFactory.decodeResource(getResources(),
				R.drawable.memi);
		final Bitmap memi_fly_img = BitmapFactory.decodeResource(
				getResources(), R.drawable.memi_fly);

		UIThread thread = new UIThread();
		thread.setDaemon(true);
		thread.start();

		/*
		 * snooze behavior: pop a snooze confirmation view, kick alarm manager.
		 */

		final View memi_layout_view = (View) findViewById(R.id.memi_layout);
		// Button snooze = (Button) findViewById(R.id.snooze);
		ImageButton alarmemi = (ImageButton) findViewById(R.id.alarmemi);
		alarmemi.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				// Do not touch!
				((ImageView) v).setClickable(false);

				a++;
				// a++;
				Vibrator mVibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				if (mAlarm.vibrate)
					mVibe.vibrate(100);

				final LinearLayout.LayoutParams lpx = (LinearLayout.LayoutParams) layout_x
						.getLayoutParams();
				final LinearLayout.LayoutParams lpy = (LinearLayout.LayoutParams) layout_y
						.getLayoutParams();

				final int rand_x = (int) (Math.random() * (memi_layout_view
						.getWidth() - memi_img.getHeight()));
				final int rand_y = (int) (Math.random() * (memi_layout_view
						.getHeight() - memi_img.getHeight()));
				final int rand_d = randomRange(-180, 180);
				img_rotate += rand_d;

				float before_x = lpx.width;
				float before_y = lpy.height;

				lpx.width = 0;
				lpy.height = 0;
				layout_x.setLayoutParams(lpx);
				layout_y.setLayoutParams(lpy);

				final Matrix matrix = new Matrix();
				matrix.postRotate(img_rotate);
				Bitmap memi_fly = Bitmap.createBitmap(memi_fly_img, 0, 0,
						memi_fly_img.getWidth(), memi_fly_img.getHeight(),
						matrix, true);
				((ImageView) v).setImageBitmap(memi_fly);

				Animation move_ani = new TranslateAnimation(before_x,
						rand_x / 2, before_y, rand_y / 2);
				move_ani.setDuration(300);

				Animation zoomin_ani = new ScaleAnimation(1, 2, 1, 2);
				zoomin_ani.setDuration(300);

				final AnimationSet animSet = new AnimationSet(true);

				animSet.addAnimation(move_ani);
				animSet.addAnimation(zoomin_ani);

				animSet.setAnimationListener(new AnimationListener() {

					public void onAnimationEnd(Animation animation) {
						((ImageView) v).setClickable(true);
						Bitmap memi = Bitmap.createBitmap(memi_img, 0, 0,
								memi_img.getWidth(), memi_img.getHeight(),
								matrix, true);
						((ImageView) v).setImageBitmap(memi);

						lpx.width = rand_x;
						lpy.height = rand_y;
						layout_x.setLayoutParams(lpx);
						layout_y.setLayoutParams(lpy);

						Animation zoomout_ani = new ScaleAnimation(2, 1, 2, 1);
						zoomout_ani.setDuration(300);
						v.startAnimation(zoomout_ani);
					}

					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					public void onAnimationStart(Animation animation) {
					}

				});

				Animation fix_ani = new TranslateAnimation(before_x, before_x,
						before_y, before_y);
				fix_ani.setDuration(0);

				Animation rotate_ani = new RotateAnimation(-rand_d, 0,
						before_x + 80, before_y + 80);
				rotate_ani.setDuration(300);

				final AnimationSet rotateSet = new AnimationSet(true);
				rotateSet.addAnimation(fix_ani);
				rotateSet.addAnimation(rotate_ani);

				rotateSet
						.setAnimationListener(new AnimationListener() {

							public void onAnimationEnd(Animation animation) {
								v.startAnimation(animSet);
							}

							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub

							}

						});
				v.startAnimation(rotateSet);

				// 만약에 매미 카운트를 넘어서면 알람 끝
				if (a >= memi_count) {
					dismiss(false);
				}
			}
		});

		/*
		 * snooze.requestFocus(); snooze.setOnClickListener(new
		 * Button.OnClickListener() { public void onClick(View v) { snooze(); }
		 * });
		 */

		/* Set the title from the passed in alarm */
		setTitle();
	}

	// Attempt to snooze this alert.
	private void snooze() {
		// Do not snooze if the snooze button is disabled.
		// if (!findViewById(R.id.snooze).isEnabled()) {
		// dismiss(false);
		// return;
		// }
		final String snooze = PreferenceManager.getDefaultSharedPreferences(
				this).getString(SettingsFragment.KEY_ALARM_SNOOZE,
				DEFAULT_SNOOZE);
		int snoozeMinutes = Integer.parseInt(snooze);

		final long snoozeTime = System.currentTimeMillis()
				+ (1000 * 60 * snoozeMinutes);
		Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this, mAlarm.id, snoozeTime);

		// Get the display time for the snooze and update the notification.
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(snoozeTime);

		// Append (snoozed) to the label.
		String label = mAlarm.getLabelOrDefault(this);
		label = getString(R.string.alarm_notify_snooze_label, label);

		// Notify the user that the alarm has been snoozed.
		Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
		cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
		cancelSnooze.putExtra(Alarms.ALARM_ID, mAlarm.id);
		PendingIntent broadcast = PendingIntent.getBroadcast(this, mAlarm.id,
				cancelSnooze, 0);
		NotificationManager nm = getNotificationManager();
		Notification n = new Notification(R.drawable.stat_notify_alarm, label,
				0);
		n.setLatestEventInfo(
				this,
				label,
				getString(R.string.alarm_notify_snooze_text,
						Alarms.formatTime(this, c)), broadcast);
		n.flags |= Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONGOING_EVENT;
		nm.notify(mAlarm.id, n);

		String displayTime = getString(R.string.alarm_alert_snooze_set,
				snoozeMinutes);
		// Intentionally log the snooze time for debugging.

		// Display the snooze minutes in a toast.
		Toast.makeText(AlarmAlertFullScreen.this, displayTime,
				Toast.LENGTH_LONG).show();
		stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
		finish();
	}

	private NotificationManager getNotificationManager() {
		return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	// Dismiss the alarm.
	private void dismiss(boolean killed) {
		// The service told us that the alarm has been killed, do not modify
		// the notification or stop the service.
		if (!killed) {
			// Cancel the notification and stop playing the alarm
			NotificationManager nm = getNotificationManager();
			nm.cancel(mAlarm.id);
			stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
		}
		finish();
	}

	/**
	 * this is called when a second alarm is triggered while a previous alert
	 * window is still active.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
		memi_count = randomRange(mAlarm.memiCount - 2, mAlarm.memiCount + 2);

		setTitle();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// If the alarm was deleted at some point, disable snooze.
		if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
			// Button snooze = (Button) findViewById(R.id.snooze);
			// snooze.setEnabled(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// No longer care about the alarm being killed.
		unregisterReceiver(mReceiver);
		unregisterReceiver(mTickReceiver);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// Do this on key down to handle a few of the system keys.
		boolean up = event.getAction() == KeyEvent.ACTION_UP;
		switch (event.getKeyCode()) {
		// Volume keys and camera keys dismiss the alarm
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_CAMERA:
		case KeyEvent.KEYCODE_FOCUS:
			if (up) {
				switch (mVolumeBehavior) {
				case 1:
					snooze();
					break;

				case 2:
					dismiss(false);
					break;

				default:
					break;
				}
			}
			return true;
		default:
			break;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {
		// Don't allow back to dismiss. This method is overriden by AlarmAlert
		// so that the dialog is dismissed.
		return;
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
			long currentTime = System.currentTimeMillis();
			long gabOfTime = (currentTime - lastTime);
			long gabOfTime2 = (currentTime - lastTime2);

			if (gabOfTime2 > 2000) {
				lastTime2 = currentTime;
				if (gabOfCount == snoozeShakeCount)
					snoozeShakeCount = 0;
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
						snoozeShakeCount++;
						if (snoozeShakeCount >= SHAKE_COUNTPOINT) {
							snoozeShakeCount = 0;
							snooze();
						}
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