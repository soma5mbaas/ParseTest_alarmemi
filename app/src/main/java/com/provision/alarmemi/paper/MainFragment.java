package com.provision.alarmemi.paper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.provision.alarmemi.paper.CustomAlertDialog.CustomAlertDialogListener;
import com.slidingmenu.lib.SlidingMenu;

public class MainFragment extends BaseFragment implements OnItemClickListener,
        OnItemLongClickListener {
	IntentFilter filter;
	static Handler progressDismiss = new ProgressDismiss();
	static boolean isRunning = false, isFirst = true;
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			updateTime();
		}

	};

	ViewGroup root;

	private void switchContent(Fragment f) {
		mActivity.switchContent(f);
	}

	private static void setAlarmIntent(Intent i) {
		mActivity.setAlarmGetIntent = i;
		mActivity.switchContent(new SetAlarmFragment());
	}

	static LinearLayout bkg;

    // 상단 시계 영역의 시간을 업데이트한다.
    void updateTime() {
        ImageView i1 = (ImageView) root.findViewById(R.id.clock1);
        ImageView i2 = (ImageView) root.findViewById(R.id.clock2);
        ImageView i3 = (ImageView) root.findViewById(R.id.clock3);
        ImageView i4 = (ImageView) root.findViewById(R.id.clock4);
        ImageView ampm = (ImageView) root.findViewById(R.id.ampm);
        bkg = (LinearLayout) root.findViewById(R.id.bkg);

        Calendar c = new GregorianCalendar();
        i1.setImageResource(getTimeResByInt((int) c.get(Calendar.HOUR) / 10));
        i2.setImageResource(getTimeResByInt((int) c.get(Calendar.HOUR) % 10));
        i3.setImageResource(getTimeResByInt((int) c.get(Calendar.MINUTE) / 10));
        i4.setImageResource(getTimeResByInt((int) c.get(Calendar.MINUTE) % 10));
        ampm.setImageResource(c.get(Calendar.AM_PM) == Calendar.AM ? R.drawable.am
                : R.drawable.pm);

        // mActivity is for 12:00AM.
        if (c.get(Calendar.AM_PM) == Calendar.AM && c.get(Calendar.HOUR) == 0) {
            i1.setImageResource(R.drawable.num1);
            i2.setImageResource(R.drawable.num2);
        }

        if (c.get(Calendar.HOUR_OF_DAY) < 6 || c.get(Calendar.HOUR_OF_DAY) > 18) {
            bkg.setBackgroundResource(R.drawable.night_bg);
        } else
            bkg.setBackgroundResource(R.drawable.day_bg);
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

	public int Dpi(int dpi) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpi,
				r.getDisplayMetrics());
		return (int) px;
	}

	static final String PREFERENCES = "AlarmClock";

	/**
	 * mContext must be false for production. If true, turns on logging, test
	 * code, etc.
	 */
	static final boolean DEBUG = false;

	private LayoutInflater mFactory;
	private static ListView mAlarmsList;
	static ScrollView mScrollView;
	private Cursor mCursor;
	static AlarmTimeAdapter adapter;
	static int row_height = 0, row_height_ = 0, row_height2 = 0,
			row_height2_ = 0;

	static Handler toastHandler = new ToastHandler();

	private class AlarmTimeAdapter extends CursorAdapter {
		public AlarmTimeAdapter(Context mActivity, Cursor cursor) {
			super(mActivity, cursor);
		}

		public View newView(Context mActivity, Cursor cursor, ViewGroup parent) {
			ViewGroup ret_margin = (ViewGroup) mFactory.inflate(
					R.layout.alarm_list_item_margin, parent, false);

			View ret = mFactory.inflate(R.layout.alarm_time, ret_margin, true);

			DigitalClock digitalClock = (DigitalClock) ret
					.findViewById(R.id.digitalClock);
			digitalClock.setLive(false);
			return ret_margin;
		}

		public void bindView(View view, final Context mActivity, Cursor cursor) {
			if (cursor.getPosition() == cursor.getCount() - 1) {
				view.findViewById(R.id.alarm_time).setBackgroundResource(
						R.drawable.pref_bottom_rounded);
				int padding = view.getPaddingLeft();
				view.setPadding(padding, 0, padding, padding);
			} else {
				view.findViewById(R.id.alarm_time).setBackgroundResource(
						R.drawable.pref_rectangle);
				int padding = view.getPaddingLeft();
				view.setPadding(padding, 0, padding, 0);
			}
			final Alarm alarm = new Alarm(cursor);

			View indicator = view.findViewById(R.id.indicator);

			LinearLayout color_layout = (LinearLayout) indicator
					.findViewById(R.id.color);
			ShapeDrawable alarm_drawable = new ShapeDrawable(new OvalShape());
			alarm_drawable.getPaint().setColor(alarm.color);
			color_layout.setBackgroundDrawable(alarm_drawable);

			// Set the initial state of the clock "checkbox"
			final CheckBox clockOnOff = (CheckBox) indicator
					.findViewById(R.id.clock_onoff);
			if (alarm.cloudEnabled)
				clockOnOff.setBackgroundResource(R.drawable.indicator_cloud_clock_onoff);
			else clockOnOff.setBackgroundResource(R.drawable.indicator_clock_onoff);
			clockOnOff.setChecked(alarm.enabled);

			// Clicking outside the "checkbox" should also change the state.
			indicator.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// clockOnOff.toggle();
					if (alarm.cloudEnabled) {
						FragmentChangeActivity.progressDialog.show();
						new Thread() {
							@Override
							public void run() {
								EnableAlarm(alarm);
								progressDismiss.sendEmptyMessage(0);
							}
						}.start();
					} else {
						Alarms.enableAlarm(mActivity, alarm.id, !alarm.enabled);
						if (!alarm.enabled) {
							String toastText = Alarms.formatToast(mActivity,
									Alarms.calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis());
							showToast(toastText);
						}
					}
				}
			});

			DigitalClock digitalClock = (DigitalClock) view
					.findViewById(R.id.digitalClock);

			// set the alarm text
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			digitalClock.updateTime(c);

			// Set the repeat text or leave it blank if it does not repeat.
			TextView daysOfWeekView = (TextView) digitalClock
					.findViewById(R.id.daysOfWeek);
			daysOfWeekView.setSelected(true);

			final String daysOfWeekStr = alarm.daysOfWeek.toString(mActivity,
					false);
			if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
				daysOfWeekView.setText(daysOfWeekStr);
				daysOfWeekView.setVisibility(View.VISIBLE);
			} else {
				daysOfWeekView.setVisibility(View.GONE);
			}

			// Display the label
			TextView labelView = (TextView) view.findViewById(R.id.label);
			if (alarm.label != null && alarm.label.length() != 0) {
				labelView.setText(alarm.label);
				labelView.setVisibility(View.VISIBLE);
			} else {
				labelView.setVisibility(View.GONE);
			}

			// Display the cloud's devices
			LinearLayout cloud_layout = (LinearLayout) view
					.findViewById(R.id.cloud_layout);
			if (alarm.cloudEnabled) {
				cloud_layout.setVisibility(View.VISIBLE);
				TextView cloud_devices = (TextView) view
						.findViewById(R.id.cloud_devices);
				cloud_devices.setText(alarm.cloudName + " - "
						+ alarm.cloudDevices);
			} else {
				cloud_layout.setVisibility(View.GONE);
			}
		}
	};

	static JSONArray json = null;
	static JSONArray tempjson = null;
	static String selectedDevice = "";
	static CharSequence items[] = null;
	static CharSequence UIDitems[] = null;
	static boolean checkedItems[] = null;

	static void EnableAlarm(final Alarm alarm) {
		String json_string = prefs.getString(alarm.cloudName
				+ "_registeredDevice", "");
		if (json_string.equals(""))
			json_string = "[]";
		try {
			json = new JSONArray(json_string);
			tempjson = new JSONArray("[]");
			items = new CharSequence[json.length()];
			UIDitems = new CharSequence[json.length()];
			checkedItems = new boolean[json.length()];
			for (int j = 0; j < json.length(); j++) {
				JSONObject jsonObj = json.getJSONObject(j);
				items[j] = jsonObj.getString("name");
				UIDitems[j] = jsonObj.getString("uid");
				checkedItems[j] = alarm.cloudUID.contains(jsonObj
						.getString("uid"));
			}
		} catch (JSONException e) {
			android.util.Log.e("url", e.toString());
		}

		selectedDevice = "";
		if (!alarm.enabled) {
			for (int i = 0; i < json.length(); i++) {
				if (UIDitems[i].toString().equals(myUUID))
					checkedItems[i] = true;
				if (checkedItems[i]) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", URLDecoder.decode(items[i].toString()));
					map.put("uid", UIDitems[i].toString());
					tempjson.put(map);
					selectedDevice += items[i] + ", ";
				}
			}
		} else {
			for (int i = 0; i < json.length(); i++) {
				if (UIDitems[i].toString().equals(myUUID))
					checkedItems[i] = false;
				if (checkedItems[i]) {
					Map<String, String> map = new HashMap<String, String>();
					map.put("name", URLDecoder.decode(items[i].toString()));
					map.put("uid", UIDitems[i].toString());
					tempjson.put(map);
					selectedDevice += items[i] + ", ";
				}
			}
		}
		if (!selectedDevice.equals(""))
			selectedDevice = selectedDevice.substring(0,
					selectedDevice.length() - 2);

		String result = null;
		try {
			String url = "http://alarmemi.appspot.com/alarmemi/alarm/enable"
					+ "?owner_name="
					+ URLEncoder.encode(alarm.cloudName, "UTF-8")
					+ "&owner_password="
					+ prefs.getString(alarm.cloudName + "_password", "")
					+ "&target_device="
					+ URLEncoder.encode(selectedDevice, "UTF-8")
					+ "&target_device_uid="
					+ URLEncoder.encode(tempjson.toString(), "UTF-8") + "&key="
					+ alarm.cloudKey + "&my_uuid=" + myUUID;
			result = ServerUtilities.connect(url, mActivity);
		} catch (UnsupportedEncodingException e) {
			android.util.Log.e("url", e.toString());
		}
		if (result == null) {
			showToast(mActivity.getString(R.string.cloud_failed));
		} else if (result.equals("CONNECTION_FAILED")) {
			showToast(mActivity.getString(R.string.connection_chk));
		} else if (result.equals("FAILED")) {
			showToast(mActivity.getString(R.string.cloud_failed));
		} else {
			alarm.cloudDevices = selectedDevice;
			alarm.cloudUID = tempjson.toString();
			Alarms.setAlarm(mActivity, alarm);

			Alarms.enableAlarm(mActivity, alarm.id, !alarm.enabled);
			if (!alarm.enabled) {
				String toastText = Alarms.formatToast(
						mActivity,
						Alarms.calculateAlarm(alarm.hour, alarm.minutes,
								alarm.daysOfWeek).getTimeInMillis());
				showToast(toastText);
			}
		}
	}

	public static void onContextItemSelected(int btnId, int position) {
		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(position);
		final Alarm alarm = new Alarm(c);
		final int alarmId = alarm.id;
		if (alarmId == -1) {
			return;
		}
		switch (btnId) {
		case R.id.delete_alarm:
			new AlertDialogBuilder(mActivity, R.string.delete_alarm,
					R.string.delete_alarm_confirm, true,
					new CustomAlertDialogListener() {
						@Override
						public void onOk() {
							if (alarm.cloudEnabled) {
								SetAlarmFragment.deleteCloudAlarm(mActivity, alarm);
							} else
								Alarms.deleteAlarm(mActivity, alarmId);
						}

						@Override
						public void onCancel() {
						}
					});
			return;

		case R.id.enable_alarm:
			if (alarm.cloudEnabled) {
				FragmentChangeActivity.progressDialog.show();
				new Thread() {
					@Override
					public void run() {
						EnableAlarm(alarm);
						progressDismiss.sendEmptyMessage(0);
					}
				}.start();
			} else {
				Alarms.enableAlarm(mActivity, alarm.id, !alarm.enabled);
				if (!alarm.enabled) {
					String toastText = Alarms.formatToast(
							mActivity,
							Alarms.calculateAlarm(alarm.hour, alarm.minutes,
									alarm.daysOfWeek).getTimeInMillis());
					showToast(toastText);
				}
			}
			return;

		case R.id.edit_alarm:
			Intent intent = new Intent();
			intent.putExtra(Alarms.ALARM_ID, alarmId);
			setAlarmIntent(intent);
			return;

		default:
			break;
		}
	}

	static class ProgressDismiss extends Handler {
		@Override
		public void handleMessage(Message msg) {
			FragmentChangeActivity.progressDialog.dismiss();
		}
	}

	static void showToast(String result) {
		Message m = new Message();
		m.obj = result;
		toastHandler.sendMessage(m);
	}

	static class ToastHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String result = (String) msg.obj;
			Toast.makeText(mActivity, result, Toast.LENGTH_SHORT).show();
		}
	}

	Handler UpdateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			new AlertDialogBuilder(mActivity, R.string.app_name,
					R.string.please_update, false,
					new CustomAlertDialogListener() {
						@Override
						public void onOk() {
							Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
							marketLaunch.setData(Uri
									.parse("market://details?id=com.provision.alarmemi"));
							mActivity.startActivity(marketLaunch);
						}

						@Override
						public void onCancel() {
						}
					});
		}
	};

	public boolean UpdateCheck() {
		if (!Internet.Check(mActivity))
			return true;
		String source = "";
		PackageInfo pi;
		String version = "";
		try {
			pi = mActivity.getPackageManager().getPackageInfo(
					mActivity.getPackageName(), 0);
			version = pi.versionName;
		} catch (NameNotFoundException e) {
			return true;
		}
		String documentURL = "http://www.provisionmod.com/alarmemi/VersionInfo.txt";
		StringBuffer url_content = new StringBuffer();
		try {
			URL url = new URL(documentURL);
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String inStr = "";
			while ((inStr = br.readLine()) != null) {
				url_content.append(inStr);
			}
			source = new String(url_content);
		} catch (Exception e) {
			return true;
		}
		float versionf = Float.parseFloat(version);
		float sourcef = Float.parseFloat(source);
		if (versionf < sourcef)
			return false;
		return true;
	}

	static SharedPreferences prefs;
	static String myUUID;
	static ImageView moreAlarm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle b) {
		mActivity.setOnLifeCycleChangeListener(this);

		myUUID = SplashActivity.myUUID;

		mFactory = LayoutInflater.from(mActivity);
		mCursor = Alarms.getAlarmsCursor(mActivity.getContentResolver());
		prefs = mActivity.getSharedPreferences("forest", Context.MODE_PRIVATE);
		new Thread() {
			@Override
			public void run() {
				SlideMenu.GetPush(mActivity);
			}
		}.start();

		new Thread() {
			@Override
			public void run() {
				if (!UpdateCheck())
					UpdateHandler.sendEmptyMessage(0);
			}
		}.start();

		root = (ViewGroup) inflater.inflate(R.layout.activity_main, null);
		ViewTreeObserver vto = root.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(this);

		if (isFirst) {
			LinearLayout row_layout2 = (LinearLayout) root
					.findViewById(R.id.row_layout2);
			LinearLayout row_layout2_ = (LinearLayout) root
					.findViewById(R.id.row_layout2_);
			row_layout2.setVisibility(View.VISIBLE);
			row_layout2_.setVisibility(View.VISIBLE);
		}

		updateLayout(inflater);
		isRunning = true;

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		mActivity.registerReceiver(mReceiver, filter);
		updateTime();
	}

	@Override
	public void onPause() {
		super.onPause();
		mActivity.unregisterReceiver(mReceiver);
	}

	static LinearLayout addAlarm;

	static void ListViewResizing() {
		if (adapter.getCount() == 0) {
			addAlarm.setBackgroundResource(R.drawable.pref_rounded);
		} else {
			addAlarm.setBackgroundResource(R.drawable.pref_top_rounded);
		}
	}

	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus || root == null)
			return;

		if (isFirst) {
			LinearLayout row_layout2 = (LinearLayout) root
					.findViewById(R.id.row_layout2);
			LinearLayout row_layout2_ = (LinearLayout) root
					.findViewById(R.id.row_layout2_);
			row_height2 = row_layout2.getHeight();
			row_layout2.setVisibility(View.GONE);
			row_height2_ = row_layout2_.getHeight() - row_height2 - 1;
			row_layout2_.setVisibility(View.GONE);

			isFirst = false;
		}
		ListViewResizing();
	}

	static Handler ListViewResizingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ListViewResizing();
		}
	};

	private void updateLayout(LayoutInflater inflater) {

		mScrollView = (ScrollView) root.findViewById(R.id.mScrollView);
		LinearLayout add_alarm_button = (LinearLayout) inflater.inflate(
				R.layout.add_alarm_button, null);
		LinearLayout alarm_list_header = (LinearLayout) inflater.inflate(
				R.layout.alarm_list_header, null);
		mAlarmsList = (ListView) root.findViewById(R.id.alarms_list);
		mAlarmsList.addHeaderView(alarm_list_header);
		mAlarmsList.addHeaderView(add_alarm_button);
		adapter = new AlarmTimeAdapter(mActivity, mCursor);
		mAlarmsList.setAdapter(adapter);
		mAlarmsList.setVerticalScrollBarEnabled(true);
		mAlarmsList.setOnItemClickListener(this);
		mAlarmsList.setOnItemLongClickListener(this);

		addAlarm = (LinearLayout) root.findViewById(R.id.add_alarm);
		addAlarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewAlarm();
			}
		});

		FragmentChangeActivity.moreAlarm = moreAlarm = (ImageView) root
				.findViewById(R.id.more_alarm);
		moreAlarm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Welcome.hideWelcomeContent(true);
				if (menu.isMenuShowing()) {
					menu.showContent();
				} else {
					menu.showMenu(true);
				}
			}
		});
		// Make the entire view selected when focused.
		moreAlarm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				v.setSelected(hasFocus);
			}
		});

		FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);

	}

	void addNewAlarm() {
		setAlarmIntent(new Intent());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ToastMaster.cancelToast();
		mCursor.close();
		isRunning = false;
	}

	public void onItemClick(AdapterView parent, View v, int pos, long id) {
		Intent intent = new Intent();
		intent.putExtra(Alarms.ALARM_ID, (int) id);
		setAlarmIntent(intent);
	}

	@Override
	public void onGlobalLayout() {
		onWindowFocusChanged(true);
		ViewTreeObserver obs = root.getViewTreeObserver();
		obs.removeGlobalOnLayoutListener(this);

		Welcome.welcomeContent
				.add(new Welcome.WelcomeContent(
						Welcome.FIRST_SCREEN_ID,
						"알람매미에 오신 것을 환영합니다!",
						"알람이 울려도 듣지도 못했거나, 알람이 울려도 끄고 다시 잔적 한번쯤은 있으실 겁니다. 그러나 이제 그런일은 더이상 없을것이라고 확신합니다. 매미와 함께 상쾌한(?) 아침을 맞이해보세요.",
						null, true));
		Welcome.welcomeContent
				.add(new Welcome.WelcomeContent(
						Welcome.ADD_ALARM_ID,
						"새 알람 추가하기",
						"이 버튼을 눌러 새 알람을 추가할 수 있으며, 숲의 다른 기기를 선택하여 여러 기기에서 동시에 울리게 할 수 있습니다.",
						addAlarm, false));
		Welcome.welcomeContent.add(new Welcome.WelcomeContent(
				Welcome.MORE_ALARM_ID, "슬라이드 메뉴 펼치기",
				"이 버튼을 눌러 슬라이드 메뉴를 펼쳐서 다른 화면으로 이동하거나 숲의 알림을 볼 수 있습니다.",
				moreAlarm, false));
		Welcome.showWelcomeContents(getActivity());
	}

	@Override
	public void onBackPressed() {
		if (ShowcaseView.opened)
			ShowcaseView.hideShowcase();
		else mActivity.finish();
	}

	@Override
	public boolean onItemLongClick(AdapterView parent, View v, int pos, long id) {

		final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(pos);
		final Alarm alarm = new Alarm(c);

		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, alarm.hour);
		cal.set(Calendar.MINUTE, alarm.minutes);
		final String time = Alarms.formatTime(mActivity, cal);
		Intent intent = new Intent(mActivity, AlarmContextMenu.class);
		intent.putExtra("time", time);
		intent.putExtra("label", alarm.label);
		intent.putExtra("color", alarm.color);
		intent.putExtra("enabled", alarm.enabled);
		intent.putExtra("isCloud", alarm.cloudEnabled);
		intent.putExtra("position", pos);
		startActivity(intent);

		return false;
	}

}
