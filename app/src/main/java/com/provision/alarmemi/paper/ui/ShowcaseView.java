package com.provision.alarmemi.paper.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.provision.alarmemi.paper.R;
import com.provision.alarmemi.paper.fragments.FragmentChangeActivity;

public class ShowcaseView extends LinearLayout implements
		OnGlobalLayoutListener, View.OnTouchListener {

	public interface OnClosedListener {
		void onClosed();
	}

	static OnClosedListener onClosedListener = null;

	public void setOnClosedListener(OnClosedListener listener) {
		onClosedListener = listener;
	}

	static Context context;
	static float clingX, clingY, metricScale, showcaseRadius;
	static String title, content;
	static int top, screen_height;
	static boolean isFirst = false, firstScreen = true;

	public ShowcaseView(Context context) {
		super(context);
		this.context = context;
		metricScale = getContext().getResources().getDisplayMetrics().density;
		showcaseRadius = metricScale * 94;

		Rect rc = new Rect();
		((Activity) context).getWindow().getDecorView()
				.getWindowVisibleDisplayFrame(rc);
		top = rc.top;
		screen_height = rc.bottom - rc.top;

		setOnTouchListener(this);
	}

	static LinearLayout blank_top;
	static TextView title_tv, content_tv;
	static Button ok_btn;
	static ShowcaseView sv;
	static int showcaseHeight;
	public static boolean opened = false;
	static ViewGroup parent_;

	public static ShowcaseView showShowcase(Context context, ViewGroup parent,
			View view, String title, String content, boolean mode,
			boolean firstScreen_) {
		firstScreen = firstScreen_;
		int[] coord = new int[2];
		((FragmentChangeActivity) context).getSlidingMenu().getContent()
				.getLocationInWindow(coord);

		opened = true;
		showcaseHeight = parent.getHeight();
		parent_ = parent;

		sv = new ShowcaseView(context);
		parent.addView(sv);
		if (firstScreen) {
			clingX = 0;
			clingY = showcaseRadius;
		} else {
			if (mode) {
				clingX = (float) (view.getLeft() + view.getWidth() / 2);
				clingY = (float) (view.getTop() + view.getHeight() / 2) + top;
			} else {
				int[] coordinates = new int[2];
				view.getLocationInWindow(coordinates);
				clingX = (float) (coordinates[0] + view.getWidth() / 2)
						- coord[0];
				clingY = (float) (coordinates[1] + view.getHeight() / 2);
			}
		}
		if (firstScreen)
			sv.setBackgroundColor(Color.parseColor("#BB000000"));
		else
			sv.setBackgroundDrawable(new BitmapDrawable(makeBackground(parent)));
		sv.setOrientation(LinearLayout.VERTICAL);
		sv.setPadding((int) (24 * metricScale), 0, (int) (24 * metricScale),
				(int) (24 * metricScale));
		sv.setGravity(Gravity.RIGHT);

		blank_top = new LinearLayout(context);
		sv.addView(blank_top);

		title_tv = new TextView(context);
		title_tv.setTextColor(Color.parseColor("#5596BE"));
		title_tv.setTextSize(30);
		title_tv.setShadowLayer(2.0f, 0f, 2.0f, Color.BLACK);
		title_tv.setText(title);
		sv.addView(title_tv);

		content_tv = new TextView(context);
		content_tv.setTextColor(Color.WHITE);
		content_tv.setShadowLayer(2.0f, 0f, 2.0f, Color.BLACK);
		content_tv.setTextSize(20);
		content_tv.setText(content);
		sv.addView(content_tv);

		LinearLayout blank_bottom = new LinearLayout(context);
		sv.addView(blank_bottom);
		LayoutParams lp = new LayoutParams(
				blank_bottom.getLayoutParams());
		lp.width = LayoutParams.MATCH_PARENT;
		lp.height = LayoutParams.MATCH_PARENT;
		lp.weight = 1f;
		blank_bottom.setLayoutParams(lp);

		ok_btn = new Button(context);
		ok_btn.setText(firstScreen ? "Next" : "OK");
		ok_btn.setBackgroundResource(R.drawable.cling_button_bg);
		ok_btn.setTextSize(20);
		ok_btn.setTextColor(Color.WHITE);
		ok_btn.setTypeface(null, Typeface.BOLD);
		ok_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hideShowcase();
			}
		});
		sv.addView(ok_btn);
		ViewGroup.LayoutParams blp = ok_btn.getLayoutParams();
		blp.width = DP2PX(80);
		blp.height = DP2PX(50);
		ok_btn.setLayoutParams(blp);

		isFirst = true;
		ViewTreeObserver vto = sv.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(sv);

		AlphaAnimation anim = new AlphaAnimation(0f, 1f);
		anim.setDuration(500);
		sv.startAnimation(anim);

		return sv;
	}

	public static void hideShowcase() {
		if (!opened || sv == null)
			return;
		opened = false;
		AlphaAnimation anim = new AlphaAnimation(1f, 0f);
		anim.setDuration(500);
		anim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				parent_.removeView(sv);
				if (onClosedListener != null)
					onClosedListener.onClosed();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
			}
		});
		sv.startAnimation(anim);
	}

	private static Bitmap makeBackground(ViewGroup parent) {

		Bitmap b = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint paint = new Paint();

		c.drawColor(Color.parseColor("#BB000000"));
		Bitmap cling = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.cling);
		c.drawBitmap(cling, clingX - cling.getWidth() / 2,
				clingY - top - cling.getHeight() / 2, paint);

		paint.setColor(0xFFFFFF);
		paint.setAlpha(0);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
		c.drawCircle(clingX, clingY - top, showcaseRadius, paint);

		return b;
	}

	static int DP2PX(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				context.getResources().getDisplayMetrics());
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (firstScreen)
			return true;
		float xDelta = Math.abs(motionEvent.getRawX() - clingX);
		float yDelta = Math.abs(motionEvent.getRawY() - clingY);
		double distanceFromFocus = Math.sqrt(Math.pow(xDelta, 2)
				+ Math.pow(yDelta, 2));
		return distanceFromFocus > showcaseRadius;

	}

	@Override
	public void onGlobalLayout() {
		if (!isFirst)
			return;
		isFirst = false;
		LayoutParams lp = (LayoutParams) blank_top
				.getLayoutParams();
		lp.width = LayoutParams.MATCH_PARENT;
		if ((int) (clingY - top + showcaseRadius) + title_tv.getHeight()
				+ content_tv.getHeight() + ok_btn.getHeight()
				+ (int) (24 * metricScale) > screen_height) {
			lp.height = (int) (clingY - top - showcaseRadius
					- content_tv.getHeight() - title_tv.getHeight());
		} else {
			lp.height = (int) (clingY - top + showcaseRadius);
		}
		blank_top.setLayoutParams(lp);
	}
}
