package com.provision.alarmemi.paper.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.ViewTreeObserver;

import com.slidingmenu.lib.SlidingMenu;

/**
 *  FragmentChangeActivity에 붙은 Fragment들의 베이스이다.
 */
public class BaseFragment extends Fragment implements
        ViewTreeObserver.OnGlobalLayoutListener,
        FragmentChangeActivity.OnLifeCycleChangeListener {

    protected static FragmentChangeActivity mActivity;
    protected static SlidingMenu menu;

    @Override public void onGlobalLayout() { }
    @Override public void onWindowFocusChanged(boolean hasFocus) { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentChangeActivity) activity;
        menu = mActivity.getSlidingMenu();
    }

    @Override
    public void onBackPressed() {
        // 기본 액션은 메인화면으로 돌아간다.
        mActivity.switchContent(new MainFragment());
    }
}
