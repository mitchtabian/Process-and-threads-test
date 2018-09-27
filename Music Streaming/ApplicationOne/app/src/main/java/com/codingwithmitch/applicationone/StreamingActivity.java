package com.codingwithmitch.applicationone;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.codingwithmitch.applicationone.adapters.AudioTrackRecyclerAdapter;
import com.codingwithmitch.applicationone.models.AudioTrack;

import java.util.ArrayList;

public class StreamingActivity extends AppCompatActivity implements
        AudioTrackRecyclerAdapter.AudioTrackClickListener,
        View.OnClickListener,
        SeekBar.OnSeekBarChangeListener
{

    private static final String TAG = "StreamingActivity";

    private static final int PLAYBACK_PAUSE = 0;
    private static final int PLAYBACK_PLAY = 1;
    private static final int PLAYBACK_APP_LAUNCH = 2;

    //ui components
    private RecyclerView mAudioTracksRecyclerView;
    private SeekBar mAudioProgressSeekbar;
    private ImageButton mPlaybackIcon;
    private TextView mAudioTrackTitle;

    //vars
    private AudioTrackRecyclerAdapter mAudioTrackRecyclerAdapter;
    private ArrayList<AudioTrack> mAudioTracks = new ArrayList<>();
    private Messenger mAudioServiceMessenger = null; // sends messages to audio service
    private Messenger mIncomingMessenger = null; // receives messages from audio service
    private boolean isAudioServiceBound = false;
    private int mPlaybackState = PLAYBACK_PAUSE;
    private AudioTrack mSelectedAudioTrack = null;
    private HandlerThread mPlaybackProgressHandlerThread = null;



    @SuppressLint("HandlerLeak")
    class IncomingMessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: received incoming message from streaming service.");

            switch (msg.what){

                case Constants.MSG_PROGRESS:{
                    Log.d(TAG, "handleMessage: message = PROGRESS.");

                    int playbackProgress = msg.arg1;
                    int maxPlaybackProgress = msg.arg2;
                    mAudioProgressSeekbar.setMax(maxPlaybackProgress);
                    mAudioProgressSeekbar.setProgress(playbackProgress);

                    break;
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        mAudioTracksRecyclerView = findViewById(R.id.audioTrackRecyclerView);
        mPlaybackIcon = findViewById(R.id.playback_button);
        mAudioTrackTitle = findViewById(R.id.audio_track_title);
        mAudioProgressSeekbar = findViewById(R.id.audio_progress_seekbar);

        mAudioProgressSeekbar.setOnSeekBarChangeListener(this);
        mPlaybackIcon.setOnClickListener(this);

        setAudioTracks();
        initRecyclerView();
        setInitialTrack();
    }

    private void setInitialTrack(){
        mAudioTrackTitle.setText(mAudioTracks.get(0).getTitle());
        mSelectedAudioTrack = mAudioTracks.get(0);
        mPlaybackState = PLAYBACK_APP_LAUNCH;
        setPlaybackIcon();
    }

    private void setAudioTracks(){
        mAudioTracks.add(new AudioTrack("Audio Test #1", Resources.AUDIO_TEST_1));
        mAudioTracks.add(new AudioTrack("Audio Test #2", Resources.AUDIO_TEST_2));
        mAudioTracks.add(new AudioTrack("CodingWithMitch Podcast#1 - Jim Wilson", Resources.AUDIO_JIM_WILSON));
        mAudioTracks.add(new AudioTrack("CodingWithMitch Podcast#2 - Justin Mitchel", Resources.AUDIO_JUSTIN_MITCHEL));
    }

    public void bindService(){
        if(!isAudioServiceRunning()){
            Intent serviceBindIntent =  new Intent(this, StreamingService.class);
            bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(TAG, "ServiceConnection: connected to service.");
            mAudioServiceMessenger = new Messenger(iBinder);
            isAudioServiceBound = true;
            mIncomingMessenger = new Messenger(new IncomingMessageHandler());
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "ServiceConnection: disconnected from service.");
            mAudioServiceMessenger = null;
            isAudioServiceBound = false;
        }

    };

        @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called.");
        super.onStop();
        if (mAudioServiceMessenger != null) {
            unbindService(serviceConnection);
            mAudioServiceMessenger = null;
            mIncomingMessenger = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startAudioStreamingService();
    }

    private void startAudioStreamingService(){
        if(!isAudioServiceRunning()){
            Intent serviceIntent = new Intent(this, StreamingService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                StreamingActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }

            bindService();

        }
    }

    private boolean isAudioServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.codingwithmitch.application.StreamingService".equals(service.service.getClassName())) {
                Log.d(TAG, "isMyServiceRunning: service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isMyServiceRunning: service is not running.");
        return false;
    }



    private void initRecyclerView(){
        mAudioTracksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAudioTrackRecyclerAdapter = new AudioTrackRecyclerAdapter(this, mAudioTracks);
        mAudioTracksRecyclerView.setAdapter(mAudioTrackRecyclerAdapter);
    }

    @Override
    public void onTrackSelected(int position) {
        mSelectedAudioTrack = mAudioTracks.get(position);
        mAudioTrackTitle.setText(mAudioTracks.get(position).getTitle());
        playSelectedTrack();
    }

    private void playSelectedTrack(){
            mPlaybackState = PLAYBACK_PLAY;
            setPlaybackIcon();
            sendMessageToService(Constants.MSG_PLAY);
    }

    private void resumeSelectedTrack(){
            mPlaybackState = PLAYBACK_PLAY;
            setPlaybackIcon();
            sendMessageToService(Constants.MSG_RESUME);
    }

    private void pauseSelectedTrack(){
            mPlaybackState = PLAYBACK_PAUSE;
            setPlaybackIcon();
            sendMessageToService(Constants.MSG_PAUSE);
    }

    private void sendMessageToService(int playback){

        Message msgResponse = Message.obtain(null, playback);

        if(playback == Constants.MSG_PLAY){
            Bundle bundle = new Bundle();
            bundle.putParcelable("audio_track", mSelectedAudioTrack);
            msgResponse.setData(bundle);
        }
        else if(playback == Constants.MSG_PAUSE){
            // no bundle in this case
        }

        try {
            msgResponse.replyTo = mIncomingMessenger;
            mAudioServiceMessenger.send(msgResponse);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void setPlaybackIcon(){
        if(mPlaybackState == PLAYBACK_PAUSE){
            mPlaybackIcon.setBackgroundResource(R.drawable.ic_play_circle_outline_black_24dp);
        }
        else if(mPlaybackState == PLAYBACK_PLAY){
            mPlaybackIcon.setBackgroundResource(R.drawable.ic_pause_circle_outline_black_24dp);
        }
        else if(mPlaybackState == PLAYBACK_APP_LAUNCH){
            mPlaybackIcon.setBackgroundResource(R.drawable.ic_play_circle_outline_black_24dp);
        }
    }

    @Override
    public void onClick(View view) {
            switch (view.getId()){
                case R.id.playback_button:{
                    if(mPlaybackState == PLAYBACK_PLAY){
                        pauseSelectedTrack();
                    }
                    else if(mPlaybackState == PLAYBACK_PAUSE){
                        resumeSelectedTrack();
                    }
                    else if(mPlaybackState == PLAYBACK_APP_LAUNCH){
                        playSelectedTrack();
                    }
                    break;
                }
            }
    }

    private void startPlaybackTracker(){
        mPlaybackProgressHandlerThread = new HandlerThread("Playback Progress Handler Thread");
        mPlaybackProgressHandlerThread.start();

        final Handler handler = new Handler(mPlaybackProgressHandlerThread.getLooper());

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Message message = Message.obtain(null, Constants.MSG_PROGRESS);
                try {
                    message.replyTo = mIncomingMessenger;
                    mAudioServiceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, 250);
            }
        };
        handler.postDelayed(runnable, 250);


    }

    private void stopPlaybackTracker(){
        if(mPlaybackProgressHandlerThread != null){
            mPlaybackProgressHandlerThread.quit();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopPlaybackTracker();
        unregisterReceiver(mPlaybackBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPlaybackTracker();
        registerPlaybackBroadcastReceiver();
    }

    private void registerPlaybackBroadcastReceiver(){
        IntentFilter playbackIntentFilter = new IntentFilter();
        playbackIntentFilter.addAction("action.playback.state.change");
        registerReceiver(mPlaybackBroadcastReceiver, playbackIntentFilter);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(b){
            Message message = Message.obtain(null, Constants.MSG_SEEK);
            message.arg1 = i; // seekbar position
            try {
                mAudioServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        stopPlaybackTracker();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mPlaybackState == PLAYBACK_PLAY){
            resumeSelectedTrack();
        }
        startPlaybackTracker();
    }

    private BroadcastReceiver mPlaybackBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mPlaybackState != PLAYBACK_APP_LAUNCH){
                Log.d(TAG, "onReceive: called.");
                mPlaybackState = PLAYBACK_PAUSE;
                setPlaybackIcon();
            }
        }
    };
}












