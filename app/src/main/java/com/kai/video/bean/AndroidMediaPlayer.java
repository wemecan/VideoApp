/*
 * Copyright (C) 2006 Bilibili
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kai.video.bean;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaMeta;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

public class AndroidMediaPlayer extends AbstractMediaPlayer {
    private final KSYMediaPlayer mInternalMediaPlayer;
    private final AndroidMediaPlayerListenerHolder mInternalListenerAdapter;
    private String mDataSource;
    private MediaDataSource mMediaDataSource;

    private final Object mInitLock = new Object();
    private  boolean mIsReleased;

    private static MediaInfo sMediaInfo;

    public AndroidMediaPlayer(Context context) {
        synchronized (mInitLock) {
            mInternalMediaPlayer = new KSYMediaPlayer.Builder(context).build();
        }
        mInternalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mInternalListenerAdapter = new AndroidMediaPlayerListenerHolder(this);
        attachInternalListeners();
    }

    public KSYMediaPlayer getInternalMediaPlayer() {
        return mInternalMediaPlayer;
    }
    @Override
    public void setDisplay(SurfaceHolder sh) {
        synchronized (mInitLock) {
            if (!mIsReleased) {
                mInternalMediaPlayer.setDisplay(sh);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void setSurface(Surface surface) {
        mInternalMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(context, uri, headers);
    }

    @Override
    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mInternalMediaPlayer.setDataSource(fd);
    }

    @Override
    public void setDataSource(String path) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = path;

        Uri uri = Uri.parse(path);
        String scheme = uri.getScheme();
        if (!TextUtils.isEmpty(scheme) && scheme.equalsIgnoreCase("file")) {
            mInternalMediaPlayer.setDataSource(uri.getPath());
        } else {
            mInternalMediaPlayer.setDataSource(path);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void setDataSource(IMediaDataSource mediaDataSource) {
        releaseMediaDataSource();

        mMediaDataSource = new MediaDataSourceProxy(mediaDataSource);
        try {
            mInternalMediaPlayer.setDataSource(mDataSource);
        }catch (Exception e){

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private static class MediaDataSourceProxy extends MediaDataSource {
        private final IMediaDataSource mMediaDataSource;

        public MediaDataSourceProxy(IMediaDataSource mediaDataSource) {
            mMediaDataSource = mediaDataSource;
        }

        @Override
        public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
            return mMediaDataSource.readAt(position, buffer, offset, size);
        }

        @Override
        public long getSize() throws IOException {
            return mMediaDataSource.getSize();
        }

        @Override
        public void close() throws IOException {
            mMediaDataSource.close();
        }
    }

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    private void releaseMediaDataSource() {
        if (mMediaDataSource != null) {
            try {
                mMediaDataSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaDataSource = null;
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mInternalMediaPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mInternalMediaPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mInternalMediaPlayer.stop();
    }

    @Override
    public void pause() throws IllegalStateException {
        mInternalMediaPlayer.pause();
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        mInternalMediaPlayer.setScreenOnWhilePlaying(screenOn);
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        mInternalMediaPlayer.getTrackInfo();
        return AndroidTrackInfo.fromMediaPlayer(mInternalMediaPlayer);
    }

    @Override
    public int getVideoWidth() {
        return mInternalMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mInternalMediaPlayer.getVideoHeight();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public boolean isPlaying() {
        try {
            return mInternalMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
            return false;
        }
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        mInternalMediaPlayer.seekTo((int) msec);
    }

    @Override
    public long getCurrentPosition() {
        try {
            return mInternalMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
            return 0;
        }
    }

    @Override
    public long getDuration() {
        try {
            return mInternalMediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
            return 0;
        }
    }

    @Override
    public void release() {
        mIsReleased = true;
        mInternalMediaPlayer.reset();
        mInternalMediaPlayer.release();
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    @Override
    public void reset() {
        try {
            mInternalMediaPlayer.reset();
        } catch (IllegalStateException e) {
            DebugLog.printStackTrace(e);
        }
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    @Override
    public void setLooping(boolean looping) {
        mInternalMediaPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return mInternalMediaPlayer.isLooping();
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mInternalMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public int getAudioSessionId() {
        return mInternalMediaPlayer.getAudioSessionId();
    }

    @Override
    public MediaInfo getMediaInfo() {
        if (sMediaInfo == null) {
            sMediaInfo = new MediaInfo();
            com.ksyun.media.player.MediaInfo info = mInternalMediaPlayer.getMediaInfo();
            sMediaInfo.mMeta = new IjkMediaMeta();
            sMediaInfo.mMeta.mFormat = info.mMeta.mFormat;
            sMediaInfo.mMeta.mBitrate = info.mMeta.mBitrate;
            sMediaInfo.mMeta.mDurationUS = info.mMeta.mDurationUS;
            sMediaInfo.mMeta.mMediaMeta = info.mMeta.mMediaMeta;
            sMediaInfo.mAudioDecoder = info.mAudioDecoder;
            sMediaInfo.mAudioDecoderImpl = info.mAudioDecoderImpl;
            sMediaInfo.mMediaPlayerName = info.mMediaPlayerName;
            sMediaInfo.mVideoDecoder = info.mVideoDecoder;
            sMediaInfo.mVideoDecoderImpl = info.mVideoDecoderImpl;
        }

        return sMediaInfo;
    }

    @Override
    public void setLogEnabled(boolean enable) {
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    /*--------------------
     * misc
     */
    @Override
    public void setWakeMode(Context context, int mode) {
        mInternalMediaPlayer.setWakeMode(context, mode);
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        mInternalMediaPlayer.setAudioStreamType(streamtype);
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
    }

    /*--------------------
     * Listeners adapter
     */
    private void attachInternalListeners() {
        mInternalMediaPlayer.setOnPreparedListener(mInternalListenerAdapter);
        mInternalMediaPlayer
                .setOnBufferingUpdateListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnCompletionListener(mInternalListenerAdapter);
        mInternalMediaPlayer
                .setOnSeekCompleteListener(mInternalListenerAdapter);
        mInternalMediaPlayer
                .setOnVideoSizeChangedListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnErrorListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnInfoListener(mInternalListenerAdapter);
        mInternalMediaPlayer.setOnTimedTextListener(mInternalListenerAdapter);
    }

    private class AndroidMediaPlayerListenerHolder implements
            KSYMediaPlayer.OnPreparedListener, KSYMediaPlayer.OnCompletionListener,
            KSYMediaPlayer.OnBufferingUpdateListener,
            KSYMediaPlayer.OnSeekCompleteListener,
            KSYMediaPlayer.OnVideoSizeChangedListener,
            KSYMediaPlayer.OnErrorListener, KSYMediaPlayer.OnInfoListener,
            KSYMediaPlayer.OnTimedTextListener {
        public final WeakReference<AndroidMediaPlayer> mWeakMediaPlayer;

        public AndroidMediaPlayerListenerHolder(AndroidMediaPlayer mp) {
            mWeakMediaPlayer = new WeakReference<AndroidMediaPlayer>(mp);
        }
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            return self != null && notifyOnInfo(what, extra);

        }

        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            return self != null && notifyOnError(what, extra);

        }

        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnVideoSizeChanged(width, height, sar_num, sar_den);
        }

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnSeekComplete();
        }

        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnBufferingUpdate(percent);
        }

        @Override
        public void onCompletion(IMediaPlayer mp) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnCompletion();
        }

        @Override
        public void onPrepared(IMediaPlayer mp) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            notifyOnPrepared();
        }

        @Override
        public void onTimedText(IMediaPlayer mp, String text) {
            AndroidMediaPlayer self = mWeakMediaPlayer.get();
            if (self == null)
                return;

            IjkTimedText ijkText = null;

            if (text != null) {
                //ijkText = new IjkTimedText(text.length(), text);
            }

            notifyOnTimedText(ijkText);
        }


    }
}
