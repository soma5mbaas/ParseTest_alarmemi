package com.provision.alarmemi.paper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.slidingmenu.lib.SlidingMenu.OnPageDraggingListener;

public class FragmentChangeActivity extends BaseActivity implements
		SettingsPreferenceFragment.OnPreferenceAttachedListener,
		SetAlarmPreferenceFragment.OnPreferenceAttachedListener {

	Intent setAlarmGetIntent;

	public interface OnLifeCycleChangeListener {
		public void onWindowFocusChanged(boolean hasFocus);

		public void onBackPressed();
	}

	private OnLifeCycleChangeListener mLifeCycleChangeListener;

	public void setOnLifeCycleChangeListener(OnLifeCycleChangeListener listener) {
		mLifeCycleChangeListener = listener;
	}

	private Fragment mContent;
	private static FrameLayout title_back;
	static Context context;

	static ImageView moreAlarm;
	static SharedPreferences prefs;
	static String welcomePrefs;
	float maxScrolledOffset, tempOffset = 0;
	public static CustomProgressDialog progressDialog;

	public static int GetNotiCnt() {
		String prefs_notify = prefs.getString("notify", "");
		if (!prefs_notify.equals("")) {
			int cnt = 0;
			String[] notify_ = prefs_notify.substring(0,
					prefs_notify.length() - 2).split("\\|\\|");
			for (int i = 0; i < notify_.length; i++) {
				if (notify_[i].endsWith("|no"))
					cnt++;
			}
			return cnt;
		} else {
			return 0;
		}

	}

	static int notify_img_scale = 0;

	static int getPx(float px) {
		float r_px = px * notify_img_scale / 64;
		return (int) r_px;
	}

	public static Bitmap getAddedCounts(Bitmap bitmap, int counts) {
		notify_img_scale = bitmap.getWidth();
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Bitmap count_bitmap = BitmapFactory.decodeResource(
				context.getResources(), R.drawable.count);
		Canvas canvas = new Canvas(output);
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final Rect rect2 = new Rect(0, 0, count_bitmap.getWidth(),
				count_bitmap.getHeight());
		final Rect rect3 = new Rect(getPx(34), getPx(34), bitmap.getWidth(),
				bitmap.getHeight());

		canvas.drawBitmap(bitmap, rect, rect, paint);
		canvas.drawBitmap(count_bitmap, rect2, rect3, paint);
		paint.setColor(Color.WHITE);
		if (counts < 10) {
			paint.setTextSize(getPx(28));
			canvas.drawText(counts + "", getPx(41), getPx(58), paint);
		} else {
			paint.setTextSize(getPx(22));
			canvas.drawText(counts + "", getPx(37), getPx(57), paint);
		}

		return output;
	}

	static Handler OnNotifyArrived = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int noti_cnt = GetNotiCnt();
			if (noti_cnt > 0)
				moreAlarm.setImageBitmap(getAddedCounts(BitmapFactory
						.decodeResource(context.getResources(),
								R.drawable.ic_more), noti_cnt));
			else
				moreAlarm.setImageBitmap(BitmapFactory.decodeResource(
						context.getResources(), R.drawable.ic_more));
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		progressDialog = new CustomProgressDialog(context);
		prefs = getSharedPreferences("forest", Context.MODE_PRIVATE);
		welcomePrefs = prefs.getString("welcome", "");
		maxScrolledOffset = prefs.getFloat("maxOffset", 1f);

		if (savedInstanceState != null)
			mContent = getSupportFragmentManager().getFragment(
					savedInstanceState, "mContent");
		if (mContent == null)
			mContent = new MainFragment(this, getSlidingMenu());

		// set the Above View
		setContentView(R.layout.content_frame);
		title_back = (FrameLayout) findViewById(R.id.title_back);
		title_back.setBackgroundColor(Color.rgb(0, 0, 0));
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, mContent)
				.commitAllowingStateLoss();

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new SlideMenu(this))
				.commitAllowingStateLoss();

		getSlidingMenu().setOnPageDraggingListener(
				new OnPageDraggingListener() {
					@Override
					public void onDrag(float positionOffset) {
						if (tempOffset < positionOffset)
							tempOffset = positionOffset;
						int c = (int) ((positionOffset / maxScrolledOffset) * 174);
						title_back.setBackgroundColor(Color.rgb(c, c, c));
					}
				});
		getSlidingMenu().setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				if (maxScrolledOffset == 1f) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putFloat("maxOffset", tempOffset);
					editor.commit();
					maxScrolledOffset = tempOffset;
				}
				title_back.setBackgroundColor(Color.rgb(174, 174, 174));
			}
		});
		getSlidingMenu().setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				title_back.setBackgroundColor(Color.rgb(0, 0, 0));
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}

	public void switchContent(Fragment fragment) {
		Welcome.hideWelcomeContent(true);
		mLifeCycleChangeListener = null;
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment)
				.commitAllowingStateLoss();
		getSlidingMenu().showContent();
	}

	@Override
	public void onPreferenceAttached(PreferenceScreen root, int xmlId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			try {
				OnNotifyArrived.sendEmptyMessage(0);
			} catch (Exception e) {
			}
		}
		if (mLifeCycleChangeListener != null)
			mLifeCycleChangeListener.onWindowFocusChanged(hasFocus);
	}

	@Override
	public void onBackPressed() {
		if (mLifeCycleChangeListener != null)
			mLifeCycleChangeListener.onBackPressed();
	}

	public static ShowcaseView showShowcaseView(View view, String title,
			String content, boolean mode, boolean firstScreen) {
		return ShowcaseView.showShowcase(context, title_back, view, title,
				content, mode, firstScreen);
	}

}
