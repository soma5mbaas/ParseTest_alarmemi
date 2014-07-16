package com.provision.alarmemi.paper;import static com.provision.alarmemi.CommonUtilities.SENDER_ID;import java.io.UnsupportedEncodingException;import java.net.URLEncoder;import org.json.JSONException;import org.json.JSONObject;import android.app.Notification;import android.app.NotificationManager;import android.app.PendingIntent;import android.content.Context;import android.content.Intent;import android.content.SharedPreferences;import android.database.Cursor;import android.os.Handler;import android.util.Log;import com.google.android.gcm.GCMBaseIntentService;public class GCMIntentService extends GCMBaseIntentService {	public GCMIntentService() {		super(SENDER_ID);	}	private static final String TAG = "===GCMIntentService===";	public void onReceive(Context context, Intent intent) {	}	@Override	protected void onRegistered(Context arg0, String registrationId) {		Log.i(TAG, "Device registered: regId = " + registrationId);	}	@Override	protected void onUnregistered(Context arg0, String arg1) {		Log.i(TAG, "unregistered = " + arg1);	}	static Handler registerHandler;	static Context context_;	static void on_Message(Context context, Intent GCMintent, String myUUID) {		context_ = context;		Log.i(TAG, "new message= ");		SharedPreferences prefs = context.getSharedPreferences("forest",				Context.MODE_PRIVATE);		String message = GCMintent.getExtras().getString("message");		Log.i("url", message);		String message_key = "";		if (message.startsWith(Notify.MEMBER_ADD)				|| message.startsWith(Notify.MEMBER_REMOVE)) {			message_key = message;			String result = null;			String message_result;			try {				String proc1[] = message.split(":");				String proc2[] = proc1[1].split("\\|");				String name = proc2[0];				result = ServerUtilities.connect(						"http://alarmemi.appspot.com/alarmemi/forest/getinfo?name="								+ URLEncoder.encode(name, "UTF-8")								+ "&password="								+ prefs.getString(name + "_password", ""),						context);				if (result.equals("CONNECTION_FAILED")) {					message_result = "CONNECTION_FAILED";				} else {					try {						Log.e("url", result);						// Convert result to json						JSONObject json = new JSONObject(result);						// Write to shared						// preferences						SharedPreferences.Editor editor = prefs.edit();						editor.putString(name + "_registeredDevice",								json.getString("registeredDevice"));						editor.commit();						message_result = "SUCCEED";					} catch (JSONException e) {						e.printStackTrace();						Log.d("Alarmemi", "Update failed");						message_result = "FAILED : " + e.toString();					}				}			} catch (Exception e) {				message_result = "FAILED : " + e.toString();			}			if (!message_result.equals("SUCCEED"))				return;		} else if (message.startsWith(Notify.ALARM_ADD)				|| message.startsWith(Notify.ALARM_EDIT)) {			Log.e("url", message);			String key = message.startsWith(Notify.ALARM_ADD) ? message					.substring(Notify.ALARM_ADD.length()).split("\\|")[0]					: message.substring(Notify.ALARM_EDIT.length())							.split("\\|")[0];			JSONObject json = null;			String result = null;			String message_result;			try {				Cursor mCursor = Alarms.getAlarmsCursorWhere(						context.getContentResolver(),						Alarm.Columns.ALARM_QUERY_COLUMNS, "cloud_key='" + key								+ "'");				String cloud_name = "";				if (mCursor.getCount() == 1) {					mCursor.moveToFirst();					cloud_name = mCursor							.getString(Alarm.Columns.ALARM_CLOUD_NAME_INDEX);				} else {					cloud_name = message.split("\\|")[1];				}				result = ServerUtilities.connect(						"http://alarmemi.appspot.com/alarmemi/alarm/get?owner_name="								+ URLEncoder.encode(cloud_name, "UTF-8")								+ "&owner_password="								+ prefs.getString(cloud_name + "_password", "")								+ "&key=" + key, context);				if (result == null) {					message_result = "FAILED";					return;				} else if (result.equals("CONNECTION_FAILED")) {					message_result = "CONNECTION_FAILED";				} else if (result.equals("FAILED")) {					message_result = "FAILED";					return;				} else if (result.equals("NOT_AVAILABLE")) {					message_result = "FAILED";					return;				} else {					json = new JSONObject(result);					Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);					String repeat_str = json.getString("repeat");					for (int i = 0; i < 7; i++) {						mNewDaysOfWeek.set(i,								repeat_str.charAt(i) == 't' ? true : false);					}					Alarm alarm = new Alarm();					String alarm_label = "";					if (mCursor.getCount() == 1) {						alarm_label = mCursor								.getString(Alarm.Columns.ALARM_MESSAGE_INDEX);						alarm.id = mCursor								.getInt(mCursor.getColumnIndex("_id"));					} else {						alarm_label = json.getString("memi_name");						alarm.id = -1;					}					if (alarm_label == null)						alarm_label = "";					message_key = (message.startsWith(Notify.ALARM_ADD) ? Notify.ALARM_ADD							: Notify.ALARM_EDIT)							+ cloud_name							+ "|"							+ ("".equals(alarm_label) ? context									.getString(R.string.noname) : alarm_label);					Log.e("url", "1");					alarm.hour = Integer.valueOf(json.getString("time")							.substring(0, 2));					alarm.minutes = Integer.valueOf(json.getString("time")							.substring(2));					alarm.daysOfWeek = mNewDaysOfWeek;					alarm.vibrate = json.getBoolean("vibrate");					alarm.label = json.getString("memi_name");					alarm.cloudEnabled = true;					alarm.cloudName = json.getString("owner_name");					alarm.cloudDevices = json.getString("target_device");					alarm.cloudKey = key;					alarm.cloudUID = json.getString("target_device_uid");					alarm.enabled = alarm.cloudUID.contains(myUUID);					Log.e("url", "2");					if (alarm.id == -1) {						Alarms.addAlarm(context, alarm);					} else {						Alarms.setAlarm(context, alarm);					}					message_result = "SUCCEED";				}			} catch (Exception e) {				message_result = "FAILED : " + e.toString();			}			if (!message_result.equals("SUCCEED"))				return;		} else if (message.startsWith(Notify.ALARM_REMOVE)) {			String key = message.substring(Notify.ALARM_REMOVE.length()).split(					"\\|")[0];			Cursor mCursor = Alarms.getAlarmsCursorWhere(					context.getContentResolver(),					Alarm.Columns.ALARM_QUERY_COLUMNS, "cloud_key='" + key							+ "'");			String cloud_name = "";			String alarm_label = "";			if (mCursor.getCount() == 1) {				mCursor.moveToFirst();				cloud_name = mCursor						.getString(Alarm.Columns.ALARM_CLOUD_NAME_INDEX);				alarm_label = mCursor						.getString(Alarm.Columns.ALARM_MESSAGE_INDEX);				Alarms.deleteAlarm(context,						mCursor.getInt(mCursor.getColumnIndex("_id")));			}			message_key = Notify.ALARM_REMOVE					+ cloud_name					+ "|"					+ ("".equals(alarm_label) ? context							.getString(R.string.noname) : alarm_label);		}		message_key += "|no";		if (message_key.length() == 0)			notify(context, message, null);		else			notify(context, Notify.getString(context, message_key), new Intent(					context, SplashActivity.class));		SharedPreferences.Editor editor = prefs.edit();		editor.putString("notify",				message_key + "||" + prefs.getString("notify", ""));		editor.commit();		try {			FragmentChangeActivity.OnNotifyArrived.sendEmptyMessage(0);			SlideMenu.OnNotifyChanged.sendEmptyMessage(0);			CloudAccountFragment.refresh_list_handler.sendEmptyMessage(0);		} catch (Exception e) {		}		try {			MainFragment.ListViewResizingHandler.sendEmptyMessage(0);		} catch (Exception e) {		}	}	@Override	protected void onMessage(Context context, Intent GCMintent) {		if (GCMintent.getAction().equals(				"com.google.android.c2dm.intent.RECEIVE")) {			String myUUID = ServerUtilities.getUUID(context);			SharedPreferences prefs = context.getSharedPreferences("forest",					Context.MODE_PRIVATE);			String message = GCMintent.getExtras().getString("message");			if (message.length() != 0) {				Log.e("url", message);				String split[] = message.split("\\|");				if (split[split.length - 3].equals(myUUID))					return;				String forest_name = split[split.length - 1];				int notify_no = Integer.valueOf(split[split.length - 2]);				if (notify_no > prefs.getInt(forest_name + "_no", -1)) {					SharedPreferences.Editor editor = prefs.edit();					editor.putInt(forest_name + "_no", notify_no);					editor.commit();					try {						ServerUtilities.connect(								"http://alarmemi.appspot.com/alarmemi/notify/read?owner_name="										+ URLEncoder.encode(forest_name,												"UTF-8")										+ "&owner_password="										+ prefs.getString(forest_name												+ "_password", "") + "&no="										+ notify_no, context);					} catch (UnsupportedEncodingException e) {					}					on_Message(context, GCMintent, myUUID);				}			}		}	}	static void notify(Context context, String message,			Intent notificationIntent) {		int icon = R.drawable.ic_launcher_alarmclock;		long when = System.currentTimeMillis();		NotificationManager notificationManager = (NotificationManager) context				.getSystemService(Context.NOTIFICATION_SERVICE);		Notification notification = new Notification(icon, message, when);		String title = context.getString(R.string.app_name);		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK				| Intent.FLAG_ACTIVITY_CLEAR_TOP);		int random_id = (int) (Math.random() * 2147483647);		PendingIntent intent = PendingIntent.getActivity(context, random_id,				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);		notification.setLatestEventInfo(context, title, message, intent);		notification.flags |= Notification.FLAG_AUTO_CANCEL;		notification.defaults |= Notification.DEFAULT_SOUND;		notification.defaults |= Notification.DEFAULT_VIBRATE;		notificationManager.notify(random_id, notification);	}	@Override	protected void onError(Context arg0, String errorId) {		Log.i(TAG, "Received error: " + errorId);	}	@Override	protected boolean onRecoverableError(Context context, String errorId) {		return super.onRecoverableError(context, errorId);	}}