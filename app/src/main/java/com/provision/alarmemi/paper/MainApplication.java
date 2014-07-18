package com.provision.alarmemi.paper;

import android.app.Application;

import com.parse.Parse;

/**
 *  메인 애플리케이션 클래스
 */
public class MainApplication extends Application {

    private static String APP_ID = "5n2TS8TrJdVGKgA7joyjbzqcmN3IHfU2tIZ2mrlY";
    private static String CLIENT_KEY = "pnkxVYIRzX9dDUFay3vgB9mtT2QqzSoiGUr6ZjVH";

    @Override
    public void onCreate() {
        Parse.initialize(this, APP_ID, CLIENT_KEY);
    }
}
