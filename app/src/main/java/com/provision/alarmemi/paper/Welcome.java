package com.provision.alarmemi.paper;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import com.provision.alarmemi.ShowcaseView.OnClosedListener;

public class Welcome {

	public static final int ADD_ALARM_ID = 0;
	public static final int MORE_ALARM_ID = 1;
	public static final int ADD_FOREST_ID = 2;
	public static final int FIRST_SCREEN_ID = 3;

	public static class WelcomeContent {
		int id;
		String title, detailText;
		View view;
		boolean firstScreen;

		WelcomeContent(final int id, final String title,
				final String detailText, final View view,
				final boolean firstScreen) {
			this.id = id;
			this.title = title;
			this.detailText = detailText;
			this.view = view;
			this.firstScreen = firstScreen;
		}
	}

	private static ShowcaseView c = null;
	private static WelcomeContent nowContent;
	private static boolean closeAll = false;

	public static ArrayList<WelcomeContent> welcomeContent = new ArrayList<WelcomeContent>();

	public static void showWelcomeContents(final Activity activity) {
		if (welcomeContent.size() == 0 || closeAll) {
			closeAll = false;
			return;
		}

		nowContent = welcomeContent.get(0);
		if (FragmentChangeActivity.welcomePrefs.contains("|" + nowContent.id
				+ "|")) {
			welcomeContent.remove(0);
			showWelcomeContents(activity);
			return;
		}

		FragmentChangeActivity.welcomePrefs += "|" + nowContent.id + "|";
		SharedPreferences.Editor editor = FragmentChangeActivity.prefs.edit();
		editor.putString("welcome", FragmentChangeActivity.welcomePrefs);
		editor.commit();

		c = FragmentChangeActivity.showShowcaseView(nowContent.view,
				nowContent.title, nowContent.detailText,
				nowContent.id == MORE_ALARM_ID, nowContent.firstScreen);
		c.setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				welcomeContent.remove(0);
				showWelcomeContents(activity);
			}
		});
	}

	public static void hideWelcomeContent(boolean isAll) {
		if (c != null && c.isShown()) {
			closeAll = isAll;
			c.hideShowcase();
		}
	}

}
