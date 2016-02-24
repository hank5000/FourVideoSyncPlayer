package com.example.hankwu.syncmultivideoplayer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class MediaCodecPlayer {
    String TAG = "CodecPlayer";
    private Surface surface;
    private String sourcePath;
    MediaExtractor mediaExtractor = null;
    MediaCodec decoder = null;
    boolean bStart = false;
    boolean bPause = false;
    Semaphore mutex = new Semaphore(1);
    int timeoutUs = 1000000; // 1 second timeout
    public boolean goDone = false;

    int index = -1;

    // use for display
    Queue<Integer> displayQueue = new LinkedList<>();
    Queue<Long> displayTimeStampQueue = new LinkedList<>();
    long startTime = -1;
    long firstTime = -1;
    HandlerThread handlerThread = null;
    Handler displayHandler = null;

    // use for test only
    boolean bWeakPlatformTest = false;
    boolean bNoTimeStamp = false;

    public boolean onGo = false;


    public void setIndex(int ii) {
        index = ii;
    }


    public void setDataSource(String p) {
        sourcePath = p;
    }

    public void setSurface(Surface s) {
        surface =  s;
    }

    public void prepare() throws IOException {

        handlerThread = new HandlerThread("DisplayHandler");
        handlerThread.start();

        displayHandler = new Handler(handlerThread.getLooper());

        mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(sourcePath);

        int numTracks = mediaExtractor.getTrackCount();
        String mine_type = null;
        MediaFormat format = null;
        for (int i = 0; i < numTracks; ++i) {
            format = mediaExtractor.getTrackFormat(i);
            mine_type = format.getString(MediaFormat.KEY_MIME);
            if (mine_type.startsWith("video/")) {
                // Must select the track we are going to get data by readSampleData()
                mediaExtractor.selectTrack(i);
                // Set required key for MediaCodec in decoder mode
                // Check http://developer.android.com/reference/android/media/MediaFormat.html
//                format.setInteger(MediaFormat.KEY_CAPTURE_RATE, 24);
//                format.setInteger(MediaFormat.KEY_PUSH_BLANK_BUFFERS_ON_STOP, 1);
                break;
            }
        }

        // TODO: Check if valid track has been selected by selectTrack()

        decoder = MediaCodec.createDecoderByType(mine_type);
        decoder.configure(format, surface, null, 0 /* 0:decoder 1:encoder */);

        decoder.setCallback(new DecoderCallback());
    }

    public void display(long currentTime) {

        if(firstTime==-1) {
            firstTime = displayTimeStampQueue.peek();
            ShareClock.shareClock.setStartTime(currentTime);
            decoder.releaseOutputBuffer(displayQueue.poll(), displayTimeStampQueue.poll());
        } else {
            long time = displayTimeStampQueue.poll();
            decoder.releaseOutputBuffer(displayQueue.poll(), time);
        }

    }

    public boolean canDisplay(long currentTime) {
        if(firstTime==-1) {
            return !displayQueue.isEmpty();
        }

        if(!displayQueue.isEmpty()) {
            long delta_display_time = displayTimeStampQueue.peek() - firstTime;
            long delta_startTime = currentTime - ShareClock.shareClock.getStartTime();
            long delayTime = delta_display_time - delta_startTime;

            if(delayTime>0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    public class DecoderCallback extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(MediaCodec mediaCodec, int i) {
            if(bPause) {
                try {
                    mutex.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            ByteBuffer inputBuffer = decoder.getInputBuffer(i);
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize > 0) {
                //frameDisplayTime = (mediaExtractor.getSampleTime() >> 10) + playStartTime;
                // Video data is valid,send input buffer to MediaCodec for decode
                decoder.queueInputBuffer(i, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                mediaExtractor.advance();
            } else {
                Log.d("HANK","EOS");
                // End-Of-Stream (EOS)
                // decoder.queueInputBuffer(i, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
                //ShareClock.shareClock.setStartTime(System.currentTimeMillis());
                firstTime = -1;
            }
        }

        @Override
        public void onOutputBufferAvailable(final MediaCodec mediaCodec,final int i, MediaCodec.BufferInfo bufferInfo) {
            displayQueue.add(i);
            displayTimeStampQueue.add(bufferInfo.presentationTimeUs/1000);
        }

        @Override
        public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
            Log.e(TAG, "something wrong on MediaCodecPlayer");
            e.printStackTrace();
        }

        @Override
        public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {

        }
    }

    public void start() {
        if(bStart && bPause==true) {
            bPause = false;
            mutex.release();
        } else {
            // using mediacodec async mode (Android 5.0+)
            bPause = false;
            decoder.start();
        }
    }

    public void stop() {
        bStart = false;
    }

    public void pause() {
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        bPause = true;
    }

}