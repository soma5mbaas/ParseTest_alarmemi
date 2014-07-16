package com.provision.alarmemi.paper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class BaseActivity extends SlidingFragmentActivity {

	protected Fragment mFrag;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set the Behind View
		setBehindContentView(R.layout.menu_frame);
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		mFrag = new SlideMenu(this);
		t.replace(R.id.menu_frame, mFrag);
		t.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.RIGHT);
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.slidingmenu_shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		/*
		 * menu.setOnPageDraggingListener(new OnPageDraggingListener() {
			@Override
			public void onDrag(float positionOffset) {
				int c = (int) (positionOffset * 236);
				title_back.setBackgroundColor(Color.rgb(c, c, c));
			}
		});
		menu.setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				title_back.setBackgroundColor(Color.rgb(236, 236, 236));
			}
		});
		menu.setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				title_back.setBackgroundColor(Color.rgb(0, 0, 0));
			}
		});
		 */

		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
