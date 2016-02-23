package com.example.hankwu.decodetoglsurface;

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
    public int frameAvailableCounter = 0;

    public void setSurfaceTextures(SurfaceTexture[] sts) {
        number_of_play = sts.length;
        //mps = new MediaPlayer[number_of_play];
        mps = new MediaCodecPlayer[number_of_play];
        stss = sts;




        for(int i=0;i<number_of_play;i++) {
            mps[i] = new MediaCodecPlayer();
            mps[i].setIndex(i);
            Surface surface = new Surface(sts[i]);
            mps[i].setSurface(surface);
            //surface.release();
        }

        for(int i=0;i<number_of_play;i++) {
            stss[i].setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    //surfaceTexture.updateTexImage();
                    frameAvailableCounter++;
                }
            });
        }

    }

    public boolean checkCanDisplay() {
        for(int i=0;i<number_of_play;i++) {
            if(!mps[i].canDisplay()) {
                return false;
            }
        }

        final long time = System.currentTimeMillis();
        for(int i=0;i<number_of_play;i++) {
            final int j = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mps[j].display(time);
                }
            }).start();
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

    public boolean isAllOnGo() {
        for(int i=0;i<number_of_play;i++) {
            if(!mps[i].onGo) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllGoDone() {
        if(frameAvailableCounter==4) {
            frameAvailableCounter = 0;
            return true;
        }
        return false;
    }


}
