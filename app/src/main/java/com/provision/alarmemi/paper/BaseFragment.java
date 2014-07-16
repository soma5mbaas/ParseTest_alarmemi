package com.provision.alarmemi.paper;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 *  FragmentChangeActivity에 붙은 Fragment들의 베이스이다.
 */
public class BaseFragment extends Fragment implements
        ViewTreeObserver.OnGlobalLayoutListener,
        FragmentChangeActivity.OnLifeCycleChangeListener {

    protected FragmentChangeActivity mActivity;

    @Override public void onGlobalLayout() { }
    @Override public void onWindowFocusChanged(boolean hasFocus) { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentChangeActivity) activity;
    }

    @Override
    public void onBackPressed() {
        // 기본 액션은 메인화면으로 돌아간다.
        mActivity.switchContent(new MainFragment());
    }
}
