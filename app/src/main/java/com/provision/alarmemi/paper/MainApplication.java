package com.provision.alarmemi.paper;

import android.app.Application;

import com.parse.Parse;
import com.provision.alarmemi.paper.utils.CommonUtilities;

/**
 *  메인 애플리케이션 클래스
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        Parse.initialize(this, CommonUtilities.PARSE_APP_ID, CommonUtilities.PARSE_CLIENT_KEY);
    }
}
