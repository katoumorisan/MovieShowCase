package com.yzl.movieshowcase;

import android.app.Application;

import com.yzl.movieshowcase.utils.CrashHandler;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化全局捕获异常handler
        CrashHandler.getInstance().init(this);
    }
}
