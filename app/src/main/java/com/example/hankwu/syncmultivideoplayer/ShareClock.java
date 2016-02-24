package com.example.hankwu.syncmultivideoplayer;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

/**
 * Created by HankWu on 16/2/19.
 */
public class ShareClock {
    static ShareClock shareClock = new ShareClock();
    private MediaCodec[] mcs = new MediaCodec[4];
    private int[] index = new int[4];
    private Semaphore[] mutexs = new Semaphore[4];
    public boolean bGo = false;
    public boolean bCanUpdate = false;


    HandlerThread handlerThread = null;
    Handler displayHandler = null;

    long startTime = -1;
    public void setStartTime(long s) {
        startTime = s;
    }

    public long getStartTime() {
        return startTime;
    }


    public void start() {
        handlerThread = new HandlerThread("Display");
        handlerThread.start();
        displayHandler = new Handler(handlerThread.getLooper());
        for(int i=0;i<4;i++) {
            mutexs[i] = new Semaphore(1);
        }
//        displayHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                bGo = false;
//                bCanUpdate = false;
//                if(MediaPlayerController.mediaPlayerControllerSingleton.DisplayIfItCan()) {
//
//                }
//                displayHandler.postDelayed(this,5);
//            }
//        },1000);
    }

    public ShareClock() {

    }

    public void setMediaCodec(int i,MediaCodec c,int ii) {
        //try {
            //mutexs[i].acquire();
            Log.d("HANK", "index:" + i);
            mcs[i] = c;
            index[i] = ii;
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
    }








}
