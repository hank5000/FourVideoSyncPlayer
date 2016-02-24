package com.example.hankwu.syncmultivideoplayer;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by HankWu on 16/1/9.
 */
public class MediaPlayerController {
    public static MediaPlayerController mediaPlayerControllerSingleton = new MediaPlayerController();
    private MediaCodecPlayer[] mps = null;
    private int number_of_play = 0;
    public  SurfaceTexture[] stss = null;

    public void setSurfaceTextures(SurfaceTexture[] sts) {
        number_of_play = sts.length;
        mps = new MediaCodecPlayer[number_of_play];

        for(int i=0;i<number_of_play;i++) {
            mps[i] = new MediaCodecPlayer();
            mps[i].setIndex(i);
            Surface surface = new Surface(sts[i]);
            mps[i].setSurface(surface);
        }

    }

    public boolean DisplayIfItCan() {

        final long time = System.currentTimeMillis();

        for(int i=0;i<number_of_play;i++) {
            if(!mps[i].canDisplay(time)) {
                return false;
            }
        }

        for(int i=0;i<number_of_play;i++) {
            final int j = i;
            mps[j].display(time);
        }
        return true;
    }

    public void setDataSources(String[] ss) throws IOException {
        for(int i=0;i<number_of_play;i++) {
            mps[i].setDataSource(ss[i]);
        }
    }

    public void prepare() throws IOException {
        for(int i=0;i<number_of_play;i++) {
            mps[i].prepare();
        }
    }

    public long startTime = 0;
    public void start() {
        startTime = System.currentTimeMillis();
        ShareClock.shareClock.setStartTime(startTime);
        for(int i=0;i<number_of_play;i++) {
            mps[i].start();
        }
    }

    public void stop() {
        for(int i=0;i<number_of_play;i++) {
            mps[i].stop();
        }
    }

    public void pause() {
        for(int i=0;i<number_of_play;i++) {
            mps[i].pause();
        }
    }

    public void seekToZero() {
        for(int i=0;i<number_of_play;i++) {
            //mps[i].seekTo(0);
        }
    }



}
