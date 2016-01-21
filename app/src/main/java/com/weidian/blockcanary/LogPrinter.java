/*
 * **
 *   @(#)LogPrinter.java 2016-01-19
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

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;


/**
 * Created by fengcunhan on 16/1/19.
 */
public class LogPrinter implements Printer {
    public static final String ACTION_BLOCK="com.weidian.blockcannary";
    public static final String EXTRA_START_TIME="block_happen_time";
    public static final String EXTRA_FINISH_TIME="block_finish_time";

    private static final String TAG="LogPrinter";
    private Context mContext;
    private static final String START_TAG=">>>>> Dispatching to";
    private static final String FINISH_TAG="<<<<< Finished to";
    private static final int START=0;
    private static final int FINISH=1;
    private static final int UNKONW=2;
    private boolean mStartedPrinting=false;
    private long mStartTimeMillis;
    private long mStartThreadTimeMillis;
    private long mBlockThresholdMillis=500;
    private StackInfoCatcher mStackInfoCatcher;
    public LogPrinter(Context context){
        this.mContext=context;
        mStackInfoCatcher=new StackInfoCatcher(context);
        mStackInfoCatcher.start();
    }

    @Override
    public void println(String x) {
        switch (isStart(x)){
            case START:
                mStartTimeMillis = System.currentTimeMillis();
                mStartThreadTimeMillis = SystemClock.currentThreadTimeMillis();
                mStartedPrinting = true;
                break;
            case FINISH:
                final long endTime = System.currentTimeMillis();
                mStartedPrinting = false;
                if (isBlock(endTime)) {
                    notifyBlockEvent(endTime,mStartTimeMillis);
                }
                break;
            default:

        }
    }

    private int isStart(String x){
        //Log.e(TAG,x+"");
        if(!TextUtils.isEmpty(x)){
            if(x.startsWith(START_TAG)){
                return START;
            }else if(x.startsWith(FINISH_TAG)){
                return FINISH;
            }
        }
        return UNKONW;
    }


    private boolean isBlock(long endTime) {
        return endTime - mStartTimeMillis > mBlockThresholdMillis;
    }

    private void notifyBlockEvent(long endTime,long startTime){
        Log.e(TAG,"block time:"+(endTime-startTime));

        LocalBroadcastManager manager= LocalBroadcastManager.getInstance(mContext);
        Intent intent=new Intent(ACTION_BLOCK);
        intent.putExtra(EXTRA_START_TIME,startTime);
        intent.putExtra(EXTRA_FINISH_TIME,endTime);
        manager.sendBroadcast(intent);
    }

    public void stop(){
        if(null!=mStackInfoCatcher){
            mStackInfoCatcher.stopTrace();
        }
    }
}
