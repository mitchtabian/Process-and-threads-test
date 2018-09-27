package com.codingwithmitch.applicationone;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.codingwithmitch.applicationone.models.AudioTrack;

import java.io.IOException;
import java.util.List;

public class StreamingService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener
{

    private static final String TAG = "StreamingService";

    private Messenger mMessenger = new Messenger(new StreamingHandler());
    private MediaPlayer mMediaPlayer = null;
    private int mSeekTo = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e(TAG, "onError: Something went wrong.");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared: called.");
        mMediaPlayer.start();
    }

    private void resetMediaPlayer(){
        mMediaPlayer.pause();
        mSeekTo = 0;
        mMediaPlayer.seekTo(0);
    }

    @SuppressLint("HandlerLeak")
    class StreamingHandler extends Handler {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: received message from client.");

            AudioTrack audioTrack = msg.getData().getParcelable("audio_track");

            switch (msg.what){
                case Constants.MSG_PLAY:{
                    Log.d(TAG, "handleMessage: message = PLAY.");
                    playAudioTrack(audioTrack);
                    break;
                }
                case Constants.MSG_RESUME:{
                    if(mSeekTo > -1){
                        mMediaPlayer.seekTo(mSeekTo);
                        mMediaPlayer.start();
                        mSeekTo = -1;
                    }
                    else{
                        if(!mMediaPlayer.isPlaying()){
                            mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition());
                            mMediaPlayer.start();
                        }
                    }
                    break;
                }
                case Constants.MSG_PAUSE:{
                    Log.d(TAG, "handleMessage: message = PAUSE.");
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();
                    }
                    break;
                }

                case Constants.MSG_PROGRESS:{
                    Log.d(TAG, "handleMessage: message = PROGRESS.");
                    Message message = Message.obtain(null, Constants.MSG_PROGRESS);
                    message.arg1 = mMediaPlayer.getCurrentPosition();
                    message.arg2 = mMediaPlayer.getDuration();
                    try {
                        msg.replyTo.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    if(mMediaPlayer.getCurrentPosition() + (mMediaPlayer.getDuration() * 0.001)  >= mMediaPlayer.getDuration()
                            && !mMediaPlayer.isPlaying()){

                        new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                Intent playbackCompleteIntent = new Intent("action.playback.state.change");
                                sendBroadcast(playbackCompleteIntent);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                resetMediaPlayer();
                                super.onPostExecute(aVoid);
                            }
                        }.execute();
                    }
                    break;
                }

                case Constants.MSG_SEEK:{
                    Log.d(TAG, "handleMessage: message = SEEK.");
                    mSeekTo = msg.arg1;
                    mMediaPlayer.seekTo(mSeekTo);

                    break;
                }
            }
        }
    }

    private void playAudioTrack(AudioTrack audioTrack){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
        }
        mMediaPlayer.reset();

        try {
            mMediaPlayer.setDataSource(audioTrack.getUrl());
            mMediaPlayer.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("A service is running in the background")
                    .setContentText("").build();

            startForeground(1, notification);
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }
}










