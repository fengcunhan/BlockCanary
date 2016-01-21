package com.weidian.blockcanary;

import android.app.Application;
import android.os.Looper;

/**
 * Created by fengcunhan on 16/1/21.
 */
public class DemoApplication extends Application{
    private LogPrinter mLogPrinter;
    @Override
    public void onCreate() {
        super.onCreate();
        mLogPrinter=new LogPrinter(this);
        Looper.getMainLooper().setMessageLogging(mLogPrinter);
    }
}
