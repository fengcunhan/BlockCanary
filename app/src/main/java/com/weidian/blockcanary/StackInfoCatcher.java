/*
 * **
 *   @(#)StackInfoCatcher.java 2016-01-19
 *
 *  Copyright 2000-2016 by Koudai Corporation.
 *
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Koudai Corporation ("Confidential Information"). You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Koudai.
 *
 * *
 */

package com.weidian.blockcanary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengcunhan on 16/1/19.
 */
public class StackInfoCatcher extends Thread {
    private static final String TAG="StackInfoCatcher";

    private static final int SIZE=1024;
    private boolean stop=false;
    private long mLastTime=0;
    private List<StackTraceInfo> mList=new ArrayList<>(SIZE);
    private Context mContext;
    private BroadcastReceiver mBroadcastReceiver;
    public StackInfoCatcher(Context context){
        this.mContext=context;
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long endTime = intent.getLongExtra(LogPrinter.EXTRA_FINISH_TIME, 0);
                long startTime = intent.getLongExtra(LogPrinter.EXTRA_START_TIME, 0);
                StackTraceInfo info = getInfoByTime(endTime, startTime);
                if (null != info) {
                    Log.e(TAG,"find block line");
                    //info.mANRError.printStackTrace();
                    Log.e(TAG,info.mLog);
                }else{
                    Log.e(TAG,"no block line find");
                }
            }
        };
        manager.registerReceiver(mBroadcastReceiver, new IntentFilter(LogPrinter.ACTION_BLOCK));
    }

    @Override
    public void run() {
        super.run();
        while(!stop){
            long currentTime=System.currentTimeMillis();
            if((currentTime-mLastTime)>500){
                mLastTime=System.currentTimeMillis();
                StackTraceInfo info=new StackTraceInfo();
                info.mTime=mLastTime;
                info.mLog=stackTraceToString(Looper.getMainLooper().getThread().getStackTrace());
                mList.add(info);
            }
            if(mList.size()>SIZE){
                mList.remove(0);
            }
        }
    }

    public StackTraceInfo getInfoByTime(long endTime,long startTime){
        for(StackTraceInfo info:mList){
            if(info.mTime>=startTime && info.mTime<=endTime){
                return info;
            }
        }
        return null;
    }

    public String stackTraceToString(StackTraceElement[] elements){
        StringBuilder result=new StringBuilder();
        if(null!=elements && elements.length>0){
            for (int i = 0; i < elements.length ; i++) {
                result.append("\tat ");
                result.append(elements[i].toString());
                result.append("\n");
            }

        }
        return result.toString();
    }


    public void stopTrace(){
        stop=true;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadcastReceiver);
    }
}
